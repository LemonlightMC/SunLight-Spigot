package su.nightexpress.sunlight.moduleImpl.friends.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendRecord;
import su.nightexpress.sunlight.user.SunUser;

/**
 * Fired after a friendship row has been added or removed.
 * <p>
 * Fires once per row, so a mutual friendship fires it twice with the owner and
 * friend swapped.
 */
public class PlayerFriendChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final FriendRecord record;
    private final boolean addition;

    public PlayerFriendChangeEvent(FriendRecord record, boolean addition) {
        this.record = record;
        this.addition = addition;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public FriendRecord getRecord() {
        return this.record;
    }

    public SunUser getOwner() {
        return this.record.getOwner();
    }

    public SunUser getFriend() {
        return this.record.getFriend();
    }

    /** True for an addition, false for a removal. */
    public boolean isAddition() {
        return this.addition;
    }
}