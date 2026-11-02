package su.nightexpress.sunlight.moduleImpl.playtime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToLongFunction;

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
import su.nightexpress.sunlight.moduleImpl.playtime.command.PlaytimeCommandProvider;
import su.nightexpress.sunlight.moduleImpl.playtime.config.PlaytimeLang;
import su.nightexpress.sunlight.moduleImpl.playtime.data.PlaytimeDay;
import su.nightexpress.sunlight.moduleImpl.playtime.data.PlaytimeDayDataManager;
import su.nightexpress.sunlight.moduleImpl.playtime.listener.PlaytimeListener;
import su.nightexpress.sunlight.moduleImpl.playtime.menu.PlaytimeCalendarMenu;
import su.nightexpress.sunlight.moduleImpl.playtime.menu.PlaytimeStatsMenu;
import su.nightexpress.sunlight.moduleImpl.playtime.menu.PlaytimeTopMenu;
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
public class PlaytimeModule extends Module {

    public static final int TOP_PAGE_SIZE = 10;
    public static final int TOP_CACHE_LIMIT = 100;

    private final PlaytimeSettings settings;
    private final PlaytimeDayDataManager dataManager;
    private final PlaytimePlaceholders placeholders;
    private final PlaytimeTopHandler topHandler;
    private final PlaytimeMilestoneHandler milestoneHandler;
    private PlaytimeProviderImpl playtimeProvider;

    private final Map<UUID, PlaytimeHistory> histories = new ConcurrentHashMap<>();

    private volatile PeriodKeys keysCache;
    private volatile long keysCacheTime;

    public PlaytimeModule(final ModuleDefinition<PlaytimeModule> definition, final SunLightPlugin plugin) {
        super(definition, plugin);
        this.settings = new PlaytimeSettings();
        this.dataManager = new PlaytimeDayDataManager(this.dataHandler);
        this.placeholders = new PlaytimePlaceholders(this);
        this.topHandler = new PlaytimeTopHandler(this);
        this.milestoneHandler = new PlaytimeMilestoneHandler(this);
    }

    @Override
    protected void loadModule(final FileConfig config) {
        UserPropertyRegistry.register(PlaytimeProperties.TOTAL);
        UserPropertyRegistry.register(PlaytimeProperties.ARCHIVED_TOTAL);
        UserPropertyRegistry.register(PlaytimeProperties.DAILY_STREAK);
        UserPropertyRegistry.register(PlaytimeProperties.WEEKLY_STREAK);
        UserPropertyRegistry.register(PlaytimeProperties.DAYS_PLAYED);
        UserPropertyRegistry.register(PlaytimeProperties.FIRST_PLAYED_DAY);
        UserPropertyRegistry.register(PlaytimeProperties.LAST_PLAYED_DAY);
        UserPropertyRegistry.register(PlaytimeProperties.LAST_SEEN);
        UserPropertyRegistry.register(PlaytimeProperties.REWARDED_DAY_KEY);
        UserPropertyRegistry.register(PlaytimeProperties.REWARDED_WEEK_KEY);
        UserPropertyRegistry.register(PlaytimeProperties.REWARDED_MONTH_KEY);
        UserPropertyRegistry.register(PlaytimeProperties.REMINDED_DAY_KEY);
        UserPropertyRegistry.register(PlaytimeProperties.REMINDED_WEEK_KEY);
        UserPropertyRegistry.register(PlaytimeProperties.REMINDED_MONTH_KEY);
        UserPropertyRegistry.register(PlaytimeProperties.SESSION_START);
        UserPropertyRegistry.register(PlaytimeProperties.MILESTONE_PROGRESS);

        this.settings.load(config);
        this.milestoneHandler.loadMilestones(config);
        config.saveChanges();
        this.plugin.injectLang(PlaytimeLang.class);

        this.dataManager.init();

        this.addListener(new PlaytimeListener(this.plugin, this));
        this.addTask(this::tick, this.settings.getTickInterval());
        this.addAsyncTask(this::flushAll, this.settings.getSaveInterval());

        this.commandRegistry.addProvider(new PlaytimeCommandProvider(this));

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

    public PlaytimeSettings getSettings() {
        return this.settings;
    }

    public PlaytimeProviderImpl provider() {
        if (playtimeProvider == null) {
            playtimeProvider = new PlaytimeProviderImpl(this);
        }
        return playtimeProvider;
    }

    public PlaytimeTopHandler topHandler() {
        return this.topHandler;
    }

    public PlaytimeMilestoneHandler milestoneHandler() {
        return this.milestoneHandler;
    }

    /**
     * Loads, prunes and caches one player's history. Runs on whatever thread
     * calls it, so the join path keeps the database work off the main thread.
     */
    public PlaytimeHistory loadHistory(final UUID playerId) {
        final PlaytimeHistory existing = this.histories.get(playerId);
        if (existing != null) {
            return existing;
        }

        final PlaytimeHistory history = new PlaytimeHistory(playerId, this.dataManager.load(playerId));
        final SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
        if (user != null) {
            history.setArchivedTotal(user.getPropertyOrDefault(PlaytimeProperties.ARCHIVED_TOTAL));
            // Preserve a all-time total (stored directly in playtime_total) when
            // there is no daily history to derive it from.
            if (history.isEmpty() && user.getPropertyOrDefault(PlaytimeProperties.ARCHIVED_TOTAL) <= 0L
                    && user.getPropertyOrDefault(PlaytimeProperties.TOTAL) > 0L) {
                history.setArchivedTotal(user.getPropertyOrDefault(PlaytimeProperties.TOTAL));
            }
        }
        this.prune(history, user);

        final PlaytimeHistory raced = this.histories.putIfAbsent(playerId, history);
        return raced == null ? history : raced;
    }

    /**
     * Drops the cached history of a player who is no longer tracked, after
     * flushing it. Deliberately does not credit anything: this also runs for
     * histories that were only loaded to answer a read, and an offline player
     * must not gain playtime from being looked at.
     */
    public void releaseHistory(final UUID playerId) {
        final PlaytimeHistory history = this.histories.get(playerId);
        if (history == null) {
            return;
        }
        this.flush(history);
        this.histories.remove(playerId, history);
    }

    /**
     * Ends a tracked session: credits the part of the current tick interval the
     * player was still online for, then flushes and drops the history. Without
     * the credit a short session would be rounded down to nothing, which matters
     * because the interval is a minute rather than a second.
     */
    public void closeSession(final UUID playerId, final boolean wasAfkAtQuit) {
        final PlaytimeHistory history = this.histories.get(playerId);
        if (history == null) {
            return;
        }
        this.accrueRemainder(playerId, history, wasAfkAtQuit);
        this.flush(history);
        // Do not evict the history while the player is online again: a fast
        // reconnect would otherwise leave them untracked.
        if (org.bukkit.Bukkit.getPlayer(playerId) == null) {
            this.histories.remove(playerId, history);
        }
    }

    /**
     * Credits the part of the current tick interval the player was still online
     * for. Without this a short session would be rounded down to nothing, which
     * matters because the interval is a minute rather than a second.
     */
    private void accrueRemainder(final UUID playerId, final PlaytimeHistory history, final boolean wasAfkAtQuit) {
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

            final boolean excludeAfk = this.settings.isExcludeAfkEnabled();
            final var afkProvider = this.plugin.afkProvider();
            if (excludeAfk && afkProvider.isPresent()) {
                if (wasAfkAtQuit) {
                    return;
                }
                final Player player = Bukkit.getPlayer(playerId);
                if (player != null && afkProvider.get().isAfk(player)) {
                    return;
                }
            }

            history.add(this.currentKeys().day(), remainder);
            final SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
            if (user != null) {
                this.writeCaches(user, history);
            }
        }
    }

    /**
     * The cached history when the player is online, otherwise a freshly loaded
     * one. The loaded copy is cached straight away, so a player who is online
     * but whose join load has not finished yet cannot end up with two copies.
     */
    public PlaytimeHistory historyOrLoad(final UUID playerId) {
        final PlaytimeHistory cached = this.histories.get(playerId);
        if (cached != null) {
            return cached;
        }

        final PlaytimeHistory history = this.loadHistory(playerId);
        history.markAccrual(System.currentTimeMillis());
        // Nobody else holds it, so this caller is responsible for dropping it
        // again once the player is confirmed offline.
        if (this.histories.get(playerId) == history && Bukkit.getPlayer(playerId) == null) {
            this.plugin.runTaskLater(() -> this.releaseIfOffline(playerId, history), 1L);
        }
        return history;
    }

    private void releaseIfOffline(final UUID playerId, final PlaytimeHistory history) {
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

    private void prune(final PlaytimeHistory history, final SunUser user) {
        final long retention = this.settings.getRetentionDays();
        if (retention <= 0L) {
            return;
        }
        final long cutoff = this.currentKeys().day() - retention + 1L;
        final long folded = history.prune(null, cutoff, true);
        if (folded <= 0L) {
            return;
        }
        this.dataManager.deleteOlderThan(history.getPlayerId(), cutoff);
        if (user != null) {
            user.setProperty(PlaytimeProperties.ARCHIVED_TOTAL, history.getArchivedTotal());
            this.writeCaches(user, history);
        }
    }

    private void resumeSession(final Player player) {
        try {
            final SunUser user = this.userManager.getOrFetch(player);
            if (user == null) {
                return;
            }
            final PlaytimeHistory history = this.histories.get(player.getUniqueId());
            if (history != null) {
                synchronized (history) {
                    this.writeCaches(user, history);
                }
            }
            if (user.getPropertyOrDefault(PlaytimeProperties.SESSION_START) <= 0L) {
                user.setProperty(PlaytimeProperties.SESSION_START, System.currentTimeMillis());
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
                final PlaytimeHistory history = this.histories.get(player.getUniqueId());
                if (history == null) {
                    // The async join load has not finished yet; skip rather than block.
                    continue;
                }

                final long lastSeen = user.getPropertyOrDefault(PlaytimeProperties.LAST_SEEN);
                if (now - lastSeen >= 60_000L) {
                    user.setProperty(PlaytimeProperties.LAST_SEEN, now);
                }

                if (excludeAfk && afkProvider.isPresent() && afkProvider.get().isAfk(player)) {
                    continue;
                }

                synchronized (history) {
                    history.add(keys.day(), deltaMs);
                    history.markAccrual(now);
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
        for (final PlaytimeHistory history : this.histories.values()) {
            try {
                this.flush(history);
            } catch (final Exception exception) {
                this.debug("Playtime flush failed for " + history.getPlayerId() + ": " + exception.getMessage());
            }
        }
    }

    private void flush(final PlaytimeHistory history) {
        final Set<Long> dirty;
        final List<PlaytimeDay> upserts = new ArrayList<>();
        final List<Long> deletes = new ArrayList<>();
        synchronized (history) {
            if (!history.isDirty()) {
                return;
            }
            dirty = history.dirtyDays();
            for (final long day : dirty) {
                final long millis = history.getMillis(day);
                if (millis > 0L) {
                    upserts.add(new PlaytimeDay(history.getPlayerId(), day, millis));
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
     * Rewrites the stored values that cannot be recomputed on demand. The period
     * totals are not among them: they are read from the history directly.
     */
    public void writeCaches(final SunUser user, final PlaytimeHistory history) {
        this.writeCaches(user, history, this.currentKeys());
    }

    private void writeCaches(final SunUser user, final PlaytimeHistory history, final PeriodKeys keys) {
        user.setProperty(PlaytimeProperties.TOTAL, history.total());
        user.setProperty(PlaytimeProperties.ARCHIVED_TOTAL, history.getArchivedTotal());

        user.setProperty(PlaytimeProperties.DAILY_STREAK, history.dailyStreak(keys.day()));
        user.setProperty(PlaytimeProperties.WEEKLY_STREAK, history.weeklyStreak(keys.day()));
        user.setProperty(PlaytimeProperties.DAYS_PLAYED, history.daysPlayed());

        final Long first = history.firstDay();
        final Long last = history.lastDay();
        user.setProperty(PlaytimeProperties.FIRST_PLAYED_DAY, first == null ? 0L : first);
        user.setProperty(PlaytimeProperties.LAST_PLAYED_DAY, last == null ? 0L : last);
    }

    public long getEffectivePlaytime(final SunUser user, final PlaytimePeriod period) {
        return this.getEffectivePlaytime(user, period, null);
    }

    public long getEffectivePlaytime(final SunUser user, final PlaytimePeriod period,
            final ToLongFunction<PlaytimePeriod> scanned) {
        if (user == null) {
            return 0L;
        }
        if (period == PlaytimePeriod.SESSION) {
            return this.getSessionPlaytime(user);
        }
        final PlaytimePeriod resolved = period == null ? PlaytimePeriod.ALLTIME : period;

        final PlaytimeHistory cached = this.histories.get(user.getId());
        if (cached != null) {
            synchronized (cached) {
                return cached.getMillis(this.currentKeys().day(), resolved);
            }
        }
        if (resolved == PlaytimePeriod.ALLTIME) {
            return Math.max(0L, user.getPropertyOrDefault(PlaytimeProperties.TOTAL));
        }
        if (scanned != null) {
            return Math.max(0L, scanned.applyAsLong(resolved));
        }
        return this.historyOrLoad(user.getId()).getMillis(this.currentKeys().day(), resolved);
    }

    /**
     * Length of the current session, derived from its start rather than
     * accumulated, so no counter can drift from the clock. A player who is not
     * online has no running session.
     */
    public long getSessionPlaytime(final SunUser user) {
        if (user == null || !this.histories.containsKey(user.getId())
                || org.bukkit.Bukkit.getPlayer(user.getId()) == null) {
            return 0L;
        }
        final long start = user.getPropertyOrDefault(PlaytimeProperties.SESSION_START);
        if (start <= 0L) {
            return 0L;
        }
        return Math.max(0L, System.currentTimeMillis() - start);
    }

    /**
     * The period id used as the reward stamp, so a goal pays once per period.
     * Derived from the clock: storing it separately would let it disagree with
     * the calendar the history is summed over.
     */
    public long periodId(final PlaytimePeriod period) {
        return this.periodId(period, this.currentKeys());
    }

    private long periodId(final PlaytimePeriod period, final PeriodKeys keys) {
        return switch (period) {
            case DAY -> keys.day();
            case WEEK -> keys.week();
            case MONTH -> keys.month();
            default -> 0L;
        };
    }

    /**
     * Per-player period totals for every stored row the boards need, in one
     * query. The leaderboard uses this so ranking offline players costs a single
     * statement instead of one load per player and period.
     * <p>
     * The window starts at the earlier of the year and the week: in early January
     * the current Monday-week still reaches into December, and those days belong
     * to the week total even though they are outside the year.
     */
    public Map<UUID, PeriodTotals> periodTotals() {
        final PeriodKeys keys = this.currentKeys();
        final long monthStart = this.monthStart(keys);
        final long yearStart = this.yearStart(keys);
        final long fromDay = Math.min(yearStart, keys.week());

        final Map<UUID, long[]> sums = new HashMap<>();
        for (final PlaytimeDay row : this.dataManager.loadSince(fromDay)) {
            if (row == null || row.getMillis() <= 0L) {
                continue;
            }
            final long[] totals = sums.computeIfAbsent(row.getPlayerId(), id -> new long[4]);
            final long day = row.getDay();
            final long millis = row.getMillis();
            if (day == keys.day()) {
                totals[0] += millis;
            }
            if (day >= keys.week()) {
                totals[1] += millis;
            }
            if (day >= monthStart) {
                totals[2] += millis;
            }
            if (day >= yearStart) {
                totals[3] += millis;
            }
        }

        final Map<UUID, PeriodTotals> result = new HashMap<>(sums.size());
        sums.forEach((id, totals) -> result.put(id, new PeriodTotals(totals[0], totals[1], totals[2], totals[3])));
        return result;
    }

    /** Day, week, month and year totals of one player. */
    public record PeriodTotals(long day, long week, long month, long year) {

        public long get(final PlaytimePeriod period) {
            return switch (period) {
                case DAY -> this.day;
                case WEEK -> this.week;
                case MONTH -> this.month;
                case YEAR -> this.year;
                default -> 0L;
            };
        }
    }

    /**
     * Daily streak. A cached streak only stays valid while the run still reaches
     * today or yesterday, so it is checked against the last played day rather
     * than trusted: a player who stopped playing a week ago has no streak left,
     * and returning the number from their last session would be a lie.
     */
    public int getDailyStreak(final SunUser user) {
        final long today = this.currentKeys().day();
        final PlaytimeHistory history = this.histories.get(user.getId());
        if (history != null) {
            synchronized (history) {
                return history.dailyStreak(today);
            }
        }
        final int cached = Math.max(0, user.getPropertyOrDefault(PlaytimeProperties.DAILY_STREAK));
        final long last = this.getLastPlayedDay(user);
        return last >= today - 1L ? cached : 0;
    }

    public int getWeeklyStreak(final SunUser user) {
        final long today = this.currentKeys().day();
        final PlaytimeHistory history = this.histories.get(user.getId());
        if (history != null) {
            synchronized (history) {
                return history.weeklyStreak(today);
            }
        }
        final int cached = Math.max(0, user.getPropertyOrDefault(PlaytimeProperties.WEEKLY_STREAK));
        // Valid while the last played day is in this Monday-week or the previous one.
        final long thisWeek = this.currentKeys().week();
        return this.getLastPlayedDay(user) >= thisWeek - 7L ? cached : 0;
    }

    public int getDaysPlayed(final SunUser user) {
        final PlaytimeHistory history = this.histories.get(user.getId());
        if (history != null) {
            synchronized (history) {
                return history.daysPlayed();
            }
        }
        return Math.max(0, user.getPropertyOrDefault(PlaytimeProperties.DAYS_PLAYED));
    }

    /** First played epoch day, or {@code 0} when the player has no history. */
    public long getFirstPlayedDay(final SunUser user) {
        final PlaytimeHistory history = this.histories.get(user.getId());
        if (history != null) {
            synchronized (history) {
                final Long first = history.firstDay();
                return first == null ? 0L : first;
            }
        }
        return Math.max(0L, user.getPropertyOrDefault(PlaytimeProperties.FIRST_PLAYED_DAY));
    }

    /** Last played epoch day, or {@code 0} when the player has no history. */
    public long getLastPlayedDay(final SunUser user) {
        final PlaytimeHistory history = this.histories.get(user.getId());
        if (history != null) {
            synchronized (history) {
                final Long last = history.lastDay();
                return last == null ? 0L : last;
            }
        }
        return Math.max(0L, user.getPropertyOrDefault(PlaytimeProperties.LAST_PLAYED_DAY));
    }

    public void startSession(final SunUser user) {
        final PlaytimeHistory history = this.histories.get(user.getId());
        if (history != null) {
            synchronized (history) {
                this.writeCaches(user, history);
            }
        }
        user.setProperty(PlaytimeProperties.SESSION_START, System.currentTimeMillis());
        user.setProperty(PlaytimeProperties.LAST_SEEN, System.currentTimeMillis());
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
        final PlaytimeHistory history = this.historyOrLoad(user.getId());
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
        final PlaytimeHistory history = this.historyOrLoad(user.getId());
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

        final PlaytimeHistory history = this.historyOrLoad(user.getId());
        final long previous;
        synchronized (history) {
            previous = period == null ? 0L : history.getMillis(currentKeys().day(), period);
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
                    user.setProperty(PlaytimeProperties.REWARDED_DAY_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REWARDED_WEEK_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REWARDED_MONTH_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REMINDED_DAY_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REMINDED_WEEK_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REMINDED_MONTH_KEY, 0L);
                    user.setProperty(PlaytimeProperties.MILESTONE_PROGRESS, new HashMap<String, Integer>());
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
                    user.setProperty(PlaytimeProperties.REWARDED_MONTH_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REMINDED_MONTH_KEY, 0L);
                }
                case "week" -> {
                    this.clearRange(history, keys.week(), keys.day());
                    user.setProperty(PlaytimeProperties.REWARDED_WEEK_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REMINDED_WEEK_KEY, 0L);
                }
                case "day" -> {
                    this.clearRange(history, keys.day(), keys.day());
                    user.setProperty(PlaytimeProperties.REWARDED_DAY_KEY, 0L);
                    user.setProperty(PlaytimeProperties.REMINDED_DAY_KEY, 0L);
                }
                case "session" -> {
                    user.setProperty(PlaytimeProperties.SESSION_START, System.currentTimeMillis());
                    return;
                }
                case "milestones", "milestone" -> {
                    user.setProperty(PlaytimeProperties.MILESTONE_PROGRESS, new HashMap<String, Integer>());
                    return;
                }
                default -> {
                    return;
                }
            }
            this.writeCaches(user, history, keys);
        }
    }

    private void clearRange(final PlaytimeHistory history, final long fromDay, final long toDay) {
        history.prune(fromDay, toDay, false);
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
    private void persist(final SunUser user, final PlaytimeHistory history) {
        this.writeCaches(user, history);

        final Set<Long> dirty = history.dirtyDays();
        final List<PlaytimeDay> upserts = new ArrayList<>();
        final List<Long> deletes = new ArrayList<>();
        for (final long day : dirty) {
            final long millis = history.getMillis(day);
            if (millis > 0L) {
                upserts.add(new PlaytimeDay(history.getPlayerId(), day, millis));
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
        return new PlaytimeStatsMenu(this, target.getId()).show(this.plugin, viewer);
    }

    public boolean openTopMenu(final Player viewer, final PlaytimePeriod period, final int page) {
        return new PlaytimeTopMenu(this, period, page).show(this.plugin, viewer);
    }

    public boolean openCalendarMenu(final Player viewer, final SunUser target, final YearMonth month) {
        return new PlaytimeCalendarMenu(this, target.getId(), month).show(this.plugin, viewer);
    }

}
