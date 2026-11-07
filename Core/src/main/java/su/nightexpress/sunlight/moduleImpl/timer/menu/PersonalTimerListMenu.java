package su.nightexpress.sunlight.moduleImpl.timer.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.ui.inventory.item.ItemPopulator;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.dialog.TimerDialogKeys;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerScope;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerFormat;

import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class PersonalTimerListMenu extends AbstractMenu implements LangContainer {

    private final SunLightPlugin plugin;
    private final TimerModule module;
    private ItemPopulator<TimerInstance> populator;

    public PersonalTimerListMenu(SunLightPlugin plugin, TimerModule module) {
        super(MenuType.GENERIC_9X6, "Personal Timers");
        this.plugin = plugin;
        this.module = module;
        this.populator = ItemPopulator.builder(TimerInstance.class)
                .actionProvider(timer -> context -> {
                    this.module.openPersonalEditor(context.getPlayer(), timer);
                    context.getViewer().refresh();
                })
                .itemProvider((context, timer) -> NightItem.fromType(Material.CLOCK)
                        .setDisplayName(GOLD.and(BOLD).wrap(timer.getDefinition().getName()) + GRAY.wrap(" [" + timer.getId() + "]"))
                        .setLore(List.of(
                                GRAY.wrap("Kind: " + WHITE.wrap(timer.getDefinition().getKind().name())),
                                GRAY.wrap("State: " + WHITE.wrap(timer.getState().name())),
                                GRAY.wrap("Time: " + WHITE.wrap(TimerFormat.format(timer))),
                                "",
                                GREEN.wrap("Click: edit/manage")
                        )).hideAllComponents())
                .slots(IntStream.range(0, 45).toArray())
                .build();
        plugin.injectLang(this);
    }

    @Override public void registerActions() {}
    @Override public void registerConditions() {}
    @Override public void defineDefaultLayout() {
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray());
        this.addNextPageButton(53);
        this.addPreviousPageButton(45);
        this.addDefaultButton("create", MenuItem.button().defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.ANVIL).setDisplayName(GREEN.wrap("Create Personal Timer")).hideAllComponents())
                .action(context -> this.plugin.showDialog(context.getPlayer(), TimerDialogKeys.PERSONAL_CREATE, TimerScope.PERSONAL, () -> context.getViewer().refresh()))
                .build()).slots(49).build());
        this.addDefaultButton("global", MenuItem.button().defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.COMPASS).setDisplayName(YELLOW.wrap("Global Timers")).hideAllComponents())
                .action(context -> this.module.openTimerList(context.getPlayer()))
                .build()).slots(45).build());
    }
    @Override protected void onLoad(FileConfig config) {}
    @Override protected void onClick(ViewerContext context, InventoryClickEvent event) {}
    @Override protected void onDrag(ViewerContext context, InventoryDragEvent event) {}
    @Override protected void onClose(ViewerContext context, InventoryCloseEvent event) {}
    @Override public void onPrepare(ViewerContext context, InventoryView view, Inventory inventory, List<MenuItem> items) {
        Player player = context.getPlayer();
        this.populator.populateTo(context, this.module.engine().personals().all(player), items);
    }
    @Override public void onReady(ViewerContext context, InventoryView view, Inventory inventory) {}
    @Override public void onRender(ViewerContext context, InventoryView view, Inventory inventory) {}
}
