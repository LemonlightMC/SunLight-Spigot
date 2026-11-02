package su.nightexpress.sunlight.moduleImpl.friends.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import su.nightexpress.sunlight.user.SunUser;

/**
 * Fired after a request has been queued, so a plugin can react to it without
 * having to queue one.
 */
public class PlayerFriendRequestEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final SunUser sender;
    private final SunUser target;
    private final long expireDate;

    public PlayerFriendRequestEvent(SunUser sender, SunUser target, long expireDate) {
        this.sender = sender;
        this.target = target;
        this.expireDate = expireDate;
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

    /** Epoch millis after which the request stops being actionable. */
    public long getExpireDate() {
        return this.expireDate;
    }
}