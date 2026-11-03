package su.nightexpress.sunlight.moduleImpl.friends.repository;

import su.nightexpress.sunlight.moduleImpl.friends.model.BlockRecord;
import su.nightexpress.sunlight.moduleImpl.friends.model.FriendRecord;
import su.nightexpress.sunlight.user.SunUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory index of the friendship graph and the block list.
 * <p>
 * Both directions of a friendship get their own entry, so every read below is a
 * single map lookup
 * rather than a scan: a mutual friendship is two {@link FriendRecord} rows, one
 * owned by each side.
 * The block map is keyed by blocker only, because a block is never mirrored.
 */
public class FriendsRepository {

    private final Map<UUID, Set<FriendRecord>> friendsByOwner;
    private final Map<UUID, Set<FriendRecord>> friendsByFriend;
    private final Map<UUID, Set<BlockRecord>> blocksByBlocker;

    public FriendsRepository() {
        this.friendsByOwner = new ConcurrentHashMap<>();
        this.friendsByFriend = new ConcurrentHashMap<>();
        this.blocksByBlocker = new ConcurrentHashMap<>();
    }

    public void clear() {
        this.friendsByOwner.clear();
        this.friendsByFriend.clear();
        this.blocksByBlocker.clear();
    }

    // Friendships

    public synchronized void upsert(final FriendRecord record) {
        this.removeRecord(record);
        this.add(record);
    }

    private synchronized void add(final FriendRecord record) {
        this.friendsByOwner.computeIfAbsent(record.getOwnerId(), key -> ConcurrentHashMap.newKeySet())
                .add(record);
        this.friendsByFriend.computeIfAbsent(record.getFriendId(), key -> ConcurrentHashMap.newKeySet())
                .add(record);
    }

    private synchronized void removeRecord(final FriendRecord record) {
        final Set<FriendRecord> owned = this.friendsByOwner.get(record.getOwnerId());
        if (owned != null) {
            owned.removeIf(existing -> existing.getFriendId().equals(record.getFriendId()));
        }
        final Set<FriendRecord> friended = this.friendsByFriend.get(record.getFriendId());
        if (friended != null) {
            friended.removeIf(existing -> existing.getOwnerId().equals(record.getOwnerId()));
        }
    }

    public synchronized void remove(final FriendRecord record) {
        this.removeRecord(record);
    }

    /**
     * That player's own side of every friendship they are in.
     * <p>
     * Ordered online first, then alphabetically, because that is what a menu or a
     * list message
     * wants and re-sorting at every call site would eventually be forgotten at one
     * of them.
     */
    public List<FriendRecord> getFriends(final UUID playerId) {
        final Set<FriendRecord> set = this.friendsByOwner.get(playerId);
        if (set == null || set.isEmpty()) {
            return List.of();
        }

        final List<FriendRecord> list = new ArrayList<>(set);
        list.sort(Comparator
                .comparing((final FriendRecord record) -> !record.isFriendOnline())
                .thenComparing(record -> record.getFriend().getName(), String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public int getFriendsCount(final UUID playerId) {
        final Set<FriendRecord> set = this.friendsByOwner.get(playerId);
        return set == null ? 0 : set.size();
    }

    public List<FriendRecord> getOnlineFriends(final UUID playerId) {
        final Set<FriendRecord> set = this.friendsByOwner.get(playerId);
        if (set == null || set.isEmpty()) {
            return List.of();
        }
        return set.stream()
                .filter(record -> record.isFriendOnline())
                .sorted(Comparator
                        .comparing((final FriendRecord record) -> !record.isFriendOnline())
                        .thenComparing(record -> record.getFriend().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public int getOnlineFriendsCount(final UUID playerId) {
        final Set<FriendRecord> set = this.friendsByOwner.get(playerId);
        if (set == null || set.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (final FriendRecord record : set) {
            if (record.isFriendOnline()) {
                count++;
            }
        }
        return count;
    }

    /**
     * The row describing what that player thinks of the other one, or {@code null}.
     */

    public FriendRecord getFriend(final UUID playerId, final UUID otherId) {
        final Set<FriendRecord> set = this.friendsByOwner.get(playerId);
        if (set == null) {
            return null;
        }
        return set.stream().filter(record -> record.getFriendId().equals(otherId)).findFirst().orElse(null);
    }

    public boolean isFriend(final UUID playerId, final UUID otherId) {
        return this.getFriend(playerId, otherId) != null;
    }

    public FriendRecord getFavorite(final UUID playerId) {
        final Set<FriendRecord> set = this.friendsByOwner.get(playerId);
        if (set == null) {
            return null;
        }
        return set.stream().filter(FriendRecord::isFavorite).findFirst().orElse(null);
    }

    /**
     * Pins one friend and unpins whichever was pinned before.
     * <p>
     * Returns the row that now holds the pin so the caller can persist both rows.
     */

    public synchronized FriendRecord setFavorite(final UUID playerId, final UUID friendId, final boolean favorite) {
        final FriendRecord target = this.getFriend(playerId, friendId);
        if (target == null) {
            return null;
        }

        if (favorite) {
            for (final FriendRecord record : this.getFriends(playerId)) {
                if (record.isFavorite() && !record.getFriendId().equals(friendId)) {
                    record.setFavorite(false);
                }
            }
        }

        target.setFavorite(favorite);
        return target;
    }

    /**
     * Every friendship on the server, for the admin command and the stats screen.
     */
    public List<FriendRecord> getAll() {
        final List<FriendRecord> list = new ArrayList<>();
        this.friendsByOwner.values().forEach(list::addAll);
        return list;
    }

    public int size() {
        return this.friendsByOwner.values().stream().mapToInt(Set::size).sum();
    }

    // Blocks

    public synchronized void upsertBlock(final BlockRecord block) {
        final Set<BlockRecord> set = this.blocksByBlocker.computeIfAbsent(block.getBlockerId(),
                key -> ConcurrentHashMap.newKeySet());
        set.removeIf(existing -> existing.getBlockedId().equals(block.getBlockedId()));
        set.add(block);
    }

    public synchronized void removeBlock(final BlockRecord block) {
        final Set<BlockRecord> set = this.blocksByBlocker.get(block.getBlockerId());
        if (set != null) {
            set.removeIf(existing -> existing.getBlockedId().equals(block.getBlockedId()));
        }
    }

    /** That player's blocks. */
    public List<BlockRecord> getBlocks(final UUID blockerId) {
        final Set<BlockRecord> set = this.blocksByBlocker.get(blockerId);
        if (set == null || set.isEmpty()) {
            return List.of();
        }

        final List<BlockRecord> list = new ArrayList<>(set);
        list.sort(Comparator.comparing((final BlockRecord block) -> !block.getBlocked().isOnline())
                .thenComparing(block -> block.getBlocked().getName(), String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public int getBlocksCount(final UUID blockerId) {
        final Set<BlockRecord> set = this.blocksByBlocker.get(blockerId);
        return set == null ? 0 : set.size();
    }

    /** Whether that player has blocked the other one. One direction only. */
    public boolean isBlocked(final UUID blockerId, final UUID blockedId) {
        final Set<BlockRecord> set = this.blocksByBlocker.get(blockerId);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(block -> block.getBlockedId().equals(blockedId));
    }

    /**
     * Whether either side has blocked the other, which is what actually stops a
     * request.
     */
    public boolean isBlockedEitherWay(final UUID playerId, final UUID otherId) {
        return this.isBlocked(playerId, otherId) || this.isBlocked(otherId, playerId);
    }

    public List<BlockRecord> getAllBlocks() {
        final List<BlockRecord> list = new ArrayList<>();
        this.blocksByBlocker.values().forEach(list::addAll);
        return list;
    }
}