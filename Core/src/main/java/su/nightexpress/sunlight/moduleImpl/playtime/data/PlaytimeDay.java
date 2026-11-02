package su.nightexpress.sunlight.moduleImpl.playtime.data;

import java.util.UUID;

/**
 * One row of the daily history: how many milliseconds a player was tracked on
 * a single epoch day.
 */
public class PlaytimeDay {

    private final UUID playerId;
    public final long day;
    public final long millis;

    public PlaytimeDay(UUID playerId, long day, long millis) {
        this.playerId = playerId;
        this.day = day;
        this.millis = Math.max(0L, millis);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public long getDay() {
        return this.day;
    }

    public long getMillis() {
        return this.millis;
    }
}
