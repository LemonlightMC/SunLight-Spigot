package su.nightexpress.sunlight.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Shared helpers for common reward patterns: command rewards and economy grants.
 */
public final class RewardUtils {

    private RewardUtils() {
    }

    public static @NotNull List<String> sanitizeCommands(@Nullable Collection<String> commands) {
        if (commands == null || commands.isEmpty()) return List.of();
        List<String> result = new ArrayList<>(commands.size());
        for (String command : commands) {
            if (command == null) continue;
            String trimmed = command.trim();
            if (!trimmed.isBlank()) result.add(trimmed);
        }
        return result;
    }

    public static boolean hasCommands(@Nullable Collection<String> commands) {
        return commands != null && commands.stream().anyMatch(s -> s != null && !s.isBlank());
    }

    public static @NotNull List<String> resolveCommands(@NotNull Player player, @Nullable Collection<String> commands, @Nullable Consumer<PlaceholderContext.Builder> extra) {
        List<String> sanitized = sanitizeCommands(commands);
        if (sanitized.isEmpty()) return List.of();

        PlaceholderContext.Builder builder = PlaceholderContext.builder().with(su.nightexpress.nightcore.util.placeholder.CommonPlaceholders.PLAYER.resolver(player));
        if (extra != null) extra.accept(builder);
        PlaceholderContext context = builder.build();

        List<String> resolved = new ArrayList<>(sanitized.size());
        for (String command : sanitized) {
            String out = context.apply(command).trim();
            if (!out.isBlank()) resolved.add(out);
        }
        return resolved;
    }

    public static void executePlayerCommands(@NotNull Player player, @Nullable Collection<String> commands, @Nullable Consumer<PlaceholderContext.Builder> extra) {
        List<String> resolved = resolveCommands(player, commands, extra);
        if (!resolved.isEmpty()) Players.dispatchCommands(player, resolved);
    }

    public static void executeConsoleCommands(@NotNull Player player, @Nullable Collection<String> commands, @Nullable Consumer<PlaceholderContext.Builder> extra) {
        List<String> resolved = resolveCommands(player, commands, extra);
        if (resolved.isEmpty()) return;
        CommandSender console = Bukkit.getConsoleSender();
        for (String command : resolved) {
            Bukkit.dispatchCommand(console, command);
        }
    }

    public static boolean deposit(@Nullable Player player, double amount) {
        if (player == null || amount <= 0D || !EconomyUtils.hasCurrency()) return false;
        try {
            EconomyBridge.api().deposit(player, amount);
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean deposit(@Nullable UUID playerId, double amount) {
        if (playerId == null || amount <= 0D || !EconomyUtils.hasCurrency()) return false;
        try {
            EconomyBridge.api().deposit(playerId, amount);
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean withdraw(@Nullable Player player, double amount) {
        if (player == null || amount <= 0D) return true;
        if (!EconomyUtils.hasCurrency()) return false;
        try {
            if (EconomyBridge.api().queryBalance(player) < amount) return false;
            EconomyBridge.api().withdraw(player, amount);
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static @NotNull String formatMoney(double amount) {
        return EconomyUtils.format(amount);
    }

    public static @NotNull String formatAmount(double amount) {
        return NumberUtil.format(amount);
    }
}
