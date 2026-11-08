package su.nightexpress.sunlight.moduleImpl.essential.command;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.sunlight.api.provider.mode.ModifyMode;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialModule;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialPerms;

import static su.nightexpress.nightcore.util.Placeholders.PLAYER_DISPLAY_NAME;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.sunlight.SLPlaceholders.*;

public class ExperienceCommandsProvider extends CommandProvider<EssentialModule> {

        private static final String PERMISSION_LEVEL = EssentialPerms.COMMAND + ".experience.level";
        private static final String PERMISSION_LEVEL_OTHERS = EssentialPerms.COMMAND + ".experience.level.others";

        private static final String PERMISSION_XP = EssentialPerms.COMMAND + ".experience.xp";
        private static final String PERMISSION_XP_OTHERS = EssentialPerms.COMMAND + ".experience.xp.others";

        private static final String PERMISSION_ROOT = EssentialPerms.COMMAND + ".experience.root";

        private static final TextLocale DESCRIPTION_ROOT = LangEntry.builder("Command.Experience.Root.Desc")
                        .text("Experience commands.");

        private static final TextLocale DESCRIPTION_LEVEL_ADD = LangEntry.builder("Command.Experience.AddLevel.Desc")
                        .text("Add XP levels.");
        private static final TextLocale DESCRIPTION_LEVEL_SET = LangEntry.builder("Command.Experience.SetLevel.Desc")
                        .text("Set XP levels.");
        private static final TextLocale DESCRIPTION_LEVEL_REMOVE = LangEntry
                        .builder("Command.Experience.RemoveLevel.Desc")
                        .text("Remove XP levels.");
        private static final TextLocale DESCRIPTION_LEVEL_RESET = LangEntry
                        .builder("Command.Experience.ResetLevel.Desc")
                        .text("Reset XP levels.");

        private static final TextLocale DESCRIPTION_XP_ADD = LangEntry.builder("Command.Experience.AddXP.Desc")
                        .text("Add experience points.");
        private static final TextLocale DESCRIPTION_XP_SET = LangEntry.builder("Command.Experience.SetXP.Desc")
                        .text("Set experience points.");
        private static final TextLocale DESCRIPTION_XP_REMOVE = LangEntry.builder("Command.Experience.RemoveXP.Desc")
                        .text("Remove experience points.");
        private static final TextLocale DESCRIPTION_XP_RESET = LangEntry.builder("Command.Experience.ResetXP.Desc")
                        .text("Reset experience points.");

        private static final MessageLocale MESSAGE_XP_SET_FEEDBACK = LangEntry
                        .builder("Command.Experience.SetXP.Feedback")
                        .chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("You have set " + WHITE.wrap(PLAYER_DISPLAY_NAME) + "'s total XP to "
                                                        + SOFT_YELLOW.wrap(GENERIC_VALUE) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_XP_SET_NOTIFY = LangEntry.builder("Command.Experience.SetXP.Notify")
                        .chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("Your total XP has been set to " + SOFT_YELLOW.wrap(GENERIC_VALUE)
                                                        + "."));

        private static final MessageLocale MESSAGE_XP_ADD_FEEDBACK = LangEntry
                        .builder("Command.Experience.AddXP.Feedback")
                        .chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("You have added " + SOFT_GREEN.wrap(GENERIC_VALUE) + " XP to "
                                                        + WHITE.wrap(PLAYER_DISPLAY_NAME) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_XP_ADD_NOTIFY = LangEntry.builder("Command.Experience.AddXP.Notify")
                        .chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("You have been given " + SOFT_GREEN.wrap(GENERIC_VALUE) + " XP."));

        private static final MessageLocale MESSAGE_XP_REMOVE_FEEDBACK = LangEntry
                        .builder("Command.Experience.RemoveXP.Feedback").chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("You have took " + SOFT_RED.wrap(GENERIC_VALUE) + " XP from "
                                                        + WHITE.wrap(PLAYER_DISPLAY_NAME) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_XP_REMOVE_NOTIFY = LangEntry
                        .builder("Command.Experience.RemoveXP.Notify").chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("You have been taken " + SOFT_RED.wrap(GENERIC_VALUE) + " XP."));

        private static final MessageLocale MESSAGE_XP_RESET_FEEDBACK = LangEntry
                        .builder("Command.Experience.RemoveXP.Feedback").chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("You have resetted "
                                                        + WHITE.wrap(PLAYER_DISPLAY_NAME) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_XP_RESET_NOTIFY = LangEntry
                        .builder("Command.Experience.ResetXP.Notify").chatMessage(
                                        Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                        GRAY.wrap("Your XP has been reset to " + SOFT_YELLOW.wrap(GENERIC_VALUE)
                                                        + "."));

        private static final MessageLocale MESSAGE_LEVEL_SET_FEEDBACK = LangEntry
                        .builder("Command.Experience.SetLevel.Feedback")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("You have set " + WHITE.wrap(PLAYER_DISPLAY_NAME) + "'s XP level to "
                                                        + SOFT_YELLOW.wrap(GENERIC_VALUE) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_LEVEL_SET_NOTIFY = LangEntry
                        .builder("Command.Experience.SetLevel.Notify")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("Your XP level has been set to " + SOFT_YELLOW.wrap(GENERIC_VALUE)
                                                        + "."));

        private static final MessageLocale MESSAGE_LEVEL_ADD_FEEDBACK = LangEntry
                        .builder("Command.Experience.AddLevel.Feedback")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("You have added " + SOFT_GREEN.wrap(GENERIC_VALUE) + " XP levels to "
                                                        + WHITE.wrap(PLAYER_DISPLAY_NAME) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_LEVEL_ADD_NOTIFY = LangEntry
                        .builder("Command.Experience.AddLevel.Notify")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("You have been given " + SOFT_GREEN.wrap(GENERIC_VALUE)
                                                        + " XP levels."));

        private static final MessageLocale MESSAGE_LEVEL_REMOVE_FEEDBACK = LangEntry
                        .builder("Command.Experience.RemoveLevel.Feedback")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("You have took " + SOFT_RED.wrap(GENERIC_VALUE) + " XP levels from "
                                                        + WHITE.wrap(PLAYER_DISPLAY_NAME) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_LEVEL_REMOVE_NOTIFY = LangEntry
                        .builder("Command.Experience.RemoveLevel.Notify")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("You have been taken " + SOFT_RED.wrap(GENERIC_VALUE)
                                                        + " XP levels."));

        private static final MessageLocale MESSAGE_LEVEL_RESET_FEEDBACK = LangEntry
                        .builder("Command.Experience.ResetLevel.Feedback")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("You have resetted "
                                                        + WHITE.wrap(PLAYER_DISPLAY_NAME) + " ("
                                                        + YELLOW.wrap(GENERIC_OLD_VALUE)
                                                        + DARK_GRAY.wrap(" → ") + GREEN.wrap(GENERIC_NEW_VALUE) + ")"));

        private static final MessageLocale MESSAGE_LEVEL_RESET_NOTIFY = LangEntry
                        .builder("Command.Experience.ResetLevel.Notify")
                        .chatMessage(
                                        Sound.ENTITY_PLAYER_LEVELUP,
                                        GRAY.wrap("Your XP level has been reset to " + SOFT_YELLOW.wrap(GENERIC_VALUE)
                                                        + "."));

        public ExperienceCommandsProvider(final EssentialModule module) {
                super(module, "experience");
        }

        @Override
        public void setup() {
                this.register("levels_add", command -> this.buildLevelCommand(command,
                                DESCRIPTION_LEVEL_ADD, ModifyMode.ADD))
                                .under("experience");
                this.register("levels_set", command -> this.buildLevelCommand(command,
                                DESCRIPTION_LEVEL_SET, ModifyMode.SET))
                                .under("experience");
                this.register("levels_remove", command -> this.buildLevelCommand(command,
                                DESCRIPTION_LEVEL_REMOVE, ModifyMode.REMOVE))
                                .under("experience");
                this.register("levels_reset", command -> command.withFullDescription(DESCRIPTION_LEVEL_RESET.text())
                                .withPermission(PERMISSION_LEVEL)
                                .executes((sender, arguments) -> {
                                        return this.changeLevels(sender, arguments, ModifyMode.RESET);
                                }))
                                .under("experience");

                this.register("xp_add", command -> this.buildXPCommand(command,
                                DESCRIPTION_XP_ADD, ModifyMode.ADD))
                                .under("experience");
                this.register("xp_set", command -> this.buildXPCommand(command,
                                DESCRIPTION_XP_SET, ModifyMode.SET))
                                .under("experience");
                this.register("xp_remove", command -> this.buildXPCommand(command,
                                DESCRIPTION_XP_REMOVE, ModifyMode.REMOVE))
                                .under("experience");
                this.register("xp_reset", command -> command.withFullDescription(DESCRIPTION_XP_RESET.text())
                                .withPermission(PERMISSION_XP)
                                .executes((sender, arguments) -> {
                                        return this.changeXP(sender, arguments, ModifyMode.RESET);
                                }))
                                .under("experience");

                this.registerRoot("experience", command -> command
                                .withFullDescription(DESCRIPTION_ROOT.text())
                                .withPermission(PERMISSION_ROOT)).aliases("xp");
        }

        private void buildLevelCommand(final dev.jorel.commandapi.CommandAPICommand command,
                        final TextLocale description, final ModifyMode mode) {
                command
                                .withFullDescription(description.text())
                                .withPermission(PERMISSION_LEVEL)
                                .withArguments(new IntegerArgument(CommandArgumentConstants.AMOUNT))
                                .withOptionalArguments(CommandArgumentConstants.targetArgument())
                                .executes((sender, arguments) -> {
                                        return this.changeLevels(sender, arguments, mode);
                                });
        }

        private void buildXPCommand(final dev.jorel.commandapi.CommandAPICommand command,
                        final TextLocale description, final ModifyMode mode) {
                command
                                .withFullDescription(description.text())
                                .withPermission(PERMISSION_XP)
                                .withArguments(new IntegerArgument(CommandArgumentConstants.AMOUNT))
                                .withOptionalArguments(CommandArgumentConstants.targetArgument())
                                .executes((sender, arguments) -> {
                                        return this.changeXP(sender, arguments, mode);
                                });
        }

        private int changeXP(final CommandSender sender, final CommandArguments arguments, final ModifyMode mode) {
                final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
                if (target == null) {
                        this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
                        return 0;
                }

                final int amount;
                if (mode == ModifyMode.RESET) {
                        amount = 0;
                } else {
                        if (!(arguments.get(CommandArgumentConstants.AMOUNT) instanceof final Integer amountValue)) {
                                return 0;
                        }
                        amount = amountValue;
                }

                return target.runAs(this.module, sender, PERMISSION_XP_OTHERS, (user, player) -> {
                        final int oldXP = player.getTotalExperience();
                        final MessageLocale feedbackMessage = switch (mode) {
                                case SET -> MESSAGE_XP_SET_FEEDBACK;
                                case REMOVE -> MESSAGE_XP_REMOVE_FEEDBACK;
                                case ADD -> MESSAGE_XP_ADD_FEEDBACK;
                                case RESET -> MESSAGE_XP_RESET_FEEDBACK;
                        };

                        final MessageLocale notifyMessage = switch (mode) {
                                case ADD -> MESSAGE_XP_ADD_NOTIFY;
                                case SET -> MESSAGE_XP_SET_NOTIFY;
                                case REMOVE -> MESSAGE_XP_REMOVE_NOTIFY;
                                case RESET -> MESSAGE_XP_RESET_NOTIFY;
                        };

                        switch (mode) {
                                case SET -> {
                                        module.setExp(player, 0F);
                                        module.giveExp(player, amount);
                                }
                                case ADD -> {
                                        module.giveExp(player, amount);
                                }
                                case REMOVE -> {
                                        module.giveExp(player, -amount);
                                }
                                case RESET -> {
                                        module.resetExp(player);
                                }
                                default -> {
                                        return;
                                }
                        }

                        if (player != sender) {
                                this.module.sendPrefixed(feedbackMessage, sender,
                                                builder -> builder
                                                                .with(GENERIC_VALUE, () -> NumberUtil.format(amount))
                                                                .with(GENERIC_OLD_VALUE, () -> NumberUtil.format(oldXP))
                                                                .with(GENERIC_NEW_VALUE,
                                                                                () -> NumberUtil.format(player
                                                                                                .getTotalExperience()))
                                                                .with(CommonPlaceholders.PLAYER.resolver(player)));
                        }

                        if (!target.silent()) {
                                this.module.sendPrefixed(notifyMessage, player, builder -> builder
                                                .with(GENERIC_VALUE, () -> NumberUtil.format(amount))
                                                .with(GENERIC_OLD_VALUE, () -> NumberUtil.format(oldXP))
                                                .with(GENERIC_NEW_VALUE,
                                                                () -> NumberUtil.format(player.getTotalExperience())));
                        }
                });
        }

        private int changeLevels(final CommandSender sender, final CommandArguments arguments, final ModifyMode mode) {
                final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
                if (target == null) {
                        this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
                        return 0;
                }

                final int amount;
                if (mode == ModifyMode.RESET) {
                        amount = 0;
                } else {
                        final Object amountArg = arguments.get(CommandArgumentConstants.AMOUNT);
                        if (!(amountArg instanceof final Integer amountValue)) {
                                return 0;
                        }
                        amount = amountValue;
                }

                return target.runAs(this.module, sender, PERMISSION_LEVEL_OTHERS, (user, player) -> {
                        final int oldLevel = player.getLevel();
                        final MessageLocale feedbackMessage = switch (mode) {
                                case SET -> MESSAGE_LEVEL_SET_FEEDBACK;
                                case REMOVE -> MESSAGE_LEVEL_REMOVE_FEEDBACK;
                                case ADD -> MESSAGE_LEVEL_ADD_FEEDBACK;
                                case RESET -> MESSAGE_LEVEL_RESET_FEEDBACK;
                        };

                        final MessageLocale notifyMessage = switch (mode) {
                                case ADD -> MESSAGE_LEVEL_ADD_NOTIFY;
                                case SET -> MESSAGE_LEVEL_SET_NOTIFY;
                                case REMOVE -> MESSAGE_LEVEL_REMOVE_NOTIFY;
                                case RESET -> MESSAGE_LEVEL_RESET_NOTIFY;
                        };

                        switch (mode) {
                                case SET -> {
                                        module.setLevel(player, amount);
                                }
                                case ADD -> {
                                        module.addLevel(player, amount);
                                }
                                case REMOVE -> {
                                        module.removeLevel(player, amount);
                                }
                                case RESET -> {
                                        module.resetLevel(player);
                                }
                                default -> {
                                        return;
                                }
                        }

                        if (player != sender) {
                                this.module.sendPrefixed(feedbackMessage, sender,
                                                builder -> builder
                                                                .with(GENERIC_VALUE, () -> NumberUtil.format(amount))
                                                                .with(GENERIC_OLD_VALUE,
                                                                                () -> NumberUtil.format(oldLevel))
                                                                .with(GENERIC_NEW_VALUE,
                                                                                () -> NumberUtil.format(
                                                                                                player.getLevel()))
                                                                .with(CommonPlaceholders.PLAYER.resolver(player)));
                        }

                        if (!target.silent()) {
                                this.module.sendPrefixed(notifyMessage, player, builder -> builder
                                                .with(GENERIC_VALUE, () -> NumberUtil.format(amount))
                                                .with(GENERIC_OLD_VALUE, () -> NumberUtil.format(oldLevel))
                                                .with(GENERIC_NEW_VALUE, () -> NumberUtil.format(player.getLevel())));
                        }
                });
        }
}
