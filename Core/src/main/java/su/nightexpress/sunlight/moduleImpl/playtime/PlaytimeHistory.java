package su.nightexpress.sunlight.moduleImpl.playtime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.moduleImpl.playtime.data.PlaytimeDay;

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
public class PlaytimeHistory {

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

    // Period buckets: rebuilt once per day, then advanced incrementally by add().
    private boolean bucketsValid;
    private long bucketDay = Long.MIN_VALUE;
    private long bucketWeekStart;
    private long bucketMonthStart;
    private long bucketYearStart;
    private long weekTotal;
    private long monthTotal;
    private long yearTotal;

    /**
     * The current run of consecutive played days, as an inclusive range. Maintained
     * by {@link #extendRuns(long)} so a steady player never triggers a scan, and
     * dropped by every removal path, which falls back to
     * {@link #rebuildRuns(long)}.
     * An empty run is expressed as {@code runEnd == Long.MIN_VALUE}.
     */
    private boolean dailyRunValid;
    private long dailyRunStart;
    private long dailyRunEnd = Long.MIN_VALUE;

    /** The same, in units of week starts (Mondays) spaced seven days apart. */
    private boolean weeklyRunValid;
    private long weeklyRunStart;
    private long weeklyRunEnd = Long.MIN_VALUE;

    /**
     * Wall clock of the last accrual, so a quit can credit the partial interval.
     */
    private long lastAccrualAt;

    public PlaytimeHistory(final UUID playerId) {
        this.playerId = playerId;
        this.days = new TreeMap<>();
        this.dirty = new HashSet<>();
    }

    /**
     * A history loaded straight from the stored rows. The caller keeps
     * responsibility for the archived total (which only it can read) and for
     * retention pruning, which folds into it.
     */
    public PlaytimeHistory(final UUID playerId, final List<PlaytimeDay> data) {
        this.playerId = playerId;
        this.days = new TreeMap<>();
        this.dirty = new HashSet<>();

        for (final PlaytimeDay row : data) {
            if (row != null && row.millis > 0L) {
                this.days.put(row.day, row.millis);
                this.retainedTotal += row.millis;
            }
        }
        this.dailyRunValid = false;
        this.weeklyRunValid = false;
        this.bucketsValid = false;
        this.lastAccrualAt = System.currentTimeMillis();
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

    public long getMillis(final long day, final PlaytimePeriod period) {
        return switch (period == null ? PlaytimePeriod.ALLTIME : period) {
            case ALLTIME -> total();
            case YEAR -> year(day);
            case MONTH -> month(day);
            case WEEK -> week(day);
            case DAY -> this.days.getOrDefault(day, 0L);
            case SESSION -> 0L;
        };
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
        if (deltaMs <= 0L) {
            return previous;
        }

        final long value = Math.max(0L, previous + deltaMs);
        this.days.put(day, value);
        this.dirty.add(day);
        if (previous == 0L) {
            // A day that did not exist until now may start or extend a streak.
            this.extendRuns(day);
        }

        this.retainedTotal += deltaMs;
        if (this.bucketsValid && day == this.bucketDay) {
            this.weekTotal += deltaMs;
            this.monthTotal += deltaMs;
            this.yearTotal += deltaMs;
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
     * the bucket aggregates and the streak runs instead of adjusting them.
     */
    public void set(final long day, final long millis) {
        final long value = Math.max(0L, millis);
        final long previous = this.days.getOrDefault(day, 0L);
        if (value == 0L) {
            if (this.days.remove(day) != null) {
                this.dirty.add(day);
                this.invalidateRuns();
            }
        } else {
            this.days.put(day, value);
            this.dirty.add(day);
            if (previous == 0L) {
                // Loading a history replays every row through here, so runs are
                // rebuilt from scratch on the first streak read rather than
                // maintained per row.
                this.invalidateRuns();
            }
        }
        this.retainedTotal += value - previous;
        this.bucketsValid = false;
    }

    public long sum(final long fromDay, final long toDay) {
        long total = 0L;
        for (final long millis : this.days.subMap(fromDay, true, toDay, true).values()) {
            total += millis;
        }
        return total;
    }

    public boolean hasPlayed(final long fromDay, final long toDay) {
        return !this.days.subMap(fromDay, true, toDay, true).isEmpty();
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
        if (!this.dailyRunValid) {
            this.rebuildRuns(today);
        }
        final long end = this.dailyRunEnd;
        if (end == Long.MIN_VALUE) {
            return 0;
        }
        // The run only counts while it still reaches today or yesterday.
        if (end != today && end != today - 1L) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, end - this.dailyRunStart + 1L);
    }

    /**
     * Consecutive played weeks ending this week, or last week when this week has
     * not been played yet. Weeks start on Monday.
     */
    public int weeklyStreak(final long today) {
        if (!this.weeklyRunValid) {
            this.rebuildRuns(today);
        }
        final long end = this.weeklyRunEnd;
        if (end == Long.MIN_VALUE) {
            return 0;
        }
        this.ensureBuckets(today);
        final long thisWeek = this.bucketWeekStart;
        if (end != thisWeek && end != thisWeek - 7L) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, (end - this.weeklyRunStart) / 7L + 1L);
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
     * Brings the bucket totals up to {@code today}.
     * <p>
     * A consecutive day is handled in constant time: the new day starts at zero
     * millis, so every bucket either carries forward unchanged or resets when its
     * own boundary was crossed. Anything else — an offline gap, or a write to a day
     * other than today — falls back to summing the ranges, which only happens once
     * on the next read after the jump.
     */
    private void ensureBuckets(final long today) {
        if (this.bucketsValid) {
            if (this.bucketDay == today) {
                return;
            }
            if (today == this.bucketDay + 1L) {
                this.rollBucketsForward(today);
                return;
            }
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

    /**
     * Advances the buckets by exactly one day. Valid because a day that has not
     * been
     * played contributes nothing, so a total only changes when the window it covers
     * changes — which is a boundary crossing, and then it restarts from zero.
     */
    private void rollBucketsForward(final long day) {
        final LocalDate date = LocalDate.ofEpochDay(day);
        final long weekStart = date.with(DayOfWeek.MONDAY).toEpochDay();
        final long monthStart = date.withDayOfMonth(1).toEpochDay();
        final long yearStart = date.withDayOfYear(1).toEpochDay();

        if (weekStart != this.bucketWeekStart) {
            this.weekTotal = 0L;
        }
        if (monthStart != this.bucketMonthStart) {
            this.monthTotal = 0L;
        }
        if (yearStart != this.bucketYearStart) {
            this.yearTotal = 0L;
        }

        this.bucketDay = day;
        this.bucketWeekStart = weekStart;
        this.bucketMonthStart = monthStart;
        this.bucketYearStart = yearStart;
        this.bucketsValid = true;
    }

    // --- Streak runs ---

    /**
     * Extends the cached runs for a day that just gained its first millisecond.
     * O(1) in the common case: a player adding today walks straight onto the end of
     * the current run.
     * <p>
     * Extending <em>backwards</em> is only safe when the day beyond it is empty. If
     * it is not, the new day bridges two separate runs, and the merged streak is
     * longer than either — that case drops the run and rebuilds on the next read.
     */
    private void extendRuns(final long day) {
        if (this.dailyRunValid) {
            if (day == this.dailyRunEnd + 1L) {
                this.dailyRunEnd = day;
            } else if (day == this.dailyRunStart - 1L) {
                if (this.days.containsKey(day - 1L)) {
                    // Bridges into an older run: the merged length is unknown here.
                    this.dailyRunValid = false;
                } else {
                    this.dailyRunStart = day;
                }
            } else if (day < this.dailyRunStart || day > this.dailyRunEnd) {
                // Starts a run somewhere else entirely: the gap means the old run
                // no longer reaches today, so it stops counting.
                this.dailyRunValid = false;
            }
        }

        if (this.weeklyRunValid) {
            final long weekStart = this.weekStartOf(day);
            if (weekStart == this.weeklyRunEnd + 7L) {
                this.weeklyRunEnd = weekStart;
            } else if (weekStart == this.weeklyRunStart - 7L) {
                if (this.hasPlayed(weekStart - 7L, weekStart - 1L)) {
                    this.weeklyRunValid = false;
                } else {
                    this.weeklyRunStart = weekStart;
                }
            } else if (weekStart < this.weeklyRunStart || weekStart > this.weeklyRunEnd) {
                this.weeklyRunValid = false;
            }
        }
    }

    private void invalidateRuns() {
        this.dailyRunValid = false;
        this.weeklyRunValid = false;
    }

    /**
     * Recomputes both runs from the map. Only reached after a removal, or after a
     * bulk load, so a steady player never pays for it.
     */
    private void rebuildRuns(final long today) {
        this.ensureBuckets(today);
        this.dailyRunValid = true;
        this.weeklyRunValid = true;

        // Daily: walk back from today (or yesterday) to the start of the run.
        this.dailyRunEnd = Long.MIN_VALUE;
        long cursor = this.days.containsKey(today) ? today : today - 1L;
        if (this.days.containsKey(cursor)) {
            this.dailyRunEnd = cursor;
            while (this.days.containsKey(cursor - 1L)) {
                cursor--;
            }
            this.dailyRunStart = cursor;
        }

        // Weekly: same walk in seven-day steps from this week (or last week).
        this.weeklyRunEnd = Long.MIN_VALUE;
        long week = this.hasPlayed(this.bucketWeekStart, this.bucketWeekStart + 6L)
                ? this.bucketWeekStart
                : this.bucketWeekStart - 7L;
        if (this.hasPlayed(week, week + 6L)) {
            this.weeklyRunEnd = week;
            while (this.hasPlayed(week - 7L, week - 1L)) {
                week -= 7L;
            }
            this.weeklyRunStart = week;
        }
    }

    private long weekStartOf(final long day) {
        return LocalDate.ofEpochDay(day).with(DayOfWeek.MONDAY).toEpochDay();
    }

    /**
     * Removes rows and folds their millis into the archived total (optional).
     * <p>
     * With a {@code fromDay} the range is inclusive on both ends, matching
     * {@code deleteRange}. Without one it removes days strictly older than
     * {@code toDay}, matching {@code deleteOlderThan}: the two must agree, or a
     * day would be archived here and left in the table, then counted twice on
     * the next load.
     *
     * @return the millis removed.
     */
    public long prune(final Long fromDay, final long toDay, final boolean archivePruned) {
        long removed = 0L;
        final var iterator = fromDay == null ? this.days.headMap(toDay, false).entrySet().iterator()
                : this.days.subMap(fromDay, true, toDay, true).entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Long, Long> entry = iterator.next();
            removed += entry.getValue();
            this.dirty.remove(entry.getKey());
            iterator.remove();
        }
        if (removed > 0L) {
            if (archivePruned) {
                this.archivedTotal += removed;
            }
            this.retainedTotal -= removed;
            this.bucketsValid = false;
            // A removal can cut a run short or split it, so it is recomputed on the
            // next streak read rather than adjusted here.
            this.invalidateRuns();
        }
        return removed;
    }

    /** Drops every retained row and zeroes the archived total. */
    public void clearAll() {
        this.days.clear();
        this.dirty.clear();
        this.retainedTotal = 0L;
        this.archivedTotal = 0L;
        this.bucketsValid = false;
        this.invalidateRuns();
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
