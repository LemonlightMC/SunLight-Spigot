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
import su.nightexpress.sunlight.moduleImpl.afk.core.AfkLang;
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
                .withPermission("sunlight.command.afk")
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission("sunlight.command.afk.others"))
                .executes((sender, arguments) -> {
                    toggleAfkMode(sender, arguments, ToggleMode.ON);
                    return 1;
                }))
                .aliases("afk-on")
                .under("afk");

        this.register("off", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_OFF_DESC.text())
                .withPermission("sunlight.command.afk")
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission("sunlight.command.afk.others"))
                .executes((sender, arguments) -> {
                    toggleAfkMode(sender, arguments, ToggleMode.OFF);
                    return 1;
                }))
                .aliases("afk-off")
                .under("afk");

        this.register("toggle", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_TOGGLE_DESC.text())
                .withPermission("sunlight.command.afk")
                .withOptionalArguments(
                        CommandArgumentConstants.targetArgument().withPermission("sunlight.command.afk.others"))
                .executes((sender, arguments) -> {
                    toggleAfkMode(sender, arguments, ToggleMode.TOGGLE);
                    return 1;
                }))
                .under("afk");

        this.register("list", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_LIST_DESC.text())
                .withPermission("sunlight.command.afk.list")
                .executes((sender, arguments) -> {
                    showList(sender);
                    return 1;
                }))
                .under("afk");

        this.register("status", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_STATUS_DESC.text())
                .withPermission("sunlight.command.afk.status")
                .withOptionalArguments(CommandArgumentConstants.targetArgument().withPermission("sunlight.command.afk.others"))
                .executes((sender, arguments) -> {
                    showStatus(sender, arguments, false);
                    return 1;
                }))
                .under("afk");

        this.register("info", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_INFO_DESC.text())
                .withPermission("sunlight.command.afk.info")
                .withOptionalArguments(CommandArgumentConstants.targetArgument().withPermission("sunlight.command.afk.others"))
                .executes((sender, arguments) -> {
                    showStatus(sender, arguments, true);
                    return 1;
                }))
                .under("afk");

        this.register("kick", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_KICK_DESC.text())
                .withPermission("sunlight.command.afk.kick")
                .withArguments(new StringArgument("player").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> CommandArgumentConstants.onlinePlayerNames())))
                .executes((sender, arguments) -> {
                    kickPlayer(sender, arguments);
                    return 1;
                }))
                .under("afk");

        this.register("gui", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_GUI_DESC.text())
                .withPermission("sunlight.command.afk.gui")
                .executesPlayer((player, arguments) -> {
                    if (!this.module.openAdminGui(player)) {
                        this.module.sendPrefixed(AfkLang.AFK_GUI_UNAVAILABLE, player);
                    }
                    return 1;
                }))
                .under("afk");

        this.registerRoot("afk", command -> command
                .withFullDescription(AfkLang.COMMAND_AFK_TOGGLE_DESC.text())
                .withPermission("sunlight.command.afk")
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

        return target.runAs(this.module, sender, "sunlight.command.afk.others", (user, targetPlayer) -> {
            final boolean state = mode.apply(this.module.isAfk(targetPlayer));
            if (state) {
                this.module.enterAfk(targetPlayer, false);
            } else {
                this.module.exitAfk(targetPlayer, false);
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
        final Player player = name == null ? (sender instanceof Player p ? p : null) : Utils.getPlayer(name);
        if (player == null) {
            if (name == null) this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            else this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return;
        }
        if (name != null && sender != player && !sender.hasPermission("sunlight.command.afk.others")) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return;
        }

        final long durationMs = java.util.concurrent.TimeUnit.MILLISECONDS.convert(this.module.getAfkDuration(player), java.util.concurrent.TimeUnit.SECONDS);
        final long idleMs = java.util.concurrent.TimeUnit.MILLISECONDS.convert(this.module.getIdleTime(player), java.util.concurrent.TimeUnit.SECONDS);
        final int untilAfk = Math.max(0, this.module.getTimeToAfk(player) - this.module.getIdleTime(player));
        int kickRemaining = -1;
        final int totalKick = this.module.getTimeToKick(player);
        if (totalKick > 0) kickRemaining = Math.max(0, totalKick - this.module.getIdleTime(player));
        final int afkKick = this.module.getAfkKickTime(player);
        if (afkKick > 0 && this.module.isAfk(player)) {
            final int afkRemaining = Math.max(0, afkKick - (int) this.module.getAfkDuration(player));
            kickRemaining = kickRemaining < 0 ? afkRemaining : Math.min(kickRemaining, afkRemaining);
        }
        final int untilKick = kickRemaining;

        final PlaceholderContext context = PlaceholderContext.builder()
                .with(CommonPlaceholders.PLAYER.resolver(player))
                .with(SLPlaceholders.GENERIC_STATE, () -> this.module.isAfk(player) ? "AFK" : "Active")
                .with("%idle%", () -> TimeFormats.formatAmount(idleMs, TimeFormatType.LITERAL))
                .with("%afk_time%", () -> TimeFormats.formatAmount(durationMs, TimeFormatType.LITERAL))
                .with("%total%", () -> TimeFormats.formatAmount(this.module.getTotalAfkTime(player), TimeFormatType.LITERAL))
                .with("%count%", () -> String.valueOf(this.module.getAfkCount(player)))
                .with("%until_afk%", () -> untilAfk <= 0 ? "now" : TimeFormats.formatAmount(java.util.concurrent.TimeUnit.MILLISECONDS.convert(untilAfk, java.util.concurrent.TimeUnit.SECONDS), TimeFormatType.LITERAL))
                .with("%until_kick%", () -> untilKick <= 0 ? "never" : TimeFormats.formatAmount(java.util.concurrent.TimeUnit.MILLISECONDS.convert(untilKick, java.util.concurrent.TimeUnit.SECONDS), TimeFormatType.LITERAL))
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

        this.module.kickForIdling(target);
        this.module.sendPrefixed(AfkLang.AFK_KICK_FEEDBACK, sender, builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
    }

    private int showList(final CommandSender sender) {
        final List<Player> afkPlayers = this.module.getAfkPlayers();

        if (afkPlayers.isEmpty()) {
            this.module.sendPrefixed(AfkLang.AFK_LIST_EMPTY, sender);
            return 1;
        }

        final String list = afkPlayers.stream()
                .map(player -> GREEN.wrap(player.getName()) + " "
                        + DARK_GRAY.wrap("(")
                        + ORANGE.wrap(TimeFormats.formatSince(this.module.getAfkEnterTimestamp(player),
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
