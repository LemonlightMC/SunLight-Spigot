package su.nightexpress.sunlight.moduleImpl.settings.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;

import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsModule;
import su.nightexpress.sunlight.moduleImpl.settings.config.SettingsLang;
import su.nightexpress.sunlight.moduleImpl.settings.config.SettingsPerms;

public class SettingsCommand extends CommandProvider<SettingsModule> {

    public SettingsCommand(final SettingsModule module) {
        super(module, "settings");
    }

    @Override
    public void setup() {
        this.registerRoot("settings", command -> command
                .withFullDescription(SettingsLang.COMMAND_SETTINGS_DESC.text())
                .withPermission(SettingsPerms.COMMAND_SETTINGS)
                .executes((sender, arguments) -> {
                    return this.open(sender);
                })
        ).aliases("setting", "preferences", "prefs");
    }

    private int open(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        this.module.openRoot(player);
        return 1;
    }
}