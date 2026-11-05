package su.nightexpress.sunlight.moduleImpl.greetings.listener;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.EventUtils;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.api.provider.dto.GreetingMessageType;
import su.nightexpress.sunlight.moduleImpl.greetings.GreetingsModule;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsAccess;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsProperties;
import su.nightexpress.sunlight.moduleImpl.settings.model.AudienceMode;
import su.nightexpress.sunlight.user.property.UserProperty;

public class GreetingsListener extends AbstractListener<SunLightPlugin> {

    private final GreetingsModule module;

    public GreetingsListener(final SunLightPlugin plugin, final GreetingsModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event) {
        boolean isFirstJoin;
        if (module.userManager().getOrFetch(event.getPlayer()).isFirstTimeJoined()
                && module.getAvailableMessage(event.getPlayer(), GreetingMessageType.FIRST_JOIN) != null) {
            isFirstJoin = true;
        } else {
            isFirstJoin = false;
        }

        NightComponent message = module.getMessage(event.getPlayer(),
                isFirstJoin ? GreetingMessageType.FIRST_JOIN : GreetingMessageType.JOIN);
        if (message == null) {
            if (!isFirstJoin) {
                return;
            }
            message = module.getMessage(event.getPlayer(), GreetingMessageType.JOIN);
            if (message == null) {
                return;
            }
        }
        this.broadcast(event.getPlayer(), message, SettingsProperties.JOIN_MESSAGE_MODE,
                component -> EventUtils.getAdapter().setJoinMessage(event, component));

        Bukkit.getScheduler().runTaskAsynchronously(module.plugin(), () -> {
            if (!module.settings.isJoinCommandsEnabled())
                return;

            final List<String> commands = isFirstJoin ? module.settings.getJoinCommandsFirst()
                    : module.settings.getJoinCommandsDefault();
            Players.dispatchCommands(event.getPlayer(), commands);
        });

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent event) {
        final NightComponent message = module.getMessage(event.getPlayer(), GreetingMessageType.QUIT);
        if (message == null) {
            return;
        }
        this.broadcast(event.getPlayer(), message, SettingsProperties.QUIT_MESSAGE_MODE,
                component -> EventUtils.getAdapter().setQuitMessage(event, component));
    }

    private void broadcast(Player source, NightComponent message, UserProperty<String> property,
            Consumer<NightComponent> eventMessage) {
        if (!SettingsAccess.isEnabled(this.plugin)) {
            eventMessage.accept(message);
            return;
        }

        AudienceMode mode = SettingsAccess.mode(this.plugin, source, property);
        if (mode == AudienceMode.EVERYONE) {
            eventMessage.accept(message);
            return;
        }

        // Suppress the server-wide message and deliver it only to the audience the
        // player picked. The source never needs to see their own join/quit line.
        eventMessage.accept(null);
        if (mode == AudienceMode.NOBODY) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(source)) {
                continue;
            }
            if (mode.allows(this.plugin, source, viewer)) {
                Players.sendMessage(viewer, message);
            }
        }
    }
}
