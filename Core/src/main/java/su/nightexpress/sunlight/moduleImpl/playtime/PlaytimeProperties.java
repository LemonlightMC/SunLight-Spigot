package su.nightexpress.sunlight.moduleImpl.playtime;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import su.nightexpress.sunlight.user.property.UserProperty;

/**
 * Persistent state of the playtime module.
 * <p>
 * Only values that cannot be recomputed live here. The period totals are absent
 * on purpose: they are sums over the daily history, and caching them would mean
 * also storing which period they belong to, because a "week total" without its
 * week is not a fact about anything. Deriving them instead keeps the rows the
 * single source of truth.
 * <p>
 * {@link #TOTAL} is the exception. It equals the archived total plus the sum of
 * the retained rows, but it is stored so the leaderboard can rank offline
 * players from the users table alone, without loading every player's days.
 */
public class PlaytimeProperties {

    public static final UserProperty<Long> TOTAL = UserProperty.create("playtime_total", Long.class, 0L, true);
    public static final UserProperty<Long> ARCHIVED_TOTAL = UserProperty.create("playtime_archived_total", Long.class, 0L, true);

    public static final UserProperty<Integer> DAILY_STREAK = UserProperty.create("playtime_daily_streak", Integer.class, 0, true);
    public static final UserProperty<Integer> WEEKLY_STREAK = UserProperty.create("playtime_weekly_streak", Integer.class, 0, true);
    public static final UserProperty<Integer> DAYS_PLAYED = UserProperty.create("playtime_days_played", Integer.class, 0, true);
    public static final UserProperty<Long> FIRST_PLAYED_DAY = UserProperty.create("playtime_first_played_day", Long.class, 0L, true);
    public static final UserProperty<Long> LAST_PLAYED_DAY = UserProperty.create("playtime_last_played_day", Long.class, 0L, true);

    public static final UserProperty<Long> LAST_SEEN = UserProperty.create("playtime_last_seen", Long.class, 0L, true);

    /**
     * Period id of the last paid goal, so a goal pays at most once per period.
     * Zero means "never", which no real period id can be.
     */
    public static final UserProperty<Long> REWARDED_DAY_KEY = UserProperty.create("playtime_rewarded_day_key", Long.class, 0L, true);
    public static final UserProperty<Long> REWARDED_WEEK_KEY = UserProperty.create("playtime_rewarded_week_key", Long.class, 0L, true);
    public static final UserProperty<Long> REWARDED_MONTH_KEY = UserProperty.create("playtime_rewarded_month_key", Long.class, 0L, true);

    public static final UserProperty<Long> REMINDED_DAY_KEY = UserProperty.create("playtime_reminded_day_key", Long.class, 0L, true);
    public static final UserProperty<Long> REMINDED_WEEK_KEY = UserProperty.create("playtime_reminded_week_key", Long.class, 0L, true);
    public static final UserProperty<Long> REMINDED_MONTH_KEY = UserProperty.create("playtime_reminded_month_key", Long.class, 0L, true);

    /** Start of the current session. The session duration is derived from it. */
    public static final UserProperty<Long> SESSION_START = UserProperty.create("playtime_session_start", Long.class, 0L, true);

    public static final UserProperty<Map<String, Integer>> MILESTONE_PROGRESS = createMilestoneProgressProperty();

    @SuppressWarnings("unchecked")
    private static UserProperty<Map<String, Integer>> createMilestoneProgressProperty() {
        return new UserProperty<>("playtime_milestone_progress",
                new TypeToken<Map<String, Integer>>() {
                }.getType(),
                (Class<Map<String, Integer>>) (Class<?>) Map.class,
                new HashMap<>(),
                true);
    }
}
