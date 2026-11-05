package su.nightexpress.sunlight.moduleImpl.settings.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsModule;

public class SettingsListener extends AbstractListener<SunLightPlugin> {

    private final SettingsModule module;

    public SettingsListener(SunLightPlugin plugin, SettingsModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.module.applyPersistentEffects(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Death clears potion effects, so persistent effects have to be restored a
        // tick after the respawn packet has been sent.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    module.applyPersistentEffects(player);
                }
            }
        }.runTask(this.plugin);
    }
}
