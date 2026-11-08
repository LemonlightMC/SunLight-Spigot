package su.nightexpress.sunlight.moduleImpl.timer.config;

import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.sunlight.SLPlaceholders.*;

public class TimerLang implements LangContainer {

    public static final TextLocale COMMAND_TIMER_DESC = LangEntry.builder("Command.Timer.Desc").text("Manage timers.");
    public static final TextLocale COMMAND_PTIMER_DESC = LangEntry.builder("Command.PTimer.Desc").text("Manage your personal timers.");

    public static final MessageLocale COMMAND_LIST_EMPTY = LangEntry.builder("Command.Timer.List.Empty")
            .chatMessage(GRAY.wrap("There are no timers."));
    public static final MessageLocale COMMAND_LIST_TITLE = LangEntry.builder("Command.Timer.List.Title")
            .chatMessage(GRAY.wrap("Timers " + DARK_GRAY.wrap("(") + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + DARK_GRAY.wrap(")") + ":"));
    public static final MessageLocale COMMAND_LIST_ENTRY = LangEntry.builder("Command.Timer.List.Entry")
            .chatMessage(SOFT_YELLOW.wrap("● ") + WHITE.wrap(GENERIC_NAME) + GRAY.wrap(" | " + GENERIC_TYPE + " | " + GENERIC_STATE + " | " + GENERIC_TIME));

    public static final MessageLocale COMMAND_INFO = LangEntry.builder("Command.Timer.Info")
            .chatMessage(GRAY.wrap("Timer ") + SOFT_YELLOW.wrap(GENERIC_NAME) + GRAY.wrap(" [" + GENERIC_VALUE + "]") + "\n"
                    + GRAY.wrap("Scope: ") + WHITE.wrap(GENERIC_MESSAGE) + GRAY.wrap(", Kind: ") + WHITE.wrap(GENERIC_TYPE) + GRAY.wrap(", State: ") + WHITE.wrap(GENERIC_STATE) + "\n"
                    + GRAY.wrap("Time: ") + WHITE.wrap(GENERIC_TIME) + GRAY.wrap(" / ") + WHITE.wrap(GENERIC_TARGET) + GRAY.wrap(" (elapsed ") + WHITE.wrap(GENERIC_AMOUNT) + GRAY.wrap(")"));

    public static final MessageLocale COMMAND_NOT_FOUND = LangEntry.builder("Command.Timer.Error.NotFound")
            .chatMessage(SOFT_RED.wrap("Timer not found: " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_ALREADY_EXISTS = LangEntry.builder("Command.Timer.Error.Exists")
            .chatMessage(SOFT_RED.wrap("Timer already exists: " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_INVALID_ID = LangEntry.builder("Command.Timer.Error.InvalidId")
            .chatMessage(SOFT_RED.wrap("Invalid timer id. Use letters, numbers, _ or -."));
    public static final MessageLocale COMMAND_INVALID_NUMBER = LangEntry.builder("Command.Timer.Error.InvalidNumber")
            .chatMessage(SOFT_RED.wrap("Invalid number: " + SOFT_YELLOW.wrap(GENERIC_VALUE) + "."));
    public static final MessageLocale COMMAND_INVALID_TIME = LangEntry.builder("Command.Timer.Error.InvalidTime")
            .chatMessage(SOFT_RED.wrap("Invalid time: " + SOFT_YELLOW.wrap(GENERIC_VALUE) + ". Use 1h30m, 90s, 10:00, or seconds."));
    public static final MessageLocale COMMAND_INVALID_KIND = LangEntry.builder("Command.Timer.Error.InvalidKind")
            .chatMessage(SOFT_RED.wrap("Invalid kind. Use " + SOFT_YELLOW.wrap("stopwatch") + " or " + SOFT_YELLOW.wrap("countdown") + "."));
    public static final MessageLocale COMMAND_INVALID_SCHEDULE = LangEntry.builder("Command.Timer.Error.InvalidSchedule")
            .chatMessage(SOFT_RED.wrap("Invalid schedule: " + SOFT_YELLOW.wrap(GENERIC_VALUE) + ". Example: 2026-12-31 20:00:00 UTC, or +30m."));
    public static final MessageLocale COMMAND_INVALID_ACTION = LangEntry.builder("Command.Timer.Error.InvalidAction")
            .chatMessage(SOFT_RED.wrap("Invalid action config: " + SOFT_YELLOW.wrap(GENERIC_VALUE) + "."));
    public static final MessageLocale COMMAND_CANNOT_START = LangEntry.builder("Command.Timer.Error.CannotStart")
            .chatMessage(SOFT_RED.wrap("Cannot start timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + " in state " + SOFT_YELLOW.wrap(GENERIC_STATE) + "."));
    public static final MessageLocale COMMAND_PERSONAL_LIMIT = LangEntry.builder("Command.Timer.Error.PersonalLimit")
            .chatMessage(SOFT_RED.wrap("You reached the personal timer limit (" + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + ")."));
    public static final MessageLocale COMMAND_CONFIRM_DELETE = LangEntry.builder("Command.Timer.ConfirmDelete")
            .chatMessage(GRAY.wrap("Confirm delete of " + SOFT_YELLOW.wrap(GENERIC_NAME) + GRAY.wrap(" with " + SOFT_YELLOW.wrap("/" + GENERIC_VALUE) + GRAY.wrap(" confirm")) + "."));
    public static final MessageLocale COMMAND_TAB_SCOREBOARD_UNAVAILABLE = LangEntry.builder("Command.Timer.Error.TabScoreboardUnavailable")
            .chatMessage(SOFT_RED.wrap("SCOREBOARD channel requires TAB plugin."));

    public static final MessageLocale COMMAND_CREATED = LangEntry.builder("Command.Timer.Created")
            .chatMessage(GRAY.wrap("Created " + SOFT_YELLOW.wrap(GENERIC_TYPE) + " timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + GRAY.wrap(" [" + GENERIC_VALUE + "].")));
    public static final MessageLocale COMMAND_STARTED = LangEntry.builder("Command.Timer.Started")
            .chatMessage(GRAY.wrap("Started timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_PAUSED = LangEntry.builder("Command.Timer.Paused")
            .chatMessage(GRAY.wrap("Paused timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_RESUMED = LangEntry.builder("Command.Timer.Resumed")
            .chatMessage(GRAY.wrap("Resumed timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_RESET = LangEntry.builder("Command.Timer.Reset")
            .chatMessage(GRAY.wrap("Reset timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_STOPPED = LangEntry.builder("Command.Timer.Stopped")
            .chatMessage(GRAY.wrap("Stopped timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_DELETED = LangEntry.builder("Command.Timer.Deleted")
            .chatMessage(GRAY.wrap("Deleted timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_RELOADED = LangEntry.builder("Command.Timer.Reloaded")
            .chatMessage(GRAY.wrap("Reloaded timer module (" + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + " global timers)."));
    public static final MessageLocale COMMAND_TIME_SET = LangEntry.builder("Command.Timer.TimeSet")
            .chatMessage(GRAY.wrap("Set timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + " time to " + SOFT_YELLOW.wrap(GENERIC_TIME) + "."));
    public static final MessageLocale COMMAND_TIME_ADDED = LangEntry.builder("Command.Timer.TimeAdded")
            .chatMessage(GRAY.wrap("Added " + SOFT_YELLOW.wrap(GENERIC_TIME) + " to timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_TIME_REMOVED = LangEntry.builder("Command.Timer.TimeRemoved")
            .chatMessage(GRAY.wrap("Removed " + SOFT_YELLOW.wrap(GENERIC_TIME) + " from timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_COPIED = LangEntry.builder("Command.Timer.Copied")
            .chatMessage(GRAY.wrap("Copied timer " + SOFT_YELLOW.wrap(GENERIC_VALUE) + " to " + SOFT_YELLOW.wrap(GENERIC_NAME) + "."));
    public static final MessageLocale COMMAND_ALL_AFFECTED = LangEntry.builder("Command.Timer.AllAffected")
            .chatMessage(GRAY.wrap("Applied " + SOFT_YELLOW.wrap(GENERIC_TYPE) + " to " + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + " global timers."));

    public static final MessageLocale TIMER_COMPLETED = LangEntry.builder("Timer.Completed")
            .chatMessage(GRAY.wrap("Timer " + SOFT_YELLOW.wrap(GENERIC_NAME) + " completed."));
    public static final MessageLocale TIMER_STOPWATCH_ENDED = LangEntry.builder("Timer.StopwatchEnded")
            .chatMessage(GRAY.wrap("Stopwatch " + SOFT_YELLOW.wrap(GENERIC_NAME) + " ended at " + SOFT_YELLOW.wrap(GENERIC_TIME) + "."));

    public static final TextLocale MENU_LIST_TITLE = LangEntry.builder("Timer.Menu.List.Title").text("Timers");
    public static final TextLocale MENU_EDITOR_TITLE = LangEntry.builder("Timer.Menu.Editor.Title").text("Timer Editor: " + GENERIC_NAME);
}
