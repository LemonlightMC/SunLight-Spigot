package su.nightexpress.sunlight.moduleImpl.extras;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.permission.PermissionBridge;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.extras.command.*;
import su.nightexpress.sunlight.moduleImpl.extras.listener.ColorsListener;
import su.nightexpress.sunlight.moduleImpl.extras.listener.SignsListener;
import su.nightexpress.sunlight.moduleImpl.extras.listener.StarterKitListener;
import su.nightexpress.sunlight.moduleImpl.extras.object.SignHandler;
import su.nightexpress.sunlight.teleport.TeleportManager;
import su.nightexpress.sunlight.user.property.UserProperty;

public class ExtrasModule extends Module {

    public static final UserProperty<Boolean> GOD = UserProperty.create("god", Boolean.class, false, true);

    private final TeleportManager teleportManager;
    private final ExtrasSettings settings;
    private final StarterKitSettings starterKitSettings;
    private final SignHandler signHandler;

    public ExtrasModule(ModuleDefinition<ExtrasModule> definition, SunLightPlugin plugin) {
        super(definition, plugin);
        this.teleportManager = plugin.teleportManager();
        this.settings = new ExtrasSettings();
        this.starterKitSettings = new StarterKitSettings();
        this.signHandler = new SignHandler(this);
    }

    public ExtrasSettings settings() {
        return this.settings;
    }

    public StarterKitSettings getStarterKitSettings() {
        return this.starterKitSettings;
    }

    public TeleportManager teleportManager() {
        return this.teleportManager;
    }

    public SignHandler signHandler() {
        return this.signHandler;
    }

    @Override
    protected void loadModule(FileConfig config) {
        this.settings.load(config);
        this.plugin.injectLang(SignsLang.class);

        this.commandRegistry.addProvider(new SignsCommandsProvider(this));
        this.starterKitSettings.load(config);
        if (PermissionBridge.hasProvider()) {
            this.commandRegistry.addProvider(new StaffCommandProvider(this));
        }

        this.commandRegistry.addProvider(new BroadcastCommandProvider(this));
        this.commandRegistry.addProvider(new CondenseCommandProvider(this));
        this.commandRegistry.addProvider(new DisposalCommandProvider(this));
        this.commandRegistry.addProvider(new HatCommandProvider(this));
        this.commandRegistry.addProvider(new SudoCommandProvider(this));
        this.commandRegistry.addProvider(new SkullCommandProvider(this));
        this.commandRegistry.addProvider(new SmiteCommandProvider(this));
        this.commandRegistry.addProvider(new SuicideCommandProvider(this));

        if (this.settings.isSignColorsEnabled() || this.settings.isAnvilColorsEnabled()) {
            this.addListener(new ColorsListener(this.plugin, this));
        }

        this.addListener(new SignsListener(this.plugin, this));

        if (this.starterKitSettings.isEnabled()) {
            this.addListener(new StarterKitListener(this.plugin, this));
        }
    }

    @Override
    protected void unloadModule() {

    }

    @Override
    public void registerPlaceholders(PlaceholderRegistry registry) {
    }
}