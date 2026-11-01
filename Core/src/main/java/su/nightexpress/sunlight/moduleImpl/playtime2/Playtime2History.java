package su.nightexpress.sunlight.moduleImpl.playtime2;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The in-memory daily history of one player. The day map is the source of
 * truth for every period total, both streaks and the calendar; the archived
 * total holds the millis of rows that retention pruning removed, so the
 * all-time total never shrinks.
 * <p>
 * Not thread safe on its own: every caller goes through the module's
 * per-player lock.
 */
public class Playtime2History {

    private final UUID playerId;
    private final TreeMap<Long, Long> days;
    private final Set<Long> dirty;
    private long archivedTotal;

    public Playtime2History(UUID playerId) {
        this.playerId = playerId;
        this.days = new TreeMap<>();
        this.dirty = new HashSet<>();
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public long getArchivedTotal() {
        return this.archivedTotal;
    }

    public void setArchivedTotal(long archivedTotal) {
        this.archivedTotal = Math.max(0L, archivedTotal);
    }

    public Map<Long, Long> getDays() {
        return Collections.unmodifiableMap(this.days);
    }

    public boolean isEmpty() {
        return this.days.isEmpty();
    }

    public long getMillis(long day) {
        return this.days.getOrDefault(day, 0L);
    }

    /**
     * Adds {@code deltaMs} to one day. A negative delta clamps the day at zero
     * rather than creating negative playtime.
     *
     * @return the resulting millis of that day.
     */
    public long add(long day, long deltaMs) {
        long value = Math.max(0L, this.days.getOrDefault(day, 0L) + deltaMs);
        if (value == 0L) {
            if (this.days.remove(day) != null) {
                this.dirty.add(day);
            }
            return 0L;
        }
        this.days.put(day, value);
        this.dirty.add(day);
        return value;
    }

    /**
     * Overwrites one day. {@code 0} removes the row, which is how today's playtime
     * is zeroed by the admin commands.
     */
    public void set(long day, long millis) {
        if (millis <= 0L) {
            if (this.days.remove(day) != null) {
                this.dirty.add(day);
            }
            return;
        }
        this.days.put(day, Math.max(0L, millis));
        this.dirty.add(day);
    }

    public long sum() {
        long total = 0L;
        for (long millis : this.days.values()) {
            total += millis;
        }
        return total;
    }

    public long sum(long fromDay, long toDay) {
        long total = 0L;
        for (long millis : this.days.subMap(fromDay, true, toDay, true).values()) {
            total += millis;
        }
        return total;
    }

    public boolean hasPlayed(long fromDay, long toDay) {
        return !this.days.subMap(fromDay, true, toDay, true).isEmpty();
    }

    /** All-time playtime: pruned rows plus everything still retained. */
    public long total() {
        return this.archivedTotal + this.sum();
    }

    public int daysPlayed() {
        return this.days.size();
    }

    public Long firstDay() {
        return this.days.isEmpty() ? null : this.days.firstKey();
    }

    public Long lastDay() {
        return this.days.isEmpty() ? null : this.days.lastKey();
    }

    /**
     * Consecutive played days ending today, or yesterday when today has not been
     * played yet. A gap of a single day breaks the streak.
     */
    public int dailyStreak(long today) {
        if (this.days.isEmpty()) {
            return 0;
        }
        long cursor = this.days.containsKey(today) ? today : today - 1L;
        int streak = 0;
        while (this.days.containsKey(cursor)) {
            streak++;
            cursor--;
        }
        return streak;
    }

    /**
     * Consecutive played weeks ending this week, or last week when this week has
     * not been played yet. Weeks start on Monday.
     */
    public int weeklyStreak(long weekStart) {
        if (this.days.isEmpty()) {
            return 0;
        }
        long cursor = this.hasPlayed(weekStart, weekStart + 6L) ? weekStart : weekStart - 7L;
        int streak = 0;
        while (this.hasPlayed(cursor, cursor + 6L)) {
            streak++;
            cursor -= 7L;
        }
        return streak;
    }

    public long year(long today, ZoneId zone) {
        LocalDate date = LocalDate.ofEpochDay(today);
        return this.sum(date.withDayOfYear(1).toEpochDay(), today);
    }

    public long month(long today, ZoneId zone) {
        LocalDate date = LocalDate.ofEpochDay(today);
        return this.sum(date.withDayOfMonth(1).toEpochDay(), today);
    }

    public long week(long today) {
        LocalDate date = LocalDate.ofEpochDay(today);
        return this.sum(date.with(DayOfWeek.MONDAY).toEpochDay(), today);
    }

    /**
     * Removes every row strictly older than {@code cutoffDay} and folds their
     * millis into the archived total.
     *
     * @return the millis folded in.
     */
    public long prune(long cutoffDay) {
        long folded = 0L;
        var iterator = this.days.headMap(cutoffDay, false).entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            folded += entry.getValue();
            this.dirty.remove(entry.getKey());
            iterator.remove();
        }
        this.archivedTotal += folded;
        return folded;
    }

    /** Drops every retained row without touching the archived total. */
    public void clearRange(long fromDay, long toDay) {
        var iterator = this.days.subMap(fromDay, true, toDay, true).entrySet().iterator();
        while (iterator.hasNext()) {
            this.dirty.remove(iterator.next().getKey());
            iterator.remove();
        }
    }

    /** Drops every retained row and zeroes the archived total. */
    public void clearAll() {
        this.days.clear();
        this.dirty.clear();
        this.archivedTotal = 0L;
    }

    /**
     * Rows written since the last flush. A dirty day that no longer exists in the
     * map was zeroed and must be deleted rather than upserted.
     */
    public Set<Long> dirtyDays() {
        return this.dirty;
    }

    public boolean isDirty() {
        return !this.dirty.isEmpty();
    }

    public void markClean(Set<Long> days) {
        this.dirty.removeAll(days);
    }

    public void markClean() {
        this.dirty.clear();
    }

    /** Puts days back into the dirty set, so a failed flush is retried. */
    public void markDirty(Set<Long> days) {
        this.dirty.addAll(days);
    }

    /** The day grid of one month, keyed by epoch day. */
    public Map<Long, Long> monthGrid(YearMonth month) {
        long from = month.atDay(1).toEpochDay();
        long to = month.atEndOfMonth().toEpochDay();
        return Collections.unmodifiableMap(this.days.subMap(from, true, to, true));
    }
}
