package su.nightexpress.sunlight.moduleImpl.timer.service;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.timer.model.*;

import java.util.*;

public class TimerDisplayService {

    private final SunLightPlugin plugin;
    private final XpBarService xpBarService;
    private final Map<String, BossBar> bossBars = new HashMap<>();
    private final Map<String, Long> chatMarkers = new HashMap<>();

    public TimerDisplayService(SunLightPlugin plugin, XpBarService xpBarService) {
        this.plugin = plugin;
        this.xpBarService = xpBarService;
    }

    public void tick(TimerInstance instance) {
        if (instance == null) return;
        List<Player> viewers = this.resolveViewers(instance);
        String timerKey = timerKey(instance);
        DisplayRule rule = this.selectRule(instance);

        if (rule == null || instance.getState() == TimerState.STOPPED || instance.getState() == TimerState.COMPLETED) {
            this.clearTimer(instance);
            return;
        }

        Set<DisplayChannel> channels = rule.getChannels() == null ? Set.of() : rule.getChannels();
        this.handleBossBar(timerKey, rule, instance, viewers, channels.contains(DisplayChannel.BOSS_BAR));
        if (!channels.contains(DisplayChannel.XP_BAR) || !instance.getDefinition().isRestoreXpOnStop()) {
            // still may need to handle XP restore when channel not active for this rule? requirement says restore prior xp on stop.
        }

        for (Player player : viewers) {
            PlaceholderContext context = PlaceholderContext.builder()
                    .with(CommonPlaceholders.PLAYER.resolver(player))
                    .andThen(SLPlaceholders.forPlayerWithPAPI(player))
                    .build();
            if (channels.contains(DisplayChannel.ACTION_BAR)) {
                Players.sendActionBar(player, context.apply(TimerFormat.apply(rule.getActionBar(), instance)));
            }
            if (channels.contains(DisplayChannel.TITLE) || channels.contains(DisplayChannel.SUBTITLE)) {
                String title = channels.contains(DisplayChannel.TITLE) ? context.apply(TimerFormat.apply(rule.getTitle(), instance)) : "";
                String sub = channels.contains(DisplayChannel.SUBTITLE) ? context.apply(TimerFormat.apply(rule.getSubtitle(), instance)) : "";
                if (channels.contains(DisplayChannel.TITLE) || channels.contains(DisplayChannel.SUBTITLE)) {
                    player.sendTitle(NightMessage.asLegacy(title), NightMessage.asLegacy(sub), 0, Math.max(25, rule.getUpdateIntervalTicks() + 5), 5);
                }
            }
            if (channels.contains(DisplayChannel.CHAT)) {
                long secondKey = instance.getDefinition().getKind() == TimerKind.COUNTDOWN ? instance.getRemainingMillis() / 1000L : instance.getElapsedMillis() / 1000L;
                long interval = Math.max(1, rule.getChatIntervalSeconds());
                String markerKey = timerKey + ":" + player.getUniqueId() + ":" + rule.getId();
                Long last = this.chatMarkers.get(markerKey);
                if (last == null || (secondKey % interval == 0 && !last.equals(secondKey))) {
                    String msg = context.apply(TimerFormat.apply(rule.getChat(), instance));
                    if (!msg.isBlank()) Players.sendMessage(player, msg);
                    this.chatMarkers.put(markerKey, secondKey);
                }
            }
            if (channels.contains(DisplayChannel.XP_BAR)) {
                this.xpBarService.apply(player, instance, timerKey);
            }
            else if (this.xpBarService.isOwnedBy(player.getUniqueId(), timerKey)) {
                this.xpBarService.restore(player, timerKey);
            }
        }

        // restore XP for viewers no longer targeted by XP_BAR
        if (channels.contains(DisplayChannel.XP_BAR)) {
            Set<UUID> visible = new HashSet<>();
            for (Player player : viewers) visible.add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!visible.contains(online.getUniqueId()) && this.xpBarService.isOwnedBy(online.getUniqueId(), timerKey)) {
                    this.xpBarService.restore(online, timerKey);
                }
            }
        }
        else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (this.xpBarService.isOwnedBy(online.getUniqueId(), timerKey)) {
                    this.xpBarService.restore(online, timerKey);
                }
            }
        }
    }

    public DisplayRule selectRule(TimerInstance instance) {
        List<DisplayRule> rules = new ArrayList<>(instance.getDefinition().getDisplayRules());
        rules.sort(Comparator.comparingInt(DisplayRule::getPriority).reversed());
        for (DisplayRule rule : rules) {
            if (rule != null && rule.matches(instance)) return rule;
        }
        return null;
    }

    public List<Player> resolveViewers(TimerInstance instance) {
        if (instance.isPersonal()) {
            if (instance.getOwnerId() == null) return List.of();
            Player owner = Bukkit.getPlayer(instance.getOwnerId());
            if (owner == null || !owner.isOnline()) return List.of();
            return List.of(owner);
        }
        TimerVisibility visibility = instance.getDefinition().getVisibility();
        VisibilityMode mode = visibility == null ? VisibilityMode.ALL : visibility.getMode();
        String permission = visibility == null ? "" : visibility.getPermission();
        List<Player> result = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (mode == VisibilityMode.PERMISSION && permission != null && !permission.isBlank() && !player.hasPermission(permission)) continue;
            result.add(player);
        }
        return result;
    }

    private void handleBossBar(String timerKey, DisplayRule rule, TimerInstance instance, List<Player> viewers, boolean enabled) {
        BossBar bar = this.bossBars.get(timerKey);
        if (!enabled) {
            if (bar != null) {
                bar.removeAll();
                this.bossBars.remove(timerKey);
            }
            return;
        }
        if (bar == null) {
            bar = Bukkit.createBossBar(NightMessage.asLegacy(TimerFormat.apply(rule.getBossBar(), instance)), parseColor(rule.getBossBarColor()), parseStyle(rule.getBossBarOverlay()));
            this.bossBars.put(timerKey, bar);
        }
        bar.setTitle(NightMessage.asLegacy(TimerFormat.apply(rule.getBossBar(), instance)));
        bar.setProgress(Math.max(0D, Math.min(1D, TimerFormat.progress(instance))));
        try { bar.setColor(parseColor(rule.getBossBarColor())); } catch (Exception ignored) {}
        try { bar.setStyle(parseStyle(rule.getBossBarOverlay())); } catch (Exception ignored) {}
        Set<Player> current = new HashSet<>(viewers);
        for (Player viewer : new ArrayList<>(bar.getPlayers())) {
            if (!current.contains(viewer)) bar.removePlayer(viewer);
        }
        for (Player player : viewers) {
            if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
        }
    }

    public void clearTimer(TimerInstance instance) {
        String timerKey = timerKey(instance);
        BossBar bar = this.bossBars.remove(timerKey);
        if (bar != null) bar.removeAll();
        this.chatMarkers.entrySet().removeIf(e -> e.getKey().startsWith(timerKey + ":"));
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (this.xpBarService.isOwnedBy(online.getUniqueId(), timerKey)) {
                this.xpBarService.restore(online, timerKey);
            }
        }
    }

    public void clearTimerForOwner(UUID ownerId) {
        if (ownerId == null) return;
        String prefix = "P:" + ownerId + ":";
        List<String> keys = new ArrayList<>();
        for (String key : this.bossBars.keySet()) if (key.startsWith(prefix)) keys.add(key);
        for (String key : keys) {
            BossBar bar = this.bossBars.remove(key);
            if (bar != null) bar.removeAll();
            this.chatMarkers.entrySet().removeIf(e -> e.getKey().startsWith(key + ":"));
        }
        Player player = Bukkit.getPlayer(ownerId);
        if (player != null) this.xpBarService.clear(player);
        else this.xpBarService.restore(Bukkit.getOfflinePlayer(ownerId).isOnline() ? Bukkit.getPlayer(ownerId) : null, null);
    }

    public void shutdown(Iterable<? extends Player> players) {
        for (BossBar bar : this.bossBars.values()) bar.removeAll();
        this.bossBars.clear();
        this.chatMarkers.clear();
        this.xpBarService.restoreAll(players);
    }

    public static String timerKey(TimerInstance instance) {
        return instance.isGlobal() ? "G:" + instance.getId() : "P:" + instance.getOwnerId() + ":" + instance.getId();
    }

    private static BarColor parseColor(String raw) {
        if (raw == null) return BarColor.YELLOW;
        try { return BarColor.valueOf(raw.trim().toUpperCase(Locale.ROOT)); } catch (Exception ignored) { return BarColor.YELLOW; }
    }

    private static BarStyle parseStyle(String raw) {
        if (raw == null) return BarStyle.SOLID;
        try { return BarStyle.valueOf(raw.trim().toUpperCase(Locale.ROOT)); } catch (Exception ignored) { return BarStyle.SOLID; }
    }
}
