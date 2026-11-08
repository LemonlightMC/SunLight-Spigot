package su.nightexpress.sunlight.moduleImpl.timer.hook;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import org.bukkit.entity.Player;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerScoreboardBackend;

import java.util.*;

public class TabTimerScoreboardBackend implements TimerScoreboardBackend {

    private final Map<String, Scoreboard> scoreboards = new HashMap<>();
    private final Map<UUID, String> shownTimers = new HashMap<>();

    @Override
    public boolean isAvailable() {
        try {
            TabAPI api = TabAPI.getInstance();
            return api != null && api.getScoreboardManager() != null;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void show(String timerKey, String title, List<String> lines, Collection<Player> viewers) {
        ScoreboardManager manager = manager();
        if (manager == null) return;
        String name = scoreboardName(timerKey);
        try {
            Scoreboard scoreboard = manager.createScoreboard(name, title == null ? "" : title, lines == null ? List.of() : new ArrayList<>(lines));
            Scoreboard previous = this.scoreboards.put(timerKey, scoreboard);
            unregister(previous);
        }
        catch (Throwable ignored) {
            return;
        }
        Scoreboard scoreboard = this.scoreboards.get(timerKey);
        if (scoreboard == null) return;

        Set<UUID> current = new HashSet<>();
        for (Player viewer : viewers) {
            if (viewer == null || !viewer.isOnline()) continue;
            current.add(viewer.getUniqueId());
            TabPlayer tabPlayer = tabPlayer(viewer);
            if (tabPlayer == null) continue;
            try {
                manager.showScoreboard(tabPlayer, scoreboard);
                this.shownTimers.put(viewer.getUniqueId(), timerKey);
            }
            catch (Throwable ignored) {
            }
        }
        // reset players who previously saw this timer but are no longer viewers
        for (Iterator<Map.Entry<UUID, String>> it = this.shownTimers.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, String> entry = it.next();
            if (!timerKey.equals(entry.getValue())) continue;
            if (current.contains(entry.getKey())) continue;
            TabPlayer tabPlayer = tabPlayer(entry.getKey());
            if (tabPlayer != null) {
                try {
                    if (manager.hasCustomScoreboard(tabPlayer)) manager.resetScoreboard(tabPlayer);
                }
                catch (Throwable ignored) {
                }
            }
            it.remove();
        }
    }

    @Override
    public void clear(String timerKey, Collection<? extends Player> onlinePlayers) {
        ScoreboardManager manager = manager();
        if (manager != null) {
            for (Iterator<Map.Entry<UUID, String>> it = this.shownTimers.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UUID, String> entry = it.next();
                if (!timerKey.equals(entry.getValue())) continue;
                TabPlayer tabPlayer = tabPlayer(entry.getKey());
                if (tabPlayer != null) {
                    try { manager.resetScoreboard(tabPlayer); } catch (Throwable ignored) {}
                }
                it.remove();
            }
        }
        Scoreboard scoreboard = this.scoreboards.remove(timerKey);
        unregister(scoreboard);
    }

    @Override
    public void shutdown() {
        ScoreboardManager manager = manager();
        for (Map.Entry<UUID, String> entry : new ArrayList<>(this.shownTimers.entrySet())) {
            TabPlayer tabPlayer = tabPlayer(entry.getKey());
            if (tabPlayer != null && manager != null) {
                try { manager.resetScoreboard(tabPlayer); } catch (Throwable ignored) {}
            }
        }
        this.shownTimers.clear();
        for (Scoreboard scoreboard : this.scoreboards.values()) unregister(scoreboard);
        this.scoreboards.clear();
    }

    private static void unregister(Scoreboard scoreboard) {
        if (scoreboard == null) return;
        try {
            scoreboard.getClass().getMethod("unregister").invoke(scoreboard);
        }
        catch (Throwable ignored) {
            try { scoreboard.getClass().getMethod("remove").invoke(scoreboard); } catch (Throwable ignored2) {}
        }
    }

    private static String scoreboardName(String timerKey) {
        String base = "sltimer_" + timerKey.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        return base.length() > 48 ? base.substring(0, 48) : base;
    }

    private static ScoreboardManager manager() {
        try {
            TabAPI api = TabAPI.getInstance();
            return api == null ? null : api.getScoreboardManager();
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static TabPlayer tabPlayer(Player player) {
        return player == null ? null : tabPlayer(player.getUniqueId());
    }

    private static TabPlayer tabPlayer(UUID uuid) {
        if (uuid == null) return null;
        try {
            return TabAPI.getInstance().getPlayer(uuid);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
}
