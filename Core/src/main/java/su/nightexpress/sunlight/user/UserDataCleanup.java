package su.nightexpress.sunlight.user;

import org.jspecify.annotations.NonNull;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Global user-data cleanup helpers.
 * <p>
 * "Unregistered" properties are keys stored in user data that are not known to
 * {@link UserPropertyRegistry} (e.g. leftovers from removed modules or renamed properties).
 * <p>
 * {@link #SAFE_REMOVABLE_PROPERTIES} lists extra property keys that are safe to wipe/reset.
 * They are removed only when cleanup runs with {@code --include-safe}. This list is static by
 * design (not configurable). Add lowercased property names here when a registered/stored key is
 * disposable and safe to remove globally.
 */
public final class UserDataCleanup {

    /**
     * (A) Extra keys safe to wipe. Removed only with {@code --include-safe}.
     * Values must be normalized (lowercase) property keys.
     */
    public static final Set<String> SAFE_REMOVABLE_PROPERTIES = Set.of();

    private UserDataCleanup() {
    }

    public static String normalizeKey(String key) {
        return key == null ? "" : key.toLowerCase(Locale.ROOT);
    }

    public static boolean isRegistered(String key) {
        return UserPropertyRegistry.getByName(key) != null;
    }

    public static boolean isSafeRemovable(String key) {
        return SAFE_REMOVABLE_PROPERTIES.contains(normalizeKey(key));
    }

    public record PropertiesRow(@NonNull UUID uuid, @NonNull String propertiesJson) {
    }

    public record Result(int rowsScanned, int rowsChanged, int unregisteredRemoved, int safeRemoved,
            int parseErrors, boolean dryRun, boolean includeSafe) {

        public int totalRemoved() {
            return this.unregisteredRemoved + this.safeRemoved;
        }
    }
}
