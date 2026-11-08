package su.nightexpress.sunlight.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightSound;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Centralized parsing and playback for sounds, particles, and firework effects.
 * <p>
 * Formats:
 * <ul>
 *     <li>Sound: {@code NAME[;volume[;pitch]]} (name may be vanilla enum/key; volume/pitch default 1)</li>
 *     <li>Particle: {@code NAME[;count[;offsetX;offsetY;offsetZ[;speed]]]} (defaults: 1,0,0,0,0)</li>
 *     <li>Firework effect: {@code TYPE;COLORS[;FADE_COLORS][;flicker][;trail][;power]}</li>
 * </ul>
 */
public final class EffectUtils {

    private EffectUtils() {
    }

    // Sounds

    public static @Nullable NightSound parseNightSound(@Nullable String raw) {
        if (raw == null) return null;
        String input = raw.trim();
        if (input.isEmpty() || "none".equalsIgnoreCase(input) || "null".equalsIgnoreCase(input)) return null;

        String[] split = input.split(";", -1);
        String name = split[0].trim();
        if (name.isEmpty()) return null;

        float volume = 1F;
        float pitch = 1F;
        try {
            if (split.length > 1 && !split[1].isBlank()) volume = Float.parseFloat(split[1].trim());
            if (split.length > 2 && !split[2].isBlank()) pitch = Float.parseFloat(split[2].trim());
        }
        catch (NumberFormatException exception) {
            return null;
        }
        return NightSound.of(name, volume, pitch);
    }

    public static boolean isValidSound(@Nullable String raw) {
        if (raw == null || raw.isBlank() || "none".equalsIgnoreCase(raw.trim())) return true;
        return parseNightSound(raw) != null;
    }

    public static @NotNull String soundName(@Nullable String raw) {
        if (raw == null) return "";
        return raw.split(";", -1)[0].trim();
    }

    public static boolean isVanillaSoundName(@Nullable String name) {
        if (name == null) return false;
        String value = name.trim();
        if (value.isEmpty() || value.contains(":") || value.contains(".")) return false;
        try {
            Sound.valueOf(value.toUpperCase(Locale.ROOT));
            return true;
        }
        catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static void playSound(@NotNull Player player, @Nullable String raw) {
        NightSound sound = parseNightSound(raw);
        if (sound == null || sound.isEmpty()) return;
        try {
            sound.play(player);
        }
        catch (Exception ignored) {
        }
    }

    // Particles

    public record ParticleSpec(@NotNull Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        public void spawn(@NotNull Player player, @NotNull Location location) {
            try {
                player.spawnParticle(this.particle, location, Math.max(0, this.count), this.offsetX, this.offsetY, this.offsetZ, this.speed);
            }
            catch (Exception ignored) {
            }
        }

        public void spawnWorld(@NotNull Location location) {
            if (location.getWorld() == null) return;
            try {
                location.getWorld().spawnParticle(this.particle, location, Math.max(0, this.count), this.offsetX, this.offsetY, this.offsetZ, this.speed);
            }
            catch (Exception ignored) {
            }
        }
    }

    public static @Nullable ParticleSpec parseParticle(@Nullable String raw) {
        if (raw == null) return null;
        String input = raw.trim();
        if (input.isEmpty() || "none".equalsIgnoreCase(input) || "null".equalsIgnoreCase(input)) return null;

        String[] split = input.split(";", -1);
        String name = split[0].trim();
        if (name.isEmpty()) return null;

        Optional<Particle> particleOpt = StringUtil.getEnum(name.toUpperCase(Locale.ROOT), Particle.class);
        if (particleOpt.isEmpty()) return null;

        int count = 1;
        double ox = 0D, oy = 0D, oz = 0D, speed = 0D;
        try {
            if (split.length > 1 && !split[1].isBlank()) count = Integer.parseInt(split[1].trim());
            if (split.length > 2 && !split[2].isBlank()) ox = Double.parseDouble(split[2].trim());
            if (split.length > 3 && !split[3].isBlank()) oy = Double.parseDouble(split[3].trim());
            if (split.length > 4 && !split[4].isBlank()) oz = Double.parseDouble(split[4].trim());
            if (split.length > 5 && !split[5].isBlank()) speed = Double.parseDouble(split[5].trim());
        }
        catch (NumberFormatException ignored) {
        }
        return new ParticleSpec(particleOpt.get(), Math.max(0, count), ox, oy, oz, speed);
    }

    public static boolean isValidParticle(@Nullable String raw) {
        if (raw == null || raw.isBlank() || "none".equalsIgnoreCase(raw.trim())) return true;
        String name = raw.split(";", -1)[0].trim();
        return StringUtil.getEnum(name.toUpperCase(Locale.ROOT), Particle.class).isPresent();
    }

    // Fireworks

    public record FireworkSpec(@NotNull FireworkEffect effect, int power) {
        public void spawn(@NotNull Location location, @Nullable Consumer<Firework> modifier) {
            if (location.getWorld() == null) return;
            try {
                location.getWorld().spawn(location, Firework.class, firework -> {
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.clearEffects();
                    meta.addEffect(this.effect);
                    meta.setPower(Math.max(0, Math.min(127, this.power)));
                    firework.setFireworkMeta(meta);
                    if (modifier != null) modifier.accept(firework);
                });
            }
            catch (Exception ignored) {
            }
        }
    }

    /**
     * Parses {@code TYPE;R,G,B:R,G,B[;FADE_R,G,B][;flicker][;trail][;power]}.
     * Colors may also be hex {@code #RRGGBB} or named basics.
     */
    public static @Nullable FireworkSpec parseFirework(@Nullable String raw) {
        if (raw == null) return null;
        String input = raw.trim();
        if (input.isEmpty() || "none".equalsIgnoreCase(input)) return null;

        String[] split = input.split(";", -1);
        Optional<FireworkEffect.Type> typeOpt = StringUtil.getEnum(split[0].trim().toUpperCase(Locale.ROOT), FireworkEffect.Type.class);
        if (typeOpt.isEmpty()) return null;

        List<Color> colors = split.length > 1 ? parseColors(split[1]) : List.of();
        if (colors.isEmpty()) colors = List.of(Color.WHITE);
        List<Color> fades = split.length > 2 ? parseColors(split[2]) : List.of();

        boolean flicker = split.length > 3 && Boolean.parseBoolean(split[3].trim());
        boolean trail = split.length > 4 && Boolean.parseBoolean(split[4].trim());
        int power = 1;
        if (split.length > 5 && !split[5].isBlank()) {
            try { power = Integer.parseInt(split[5].trim()); } catch (NumberFormatException ignored) {}
        }

        FireworkEffect.Builder builder = FireworkEffect.builder()
            .with(typeOpt.get())
            .withColor(colors)
            .flicker(flicker)
            .trail(trail);
        if (!fades.isEmpty()) builder.withFade(fades);
        return new FireworkSpec(builder.build(), power);
    }

    private static @NotNull List<Color> parseColors(@Nullable String raw) {
        List<Color> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) return result;
        for (String token : raw.split("[:\\s]+")) {
            Color color = parseColor(token.trim());
            if (color != null) result.add(color);
        }
        return result;
    }

    public static @Nullable Color parseColor(@Nullable String raw) {
        if (raw == null || raw.isBlank()) return null;
        String value = raw.trim();
        if (value.startsWith("#") && value.length() == 7) {
            try {
                int rgb = Integer.parseInt(value.substring(1), 16);
                return Color.fromRGB(rgb);
            }
            catch (NumberFormatException ignored) {
                return null;
            }
        }
        String[] rgb = value.split(",");
        if (rgb.length == 3) {
            try {
                return Color.fromRGB(
                    Math.clamp(Integer.parseInt(rgb[0].trim()), 0, 255),
                    Math.clamp(Integer.parseInt(rgb[1].trim()), 0, 255),
                    Math.clamp(Integer.parseInt(rgb[2].trim()), 0, 255)
                );
            }
            catch (NumberFormatException ignored) {
            }
        }
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "WHITE" -> Color.WHITE;
            case "BLACK" -> Color.BLACK;
            case "RED" -> Color.RED;
            case "GREEN", "LIME" -> Color.LIME;
            case "BLUE" -> Color.BLUE;
            case "YELLOW" -> Color.YELLOW;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "AQUA", "CYAN" -> Color.AQUA;
            case "FUCHSIA", "MAGENTA", "PINK" -> Color.FUCHSIA;
            case "GRAY", "GREY" -> Color.GRAY;
            case "SILVER", "LIGHT_GRAY", "LIGHT_GREY" -> Color.SILVER;
            case "MAROON" -> Color.MAROON;
            case "NAVY" -> Color.NAVY;
            case "OLIVE" -> Color.OLIVE;
            case "TEAL" -> Color.TEAL;
            default -> null;
        };
    }
}
