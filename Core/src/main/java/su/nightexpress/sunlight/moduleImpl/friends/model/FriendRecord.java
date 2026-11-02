package su.nightexpress.sunlight.moduleImpl.friends.model;

import org.bukkit.entity.Player;

import su.nightexpress.nightcore.util.placeholder.PlaceholderResolvable;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolver;
import su.nightexpress.sunlight.moduleImpl.friends.FriendsPlaceholders;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.Utils;

import java.util.UUID;

/**
 * The module's own friendship row.
 * <p>
 * Named to avoid clashing with the API's {@code FriendHandle}: this one is
 * mutable and carries the
 * repository bookkeeping, while the API type is an immutable snapshot handed to
 * other plugins.
 * <p>
 * A mutual friendship is two of these rows, one owned by each side, because the
 * favourite flag is
 * per-owner: two friends of each other may have pinned each other differently.
 */
public class FriendRecord implements PlaceholderResolvable {

    private final SunUser owner;
    private final SunUser friend;

    private long createDate;
    private boolean favorite;

    public FriendRecord(final SunUser owner, final SunUser friend, final long createDate, final boolean favorite) {
        this.owner = owner;
        this.friend = friend;
        this.createDate = createDate;
        this.favorite = favorite;
    }

    public static FriendRecord create(final SunUser owner, final SunUser friend) {
        return new FriendRecord(owner, friend, System.currentTimeMillis(), false);
    }

    @Override
    public PlaceholderResolver placeholders() {
        return FriendsPlaceholders.FRIEND.resolver(this);
    }

    public SunUser getOwner() {
        return this.owner;
    }

    public SunUser getFriend() {
        return this.friend;
    }

    public UUID getOwnerId() {
        return this.owner.getId();
    }

    public String getOwnerName() {
        return this.owner.getName();
    }

    public boolean isOwnerOnline() {
        return Utils.isOnline(this.friend.getId());
    }

    public UUID getFriendId() {
        return this.friend.getId();
    }

    public String getFriendName() {
        return this.friend.getName();
    }

    public boolean isFriendOnline() {
        return Utils.isOnline(this.friend.getId());
    }

    public long getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(final long createDate) {
        this.createDate = createDate;
    }

    public boolean isFavorite() {
        return this.favorite;
    }

    public void setFavorite(final boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isOwner(final Player player) {
        return player == null ? false : this.owner.getId().equals(player.getUniqueId());
    }

    public boolean isOwner(final UUID playerId) {
        return this.owner.getId().equals(playerId);
    }

    public boolean isMember(final UUID playerId) {
        return this.owner.getId().equals(playerId) || this.friend.getId().equals(playerId);
    }

    /** The other party, or {@code null} when that player is neither of the two. */

    public SunUser getOther(final UUID playerId) {
        if (this.owner.getId().equals(playerId)) {
            return this.friend;
        }
        if (this.friend.getId().equals(playerId)) {
            return this.owner;
        }
        return null;
    }
}