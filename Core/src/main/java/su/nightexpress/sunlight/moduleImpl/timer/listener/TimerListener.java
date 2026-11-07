package su.nightexpress.sunlight.moduleImpl.timer.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TimerListener extends AbstractListener<SunLightPlugin> {

    private final TimerModule module;

    public TimerListener(SunLightPlugin plugin, TimerModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        String type = event.getEntityType().name().toUpperCase(Locale.ROOT);
        if (event.getEntityType() == EntityType.ENDER_DRAGON || "ENTITY_DEATH".equals(type) || event.getEntity() != null) {
            for (TimerInstance instance : new ArrayList<>(this.module.engine().globals().all())) {
                this.module.engine().triggerEvent(instance, true, "ENTITY_DEATH", type, null);
            }
            // personal timers: check loaded personal timers (all owners online or cached)
            for (Map.Entry<UUID, Map<String, TimerInstance>> entry : this.module.engine().personals().runtimeEntries()) {
                for (TimerInstance instance : new ArrayList<>(entry.getValue().values())) {
                    this.module.engine().triggerEvent(instance, false, "ENTITY_DEATH", type, entry.getKey());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (TimerInstance instance : new ArrayList<>(this.module.engine().personals().getOrLoad(player).values())) {
            this.module.engine().triggerEvent(instance, false, "PLAYER_DEATH", "", player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // load personal timers so schedules can run
        this.module.engine().personals().getOrLoad(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (TimerInstance instance : new ArrayList<>(this.module.engine().personals().getOrLoad(player).values())) {
            this.module.engine().triggerEvent(instance, false, "PLAYER_QUIT", "", player.getUniqueId());
        }
        this.module.engine().displays().clearTimerForOwner(player.getUniqueId());
        this.module.engine().personals().unload(player.getUniqueId());
    }
}
