package su.nightexpress.sunlight.moduleImpl.essential.command;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialModule;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialPerms;
import su.nightexpress.sunlight.teleport.TeleportContext;
import su.nightexpress.sunlight.teleport.TeleportFlag;
import su.nightexpress.sunlight.teleport.TeleportType;

public class LookTpCommandProvider extends CommandProvider<EssentialModule> {

    private static final String PERMISSION = EssentialPerms.COMMAND + ".looktp";

    private static final TextLocale DESCRIPTION = LangEntry.builder("Command.LookTp.Desc").text(
            "Teleport to the block you are looking at.");

    public LookTpCommandProvider(final EssentialModule module) {
        super(module, "looktp");
    }

    @Override
    public void setup() {
        this.register("looktp", command -> command
                .withFullDescription(DESCRIPTION.text())
                .withPermission(PERMISSION)
                .withRequirement(sender -> sender instanceof Player)
                .withOptionalArguments(CommandArgumentConstants.targetArgument())
                .executes(this::execute));
    }

    private int execute(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player executor)) {
            return 0;
        }
        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(su.nightexpress.nightcore.core.config.CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }

        final Block sight = executor.getTargetBlockExact(80);
        if (sight == null) {
            return 0;
        }
        final Location destination = sight.getLocation().add(0.5, 1.0, 0.5);
        destination.setYaw(executor.getLocation().getYaw());
        destination.setPitch(executor.getLocation().getPitch());

        return target.runAs(this.module, sender, PERMISSION, (user, player) -> {
            final TeleportContext context = TeleportContext.builder(this.module, player,
                    destination)
                    .sender(sender)
                    .withFlag(TeleportFlag.LOOK_FOR_SURFACE)
                    .withFlag(TeleportFlag.AVOID_LAVA)
                    .withFlag(TeleportFlag.CENTERED)
                    .build();
            this.module.teleportManager().teleportAsync(context, TeleportType.OTHER);
        });
    }
}