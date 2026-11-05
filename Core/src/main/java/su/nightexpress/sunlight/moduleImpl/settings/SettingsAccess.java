package su.nightexpress.sunlight.moduleImpl.settings;

import org.bukkit.entity.Player;

import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.settings.model.AudienceMode;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.property.UserProperty;

public final class SettingsAccess {

    private SettingsAccess() {
    }

    public static boolean isEnabled(SunLightPlugin plugin) {
        return plugin.moduleManager().isPresent(SettingsModule.class);
    }

    public static AudienceMode mode(SunLightPlugin plugin, Player player, UserProperty<String> property) {
        SunUser user = plugin.userManager().getOrFetch(player);
        return AudienceMode.byId(user.getPropertyOrDefault(property));
    }

    public static AudienceMode mode(SunUser user, UserProperty<String> property) {
        return AudienceMode.byId(user.getPropertyOrDefault(property));
    }

    /**
     * Checks whether the receiver currently accepts the sender for the given
     * audience setting. Falls back to the module's own boolean toggle when the
     * settings module is disabled, so servers that never enable it keep their old
     * behaviour.
     */
    public static boolean allows(SunLightPlugin plugin, Player receiver, Player sender,
                                 UserProperty<String> property, UserProperty<Boolean> fallback) {
        if (!isEnabled(plugin)) {
            return plugin.userManager().getOrFetch(receiver).getPropertyOrDefault(fallback);
        }
        return allows(plugin, receiver, sender, property);
    }

    public static boolean allows(SunLightPlugin plugin, Player receiver, Player sender, UserProperty<String> property) {
        return mode(plugin, receiver, property).allows(plugin, receiver, sender);
    }

    public static void setMode(SunLightPlugin plugin, Player player, UserProperty<String> property, AudienceMode mode) {
        setMode(plugin.userManager().getOrFetch(player), property, mode);
    }

    public static void setMode(SunUser user, UserProperty<String> property, AudienceMode mode) {
        user.setProperty(property, mode.id());
        user.markDirty();
    }

    /**
     * Mirrors an old boolean toggle into the richer audience mode. Used by the
     * legacy commands and menus so both entry points stay authoritative.
     */
    public static void setEnabled(SunUser user, UserProperty<String> property, boolean enabled) {
        setMode(user, property, enabled ? AudienceMode.EVERYONE : AudienceMode.NOBODY);
    }

    /**
     * Mirrors an audience mode into the legacy boolean toggle that other parts of
     * the plugin still read.
     */
    public static void syncBoolean(SunUser user, UserProperty<String> modeProperty,
                                   UserProperty<Boolean> booleanProperty) {
        boolean enabled = mode(user, modeProperty) != AudienceMode.NOBODY;
        user.setProperty(booleanProperty, enabled);
        user.markDirty();
    }
}
