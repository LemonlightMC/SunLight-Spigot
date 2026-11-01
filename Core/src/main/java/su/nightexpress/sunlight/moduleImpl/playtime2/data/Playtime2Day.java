package su.nightexpress.sunlight.moduleImpl.playtime2.data;

import java.util.UUID;

/**
 * One row of the daily history: how many milliseconds a player was tracked on
 * a single epoch day.
 */
public class Playtime2Day {

    private final UUID playerId;
    private final long day;
    private final long millis;

    public Playtime2Day(UUID playerId, long day, long millis) {
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
