package su.nightexpress.sunlight.moduleImpl.timer.service;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.timer.model.ActionBundle;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.utils.EconomyUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class TimerActionService {

    public record ValidationResult(boolean ok, List<String> errors) {
        public static ValidationResult success() { return new ValidationResult(true, List.of()); }
    }

    private final SunLightPlugin plugin;

    public TimerActionService(SunLightPlugin plugin) {
        this.plugin = plugin;
    }

    public ValidationResult validate(ActionBundle bundle) {
        if (bundle == null) return ValidationResult.success();
        List<String> errors = new ArrayList<>();
        if (!bundle.getSound().isBlank() && !isValidSound(bundle.getSound())) errors.add("Invalid sound: " + bundle.getSound() + " (use NAME;volume;pitch)");
        if (!bundle.getParticle().isBlank() && !isValidParticle(bundle.getParticle())) errors.add("Invalid particle: " + bundle.getParticle() + " (use TYPE;count;ox;oy;oz;speed)");
        if (!bundle.getFirework().isBlank() && !isValidFirework(bundle.getFirework())) errors.add("Invalid firework: " + bundle.getFirework() + " (use power;type;color)");
        return new ValidationResult(errors.isEmpty(), errors);
    }

    public void execute(ActionBundle bundle, TimerInstance instance, Collection<Player> viewers) {
        if (bundle == null || bundle.isEmpty()) return;
        List<Player> targets = viewers == null ? List.of() : new ArrayList<>(viewers);

        for (Player player : targets) {
            PlaceholderContext context = PlaceholderContext.builder()
                    .with(CommonPlaceholders.PLAYER.resolver(player))
                    .andThen(SLPlaceholders.forPlayerWithPAPI(player))
                    .build();

            for (String raw : bundle.getMessages()) {
                String text = context.apply(TimerFormat.apply(raw, instance));
                if (!text.isBlank()) Players.sendMessage(player, text);
            }

            for (String raw : bundle.getPlayerCommands()) {
                String cmd = stripSlash(context.apply(TimerFormat.apply(raw, instance)));
                if (!cmd.isBlank()) player.performCommand(cmd);
            }

            if (bundle.getMoney() != 0D && EconomyUtils.hasCurrency()) {
                if (bundle.getMoney() > 0D) EconomyUtils.deposit(player, bundle.getMoney());
                else if (EconomyUtils.canAfford(player, Math.abs(bundle.getMoney()))) EconomyUtils.withdraw(player, Math.abs(bundle.getMoney()));
            }

            playSound(player, bundle.getSound());
            playParticle(player, bundle.getParticle());
            spawnFirework(player, bundle.getFirework());
        }

        if (!bundle.getConsoleCommands().isEmpty()) {
            CommandSender console = Bukkit.getConsoleSender();
            List<String> commands = new ArrayList<>();
            for (String raw : bundle.getConsoleCommands()) {
                String cmd = stripSlash(TimerFormat.apply(raw, instance));
                if (!cmd.isBlank()) commands.add(cmd);
            }
            this.plugin.runTask(() -> {
                for (String command : commands) {
                    try {
                        Bukkit.dispatchCommand(console, command);
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });
        }
    }

    private static String stripSlash(String command) {
        if (command == null) return "";
        String c = command.trim();
        return c.startsWith("/") ? c.substring(1) : c;
    }

    private boolean isValidSound(String raw) {
        try {
            String[] parts = raw.split(";");
            if (parts.length == 0 || parts[0].trim().isBlank()) return false;
            if (parts.length > 1) Float.parseFloat(parts[1]);
            if (parts.length > 2) Float.parseFloat(parts[2]);
            return true;
        } catch (Exception e) { return false; }
    }

    private boolean isValidParticle(String raw) {
        try {
            String[] parts = raw.split(";");
            Particle.valueOf(parts[0].trim().toUpperCase(Locale.ROOT));
            if (parts.length > 1) Integer.parseInt(parts[1]);
            if (parts.length > 2) Double.parseDouble(parts[2]);
            if (parts.length > 3) Double.parseDouble(parts[3]);
            if (parts.length > 4) Double.parseDouble(parts[4]);
            if (parts.length > 5) Double.parseDouble(parts[5]);
            return true;
        } catch (Exception e) { return false; }
    }

    private boolean isValidFirework(String raw) {
        try {
            String[] parts = raw.split(";");
            if (parts.length > 0 && !parts[0].isBlank()) Integer.parseInt(parts[0].trim());
            if (parts.length > 1 && !parts[1].isBlank()) FireworkEffect.Type.valueOf(parts[1].trim().toUpperCase(Locale.ROOT));
            return true;
        } catch (Exception e) { return false; }
    }

    private void playSound(Player player, String raw) {
        if (raw == null || raw.isBlank()) return;
        try {
            String[] parts = raw.split(";");
            String name = parts[0].trim();
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1f;
            org.bukkit.Sound sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT).replace(' ', '_')));
            if (sound == null) {
                try { sound = org.bukkit.Sound.valueOf(name.toUpperCase(Locale.ROOT).replace('.', '_').replace(' ', '_')); } catch (Exception ignored) {}
            }
            if (sound != null) player.playSound(player.getLocation(), sound, volume, pitch);
            else this.plugin.warn("[Timer] Invalid/unknown sound: " + raw);
        }
        catch (Exception ignored) {
            this.plugin.warn("[Timer] Invalid sound spec: " + raw);
        }
    }

    private void playParticle(Player player, String raw) {
        if (raw == null || raw.isBlank()) return;
        try {
            String[] parts = raw.split(";");
            Particle particle = Particle.valueOf(parts[0].trim().toUpperCase(Locale.ROOT));
            int count = parts.length > 1 ? Integer.parseInt(parts[1]) : 10;
            double ox = parts.length > 2 ? Double.parseDouble(parts[2]) : 0.3;
            double oy = parts.length > 3 ? Double.parseDouble(parts[3]) : 0.5;
            double oz = parts.length > 4 ? Double.parseDouble(parts[4]) : 0.3;
            double speed = parts.length > 5 ? Double.parseDouble(parts[5]) : 0.01;
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), Math.max(1, count), ox, oy, oz, speed);
        }
        catch (Exception ignored) {
            this.plugin.warn("[Timer] Invalid particle spec: " + raw);
        }
    }

    private void spawnFirework(Player player, String raw) {
        if (raw == null || raw.isBlank()) return;
        try {
            String[] parts = raw.split(";");
            int power = parts.length > 0 && !parts[0].isBlank() ? Integer.parseInt(parts[0]) : 1;
            FireworkEffect.Type type = parts.length > 1 ? FireworkEffect.Type.valueOf(parts[1].trim().toUpperCase(Locale.ROOT)) : FireworkEffect.Type.BALL;
            Color color = parts.length > 2 ? parseColor(parts[2].split(",")[0]) : Color.YELLOW;
            Location loc = player.getLocation();
            Firework firework = (Firework) player.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(type).withColor(color).trail(true).flicker(false).build());
            meta.setPower(Math.max(0, Math.min(3, power)));
            firework.setFireworkMeta(meta);
        }
        catch (Exception ignored) {
            this.plugin.warn("[Timer] Invalid firework spec: " + raw);
        }
    }

    private static Color parseColor(String raw) {
        if (raw == null) return Color.WHITE;
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "RED" -> Color.RED;
            case "BLUE" -> Color.BLUE;
            case "GREEN", "LIME" -> Color.LIME;
            case "YELLOW" -> Color.YELLOW;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "AQUA", "CYAN" -> Color.AQUA;
            case "WHITE" -> Color.WHITE;
            case "BLACK" -> Color.BLACK;
            case "PINK", "FUCHSIA" -> Color.FUCHSIA;
            default -> Color.WHITE;
        };
    }
}
