package su.nightexpress.sunlight.api.provider;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public interface EssentialProvider {

    /** Whether the module's god mode feature is switched on at all. */
    boolean isGodEnabled();

    /** Whether the module's invulnerability feature is switched on at all. */
    boolean isInvulnerabilityEnabled();

    /**
     * Whether the player currently has god mode. Always {@code false} when the
     * feature is off.
     */
    boolean isGod(Player player);

    /**
     * Turns god mode on or off for a player, persisting the choice.
     * <p>
     * The actual invulnerability is applied by the module's own listener, on the
     * next tick — not
     * synchronously by this call. {@link Player#isInvulnerable()} therefore lags
     * this by a tick.
     */
    void setGod(Player player, boolean god);

    /** Flips the player's god mode, returning the new state. */
    default boolean toggleGod(Player player) {
        final boolean state = !this.isGod(player);
        this.setGod(player, state);
        return state;
    }

    /**
     * Whether the player is currently invulnerable, through this plugin or any
     * other.
     */
    default boolean isInvulnerable(Player player) {
        return player.isInvulnerable();
    }

    boolean setInvulnerable(Player player, boolean invulnerable);

    default boolean toggleInvulnerable(Player player) {
        return this.setInvulnerable(player, !this.isInvulnerable(player));
    }

    // Gamemode

    boolean setGamemode(Player player, GameMode mode);

    GameMode getGamemode(Player player);

    // Food

    int setFoodLevel(Player player, int amount);

    int getFoodLevel(Player player);

    int resetFoodLevel(Player player);

    // Saturation

    float setSaturation(Player player, float amount);

    float getSaturation(Player player);

    float resetSaturation(Player player);

    // Exhaustion

    float setExhaustion(Player player, float amount);

    float getExhaustion(Player player);

    float resetExhaustion(Player player);

    // Flying

    boolean isFlying(Player player);

    boolean setFlying(Player player, boolean flying);

    default boolean toggleFlying(Player player) {
        return this.setFlying(player, !this.isFlying(player));
    }

    // Allow flight

    boolean isAllowFlight(Player player);

    boolean setAllowFlight(Player player, boolean allow);

    default boolean toggleAllowFlight(Player player) {
        return this.setAllowFlight(player, !this.isAllowFlight(player));
    }

    // Fly speed

    float getFlySpeed(Player player);

    float setFlySpeed(Player player, float speed);

    float resetFlySpeed(Player player);

    // Walk speed

    float getWalkSpeed(Player player);

    float setWalkSpeed(Player player, float speed);

    float resetWalkSpeed(Player player);

    // Health

    double getHealth(Player player);

    double getMaxHealth(Player player);

    double setHealth(Player player, double amount);

    double resetHealth(Player player);

    // Experience

    int getLevel(Player player);

    void setLevel(Player player, int level);

    int getTotalExperience(Player player);

    void setTotalExperience(Player player, int amount);
}
