package su.nightexpress.sunlight.moduleImpl.playtime2;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import su.nightexpress.sunlight.user.property.UserProperty;

/**
 * Persistent state of the playtime2 module. The {@code playtime_v2_} prefix is
 * deliberate: the old module's {@code playtime_*} keys are never registered, so
 * its data is ignored on read and dropped on the next user save.
 * <p>
 * The counters are a cache of the daily history, recomputed whenever the
 * history changes. The archived total is the only counter that is not
 * derivable from the retained rows.
 */
public class Playtime2Properties {

    public static final UserProperty<Long> TOTAL = UserProperty.create("playtime_v2_total", Long.class, 0L, true);
    public static final UserProperty<Long> ARCHIVED_TOTAL = UserProperty.create("playtime_v2_archived_total", Long.class, 0L, true);
    public static final UserProperty<Long> YEAR = UserProperty.create("playtime_v2_year", Long.class, 0L, true);
    public static final UserProperty<Long> MONTH = UserProperty.create("playtime_v2_month", Long.class, 0L, true);
    public static final UserProperty<Long> WEEK = UserProperty.create("playtime_v2_week", Long.class, 0L, true);
    public static final UserProperty<Long> DAY = UserProperty.create("playtime_v2_day", Long.class, 0L, true);

    public static final UserProperty<Long> DAY_KEY = UserProperty.create("playtime_v2_day_key", Long.class, 0L, true);
    public static final UserProperty<Long> WEEK_KEY = UserProperty.create("playtime_v2_week_key", Long.class, 0L, true);
    public static final UserProperty<Long> MONTH_KEY = UserProperty.create("playtime_v2_month_key", Long.class, 0L, true);
    public static final UserProperty<Long> YEAR_KEY = UserProperty.create("playtime_v2_year_key", Long.class, 0L, true);

    public static final UserProperty<Integer> DAILY_STREAK = UserProperty.create("playtime_v2_daily_streak", Integer.class, 0, true);
    public static final UserProperty<Integer> WEEKLY_STREAK = UserProperty.create("playtime_v2_weekly_streak", Integer.class, 0, true);
    public static final UserProperty<Integer> DAYS_PLAYED = UserProperty.create("playtime_v2_days_played", Integer.class, 0, true);
    public static final UserProperty<Long> FIRST_PLAYED_DAY = UserProperty.create("playtime_v2_first_played_day", Long.class, 0L, true);
    public static final UserProperty<Long> LAST_PLAYED_DAY = UserProperty.create("playtime_v2_last_played_day", Long.class, 0L, true);

    public static final UserProperty<Long> LAST_SEEN = UserProperty.create("playtime_v2_last_seen", Long.class, 0L, true);

    public static final UserProperty<Long> REWARDED_DAY_KEY = UserProperty.create("playtime_v2_rewarded_day_key", Long.class, 0L, true);
    public static final UserProperty<Long> REWARDED_WEEK_KEY = UserProperty.create("playtime_v2_rewarded_week_key", Long.class, 0L, true);
    public static final UserProperty<Long> REWARDED_MONTH_KEY = UserProperty.create("playtime_v2_rewarded_month_key", Long.class, 0L, true);

    public static final UserProperty<Long> REMINDED_DAY_KEY = UserProperty.create("playtime_v2_reminded_day_key", Long.class, 0L, true);
    public static final UserProperty<Long> REMINDED_WEEK_KEY = UserProperty.create("playtime_v2_reminded_week_key", Long.class, 0L, true);
    public static final UserProperty<Long> REMINDED_MONTH_KEY = UserProperty.create("playtime_v2_reminded_month_key", Long.class, 0L, true);

    public static final UserProperty<Long> SESSION = UserProperty.create("playtime_v2_session", Long.class, 0L, true);
    public static final UserProperty<Long> SESSION_START = UserProperty.create("playtime_v2_session_start", Long.class, 0L, true);

    public static final UserProperty<Map<String, Integer>> MILESTONE_PROGRESS = createMilestoneProgressProperty();

    @SuppressWarnings("unchecked")
    private static UserProperty<Map<String, Integer>> createMilestoneProgressProperty() {
        return new UserProperty<>("playtime_v2_milestone_progress",
                new TypeToken<Map<String, Integer>>() {
                }.getType(),
                (Class<Map<String, Integer>>) (Class<?>) Map.class,
                new HashMap<>(),
                true);
    }
}
