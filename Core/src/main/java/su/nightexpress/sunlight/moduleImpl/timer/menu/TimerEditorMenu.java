package su.nightexpress.sunlight.moduleImpl.timer.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.inventory.action.ObjectActionContext;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.dialog.TimerDialogKeys;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerFormat;

import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class TimerEditorMenu extends AbstractObjectMenu<TimerInstance> {

    private final SunLightPlugin plugin;
    private final TimerModule module;

    public TimerEditorMenu(SunLightPlugin plugin, TimerModule module) {
        super(MenuType.GENERIC_9X6, "Timer Editor", TimerInstance.class);
        this.plugin = plugin;
        this.module = module;
    }

    @Override public void registerActions() {}
    @Override public void registerConditions() {}
    @Override public void defineDefaultLayout() {
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray());
        this.addButton("name", 10, NightItem.fromType(Material.NAME_TAG).setDisplayName(YELLOW.wrap("Name")).setLore(List.of(GRAY.wrap("Edit display name."))).hideAllComponents(), c -> open(c, TimerDialogKeys.TIMER_NAME));
        this.addButton("duration", 11, NightItem.fromType(Material.CLOCK).setDisplayName(YELLOW.wrap("Kind/Duration")).setLore(List.of(GRAY.wrap("Countdown/Stopwatch, seconds, target."))).hideAllComponents(), c -> open(c, TimerDialogKeys.TIMER_DURATION));
        this.addButton("schedule", 12, NightItem.fromType(Material.CLOCK).setDisplayName(YELLOW.wrap("Schedule (Start At)")).setLore(List.of(GRAY.wrap("Datetime + zone, no recurring."))).hideAllComponents(), c -> open(c, TimerDialogKeys.TIMER_SCHEDULE));
        this.addButton("visibility", 13, NightItem.fromType(Material.ENDER_EYE).setDisplayName(YELLOW.wrap("Visibility")).setLore(List.of(GRAY.wrap("ALL/PERMISSION/SELF"))).hideAllComponents(), c -> open(c, TimerDialogKeys.TIMER_VISIBILITY));
        this.addButton("rules", 14, NightItem.fromType(Material.PAINTING).setDisplayName(YELLOW.wrap("Display Rules")).setLore(List.of(GRAY.wrap("Staged actionbar/bossbar/title/subtitle/xp/chat."))).hideAllComponents(), c -> open(c, TimerDialogKeys.TIMER_RULE));
        this.addButton("triggers", 15, NightItem.fromType(Material.TRIPWIRE_HOOK).setDisplayName(YELLOW.wrap("Stop Events")).setLore(List.of(GRAY.wrap("Dragon death, player death/quit, etc."))).hideAllComponents(), c -> open(c, TimerDialogKeys.TIMER_TRIGGER));
        this.addButton("actions", 16, NightItem.fromType(Material.FIREWORK_ROCKET).setDisplayName(YELLOW.wrap("Actions")).setLore(List.of(GRAY.wrap("Sound/particle/firework/commands/money/messages."))).hideAllComponents(), c -> open(c, TimerDialogKeys.TIMER_ACTION));

        this.addButton("start", 28, NightItem.fromType(Material.LIME_DYE).setDisplayName(GREEN.wrap("Start")).hideAllComponents(), c -> { this.module.engine().start(c.getObject(), true, true); c.getViewer().refresh(); });
        this.addButton("pause", 29, NightItem.fromType(Material.YELLOW_DYE).setDisplayName(YELLOW.wrap("Pause")).hideAllComponents(), c -> { this.module.engine().pause(c.getObject(), true); c.getViewer().refresh(); });
        this.addButton("resume", 30, NightItem.fromType(Material.LIGHT_BLUE_DYE).setDisplayName(GOLD.wrap("Resume")).hideAllComponents(), c -> { this.module.engine().resume(c.getObject(), true); c.getViewer().refresh(); });
        this.addButton("reset", 31, NightItem.fromType(Material.ORANGE_DYE).setDisplayName(RED.wrap("Reset")).hideAllComponents(), c -> { this.module.engine().reset(c.getObject(), true); c.getViewer().refresh(); });
        this.addButton("stop", 32, NightItem.fromType(Material.GRAY_DYE).setDisplayName(RED.wrap("Stop")).hideAllComponents(), c -> { this.module.engine().stop(c.getObject(), true); c.getViewer().refresh(); });
        this.addButton("delete", 34, NightItem.fromType(Material.BARRIER).setDisplayName(DARK_RED.and(BOLD).wrap("Delete")).hideAllComponents(), c -> {
            TimerInstance t = c.getObject();
            this.module.engine().stop(t, true); this.module.engine().displays().clearTimer(t); this.module.engine().globals().delete(t.getId());
            this.module.openTimerList(c.getPlayer());
        });

        this.addDefaultButton("back", MenuItem.button().defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.COMPASS).setDisplayName(GREEN.wrap("Back")).hideAllComponents())
                .action(this.createObjectAction(c -> this.module.openTimerList(c.getPlayer()))).build()).slots(49).build());
    }

    private void addButton(String id, int slot, NightItem icon, java.util.function.Consumer<ObjectActionContext<TimerInstance>> action) {
        this.addDefaultButton(id, MenuItem.button().defaultState(ItemState.builder().icon(icon).action(this.createObjectAction(action::accept)).build()).slots(slot).build());
    }

    private void open(ObjectActionContext<TimerInstance> context, su.nightexpress.nightcore.ui.dialog.wrap.DialogKey<TimerInstance> key) {
        this.plugin.showDialog(context.getPlayer(), key, context.getObject(), () -> context.getViewer().refresh());
    }

    @Override protected void onLoad(FileConfig config) {}
    @Override protected void onClick(ViewerContext context, InventoryClickEvent event) {}
    @Override protected void onDrag(ViewerContext context, InventoryDragEvent event) {}
    @Override protected void onClose(ViewerContext context, InventoryCloseEvent event) {}
    @Override public void onPrepare(ViewerContext context, InventoryView view, Inventory inventory, List<MenuItem> items) {}
    @Override public void onReady(ViewerContext context, InventoryView view, Inventory inventory) {
    }
    @Override public void onRender(ViewerContext context, InventoryView view, Inventory inventory) {}
}
