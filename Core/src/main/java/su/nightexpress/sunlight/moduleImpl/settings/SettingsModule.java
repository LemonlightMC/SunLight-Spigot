package su.nightexpress.sunlight.moduleImpl.settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.chat.ChatModule;
import su.nightexpress.sunlight.moduleImpl.chat.ChatProperties;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialModule;
import su.nightexpress.sunlight.moduleImpl.friends.FriendsProperties;
import su.nightexpress.sunlight.moduleImpl.glow.GlowModule;
import su.nightexpress.sunlight.moduleImpl.nametags.NametagsModule;
import su.nightexpress.sunlight.moduleImpl.profiles.ProfilesModule;
import su.nightexpress.sunlight.moduleImpl.ptp.PTPProperties;
import su.nightexpress.sunlight.moduleImpl.settings.command.SettingsCommand;
import su.nightexpress.sunlight.moduleImpl.settings.config.SettingsLang;
import su.nightexpress.sunlight.moduleImpl.settings.config.SettingsPerms;
import su.nightexpress.sunlight.moduleImpl.settings.listener.SettingsListener;
import su.nightexpress.sunlight.moduleImpl.settings.menu.PanelMenu;
import su.nightexpress.sunlight.moduleImpl.settings.model.AudienceMode;
import su.nightexpress.sunlight.moduleImpl.settings.model.SettingDefinition;
import su.nightexpress.sunlight.moduleImpl.settings.model.SettingKind;
import su.nightexpress.sunlight.moduleImpl.settings.model.SettingsPanel;
import su.nightexpress.sunlight.moduleImpl.settings.registry.SettingsRegistry;
import su.nightexpress.sunlight.moduleImpl.vanish.VanishModule;
import su.nightexpress.sunlight.moduleImpl.voting.VotingModule;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.property.UserProperty;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;

public class SettingsModule extends Module {

    private static final List<String> AUDIENCE_FULL = List.of("everyone", "team", "friends", "nobody");
    private static final List<String> AUDIENCE_NO_FRIENDS = List.of("everyone", "team", "nobody");

    private final SettingsRegistry registry;
    private final Map<String, PanelMenu> menus;

    public SettingsModule(final ModuleDefinition<SettingsModule> definition, final SunLightPlugin plugin) {
        super(definition, plugin);
        this.registry = new SettingsRegistry();
        this.menus = new LinkedHashMap<>();
    }

    @Override
    protected void loadModule(final FileConfig config) {
        this.plugin.injectLang(SettingsLang.class);
        this.registerProperties();

        this.buildPanels();
        this.loadMenus();

        this.addListener(new SettingsListener(this.plugin, this));
        this.commandRegistry.addProvider(new SettingsCommand(this));
    }

    @Override
    protected void unloadModule() {
        this.registry.clear();
        this.menus.clear();
    }

    @Override
    public void registerPlaceholders(final PlaceholderRegistry registry) {
    }

    private void registerProperties() {
        UserPropertyRegistry.register(SettingsProperties.PM_MODE);
        UserPropertyRegistry.register(SettingsProperties.MENTIONS_MODE);
        UserPropertyRegistry.register(SettingsProperties.CHAT_COLOR);
        UserPropertyRegistry.register(SettingsProperties.CHAT_COLOR_GRADIENT);
        UserPropertyRegistry.register(SettingsProperties.CHAT_FORMATTING);
        UserPropertyRegistry.register(SettingsProperties.FRIEND_REQUEST_MODE);
        UserPropertyRegistry.register(SettingsProperties.FRIEND_TELEPORT_MODE);
        UserPropertyRegistry.register(SettingsProperties.TPA_REQUEST_MODE);
        UserPropertyRegistry.register(SettingsProperties.TPA_HERE_MODE);
        UserPropertyRegistry.register(SettingsProperties.TPA_AUTO_ACCEPT_MODE);
        UserPropertyRegistry.register(SettingsProperties.PAY_ALLOW_MODE);
        UserPropertyRegistry.register(SettingsProperties.PAY_ALERTS_MODE);
        UserPropertyRegistry.register(SettingsProperties.SCOREBOARD_ENABLED);
        UserPropertyRegistry.register(SettingsProperties.JOIN_MESSAGE_MODE);
        UserPropertyRegistry.register(SettingsProperties.QUIT_MESSAGE_MODE);
        UserPropertyRegistry.register(SettingsProperties.DEATH_MESSAGE_MODE);
        UserPropertyRegistry.register(SettingsProperties.REMINDER_PLAYTIME);
        UserPropertyRegistry.register(SettingsProperties.REMINDER_VOTING);
        UserPropertyRegistry.register(SettingsProperties.REMINDER_ADVERTISEMENTS);
        UserPropertyRegistry.register(SettingsProperties.NIGHT_VISION);
        UserPropertyRegistry.register(SettingsProperties.SPEED_LEVEL);
        UserPropertyRegistry.register(SettingsProperties.JUMP_LEVEL);
        UserPropertyRegistry.register(SettingsProperties.INVISIBILITY);
        UserPropertyRegistry.register(EssentialModule.GOD);
        UserPropertyRegistry.register(VanishModule.VANISH);

        // Properties owned by other modules but also exposed here.
        UserPropertyRegistry.register(ChatProperties.CONVERSATIONS);
        UserPropertyRegistry.register(ChatProperties.MENTIONS);
        UserPropertyRegistry.register(ChatProperties.CHAT_SPY_INFO);
        UserPropertyRegistry.register(ChatProperties.SOCIAL_SPY_INFO);
        UserPropertyRegistry.register(ChatProperties.COMMAND_SPY_INFO);
        UserPropertyRegistry.register(FriendsProperties.REQUESTS_ENABLED);
        UserPropertyRegistry.register(FriendsProperties.LOGIN_ALERTS);
        UserPropertyRegistry.register(FriendsProperties.FRIEND_TELEPORT);
        UserPropertyRegistry.register(PTPProperties.TELEPORT_REQUESTS);
    }

    private void buildPanels() {
        this.registry.register(this.buildRootPanel());
        this.registry.register(this.buildChatPanel());
        this.registry.register(this.buildChatColorPanel());
        this.registry.register(this.buildFriendsPanel());
        this.registry.register(this.buildTpaPanel());
        this.registry.register(this.buildPaymentsPanel());
        this.registry.register(this.buildGeneralPanel());
        this.registry.register(this.buildPlayerPanel());
        this.registry.register(this.buildAdminPanel());
        this.registry.register(this.buildSpyPanel());
    }

    private SettingsPanel buildRootPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.link("chat", SettingsPerms.PANEL_CHAT, "chat", Material.BOOK, "Chat", "Private messages, mentions and chat color."));
        settings.add(this.link("friends", SettingsPerms.PANEL_FRIENDS, "friends", Material.PLAYER_HEAD, "Friends", "Friend requests and teleports."));
        settings.add(this.link("tpa", SettingsPerms.PANEL_TPA, "tpa", Material.ENDER_PEARL, "Teleport Requests", "TPA and TPAHere requests."));
        settings.add(this.link("payments", SettingsPerms.PANEL_PAYMENTS, "payments", Material.GOLD_INGOT, "Payments", "Allow payments and payment alerts."));
        settings.add(this.link("general", SettingsPerms.PANEL_GENERAL, "general", Material.COMPASS, "General", "Messages, scoreboard and reminders."));
        settings.add(this.link("player", SettingsPerms.PANEL_PLAYER, "player", Material.NETHER_STAR, "Player", "Night vision, glow, nametag and profile."));
        settings.add(this.link("admin", SettingsPerms.PANEL_ADMIN, "admin", Material.REDSTONE, "Admin", "Spy and staff tools."));
        return new SettingsPanel("root", SettingsLang.MENU_ROOT_TITLE.text(), null, MenuType.GENERIC_9X5, settings, null);
    }

    private SettingsPanel buildChatPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.multi("pm", SettingsPerms.SETTING_PM, SettingsProperties.PM_MODE, Material.PAPER, "Allow PM", "Who can send you private messages.", AUDIENCE_FULL));
        settings.add(this.multi("mentions", SettingsPerms.SETTING_MENTIONS, SettingsProperties.MENTIONS_MODE, Material.NAME_TAG, "Mentions", "Who can mention you in chat.", AUDIENCE_FULL));
        settings.add(this.link("chat_color", SettingsPerms.PANEL_CHAT_COLOR, "chat_color", Material.WRITABLE_BOOK, "Chat Color", "Single color, gradient and formatting."));
        return new SettingsPanel("chat", SettingsLang.MENU_CHAT_TITLE.text(), SettingsPerms.PANEL_CHAT, MenuType.GENERIC_9X3, settings, "root");
    }

    private SettingsPanel buildChatColorPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.multi("color", SettingsPerms.SETTING_CHAT_COLOR, SettingsProperties.CHAT_COLOR, Material.RED_DYE, "Color",
                "Your chat color.", SettingsProperties.CHAT_COLORS));
        settings.add(this.multi("gradient", SettingsPerms.SETTING_CHAT_COLOR, SettingsProperties.CHAT_COLOR_GRADIENT, Material.PURPLE_DYE, "Gradient",
                "Your chat gradient.", SettingsProperties.CHAT_GRADIENTS));
        settings.add(this.multi("formatting", SettingsPerms.SETTING_CHAT_COLOR, SettingsProperties.CHAT_FORMATTING, Material.BOOK, "Formatting",
                "Your chat formatting.", SettingsProperties.CHAT_FORMATTING_VALUES));
        return new SettingsPanel("chat_color", SettingsLang.MENU_CHAT_COLOR_TITLE.text(), SettingsPerms.PANEL_CHAT_COLOR, MenuType.GENERIC_9X3, settings, "chat");
    }

    private SettingsPanel buildFriendsPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.multi("requests", SettingsPerms.SETTING_FRIEND_REQUESTS, SettingsProperties.FRIEND_REQUEST_MODE, Material.PAPER, "Friend Requests",
                "Who can send you friend requests.", AUDIENCE_NO_FRIENDS));
        settings.add(this.multi("teleport", SettingsPerms.SETTING_FRIEND_TELEPORT, SettingsProperties.FRIEND_TELEPORT_MODE, Material.ENDER_PEARL, "Friend Teleports",
                "Who among your friends can teleport to you.", AUDIENCE_NO_FRIENDS));
        return new SettingsPanel("friends", SettingsLang.MENU_FRIENDS_TITLE.text(), SettingsPerms.PANEL_FRIENDS, MenuType.GENERIC_9X3, settings, "root");
    }

    private SettingsPanel buildTpaPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.multi("tpa", SettingsPerms.SETTING_TPA, SettingsProperties.TPA_REQUEST_MODE, Material.ENDER_PEARL, "Allow TPA",
                "Who can send you TPA requests.", AUDIENCE_FULL));
        settings.add(this.multi("tpahere", SettingsPerms.SETTING_TPAHERE, SettingsProperties.TPA_HERE_MODE, Material.ENDER_EYE, "Allow TPAHere",
                "Who can send you TPAHere requests.", AUDIENCE_FULL));
        settings.add(this.multi("auto_accept", SettingsPerms.SETTING_TPA_AUTO, SettingsProperties.TPA_AUTO_ACCEPT_MODE, Material.CLOCK, "Auto Accept TPA",
                "Who may be teleported to you without asking.", AUDIENCE_FULL));
        return new SettingsPanel("tpa", SettingsLang.MENU_TPA_TITLE.text(), SettingsPerms.PANEL_TPA, MenuType.GENERIC_9X3, settings, "root");
    }

    private SettingsPanel buildPaymentsPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.multi("pay", SettingsPerms.SETTING_PAY, SettingsProperties.PAY_ALLOW_MODE, Material.GOLD_INGOT, "Allow Payments",
                "Who can send you money.", AUDIENCE_FULL));
        settings.add(this.multi("pay_alerts", SettingsPerms.SETTING_PAY_ALERTS, SettingsProperties.PAY_ALERTS_MODE, Material.BELL, "Payment Alerts",
                "Who can trigger payment notifications.", AUDIENCE_FULL));
        return new SettingsPanel("payments", SettingsLang.MENU_PAYMENTS_TITLE.text(), SettingsPerms.PANEL_PAYMENTS, MenuType.GENERIC_9X3, settings, "root");
    }

    private SettingsPanel buildGeneralPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.toggle("scoreboard", SettingsPerms.SETTING_SCOREBOARD, SettingsProperties.SCOREBOARD_ENABLED, Material.PAPER, "Scoreboard", "Show or hide the scoreboard."));
        settings.add(this.multi("join_message", SettingsPerms.SETTING_JOIN_MESSAGE, SettingsProperties.JOIN_MESSAGE_MODE, Material.OAK_DOOR, "Join Messages",
                "Who sees your join message.", AUDIENCE_FULL));
        settings.add(this.multi("quit_message", SettingsPerms.SETTING_QUIT_MESSAGE, SettingsProperties.QUIT_MESSAGE_MODE, Material.IRON_DOOR, "Quit Messages",
                "Who sees your quit message.", AUDIENCE_FULL));
        settings.add(this.multi("death_message", SettingsPerms.SETTING_DEATH_MESSAGE, SettingsProperties.DEATH_MESSAGE_MODE, Material.SKELETON_SKULL, "Death Messages",
                "Who sees your death message.", AUDIENCE_FULL));
        settings.add(this.toggle("reminder_playtime", SettingsPerms.SETTING_REMINDERS, SettingsProperties.REMINDER_PLAYTIME, Material.CLOCK, "Playtime Reminders", "Receive playtime goal reminders."));
        settings.add(this.toggle("reminder_voting", SettingsPerms.SETTING_REMINDERS, SettingsProperties.REMINDER_VOTING, Material.EMERALD, "Voting Reminders", "Receive voting reminders."));
        settings.add(this.toggle("reminder_ads", SettingsPerms.SETTING_REMINDERS, SettingsProperties.REMINDER_ADVERTISEMENTS, Material.BOOK, "Advertisement Reminders", "Receive advertisement reminders."));
        return new SettingsPanel("general", SettingsLang.MENU_GENERAL_TITLE.text(), SettingsPerms.PANEL_GENERAL, MenuType.GENERIC_9X4, settings, "root");
    }

    private SettingsPanel buildPlayerPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.toggle("night_vision", SettingsPerms.SETTING_NIGHT_VISION, SettingsProperties.NIGHT_VISION, Material.ENDER_EYE, "Night Vision", "Permanent night vision."));
        settings.add(this.action("glow", SettingsPerms.SETTING_GLOW, Material.GLOWSTONE_DUST, "Glow", "Open the glow menu.",
                player -> this.plugin.moduleManager().getByType(GlowModule.class).ifPresent(module -> module.openGlowMenu(player))));
        settings.add(this.action("nametag", SettingsPerms.SETTING_NAMETAG, Material.NAME_TAG, "Nametag", "Open the nametag menu.",
                player -> this.plugin.moduleManager().getByType(NametagsModule.class).ifPresent(module -> module.openTagsMenu(player))));
        settings.add(this.action("profile", SettingsPerms.SETTING_PROFILE, Material.PLAYER_HEAD, "Profile", "Open the profile menu.",
                player -> this.plugin.moduleManager().getByType(ProfilesModule.class).ifPresent(module -> module.openProfilesMenu(player))));
        return new SettingsPanel("player", SettingsLang.MENU_PLAYER_TITLE.text(), SettingsPerms.PANEL_PLAYER, MenuType.GENERIC_9X3, settings, "root");
    }

    private SettingsPanel buildAdminPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.link("spy", SettingsPerms.PANEL_SPY, "spy", Material.SPYGLASS, "Spy", "Open the spy menu."));
        settings.add(this.toggle("god", SettingsPerms.SETTING_GOD, EssentialModule.GOD, Material.TOTEM_OF_UNDYING, "God", "Toggle god mode."));
        settings.add(this.multi("speed", SettingsPerms.SETTING_SPEED, SettingsProperties.SPEED_LEVEL, Material.FEATHER, "Speed",
                "Set your walk and fly speed.", List.of("0", "1", "2", "3", "4", "5")));
        settings.add(this.multi("jump", SettingsPerms.SETTING_JUMP, SettingsProperties.JUMP_LEVEL, Material.SLIME_BALL, "Jump",
                "Set your jump boost level.", List.of("0", "1", "2", "3", "4", "5")));
        settings.add(this.toggle("vanish", SettingsPerms.SETTING_VANISH, VanishModule.VANISH, Material.GLASS, "Vanish", "Toggle vanish."));
        settings.add(this.toggle("invisibility", SettingsPerms.SETTING_INVISIBILITY, SettingsProperties.INVISIBILITY, Material.POTION, "Invisibility", "Toggle invisibility."));
        return new SettingsPanel("admin", SettingsLang.MENU_ADMIN_TITLE.text(), SettingsPerms.PANEL_ADMIN, MenuType.GENERIC_9X4, settings, "root");
    }

    private SettingsPanel buildSpyPanel() {
        List<SettingDefinition> settings = new ArrayList<>();
        settings.add(this.toggle("spy_chat", SettingsPerms.SETTING_SPY_CHAT, ChatProperties.CHAT_SPY_INFO, Material.ENDER_EYE, "Chat Spy", "See other players' chat."));
        settings.add(this.toggle("spy_social", SettingsPerms.SETTING_SPY_SOCIAL, ChatProperties.SOCIAL_SPY_INFO, Material.PLAYER_HEAD, "Social Spy", "See social commands."));
        settings.add(this.toggle("spy_command", SettingsPerms.SETTING_SPY_COMMAND, ChatProperties.COMMAND_SPY_INFO, Material.COMMAND_BLOCK, "Command Spy", "See commands used by players."));
        settings.add(this.action("open_spy", SettingsPerms.PANEL_SPY, Material.SPYGLASS, "Open Spy Menu", "Choose who to spy on.",
                player -> this.plugin.moduleManager().getByType(ChatModule.class)
                        .ifPresent(module -> module.openSpyDialog(player))));
        return new SettingsPanel("spy", SettingsLang.MENU_SPY_TITLE.text(), SettingsPerms.PANEL_SPY, MenuType.GENERIC_9X3, settings, "admin");
    }

    private SettingDefinition link(String id, String permission, String target, Material icon, String name, String description) {
        return SettingDefinition.builder(id, SettingKind.LINK)
                .permission(permission)
                .targetPanel(target)
                .icon(icon)
                .displayName(name)
                .description(description)
                .build();
    }

    private SettingDefinition action(String id, String permission, Material icon, String name, String description, java.util.function.Consumer<Player> action) {
        return SettingDefinition.builder(id, SettingKind.ACTION)
                .permission(permission)
                .icon(icon)
                .displayName(name)
                .description(description)
                .action(action)
                .build();
    }

    private SettingDefinition toggle(String id, String permission, UserProperty<Boolean> property, Material icon, String name, String description) {
        return SettingDefinition.builder(id, SettingKind.TOGGLE)
                .permission(permission)
                .property(property)
                .icon(icon)
                .displayName(name)
                .description(description)
                .build();
    }

    private SettingDefinition multi(String id, String permission, UserProperty<String> property, Material icon, String name, String description, List<String> values) {
        return SettingDefinition.builder(id, SettingKind.MULTI_STATE)
                .permission(permission)
                .property(property)
                .values(values)
                .icon(icon)
                .displayName(name)
                .description(description)
                .build();
    }

    private void loadMenus() {
        for (SettingsPanel panel : this.registry.panels()) {
            String fileName = panel.isRoot() ? "settings.yml" : "settings-" + panel.id() + ".yml";
            FileConfig config = FileConfig.load(this.getLocalUIPath(), fileName);

            // Read the configured menu type before the default layout is built, so
            // resizing a panel in its config actually resizes the layout too.
            MenuType menuType = config.get(ConfigTypes.MENU_TYPE, "Settings.MenuType", panel.menuType());

            PanelMenu menu = new PanelMenu(this.plugin, this, panel, menuType);
            menu.load(config);
            this.menus.put(panel.id(), menu);
        }
    }

    public boolean openRoot(Player player) {
        return this.openPanel(player, "root");
    }

    public boolean openPanel(Player player, String panelId) {
        SettingsPanel panel = this.registry.get(panelId);
        if (panel == null) return false;
        if (panel.permission() != null && !player.hasPermission(panel.permission())) return false;

        PanelMenu menu = this.menus.get(panelId);
        return menu != null && menu.show(player);
    }

    public void handleSettingChange(Player player, SettingDefinition setting) {
        SunUser user = this.userManager.getOrFetch(player);
        UserProperty<?> property = setting.property();

        if (property == SettingsProperties.PM_MODE) {
            SettingsAccess.syncBoolean(user, SettingsProperties.PM_MODE, ChatProperties.CONVERSATIONS);
        } else if (property == SettingsProperties.MENTIONS_MODE) {
            SettingsAccess.syncBoolean(user, SettingsProperties.MENTIONS_MODE, ChatProperties.MENTIONS);
        } else if (property == SettingsProperties.FRIEND_REQUEST_MODE) {
            SettingsAccess.syncBoolean(user, SettingsProperties.FRIEND_REQUEST_MODE, FriendsProperties.REQUESTS_ENABLED);
        } else if (property == SettingsProperties.FRIEND_TELEPORT_MODE) {
            SettingsAccess.syncBoolean(user, SettingsProperties.FRIEND_TELEPORT_MODE, FriendsProperties.FRIEND_TELEPORT);
        } else if (property == SettingsProperties.TPA_REQUEST_MODE || property == SettingsProperties.TPA_HERE_MODE) {
            boolean requests = SettingsAccess.mode(user, SettingsProperties.TPA_REQUEST_MODE) != AudienceMode.NOBODY
                    || SettingsAccess.mode(user, SettingsProperties.TPA_HERE_MODE) != AudienceMode.NOBODY;
            user.setProperty(PTPProperties.TELEPORT_REQUESTS, requests);
            user.markDirty();
        } else if (property == SettingsProperties.REMINDER_VOTING) {
            this.plugin.moduleManager().getByType(VotingModule.class)
                    .ifPresent(module -> module.setReminderEnabled(player.getUniqueId(), user.getPropertyOrDefault(SettingsProperties.REMINDER_VOTING)));
        } else if (property == VanishModule.VANISH) {
            boolean vanished = user.getPropertyOrDefault(VanishModule.VANISH);
            this.plugin.moduleManager().getByType(VanishModule.class)
                    .ifPresent(module -> module.vanish(player, vanished));
        } else if (property == EssentialModule.GOD) {
            boolean god = user.getPropertyOrDefault(EssentialModule.GOD);
            this.plugin.moduleManager().getByType(EssentialModule.class)
                    .ifPresent(module -> module.setGod(player, god));
        }

        this.applyPersistentEffects(player);
    }

    public void applyPersistentEffects(Player player) {
        SunUser user = this.userManager.getOrFetch(player);

        // Only touch a setting that has actually been chosen, so joining does not
        // override effects granted by other plugins for players who never used this
        // menu.
        if (user.hasProperty(SettingsProperties.NIGHT_VISION)) {
            if (user.getPropertyOrDefault(SettingsProperties.NIGHT_VISION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
            } else {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }

        if (user.hasProperty(SettingsProperties.INVISIBILITY)) {
            if (user.getPropertyOrDefault(SettingsProperties.INVISIBILITY)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            } else {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }

        if (user.hasProperty(SettingsProperties.SPEED_LEVEL)) {
            int speed = this.parseInt(user.getPropertyOrDefault(SettingsProperties.SPEED_LEVEL), 0);
            float speedValue = Math.min(1.0F, 0.2F + (speed * 0.05F));
            player.setWalkSpeed(speedValue);
            player.setFlySpeed(speedValue);
        }

        if (user.hasProperty(SettingsProperties.JUMP_LEVEL)) {
            int jump = this.parseInt(user.getPropertyOrDefault(SettingsProperties.JUMP_LEVEL), 0);
            if (jump > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, jump - 1, false, false, false));
            } else {
                player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            }
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}