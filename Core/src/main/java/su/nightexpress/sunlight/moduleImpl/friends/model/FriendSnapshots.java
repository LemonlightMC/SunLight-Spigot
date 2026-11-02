package su.nightexpress.sunlight.moduleImpl.friends.model;

import org.bukkit.Bukkit;

import su.nightexpress.sunlight.api.provider.dto.FriendHandle;

/**
 * Builds the immutable API snapshot of a {@link FriendRecord}.
 * <p>
 * The last-seen stamp comes from the caller, not from the friendship row, so it
 * stays correct while a player is online and survives a rename.
 */
public final class FriendSnapshots {

    private FriendSnapshots() {
    }

    public static FriendHandle of(final FriendRecord record, final long lastSeen) {
        return new FriendHandle(
                record.getOwnerId(),
                record.getOwner().getName(),
                record.getFriendId(),
                record.getFriend().getName(),
                record.getCreateDate(),
                record.isFavorite(),
                Bukkit.getPlayer(record.getFriendId()) != null,
                normalize(lastSeen));
    }

    public static FriendHandle ofBlocked(final BlockRecord block, final long lastSeen) {
        return new FriendHandle(
                block.getBlockerId(),
                block.getBlocker().getName(),
                block.getBlockedId(),
                block.getBlocked().getName(),
                block.getCreateDate(),
                false,
                Bukkit.getPlayer(block.getBlockedId()) != null,
                normalize(lastSeen));
    }

    /**
     * Zero means "never seen", which the API contract allows as a null-ish value.
     */
    private static Long normalize(final long lastSeen) {
        return lastSeen <= 0L ? null : lastSeen;
    }
}