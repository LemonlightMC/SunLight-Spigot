package su.nightexpress.sunlight.moduleImpl.afk.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.sunlight.hook.HookId;
import su.nightexpress.sunlight.moduleImpl.afk.AfkModule;
import su.nightexpress.sunlight.utils.Utils;

/**
 * Exposes the player's AFK state as LuckPerms context {@code afkstate=true|false}.
 * <p>
 * LuckPerms is compile-only; every call is guarded so the feature is inactive when LuckPerms is absent.
 */
public class AfkContextHook {

    public static final String CONTEXT_KEY = "afkstate";

    private final AfkModule module;
    private ContextCalculator<Player> calculator;

    public AfkContextHook(@NotNull AfkModule module) {
        this.module = module;
    }

    public boolean isAvailable() {
        return this.api() != null;
    }

    public synchronized void register() {
        if (this.calculator != null) return;
        LuckPerms api = this.api();
        if (api == null) return;
        try {
            ContextCalculator<Player> calculator = ContextCalculator.forSingleContext(CONTEXT_KEY, player -> {
                try {
                    return this.module.isAfk(player) ? "true" : "false";
                }
                catch (Exception exception) {
                    return "false";
                }
            });
            api.getContextManager().registerCalculator(calculator);
            this.calculator = calculator;
        }
        catch (Throwable throwable) {
            this.calculator = null;
        }
    }

    public synchronized void unregister() {
        if (this.calculator == null) return;
        try {
            LuckPerms api = this.api();
            if (api != null) api.getContextManager().unregisterCalculator(this.calculator);
        }
        catch (Throwable ignored) {
        }
        this.calculator = null;
    }

    public void signalUpdate(@NotNull Player player) {
        if (this.calculator == null) return;
        try {
            LuckPerms api = this.api();
            if (api != null) api.getContextManager().signalContextUpdate(player);
        }
        catch (Throwable ignored) {
        }
    }

    private LuckPerms api() {
        if (!Utils.isLoaded(HookId.LUCKPERMS)) return null;
        try {
            return LuckPermsProvider.get();
        }
        catch (Throwable throwable) {
            return null;
        }
    }
}
