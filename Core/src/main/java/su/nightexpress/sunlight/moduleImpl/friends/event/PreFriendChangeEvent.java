package su.nightexpress.sunlight.moduleImpl.friends.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendRecord;
import su.nightexpress.sunlight.user.SunUser;

/**
 * Fired before a friendship is created or removed, in either direction.
 * <p>
 * Fires once per row, so a mutual friendship fires it twice. That is
 * deliberate: a plugin that
 * only wants to know "did these two just become friends" can watch for the
 * pair, while a plugin
 * maintaining its own per-owner index needs each row.
 */
public class PreFriendChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final FriendRecord record;
    private final boolean removal;
    private boolean cancelled;

    public PreFriendChangeEvent(FriendRecord record, boolean removal) {
        this.record = record;
        this.removal = removal;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /** The row about to be written or deleted. */
    public FriendRecord getRecord() {
        return this.record;
    }

    public SunUser getOwner() {
        return this.record.getOwner();
    }

    public SunUser getFriend() {
        return this.record.getFriend();
    }

    /** True for a removal, false for an addition. */
    public boolean isRemoval() {
        return this.removal;
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