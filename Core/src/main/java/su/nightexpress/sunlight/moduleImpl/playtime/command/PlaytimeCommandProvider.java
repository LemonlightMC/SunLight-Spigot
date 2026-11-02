package su.nightexpress.sunlight.moduleImpl.playtime.command;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeModule;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeTopHandler.TopEntry;
import su.nightexpress.sunlight.moduleImpl.playtime.config.PlaytimeLang;
import su.nightexpress.sunlight.moduleImpl.playtime.config.PlaytimePerms;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

public class PlaytimeCommandProvider extends CommandProvider<PlaytimeModule> {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public PlaytimeCommandProvider(final PlaytimeModule module) {
        super(module, "playtime");
    }

    @Override
    public void setup() {
        this.register("stats", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_STATS_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_STATS)
                .withArguments(CommandArgumentConstants.periodArgument())
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(PlaytimePerms.COMMAND_STATS_OTHERS))
                .executes(this::showStats))
                .under("playtime");

        this.register("top", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_TOP_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_TOP)
                .withOptionalArguments(
                        CommandArgumentConstants.periodArgument(),
                        new IntegerArgument("page"))
                .executes(this::showTop))
                .aliases("playtop")
                .under("playtime");

        this.register("goal", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_GOAL_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_GOAL)
                .withOptionalArguments(
                        CommandArgumentConstants.periodArgument(),
                        new IntegerArgument("minutes"))
                .executes(this::goal))
                .under("playtime");

        this.register("menu", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_MENU_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_MENU)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(PlaytimePerms.COMMAND_MENU_OTHERS))
                .executes(this::showMenu))
                .aliases("gui")
                .under("playtime");

        this.register("calendar", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_CALENDAR_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_CALENDAR)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument()
                                .withPermission(PlaytimePerms.COMMAND_CALENDAR_OTHERS))
                .executes(this::showCalendar))
                .under("playtime");

        this.register("add", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_ADMIN_ADD_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_ADMIN_ADD)
                .withArguments(new IntegerArgument("minutes", 1))
                .withOptionalArguments(CommandArgumentConstants.targetArgument())
                .executes(this::addPlaytime))
                .under("playtime");

        this.register("set", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_ADMIN_SET_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_ADMIN_SET)
                .withArguments(new IntegerArgument("minutes", 0))
                .withOptionalArguments(CommandArgumentConstants.targetArgument())
                .executes(this::setPlaytime))
                .under("playtime");

        this.register("reset", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_ADMIN_RESET_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_ADMIN_RESET)
                .withArguments(this.resetScopeArgument())
                .withOptionalArguments(CommandArgumentConstants.targetArgument())
                .executes(this::resetPlaytime))
                .under("playtime");

        this.register("reload", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_ADMIN_RELOAD_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_ADMIN_RELOAD)
                .executes((sender, arguments) -> {
                    this.module.reloadModule();
                    this.module.sendPrefixed(PlaytimeLang.ADMIN_RELOADED, sender);
                    return 1;
                }))
                .under("playtime");

        this.registerRoot("playtime", command -> command
                .withFullDescription(PlaytimeLang.COMMAND_PLAYTIME_DESC.text())
                .withPermission(PlaytimePerms.COMMAND_PLAYTIME)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(PlaytimePerms.COMMAND_STATS_OTHERS))
                .executes(this::showOwnStats));
    }

    private Argument<String> resetScopeArgument() {
        return CommandArgumentConstants.string(
                "scope",
                info -> List.of("all", "alltime", "year", "month", "week", "day", "session", "milestones"));
    }

    private int showOwnStats(final CommandSender sender, final CommandArguments arguments) {
        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        // Bare /playtime opens the menu for players checking themselves.
        if (!target.hasTarget() && sender instanceof final Player player
                && sender.hasPermission(PlaytimePerms.COMMAND_MENU)) {
            final SunUser self = this.module.userManager().getOrFetch(player);
            return self != null && this.module.openStatsMenu(player, self) ? 1 : 0;
        }
        return target.runAs(this.module, sender, PlaytimePerms.COMMAND_STATS_OTHERS, (user, targetPlayer) -> {
            this.module.sendPrefixed(PlaytimeLang.STATS_HEADER, sender, b -> b
                    .with(SLPlaceholders.PLAYER_NAME, user::getName));

            this.module.sendPrefixed(PlaytimeLang.STATS_TOTAL, sender, b -> b
                    .with(SLPlaceholders.GENERIC_TIME,
                            () -> TimeUtil
                                    .formatMillis(this.module.getEffectivePlaytime(user, PlaytimePeriod.ALLTIME))));

            this.sendPeriodLine(sender, "Year", this.module.getEffectivePlaytime(user, PlaytimePeriod.YEAR));
            this.sendPeriodLine(sender, "Month", this.module.getEffectivePlaytime(user, PlaytimePeriod.MONTH));
            this.sendPeriodLine(sender, "Week", this.module.getEffectivePlaytime(user, PlaytimePeriod.WEEK));
            this.sendPeriodLine(sender, "Day", this.module.getEffectivePlaytime(user, PlaytimePeriod.DAY));

            this.module.sendPrefixed(PlaytimeLang.STATS_STREAK_DAILY, sender, b -> b
                    .with(SLPlaceholders.GENERIC_VALUE, () -> String.valueOf(this.module.getDailyStreak(user))));
            this.module.sendPrefixed(PlaytimeLang.STATS_STREAK_WEEKLY, sender, b -> b
                    .with(SLPlaceholders.GENERIC_VALUE, () -> String.valueOf(this.module.getWeeklyStreak(user))));
            this.module.sendPrefixed(PlaytimeLang.STATS_DAYS_PLAYED, sender, b -> b
                    .with(SLPlaceholders.GENERIC_VALUE, () -> String.valueOf(this.module.getDaysPlayed(user))));
            this.sendPlayedLine(sender, PlaytimeLang.STATS_FIRST_PLAYED, this.module.getFirstPlayedDay(user));
            this.sendPlayedLine(sender, PlaytimeLang.STATS_LAST_PLAYED, this.module.getLastPlayedDay(user));
        });
    }

    private void sendPlayedLine(final CommandSender sender,
            final su.nightexpress.nightcore.locale.entry.MessageLocale locale,
            final long epochDay) {
        if (epochDay <= 0L) {
            return;
        }
        this.module.sendPrefixed(locale, sender, b -> b
                .with(SLPlaceholders.GENERIC_TIME, () -> LocalDate.ofEpochDay(epochDay).format(DATE)));
    }

    private void sendPeriodLine(final CommandSender sender, final String name, final long ms) {
        this.module.sendPrefixed(PlaytimeLang.STATS_PERIOD, sender, b -> b
                .with(SLPlaceholders.GENERIC_NAME, () -> name)
                .with(SLPlaceholders.GENERIC_TIME, () -> TimeUtil.formatMillis(ms)));
    }

    private int showStats(final CommandSender sender, final CommandArguments arguments) {
        final PlaytimePeriod period = arguments.getOrDefaultUnchecked("period", PlaytimePeriod.ALLTIME);

        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        return target.runAs(this.module, sender, PlaytimePerms.COMMAND_STATS_OTHERS, (user, targetPlayer) -> {
            this.module.sendPrefixed(PlaytimeLang.STATS_HEADER, sender, b -> b
                    .with(SLPlaceholders.PLAYER_NAME, user::getName));

            if (period == PlaytimePeriod.ALLTIME) {
                this.module.sendPrefixed(PlaytimeLang.STATS_TOTAL, sender, b -> b
                        .with(SLPlaceholders.GENERIC_TIME,
                                () -> TimeUtil
                                        .formatMillis(this.module.getEffectivePlaytime(user, PlaytimePeriod.ALLTIME))));
                return;
            }

            this.sendPeriodLine(sender, period.displayname(), this.module.getEffectivePlaytime(user, period));
        });
    }

    private int showTop(final CommandSender sender, final CommandArguments arguments) {
        final PlaytimePeriod period = arguments.getOrDefaultUnchecked("period", PlaytimePeriod.ALLTIME);
        final Object pageObj = arguments.get("page");
        int page = 1;
        if (pageObj instanceof final Integer value) {
            page = value;
        }

        if (sender instanceof final Player player) {
            return this.module.openTopMenu(player, period, page) ? 1 : 0;
        }
        final List<TopEntry> entries = new ArrayList<>(this.module.topHandler().getTop(period));
        final int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) PlaytimeModule.TOP_PAGE_SIZE));
        final int currentPage = Math.min(Math.max(1, page), totalPages);

        this.module.sendPrefixed(PlaytimeLang.TOP_TITLE, sender, b -> b
                .with(SLPlaceholders.GENERIC_NAME, () -> period.displayname() + "  " + currentPage + "/" + totalPages));

        final int start = (currentPage - 1) * PlaytimeModule.TOP_PAGE_SIZE;
        final int end = Math.min(entries.size(), start + PlaytimeModule.TOP_PAGE_SIZE);
        for (int index = start; index < end; index++) {
            final TopEntry entry = entries.get(index);
            final String rank = String.valueOf(index + 1);
            final String formatted = TimeUtil.formatMillis(entry.value());
            this.module.sendPrefixed(PlaytimeLang.TOP_ENTRY, sender, b -> b
                    .with(SLPlaceholders.GENERIC_PAGE, () -> rank)
                    .with(SLPlaceholders.PLAYER_NAME, entry::name)
                    .with(SLPlaceholders.GENERIC_TOTAL, () -> formatted));
        }
        return 1;
    }

    private int showMenu(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }
        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        return target.runAs(this.module, sender, PlaytimePerms.COMMAND_MENU_OTHERS, (user, targetPlayer) -> {
            this.module.openStatsMenu(player, user);
        });
    }

    private int showCalendar(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }
        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        return target.runAs(this.module, sender, PlaytimePerms.COMMAND_CALENDAR_OTHERS, (user, targetPlayer) -> {
            this.module.openCalendarMenu(player, user, null);
        });
    }

    private int goal(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final PlaytimePeriod period = arguments.getUnchecked("period");
        final Object minutesObj = arguments.get("minutes");

        final SunUser user = this.module.userManager().getOrFetch(player);
        if (user == null) {
            return 0;
        }

        if (period == null) {
            this.showGoals(sender, user);
            return 1;
        }

        if (!(minutesObj instanceof final Integer minutes)) {
            this.showGoals(sender, user);
            return 1;
        }

        if (!sender.hasPermission(PlaytimePerms.COMMAND_GOAL_SET)) {
            this.module.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, sender);
            return 0;
        }

        if (minutes < 0) {
            this.module.sendPrefixed(PlaytimeLang.ERROR_GOAL_NEGATIVE, sender);
            return 0;
        }

        if (minutes == 0) {
            if (!this.isGoalPeriod(period)) {
                this.showGoals(sender, user);
                return 0;
            }
            this.module.getSettings().setGoalMinutes(period, 0L);
            this.module.sendPrefixed(PlaytimeLang.GOAL_RESET, sender, b -> b
                    .with(SLPlaceholders.GENERIC_NAME, period::displayname));
            return 1;
        }

        final long min = this.module.getSettings().getGoalMinMinutes();
        final long max = this.module.getSettings().getGoalMaxMinutes();
        if ((min > 0 && minutes < min) || (max > 0 && minutes > max)) {
            this.module.sendPrefixed(PlaytimeLang.GOAL_ERROR_RANGE, sender, b -> b
                    .with(SLPlaceholders.GENERIC_MIN, () -> String.valueOf(Math.max(0, min)))
                    .with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(Math.max(0, max))));
            return 0;
        }

        if (!this.isGoalPeriod(period) || !this.module.getSettings().setGoalMinutes(period, minutes)) {
            this.showGoals(sender, user);
            return 0;
        }

        this.module.sendPrefixed(PlaytimeLang.GOAL_SET, sender, b -> b
                .with(SLPlaceholders.GENERIC_NAME, period::displayname)
                .with(SLPlaceholders.GENERIC_TIME, () -> TimeUtil.formatMillis(minutes * 60_000L)));
        return 1;
    }

    private void showGoals(final CommandSender viewer, final SunUser user) {
        this.module.sendPrefixed(PlaytimeLang.GOAL_HEADER, viewer);
        this.sendGoalLine(viewer, "Daily", this.module.getEffectivePlaytime(user, PlaytimePeriod.DAY),
                this.module.getSettings().getGoalMs(PlaytimePeriod.DAY));
        this.sendGoalLine(viewer, "Weekly", this.module.getEffectivePlaytime(user, PlaytimePeriod.WEEK),
                this.module.getSettings().getGoalMs(PlaytimePeriod.WEEK));
        this.sendGoalLine(viewer, "Monthly", this.module.getEffectivePlaytime(user, PlaytimePeriod.MONTH),
                this.module.getSettings().getGoalMs(PlaytimePeriod.MONTH));
    }

    private void sendGoalLine(final CommandSender viewer, final String name, final long current, final long goal) {
        if (goal <= 0L) {
            this.module.sendPrefixed(PlaytimeLang.GOAL_LINE_INACTIVE, viewer, b -> b
                    .with(SLPlaceholders.GENERIC_NAME, () -> name));
            return;
        }
        final long percent = current <= 0L ? 0L : Math.min(100L, current * 100L / Math.max(1L, goal));
        this.module.sendPrefixed(PlaytimeLang.GOAL_LINE, viewer, b -> b
                .with(SLPlaceholders.GENERIC_NAME, () -> name)
                .with(SLPlaceholders.GENERIC_VALUE, () -> String.valueOf(percent))
                .with(SLPlaceholders.GENERIC_CURRENT, () -> TimeUtil.formatMillis(Math.max(0L, current)))
                .with(SLPlaceholders.GENERIC_MAX, () -> TimeUtil.formatMillis(goal)));
    }

    private boolean isGoalPeriod(final PlaytimePeriod period) {
        return period == PlaytimePeriod.DAY || period == PlaytimePeriod.WEEK || period == PlaytimePeriod.MONTH;
    }

    private int addPlaytime(final CommandSender sender, final CommandArguments arguments) {
        final Object minutesObj = arguments.get("minutes");
        if (!(minutesObj instanceof final Integer minutes)) {
            return 0;
        }
        if (minutes <= 0) {
            this.module.sendPrefixed(PlaytimeLang.ERROR_GOAL_NEGATIVE, sender);
            return 0;
        }
        final long deltaMs = (long) minutes * 60_000L;

        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        return target.runAs(this.module, sender, PlaytimePerms.COMMAND_ADMIN_ADD, (user, targetPlayer) -> {
            this.module.addPlaytime(user, deltaMs);
            this.module.sendPrefixed(PlaytimeLang.ADMIN_ADDED, sender, b -> b
                    .with(SLPlaceholders.GENERIC_TIME, () -> TimeUtil.formatMillis(deltaMs))
                    .with(SLPlaceholders.PLAYER_NAME, user::getName));
        });
    }

    private int setPlaytime(final CommandSender sender, final CommandArguments arguments) {
        final Object minutesObj = arguments.get("minutes");
        if (!(minutesObj instanceof final Integer minutes)) {
            return 0;
        }
        if (minutes < 0) {
            this.module.sendPrefixed(PlaytimeLang.ERROR_GOAL_NEGATIVE, sender);
            return 0;
        }
        final long valueMs = (long) minutes * 60_000L;

        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        return target.runAs(this.module, sender, PlaytimePerms.COMMAND_ADMIN_SET, (user, targetPlayer) -> {
            this.module.setPlaytime(user, valueMs);
            this.module.sendPrefixed(PlaytimeLang.ADMIN_SET, sender, b -> b
                    .with(SLPlaceholders.GENERIC_TIME, () -> TimeUtil.formatMillis(valueMs))
                    .with(SLPlaceholders.PLAYER_NAME, user::getName));
        });
    }

    private int resetPlaytime(final CommandSender sender, final CommandArguments arguments) {
        final Object scopeObj = arguments.get("scope");
        if (!(scopeObj instanceof final String scopeRaw) || scopeRaw.isBlank()) {
            return 0;
        }
        final String scope = Utils.lowercase(scopeRaw.trim());

        if (!List.of("all", "alltime", "total", "year", "month", "week", "day", "session", "milestones", "milestone")
                .contains(scope)) {
            return 0;
        }

        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        return target.runAs(this.module, sender, PlaytimePerms.COMMAND_ADMIN_RESET, (user, targetPlayer) -> {
            this.module.resetScope(user, scope);
            this.module.sendPrefixed(PlaytimeLang.ADMIN_RESET, sender, b -> b
                    .with(SLPlaceholders.GENERIC_NAME, () -> scope)
                    .with(SLPlaceholders.PLAYER_NAME, user::getName));
        });
    }
}
