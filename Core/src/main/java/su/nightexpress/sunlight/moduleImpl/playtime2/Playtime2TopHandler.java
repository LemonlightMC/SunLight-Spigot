package su.nightexpress.sunlight.moduleImpl.playtime2;

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

public class Playtime2TopHandler {

    private final Playtime2Module module;
    private final Map<PlaytimePeriod, List<TopEntry>> topCache;

    private final AtomicBoolean topScanRunning = new AtomicBoolean(false);
    private volatile long topCacheTimestamp;
    private volatile long lastDbScan;
    private volatile boolean topRequested;

    public Playtime2TopHandler(final Playtime2Module module) {
        this.module = module;
        this.topCache = new ConcurrentHashMap<>();
        this.topCacheTimestamp = 0L;
        this.lastDbScan = 0L;
        this.topRequested = false;
    }

    public void unload() {
        this.topCache.clear();
        this.topCacheTimestamp = 0L;
        this.lastDbScan = 0L;
        this.topRequested = false;
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

        final Map<UUID, SunUser> merged = new LinkedHashMap<>();

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
            for (final SunUser persisted : users) {
                if (persisted == null) {
                    continue;
                }
                merged.put(persisted.getId(), persisted);
            }
            this.lastDbScan = now;
            this.topRequested = false;
            module.debug("Top database scan: " + merged.size() + " users in "
                    + ((System.nanoTime() - scanStart) / 1_000_000L) + "ms.");
        }

        try {
            final Collection<SunUser> live = module.userManager().getAll();
            if (live != null) {
                for (final SunUser user : live) {
                    if (user == null) {
                        continue;
                    }
                    merged.put(user.getId(), user);
                }
            }
        } catch (final Exception exception) {
            // Live data is best-effort; persisted rows still produce a board.
        }

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
        // from their live history, everyone else from the cached counters.
        final Map<PlaytimePeriod, List<TopEntry>> boards = new LinkedHashMap<>();
        for (final PlaytimePeriod period : List.of(PlaytimePeriod.ALLTIME, PlaytimePeriod.YEAR,
                PlaytimePeriod.MONTH, PlaytimePeriod.WEEK, PlaytimePeriod.DAY)) {
            boards.put(period, new ArrayList<>());
        }

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
            for (final Map.Entry<PlaytimePeriod, List<TopEntry>> entry : boards.entrySet()) {
                final long value = module.getEffectivePlaytime(user, entry.getKey());
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
        if (entries.size() > Playtime2Module.TOP_CACHE_LIMIT) {
            entries = new ArrayList<>(entries.subList(0, Playtime2Module.TOP_CACHE_LIMIT));
        }
        return List.copyOf(entries);
    }
}
