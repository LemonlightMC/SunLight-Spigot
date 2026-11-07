package su.nightexpress.sunlight.moduleImpl.extras.command;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasModule;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasPerms;
import su.nightexpress.sunlight.utils.Utils;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.BR;

public class BroadcastCommandProvider extends CommandProvider<ExtrasModule> {

    private static final String PERMISSION = ExtrasPerms.COMMAND + ".broadcast";
    private static final TextLocale DESCRIPTION = LangEntry.builder("Command.Broadcast.Desc")
            .text("Broadcast a message.");

    public BroadcastCommandProvider(final ExtrasModule module) {
        super(module, "broadcast");
    }

    @Override
    public void setup() {
        this.register("broadcast", command -> command
                .withFullDescription(DESCRIPTION.text())
                .withPermission(PERMISSION)
                .withArguments(new GreedyStringArgument(CommandArgumentConstants.TEXT))
                .executes((sender, arguments) -> {
                    return this.broadcast(sender, arguments);
                }));
    }

    private int broadcast(final CommandSender sender, final CommandArguments arguments) {
        if (!(arguments.get(CommandArgumentConstants.TEXT) instanceof final String text)) {
            return 0;
        }

        final String format = String.join(BR, this.module.settings().broadcastFormat.get());
        final NightComponent component = NightMessage.parse(format.replace(SLPlaceholders.GENERIC_MESSAGE, text));

        Utils.onlinePlayers().forEach(player -> Players.sendMessage(player, component));
        Players.sendMessage(this.module.plugin().getServer().getConsoleSender(), component);
        return 1;
    }
}
