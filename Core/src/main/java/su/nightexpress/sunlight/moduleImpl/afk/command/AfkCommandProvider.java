package su.nightexpress.sunlight.moduleImpl.afk.command;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.api.provider.mode.ToggleMode;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.afk.AfkModule;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkLang;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkPerms;
import su.nightexpress.sunlight.utils.Utils;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class AfkCommandProvider extends CommandProvider<AfkModule> {

    public AfkCommandProvider(final AfkModule module) {
        super(module, "afk");
    }

    @Override
    public void setup() {
        this.register("on", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_ON_DESC.text())
                .withPermission(AfkPerms.COMMAND)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(AfkPerms.COMMAND_OTHERS))
                .executes((sender, arguments) -> {
                    toggleAfkMode(sender, arguments, ToggleMode.ON);
                    return 1;
                }))
                .aliases("afk-on")
                .under("afk");

        this.register("off", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_OFF_DESC.text())
                .withPermission(AfkPerms.COMMAND)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(AfkPerms.COMMAND_OTHERS))
                .executes((sender, arguments) -> {
                    toggleAfkMode(sender, arguments, ToggleMode.OFF);
                    return 1;
                }))
                .aliases("afk-off")
                .under("afk");

        this.register("toggle", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_TOGGLE_DESC.text())
                .withPermission(AfkPerms.COMMAND)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(AfkPerms.COMMAND_OTHERS))
                .executes((sender, arguments) -> {
                    toggleAfkMode(sender, arguments, ToggleMode.TOGGLE);
                    return 1;
                }))
                .under("afk");

        this.register("list", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_LIST_DESC.text())
                .withPermission(AfkPerms.COMMAND_LIST)
                .executes((sender, arguments) -> {
                    showList(sender);
                    return 1;
                }))
                .under("afk");

        this.register("status", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_STATUS_DESC.text())
                .withPermission(AfkPerms.COMMAND_STATUS)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(AfkPerms.COMMAND_OTHERS))
                .executes((sender, arguments) -> {
                    showStatus(sender, arguments, false);
                    return 1;
                }))
                .under("afk");

        this.register("info", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_INFO_DESC.text())
                .withPermission(AfkPerms.COMMAND_INFO)
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission(AfkPerms.COMMAND_OTHERS))
                .executes((sender, arguments) -> {
                    showStatus(sender, arguments, true);
                    return 1;
                }))
                .under("afk");

        this.register("kick", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_KICK_DESC.text())
                .withPermission(AfkPerms.COMMAND_KICK)
                .withArguments(new StringArgument("player").replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> CommandArgumentConstants.onlinePlayerNames())))
                .executes((sender, arguments) -> {
                    kickPlayer(sender, arguments);
                    return 1;
                }))
                .under("afk");

        this.register("gui", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_GUI_DESC.text())
                .withPermission(AfkPerms.COMMAND_GUI)
                .executesPlayer((player, arguments) -> {
                    if (!this.module.openAdminGui(player)) {
                        this.module.sendPrefixed(AfkLang.AFK_GUI_UNAVAILABLE, player);
                    }
                    return 1;
                }))
                .under("afk");

        this.registerRoot("afk", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_TOGGLE_DESC.text())
                .withPermission(AfkPerms.COMMAND)
                .executes((sender, arguments) -> {
                    return this.toggleAfkMode(sender, arguments, ToggleMode.TOGGLE);
                }));
    }

    private int toggleAfkMode(final CommandSender sender, final CommandArguments arguments, final ToggleMode mode) {
        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }

        return target.runAs(this.module, sender, AfkPerms.COMMAND_OTHERS, (user, targetPlayer) -> {
            final boolean state = mode.apply(this.module.isAfk(targetPlayer));
            if (state) {
                this.module.handler().enterAfk(targetPlayer, false);
            } else {
                this.module.handler().exitAfk(targetPlayer, false);
            }

            if (sender != targetPlayer) {
                this.module.sendPrefixed(AfkLang.AFK_TOGGLE_FEEDBACK, sender, builder -> builder
                        .with(CommonPlaceholders.PLAYER.resolver(targetPlayer)));
            }
        });
    }

    private void showStatus(final CommandSender sender, final CommandArguments arguments, final boolean detailed) {
        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return;
        }

        final String name = target.playerName();
        final Player player = name == null ? (sender instanceof final Player p ? p : null) : Utils.getPlayer(name);
        if (player == null) {
            if (name == null)
                this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            else
                this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return;
        }
        if (name != null && sender != player && !sender.hasPermission(AfkPerms.COMMAND_OTHERS)) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return;
        }

        final int idleSeconds = this.module.handler().getIdleTime(player);
        final long afkDurationSeconds = this.module.handler().getAfkDuration(player);
        final long durationMs = java.util.concurrent.TimeUnit.MILLISECONDS
                .convert(afkDurationSeconds, java.util.concurrent.TimeUnit.SECONDS);
        final long idleMs = java.util.concurrent.TimeUnit.MILLISECONDS
                .convert(idleSeconds, java.util.concurrent.TimeUnit.SECONDS);
        final int untilAfk = Math.max(0, this.module.handler().getTimeToAfk(player) - idleSeconds);
        final int untilKick = this.module.handler().getSecondsUntilKick(player);

        final PlaceholderContext context = PlaceholderContext.builder()
                .with(CommonPlaceholders.PLAYER.resolver(player))
                .with(SLPlaceholders.GENERIC_STATE, () -> this.module.isAfk(player) ? "AFK" : "Active")
                .with("%idle%", () -> TimeFormats.formatAmount(idleMs, TimeFormatType.LITERAL))
                .with("%afk_time%", () -> TimeFormats.formatAmount(durationMs, TimeFormatType.LITERAL))
                .with("%total%",
                        () -> TimeFormats.formatAmount(this.module.handler().getTotalAfkTime(player),
                                TimeFormatType.LITERAL))
                .with("%count%", () -> String.valueOf(this.module.handler().getAfkCount(player)))
                .with("%until_afk%",
                        () -> untilAfk <= 0 ? "now"
                                : TimeFormats.formatAmount(java.util.concurrent.TimeUnit.MILLISECONDS.convert(untilAfk,
                                        java.util.concurrent.TimeUnit.SECONDS), TimeFormatType.LITERAL))
                .with("%until_kick%",
                        () -> untilKick < 0 ? "never"
                                : TimeFormats.formatAmount(java.util.concurrent.TimeUnit.MILLISECONDS.convert(untilKick,
                                        java.util.concurrent.TimeUnit.SECONDS), TimeFormatType.LITERAL))
                .build();

        this.module.sendPrefixed(detailed ? AfkLang.AFK_INFO : AfkLang.AFK_STATUS, sender, context);
    }

    private void kickPlayer(final CommandSender sender, final CommandArguments arguments) {
        final String name = (String) arguments.get("player");
        final Player target = name == null ? null : Utils.getPlayer(name);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return;
        }

        this.module.handler().kickForIdling(target);
        this.module.sendPrefixed(AfkLang.AFK_KICK_FEEDBACK, sender,
                builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
    }

    private int showList(final CommandSender sender) {
        final List<Player> afkPlayers = this.module.handler().getAfkPlayers();

        if (afkPlayers.isEmpty()) {
            this.module.sendPrefixed(AfkLang.AFK_LIST_EMPTY, sender);
            return 1;
        }

        final String list = afkPlayers.stream()
                .map(player -> GREEN.wrap(player.getName()) + " "
                        + DARK_GRAY.wrap("(")
                        + ORANGE.wrap(TimeFormats.formatSince(this.module.handler().getAfkEnterTimestamp(player),
                                TimeFormatType.LITERAL))
                        + DARK_GRAY.wrap(")"))
                .collect(Collectors.joining(DARK_GRAY.wrap(", ")));

        final PlaceholderContext contextBuilder = PlaceholderContext.builder()
                .with(SLPlaceholders.GENERIC_LIST, () -> list)
                .build();
        this.module.sendPrefixed(AfkLang.AFK_LIST, sender, contextBuilder);
        return 1;
    }
}
