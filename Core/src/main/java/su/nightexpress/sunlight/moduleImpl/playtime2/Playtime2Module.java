package su.nightexpress.sunlight.moduleImpl.playtime2;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.api.event.PlaytimeAdjustedEvent;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.moduleImpl.playtime2.command.Playtime2CommandProvider;
import su.nightexpress.sunlight.moduleImpl.playtime2.config.Playtime2Lang;
import su.nightexpress.sunlight.moduleImpl.playtime2.data.Playtime2Day;
import su.nightexpress.sunlight.moduleImpl.playtime2.data.Playtime2DayDataManager;
import su.nightexpress.sunlight.moduleImpl.playtime2.listener.Playtime2Listener;
import su.nightexpress.sunlight.moduleImpl.playtime2.menu.Playtime2CalendarMenu;
import su.nightexpress.sunlight.moduleImpl.playtime2.menu.Playtime2StatsMenu;
import su.nightexpress.sunlight.moduleImpl.playtime2.menu.Playtime2TopMenu;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

/**
 * Playtime tracking whose source of truth is one row per played day.
 * <p>
 * The daily history lives in its own table and is cached per online player.
 * Every period total, both streaks, the days-played count and the calendar are
 * derived from that history; the counters stored on the user are only a cache
 * of it, so reads stay cheap and the leaderboard can rank offline players
 * without loading their rows.
 */
public class Playtime2Module extends Module {

    public static final int TOP_PAGE_SIZE = 10;
    public static final int TOP_CACHE_LIMIT = 100;

    private final Playtime2Settings settings;
    private final Playtime2DayDataManager dataManager;
    private final Playtime2Placeholders placeholders;
    private final Playtime2TopHandler topHandler;
    private final Playtime2MilestoneHandler milestoneHandler;
    private PlaytimeProviderImpl playtimeProvider;

    private final Map<UUID, Playtime2History> histories = new ConcurrentHashMap<>();

    private volatile PeriodKeys keysCache;
    private volatile long keysCacheTime;

    public Playtime2Module(final ModuleDefinition<Playtime2Module> definition, final SunLightPlugin plugin) {
        super(definition, plugin);
        this.settings = new Playtime2Settings();
        this.dataManager = new Playtime2DayDataManager(this.dataHandler);
        this.placeholders = new Playtime2Placeholders(this);
        this.topHandler = new Playtime2TopHandler(this);
        this.milestoneHandler = new Playtime2MilestoneHandler(this);
    }

    @Override
    protected void loadModule(final FileConfig config) {
        this.settings.load(config);
        this.milestoneHandler.loadMilestones(config);
        config.saveChanges();
        this.plugin.injectLang(Playtime2Lang.class);

        this.dataManager.init();

        // Must happen before any SunUser is deserialised.
        UserPropertyRegistry.register(Playtime2Properties.TOTAL);
        UserPropertyRegistry.register(Playtime2Properties.ARCHIVED_TOTAL);
        UserPropertyRegistry.register(Playtime2Properties.YEAR);
        UserPropertyRegistry.register(Playtime2Properties.MONTH);
        UserPropertyRegistry.register(Playtime2Properties.WEEK);
        UserPropertyRegistry.register(Playtime2Properties.DAY);
        UserPropertyRegistry.register(Playtime2Properties.DAY_KEY);
        UserPropertyRegistry.register(Playtime2Properties.WEEK_KEY);
        UserPropertyRegistry.register(Playtime2Properties.MONTH_KEY);
        UserPropertyRegistry.register(Playtime2Properties.YEAR_KEY);
        UserPropertyRegistry.register(Playtime2Properties.DAILY_STREAK);
        UserPropertyRegistry.register(Playtime2Properties.WEEKLY_STREAK);
        UserPropertyRegistry.register(Playtime2Properties.DAYS_PLAYED);
        UserPropertyRegistry.register(Playtime2Properties.FIRST_PLAYED_DAY);
        UserPropertyRegistry.register(Playtime2Properties.LAST_PLAYED_DAY);
        UserPropertyRegistry.register(Playtime2Properties.LAST_SEEN);
        UserPropertyRegistry.register(Playtime2Properties.REWARDED_DAY_KEY);
        UserPropertyRegistry.register(Playtime2Properties.REWARDED_WEEK_KEY);
        UserPropertyRegistry.register(Playtime2Properties.REWARDED_MONTH_KEY);
        UserPropertyRegistry.register(Playtime2Properties.REMINDED_DAY_KEY);
        UserPropertyRegistry.register(Playtime2Properties.REMINDED_WEEK_KEY);
        UserPropertyRegistry.register(Playtime2Properties.REMINDED_MONTH_KEY);
        UserPropertyRegistry.register(Playtime2Properties.SESSION);
        UserPropertyRegistry.register(Playtime2Properties.SESSION_START);
        UserPropertyRegistry.register(Playtime2Properties.MILESTONE_PROGRESS);

        this.addListener(new Playtime2Listener(this.plugin, this));
        this.addTask(this::tick, this.settings.getTickInterval());
        this.addAsyncTask(this::flushAll, this.settings.getSaveInterval());

        this.commandRegistry.addProvider(new Playtime2CommandProvider(this));

        // A reload leaves players online without a join event, so their history has
        // to be loaded here. The database work stays off the main thread.
        final List<Player> online = new ArrayList<>(Utils.onlinePlayers());
        this.plugin.runTaskAsync(() -> {
            for (final Player player : online) {
                try {
                    this.loadHistory(player.getUniqueId());
                } catch (final Exception exception) {
                    this.debug("Playtime history load failed for " + player.getName() + ": " + exception.getMessage());
                }
            }
            this.plugin.runTask(() -> online.forEach(this::resumeSession));
        });
        this.topHandler.refreshTopCacheAsync();
    }

    @Override
    protected void unloadModule() {
        this.flushAll();
        this.histories.clear();
        this.topHandler.unload();
        this.milestoneHandler.unload();
    }

    public void reloadModule() {
        final FileConfig config = this.getConfig();
        this.settings.load(config);
        this.keysCache = null;
        this.milestoneHandler.loadMilestones(config);
        config.saveChanges();
        this.topHandler.refreshTopCacheAsync();
    }

    @Override
    public void registerPlaceholders(final PlaceholderRegistry registry) {
        this.placeholders.registerPlaceholders(registry);
    }

    public Playtime2Settings getSettings() {
        return this.settings;
    }

    public PlaytimeProviderImpl provider() {
        if (playtimeProvider == null) {
            playtimeProvider = new PlaytimeProviderImpl(this);
        }
        return playtimeProvider;
    }

    public Playtime2TopHandler topHandler() {
        return this.topHandler;
    }

    public Playtime2MilestoneHandler milestoneHandler() {
        return this.milestoneHandler;
    }

    // --- History lifecycle ---

    /**
     * Loads, prunes and caches one player's history. Runs on whatever thread
     * calls it, so the join path keeps the database work off the main thread.
     */
    public Playtime2History loadHistory(final UUID playerId) {
        final Playtime2History existing = this.histories.get(playerId);
        if (existing != null) {
            return existing;
        }

        final Playtime2History history = new Playtime2History(playerId);
        for (final Playtime2Day row : this.dataManager.load(playerId)) {
            if (row != null && row.getMillis() > 0L) {
                history.set(row.getDay(), row.getMillis());
            }
        }
        history.markClean();

        final SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
        if (user != null) {
            history.setArchivedTotal(user.getPropertyOrDefault(Playtime2Properties.ARCHIVED_TOTAL));
        }
        this.prune(history, user);
        history.markAccrual(System.currentTimeMillis());

        final Playtime2History raced = this.histories.putIfAbsent(playerId, history);
        return raced == null ? history : raced;
    }

    /** Drops the cached history of a player who just left, after flushing it. */
    public void releaseHistory(final UUID playerId) {
        final Playtime2History history = this.histories.get(playerId);
        if (history == null) {
            return;
        }
        this.accrueRemainder(playerId, history);
        this.flush(history);
        this.histories.remove(playerId, history);
    }

    /**
     * Credits the part of the current tick interval the player was still online
     * for. Without this a short session would be rounded down to nothing, which
     * matters because the interval is a minute rather than a second.
     */
    private void accrueRemainder(final UUID playerId, final Playtime2History history) {
        final long now = System.currentTimeMillis();
        synchronized (history) {
            final long last = history.getLastAccrualAt();
            if (last <= 0L) {
                history.markAccrual(now);
                return;
            }
            final long elapsed = now - last;
            final long interval = this.settings.getTickInterval() * 50L;
            final long remainder = Math.min(elapsed, interval);
            history.markAccrual(now);
            if (remainder <= 0L) {
                return;
            }

            final Player player = Bukkit.getPlayer(playerId);
            final boolean excludeAfk = this.settings.isExcludeAfkEnabled();
            final var afkProvider = this.plugin.afkProvider();
            if (player != null && excludeAfk && afkProvider.isPresent() && afkProvider.get().isAfk(player)) {
                return;
            }

            history.add(this.currentKeys().day(), remainder);
            final SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
            if (user != null) {
                user.setProperty(Playtime2Properties.SESSION,
                        user.getPropertyOrDefault(Playtime2Properties.SESSION) + remainder);
                this.writeCaches(user, history);
            }
        }
    }

    public Playtime2History history(final UUID playerId) {
        return this.histories.get(playerId);
    }

    /**
     * The cached history when the player is online, otherwise a freshly loaded
     * one. The loaded copy is cached straight away, so a player who is online
     * but whose join load has not finished yet cannot end up with two copies.
     */
    public Playtime2History historyOrLoad(final UUID playerId) {
        final Playtime2History cached = this.histories.get(playerId);
        if (cached != null) {
            return cached;
        }
        final Playtime2History history = new Playtime2History(playerId);
        for (final Playtime2Day row : this.dataManager.load(playerId)) {
            if (row != null && row.getMillis() > 0L) {
                history.set(row.getDay(), row.getMillis());
            }
        }
        history.markClean();

        final Playtime2History raced = this.histories.putIfAbsent(playerId, history);
        if (raced != null) {
            return raced;
        }
        history.markAccrual(System.currentTimeMillis());
        // Nobody else holds it, so this caller is responsible for dropping it
        // again once the player is confirmed offline.
        if (Bukkit.getPlayer(playerId) == null) {
            this.plugin.runTaskLater(() -> this.releaseIfOffline(playerId, history), 1L);
        }
        return history;
    }

    private void releaseIfOffline(final UUID playerId, final Playtime2History history) {
        if (Bukkit.getPlayer(playerId) != null) {
            return;
        }
        if (this.histories.get(playerId) != history) {
            return;
        }
        this.plugin.runTaskAsync(() -> {
            if (Bukkit.getPlayer(playerId) == null) {
                this.releaseHistory(playerId);
            }
        });
    }

    private void prune(final Playtime2History history, final SunUser user) {
        final long retention = this.settings.getRetentionDays();
        if (retention <= 0L) {
            return;
        }
        final long cutoff = this.currentKeys().day() - retention;
        final long folded = history.prune(cutoff);
        if (folded <= 0L) {
            return;
        }
        this.dataManager.deleteOlderThan(history.getPlayerId(), cutoff);
        if (user != null) {
            user.setProperty(Playtime2Properties.ARCHIVED_TOTAL, history.getArchivedTotal());
            this.writeCaches(user, history);
        }
    }

    private void resumeSession(final Player player) {
        try {
            final SunUser user = this.userManager.getOrFetch(player);
            if (user == null) {
                return;
            }
            final Playtime2History history = this.histories.get(player.getUniqueId());
            if (history != null) {
                this.writeCaches(user, history);
            }
            if (user.getPropertyOrDefault(Playtime2Properties.SESSION_START) <= 0L) {
                user.setProperty(Playtime2Properties.SESSION_START, System.currentTimeMillis());
            }
        } catch (final Exception exception) {
            this.debug("Playtime init failed for " + player.getName() + ": " + exception.getMessage());
        }
    }

    // --- Tick ---

    private void tick() {
        final long deltaMs = this.settings.getTickInterval() * 50L;
        if (deltaMs <= 0L) {
            return;
        }

        final boolean excludeAfk = this.settings.isExcludeAfkEnabled();
        final var afkProvider = this.plugin.afkProvider();
        final PeriodKeys keys = this.currentKeys();
        final long now = System.currentTimeMillis();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            try {
                final SunUser user = this.userManager.getOrFetch(player);
                if (user == null) {
                    continue;
                }
                final Playtime2History history = this.histories.get(player.getUniqueId());
                if (history == null) {
                    // The async join load has not finished yet; skip rather than block.
                    continue;
                }

                final long lastSeen = user.getPropertyOrDefault(Playtime2Properties.LAST_SEEN);
                if (now - lastSeen >= 60_000L) {
                    user.setProperty(Playtime2Properties.LAST_SEEN, now);
                }

                if (excludeAfk && afkProvider.isPresent() && afkProvider.get().isAfk(player)) {
                    continue;
                }

                synchronized (history) {
                    history.add(keys.day(), deltaMs);
                    history.markAccrual(now);
                    user.setProperty(Playtime2Properties.SESSION,
                            user.getPropertyOrDefault(Playtime2Properties.SESSION) + deltaMs);
                    this.writeCaches(user, history, keys);
                }

                this.milestoneHandler.checkGoals(player, user);
                this.milestoneHandler.checkMilestones(player, user);
            } catch (final Exception exception) {
                // One corrupt record must never kill the tick for everyone else.
                this.debug("Playtime tick failed for " + player.getName() + ": " + exception.getMessage());
            }
        }
    }

    // --- Persistence ---

    private void flushAll() {
        for (final Playtime2History history : this.histories.values()) {
            try {
                this.flush(history);
            } catch (final Exception exception) {
                this.debug("Playtime flush failed for " + history.getPlayerId() + ": " + exception.getMessage());
            }
        }
    }

    private void flush(final Playtime2History history) {
        final Set<Long> dirty;
        final List<Playtime2Day> upserts = new ArrayList<>();
        final List<Long> deletes = new ArrayList<>();
        synchronized (history) {
            if (!history.isDirty()) {
                return;
            }
            dirty = history.dirtyDays();
            for (final long day : dirty) {
                final long millis = history.getMillis(day);
                if (millis > 0L) {
                    upserts.add(new Playtime2Day(history.getPlayerId(), day, millis));
                } else {
                    deletes.add(day);
                }
            }
            history.markClean(dirty);
        }

        try {
            this.dataManager.save(upserts);
            for (final long day : deletes) {
                this.dataManager.deleteRange(history.getPlayerId(), day, day);
            }
        } catch (final RuntimeException exception) {
            // The write failed, so the days go back into the dirty set and the next
            // flush retries them.
            synchronized (history) {
                history.markDirty(dirty);
            }
            throw exception;
        }
    }

    // --- Period keys ---

    public record PeriodKeys(long day, long week, long month, long year) {
    }

    public PeriodKeys currentKeys() {
        final long now = System.currentTimeMillis();

        final PeriodKeys cached = this.keysCache;
        if (cached != null && now - this.keysCacheTime < 1000L) {
            return cached;
        }
        final ZonedDateTime dateTime = ZonedDateTime.now(TimeUtil.getZoneId());
        final PeriodKeys keys = new PeriodKeys(
                dateTime.toLocalDate().toEpochDay(),
                dateTime.toLocalDate().with(DayOfWeek.MONDAY).toEpochDay(),
                (long) dateTime.getYear() * 12L + (dateTime.getMonthValue() - 1),
                dateTime.getYear());
        this.keysCache = keys;
        this.keysCacheTime = now;
        return keys;
    }

    // --- Reads ---

    /**
     * Rewrites every cached counter from the history. Called after anything that
     * changes the history, so the stored counters never drift from the rows.
     */
    public void writeCaches(final SunUser user, final Playtime2History history) {
        this.writeCaches(user, history, this.currentKeys());
    }

    private void writeCaches(final SunUser user, final Playtime2History history, final PeriodKeys keys) {
        user.setProperty(Playtime2Properties.TOTAL, history.total());
        user.setProperty(Playtime2Properties.ARCHIVED_TOTAL, history.getArchivedTotal());
        user.setProperty(Playtime2Properties.YEAR, history.year(keys.day()));
        user.setProperty(Playtime2Properties.MONTH, history.month(keys.day()));
        user.setProperty(Playtime2Properties.WEEK, history.week(keys.day()));
        user.setProperty(Playtime2Properties.DAY, history.getMillis(keys.day()));

        user.setProperty(Playtime2Properties.DAY_KEY, keys.day());
        user.setProperty(Playtime2Properties.WEEK_KEY, keys.week());
        user.setProperty(Playtime2Properties.MONTH_KEY, keys.month());
        user.setProperty(Playtime2Properties.YEAR_KEY, keys.year());

        user.setProperty(Playtime2Properties.DAILY_STREAK, history.dailyStreak(keys.day()));
        user.setProperty(Playtime2Properties.WEEKLY_STREAK, history.weeklyStreak(keys.day()));
        user.setProperty(Playtime2Properties.DAYS_PLAYED, history.daysPlayed());

        final Long first = history.firstDay();
        final Long last = history.lastDay();
        user.setProperty(Playtime2Properties.FIRST_PLAYED_DAY, first == null ? 0L : first);
        user.setProperty(Playtime2Properties.LAST_PLAYED_DAY, last == null ? 0L : last);
    }

    public long getEffectivePlaytime(final SunUser user, final PlaytimePeriod period) {
        if (user == null) {
            return 0L;
        }
        if (period == PlaytimePeriod.SESSION) {
            return Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.SESSION));
        }

        // Online players answer from the live history, so a tick that has not
        // rewritten the caches yet is still visible.
        final Playtime2History history = this.histories.get(user.getId());
        if (history != null) {
            return this.readHistory(history, period);
        }
        return this.readCached(user, period);
    }

    private long readHistory(final Playtime2History history, final PlaytimePeriod period) {
        final PeriodKeys keys = this.currentKeys();
        return switch (period == null ? PlaytimePeriod.ALLTIME : period) {
            case ALLTIME -> history.total();
            case YEAR -> history.year(keys.day());
            case MONTH -> history.month(keys.day());
            case WEEK -> history.week(keys.day());
            case DAY -> history.getMillis(keys.day());
            case SESSION -> 0L;
        };
    }

    private long readCached(final SunUser user, final PlaytimePeriod period) {
        final PeriodKeys keys = this.currentKeys();
        return switch (period == null ? PlaytimePeriod.ALLTIME : period) {
            case ALLTIME -> Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.TOTAL));
            case YEAR -> user.getPropertyOrDefault(Playtime2Properties.YEAR_KEY) == keys.year()
                    ? Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.YEAR))
                    : 0L;
            case MONTH -> user.getPropertyOrDefault(Playtime2Properties.MONTH_KEY) == keys.month()
                    ? Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.MONTH))
                    : 0L;
            case WEEK -> user.getPropertyOrDefault(Playtime2Properties.WEEK_KEY) == keys.week()
                    ? Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.WEEK))
                    : 0L;
            case DAY -> user.getPropertyOrDefault(Playtime2Properties.DAY_KEY) == keys.day()
                    ? Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.DAY))
                    : 0L;
            case SESSION -> Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.SESSION));
        };
    }

    public long getEffectivePlaytime(final Player player, final PlaytimePeriod period) {
        if (player == null) {
            return 0L;
        }
        final SunUser user = this.userManager.getOrFetch(player);
        return user == null ? 0L : this.getEffectivePlaytime(user, period);
    }

    /**
     * Playtime between two epoch days, inclusive. Loads the history when needed.
     */
    public long getPlaytime(final UUID playerId, final long fromDay, final long toDay) {
        final Playtime2History history = this.historyOrLoad(playerId);
        synchronized (history) {
            return history.sum(fromDay, toDay);
        }
    }

    public int getDailyStreak(final SunUser user) {
        final Playtime2History history = this.histories.get(user.getId());
        if (history != null) {
            return history.dailyStreak(this.currentKeys().day());
        }
        return Math.max(0, user.getPropertyOrDefault(Playtime2Properties.DAILY_STREAK));
    }

    public int getWeeklyStreak(final SunUser user) {
        final Playtime2History history = this.histories.get(user.getId());
        if (history != null) {
            return history.weeklyStreak(this.currentKeys().day());
        }
        return Math.max(0, user.getPropertyOrDefault(Playtime2Properties.WEEKLY_STREAK));
    }

    public int getDaysPlayed(final SunUser user) {
        final Playtime2History history = this.histories.get(user.getId());
        if (history != null) {
            return history.daysPlayed();
        }
        return Math.max(0, user.getPropertyOrDefault(Playtime2Properties.DAYS_PLAYED));
    }

    /** First played epoch day, or {@code 0} when the player has no history. */
    public long getFirstPlayedDay(final SunUser user) {
        final Playtime2History history = this.histories.get(user.getId());
        if (history != null) {
            final Long first = history.firstDay();
            return first == null ? 0L : first;
        }
        return Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.FIRST_PLAYED_DAY));
    }

    /** Last played epoch day, or {@code 0} when the player has no history. */
    public long getLastPlayedDay(final SunUser user) {
        final Playtime2History history = this.histories.get(user.getId());
        if (history != null) {
            final Long last = history.lastDay();
            return last == null ? 0L : last;
        }
        return Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.LAST_PLAYED_DAY));
    }

    /** The day grid of one month, keyed by epoch day. Empty days are absent. */
    public Map<Long, Long> monthGrid(final UUID playerId, final YearMonth month) {
        final Playtime2History history = this.historyOrLoad(playerId);
        synchronized (history) {
            return history.monthGrid(month);
        }
    }

    public void startSession(final SunUser user) {
        final Playtime2History history = this.histories.get(user.getId());
        if (history != null) {
            synchronized (history) {
                this.writeCaches(user, history);
            }
        }
        user.setProperty(Playtime2Properties.SESSION, 0L);
        user.setProperty(Playtime2Properties.SESSION_START, System.currentTimeMillis());
        user.setProperty(Playtime2Properties.LAST_SEEN, System.currentTimeMillis());
    }

    public void dispatchRewardCommands(final Player player, final List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        final PlaceholderContext context = PlaceholderContext.builder()
                .with(CommonPlaceholders.PLAYER.resolver(player))
                .andThen(SLPlaceholders.forPlayerWithPAPI(player))
                .build();

        final List<String> resolved = new ArrayList<>(commands);
        resolved.replaceAll(command -> context.apply(command
                .replace("%player_name%", player.getName())
                .replace("%player_display_name%", player.getDisplayName())));

        final CommandSender console = this.plugin.getServer().getConsoleSender();
        this.plugin.runTask(() -> resolved
                .forEach(command -> this.plugin.getServer().dispatchCommand(console, command)));
    }

    // --- Admin operations ---

    /**
     * Adds playtime to today. The period argument of the old command is gone:
     * every period total is a sum over the daily history, so crediting today
     * moves all of them.
     */
    public void addPlaytime(final SunUser user, final long deltaMs) {
        if (deltaMs == 0L || user == null) {
            return;
        }
        final Playtime2History history = this.historyOrLoad(user.getId());
        final long today = this.currentKeys().day();

        final long previous;
        final long next;
        synchronized (history) {
            previous = history.getMillis(today);
            next = Math.max(0L, previous + deltaMs);
        }

        final PlaytimeAdjustedEvent event = new PlaytimeAdjustedEvent(user, PlaytimeAdjustedEvent.Operation.ADD,
                PlaytimePeriod.DAY, null, previous, next);
        this.plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        synchronized (history) {
            history.set(today, next);
            this.persist(user, history);
        }
    }

    /** Sets today's playtime to an absolute value. */
    public void setPlaytime(final SunUser user, final long valueMs) {
        if (user == null) {
            return;
        }
        final long value = Math.max(0L, valueMs);
        final Playtime2History history = this.historyOrLoad(user.getId());
        final long today = this.currentKeys().day();

        final long previous;
        synchronized (history) {
            previous = history.getMillis(today);
        }

        final PlaytimeAdjustedEvent event = new PlaytimeAdjustedEvent(user, PlaytimeAdjustedEvent.Operation.SET,
                PlaytimePeriod.DAY, null, previous, value);
        this.plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        synchronized (history) {
            history.set(today, value);
            this.persist(user, history);
        }
    }

    public void resetScope(final SunUser user, final String scope) {
        final String normalized = Utils.lowercase(scope);
        if (normalized == null || user == null) {
            return;
        }

        final PlaytimePeriod period = switch (normalized) {
            case "all", "alltime", "total" -> PlaytimePeriod.ALLTIME;
            case "year" -> PlaytimePeriod.YEAR;
            case "month" -> PlaytimePeriod.MONTH;
            case "week" -> PlaytimePeriod.WEEK;
            case "day" -> PlaytimePeriod.DAY;
            default -> null;
        };

        final Playtime2History history = this.historyOrLoad(user.getId());
        final long previous;
        synchronized (history) {
            previous = period == null ? 0L : this.readHistory(history, period);
        }

        final PlaytimeAdjustedEvent event = new PlaytimeAdjustedEvent(user, PlaytimeAdjustedEvent.Operation.RESET,
                period, normalized, previous, 0L);
        this.plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        final PeriodKeys keys = this.currentKeys();
        synchronized (history) {
            switch (normalized) {
                case "all" -> {
                    history.clearAll();
                    user.setProperty(Playtime2Properties.SESSION, 0L);
                    user.setProperty(Playtime2Properties.REWARDED_DAY_KEY, 0L);
                    user.setProperty(Playtime2Properties.REWARDED_WEEK_KEY, 0L);
                    user.setProperty(Playtime2Properties.REWARDED_MONTH_KEY, 0L);
                    user.setProperty(Playtime2Properties.REMINDED_DAY_KEY, 0L);
                    user.setProperty(Playtime2Properties.REMINDED_WEEK_KEY, 0L);
                    user.setProperty(Playtime2Properties.REMINDED_MONTH_KEY, 0L);
                    user.setProperty(Playtime2Properties.MILESTONE_PROGRESS, new HashMap<String, Integer>());
                    this.dataManager.deleteAll(user.getId());
                    history.markClean();
                }
                case "alltime", "total" -> {
                    history.clearAll();
                    this.dataManager.deleteAll(user.getId());
                    history.markClean();
                }
                case "year" -> this.clearRange(history, this.yearStart(keys), keys.day());
                case "month" -> {
                    this.clearRange(history, this.monthStart(keys), keys.day());
                    user.setProperty(Playtime2Properties.REWARDED_MONTH_KEY, 0L);
                    user.setProperty(Playtime2Properties.REMINDED_MONTH_KEY, 0L);
                }
                case "week" -> {
                    this.clearRange(history, keys.week(), keys.day());
                    user.setProperty(Playtime2Properties.REWARDED_WEEK_KEY, 0L);
                    user.setProperty(Playtime2Properties.REMINDED_WEEK_KEY, 0L);
                }
                case "day" -> {
                    this.clearRange(history, keys.day(), keys.day());
                    user.setProperty(Playtime2Properties.REWARDED_DAY_KEY, 0L);
                    user.setProperty(Playtime2Properties.REMINDED_DAY_KEY, 0L);
                }
                case "session" -> {
                    user.setProperty(Playtime2Properties.SESSION, 0L);
                    user.setProperty(Playtime2Properties.SESSION_START, System.currentTimeMillis());
                    return;
                }
                case "milestones", "milestone" -> {
                    user.setProperty(Playtime2Properties.MILESTONE_PROGRESS, new HashMap<String, Integer>());
                    return;
                }
                default -> {
                    return;
                }
            }
            this.writeCaches(user, history, keys);
        }
    }

    private void clearRange(final Playtime2History history, final long fromDay, final long toDay) {
        history.clearRange(fromDay, toDay);
        this.dataManager.deleteRange(history.getPlayerId(), fromDay, toDay);
    }

    private long yearStart(final PeriodKeys keys) {
        return LocalDate.ofEpochDay(keys.day()).withDayOfYear(1).toEpochDay();
    }

    private long monthStart(final PeriodKeys keys) {
        return LocalDate.ofEpochDay(keys.day()).withDayOfMonth(1).toEpochDay();
    }

    /**
     * Writes the caches and the changed rows immediately. Online players are
     * also covered by the periodic flush, so a failed write here is retried.
     */
    private void persist(final SunUser user, final Playtime2History history) {
        this.writeCaches(user, history);

        final Set<Long> dirty = history.dirtyDays();
        final List<Playtime2Day> upserts = new ArrayList<>();
        final List<Long> deletes = new ArrayList<>();
        for (final long day : dirty) {
            final long millis = history.getMillis(day);
            if (millis > 0L) {
                upserts.add(new Playtime2Day(history.getPlayerId(), day, millis));
            } else {
                deletes.add(day);
            }
        }
        history.markClean(dirty);
        try {
            this.dataManager.save(upserts);
            for (final long day : deletes) {
                this.dataManager.deleteRange(history.getPlayerId(), day, day);
            }
        } catch (final RuntimeException exception) {
            history.markDirty(dirty);
            throw exception;
        }
    }

    // --- Display ---

    public boolean openStatsMenu(final Player viewer, final SunUser target) {
        return new Playtime2StatsMenu(this, target.getId()).show(this.plugin, viewer);
    }

    public boolean openTopMenu(final Player viewer, final PlaytimePeriod period) {
        return new Playtime2TopMenu(this, period).show(this.plugin, viewer);
    }

    public boolean openCalendarMenu(final Player viewer, final SunUser target, final YearMonth month) {
        return new Playtime2CalendarMenu(this, target.getId(), month).show(this.plugin, viewer);
    }

    /** Days the player has a recorded row for. Retained rows only. */
    public int getDaysPlayed(final UUID playerId) {
        final SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
        return user == null ? 0 : this.getDaysPlayed(user);
    }

    public long getFirstPlayedDay(final UUID playerId) {
        final SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
        return user == null ? 0L : this.getFirstPlayedDay(user);
    }

    public long getLastPlayedDay(final UUID playerId) {
        final SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
        return user == null ? 0L : this.getLastPlayedDay(user);
    }

    /**
     * Playtime per epoch day inside {@code [fromDay, toDay]}. Days without a row
     * are absent rather than zero.
     */
    public Map<Long, Long> getHistory(final UUID playerId, final long fromDay, final long toDay) {
        final Playtime2History history = this.historyOrLoad(playerId);
        final Map<Long, Long> result = new HashMap<>();
        synchronized (history) {
            for (final Map.Entry<Long, Long> entry : history.getDays().entrySet()) {
                if (entry.getKey() >= fromDay && entry.getKey() <= toDay) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    /** Exposed for the leaderboard scan, which ranks every stored user. */

}
