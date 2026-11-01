package su.nightexpress.sunlight.moduleImpl.playtime2.data;

import java.util.UUID;

import su.nightexpress.nightcore.db.column.Column;

/**
 * Columns of the {@code <prefix>_playtime_days} table. The primary key is the
 * composite of {@link #PLAYER_ID} and {@link #DAY}, so one row exists per
 * player per epoch day.
 */
public class Playtime2DayColumns {

    public static final Column<UUID> PLAYER_ID = Column.uuidType("playerId").primaryKey().build();
    public static final Column<Long> DAY = Column.longType("day").primaryKey().build();
    public static final Column<Long> MILLIS = Column.longType("millis").build();
}
