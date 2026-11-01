package su.nightexpress.sunlight.moduleImpl.playtime;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimeGoal;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

public class PlaytimeSettings extends AbstractConfig {

    /**
     * Periods that support goals. The goal trackers (and by extension the
     * in-game goal command) only cover these — session/alltime goals would
     * either never roll over or never reset, so they are deliberately absent.
     */
    private static final List<PlaytimePeriod> GOAL_PERIODS = List.of(
            PlaytimePeriod.DAY, PlaytimePeriod.WEEK, PlaytimePeriod.MONTH);

    private static final Map<PlaytimePeriod, PlaytimeGoal> DEFAULT_GOALS = createDefaultGoals();

    private final ConfigProperty<Boolean> excludeAfk = this.addProperty(ConfigTypes.BOOLEAN,
            "Playtime.Tracking.exclude-afk",
            true,
            "Sets whether time spent AFK (AFK module) does not count as playtime.");

    private final ConfigProperty<Long> tickInterval = this.addProperty(ConfigTypes.LONG,
            "Playtime.Tracking.tick-interval",
            20L,
            "Sets how often (in game ticks) playtime is accumulated. [1 second = 20 ticks]");

    private final ConfigProperty<String> timezone = this.addProperty(ConfigTypes.STRING,
            "Playtime.Tracking.timezone",
            "system",
            "Sets the timezone used for day/week/month rollovers.",
            "Use 'system' to follow the global 'General.Time_Zone' setting, or any IANA id, e.g. 'Europe/Berlin', 'America/New_York'.");

    private final ConfigProperty<Long> topRefreshInterval = this.addProperty(ConfigTypes.LONG,
            "Playtime.Top.refresh-Interval",
            600L,
            "Sets how often (in game ticks) the /playtop leaderboard cache is rebuilt. [1 second = 20 ticks]");

    private final ConfigProperty<Long> topDbScanInterval = this.addProperty(ConfigTypes.LONG,
            "Playtime.Top.database-Scan-Interval",
            12000L,
            "Sets how often (in game ticks) the leaderboard scans the whole database.",
            "Live players refresh every Refresh-Interval above; offline players are merged in",
            "at most this often, and only when the leaderboard was actually requested.",
            "[1 second = 20 ticks, 12000 = 10 minutes]");

    private final ConfigProperty<String> goalMinDuration = this.addProperty(ConfigTypes.STRING,
            "Playtime.Goals.min-duration",
            "1m",
            "Sets the minimal duration an in-game goal change may set through /playtime goal.",
            "Accepts '30s', '5m', '1h', '7d', combinations like '1h30m', or plain seconds.",
            "A negative or malformed value disables the module.");

    private final ConfigProperty<String> goalMaxDuration = this.addProperty(ConfigTypes.STRING,
            "Playtime.Goals.max-duration",
            "30d",
            "Sets the maximal duration an in-game goal change may set.",
            "A negative, malformed, or smaller-than-min value disables the module.");

    private final ConfigProperty<Boolean> goalReminders = this.addProperty(ConfigTypes.BOOLEAN,
            "Playtime.Goals.reminders",
            true,
            "Sets whether players are notified shortly before one of their goals is reached.");

    private final ConfigProperty<Map<PlaytimePeriod, PlaytimeGoal>> goals = this.addProperty(
            ConfigProperty.of(
                    ConfigTypes.forMap(
                            PlaytimeSettings::parseGoalPeriod,
                            PlaytimePeriod::getConfigName,
                            new PlaytimeGoal.PlaytimeGoalCodec(),
                            HashMap::new),
                    "Playtime.Goals.Periods",
                    DEFAULT_GOALS,
                    "Playtime goals per period. A period that is missing here, or whose duration is 0, has no goal.",
                    "When a player reaches the duration within the period, the rewards are executed once per period.",
                    "A reminder is sent as soon as the remaining time fits into the remainder-window.",
                    "This can also be changed in-game via /playtime goal; such changes are saved back here."));

    private FileConfig fileConfig;
    private long goalMinMinutes;
    private long goalMaxMinutes;

    public void load(final FileConfig config) {
        this.fileConfig = config;
        super.load(config);

        goalMinMinutes = TimeUtil.parseDurationSeconds(goalMinDuration.get());
        if (goalMinMinutes < 0) {
            throw new IllegalArgumentException("Invalid Min Duration for Goals");
        }
        goalMinMinutes = Math.floorDiv(goalMinMinutes, 60);
        goalMaxMinutes = TimeUtil.parseDurationSeconds(goalMaxDuration.get());
        if (goalMaxMinutes < 0 || goalMaxMinutes < goalMinMinutes) {
            throw new IllegalArgumentException("Invalid Max Duration for Goals");
        }
        goalMaxMinutes = Math.floorDiv(goalMaxMinutes, 60);
    }

    public boolean isExcludeAfkEnabled() {
        return this.excludeAfk.get();
    }

    public long getTickInterval() {
        return Math.max(1L, this.tickInterval.get());
    }

    public ZoneId getZoneId() {
        // 'system' means "follow the global General.Time_Zone", not the JVM zone.
        return TimeUtil.resolveZoneId(this.timezone.get(), TimeUtil.getZoneId());
    }

    public long getTopRefreshInterval() {
        return Math.max(1000L, this.topRefreshInterval.get() * 50L);
    }

    public long getTopDbScanInterval() {
        return Math.max(600L, this.topDbScanInterval.get());
    }

    public long getGoalMinMinutes() {
        return goalMinMinutes;
    }

    public long getGoalMaxMinutes() {
        return goalMaxMinutes;
    }

    public boolean isRemindersEnabled() {
        return this.goalReminders.get();
    }

    public Map<PlaytimePeriod, PlaytimeGoal> getGoals() {
        return this.goals.get();
    }

    public PlaytimeGoal getGoal(PlaytimePeriod period) {
        return period == null ? null : this.goals.get().get(period);
    }

    /**
     * Goal duration for a period, in milliseconds. {@code 0} means the period has
     * no enabled goal.
     */
    public long getGoalMs(PlaytimePeriod period) {
        final PlaytimeGoal goal = this.getGoal(period);
        return goal == null ? 0L : goal.duration();
    }

    /**
     * Overrides a goal duration in-game ({@code minutes}, where {@code 0} disables
     * the goal) and persists the change back to the playtime config file, so it
     * survives restarts.
     *
     * @return whether the goal existed and the change was applied and saved.
     */
    public boolean setGoalMinutes(final PlaytimePeriod period, final long minutes) {
        if (period == null || this.fileConfig == null) {
            return false;
        }
        final PlaytimeGoal current = this.getGoal(period);
        if (current == null) {
            return false;
        }

        final Map<PlaytimePeriod, PlaytimeGoal> updated = new HashMap<>(this.goals.get());
        updated.put(period, current.withMinutes(minutes));
        this.goals.set(updated);
        this.goals.write(this.fileConfig);
        this.fileConfig.saveChanges();
        return true;
    }

    private static Map<PlaytimePeriod, PlaytimeGoal> createDefaultGoals() {
        final Map<PlaytimePeriod, PlaytimeGoal> defaults = new LinkedHashMap<>();
        defaults.put(PlaytimePeriod.DAY, new PlaytimeGoal("1h", "15m",
                List.of("eco give %player_name% 1000", "give %player_name% diamond 8")));
        defaults.put(PlaytimePeriod.WEEK, new PlaytimeGoal("8h", "1h",
                List.of("eco give %player_name% 5000", "give %player_name% netherite_ingot 4")));
        defaults.put(PlaytimePeriod.MONTH, new PlaytimeGoal("40h", "4h",
                List.of("eco give %player_name% 20000", "give %player_name% netherite_block 2")));
        return Collections.unmodifiableMap(defaults);
    }

    /**
     * Map reader for goal section keys: unknown or unsupported period names must
     * be skipped rather than folded into {@link PlaytimePeriod#ALLTIME}.
     */
    private static PlaytimePeriod parseGoalPeriod(final String raw) {
        final String id = Utils.lowercase(raw);
        if (id == null) {
            return null;
        }
        for (final PlaytimePeriod period : GOAL_PERIODS) {
            if (period.getConfigName().equals(id)) {
                return period;
            }
        }
        return null;
    }
}
