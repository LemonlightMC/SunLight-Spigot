package su.nightexpress.sunlight.moduleImpl.afk.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;

import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.afk.ActivityType;
import su.nightexpress.sunlight.moduleImpl.afk.AfkModule;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkLang;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkSettings;

public class AfkListener extends AbstractListener<SunLightPlugin> {

    private final AfkModule module;
    private final AfkSettings settings;

    public AfkListener(final SunLightPlugin plugin, final AfkModule module) {
        super(plugin);
        this.module = module;
        this.settings = module.getSettings();
    }

    private boolean isAfk(final Player player) {
        return this.module.isAfk(player);
    }

    private void notifyBlocked(final Player player) {
        this.module.sendPrefixed(AfkLang.ERROR_ACTION_BLOCKED, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (this.module.handler().isWithinRejoinKickWindow(player)) {
            Players.kick(player, this.module.getPrefixed(AfkLang.KICK_REJOIN).getText());
            return;
        }

        this.module.handler().track(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        this.module.handler().untrack(event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onIdleDeath(final PlayerDeathEvent event) {
        this.module.handler().exitAfk(event.getEntity(), true); // Force exit AFK mode on death.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIdleTeleport(final PlayerTeleportEvent event) {
        this.module.handler().exitAfk(event.getPlayer(), false); // Force exit AFK mode on teleportations.
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedMove(final PlayerMoveEvent event) {
        if (!this.settings.blockMovement.get())
            return;
        if (!event.hasChangedBlock())
            return;
        if (!this.isAfk(event.getPlayer()))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedBreak(final BlockBreakEvent event) {
        if (!this.settings.blockBlockBreak.get())
            return;
        if (!this.isAfk(event.getPlayer()))
            return;

        event.setCancelled(true);
        this.notifyBlocked(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedPlace(final BlockPlaceEvent event) {
        if (!this.settings.blockBlockPlace.get())
            return;
        if (!this.isAfk(event.getPlayer()))
            return;

        event.setCancelled(true);
        this.notifyBlocked(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedInteract(final PlayerInteractEvent event) {
        if (!this.settings.blockInteract.get())
            return;
        if (!this.isAfk(event.getPlayer()))
            return;

        event.setCancelled(true);
        this.notifyBlocked(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedEntityInteract(final PlayerInteractEntityEvent event) {
        if (!this.settings.blockInteract.get())
            return;
        if (!this.isAfk(event.getPlayer()))
            return;

        event.setCancelled(true);
        this.notifyBlocked(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedInventoryClick(final InventoryClickEvent event) {
        if (!this.settings.blockInventory.get())
            return;
        if (!(event.getWhoClicked() instanceof final Player player))
            return;
        if (!this.isAfk(player))
            return;

        event.setCancelled(true);
        this.notifyBlocked(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedInventoryDrag(final InventoryDragEvent event) {
        if (!this.settings.blockInventory.get())
            return;
        if (!(event.getWhoClicked() instanceof final Player player))
            return;
        if (!this.isAfk(player))
            return;

        event.setCancelled(true);
        this.notifyBlocked(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedDrop(final PlayerDropItemEvent event) {
        if (!this.settings.blockItemDrop.get())
            return;
        if (!this.isAfk(event.getPlayer()))
            return;

        event.setCancelled(true);
        this.notifyBlocked(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedPickup(final EntityPickupItemEvent event) {
        if (!this.settings.blockItemPickup.get())
            return;
        if (!(event.getEntity() instanceof final Player player))
            return;
        if (!this.isAfk(player))
            return;

        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedCommand(final PlayerCommandPreprocessEvent event) {
        if (!this.settings.blockCommands.get())
            return;
        if (!this.isAfk(event.getPlayer()))
            return;
        if (this.settings.isCommandIgnored(event.getMessage()))
            return;

        event.setCancelled(true);
        this.notifyBlocked(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedDamageIncoming(final EntityDamageEvent event) {
        if (!this.settings.blockDamageIncoming.get())
            return;
        if (!(event.getEntity() instanceof final Player player))
            return;
        if (!this.isAfk(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedDamageOutgoing(final EntityDamageByEntityEvent event) {
        if (!this.settings.blockDamageOutgoing.get())
            return;
        if (!(event.getDamager() instanceof final Player player))
            return;
        if (!this.isAfk(player))
            return;

        event.setCancelled(true);
        this.notifyBlocked(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockedMobTargeting(final EntityTargetLivingEntityEvent event) {
        if (!this.settings.blockMobTargeting.get())
            return;
        if (!(event.getTarget() instanceof final Player player))
            return;
        if (!this.isAfk(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        this.module.handler().trackActivity(event.getPlayer(), ActivityType.INTERACT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityCommand(final PlayerCommandPreprocessEvent event) {
        if (this.settings.isCommandIgnored(event.getMessage()))
            return;

        this.module.handler().trackActivity(event.getPlayer(), ActivityType.COMMAND);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityBreak(final BlockBreakEvent event) {
        this.module.handler().trackActivity(event.getPlayer(), ActivityType.BLOCK_BREAK);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityPlace(final BlockPlaceEvent event) {
        this.module.handler().trackActivity(event.getPlayer(), ActivityType.BLOCK_PLACE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityPickup(final EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof final Player player))
            return;

        this.module.handler().trackActivity(player, ActivityType.ITEM_PICKUP);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityContainer(final InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof final Player player))
            return;

        this.module.handler().trackActivity(player, ActivityType.CONTAINER);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityFish(final PlayerFishEvent event) {
        switch (event.getState()) {
            case CAUGHT_FISH, CAUGHT_ENTITY ->
                this.module.handler().trackActivity(event.getPlayer(), ActivityType.FISH);
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActivityVehicle(final VehicleEnterEvent event) {
        if (event.getEntered() instanceof final Player player) {
            this.module.handler().trackActivity(player, ActivityType.VEHICLE);
        }
    }
}