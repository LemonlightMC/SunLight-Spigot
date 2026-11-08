package su.nightexpress.sunlight.moduleImpl.afk;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import su.nightexpress.nightcore.bridge.chat.UniversalChatEvent;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.SLUtils;
import su.nightexpress.sunlight.api.event.AfkChangeEvent;
import su.nightexpress.sunlight.api.event.AfkEnterEvent;
import su.nightexpress.sunlight.api.event.AfkLeaveEvent;
import su.nightexpress.sunlight.hook.HookId;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkLang;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkPerms;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkSettings;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.Utils;

public class AfkHandler {
    private final AfkModule module;
    private final AfkSettings settings;

    private final Map<UUID, ActivityTracker> activityTrackerMap;
    private final Map<UUID, Long> kickedForIdling;
    private Set<String> exemptWorldsLower;
    private Set<String> exemptRegionsLower;
    private Particle enterParticle;
    private Particle exitParticle;
    private long statisticsSessions;
    private long statisticsTotalAfkTime;

    public AfkHandler(final AfkModule module) {
        this.module = module;
        this.settings = module.getSettings();
        this.activityTrackerMap = new ConcurrentHashMap<>();
        this.kickedForIdling = new ConcurrentHashMap<>();
        this.exemptWorldsLower = Set.of();
        this.exemptRegionsLower = Set.of();
    }

    public void rebuildExemptCaches() {
        this.exemptWorldsLower = this.settings.exemptWorlds.get().stream()
                .filter(world -> world != null && !world.isBlank())
                .map(world -> world.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        this.exemptRegionsLower = this.settings.exemptRegions.get().stream()
                .filter(region -> region != null && !region.isBlank())
                .map(region -> region.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        this.enterParticle = this.resolveParticle(this.settings.particlesEnterType.get());
        this.exitParticle = this.resolveParticle(this.settings.particlesExitType.get());
    }

    private Particle resolveParticle(final String typeName) {
        if (typeName == null || typeName.isBlank() || "NONE".equalsIgnoreCase(typeName.trim())) {
            return null;
        }
        try {
            return Particle.valueOf(typeName.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    protected void shutdown() {
        this.activityTrackerMap.clear();
        this.kickedForIdling.clear();
    }

    public boolean isAfk(final Player player) {
        final ActivityTracker tracker = this.activityTracker(player);
        return tracker == null ? false : tracker.isAfk();
    }

    public void track(final Player player) {
        this.activityTrackerMap.put(player.getUniqueId(), new ActivityTracker(this.settings));
    }

    public void untrack(final Player player, final boolean silent) {
        this.exitAfk(player, silent);
        this.activityTrackerMap.remove(player.getUniqueId());
    }

    public void exitAfk(final Player player, final boolean silent) {
        final ActivityTracker tracker = this.activityTracker(player);
        // Only reset the counters if the transition really happened. A listener that
        // cancelled AfkChangeEvent leaves the player AFK, and wiping their idle time
        // here would restart the kick timers mid-session.
        if (tracker.isAfk()) {
            if (this.handleAfkExit(player, tracker, silent)) {
                tracker.resetCounters();
            }
        }
    }

    public void enterAfk(final Player player, final boolean silent) {
        final ActivityTracker tracker = this.activityTracker(player);
        if (!tracker.isAfk()) {
            this.handleAfkEnter(player, tracker, silent);
        }
    }

    public void trackActivity(final Player player, final ActivityType type) {
        final ActivityTracker tracker = this.activityTracker(player);
        if (tracker != null) {
            tracker.countActivity(type);
        }
    }

    public void trackActivity(final Player player, final int amount) {
        final ActivityTracker tracker = this.activityTracker(player);
        if (tracker != null) {
            tracker.countActivity(amount);
        }
    }

    public boolean isExempt(final Player player) {
        if (player.hasPermission(AfkPerms.BYPASS)) {
            return true;
        }
        if (this.settings.vanishIgnorePlayers.get() && this.isVanishedPlayer(player)) {
            return true;
        }

        if (this.isExemptWorld(player.getWorld().getName())) {
            return true;
        }

        if (!HookId.hasWorldGuard() || !Utils.isInstalled(HookId.WORLD_GUARD)) {
            return false;
        }
        if (this.settings.exemptRegions.get().isEmpty()) {
            return false;
        }

        return this.isInExemptRegion(player);
    }

    private boolean isVanishedPlayer(final Player player) {
        return module.plugin().vanishProvider().map(provider -> provider.isVanished(player))
                .orElseGet(() -> SLUtils.isVanished(player));
    }

    private void updateSleepingIgnored(final Player player, final boolean afk) {
        if (!this.settings.ignoreSleeping.get())
            return;
        try {
            player.setSleepingIgnored(afk);
        } catch (final Exception ignored) {
        }
    }

    private boolean shouldIgnoreBypassMovement(final Player player, final Location location) {
        if (!this.settings.bypassDetectionEnabled.get())
            return false;

        if (this.settings.bypassIgnoreVehicle.get() && player.isInsideVehicle()) {
            return true;
        }

        if (this.settings.bypassIgnoreWaterFlow.get() && this.isInFlowingWater(location)) {
            return true;
        }

        return false;
    }

    private boolean isInFlowingWater(final Location location) {
        final Block block = location.getBlock();
        if (!block.isLiquid())
            return false;
        final Material type = block.getType();
        if (type != Material.WATER && type != Material.BUBBLE_COLUMN)
            return false;

        final BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
        for (final BlockFace face : faces) {
            final Block adjacent = block.getRelative(face);
            if (adjacent.getType() == Material.WATER || adjacent.getType() == Material.BUBBLE_COLUMN) {
                final Block beyond = adjacent.getRelative(face);
                if (beyond.getType() == Material.WATER || beyond.getType() == Material.BUBBLE_COLUMN) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExemptWorld(final String worldName) {
        return worldName != null && !this.exemptWorldsLower.isEmpty()
                && this.exemptWorldsLower.contains(worldName.toLowerCase(Locale.ROOT));
    }

    private boolean isInExemptRegion(final Player player) {
        if (this.exemptRegionsLower.isEmpty()) {
            return false;
        }
        try {
            final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            final ApplicableRegionSet set = container.createQuery()
                    .getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
            for (final ProtectedRegion region : set) {
                if (this.exemptRegionsLower.contains(region.getId().toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        } catch (final Exception exception) {
            module.warn("Failed to check WorldGuard exempt regions for " + player.getName() + ": " + exception.getMessage());
        }
        return false;
    }

    public void tickTrackers() {
        if (this.activityTrackerMap.isEmpty()) {
            return;
        }
        this.pruneKickRecords();

        final Iterator<Map.Entry<UUID, ActivityTracker>> it = this.activityTrackerMap.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<UUID, ActivityTracker> entry = it.next();
            final ActivityTracker tracker = entry.getValue();
            final Player player = Utils.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                it.remove();
                continue;
            }

            if (this.isExempt(player)) {
                if (tracker.isAfk()) {
                    this.exitAfk(player, true);
                }
                tracker.resetCounters();
                continue;
            }

            final Location location = player.getLocation();
            final boolean bypassMovement = this.shouldIgnoreBypassMovement(player, location);
            final boolean trackRotation = !bypassMovement && this.settings.movementTrackRotation.get();
            tracker.updatePosition(BlockPos.from(location), location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch(), !bypassMovement, trackRotation);
            tracker.tick();

            final int idleTime = tracker.getIdleTime();
            final int timeToKick = this.getTimeToKick(player);
            final int afkKickTime = this.getAfkKickTime(player);

            if (tracker.isAfk()) {
                if (tracker.isEnoughActivity()) {
                    this.handleAfkExit(player, tracker, false);
                    continue;
                }

                if (timeToKick > 0 && idleTime >= timeToKick) {
                    this.kickForIdling(player);
                    continue;
                }

                final long afkDuration = this.getAfkDuration(player);
                if (afkKickTime > 0 && afkDuration >= afkKickTime) {
                    this.kickForIdling(player);
                    continue;
                }

                this.sendKickWarning(player, tracker, idleTime, afkDuration, timeToKick, afkKickTime);
                this.sendStatusBar(player, tracker);
            } else {
                if (timeToKick > 0 && idleTime >= timeToKick) {
                    this.kickForIdling(player);
                    continue;
                }

                final int timeToAfk = this.getTimeToAfk(player);
                if (timeToAfk > 0 && idleTime >= timeToAfk) {
                    this.handleAfkEnter(player, tracker, false);
                }
            }
        }
    }

    private void pruneKickRecords() {
        final int window = this.settings.rejoinKickAfter.get();

        if (window == 0) {
            this.kickedForIdling.clear();
            return;
        }
        if (window < 0)
            return;

        this.kickedForIdling.values().removeIf(kickedAt -> System.currentTimeMillis() - kickedAt > window * 1000L);
    }

    public void handleChatEvent(final UniversalChatEvent event) {
        if (event.isCancelled())
            return;

        final Player player = event.getPlayer();
        final boolean afk = this.isAfk(player);
        if (this.settings.blockChat.get() && afk) {
            event.setCancelled(true);
            module.sendPrefixed(AfkLang.ERROR_ACTION_BLOCKED, player);
            return;
        }

        final int points = this.settings.getActivityPoints(ActivityType.CHAT);
        if (points > 0) {
            this.trackActivity(player, points);
        }
    }

    private boolean handleAfkEnter(final Player player, final ActivityTracker tracker, final boolean silent) {
        final int idleTime = tracker.getIdleTime();

        // Fired before anything is applied, so cancelling leaves the player awake and
        // skips every
        // side effect below (commands, sound, broadcast, AFK session start).
        final AfkChangeEvent changeEvent = new AfkChangeEvent(player, true);
        module.plugin().getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled())
            return false;

        tracker.resetCounters();
        tracker.setAfkTimestamp();
        this.updateSleepingIgnored(player, true);

        final PlaceholderContext context = PlaceholderContext.builder()
                .with(CommonPlaceholders.PLAYER.resolver(player))
                .andThen(CommonPlaceholders.forPlaceholderAPI(player))
                .build();

        final List<String> commands = context.apply(this.settings.afkCommands.get());
        Players.dispatchCommands(player, commands);

        module.plugin().getPluginManager().callEvent(new AfkEnterEvent(player, idleTime));

        this.playAfkEffects(player, true);
        if (module.luckPermsHook != null)
            module.luckPermsHook.signalUpdate(player);

        final boolean announce = !silent
                && !(this.settings.vanishSuppressBroadcasts.get() && this.isVanishedPlayer(player));
        if (announce) {
            module.broadcastPrefixed(AfkLang.AFK_ENTER_BROADCAST, context);
            if (module.discordHook != null)
                module.discordHook.sendEnter(player, idleTime);
        }

        return true;
    }

    private boolean handleAfkExit(final Player player, final ActivityTracker tracker, final boolean silent) {
        final long afkDuration = System.currentTimeMillis() - tracker.getAfkEnterTimestamp();

        // Fired before anything is applied, so cancelling leaves the player AFK and
        // skips every
        // side effect below — notably the AFK session is not recorded and no wake-up
        // commands run.
        final AfkChangeEvent changeEvent = new AfkChangeEvent(player, false);
        module.plugin().getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled())
            return false;

        this.updateSleepingIgnored(player, false);

        if (afkDuration > 0L) {
            this.recordAfkSession(player, afkDuration);
        }

        final PlaceholderContext context = PlaceholderContext.builder()
                .with(SLPlaceholders.GENERIC_TIME,
                        () -> TimeFormats.formatSince(tracker.getAfkEnterTimestamp(), TimeFormatType.LITERAL))
                .with(CommonPlaceholders.PLAYER.resolver(player))
                .andThen(CommonPlaceholders.forPlaceholderAPI(player))
                .build();

        final List<String> commands = context.apply(this.settings.wakeUpCommands.get());
        Players.dispatchCommands(player, commands);

        module.plugin().getPluginManager().callEvent(new AfkLeaveEvent(player, afkDuration));

        this.playAfkEffects(player, false);
        if (module.luckPermsHook != null)
            module.luckPermsHook.signalUpdate(player);

        final boolean announce = !silent
                && !(this.settings.vanishSuppressBroadcasts.get() && this.isVanishedPlayer(player));
        if (announce) {
            module.broadcastPrefixed(AfkLang.AFK_EXIT_BROADCAST, context);
            if (module.discordHook != null)
                module.discordHook.sendExit(player, afkDuration);
        }

        tracker.resetCounters();
        return true;
    }

    private void recordAfkSession(final Player player, final long durationMs) {
        final SunUser user = module.userManager().getOrFetch(player);
        user.setProperty(AfkModule.AFK_TOTAL_TIME, user.getPropertyOrDefault(AfkModule.AFK_TOTAL_TIME) + durationMs);
        user.setProperty(AfkModule.AFK_COUNT, user.getPropertyOrDefault(AfkModule.AFK_COUNT) + 1);
        this.statisticsSessions++;
        this.statisticsTotalAfkTime += Math.max(0L, durationMs);
    }

    private void playAfkEffects(final Player player, final boolean enter) {
        (enter ? this.settings.soundEnter.get() : this.settings.soundExit.get()).play(player);

        final boolean enabled = enter ? this.settings.particlesEnterEnabled.get()
                : this.settings.particlesExitEnabled.get();
        if (!enabled)
            return;

        final Particle particle = enter ? this.enterParticle : this.exitParticle;
        if (particle == null)
            return;

        final int count = Math.max(1,
                enter ? this.settings.particlesEnterCount.get() : this.settings.particlesExitCount.get());
        final double offset = Math.max(0D,
                enter ? this.settings.particlesEnterOffset.get() : this.settings.particlesExitOffset.get());
        final Location location = player.getLocation().clone().add(0D, 1D, 0D);

        try {
            player.getWorld().spawnParticle(particle, location, count, offset, 0.45D, offset, 0D);
        } catch (final Exception ignored) {
            // Some particles require extra data; ignore misconfiguration safely.
        }
    }

    public void kickForIdling(final Player player) {
        final String idleFormatted = TimeFormats.formatAmount(
                TimeUnit.MILLISECONDS.convert(this.getIdleTime(player), TimeUnit.SECONDS),
                TimeFormatType.LITERAL);
        final String reason = PlaceholderContext.builder()
                .with(SLPlaceholders.GENERIC_TIME, () -> idleFormatted)
                .andThen(SLPlaceholders.forPlayerWithPAPI(player))
                .build()
                .apply(String.join("\n", this.settings.kickText.get()));

        if (this.settings.rejoinKickAfter.get() != 0) {
            this.kickedForIdling.put(player.getUniqueId(), System.currentTimeMillis());
        }

        if (module.discordHook != null) {
            module.discordHook.sendKick(player, idleFormatted);
        }
        Players.kick(player, reason);
    }

    /**
     * @return seconds until the player is kicked for idling/AFK, or {@code -1} if no kick applies.
     */
    public int getSecondsUntilKick(final Player player) {
        final ActivityTracker tracker = this.activityTracker(player);
        if (tracker == null) {
            return -1;
        }
        return this.getSecondsUntilKick(player, tracker, tracker.getIdleTime(), this.getAfkDuration(player));
    }

    private int getSecondsUntilKick(final Player player, final ActivityTracker tracker, final int idleTime,
            final long afkDurationSeconds) {
        int remaining = -1;
        final int totalKick = this.getTimeToKick(player);
        if (totalKick > 0) {
            remaining = Math.max(0, totalKick - idleTime);
        }
        final int afkKick = this.getAfkKickTime(player);
        if (afkKick > 0 && tracker.isAfk() && afkDurationSeconds < afkKick) {
            final int afkRemaining = (int) Math.max(0L, afkKick - afkDurationSeconds);
            remaining = remaining < 0 ? afkRemaining : Math.min(remaining, afkRemaining);
        }
        return remaining;
    }

    public boolean isWithinRejoinKickWindow(final Player player) {
        final int window = this.settings.rejoinKickAfter.get();
        if (window == 0)
            return false;

        final Long kickedAt = this.kickedForIdling.remove(player.getUniqueId());
        if (kickedAt == null)
            return false;

        if (window < 0)
            return true;

        return System.currentTimeMillis() - kickedAt <= window * 1000L;
    }

    public long getStatisticsSessions() {
        return this.statisticsSessions;
    }

    public long getStatisticsTotalAfkTime() {
        return this.statisticsTotalAfkTime;
    }

    public long getStatisticsAverageAfkTime() {
        return this.statisticsSessions <= 0L ? 0L : this.statisticsTotalAfkTime / this.statisticsSessions;
    }

    private void sendKickWarning(final Player player, final ActivityTracker tracker, final int idleTime,
            final long afkDuration, final int timeToKick, final int afkKickTime) {
        final int ahead = this.settings.kickWarningAhead.get();
        if (ahead <= 0)
            return;

        int remaining = -1;
        if (timeToKick > 0 && idleTime < timeToKick) {
            remaining = timeToKick - idleTime;
        }
        if (afkKickTime > 0 && afkDuration < afkKickTime) {
            final int afkRemaining = (int) (afkKickTime - afkDuration);
            remaining = remaining < 0 ? afkRemaining : Math.min(remaining, afkRemaining);
        }
        if (remaining <= 0 || remaining > ahead)
            return;

        final long now = System.currentTimeMillis();
        final int interval = this.settings.kickWarningInterval.get();
        if (interval > 0 && now - tracker.getLastKickWarningSent() < interval * 1000L)
            return;

        tracker.setLastKickWarningSent(now);

        final int timeLeft = remaining;
        module.sendPrefixed(AfkLang.KICK_WARNING, player, builder -> builder
                .with(SLPlaceholders.GENERIC_TIME, () -> TimeFormats.formatAmount(
                        TimeUnit.MILLISECONDS.convert(timeLeft, TimeUnit.SECONDS), TimeFormatType.LITERAL)));
    }

    private void sendStatusBar(final Player player, final ActivityTracker tracker) {
        if (tracker.isWakingUp() && this.settings.afkWakeUpBarEnabled.get()) {
            Players.sendActionBar(player, PlaceholderContext.builder()
                    .with(SLPlaceholders.GENERIC_REMAIN, () -> String.valueOf(tracker.getWakeUpProgress()))
                    .with(SLPlaceholders.GENERIC_TIME_LEFT,
                            () -> TimeFormats.formatAmount(
                                    TimeUnit.MILLISECONDS.convert(tracker.getWakeUpTimeLeft(), TimeUnit.SECONDS),
                                    TimeFormatType.LITERAL))
                    .andThen(SLPlaceholders.forPlayerWithPAPI(player))
                    .build()
                    .apply(this.settings.afkWakeUpBarText.get()));
            return;
        }

        if (this.settings.afkStatusBarEnabled.get()) {
            Players.sendActionBar(player, PlaceholderContext.builder()
                    .with(SLPlaceholders.GENERIC_TIME,
                            () -> TimeFormats.formatSince(tracker.getAfkEnterTimestamp(), TimeFormatType.LITERAL))
                    .andThen(SLPlaceholders.forPlayerWithPAPI(player))
                    .build()
                    .apply(this.settings.afkStatusBarText.get()));
        }
    }

    public Set<ActivityTracker> getActivityTrackers() {
        return new HashSet<>(this.activityTrackerMap.values());
    }

    public int getAfkPlayerCount() {
        int count = 0;
        for (final Map.Entry<UUID, ActivityTracker> entry : this.activityTrackerMap.entrySet()) {
            if (entry.getValue().isAfk() && Utils.getPlayer(entry.getKey()) != null) {
                count++;
            }
        }
        return count;
    }

    public List<Player> getAfkPlayers() {
        return this.activityTrackerMap.entrySet().stream()
                .filter(entry -> entry.getValue().isAfk())
                .map(entry -> Utils.getPlayer(entry.getKey()))
                .filter(obj -> obj != null)
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public ActivityTracker activityTracker(final Player player) {
        return activityTrackerMap.get(player.getUniqueId());
    }

    public int getTimeToAfk(final Player player) {
        return this.settings.idleAfkTimes.get().getGreatest(player).intValue();
    }

    public int getTimeToKick(final Player player) {
        return this.settings.idleKickTimes.get().getGreatest(player).intValue();
    }

    public int getAfkKickTime(final Player player) {
        return this.settings.afkKickTimes.get().getGreatest(player).intValue();
    }

    public int getIdleTime(final Player player) {
        final ActivityTracker tracker = this.activityTracker(player);
        return tracker != null ? tracker.getIdleTime() : 0;
    }

    public long getAfkEnterTimestamp(final Player player) {
        final ActivityTracker tracker = this.activityTracker(player);
        return tracker != null ? tracker.getAfkEnterTimestamp() : 0;
    }

    public long getAfkDuration(final Player player) {
        final ActivityTracker tracker = this.activityTracker(player);
        if (tracker == null) {
            return 0;
        }
        return TimeUnit.SECONDS.convert(tracker.duration(), TimeUnit.MILLISECONDS);
    }

    public long getTotalAfkTime(final Player player) {
        return module.userManager().getOrFetch(player).getPropertyOrDefault(AfkModule.AFK_TOTAL_TIME);
    }

    public int getAfkCount(final Player player) {
        return module.userManager().getOrFetch(player).getPropertyOrDefault(AfkModule.AFK_COUNT);
    }
}
