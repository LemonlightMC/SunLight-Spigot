package su.nightexpress.sunlight.moduleImpl.friends.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import su.nightexpress.sunlight.user.SunUser;

/**
 * Fired after a block has been added or lifted.
 */
public class PlayerFriendBlockEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final SunUser blocker;
    private final SunUser blocked;
    private final boolean blocking;

    public PlayerFriendBlockEvent(SunUser blocker, SunUser blocked, boolean blocking) {
        this.blocker = blocker;
        this.blocked = blocked;
        this.blocking = blocking;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public SunUser getBlocker() {
        return this.blocker;
    }

    public SunUser getBlocked() {
        return this.blocked;
    }

    /** True for a new block, false for a lifted one. */
    public boolean isBlocking() {
        return this.blocking;
    }
}