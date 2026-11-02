package su.nightexpress.sunlight.moduleImpl.playtime.data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import su.nightexpress.nightcore.db.statement.condition.Operator;
import su.nightexpress.nightcore.db.statement.condition.Wheres;
import su.nightexpress.nightcore.db.statement.template.SelectStatement;
import su.nightexpress.nightcore.db.table.Table;
import su.nightexpress.sunlight.data.DataHandler;

/**
 * Owns the {@code <prefix>_playtime_days} table. Rows are keyed by player and
 * epoch day, so the whole history of one player is a single range query and a
 * prune is a single range delete.
 */
public class PlaytimeDayDataManager {

    private final DataHandler dataHandler;

    private Table table;

    public PlaytimeDayDataManager(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void init() {
        this.table = Table.builder(this.dataHandler.getTablePrefix() + "_playtime_days")
                .withColumn(PlaytimeDayColumns.PLAYER_ID, PlaytimeDayColumns.DAY, PlaytimeDayColumns.MILLIS)
                .build();
        this.dataHandler.createTable(this.table);
    }

    public List<PlaytimeDay> load(UUID playerId) {
        Wheres<Object> wheres = Wheres.whereUUID(PlaytimeDayColumns.PLAYER_ID, o -> playerId);
        return this.dataHandler.selectWhere(this.table,
                SelectStatement.builder(PlaytimeDayQueries.ROW_MAPPER).build(), wheres);
    }

    /**
     * Rows of one player strictly older than {@code cutoffDay}, oldest first.
     * Used by the per-player prune, which folds these into the archived total.
     */
    public List<PlaytimeDay> loadOlderThan(UUID playerId, long cutoffDay) {
        Wheres<Object> wheres = Wheres.whereUUID(PlaytimeDayColumns.PLAYER_ID, o -> playerId)
                .and(PlaytimeDayColumns.DAY, Operator.SMALLER, o -> cutoffDay);
        return this.dataHandler.selectWhere(this.table,
                SelectStatement.builder(PlaytimeDayQueries.ROW_MAPPER).build(), wheres);
    }

    /**
     * Rows of every player on or after {@code fromDay}. One query serves the
     * whole leaderboard, which is why it is not split per player.
     */
    public List<PlaytimeDay> loadSince(long fromDay) {
        Wheres<Object> wheres = Wheres.where(PlaytimeDayColumns.DAY, Operator.GREATER_OR_EQUAL, o -> fromDay);
        return this.dataHandler.selectWhere(this.table,
                SelectStatement.builder(PlaytimeDayQueries.ROW_MAPPER).build(), wheres);
    }

    public List<PlaytimeDay> loadAll() {
        return this.dataHandler.selectAny(this.table,
                SelectStatement.builder(PlaytimeDayQueries.ROW_MAPPER).build());
    }

    public void save(Collection<PlaytimeDay> rows) {
        if (rows.isEmpty()) {
            return;
        }
        this.dataHandler.insert(this.table, PlaytimeDayQueries.UPSERT, rows);
    }

    public void deleteOlderThan(UUID playerId, long cutoffDay) {
        Wheres<Object> wheres = Wheres.whereUUID(PlaytimeDayColumns.PLAYER_ID, o -> playerId)
                .and(PlaytimeDayColumns.DAY, Operator.SMALLER, o -> cutoffDay);
        this.dataHandler.delete(this.table, wheres);
    }

    public void deleteRange(UUID playerId, long fromDay, long toDay) {
        Wheres<Object> wheres = Wheres.whereUUID(PlaytimeDayColumns.PLAYER_ID, o -> playerId)
                .and(PlaytimeDayColumns.DAY, Operator.GREATER_OR_EQUAL, o -> fromDay)
                .and(PlaytimeDayColumns.DAY, Operator.SMALLER_OR_EQUAL, o -> toDay);
        this.dataHandler.delete(this.table, wheres);
    }

    public void deleteAll(UUID playerId) {
        Wheres<Object> wheres = Wheres.whereUUID(PlaytimeDayColumns.PLAYER_ID, o -> playerId);
        this.dataHandler.delete(this.table, wheres);
    }
}
