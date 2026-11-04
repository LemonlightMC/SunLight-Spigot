package su.nightexpress.sunlight.moduleImpl.friends;

import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.TypedPlaceholder;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendRecord;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

/**
 * Placeholder tokens used inside the module's own messages, menus and icons.
 * <p>
 * The PAPI-facing keys are registered separately in
 * {@link FriendsModule#registerPlaceholders(su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry)}:
 * those take a payload, which is why they cannot live in module table.
 */
public class FriendsPlaceholders {

    private static final int FRIEND_LIST_LIMITED_COUNT = 10;

    public static final String FRIEND_NAME = "%friend_name%";
    public static final String FRIEND_UUID = "%friend_uuid%";
    public static final String FRIEND_OWNER = "%friend_owner%";
    public static final String FRIEND_OWNER_UUID = "%friend_owner_uuid%";
    public static final String FRIEND_ONLINE = "%friend_online%";
    public static final String FRIEND_CREATE_DATE = "%friend_create_date%";
    public static final String FRIEND_AGE = "%friend_age%";
    public static final String FRIEND_FAVORITE = "%friend_favorite%";
    public static final String FRIEND_STATUS = "%friend_status%";

    /**
     * Resolved per viewer rather than per record, so it has no entry in
     * {@link #FRIEND}: the menus
     * and the admin message supply it themselves.
     */
    public static final String FRIEND_LIST = "%friend_list%";
    public static final String FRIEND_LAST_SEEN = "%friend_last_seen%";

    public static final TypedPlaceholder<FriendRecord> FRIEND = TypedPlaceholder
            .builder(FriendRecord.class)
            .with(FRIEND_NAME, record -> record.getFriendName())
            .with(FRIEND_UUID, record -> record.getFriendId().toString())
            .with(FRIEND_OWNER, record -> record.getOwnerName())
            .with(FRIEND_OWNER_UUID, record -> record.getOwnerId().toString())
            .with(FRIEND_ONLINE, record -> CoreLang.STATE_YES_NO.get(record.isFriendOnline()))
            .with(FRIEND_CREATE_DATE, record -> TimeUtil.formatDateTime(record.getCreateDate()))
            .with(FRIEND_AGE, record -> formatAge(record.getCreateDate()))
            .with(FRIEND_FAVORITE, record -> CoreLang.STATE_YES_NO.get(record.isFavorite()))
            .with(FRIEND_STATUS, record -> record.isFriendOnline() ? "Online" : "Offline")
            .build();

    private FriendsPlaceholders() {
    }

    /**
     * Shared formatting so a menu icon and a chat message never disagree on how an
     * age reads.
     * A zero or negative stamp means "never", which is what a never-seen friend
     * has.
     */
    public static String formatAge(final long sinceMillis) {
        if (sinceMillis <= 0L) {
            return CoreLang.OTHER_NEVER.text();
        }
        return TimeFormats.formatSince(sinceMillis, TimeFormatType.LITERAL);
    }

    public static String formatLastSeen(final long lastSeenMillis) {
        if (lastSeenMillis <= 0L) {
            return CoreLang.OTHER_NEVER.text();
        }
        return TimeFormats.formatSince(lastSeenMillis, TimeFormatType.LITERAL) + " ago";
    }

    /**
     * Comma-separated names, or "None" when the list is empty, for a scoreboard
     * line.
     */
    public static String names(final List<FriendRecord> records) {
        if (records.isEmpty()) {
            return CoreLang.OTHER_NONE.text();
        }
        return records.stream().map(record -> record.getFriendName()).collect(Collectors.joining(", "));
    }

    public static void registerPlaceholders(final FriendsModule module, final PlaceholderRegistry registry) {

        // Counters are prefixed 'count_' rather than named after the metric, because
        // the registry
        // resolves the longest matching key and hands the remainder to the handler as a
        // payload:
        // a key like 'friends_online' would swallow '%sunlight_friends_online_notch%'
        // and answer
        // with the count instead of failing.
        registry.register("friends_count", (player, payload) -> String
                .valueOf(module.repository().getFriendsCount(player.getUniqueId())));
        registry.register("friends_count_online", (player, payload) -> String
                .valueOf(module.repository().getOnlineFriendsCount(player.getUniqueId())));
        registry.register("friends_count_offline", (player, payload) -> String
                .valueOf(module.repository().getFriendsCount(player.getUniqueId())
                        - module.repository().getOnlineFriendsCount(player.getUniqueId())));
        registry.register("friends_count_blocked", (player, payload) -> String
                .valueOf(module.repository().getBlocks(player.getUniqueId()).size()));
        registry.register("friends_count_pending", (player, payload) -> String
                .valueOf(module.requestRepository().countTotal(player.getUniqueId())));
        registry.register("friends_count_pending_in", (player, payload) -> String
                .valueOf(module.requestRepository().countIncoming(player.getUniqueId())));
        registry.register("friends_count_pending_out", (player, payload) -> String
                .valueOf(module.requestRepository().countOutgoing(player.getUniqueId())));

        registry.register("friends_limit", (player, payload) -> {
            final int limit = module.getFriendLimit(player);
            return limit >= 0 ? NumberUtil.format(limit) : CoreLang.OTHER_INFINITY.text();
        });
        registry.register("friends_slots_free", (player, payload) -> {
            final int limit = module.getFriendLimit(player);
            if (limit < 0) {
                return CoreLang.OTHER_INFINITY.text();
            }
            return NumberUtil.format(Math.max(0, limit - module.repository().getFriendsCount(player.getUniqueId())));
        });

        registry.register("friends_favorite_name", (player, payload) -> {
            final FriendRecord favorite = module.repository().getFavorite(player.getUniqueId());
            return favorite == null ? CoreLang.OTHER_NONE.text() : favorite.getFriend().getName();
        });
        registry.register("friends_favorite_online", (player, payload) -> {
            final FriendRecord favorite = module.repository().getFavorite(player.getUniqueId());
            return CoreLang.STATE_YES_NO.get(favorite != null && favorite.isFriendOnline());
        });

        registry.register("friends_list_names", (player, payload) -> FriendsPlaceholders
                .names(module.getFriends(player.getUniqueId())));
        registry.register("friends_list_names_online", (player, payload) -> FriendsPlaceholders
                .names(module.getOnlineFriends(player.getUniqueId())));
        registry.register("friends_list_limited", (player, payload) -> {
            final List<FriendRecord> records = module.getFriends(player.getUniqueId());
            if (records.isEmpty()) {
                return CoreLang.OTHER_NONE.text();
            }
            return records.stream().limit(FRIEND_LIST_LIMITED_COUNT)
                    .map(record -> record.getFriend().getName())
                    .collect(Collectors.joining(", "));
        });
        registry.register("friends_list_online_first", (player, payload) -> {
            final List<FriendRecord> records = module.getFriends(player.getUniqueId());
            return records.isEmpty() ? CoreLang.OTHER_NONE.text() : records.getFirst().getFriend().getName();
        });

        // Per-player checks. The payload is the other player's name, resolved online
        // first and then through the user cache, so a scoreboard can ask about an
        // offline friend without a database round trip.
        registry.register("friends_relation", (player, payload) -> {
            final UUID otherId = module.resolveId(payload);
            if (otherId == null) {
                return "None";
            }
            final UUID moduleId = player.getUniqueId();
            if (moduleId.equals(otherId)) {
                return "Self";
            }
            if (module.repository().isFriend(moduleId, otherId)) {
                return "Friend";
            }
            if (module.repository().isBlockedEitherWay(moduleId, otherId)) {
                return "Blocked";
            }
            if (module.requestRepository().hasIncoming(moduleId, otherId)) {
                return "Pending Incoming";
            }
            if (module.requestRepository().getOutgoing(otherId, moduleId) != null) {
                return "Pending Outgoing";
            }
            return "None";
        });
        registry.register("friends_is_friend", (player, payload) -> {
            final UUID otherId = module.resolveId(payload);
            return CoreLang.STATE_YES_NO.get(otherId != null && module.isFriend(player.getUniqueId(), otherId));
        });
        registry.register("friends_is_blocked", (player, payload) -> {
            final UUID otherId = module.resolveId(payload);
            return CoreLang.STATE_YES_NO
                    .get(otherId != null && module.isBlocked(player.getUniqueId(), otherId));
        });
        registry.register("friends_last_seen", (player, payload) -> {
            final UUID otherId = module.resolveId(payload);
            if (otherId == null) {
                return CoreLang.OTHER_NONE.text();
            }
            final Player online = Utils.getPlayer(otherId);
            if (online != null) {
                return "Online now";
            }
            return FriendsPlaceholders.formatLastSeen(
                    module.userManager().getRepository().getById(otherId).map(SunUser::getLastOnline).orElse(0L));
        });

        // Toggles, matching how the other modules expose theirs.
        registry.register("friends_requests_enabled", (player, payload) -> CoreLang.STATE_ENABLED_DISALBED
                .get(module.isRequestsEnabled(player.getUniqueId())));
        registry.register("friends_login_alerts", (player, payload) -> CoreLang.STATE_ENABLED_DISALBED
                .get(module.isLoginAlertsEnabled(player.getUniqueId())));
        registry.register("friends_teleport_enabled", (player, payload) -> CoreLang.STATE_ENABLED_DISALBED
                .get(module.isFriendTeleportEnabled(player.getUniqueId())));

        registry.register("friends_total", (player, payload) -> String.valueOf(module.repository().size()));
    }
}