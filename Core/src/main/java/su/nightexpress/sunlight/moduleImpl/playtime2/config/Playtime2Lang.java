package su.nightexpress.sunlight.moduleImpl.playtime2.config;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.DARK_GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GOLD;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GREEN;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.SOFT_RED;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.WHITE;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_CURRENT;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_MAX;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_MIN;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_NAME;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_PAGE;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_TIME;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_TOTAL;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_VALUE;
import static su.nightexpress.sunlight.SLPlaceholders.PLAYER_NAME;

import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;

public class Playtime2Lang implements LangContainer {

        public static final TextLocale COMMAND_PLAYTIME_DESC = LangEntry.builder("Command.Playtime.Root.Desc")
                        .text("Your playtime statistics.");

        public static final TextLocale COMMAND_STATS_DESC = LangEntry.builder("Command.Playtime.Stats.Desc")
                        .text("Your playtime for a specific period.");

        public static final TextLocale COMMAND_TOP_DESC = LangEntry.builder("Command.Playtime.Top.Desc")
                        .text("Playtime leaderboard.");

        public static final TextLocale COMMAND_GOAL_DESC = LangEntry.builder("Command.Playtime.Goal.Desc")
                        .text("View or change your playtime goals.");

        public static final TextLocale COMMAND_MENU_DESC = LangEntry.builder("Command.Playtime.Menu.Desc")
                        .text("Open the playtime menu.");

        public static final TextLocale COMMAND_CALENDAR_DESC = LangEntry.builder("Command.Playtime.Calendar.Desc")
                        .text("Open the playtime calendar.");

        public static final TextLocale COMMAND_ADMIN_ADD_DESC = LangEntry.builder("Command.Playtime.AdminAdd.Desc")
                        .text("Add playtime to a player's current day.");

        public static final TextLocale COMMAND_ADMIN_SET_DESC = LangEntry.builder("Command.Playtime.AdminSet.Desc")
                        .text("Set a player's playtime for the current day.");

        public static final TextLocale COMMAND_ADMIN_RESET_DESC = LangEntry.builder("Command.Playtime.AdminReset.Desc")
                        .text("Reset a player's playtime data.");

        public static final TextLocale COMMAND_ADMIN_RELOAD_DESC = LangEntry
                        .builder("Command.Playtime.AdminReload.Desc")
                        .text("Reload the playtime module.");

        public static final MessageLocale STATS_HEADER = LangEntry.builder("Playtime.Stats.Header")
                        .chatMessage(DARK_GRAY.wrap("[" + GOLD.wrap("Playtime") + "] ") + WHITE.wrap(PLAYER_NAME));

        public static final MessageLocale STATS_TOTAL = LangEntry.builder("Playtime.Stats.Total")
                        .chatMessage(GRAY.wrap("Total: ") + WHITE.wrap(GENERIC_TIME));

        public static final MessageLocale STATS_PERIOD = LangEntry.builder("Playtime.Stats.Period")
                        .chatMessage(GRAY.wrap("▸ ") + GENERIC_NAME + GRAY.wrap(": ") + WHITE.wrap(GENERIC_TIME));

        public static final MessageLocale STATS_STREAK_DAILY = LangEntry.builder("Playtime.Stats.StreakDaily")
                        .chatMessage(GRAY.wrap("Streak (daily): ") + WHITE.wrap(GENERIC_VALUE)
                                        + DARK_GRAY.wrap(" days"));

        public static final MessageLocale STATS_STREAK_WEEKLY = LangEntry.builder("Playtime.Stats.StreakWeekly")
                        .chatMessage(GRAY.wrap("Streak (weekly): ") + WHITE.wrap(GENERIC_VALUE)
                                        + DARK_GRAY.wrap(" weeks"));

        public static final MessageLocale STATS_DAYS_PLAYED = LangEntry.builder("Playtime.Stats.DaysPlayed")
                        .chatMessage(GRAY.wrap("Days played: ") + WHITE.wrap(GENERIC_VALUE));

        public static final MessageLocale STATS_FIRST_PLAYED = LangEntry.builder("Playtime.Stats.FirstPlayed")
                        .chatMessage(GRAY.wrap("First played: ") + WHITE.wrap(GENERIC_TIME));

        public static final MessageLocale STATS_LAST_PLAYED = LangEntry.builder("Playtime.Stats.LastPlayed")
                        .chatMessage(GRAY.wrap("Last played: ") + WHITE.wrap(GENERIC_TIME));

        public static final MessageLocale GOAL_HEADER = LangEntry.builder("Playtime.Goal.Header")
                        .chatMessage(DARK_GRAY.wrap("[" + GOLD.wrap("Goals") + "]"));

        public static final MessageLocale GOAL_LINE = LangEntry.builder("Playtime.Goal.Line")
                        .chatMessage(GRAY.wrap("▸ ") + GENERIC_NAME + GRAY.wrap(": ") + WHITE.wrap(GENERIC_VALUE)
                                        + GRAY.wrap("% (")
                                        + WHITE.wrap(GENERIC_CURRENT) + GRAY.wrap(" / ") + WHITE.wrap(GENERIC_MAX)
                                        + GRAY.wrap(")"));

        public static final MessageLocale GOAL_LINE_INACTIVE = LangEntry.builder("Playtime.Goal.LineInactive")
                        .chatMessage(GRAY.wrap("▸ ") + GENERIC_NAME + GRAY.wrap(": ") + SOFT_RED.wrap("No goal set"));

        public static final MessageLocale GOAL_SET = LangEntry.builder("Playtime.Goal.Set")
                        .chatMessage(GREEN.wrap(GENERIC_NAME) + GREEN.wrap(" goal is now ") + WHITE.wrap(GENERIC_TIME)
                                        + GREEN.wrap("."));

        public static final MessageLocale GOAL_RESET = LangEntry.builder("Playtime.Goal.Reset")
                        .chatMessage(GREEN.wrap(GENERIC_NAME) + GREEN.wrap(" goal is disabled."));

        public static final MessageLocale GOAL_ERROR_RANGE = LangEntry.builder("Playtime.Goal.Error.Range")
                        .chatMessage(SOFT_RED.wrap("Allowed minutes range: ") + WHITE.wrap(GENERIC_MIN)
                                        + SOFT_RED.wrap(" – ")
                                        + WHITE.wrap(GENERIC_MAX) + SOFT_RED.wrap(". Use 0 to disable the goal."));

        public static final MessageLocale REWARD_GOAL = LangEntry.builder("Playtime.Goal.Reward")
                        .chatMessage(GREEN.wrap("You reached the ") + GENERIC_NAME
                                        + GREEN.wrap(" goal and received a reward!"));

        public static final MessageLocale REMINDER_GOAL = LangEntry.builder("Playtime.Goal.Reminder")
                        .chatMessage(GOLD.wrap("You need only ") + WHITE.wrap(GENERIC_TIME)
                                        + GOLD.wrap(" more to reach the ")
                                        + GENERIC_NAME + GOLD.wrap(" goal!"));

        public static final MessageLocale TOP_TITLE = LangEntry.builder("Playtime.Top.Title")
                        .chatMessage(DARK_GRAY.wrap("[" + GOLD.wrap("Playtime Top") + "] ") + GENERIC_NAME);

        public static final MessageLocale TOP_ENTRY = LangEntry.builder("Playtime.Top.Entry")
                        .chatMessage(GRAY.wrap("#") + WHITE.wrap(GENERIC_PAGE) + GRAY.wrap(" ")
                                        + WHITE.wrap(PLAYER_NAME)
                                        + GRAY.wrap(" – ") + WHITE.wrap(GENERIC_TOTAL));

        public static final MessageLocale ERROR_GOAL_NEGATIVE = LangEntry.builder("Playtime.Error.GoalNegative")
                        .chatMessage(SOFT_RED.wrap("Minutes amount must not be negative."));

        public static final MessageLocale ADMIN_ADDED = LangEntry.builder("Playtime.Admin.Added")
                        .chatMessage(GREEN.wrap("Added ") + WHITE.wrap(GENERIC_TIME) + GREEN.wrap(" to ")
                                        + WHITE.wrap(PLAYER_NAME)
                                        + GREEN.wrap("'s current day."));

        public static final MessageLocale ADMIN_SET = LangEntry.builder("Playtime.Admin.Set")
                        .chatMessage(GREEN.wrap("Set ") + WHITE.wrap(PLAYER_NAME)
                                        + GREEN.wrap("'s playtime for today to ")
                                        + WHITE.wrap(GENERIC_TIME) + GREEN.wrap("."));

        public static final MessageLocale ADMIN_RESET = LangEntry.builder("Playtime.Admin.Reset")
                        .chatMessage(GREEN.wrap("Reset ") + WHITE.wrap(GENERIC_NAME) + GREEN.wrap(" playtime data of ")
                                        + WHITE.wrap(PLAYER_NAME) + GREEN.wrap("."));

        public static final MessageLocale ADMIN_RELOADED = LangEntry.builder("Playtime.Admin.Reloaded")
                        .chatMessage(GREEN.wrap("Playtime configuration reloaded."));

        public static final MessageLocale MILESTONE_REWARD = LangEntry.builder("Playtime.Milestone.Reward")
                        .chatMessage(GREEN.wrap("Milestone ") + WHITE.wrap(GENERIC_NAME)
                                        + GREEN.wrap(" reached — reward claimed!"));

        public static final TextLocale MENU_TITLE_STATS = LangEntry.builder("Playtime.Menu.StatsTitle")
                        .text("Playtime");

        public static final TextLocale MENU_TITLE_TOP = LangEntry.builder("Playtime.Menu.TopTitle")
                        .text("Playtime Top");

        public static final TextLocale MENU_TITLE_CALENDAR = LangEntry.builder("Playtime.Menu.CalendarTitle")
                        .text("Playtime Calendar");

        public static final TextLocale MENU_BUTTON_BACK = LangEntry.builder("Playtime.Menu.ButtonBack")
                        .text("Back");

        public static final TextLocale MENU_BUTTON_CLOSE = LangEntry.builder("Playtime.Menu.ButtonClose")
                        .text("Close");

        public static final TextLocale MENU_BUTTON_STATS = LangEntry.builder("Playtime.Menu.ButtonStats")
                        .text("My Stats");

        public static final TextLocale MENU_BUTTON_TOP = LangEntry.builder("Playtime.Menu.ButtonTop")
                        .text("Leaderboard");

        public static final TextLocale MENU_BUTTON_CALENDAR = LangEntry.builder("Playtime.Menu.ButtonCalendar")
                        .text("Calendar");

        public static final TextLocale MENU_BUTTON_PREVIOUS = LangEntry.builder("Playtime.Menu.ButtonPrevious")
                        .text("Previous Month");

        public static final TextLocale MENU_BUTTON_NEXT = LangEntry.builder("Playtime.Menu.ButtonNext")
                        .text("Next Month");
}
