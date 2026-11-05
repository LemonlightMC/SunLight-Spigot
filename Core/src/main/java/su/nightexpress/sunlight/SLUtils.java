package su.nightexpress.sunlight;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.sunlight.config.Config;
import su.nightexpress.sunlight.hook.HookId;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SLUtils {

    public static final String CONSOLE_NAME = Bukkit.getServer().getConsoleSender().getName();

    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void setDateFormatter(String pattern) {
        try {
            dateFormatter = DateTimeFormatter.ofPattern(pattern);
        } catch (Exception ignored) {
        }
    }

    public static void setTimeFormatter(String pattern) {
        try {
            timeFormatter = DateTimeFormatter.ofPattern(pattern);
        } catch (Exception ignored) {
        }
    }

    public static boolean hasPacketLibrary() {
        return Utils.isInstalled(HookId.PACKET_EVENTS) || Utils.isInstalled(HookId.PROTOCOL_LIB);
    }

    @Deprecated
    public static String createIdentifier(Player player) {
        String uuid = player.getUniqueId().toString();

        // Bedrock players have UUIDs leading with zeros.
        if (Players.isBedrock(player)) {
            uuid = new StringBuilder(uuid).reverse().toString();
        }

        return uuid;
    }

    public static boolean isVanished(Player player) {
        return player.hasMetadata("vanished");
    }

    public static String formatDate(long timestamp) {
        return dateFormatter.format(TimeUtil.getLocalDateTimeOf(timestamp));
    }

    public static String formatTime(LocalTime localTime) {
        return timeFormatter.format(localTime);
    }

    @Deprecated
    public static String getSenderName(String name) {
        if (name.equalsIgnoreCase(CONSOLE_NAME)) {
            return Config.CONSOLE_NAME.get();
        }
        Player player = Utils.getPlayer(name);
        if (player != null) {
            return player.getDisplayName();
        }
        return name;
    }

    @Deprecated
    public static String getSenderName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return Config.CONSOLE_NAME.get();
        }
        if (sender instanceof Player player) {
            return player.getDisplayName();
        }
        return sender.getName();
    }

    public static Optional<InetAddress> getInetAddress(Player player) {
        return Optional.ofNullable(player.getAddress()).map(InetSocketAddress::getAddress);
    }
}
