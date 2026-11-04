package su.nightexpress.sunlight.moduleImpl.friends.repository;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import su.nightexpress.sunlight.user.SunUser;

import java.util.UUID;

/**
 * A friendship that a player has blocked one side of.
 * <p>
 * Deliberately not merged into the friendship table: a block must survive the
 * friendship being
 * deleted, and it has no favourite flag, create date per side, or symmetry to
 * model.
 */
public class BlockRecord implements su.nightexpress.sunlight.api.provider.dto.BlockRecord {

    private final SunUser blocker;
    private final SunUser blocked;
    private final long createDate;

    public BlockRecord(final SunUser blocker, final SunUser blocked, final long createDate) {
        this.blocker = blocker;
        this.blocked = blocked;
        this.createDate = createDate;
    }

    public static BlockRecord create(final SunUser blocker, final SunUser blocked) {
        return new BlockRecord(blocker, blocked, System.currentTimeMillis());
    }

    public SunUser getBlocker() {
        return this.blocker;
    }

    public UUID getBlockerId() {
        return this.blocker.getId();
    }

    public String getBlockerName() {
        return this.blocker.getName();
    }

    public SunUser getBlocked() {
        return this.blocked;
    }

    public UUID getBlockedId() {
        return this.blocked.getId();
    }

    public String getBlockedName() {
        return this.blocked.getName();
    }

    public long getCreateDate() {
        return this.createDate;
    }

    /**
     * Whether this block stands between those two players in either direction.
     * Blocking is not
     * symmetric in storage but it is in effect: if either has blocked the other,
     * neither may send
     * a request.
     */
    public boolean isBetween(final UUID playerId, final UUID otherId) {
        return this.involves(playerId) && this.involves(otherId);
    }

    public boolean involves(final UUID playerId) {
        return this.blocker.getId().equals(playerId) || this.blocked.getId().equals(playerId);
    }

    public boolean isBlocking(final UUID playerId) {
        return this.blocker.getId().equals(playerId);
    }

    /** Whether the blocked player is online right now. */
    public boolean isBlockedOnline() {
        return Bukkit.getPlayer(this.blocked.getId()) != null;
    }

    public Player getBlockedPlayer() {
        return Bukkit.getPlayer(this.blocked.getId());
    }
}