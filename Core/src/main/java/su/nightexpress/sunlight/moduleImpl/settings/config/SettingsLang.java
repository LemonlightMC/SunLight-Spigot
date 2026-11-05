package su.nightexpress.sunlight.moduleImpl.settings.config;

import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.TextLocale;

public class SettingsLang implements LangContainer {

    public static final TextLocale COMMAND_SETTINGS_DESC = LangEntry.builder("Settings.Command.Settings.Desc")
            .text("Open the settings menu.");

    public static final TextLocale MENU_ROOT_TITLE = LangEntry.builder("Settings.Menu.Root.Title").text("Settings");
    public static final TextLocale MENU_CHAT_TITLE = LangEntry.builder("Settings.Menu.Chat.Title").text("Chat Settings");
    public static final TextLocale MENU_CHAT_COLOR_TITLE = LangEntry.builder("Settings.Menu.ChatColor.Title").text("Chat Color");
    public static final TextLocale MENU_FRIENDS_TITLE = LangEntry.builder("Settings.Menu.Friends.Title").text("Friends Settings");
    public static final TextLocale MENU_TPA_TITLE = LangEntry.builder("Settings.Menu.Tpa.Title").text("Teleport Settings");
    public static final TextLocale MENU_PAYMENTS_TITLE = LangEntry.builder("Settings.Menu.Payments.Title").text("Payment Settings");
    public static final TextLocale MENU_GENERAL_TITLE = LangEntry.builder("Settings.Menu.General.Title").text("General Settings");
    public static final TextLocale MENU_PLAYER_TITLE = LangEntry.builder("Settings.Menu.Player.Title").text("Player Settings");
    public static final TextLocale MENU_ADMIN_TITLE = LangEntry.builder("Settings.Menu.Admin.Title").text("Admin Settings");
    public static final TextLocale MENU_SPY_TITLE = LangEntry.builder("Settings.Menu.Spy.Title").text("Spy Settings");

    private SettingsLang() {
    }
}