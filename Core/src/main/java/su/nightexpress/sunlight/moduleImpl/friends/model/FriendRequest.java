package su.nightexpress.sunlight.moduleImpl.friends.model;

import su.nightexpress.sunlight.api.provider.dto.FriendRequestHandle;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.TimeUtil;

import java.util.UUID;

/**
 * A pending friend request.
 * <p>
 * Lives in memory only, like the PTP request queue: a request nobody answered
 * before the timeout
 * is not worth a database row, and writing one per attempt would hand the
 * database exactly the
 * flooding problem the request limits exist to prevent.
 */
public class FriendRequest implements su.nightexpress.sunlight.api.provider.dto.FriendRequest {

    private final SunUser sender;
    private final SunUser target;
    private final long expireDate;

    public FriendRequest(final SunUser sender, final SunUser target, final long expireDate) {
        this.sender = sender;
        this.target = target;
        this.expireDate = expireDate;
    }

    public static FriendRequest create(final SunUser sender, final SunUser target, final int timeoutSeconds) {
        return new FriendRequest(sender, target, TimeUtil.createFutureTimestamp(timeoutSeconds));
    }

    public SunUser getSender() {
        return this.sender;
    }

    public SunUser getTarget() {
        return this.target;
    }

    public UUID getSenderId() {
        return this.sender.getId();
    }

    public UUID getTargetId() {
        return this.target.getId();
    }

    public String getSenderName() {
        return this.sender.getName();
    }

    public String getTargetName() {
        return this.target.getName();
    }

    public long getExpireDate() {
        return this.expireDate;
    }

    public boolean isExpired() {
        return this.expireDate >= 0L && System.currentTimeMillis() > this.expireDate;
    }

    /** Whether this request runs from that sender to that target. */
    public boolean isFrom(final UUID senderId, final UUID targetId) {
        return this.getSenderId().equals(senderId) && this.getTargetId().equals(targetId);
    }

    /** Whether that player is the one who sent it. */
    public boolean isSentBy(final UUID playerId) {
        return this.getSenderId().equals(playerId);
    }

    /** Whether that player is the one who receives it. */
    public boolean isSentTo(final UUID playerId) {
        return this.getTargetId().equals(playerId);
    }

    /** Whether this request is the same pair as that one, ignoring direction. */
    public boolean isBetween(final UUID playerId, final UUID otherId) {
        return (this.getSenderId().equals(playerId) && this.getTargetId().equals(otherId))
                || (this.getSenderId().equals(otherId) && this.getTargetId().equals(playerId));
    }

    /** Whether that name matches the sender. */
    public boolean isSender(final String name) {
        return name != null && this.sender.getName().equals(name);
    }

    /** Whether that name matches the receiver. */
    public boolean isTarget(final String name) {
        return name != null && this.target.getName().equals(name);
    }

    public FriendRequestHandle toHandle() {
        return new FriendRequestHandle(this.getSenderId(), this.getSenderName(), this.getTargetId(),
                this.getTargetName(), this.getExpireDate());
    }
}