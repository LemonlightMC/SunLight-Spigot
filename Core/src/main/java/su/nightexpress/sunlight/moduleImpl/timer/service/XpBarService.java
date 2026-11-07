package su.nightexpress.sunlight.moduleImpl.timer.service;

import org.bukkit.entity.Player;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerKind;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XpBarService {

    private static final class Snapshot {
        final int level;
        final float exp;
        final String timerKey;
        Snapshot(int level, float exp, String timerKey) {
            this.level = level;
            this.exp = exp;
            this.timerKey = timerKey;
        }
    }

    private final Map<UUID, Snapshot> snapshots = new HashMap<>();

    public void apply(Player player, TimerInstance instance, String timerKey) {
        if (player == null || !player.isOnline()) return;
        this.snapshots.computeIfAbsent(player.getUniqueId(), id -> new Snapshot(player.getLevel(), player.getExp(), timerKey));
        Snapshot existing = this.snapshots.get(player.getUniqueId());
        if (existing != null && !timerKey.equals(existing.timerKey)) {
            // another timer took over - keep original snapshot but update owner key
            this.snapshots.put(player.getUniqueId(), new Snapshot(existing.level, existing.exp, timerKey));
        }
        long seconds = instance.getDefinition().getKind() == TimerKind.COUNTDOWN ? Math.max(0L, instance.getRemainingMillis() / 1000L) : Math.max(0L, instance.getElapsedMillis() / 1000L);
        int level = (int) Math.min(Integer.MAX_VALUE, seconds);
        float progress = (float) Math.max(0D, Math.min(1D, TimerFormat.progress(instance)));
        player.setLevel(level);
        player.setExp(progress);
    }

    public void restore(Player player, String timerKey) {
        if (player == null) return;
        Snapshot snapshot = this.snapshots.get(player.getUniqueId());
        if (snapshot == null) return;
        if (timerKey != null && !timerKey.equals(snapshot.timerKey)) return;
        if (player.isOnline()) {
            player.setLevel(snapshot.level);
            player.setExp(snapshot.exp);
        }
        this.snapshots.remove(player.getUniqueId());
    }

    public void restoreAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            Snapshot snapshot = this.snapshots.remove(player.getUniqueId());
            if (snapshot != null && player.isOnline()) {
                player.setLevel(snapshot.level);
                player.setExp(snapshot.exp);
            }
        }
    }

    public void clear(Player player) {
        this.snapshots.remove(player.getUniqueId());
    }

    public boolean isOwnedBy(UUID playerId, String timerKey) {
        Snapshot snapshot = this.snapshots.get(playerId);
        return snapshot != null && timerKey.equals(snapshot.timerKey);
    }
}
