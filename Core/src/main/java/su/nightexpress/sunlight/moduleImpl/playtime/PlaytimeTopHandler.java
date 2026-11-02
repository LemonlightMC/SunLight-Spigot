package su.nightexpress.sunlight.moduleImpl.playtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;

import su.nightexpress.sunlight.data.DataQueries;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.user.SunUser;

public class PlaytimeTopHandler {

    private final PlaytimeModule module;
    private final Map<PlaytimePeriod, List<TopEntry>> topCache;

    private final AtomicBoolean topScanRunning = new AtomicBoolean(false);
    private volatile long topCacheTimestamp;
    private volatile long lastDbScan;
    private volatile boolean topRequested;
    private volatile Map<UUID, SunUser> lastMerged = Map.of();

    /**
     * Day/week/month/year totals scanned in one query, so offline players can be
     * ranked for every period without a load each. Empty until the first scan.
     */
    private volatile Map<UUID, PlaytimeModule.PeriodTotals> periodTotals = Map.of();

    public PlaytimeTopHandler(final PlaytimeModule module) {
        this.module = module;
        this.topCache = new ConcurrentHashMap<>();
        this.topCacheTimestamp = 0L;
        this.lastDbScan = 0L;
        this.topRequested = false;
    }

    public void unload() {
        this.topCache.clear();
        this.periodTotals = Map.of();
        this.topCacheTimestamp = 0L;
        this.lastDbScan = 0L;
        this.topRequested = false;
        this.lastMerged = Map.of();
    }

    public record TopEntry(UUID id, String name, long value) {
    }

    public List<TopEntry> getTop(PlaytimePeriod period) {
        this.topRequested = true;
        period = period == null ? PlaytimePeriod.ALLTIME : period;
        final List<TopEntry> cached = this.topCache.getOrDefault(period, List.of());
        if (System.currentTimeMillis() - this.topCacheTimestamp > module.getSettings().getTopRefreshInterval()) {
            this.refreshTopCacheAsync();
        }
        return cached;
    }

    public List<TopEntry> board(final PlaytimePeriod period) {
        return this.topCache.getOrDefault(period == null ? PlaytimePeriod.ALLTIME : period, List.of());
    }

    public void requestTop() {
        this.topRequested = true;
    }

    public void refreshTopCacheAsync() {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(module.plugin(), task -> this.refreshTopCache());
        } catch (final Exception exception) {
            this.refreshTopCache();
        }
    }

    private void refreshTopCache() {
        if (!this.topScanRunning.compareAndSet(false, true)) {
            return;
        }
        try {
            this.refreshTopCacheUnsafe();
        } finally {
            this.topScanRunning.set(false);
        }
    }

    private void refreshTopCacheUnsafe() {
        final long now = System.currentTimeMillis();
        final boolean useDB = (this.topRequested || this.lastDbScan == 0L)
                && now - this.lastDbScan > module.getSettings().getTopDbScanInterval() * 50L;

        final Map<UUID, SunUser> merged = new LinkedHashMap<>(this.lastMerged);

        if (useDB) {
            final long scanStart = System.nanoTime();
            Collection<SunUser> users = List.of();
            try {
                users = module.plugin().dataHandler().selectAny(
                        module.plugin().dataHandler().getUsersTable(),
                        DataQueries.SELECT_USER);
            } catch (final Exception exception) {
                module.debug("Top refresh: database scan failed, falling back to cached users.");
            }
            merged.clear();
            for (final SunUser persisted : users) {
                if (persisted == null) {
                    continue;
                }
                merged.put(persisted.getId(), persisted);
            }
            // One range query for every player's day/week/month/year totals, so
            // offline players are ranked without a load per player.
            this.periodTotals = module.periodTotals();
            this.lastDbScan = now;
            this.topRequested = false;
            module.debug("Top database scan: " + merged.size() + " users in "
                    + ((System.nanoTime() - scanStart) / 1_000_000L) + "ms.");
        }

        // Refresh already-loaded (online) users without hitting the database.
        try {
            for (final org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                final SunUser user = module.userManager().getOrFetch(player);
                if (user != null) {
                    merged.put(user.getId(), user);
                }
            }
        } catch (final Exception exception) {
            // Live data is best-effort; persisted rows still produce a board.
        }

        this.lastMerged = Map.copyOf(merged);
        this.rebuildTopBoards(merged);
        this.topCacheTimestamp = System.currentTimeMillis();
    }

    private void rebuildTopBoards(final Map<UUID, SunUser> users) {
        if (users.isEmpty()) {
            for (final PlaytimePeriod period : PlaytimePeriod.values()) {
                this.topCache.put(period, List.of());
            }
            return;
        }

        // One pass over the users, one board per period. Online players answer
        // from their live history, offline ones from the scanned period totals.
        final Map<PlaytimePeriod, List<TopEntry>> boards = new LinkedHashMap<>();
        for (final PlaytimePeriod period : List.of(PlaytimePeriod.ALLTIME, PlaytimePeriod.YEAR,
                PlaytimePeriod.MONTH, PlaytimePeriod.WEEK, PlaytimePeriod.DAY)) {
            boards.put(period, new ArrayList<>());
        }

        final Map<UUID, PlaytimeModule.PeriodTotals> totals = this.periodTotals;

        for (final SunUser user : users.values()) {
            if (user == null) {
                continue;
            }
            final String name = user.getName();
            if (name == null || name.isBlank()) {
                continue;
            }
            final UUID id = user.getId();
            if (id == null) {
                continue;
            }
            // A player with no rows this year has zero for every period board.
            // The supplier must never be null here: falling back to a load would
            // mean one database read per offline player inside this scan.
            final PlaytimeModule.PeriodTotals scanned = totals.getOrDefault(id,
                    new PlaytimeModule.PeriodTotals(0L, 0L, 0L, 0L));
            for (final Map.Entry<PlaytimePeriod, List<TopEntry>> entry : boards.entrySet()) {
                final PlaytimePeriod period = entry.getKey();
                final long value = module.getEffectivePlaytime(user, period, scanned::get);
                if (value > 0L) {
                    entry.getValue().add(new TopEntry(id, name, value));
                }
            }
        }

        final Comparator<TopEntry> order = (first, second) -> Long.compare(second.value(), first.value());
        for (final Map.Entry<PlaytimePeriod, List<TopEntry>> entry : boards.entrySet()) {
            this.topCache.put(entry.getKey(), topSnapshot(entry.getValue(), order));
        }
    }

    private static List<TopEntry> topSnapshot(List<TopEntry> entries, final Comparator<TopEntry> order) {
        if (entries.isEmpty()) {
            return List.of();
        }
        entries.sort(order);
        if (entries.size() > PlaytimeModule.TOP_CACHE_LIMIT) {
            entries = new ArrayList<>(entries.subList(0, PlaytimeModule.TOP_CACHE_LIMIT));
        }
        return List.copyOf(entries);
    }
}
