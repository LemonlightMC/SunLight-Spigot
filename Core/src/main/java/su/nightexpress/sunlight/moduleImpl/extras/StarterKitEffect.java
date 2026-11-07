package su.nightexpress.sunlight.moduleImpl.extras;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public record StarterKitEffect(@NotNull PotionEffectType type, int durationSeconds, int amplifier) {

    @Nullable
    public static StarterKitEffect parse(@NotNull String raw) {
        String[] parts = raw.trim().split(":");
        if (parts.length == 0) return null;

        PotionEffectType type = PotionEffectType.getByName(parts[0].trim().toUpperCase(Locale.ROOT));
        if (type == null) return null;

        int durationSeconds = -1;
        int amplifier = 1;

        if (parts.length == 2) {
            amplifier = parseInt(parts[1], 1);
        }
        else if (parts.length >= 3) {
            durationSeconds = parseInt(parts[1], -1);
            amplifier = parseInt(parts[2], 1);
        }

        return new StarterKitEffect(type, durationSeconds, amplifier);
    }

    @NotNull
    public static StarterKitEffect legacy(@NotNull String typeName, int amplifier) {
        PotionEffectType type = PotionEffectType.getByName(typeName.trim().toUpperCase(Locale.ROOT));
        if (type == null) type = PotionEffectType.SPEED;
        return new StarterKitEffect(type, -1, amplifier);
    }

    @NotNull
    public String serialize() {
        return this.type.getName() + ":" + this.durationSeconds + ":" + this.amplifier;
    }

    @Nullable
    public PotionEffect createEffect() {
        int amp = Math.max(0, this.amplifier - 1);
        int ticks = this.durationSeconds < 0 ? Integer.MAX_VALUE : Math.max(1, this.durationSeconds) * 20;
        return new PotionEffect(this.type, ticks, amp, false, false, false);
    }

    private static int parseInt(@NotNull String input, int fallback) {
        try {
            return Integer.parseInt(input.trim());
        }
        catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
