package su.nightexpress.sunlight.moduleImpl.essential;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.api.provider.EssentialProvider;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.essential.command.*;
import su.nightexpress.sunlight.moduleImpl.essential.listener.GodListener;
import su.nightexpress.sunlight.moduleImpl.essential.listener.InvulnerabilityListener;
import su.nightexpress.sunlight.teleport.TeleportManager;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.sunlight.user.property.UserProperty;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;

public class EssentialModule extends Module implements EssentialProvider {

    public static final UserProperty<Boolean> GOD = UserProperty.create("god", Boolean.class, false, true);

    private final TeleportManager teleportManager;
    private final EssentialSettings settings;

    public EssentialModule(ModuleDefinition<EssentialModule> definition, SunLightPlugin plugin) {
        super(definition, plugin);
        this.teleportManager = plugin.teleportManager();
        this.settings = new EssentialSettings();
    }

    public EssentialSettings settings() {
        return this.settings;
    }

    public TeleportManager teleportManager() {
        return this.teleportManager;
    }

    @Override
    protected void loadModule(FileConfig config) {
        this.plugin.injectLang(EssentialLang.class);
        this.settings.load(config);

        if (this.settings.isInvulnerabilityEnabled()) {
            this.commandRegistry.addProvider(new InvulnerabilityCommandProvider(this));
        }

        this.commandRegistry.addProvider(new DimensionCommandProvider(this));
        this.commandRegistry.addProvider(new EnchantCommandsProvider(this));
        this.commandRegistry.addProvider(new ExperienceCommandsProvider(this));
        this.commandRegistry.addProvider(new FlyCommandProvider(this));
        this.commandRegistry.addProvider(new FlySpeedCommandProvider(this));
        this.commandRegistry.addProvider(new FoodLevelCommandProvider(this));
        this.commandRegistry.addProvider(new GamemodeCommandProvider(this));
        if (this.settings.isGodEnabled()) {
            this.commandRegistry.addProvider(new GodCommandProvider(this));
        }
        this.commandRegistry.addProvider(new HealthCommandProvider(this));
        this.commandRegistry.addProvider(new NearCommandProvider(this));
        this.commandRegistry.addProvider(new PlayerInfoCommandProvider(this));
        this.commandRegistry.addProvider(new JumpCommandProvider(this));
        this.commandRegistry.addProvider(new SpeedCommandProvider(this));
        this.commandRegistry.addProvider(new TeleportCommandsProvider(this));
        this.commandRegistry.addProvider(new TimeCommandProvider(this));

        if (this.settings.isGodEnabled()) {
            UserPropertyRegistry.register(GOD);
            this.addListener(new GodListener(this.plugin, this));
        }
        if (this.settings.isInvulnerabilityEnabled()) {
            this.addListener(new InvulnerabilityListener(this.plugin, this, this.settings));
        }
    }

    @Override
    protected void unloadModule() {

    }

    @Override
    public boolean isGodEnabled() {
        return this.settings.isGodEnabled();
    }

    @Override
    public boolean isInvulnerabilityEnabled() {
        return this.settings.isInvulnerabilityEnabled();
    }

    @Override
    public boolean isGod(@NotNull Player player) {
        if (!this.settings.isGodEnabled())
            return false;

        return this.userManager.getOrFetch(player).getPropertyOrDefault(GOD);
    }

    @Override
    public void setGod(@NotNull Player player, boolean god) {
        if (!this.settings.isGodEnabled())
            return;

        this.userManager.getOrFetch(player).setProperty(GOD, god);
    }

    private static final int MAX_VALUE = 20;

    @Override
    public boolean setInvulnerable(@NotNull Player player, boolean invulnerable) {
        player.setInvulnerable(invulnerable);
        return invulnerable;
    }

    @Override
    public boolean setGamemode(@NotNull Player player, GameMode mode) {
        if (player.isOnline()) {
            player.setGameMode(mode);
        } else {
            final su.nightexpress.sunlight.nms.SunNMS internals = this.plugin().getInternals();
            if (internals == null) {
                this.plugin().getLogger().warning(su.nightexpress.sunlight.config.Lang.ERROR_NO_INTERNALS_HANDLER.value().getText());
            }
            try {
                internals.setGameMode(player, mode);
            } catch (Exception e) {
                this.plugin().getLogger()
                        .warning("Failed to set gamemode for offline player " + player.getName() + " to " + mode.name());
                return false;
            }
        }
        return true;
    }

    @Override
    public GameMode getGamemode(@NotNull Player player) {
        if (player.isOnline()) {
            return player.getGameMode();
        } else {
            final su.nightexpress.sunlight.nms.SunNMS internals = this.plugin().getInternals();
            if (internals == null) {
                this.plugin().getLogger().warning(su.nightexpress.sunlight.config.Lang.ERROR_NO_INTERNALS_HANDLER.value().getText());
                return null;
            }
            try {
                return internals.getGameMode();
            } catch (Exception e) {
                this.plugin().getLogger()
                        .warning("Failed to get gamemode for offline player " + player.getName());
                return null;
            }
        }
    }

    @Override
    public int setFoodLevel(@NotNull Player player, int amount) {
        amount = (int) Math.clamp(amount, 0, MAX_VALUE);
        player.setFoodLevel(amount);
        return amount;
    }

    @Override
    public int getFoodLevel(@NotNull Player player) {
        return player.getFoodLevel();
    }

    @Override
    public int resetFoodLevel(@NotNull Player player) {
        return setFoodLevel(player, MAX_VALUE);
    }

    @Override
    public float setSaturation(@NotNull Player player, float amount) {
        amount = (int) Math.clamp(amount, 0, MAX_VALUE);
        player.setSaturation(amount);
        return amount;
    }

    @Override
    public float getSaturation(@NotNull Player player) {
        return player.getSaturation();
    }

    @Override
    public float resetSaturation(@NotNull Player player) {
        return setSaturation(player, MAX_VALUE);
    }

    @Override
    public float setExhaustion(@NotNull Player player, float amount) {
        amount = (int) Math.clamp(amount, 0, MAX_VALUE);
        player.setExhaustion(amount);
        return amount;
    }

    @Override
    public float getExhaustion(@NotNull Player player) {
        return player.getExhaustion();
    }

    @Override
    public float resetExhaustion(@NotNull Player player) {
        return setExhaustion(player, MAX_VALUE);
    }

    @Override
    public boolean isFlying(@NotNull Player player) {
        return player.isFlying();
    }

    @Override
    public boolean setFlying(@NotNull Player player, boolean flying) {
        player.setFlying(flying);
        return flying;
    }

    @Override
    public boolean isAllowFlight(@NotNull Player player) {
        return player.getAllowFlight();
    }

    @Override
    public boolean setAllowFlight(@NotNull Player player, boolean allow) {
        player.setAllowFlight(allow);
        return allow;
    }

    @Override
    public float getFlySpeed(@NotNull Player player) {
        return player.getFlySpeed();
    }

    @Override
    public float setFlySpeed(@NotNull Player player, float speed) {
        speed = Math.clamp(speed, -1.0f, 1.0f);
        player.setFlySpeed(speed);
        return speed;
    }

    @Override
    public float resetFlySpeed(@NotNull Player player) {
        return setFlySpeed(player, 0.1f);
    }

    @Override
    public float getWalkSpeed(@NotNull Player player) {
        return player.getWalkSpeed();
    }

    @Override
    public float setWalkSpeed(@NotNull Player player, float speed) {
        speed = Math.clamp(speed, -1.0f, 1.0f);
        player.setWalkSpeed(speed);
        return speed;
    }

    @Override
    public float resetWalkSpeed(@NotNull Player player) {
        return setWalkSpeed(player, 0.2f);
    }

    @Override
    public double getHealth(@NotNull Player player) {
        return player.getHealth();
    }

    @Override
    public double getMaxHealth(@NotNull Player player) {
        return su.nightexpress.nightcore.util.EntityUtil.getAttributeValue(player, Attribute.MAX_HEALTH);
    }

    @Override
    public double setHealth(@NotNull Player player, double amount) {
        final double max = getMaxHealth(player);
        amount = Math.clamp(amount, 0D, max);
        player.setHealth(amount);
        return amount;
    }

    @Override
    public double resetHealth(@NotNull Player player) {
        return setHealth(player, getMaxHealth(player));
    }

    @Override
    public int getLevel(@NotNull Player player) {
        return player.getLevel();
    }

    @Override
    public void setLevel(@NotNull Player player, int level) {
        player.setLevel(level);
    }

    @Override
    public int getTotalExperience(@NotNull Player player) {
        return player.getTotalExperience();
    }

    @Override
    public void setTotalExperience(@NotNull Player player, int amount) {
        player.setTotalExperience(amount);
    }

    @Override
    public void registerPlaceholders(PlaceholderRegistry registry) {
        if (this.settings.isGodEnabled()) {
            registry.register("essential_god_state", (player, payload) -> {
                return CoreLang.STATE_ENABLED_DISALBED
                        .get(this.userManager.getOrFetch(player).getPropertyOrDefault(GOD));
            });

            registry.register("essential_god_bool", (player, payload) -> {
                return String.valueOf(this.userManager.getOrFetch(player).getPropertyOrDefault(GOD));
            });
        }
        if (this.settings.isInvulnerabilityEnabled()) {
            registry.register("essential_invulnerability_state", (player, payload) -> {
                return CoreLang.STATE_ENABLED_DISALBED.get(player.isInvulnerable());
            });

            registry.register("essential_invulnerability_bool", (player, payload) -> {
                return String.valueOf(player.isInvulnerable());
            });
        }
    }
}