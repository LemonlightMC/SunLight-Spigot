package su.nightexpress.sunlight.moduleImpl.afk.config;

import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.BooleanLocale;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.sunlight.SLPlaceholders.*;

public class AfkLang implements LangContainer {

    public static final TextLocale COMMAND_AFK_OFF_DESC    = LangEntry.builder("Afk.Command.Afk.Off.Desc").text("Exit AFK mode.");
    public static final TextLocale COMMAND_AFK_ON_DESC     = LangEntry.builder("Afk.Command.Afk.On.Desc").text("Enter AFK mode.");
    public static final TextLocale COMMAND_AFK_TOGGLE_DESC = LangEntry.builder("Afk.Command.Afk.Toggle.Desc").text("Toggle AFK mode.");
    public static final TextLocale COMMAND_AFK_LIST_DESC   = LangEntry.builder("Afk.Command.Afk.List.Desc").text("List all AFK players.");

    public static final BooleanLocale PLACEHOLDER_MODE = LangEntry.builder("Afk.Placeholder.Mode").bool(
        DARK_GRAY.wrap(" [AFK]"),
        ""
    );

    public static final MessageLocale AFK_TOGGLE_FEEDBACK = LangEntry.builder("Afk.Command.Afk.Done.Others").chatMessage(
        GRAY.wrap("You have set " + ORANGE.wrap(PLAYER_DISPLAY_NAME) + "'s AFK mode on " + WHITE.wrap(GENERIC_STATE) + "."));

    public static final MessageLocale AFK_ENTER_BROADCAST = LangEntry.builder("Afk.Mode.Enter").chatMessage(
        GRAY.wrap(WHITE.wrap(PLAYER_DISPLAY_NAME) + " is AFK now."));

    public static final MessageLocale AFK_EXIT_BROADCAST = LangEntry.builder("Afk.Mode.Exit").chatMessage(
        GRAY.wrap(WHITE.wrap(PLAYER_DISPLAY_NAME) + " is back. (was AFK for " + ORANGE.wrap(GENERIC_TIME) + ")"));

    public static final MessageLocale AFK_LIST_EMPTY = LangEntry.builder("Afk.Command.Afk.List.Empty").chatMessage(
        GRAY.wrap("There are no AFK players."));

    public static final MessageLocale AFK_LIST = LangEntry.builder("Afk.Command.Afk.List").chatMessage(
        DARK_GRAY.wrap("[" + ORANGE.wrap("AFK") + "]") + " " + GRAY.wrap("Online AFK players: ") + WHITE.wrap(GENERIC_LIST));

    public static final MessageLocale KICK_WARNING = LangEntry.builder("Afk.Kick.Warning").chatMessage(
        SOFT_RED.wrap("You're AFK for too long! You will be kicked in " + WHITE.wrap(GENERIC_TIME) + "."));

    public static final MessageLocale KICK_REJOIN = LangEntry.builder("Afk.Kick.Rejoin").chatMessage(
        SOFT_RED.wrap("You have just been kicked for idling, please wait before joining back."));

    public static final MessageLocale ERROR_ACTION_BLOCKED = LangEntry.builder("Afk.Error.ActionBlocked").chatMessage(
        SOFT_RED.wrap("You cannot do this while being AFK!"));

    public static final TextLocale COMMAND_AFK_STATUS_DESC = LangEntry.builder("Afk.Command.Afk.Status.Desc").text("Show AFK status.");
    public static final TextLocale COMMAND_AFK_INFO_DESC = LangEntry.builder("Afk.Command.Afk.Info.Desc").text("Show detailed AFK information.");
    public static final TextLocale COMMAND_AFK_KICK_DESC = LangEntry.builder("Afk.Command.Afk.Kick.Desc").text("Kick a player for being idle/AFK.");
    public static final TextLocale COMMAND_AFK_GUI_DESC = LangEntry.builder("Afk.Command.Afk.Gui.Desc").text("Open the AFK admin GUI.");

    public static final MessageLocale AFK_STATUS = LangEntry.builder("Afk.Command.Afk.Status").chatMessage(
        GRAY.wrap("AFK status for " + ORANGE.wrap(PLAYER_DISPLAY_NAME) + ": " + WHITE.wrap(GENERIC_STATE) + GRAY.wrap(", idle ") + WHITE.wrap("%idle%") + GRAY.wrap(", AFK ") + WHITE.wrap("%afk_time%") + GRAY.wrap(", total ") + WHITE.wrap("%total%") + GRAY.wrap(", sessions ") + WHITE.wrap("%count%") + GRAY.wrap(".")));

    public static final MessageLocale AFK_INFO = LangEntry.builder("Afk.Command.Afk.Info").chatMessage(
        GRAY.wrap("AFK info for " + ORANGE.wrap(PLAYER_DISPLAY_NAME) + ": state " + WHITE.wrap(GENERIC_STATE)
            + GRAY.wrap(", idle ") + WHITE.wrap("%idle%")
            + GRAY.wrap(", AFK duration ") + WHITE.wrap("%afk_time%")
            + GRAY.wrap(", until AFK ") + WHITE.wrap("%until_afk%")
            + GRAY.wrap(", until kick ") + WHITE.wrap("%until_kick%")
            + GRAY.wrap(", total AFK ") + WHITE.wrap("%total%")
            + GRAY.wrap(", sessions ") + WHITE.wrap("%count%") + GRAY.wrap(".")));

    public static final MessageLocale AFK_KICK_FEEDBACK = LangEntry.builder("Afk.Command.Afk.Kick.Feedback").chatMessage(
        GRAY.wrap("You have kicked " + ORANGE.wrap(PLAYER_DISPLAY_NAME) + GRAY.wrap(" for idling.")));

    public static final MessageLocale AFK_GUI_UNAVAILABLE = LangEntry.builder("Afk.Command.Afk.Gui.Unavailable").chatMessage(
        SOFT_RED.wrap("The AFK admin GUI is not available."));
}