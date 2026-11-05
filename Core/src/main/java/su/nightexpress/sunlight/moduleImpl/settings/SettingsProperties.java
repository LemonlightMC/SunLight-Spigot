package su.nightexpress.sunlight.moduleImpl.settings;

import java.util.List;

import su.nightexpress.sunlight.user.property.UserProperty;

public class SettingsProperties {

    public static final List<String> CHAT_COLORS = List.of("none", "red", "blue", "green", "yellow", "aqua", "gold", "white", "gray");
    public static final List<String> CHAT_GRADIENTS = List.of("none", "rainbow", "sunset", "ocean");
    public static final List<String> CHAT_FORMATTING_VALUES = List.of("none", "bold", "italic", "underline", "bold_italic");

    public static final UserProperty<String> PM_MODE = UserProperty.create("settings_pm_mode", String.class, "everyone", true);
    public static final UserProperty<String> MENTIONS_MODE = UserProperty.create("settings_mentions_mode", String.class, "everyone", true);

    public static final UserProperty<String> CHAT_COLOR = UserProperty.create("settings_chat_color", String.class, "none", true);
    public static final UserProperty<String> CHAT_COLOR_GRADIENT = UserProperty.create("settings_chat_gradient", String.class, "none", true);
    public static final UserProperty<String> CHAT_FORMATTING = UserProperty.create("settings_chat_formatting", String.class, "none", true);

    public static final UserProperty<String> FRIEND_REQUEST_MODE = UserProperty.create("settings_friend_request_mode", String.class, "everyone", true);
    public static final UserProperty<String> FRIEND_TELEPORT_MODE = UserProperty.create("settings_friend_teleport_mode", String.class, "everyone", true);

    public static final UserProperty<String> TPA_REQUEST_MODE = UserProperty.create("settings_tpa_request_mode", String.class, "everyone", true);
    public static final UserProperty<String> TPA_HERE_MODE = UserProperty.create("settings_tpa_here_mode", String.class, "everyone", true);
    public static final UserProperty<String> TPA_AUTO_ACCEPT_MODE = UserProperty.create("settings_tpa_auto_accept_mode", String.class, "nobody", true);

    public static final UserProperty<String> PAY_ALLOW_MODE = UserProperty.create("settings_pay_allow_mode", String.class, "everyone", true);
    public static final UserProperty<String> PAY_ALERTS_MODE = UserProperty.create("settings_pay_alerts_mode", String.class, "everyone", true);

    public static final UserProperty<Boolean> SCOREBOARD_ENABLED = UserProperty.create("settings_scoreboard", Boolean.class, true, true);

    public static final UserProperty<String> JOIN_MESSAGE_MODE = UserProperty.create("settings_join_message_mode", String.class, "everyone", true);
    public static final UserProperty<String> QUIT_MESSAGE_MODE = UserProperty.create("settings_quit_message_mode", String.class, "everyone", true);
    public static final UserProperty<String> DEATH_MESSAGE_MODE = UserProperty.create("settings_death_message_mode", String.class, "everyone", true);

    public static final UserProperty<Boolean> REMINDER_PLAYTIME = UserProperty.create("settings_reminder_playtime", Boolean.class, true, true);
    public static final UserProperty<Boolean> REMINDER_VOTING = UserProperty.create("settings_reminder_voting", Boolean.class, true, true);
    public static final UserProperty<Boolean> REMINDER_ADVERTISEMENTS = UserProperty.create("settings_reminder_advertisements", Boolean.class, true, true);

    public static final UserProperty<Boolean> NIGHT_VISION = UserProperty.create("settings_night_vision", Boolean.class, false, true);

    public static final UserProperty<String> SPEED_LEVEL = UserProperty.create("settings_speed_level", String.class, "0", true);
    public static final UserProperty<String> JUMP_LEVEL = UserProperty.create("settings_jump_level", String.class, "0", true);
    public static final UserProperty<Boolean> INVISIBILITY = UserProperty.create("settings_invisibility", Boolean.class, false, true);

    private SettingsProperties() {
    }
}