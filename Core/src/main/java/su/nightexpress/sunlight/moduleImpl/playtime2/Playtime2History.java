package su.nightexpress.sunlight.moduleImpl.playtime2;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
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
 * The aggregates are maintained rather than recomputed.
 * {@link #add(long, long)}
 * is the per-tick hot path and only ever touches today, which lies inside the
 * current day, week, month and year bucket, so each of those totals advances by
 * the same delta in constant time. The bucket boundaries are rebuilt once per
 * day; streaks depend on which days exist rather than on how much was played,
 * so
 * they are cached against a revision that only moves when a day is added or
 * removed.
 * <p>
 * Not thread safe on its own: every caller goes through the module's
 * per-player lock.
 */
public class Playtime2History {

    private final UUID playerId;
    private final TreeMap<Long, Long> days;
    private final Set<Long> dirty;

    /**
     * Sum of every retained row, kept in step with the map so {@link #total()} is
     * O(1).
     */
    private long retainedTotal;

    /** Millis of rows that retention pruning removed. */
    private long archivedTotal;

    /**
     * Bumped whenever the key set changes, i.e. a day starts or stops existing.
     * Value-only changes do not bump it, because the aggregates and streaks are
     * unaffected by them.
     */
    private long structureRevision;

    // Period buckets: rebuilt once per day, then advanced incrementally by add().
    private boolean bucketsValid;
    private long bucketDay = Long.MIN_VALUE;
    private long bucketWeekStart;
    private long bucketMonthStart;
    private long bucketYearStart;
    private long weekTotal;
    private long monthTotal;
    private long yearTotal;

    // Streaks are cached against (day, revision) so the per-tick read is a field
    // read.
    private long streakDay = Long.MIN_VALUE;
    private long streakRevision = -1L;
    private int cachedDailyStreak;
    private int cachedWeeklyStreak;

    /**
     * Wall clock of the last accrual, so a quit can credit the partial interval.
     */
    private long lastAccrualAt;

    public Playtime2History(final UUID playerId) {
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

    public void setArchivedTotal(final long archivedTotal) {
        this.archivedTotal = Math.max(0L, archivedTotal);
    }

    public Map<Long, Long> getDays() {
        return Collections.unmodifiableMap(this.days);
    }

    public boolean isEmpty() {
        return this.days.isEmpty();
    }

    public long getMillis(final long day) {
        return this.days.getOrDefault(day, 0L);
    }

    /**
     * Adds {@code deltaMs} to one day. A negative delta clamps the day at zero
     * rather than creating negative playtime.
     * <p>
     * This is the per-tick path: when the day is the one the bucket aggregates
     * were built for, every bucket advances by the applied delta without touching
     * the map beyond the single write.
     *
     * @return the resulting millis of that day.
     */
    public long add(final long day, final long deltaMs) {
        final long previous = this.days.getOrDefault(day, 0L);
        final long value = Math.max(0L, previous + deltaMs);
        final long applied = value - previous;
        if (applied == 0L) {
            return value;
        }

        if (value == 0L) {
            if (this.days.remove(day) != null) {
                this.dirty.add(day);
                this.structureRevision++;
            }
        } else {
            this.days.put(day, value);
            this.dirty.add(day);
            if (previous == 0L) {
                this.structureRevision++;
            }
        }

        this.retainedTotal += applied;

        if (this.bucketsValid && day == this.bucketDay) {
            this.weekTotal += applied;
            this.monthTotal += applied;
            this.yearTotal += applied;
        } else {
            // A day outside the cached buckets cannot be folded into them, so they
            // are rebuilt on the next read.
            this.bucketsValid = false;
        }
        return value;
    }

    /**
     * Overwrites one day. {@code 0} removes the row, which is how today's playtime
     * is zeroed by the admin commands. Also the bulk-load path, so it invalidates
     * the bucket aggregates instead of adjusting them.
     */
    public void set(final long day, final long millis) {
        final long value = Math.max(0L, millis);
        final long previous = this.days.getOrDefault(day, 0L);
        if (value == 0L) {
            if (this.days.remove(day) != null) {
                this.dirty.add(day);
                this.structureRevision++;
            }
        } else {
            this.days.put(day, value);
            this.dirty.add(day);
            if (previous == 0L) {
                this.structureRevision++;
            }
        }
        this.retainedTotal += value - previous;
        this.bucketsValid = false;
    }

    /** Sum of every retained row. */
    public long sum() {
        return this.retainedTotal;
    }

    public long sum(final long fromDay, final long toDay) {
        long total = 0L;
        for (final long millis : this.days.subMap(fromDay, true, toDay, true).values()) {
            total += millis;
        }
        return total;
    }

    /** All-time playtime: pruned rows plus everything still retained. */
    public long total() {
        return this.archivedTotal + this.retainedTotal;
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
    public int dailyStreak(final long today) {
        this.ensureStreaks(today);
        return this.cachedDailyStreak;
    }

    /**
     * Consecutive played weeks ending this week, or last week when this week has
     * not been played yet. Weeks start on Monday.
     */
    public int weeklyStreak(final long today) {
        this.ensureStreaks(today);
        return this.cachedWeeklyStreak;
    }

    public long year(final long today) {
        this.ensureBuckets(today);
        return this.yearTotal;
    }

    public long month(final long today) {
        this.ensureBuckets(today);
        return this.monthTotal;
    }

    public long week(final long today) {
        this.ensureBuckets(today);
        return this.weekTotal;
    }

    /**
     * Rebuilds the bucket totals for the calendar day {@code today}. Runs once per
     * day per player; every other call is a field comparison.
     */
    private void ensureBuckets(final long today) {
        if (this.bucketsValid && this.bucketDay == today) {
            return;
        }
        final LocalDate date = LocalDate.ofEpochDay(today);
        this.bucketDay = today;
        this.bucketWeekStart = date.with(DayOfWeek.MONDAY).toEpochDay();
        this.bucketMonthStart = date.withDayOfMonth(1).toEpochDay();
        this.bucketYearStart = date.withDayOfYear(1).toEpochDay();
        this.weekTotal = this.sum(this.bucketWeekStart, today);
        this.monthTotal = this.sum(this.bucketMonthStart, today);
        this.yearTotal = this.sum(this.bucketYearStart, today);
        this.bucketsValid = true;
    }

    private void ensureStreaks(final long today) {
        if (this.streakDay == today && this.streakRevision == this.structureRevision) {
            return;
        }
        this.ensureBuckets(today);
        this.streakDay = today;
        this.streakRevision = this.structureRevision;
        this.cachedDailyStreak = this.computeDailyStreak(today);
        this.cachedWeeklyStreak = this.computeWeeklyStreak();
    }

    private int computeDailyStreak(final long today) {
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

    private int computeWeeklyStreak() {
        if (this.days.isEmpty()) {
            return 0;
        }
        long cursor = this.sum(this.bucketWeekStart, this.bucketWeekStart + 6L) > 0L
                ? this.bucketWeekStart
                : this.bucketWeekStart - 7L;
        int streak = 0;
        while (this.sum(cursor, cursor + 6L) > 0L) {
            streak++;
            cursor -= 7L;
        }
        return streak;
    }

    /**
     * Removes every row strictly older than {@code cutoffDay} and folds their
     * millis into the archived total.
     *
     * @return the millis folded in.
     */
    public long prune(final long cutoffDay) {
        long folded = 0L;
        final var iterator = this.days.headMap(cutoffDay, false).entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Long, Long> entry = iterator.next();
            folded += entry.getValue();
            this.dirty.remove(entry.getKey());
            iterator.remove();
            this.structureRevision++;
        }
        if (folded > 0L) {
            this.archivedTotal += folded;
            this.retainedTotal -= folded;
            this.bucketsValid = false;
        }
        return folded;
    }

    /** Drops every retained row without touching the archived total. */
    public void clearRange(final long fromDay, final long toDay) {
        long removed = 0L;
        final var iterator = this.days.subMap(fromDay, true, toDay, true).entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Long, Long> entry = iterator.next();
            removed += entry.getValue();
            this.dirty.remove(entry.getKey());
            iterator.remove();
            this.structureRevision++;
        }
        if (removed > 0L) {
            this.retainedTotal -= removed;
            this.bucketsValid = false;
        }
    }

    /** Drops every retained row and zeroes the archived total. */
    public void clearAll() {
        this.days.clear();
        this.dirty.clear();
        this.retainedTotal = 0L;
        this.archivedTotal = 0L;
        this.structureRevision++;
        this.bucketsValid = false;
    }

    /**
     * Rows written since the last flush. A dirty day that no longer exists in the
     * map was zeroed and must be deleted rather than upserted.
     */
    public Set<Long> dirtyDays() {
        return Set.copyOf(this.dirty);
    }

    public void markClean(final Set<Long> days) {
        this.dirty.removeAll(days);
    }

    /** Puts days back into the dirty set, so a failed flush is retried. */
    public void markDirty(final Set<Long> days) {
        this.dirty.addAll(days);
    }

    public void markClean() {
        this.dirty.clear();
    }

    public boolean isDirty() {
        return !this.dirty.isEmpty();
    }

    /**
     * When playtime was last credited. Used to credit the fraction of an interval
     * that a player was still online for when they quit.
     */
    public long getLastAccrualAt() {
        return this.lastAccrualAt;
    }

    public void markAccrual(final long timestamp) {
        this.lastAccrualAt = timestamp;
    }

    /** The day grid of one month, keyed by epoch day. */
    public Map<Long, Long> monthGrid(final YearMonth month) {
        final long from = month.atDay(1).toEpochDay();
        final long to = month.atEndOfMonth().toEpochDay();
        return Collections.unmodifiableMap(new TreeMap<>(this.days.subMap(from, true, to, true)));
    }
}
