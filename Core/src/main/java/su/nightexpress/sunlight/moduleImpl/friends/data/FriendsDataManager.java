package su.nightexpress.sunlight.moduleImpl.friends.data;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nightcore.db.statement.RowMapper;
import su.nightexpress.nightcore.db.statement.condition.Operator;
import su.nightexpress.nightcore.db.statement.condition.Wheres;
import su.nightexpress.nightcore.db.statement.template.InsertStatement;
import su.nightexpress.nightcore.db.statement.template.SelectStatement;
import su.nightexpress.nightcore.db.statement.template.UpdateStatement;
import su.nightexpress.nightcore.db.table.Table;
import su.nightexpress.sunlight.data.DataHandler;
import su.nightexpress.sunlight.moduleImpl.friends.FriendsModule;
import su.nightexpress.sunlight.moduleImpl.friends.repository.BlockRecord;
import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendRecord;
import su.nightexpress.sunlight.user.SunUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static su.nightexpress.sunlight.moduleImpl.friends.data.FriendsColumns.*;

public class FriendsDataManager {

    private final DataHandler dataHandler;
    private FriendsModule module;

    private Table tableFriends;
    private Table tableBlocks;

    public FriendsDataManager(final DataHandler dataHandler, final FriendsModule module) {
        this.dataHandler = dataHandler;
        this.module = module;
    }

    public void init(final String tablePrefix) {
        this.tableFriends = Table.builder(tablePrefix + "_relations")
                .withColumn(ID, OWNER_ID, OWNER_NAME, FRIEND_ID, FRIEND_NAME, CREATE_DATE, FAVORITE)
                .build();

        this.tableBlocks = Table.builder(tablePrefix + "_blocks")
                .withColumn(BLOCK_ID, BLOCKER_ID, BLOCKER_NAME, BLOCKED_ID, BLOCKED_NAME, BLOCK_CREATE_DATE)
                .build();

        this.dataHandler.createTable(this.tableFriends);
        this.dataHandler.createTable(this.tableBlocks);

        this.dataHandler.addTableSync(this.tableFriends, this::syncFriend);
        this.dataHandler.addTableSync(this.tableBlocks, this::syncBlock);
    }

    private final RowMapper<FriendRecord> FRIEND_LOADER = resultSet -> {
        try {
            final UUID ownerId = readUuid(resultSet, OWNER_ID.getName());
            final UUID friendId = readUuid(resultSet, FRIEND_ID.getName());

            if (ownerId == null || friendId == null) {
                return null;
            }
            if (ownerId.equals(friendId)) {
                // A self-friendship is meaningless and would inflate every count that reads
                // this row.
                return null;
            }
            final SunUser owner = module.userManager().getRepository().getById(ownerId).orElse(null);
            final SunUser friend = module.userManager().getRepository().getById(ownerId).orElse(null);
            if (owner == null || friend == null) {
                return null;
            }

            return new FriendRecord(
                    owner,
                    friend,
                    resultSet.getLong(CREATE_DATE.getName()),
                    resultSet.getBoolean(FAVORITE.getName()));
        } catch (final SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    private final RowMapper<BlockRecord> BLOCK_LOADER = resultSet -> {
        try {
            final UUID blockerId = readUuid(resultSet, BLOCKER_ID.getName());
            final UUID blockedId = readUuid(resultSet, BLOCKED_ID.getName());
            final String blockerName = resultSet.getString(BLOCKER_NAME.getName());
            final String blockedName = resultSet.getString(BLOCKED_NAME.getName());

            if (blockerId == null || blockedId == null || blockerName == null || blockedName == null) {
                return null;
            }
            if (blockerId.equals(blockedId)) {
                return null;
            }
            final SunUser blocker = module.userManager().getRepository().getById(blockerId).orElse(null);
            final SunUser blocked = module.userManager().getRepository().getById(blockedId).orElse(null);
            if (blocker == null || blocked == null) {
                return null;
            }

            return new BlockRecord(
                    blocker,
                    blocked,
                    resultSet.getLong(BLOCK_CREATE_DATE.getName()));
        } catch (final SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    private final SelectStatement<FriendRecord> SELECT_FRIEND = SelectStatement
            .builder(FRIEND_LOADER)
            .build();

    private final SelectStatement<BlockRecord> SELECT_BLOCK = SelectStatement
            .builder(BLOCK_LOADER)
            .build();

    private static final InsertStatement<FriendRecord> INSERT_FRIEND = InsertStatement
            .builder(FriendRecord.class)
            .setUUID(OWNER_ID, FriendRecord::getOwnerId)
            .setString(OWNER_NAME, record -> record.getOwnerName())
            .setUUID(FRIEND_ID, FriendRecord::getFriendId)
            .setString(FRIEND_NAME, record -> record.getFriendName())
            .setLong(CREATE_DATE, FriendRecord::getCreateDate)
            .setBoolean(FAVORITE, FriendRecord::isFavorite)
            .build();

    private static final UpdateStatement<FriendRecord> UPDATE_FRIEND = UpdateStatement
            .builder(FriendRecord.class)
            .setString(OWNER_NAME, record -> record.getOwnerName())
            .setString(FRIEND_NAME, record -> record.getFriendName())
            .setLong(CREATE_DATE, FriendRecord::getCreateDate)
            .setBoolean(FAVORITE, FriendRecord::isFavorite)
            .build();

    private static final InsertStatement<BlockRecord> INSERT_BLOCK = InsertStatement
            .builder(BlockRecord.class)
            .setUUID(BLOCKER_ID, BlockRecord::getBlockerId)
            .setString(BLOCKER_NAME, record -> record.getBlocker().getName())
            .setUUID(BLOCKED_ID, BlockRecord::getBlockedId)
            .setString(BLOCKED_NAME, record -> record.getBlocked().getName())
            .setLong(BLOCK_CREATE_DATE, BlockRecord::getCreateDate)
            .build();

    private void syncFriend(final ResultSet resultSet) {
        try {
            final FriendRecord record = FRIEND_LOADER.map(resultSet);
            if (record == null) {
                return;
            }
            this.module.repository().upsert(record);
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void syncBlock(final ResultSet resultSet) {
        try {
            final BlockRecord block = BLOCK_LOADER.map(resultSet);
            if (block == null) {
                return;
            }
            this.module.repository().upsertBlock(block);
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    // Friendships

    public List<FriendRecord> getFriends() {
        return this.dataHandler.selectAny(this.tableFriends, SELECT_FRIEND);
    }

    public List<FriendRecord> getFriends(final UUID ownerId) {
        return this.dataHandler.selectWhere(this.tableFriends, SELECT_FRIEND,
                Wheres.whereUUID(OWNER_ID, o -> ownerId));
    }

    /**
     * Re-reads one row and reports whether it is still there.
     * <p>
     * This is the cross-server half of the friendship guard: another server in the
     * same sync window
     * may already have made them friends, in which case this server's own row would
     * be a duplicate.
     */
    public boolean existsInDatabase(final UUID ownerId, final UUID friendId) {
        return this.dataHandler.selectFirst(this.tableFriends, SELECT_FRIEND,
                Wheres.whereUUID(OWNER_ID, o -> ownerId)
                        .and(FRIEND_ID, Operator.EQUALS, o -> friendId.toString()))
                .isPresent();
    }

    public void insertFriend(final FriendRecord record) {
        this.dataHandler.insert(this.tableFriends, INSERT_FRIEND, record);
    }

    public void updateFriend(final FriendRecord friend) {
        this.dataHandler.update(this.tableFriends, UPDATE_FRIEND, friend,
                Wheres.whereUUID(OWNER_ID, FriendRecord::getOwnerId)
                        .and(FRIEND_ID, Operator.EQUALS, record -> friend.getFriendId().toString()));
    }

    public void deleteFriend(final FriendRecord record) {
        this.dataHandler.delete(this.tableFriends,
                Wheres.whereUUID(OWNER_ID, o -> record.getOwnerId())
                        .and(FRIEND_ID, Operator.EQUALS, o -> record.getFriendId().toString()));
    }

    public void deleteFriends(final UUID ownerId) {
        this.dataHandler.delete(this.tableFriends, Wheres.whereUUID(OWNER_ID, o -> ownerId));
    }

    public void deleteFriends(final String ownerName) {
        this.dataHandler.delete(this.tableFriends,
                Wheres.where(OWNER_NAME, Operator.EQUALS_IGNORE_CASE, o -> ownerName));
    }

    // Blocks

    public List<BlockRecord> getBlocks() {
        return this.dataHandler.selectAny(this.tableBlocks, SELECT_BLOCK);
    }

    public List<BlockRecord> getBlocks(final UUID blockerId) {
        return this.dataHandler.selectWhere(this.tableBlocks, SELECT_BLOCK,
                Wheres.whereUUID(BLOCKER_ID, o -> blockerId));
    }

    public boolean existsBlockInDatabase(final UUID blockerId, final UUID blockedId) {
        return this.dataHandler.selectFirst(this.tableBlocks, SELECT_BLOCK,
                Wheres.whereUUID(BLOCKER_ID, o -> blockerId)
                        .and(BLOCKED_ID, Operator.EQUALS, o -> blockedId.toString()))
                .isPresent();
    }

    public void insertBlock(final BlockRecord block) {
        this.dataHandler.insert(this.tableBlocks, INSERT_BLOCK, block);
    }

    public void deleteBlock(final BlockRecord block) {
        this.dataHandler.delete(this.tableBlocks,
                Wheres.whereUUID(BLOCKER_ID, o -> block.getBlockerId())
                        .and(BLOCKED_ID, Operator.EQUALS, o -> block.getBlockedId().toString()));
    }

    public void deleteBlocks(final UUID blockerId) {
        this.dataHandler.delete(this.tableBlocks, Wheres.whereUUID(BLOCKER_ID, o -> blockerId));
    }

    /**
     * Drops rows naming a player who no longer has a user record, so a deleted
     * account does not
     * leave a friendship that nothing can ever resolve again.
     * <p>
     * Off by default, because the users table is not the only record of a player: a
     * row may name
     * somebody whose user record is written by another server in the same sync
     * window. Turning this
     * on is only safe on a single-server setup.
     */
    public void purgeOrphans() {
        for (final FriendRecord record : this.getFriends()) {
            if (this.isOrphan(record.getOwnerId()) || this.isOrphan(record.getFriendId())) {
                this.deleteFriend(record);
            }
        }

        for (final BlockRecord block : this.getBlocks()) {
            if (this.isOrphan(block.getBlockerId()) || this.isOrphan(block.getBlockedId())) {
                this.deleteBlock(block);
            }
        }
    }

    private boolean isOrphan(@NotNull final UUID playerId) {
        return !this.module.userManager().getDataAccessor().isExists(playerId);
    }

    /**
     * Reads a uuid column that may be null, which is what the nullable-column reads
     * look like
     * across SQLite and MySQL alike.
     */
    private static UUID readUuid(final ResultSet resultSet, final String column) throws SQLException {
        final String raw = resultSet.getString(column);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

}