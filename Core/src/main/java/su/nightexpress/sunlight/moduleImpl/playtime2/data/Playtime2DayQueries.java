package su.nightexpress.sunlight.moduleImpl.playtime2.data;

import java.sql.SQLException;
import java.util.UUID;

import su.nightexpress.nightcore.db.statement.RowMapper;
import su.nightexpress.nightcore.db.statement.template.InsertStatement;

public class Playtime2DayQueries {

    public static final RowMapper<Playtime2Day> ROW_MAPPER = resultSet -> {
        try {
            UUID playerId = Playtime2DayColumns.PLAYER_ID.readOrThrow(resultSet);
            long day = Playtime2DayColumns.DAY.readOrThrow(resultSet);
            long millis = Playtime2DayColumns.MILLIS.readOrThrow(resultSet);
            return new Playtime2Day(playerId, day, millis);
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
    public static final InsertStatement<Playtime2Day> UPSERT = InsertStatement.<Playtime2Day>builder()
            .setUUID(Playtime2DayColumns.PLAYER_ID, Playtime2Day::getPlayerId)
            .setLong(Playtime2DayColumns.DAY, Playtime2Day::getDay)
            .setLong(Playtime2DayColumns.MILLIS, Playtime2Day::getMillis)
            .updateOnConflict()
            .build();
}
