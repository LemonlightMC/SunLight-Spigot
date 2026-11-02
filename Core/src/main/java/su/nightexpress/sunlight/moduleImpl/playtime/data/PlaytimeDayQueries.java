package su.nightexpress.sunlight.moduleImpl.playtime.data;

import java.sql.SQLException;
import java.util.UUID;

import su.nightexpress.nightcore.db.statement.RowMapper;
import su.nightexpress.nightcore.db.statement.template.InsertStatement;

public class PlaytimeDayQueries {

    public static final RowMapper<PlaytimeDay> ROW_MAPPER = resultSet -> {
        try {
            UUID playerId = PlaytimeDayColumns.PLAYER_ID.readOrThrow(resultSet);
            long day = PlaytimeDayColumns.DAY.readOrThrow(resultSet);
            long millis = PlaytimeDayColumns.MILLIS.readOrThrow(resultSet);
            return new PlaytimeDay(playerId, day, millis);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    /**
     * Upsert: the composite primary key is detected as the conflict key, so a
     * second write for the same player and day overwrites {@code millis} instead
     * of failing.
     */
    public static final InsertStatement<PlaytimeDay> UPSERT = InsertStatement.<PlaytimeDay>builder()
            .setUUID(PlaytimeDayColumns.PLAYER_ID, PlaytimeDay::getPlayerId)
            .setLong(PlaytimeDayColumns.DAY, PlaytimeDay::getDay)
            .setLong(PlaytimeDayColumns.MILLIS, PlaytimeDay::getMillis)
            .updateOnConflict()
            .build();
}
