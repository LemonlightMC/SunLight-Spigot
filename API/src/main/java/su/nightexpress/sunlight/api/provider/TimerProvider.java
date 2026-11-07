package su.nightexpress.sunlight.api.provider;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TimerProvider {

    Set<String> getGlobalTimerIds();

    Set<String> getPersonalTimerIds(UUID playerId);

    boolean hasGlobalTimer(String id);

    boolean hasPersonalTimer(UUID playerId, String id);

    Optional<String> getTimerState(String id);

    Optional<String> getPersonalTimerState(UUID playerId, String id);

    boolean startGlobalTimer(String id);

    boolean pauseGlobalTimer(String id);

    boolean resumeGlobalTimer(String id);

    boolean stopGlobalTimer(String id);

    boolean resetGlobalTimer(String id);

    boolean startPersonalTimer(Player player, String id);

    boolean pausePersonalTimer(Player player, String id);

    boolean resumePersonalTimer(Player player, String id);

    boolean stopPersonalTimer(Player player, String id);

    boolean resetPersonalTimer(Player player, String id);
}
