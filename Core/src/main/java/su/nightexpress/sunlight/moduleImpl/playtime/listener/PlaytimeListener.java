package su.nightexpress.sunlight.moduleImpl.playtime.listener;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeModule;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeProperties;
import su.nightexpress.sunlight.user.SunUser;

public class PlaytimeListener extends AbstractListener<SunLightPlugin> {

    private final PlaytimeModule module;

    public PlaytimeListener(final SunLightPlugin plugin, final PlaytimeModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.plugin.runTaskAsync(() -> {
            try {
                this.module.loadHistory(player.getUniqueId());
            } catch (final Exception exception) {
                this.module
                        .debug("Playtime history load failed for " + player.getName() + ": " + exception.getMessage());
                return;
            }
            this.plugin.runTask(() -> {
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
                    this.module.debug(
                            "Playtime session start failed for " + player.getName() + ": " + exception.getMessage());
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();
        final boolean wasAfk = this.module.getSettings().isExcludeAfkEnabled()
                && this.plugin.afkProvider().map(provider -> provider.isAfk(player)).orElse(false);
        try {
            final SunUser user = this.module.userManager().getOrFetch(player);
            if (user != null) {
                user.setProperty(PlaytimeProperties.LAST_SEEN, System.currentTimeMillis());
            }
        } catch (final Exception ignored) {
        }
        // Flush and drop the cached history off the main thread.
        this.plugin.runTaskAsync(() -> this.module.closeSession(playerId, wasAfk));
    }
}
