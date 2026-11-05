package su.nightexpress.sunlight.moduleImpl.essential.command;

import java.util.stream.IntStream;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
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
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_AMOUNT;

public class JumpCommandProvider extends CommandProvider<EssentialModule> {

    private static final float DEF_STRENGTH = 0.42F;
    private static final float MAX_STRENGTH = 2.0F;
    private static final int STRENGTHS_AMOUNT = 10;

    private static final String PERMISSION = EssentialPerms.COMMAND + ".jump";
    private static final String PERMISSION_OTHERS = EssentialPerms.COMMAND + ".jump.others";

    private static final TextLocale DESCRIPTION = LangEntry.builder("Command.Jump.Desc")
            .text("Change jump strength.");

    private static final MessageLocale MESSAGE_SET_NOTIFY = LangEntry.builder("Command.Jump.Done.Notify")
            .chatMessage(
                    GRAY.wrap("Your jump strength has been set to " + SOFT_YELLOW.wrap(GENERIC_AMOUNT)
                            + "."));

    private static final MessageLocale MESSAGE_SET_FEEDBACK = LangEntry
            .builder("Command.Jump.Done.Target")
            .chatMessage(
                    GRAY.wrap("You have set " + WHITE.wrap(PLAYER_DISPLAY_NAME) + "'s jump strength to "
                            + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + "."));

    public JumpCommandProvider(final EssentialModule module) {
        super(module, "jump");
    }

    @Override
    public void setup() {
        this.register("jump", command -> command
                .withFullDescription(DESCRIPTION.text())
                .withPermission(PERMISSION)
                .withArguments(new IntegerArgument(CommandArgumentConstants.VALUE, 1)
                        .replaceSuggestions(ArgumentSuggestions.stringCollection(
                                info -> IntStream.range(1, STRENGTHS_AMOUNT + 1).boxed().map(String::valueOf)
                                        .toList())))
                .withOptionalArguments(CommandArgumentConstants.targetArgument())
                .executes((sender, arguments) -> {
                    return this.changeJumpStrength(sender, arguments);
                }));
    }

    private int changeJumpStrength(final CommandSender sender, final CommandArguments arguments) {
        final Object valueArg = arguments.get(CommandArgumentConstants.VALUE);
        if (!(valueArg instanceof final Integer value)) {
            return 0;
        }

        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }

        return target.runAs(this.module, sender, PERMISSION_OTHERS, (user, player) -> {
            final int strength = Math.clamp(value, 1, STRENGTHS_AMOUNT);
            final float realStrength = DEF_STRENGTH
                    + (MAX_STRENGTH - DEF_STRENGTH) * (strength - 1) / (STRENGTHS_AMOUNT - 1);

            module.setJumpStrength(player, realStrength);

            if (sender != player) {
                this.module.sendPrefixed(MESSAGE_SET_FEEDBACK, sender, replacer -> replacer
                        .with(CommonPlaceholders.PLAYER.resolver(player))
                        .with(GENERIC_AMOUNT, () -> String.valueOf(strength)));
            }

            if (!target.silent()) {
                this.module.sendPrefixed(MESSAGE_SET_NOTIFY, player, replacer -> replacer
                        .with(GENERIC_AMOUNT, () -> String.valueOf(strength)));
            }
        });
    }
}