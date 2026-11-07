package su.nightexpress.sunlight.moduleImpl.extras;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum StarterKitSoundAudience {

    PLAYER,
    EVERYONE,
    WORLD;

    @NotNull
    public static StarterKitSoundAudience byName(@NotNull String name) {
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException exception) {
            return PLAYER;
        }
    }

    public void play(@NotNull Player target, @NotNull Sound sound, float volume, float pitch) {
        Location location = target.getLocation();
        switch (this) {
            case PLAYER -> target.playSound(location, sound, volume, pitch);
            case EVERYONE -> {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    viewer.playSound(viewer.getLocation(), sound, volume, pitch);
                }
            }
            case WORLD -> {
                World world = target.getWorld();
                for (Player viewer : world.getPlayers()) {
                    viewer.playSound(viewer.getLocation(), sound, volume, pitch);
                }
            }
        }
    }
}
