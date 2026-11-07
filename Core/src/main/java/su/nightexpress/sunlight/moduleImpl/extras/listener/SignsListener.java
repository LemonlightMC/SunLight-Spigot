package su.nightexpress.sunlight.moduleImpl.extras.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasModule;

public class SignsListener extends AbstractListener<SunLightPlugin> {

    private final ExtrasModule module;

    public SignsListener(final SunLightPlugin plugin, final ExtrasModule module) {
        super(plugin);
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilPrepare(final PrepareAnvilEvent event) {
        final ItemStack result = event.getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }

        if (this.module.signHandler().hasSignatures(result)) {
            if (this.module.settings().SIGN_ANVIL_BLOCK_RENAME.get()) {
                event.setResult(null);
                return;
            }
            if (this.module.settings().SIGN_ANVIL_STRIP_ON_RENAME.get()) {
                this.module.signHandler().removeSignatures(result, signature -> true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftPrepare(final PrepareItemCraftEvent event) {
        boolean isSigned = false;
        for (final ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && !ingredient.getType().isAir()
                    && this.module.signHandler().hasSignatures(ingredient)) {
                        isSigned = true;
                break;
            }
        }

        if (!isSigned) {
            return;
        }

        if (!this.module.settings().SIGN_CRAFTING_ALLOW_SIGNED.get()) {
            event.getInventory().setResult(null);
            return;
        }

        if (!this.module.settings().SIGN_CRAFTING_KEEP_SIGNATURES.get()) {
            final ItemStack result = event.getInventory().getResult();
            if (result != null && !result.getType().isAir()) {
                this.module.signHandler().removeSignatures(result, signature -> true);
                event.getInventory().setResult(result);
            }
        }
    }
}
