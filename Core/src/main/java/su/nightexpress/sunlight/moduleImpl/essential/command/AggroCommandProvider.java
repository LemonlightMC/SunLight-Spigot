package su.nightexpress.sunlight.moduleImpl.essential.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialModule;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialPerms;

import static su.nightexpress.nightcore.util.Placeholders.PLAYER_DISPLAY_NAME;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.sunlight.SLPlaceholders.*;

public class AggroCommandProvider extends CommandProvider<EssentialModule> {

    private static final String COMMAND_ROOT = "aggro";

    private static final String COMMAND_SET = "set";
    private static final String COMMAND_CLEAR = "clear";

    private static final String PERMISSION_ROOT = EssentialPerms.COMMAND_AGGRO;
    private static final String PERMISSION_OTHERS = EssentialPerms.COMMAND_AGGRO_OTHERS;

    private static final TextLocale DESCRIPTION_ROOT = LangEntry.builder("Essential.Command.Aggro.Desc")
            .text("Manage mob aggro in an area.");
    private static final TextLocale DESCRIPTION_SET = LangEntry.builder("Essential.Command.Aggro.Set.Desc")
            .text("Make mobs target a player.");
    private static final TextLocale DESCRIPTION_CLEAR = LangEntry.builder("Essential.Command.Aggro.Clear.Desc")
            .text("Clear mob targets in an area.");

    private static final MessageLocale MESSAGE_SET_FEEDBACK = LangEntry.builder("Essential.Aggro.Set.Feedback")
            .chatMessage(
                    GRAY.wrap("You have set " + WHITE.wrap("mobs") + " within " + SOFT_YELLOW.wrap(GENERIC_VALUE)
                            + " blocks to target " + WHITE.wrap(PLAYER_DISPLAY_NAME) + "."));
    private static final MessageLocale MESSAGE_CLEAR_FEEDBACK = LangEntry.builder("Essential.Aggro.Clear.Feedback")
            .chatMessage(
                    GRAY.wrap("You have cleared targets of " + WHITE.wrap("mobs") + " within "
                            + SOFT_YELLOW.wrap(GENERIC_VALUE)
                            + " blocks."));

    public AggroCommandProvider(final EssentialModule module) {
        super(module, "aggro");
    }

    @Override
    public void setup() {
        this.register(COMMAND_SET, command -> this.buildSetCommand(command))
                .under(COMMAND_ROOT);
        this.register(COMMAND_CLEAR, command -> this.buildClearCommand(command))
                .under(COMMAND_ROOT);

        this.registerRoot(COMMAND_ROOT, command -> command
                .withFullDescription(DESCRIPTION_ROOT.text())
                .withPermission(PERMISSION_ROOT));
    }

    private void buildSetCommand(final dev.jorel.commandapi.CommandAPICommand command) {
        command
                .withFullDescription(DESCRIPTION_SET.text())
                .withPermission(PERMISSION_ROOT)
                .withArguments(new StringArgument(CommandArgumentConstants.PLAYER))
                .withOptionalArguments(new IntegerArgument(CommandArgumentConstants.RADIUS, 1))
                .withOptionalArguments(new IntegerArgument(CommandArgumentConstants.TIME, 0))
                .executes((sender, arguments) -> {
                    return this.setAggro(sender, arguments);
                });
    }

    private void buildClearCommand(final dev.jorel.commandapi.CommandAPICommand command) {
        command
                .withFullDescription(DESCRIPTION_CLEAR.text())
                .withPermission(PERMISSION_ROOT)
                .withOptionalArguments(new IntegerArgument(CommandArgumentConstants.RADIUS, 1))
                .executes((sender, arguments) -> {
                    return this.clearAggro(sender, arguments);
                });
    }

    private int setAggro(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player executor)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        if (!(arguments.get(CommandArgumentConstants.PLAYER) instanceof final String playerName)
                || playerName.isBlank()) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        final CommandArgumentConstants.Target target = new CommandArgumentConstants.Target(playerName, false);

        final int radius = (arguments.get(CommandArgumentConstants.RADIUS) instanceof final Integer r && r > 0) ? r
                : 16;
        final int duration = (arguments.get(CommandArgumentConstants.TIME) instanceof final Integer d && d >= 0) ? d
                : 0;

        return target.runAs(this.module, sender, PERMISSION_OTHERS, (user, player) -> {
            this.module.setAggro(executor, radius, duration * 20);

            this.module.sendPrefixed(MESSAGE_SET_FEEDBACK, sender, builder -> builder
                    .with(CommonPlaceholders.PLAYER.resolver(player))
                    .with(su.nightexpress.sunlight.SLPlaceholders.GENERIC_VALUE, () -> String.valueOf(radius)));
        });
    }

    private int clearAggro(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player executor)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }
        final int radius = (arguments.get(CommandArgumentConstants.RADIUS) instanceof final Integer r && r > 0) ? r
                : 16;

        this.module.clearAggro(executor, radius);
        this.module.sendPrefixed(MESSAGE_CLEAR_FEEDBACK, sender, builder -> builder
                .with(su.nightexpress.sunlight.SLPlaceholders.GENERIC_VALUE, () -> String.valueOf(radius)));
        return 1;
    }
}