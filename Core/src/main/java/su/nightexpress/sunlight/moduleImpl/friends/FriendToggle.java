package su.nightexpress.sunlight.moduleImpl.friends;

import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.property.UserProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The three per-player friends toggles, addressed by name.
 * <p>
 * Named rather than numbered because they are user-facing: the command and the
 * settings menu take
 * the same strings, so a typo has to fail loudly in one place only.
 */
public enum FriendToggle {

    REQUESTS("requests", "Friend Requests", FriendsProperties.REQUESTS_ENABLED),
    LOGIN_ALERTS("alerts", "Login Alerts", FriendsProperties.LOGIN_ALERTS),
    TELEPORT("teleport", "Friend Teleport", FriendsProperties.FRIEND_TELEPORT);

    private final String id;
    private final String display;
    private final UserProperty<Boolean> property;

    FriendToggle(final String id, final String display, final UserProperty<Boolean> property) {
        this.id = id;
        this.display = display;
        this.property = property;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.display;
    }

    /** Reads the toggle for a loaded user. */
    public boolean get(final SunUser user) {
        return user.getPropertyOrDefault(this.property);
    }

    /** Records the toggle on a loaded user. The caller persists the user. */
    public void set(final SunUser user, final boolean value) {
        user.setProperty(this.property, value);
        user.markDirty();
    }

    /**
     * The ids, for a command's suggestions and for the "not a friends setting"
     * message.
     */
    public static List<String> ids() {
        return Arrays.stream(values()).map(FriendToggle::id).toList();
    }

    public static FriendToggle byId(final String id) {
        if (id == null) {
            return null;
        }
        final String key = id.trim().toLowerCase(Locale.ROOT);
        for (final FriendToggle toggle : values()) {
            if (toggle.id.equals(key)) {
                return toggle;
            }
        }
        return null;
    }
}