package su.nightexpress.sunlight.moduleImpl.playtime2.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.playtime2.Playtime2Module;
import su.nightexpress.sunlight.moduleImpl.playtime2.Playtime2Properties;
import su.nightexpress.sunlight.user.SunUser;

public class Playtime2Listener extends AbstractListener<SunLightPlugin> {

    private final Playtime2Module module;

    public Playtime2Listener(final SunLightPlugin plugin, final Playtime2Module module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // The history load hits the database, so it stays off the main thread. The
        // session only starts once the rows are cached.
        this.plugin.runTaskAsync(() -> {
            try {
                this.module.loadHistory(player.getUniqueId());
            } catch (final Exception exception) {
                this.module.debug("Playtime history load failed for " + player.getName() + ": " + exception.getMessage());
                return;
            }
            this.plugin.runTask(() -> this.startSession(player));
        });
    }

    private void startSession(final Player player) {
        if (!player.isOnline()) {
            this.module.releaseHistory(player.getUniqueId());
            return;
        }
        try {
            final SunUser user = this.module.userManager().getOrFetch(player);
            if (user == null) {
                return;
            }
            this.module.startSession(user);
        } catch (final Exception exception) {
            this.module.debug("Playtime session start failed for " + player.getName() + ": " + exception.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        try {
            final SunUser user = this.module.userManager().getOrFetch(player);
            if (user != null) {
                user.setProperty(Playtime2Properties.LAST_SEEN, System.currentTimeMillis());
            }
        } catch (final Exception ignored) {
        }
        // Flush and drop the cached history off the main thread.
        this.plugin.runTaskAsync(() -> this.module.releaseHistory(player.getUniqueId()));
    }
}
