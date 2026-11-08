package su.nightexpress.sunlight.moduleImpl.afk.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.MenuViewer;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.afk.AfkModule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class AfkAdminMenu extends AbstractMenu {

    private static final int PAGE_SIZE = 36;
    private static final int SLOT_CLOSE = 49;
    private static final int SLOT_REFRESH = 48;

    private final SunLightPlugin plugin;
    private final AfkModule module;

    public AfkAdminMenu(final SunLightPlugin plugin, final AfkModule module) {
        super(MenuType.GENERIC_9X6, ORANGE.wrap("AFK Players"));
        this.plugin = plugin;
        this.module = module;
    }

    public boolean open(final Player player) {
        return this.show(this.plugin, player);
    }

    @Override
    public void registerActions() {
    }

    @Override
    public void registerConditions() {
    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray());
        this.addNextPageButton(53);
        this.addPreviousPageButton(45);
    }

    @Override
    protected void onLoad(final FileConfig config) {
    }

    @Override
    protected void onClick(final ViewerContext context, final InventoryClickEvent event) {
    }

    @Override
    protected void onDrag(final ViewerContext context, final InventoryDragEvent event) {
    }

    @Override
    protected void onClose(final ViewerContext context, final InventoryCloseEvent event) {
    }

    @Override
    public void onPrepare(final ViewerContext context, final InventoryView view, final Inventory inventory,
            final List<MenuItem> list) {
        final Player viewerPlayer = context.getPlayer();
        final MenuViewer viewer = context.getViewer();
        final List<Player> players = this.module.handler().getAfkPlayers();

        viewer.setTotalPages(Math.max(1, (players.size() + PAGE_SIZE - 1) / PAGE_SIZE));
        final int from = (viewer.getCurrentPage() - 1) * PAGE_SIZE;

        if (players.isEmpty()) {
            final NightItem empty = NightItem.fromType(Material.BARRIER)
                    .setDisplayName(SOFT_RED.wrap("No AFK players"))
                    .setLore(List.of(GRAY.wrap("There are currently no players in AFK mode.")));
            list.add(MenuItem.button().defaultState(empty, c -> this.show(this.plugin, c.getPlayer())).slots(22)
                    .build());
        } else if (from < players.size()) {
            final int to = Math.min(players.size(), from + PAGE_SIZE);
            for (int i = from; i < to; i++) {
                final Player target = players.get(i);
                final List<String> lore = new ArrayList<>();
                lore.add(GRAY.wrap("AFK for: ") + ORANGE.wrap(TimeFormats
                        .formatSince(this.module.handler().getAfkEnterTimestamp(target), TimeFormatType.LITERAL)));
                lore.add(GRAY.wrap("Idle: ")
                        + WHITE.wrap(TimeFormats.formatAmount(TimeUnit.MILLISECONDS.convert(this.module
                                .handler().getIdleTime(target), TimeUnit.SECONDS), TimeFormatType.LITERAL)));
                lore.add(GRAY.wrap("Total AFK: ") + WHITE.wrap(TimeFormats
                        .formatAmount(this.module.handler().getTotalAfkTime(target), TimeFormatType.LITERAL)));
                lore.add("");
                lore.add(GREEN.wrap("Left-click: ") + GRAY.wrap("Wake player"));
                lore.add(SOFT_RED.wrap("Right-click: ") + GRAY.wrap("Kick for idling"));
                lore.add(GOLD.wrap("Shift-click: ") + GRAY.wrap("Teleport to player"));

                final NightItem icon = NightItem.fromType(Material.CLOCK)
                        .setDisplayName(ORANGE.wrap(target.getName()))
                        .setLore(lore);

                final int slot = i - from;
                list.add(MenuItem.button().defaultState(icon, action -> {
                    final Player admin = action.getPlayer();
                    if (!target.isOnline()) {
                        this.show(this.plugin, admin);
                        return;
                    }
                    if (action.getEvent() instanceof InventoryClickEvent click) {
                        if (click.isShiftClick()) {
                            admin.teleport(target.getLocation());
                        } else if (click.isRightClick()) {
                            this.module.handler().kickForIdling(target);
                        } else {
                            this.module.handler().exitAfk(target, false);
                        }
                    } else {
                        this.module.handler().exitAfk(target, false);
                    }
                    this.plugin.runTask(() -> this.show(this.plugin, admin));
                }).slots(slot).build());
            }
        }

        final NightItem refresh = NightItem.fromType(Material.LIME_DYE).setDisplayName(GREEN.wrap("Refresh"))
                .setLore(List.of(GRAY.wrap("Click to refresh the AFK list.")));
        list.add(MenuItem.button().defaultState(refresh, action -> this.show(this.plugin, action.getPlayer()))
                .slots(SLOT_REFRESH).build());

        final NightItem close = NightItem.fromType(Material.BARRIER).setDisplayName(GRAY.wrap("Close"));
        list.add(MenuItem.button().defaultState(close, action -> action.getViewer().closeMenu()).slots(SLOT_CLOSE)
                .build());
    }

    @Override
    public void onReady(final ViewerContext context, final InventoryView view, final Inventory inventory) {
    }

    @Override
    public void onRender(final ViewerContext context, final InventoryView view, final Inventory inventory) {
    }
}
