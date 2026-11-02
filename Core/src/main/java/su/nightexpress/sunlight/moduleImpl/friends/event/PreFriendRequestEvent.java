package su.nightexpress.sunlight.moduleImpl.friends.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import su.nightexpress.sunlight.user.SunUser;

/**
 * Fired before a friend request is queued, so a plugin can refuse it.
 * <p>
 * Fired before any limit check has been made public, so a handler that wants to
 * replace the rules
 * can do so; a handler that wants to merely observe the outcome should listen
 * to
 * {@link PlayerFriendRequestEvent} instead.
 */
public class PreFriendRequestEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final SunUser sender;
    private final SunUser target;
    private boolean cancelled;

    public PreFriendRequestEvent(SunUser sender, SunUser target) {
        this.sender = sender;
        this.target = target;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public SunUser getSender() {
        return this.sender;
    }

    public SunUser getTarget() {
        return this.target;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}