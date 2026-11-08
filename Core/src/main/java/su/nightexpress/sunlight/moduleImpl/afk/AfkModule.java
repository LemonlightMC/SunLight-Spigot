package su.nightexpress.sunlight.moduleImpl.afk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

import su.nightexpress.nightcore.bridge.chat.UniversalChatEventHandler;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.api.provider.AfkProvider;
import su.nightexpress.sunlight.hook.HookId;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.afk.command.AfkCommandProvider;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkLang;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkSettings;
import su.nightexpress.sunlight.moduleImpl.afk.hook.AfkContextHook;
import su.nightexpress.sunlight.moduleImpl.afk.hook.AfkDiscordHook;
import su.nightexpress.sunlight.moduleImpl.afk.listener.AfkListener;
import su.nightexpress.sunlight.moduleImpl.afk.menu.AfkAdminMenu;
import su.nightexpress.sunlight.user.property.UserProperty;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;
import su.nightexpress.sunlight.utils.Utils;

public class AfkModule extends Module implements AfkProvider {

    public static final UserProperty<Long> AFK_TOTAL_TIME = UserProperty.create("afk_total_time", Long.class, 0L, true);
    public static final UserProperty<Integer> AFK_COUNT = UserProperty.create("afk_count", Integer.class, 0, true);

    private final AfkSettings settings;

    public AfkHandler handler;
    public UniversalChatEventHandler chatEventHandler;
    public AfkContextHook luckPermsHook;
    public AfkDiscordHook discordHook;
    public AfkAdminMenu adminMenu;

    public AfkModule(final ModuleDefinition<AfkModule> definition, final SunLightPlugin plugin) {
        super(definition, plugin);
        this.settings = new AfkSettings();
        this.handler = new AfkHandler(this);
    }

    @Override
    protected void loadModule(final FileConfig config) {
        this.settings.load(config);
        this.handler.rebuildExemptCaches();
        this.plugin.injectLang(AfkLang.class);

        UserPropertyRegistry.register(AFK_TOTAL_TIME);
        UserPropertyRegistry.register(AFK_COUNT);

        this.addListener(new AfkListener(this.plugin, this));
        if (this.settings.blockChat.get() || this.settings.getActivityPoints(ActivityType.CHAT) > 0) {
            this.chatEventHandler = handler::handleChatEvent;
            this.plugin.addChatHandler(EventPriority.LOWEST, this.chatEventHandler);
        }

        this.addTask(handler::tickTrackers, 20L);
        Utils.onlinePlayers().forEach(handler::track);

        if (this.settings.luckPermsContextEnabled.get() && HookId.hasLuckPerms()) {
            this.luckPermsHook = new AfkContextHook(this);
            this.luckPermsHook.register();
            if (this.luckPermsHook.isAvailable()) {
                this.debug("LuckPerms context 'afkstate' registered.");
            }
        }
        this.discordHook = new AfkDiscordHook(this.settings);
        this.adminMenu = new AfkAdminMenu(this.plugin, this);
        this.commandRegistry.addProvider(new AfkCommandProvider(this));
    }

    @Override
    protected void unloadModule() {
        Utils.onlinePlayers().forEach(player -> handler.exitAfk(player, true));

        if (this.chatEventHandler != null) {
            this.plugin.removeChatHandler(this.chatEventHandler);
            this.chatEventHandler = null;
        }

        if (this.luckPermsHook != null) {
            this.luckPermsHook.unregister();
            this.luckPermsHook = null;
        }

        handler.shutdown();
    }

    @Override
    public void registerPlaceholders(final PlaceholderRegistry registry) {
        AfkPlaceholders.registerPlaceholders(this, registry);
    }

    public boolean openAdminGui(final Player player) {
        return this.adminMenu != null && this.adminMenu.open(player);
    }

    public AfkSettings getSettings() {
        return this.settings;
    }

    public AfkHandler handler() {
        return handler;
    }

    @Override
    public boolean isAfk(final Player player) {
        return handler.isAfk(player);
    }

}