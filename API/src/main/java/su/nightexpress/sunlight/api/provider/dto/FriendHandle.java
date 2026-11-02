package su.nightexpress.sunlight.api.provider.dto;

import java.util.UUID;

/**
 * A single friendship as seen from one side of it.
 * <p>
 * The module stores a friendship as one row per owner, so {@link #ownerId()}
 * and {@link #friendId()}
 * are never the same. {@link #favorite()} is likewise per-owner: two friends of
 * each other may
 * have pinned each other differently.
 * <p>
 * Everything is a primitive, a JDK type or a type declared in this package,
 * because the API module
 * must not depend on Core.
 */
public record FriendHandle(UUID ownerId,
        String ownerName,
        UUID friendId,
        String friendName,
        long createDate,
        boolean favorite,
        boolean online,
        Long lastSeen) {

    public boolean isSelf() {
        return this.ownerId.equals(this.friendId);
    }

    /** Age of the friendship in milliseconds. */
    public long getAge() {
        return System.currentTimeMillis() - this.createDate;
    }
}