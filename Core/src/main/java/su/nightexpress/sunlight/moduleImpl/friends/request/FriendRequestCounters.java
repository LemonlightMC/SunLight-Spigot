package su.nightexpress.sunlight.moduleImpl.friends.request;

import su.nightexpress.sunlight.moduleImpl.friends.FriendsProperties;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.TimeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reads and advances the daily request counters stored on a {@link SunUser}.
 * <p>
 * Kept out of the module so the rollover rule lives in exactly one place: a
 * stored count from an earlier day reads as zero rather than silently carrying
 * yesterday's total into today.
 */
public final class FriendRequestCounters {

    private FriendRequestCounters() {
    }

    /** How many requests that player has sent today, server-wide. */
    public static int getTotal(final SunUser user) {
        if (!today().equals(user.getPropertyOrDefault(FriendsProperties.REQUEST_DAY_KEY))) {
            return 0;
        }
        return user.getPropertyOrDefault(FriendsProperties.REQUEST_DAY_COUNT);
    }

    /** How many requests that player has sent today to one specific target. */
    public static int getForTarget(final SunUser user, final UUID targetId) {
        if (!today().equals(user.getPropertyOrDefault(FriendsProperties.REQUEST_DAY_KEY))) {
            return 0;
        }
        return targetCounts(user).getOrDefault(targetId.toString(), 0);
    }

    /**
     * Counts one request from that player to that target, rolling the day over when
     * needed.
     * <p>
     * Both counters advance together so the per-target read cannot disagree with
     * the total.
     */
    public static void increment(final SunUser user, final UUID targetId) {
        final String today = today();
        final boolean sameDay = today.equals(user.getPropertyOrDefault(FriendsProperties.REQUEST_DAY_KEY));

        final int total = sameDay ? user.getPropertyOrDefault(FriendsProperties.REQUEST_DAY_COUNT) : 0;

        final Map<String, Integer> targets = sameDay
                ? new HashMap<>(targetCounts(user))
                : new HashMap<>();
        targets.merge(targetId.toString(), 1, Integer::sum);

        user.setProperty(FriendsProperties.REQUEST_DAY_KEY, today);
        user.setProperty(FriendsProperties.REQUEST_DAY_COUNT, total + 1);
        user.setProperty(FriendsProperties.REQUEST_TARGET_DAY_COUNTS, targets);
        user.markDirty();
    }

    private static Map<String, Integer> targetCounts(final SunUser user) {
        final Map<String, Integer> map = user.getPropertyOrDefault(FriendsProperties.REQUEST_TARGET_DAY_COUNTS);
        return map == null ? Map.of() : map;
    }

    public static String today() {
        return TimeUtil.getCurrentDate().toString();
    }
}