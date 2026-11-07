package su.nightexpress.sunlight.moduleImpl.essential;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.api.provider.EssentialProvider;
import su.nightexpress.sunlight.api.provider.mode.ModifyMode;
import su.nightexpress.sunlight.api.provider.mode.ToggleMode;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.essential.command.*;
import su.nightexpress.sunlight.moduleImpl.essential.listener.InvulnerabilityListener;
import su.nightexpress.sunlight.teleport.TeleportManager;
import su.nightexpress.sunlight.user.property.UserProperty;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;

public class EssentialModule extends Module implements EssentialProvider {

    public static final UserProperty<Boolean> GOD = UserProperty.create("god", Boolean.class, false, true);

    private final TeleportManager teleportManager;
    private final EssentialSettings settings;

    public EssentialModule(final ModuleDefinition<EssentialModule> definition, final SunLightPlugin plugin) {
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
    protected void loadModule(final FileConfig config) {
        this.plugin.injectLang(EssentialLang.class);
        this.settings.load(config);

        if (this.settings.isInvulnerabilityEnabled()) {
            this.commandRegistry.addProvider(new InvulnerabilityCommandProvider(this));
        }

        this.commandRegistry.addProvider(new AggroCommandProvider(this));
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
        this.commandRegistry.addProvider(new LookTpCommandProvider(this));
        this.commandRegistry.addProvider(new RenameCommandProvider(this));
        this.commandRegistry.addProvider(new SpeedCommandProvider(this));
        this.commandRegistry.addProvider(new TeleportCommandsProvider(this));
        this.commandRegistry.addProvider(new TimeCommandProvider(this));

        if (this.settings.isGodEnabled()) {
            UserPropertyRegistry.register(GOD);
        }
        if (this.settings.isInvulnerabilityEnabled() || this.settings.isGodEnabled()) {
            this.addListener(new InvulnerabilityListener(this.plugin, this, this.settings));
        }
    }

    @Override
    protected void unloadModule() {

    }

    @Override
    public void registerPlaceholders(final PlaceholderRegistry registry) {
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

    @Override
    public boolean isGodEnabled() {
        return this.settings.isGodEnabled();
    }

    @Override
    public boolean isInvulnerabilityEnabled() {
        return this.settings.isInvulnerabilityEnabled();
    }

    @Override
    public boolean isGod(final Player player) {
        if (!this.settings.isGodEnabled()) {
            return false;
        }
        return this.userManager.getOrFetch(player).getPropertyOrDefault(GOD);
    }

    @Override
    public void setGod(final Player player, final boolean god) {
        if (!this.settings.isGodEnabled())
            return;

        this.userManager.getOrFetch(player).setProperty(GOD, god);
    }

    private static final int MAX_VALUE = 20;

    @Override
    public boolean setInvulnerable(final Player player, final boolean invulnerable) {
        player.setInvulnerable(invulnerable);
        return invulnerable;
    }

    @Override
    public int setAggro(final Player player, final double radius) {
        if (player == null || player.getWorld() == null) {
            return 0;
        }
        int count = 0;
        for (final org.bukkit.entity.Entity entity : player.getNearbyEntities(32D, 32D, 32D)) {
            if (entity != player && entity instanceof final Mob mob) {
                mob.setTarget(player);
                count++;
            }
        }
        return count;
    }

    @Override
    public int setAggro(final Player player, final double radius, int duration) {
        if (player == null || player.getWorld() == null || duration <= 0) {
            return 0;
        }
        int count = 0;
        for (final org.bukkit.entity.Entity entity : player.getNearbyEntities(32D, 32D, 32D)) {
            if (entity != player && entity instanceof final Mob mob) {
                mob.setTarget(player);
                count++;
            }
        }
        plugin.scheduler().runTaskLater(() -> {
            clearAggro(player, radius);
        }, duration);
        return count;
    }

    @Override
    public int setAggro(final Location center, final double radius, final Player target) {
        if (center == null || center.getWorld() == null || target == null) {
            return 0;
        }
        int count = 0;
        for (final LivingEntity entity : center.getWorld().getNearbyLivingEntities(center, radius, radius, radius)) {
            if (entity instanceof final Mob mob && entity != target) {
                mob.setTarget(target);
                count++;
            }
        }
        return count;
    }

    @Override
    public int clearAggro(final Location center, final double radius) {
        if (center == null || center.getWorld() == null) {
            return 0;
        }
        int count = 0;
        for (final LivingEntity entity : center.getWorld().getNearbyLivingEntities(center, radius, radius, radius)) {
            if (entity instanceof final Mob mob) {
                mob.setTarget(null);
                count++;
            }
        }
        return count;
    }

    @Override
    public int clearAggro(final Player player, final double radius) {
        if (player == null || player.getWorld() == null) {
            return 0;
        }
        int count = 0;
        for (final org.bukkit.entity.Entity entity : player.getNearbyEntities(32D, 32D, 32D)) {
            if (entity instanceof final Mob mob && player.equals(mob.getTarget())) {
                mob.setTarget(null);
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean setGamemode(final Player player, final GameMode mode) {
        if (player.isOnline()) {
            player.setGameMode(mode);
        } else {
            final su.nightexpress.sunlight.nms.SunNMS internals = this.plugin().getInternals();
            if (internals == null) {
                this.plugin().getLogger()
                        .warning(su.nightexpress.sunlight.config.Lang.ERROR_NO_INTERNALS_HANDLER.value().getText());
            }
            try {
                internals.setGameMode(player, mode);
            } catch (final Exception e) {
                this.plugin().getLogger()
                        .warning(
                                "Failed to set gamemode for offline player " + player.getName() + " to " + mode.name());
                return false;
            }
        }
        return true;
    }

    @Override
    public GameMode getGamemode(final Player player) {
        if (player.isOnline()) {
            return player.getGameMode();
        } else {
            final su.nightexpress.sunlight.nms.SunNMS internals = this.plugin().getInternals();
            if (internals == null) {
                this.plugin().getLogger()
                        .warning(su.nightexpress.sunlight.config.Lang.ERROR_NO_INTERNALS_HANDLER.value().getText());
                return null;
            }
            try {
                return internals.getGameMode(player);
            } catch (final Exception e) {
                this.plugin().getLogger()
                        .warning("Failed to get gamemode for offline player " + player.getName());
                return null;
            }
        }
    }

    @Override
    public int setFoodLevel(final Player player, int amount) {
        amount = (int) Math.clamp(amount, 0, MAX_VALUE);
        player.setFoodLevel(amount);
        return amount;
    }

    @Override
    public int setFoodLevel(final Player player, int amount, ModifyMode mode) {
        if (mode.equals(ModifyMode.RESET)) {
            player.setFoodLevel(MAX_VALUE);
            return MAX_VALUE;
        }
        amount = (int) Math.clamp(mode.modify(getFoodLevel(player), amount), 0, MAX_VALUE);
        player.setFoodLevel(amount);
        return amount;
    }

    @Override
    public int getFoodLevel(final Player player) {
        return player.getFoodLevel();
    }

    @Override
    public int resetFoodLevel(final Player player) {
        return setFoodLevel(player, MAX_VALUE);
    }

    @Override
    public float setSaturation(final Player player, float amount) {
        amount = (int) Math.clamp(amount, 0, MAX_VALUE);
        player.setSaturation(amount);
        return amount;
    }

    @Override
    public float getSaturation(final Player player) {
        return player.getSaturation();
    }

    @Override
    public float resetSaturation(final Player player) {
        return setSaturation(player, MAX_VALUE);
    }

    @Override
    public float setExhaustion(final Player player, float amount) {
        amount = (int) Math.clamp(amount, 0, MAX_VALUE);
        player.setExhaustion(amount);
        return amount;
    }

    @Override
    public float getExhaustion(final Player player) {
        return player.getExhaustion();
    }

    @Override
    public float resetExhaustion(final Player player) {
        return setExhaustion(player, MAX_VALUE);
    }

    @Override
    public boolean isFlying(final Player player) {
        return player.isFlying();
    }

    @Override
    public boolean setFlying(final Player player, final boolean flying) {
        player.setFlying(flying);
        return flying;
    }

    @Override
    public boolean isAllowFlight(final Player player) {
        return player.getAllowFlight();
    }

    @Override
    public boolean setAllowFlight(final Player player, final boolean allow) {
        player.setAllowFlight(allow);
        return allow;
    }

    @Override
    public boolean setAllowFlight(final Player player, final ToggleMode mode) {
        final boolean state = mode.apply(isAllowFlight(player));
        player.setAllowFlight(state);
        return state;
    }

    @Override
    public float getFlySpeed(final Player player) {
        return player.getFlySpeed();
    }

    @Override
    public float setFlySpeed(final Player player, float speed) {
        speed = Math.clamp(speed, -1.0f, 1.0f);
        player.setFlySpeed(speed);
        return speed;
    }

    @Override
    public float setFlySpeed(final Player player, float speed, ModifyMode mode) {
        speed = (float) Math.clamp(mode.modify(getFlySpeed(player), speed), -1.0f, 1.0f);
        player.setFlySpeed(speed);
        return speed;
    }

    @Override
    public float resetFlySpeed(final Player player) {
        return setFlySpeed(player, 0.1f);
    }

    @Override
    public float getWalkSpeed(final Player player) {
        return player.getWalkSpeed();
    }

    @Override
    public float setWalkSpeed(final Player player, float speed) {
        speed = Math.clamp(speed, -1.0f, 1.0f);
        player.setWalkSpeed(speed);
        return speed;
    }

    @Override
    public float setWalkSpeed(final Player player, float speed, ModifyMode mode) {
        speed = (float) Math.clamp(mode.modify(getWalkSpeed(player), speed), -1.0f, 1.0f);
        player.setWalkSpeed(speed);
        return speed;
    }

    @Override
    public float resetWalkSpeed(final Player player) {
        return setWalkSpeed(player, 0.2f);
    }

    @Override
    public float getJumpStrength(final Player player) {
        AttributeInstance instance = player.getAttribute(Attribute.JUMP_STRENGTH);
        return instance == null ? 0.42F : (float) instance.getBaseValue();
    }

    @Override
    public float setJumpStrength(final Player player, float strength) {
        strength = Math.clamp(strength, 0.0F, 2.0F);
        AttributeInstance instance = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (instance != null) {
            instance.setBaseValue(strength);
        }
        return strength;
    }

    @Override
    public float setJumpStrength(final Player player, float strength, ModifyMode mode) {
        strength = (float) Math.clamp(mode.modify(getJumpStrength(player), strength), 0.0F, 2.0F);
        AttributeInstance instance = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (instance != null) {
            instance.setBaseValue(strength);
        }
        return strength;
    }

    @Override
    public float resetJumpStrength(final Player player) {
        return setJumpStrength(player, 0.42F);
    }

    @Override
    public double getHealth(final Player player) {
        return player.getHealth();
    }

    @Override
    public double getMaxHealth(final Player player) {
        AttributeInstance instance = player.getAttribute(Attribute.MAX_HEALTH);
        return instance == null ? MAX_VALUE : instance.getValue();
    }

    @Override
    public double setMaxHealth(final Player player, final double amount) {
        AttributeInstance instance = player.getAttribute(Attribute.MAX_HEALTH);
        if (instance == null) {
            return MAX_VALUE;
        }
        double newMaxHealth = Math.clamp(amount, 1D, Double.MAX_VALUE);
        instance.setBaseValue(newMaxHealth);
        if (player.getHealth() > newMaxHealth) {
            player.setHealth(newMaxHealth);
        }
        return newMaxHealth;
    }

    @Override
    public double setMaxHealth(final Player player, final double amount, final ModifyMode mode) {
        AttributeInstance instance = player.getAttribute(Attribute.MAX_HEALTH);
        if (instance == null) {
            return MAX_VALUE;
        }
        double newMaxHealth = Math.clamp(mode.modify(instance.getValue(), amount), 1D, Double.MAX_VALUE);
        instance.setBaseValue(newMaxHealth);
        if (player.getHealth() > newMaxHealth) {
            player.setHealth(newMaxHealth);
        }
        return newMaxHealth;
    }

    @Override
    public double setHealth(final Player player, double amount) {
        amount = Math.clamp(amount, 0D, getMaxHealth(player));
        if (amount < player.getHealth()) {
            player.sendHurtAnimation(0f); // Play hurt animation.
        }
        player.setHealth(amount);
        return amount;
    }

    @Override
    public double setHealth(final Player player, double amount, ModifyMode mode) {
        if (mode == ModifyMode.RESET) {
            amount = getMaxHealth(player);
        } else {
            amount = Math.clamp(mode.modify(getHealth(player), amount), 0D, getMaxHealth(player));
            if (amount < player.getHealth()) {
                player.sendHurtAnimation(0f); // Play hurt animation.
            }
        }
        player.setHealth(amount);
        return amount;
    }

    @Override
    public double addHealth(final Player player, double amount) {
        return setHealth(player, getHealth(player), ModifyMode.ADD);
    }

    @Override
    public double removeHealth(final Player player, double amount) {
        return setHealth(player, getHealth(player), ModifyMode.REMOVE);
    }

    @Override
    public double resetHealth(final Player player) {
        return setHealth(player, getHealth(player), ModifyMode.RESET);
    }

    @Override
    public int getLevel(final Player player) {
        if (player == null) {
            return 0;
        }
        return player.getLevel();
    }

    @Override
    public void setLevel(final Player player, final int level) {
        if (player == null) {
            return;
        }
        player.setLevel(Math.max(level, 0));
    }

    @Override
    public void addLevel(final Player player, final int level) {
        if (player == null) {
            return;
        }
        player.setLevel(Math.max(getLevel(player) + level, 0));
    }

    @Override
    public void removeLevel(final Player player, final int level) {
        if (player == null) {
            return;
        }
        player.setLevel(Math.max(getLevel(player) - level, 0));
    }

    @Override
    public void resetLevel(final Player player) {
        if (player == null) {
            return;
        }
        player.setLevel(0);
    }

    @Override
    public float getExp(final Player player) {
        if (player == null) {
            return 0;
        }
        return player.getExp();
    }

    @Override
    public void setExp(final Player player, final float percentage) {
        if (player == null) {
            return;
        }
        player.setExp(Math.clamp(percentage, 0, 1));
    }

    @Override
    public void giveExp(final Player player, final int amount) {
        if (player == null) {
            return;
        }
        player.giveExp(amount);
    }

    @Override
    public void resetExp(final Player player) {
        if (player == null) {
            return;
        }
        player.setExp(0);
    }

    @Override
    public int getTotalExperience(final Player player) {
        if (player == null) {
            return 0;
        }
        return player.getTotalExperience();
    }

    @Override
    public void setTotalExperience(final Player player, final int amount) {
        if (player == null) {
            return;
        }
        player.setTotalExperience(amount);
    }
}