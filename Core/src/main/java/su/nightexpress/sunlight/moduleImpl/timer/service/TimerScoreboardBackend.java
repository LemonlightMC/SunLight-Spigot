package su.nightexpress.sunlight.moduleImpl.timer.service;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public interface TimerScoreboardBackend {
    void show(String timerKey, String title, List<String> lines, Collection<Player> viewers);
    void clear(String timerKey, Collection<? extends Player> onlinePlayers);
    void shutdown();
    boolean isAvailable();
}
