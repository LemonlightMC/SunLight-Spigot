package su.nightexpress.sunlight.moduleImpl.settings.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsModule;
import su.nightexpress.sunlight.moduleImpl.settings.model.AudienceMode;
import su.nightexpress.sunlight.moduleImpl.settings.model.SettingDefinition;
import su.nightexpress.sunlight.moduleImpl.settings.model.SettingKind;
import su.nightexpress.sunlight.moduleImpl.settings.model.SettingsPanel;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.property.UserProperty;

public class PanelMenu extends AbstractMenu {

    private final SettingsModule module;
    private final SettingsPanel panel;

    public PanelMenu(SunLightPlugin plugin, SettingsModule module, SettingsPanel panel, MenuType menuType) {
        super(plugin, menuType, panel.title());
        this.module = module;
        this.panel = panel;
    }

    public SettingsPanel panel() {
        return this.panel;
    }

    @Override
    public void registerActions() {
    }

    @Override
    public void registerConditions() {
    }

    @Override
    public void defineDefaultLayout() {
        int rows = this.rowsFor(this.menuType.get());
        String parentId = this.panel.parentPanelId();

        List<Integer> slots = new ArrayList<>();

        if (rows >= 3) {
            int bottomRow = (rows - 1) * 9;

            this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(0, 9).toArray());

            if (parentId != null) {
                this.addBackButton(parentId, bottomRow);
                // Leave the slot under the back button empty so the two default
                // buttons cannot fight over the same inventory slot.
                this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE,
                        IntStream.range(bottomRow + 1, rows * 9).toArray());
            } else {
                this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE,
                        IntStream.range(bottomRow, rows * 9).toArray());
            }

            for (int row = 1; row < rows - 1; row++) {
                for (int col = 1; col < 8; col++) {
                    slots.add(row * 9 + col);
                }
            }
        } else {
            // A panel resized to one or two rows has no border to spare. Let the
            // settings use the whole inventory instead of silently dropping them.
            int start = 0;
            if (parentId != null) {
                this.addBackButton(parentId, 0);
                start = 1;
            }
            for (int slot = start; slot < rows * 9; slot++) {
                slots.add(slot);
            }
        }

        int index = 0;
        for (SettingDefinition setting : this.panel.settings()) {
            if (index >= slots.size()) break;
            this.addSettingButton(setting, slots.get(index++));
        }
    }

    private void addBackButton(String parentId, int slot) {
        this.addDefaultButton("back", MenuItem.button()
                .defaultState(ItemState.builder()
                        .icon(NightItem.fromType(Material.SPECTRAL_ARROW)
                                .setDisplayName(TagWrappers.WHITE.wrap("Back")))
                        .action(context -> this.module.openPanel(context.getPlayer(), parentId))
                        .build())
                .slots(slot)
                .build());
    }

    private void addSettingButton(SettingDefinition setting, int slot) {
        MenuItem.Builder builder = MenuItem.button();

        if (setting.kind() == SettingKind.TOGGLE) {
            builder.state("enabled", this.buildToggleState(setting, true));
            builder.state("disabled", this.buildToggleState(setting, false));
        } else if (setting.kind() == SettingKind.MULTI_STATE) {
            for (String value : setting.values()) {
                builder.state(value, this.buildValueState(setting, value));
            }
            // Renders when the stored value is unknown or no longer permitted, so the
            // player can always cycle back into an allowed value.
            builder.state("fallback", this.buildValueFallbackState(setting));
        } else {
            builder.defaultState(this.buildActionState(setting));
        }

        this.addDefaultButton(setting.id(), builder.slots(slot).build());
    }

    private ItemState buildToggleState(SettingDefinition setting, boolean enabled) {
        UserProperty<Boolean> property = this.booleanProperty(setting);
        return ItemState.builder()
                .icon(NightItem.fromType(setting.icon())
                        .setDisplayName(TagWrappers.YELLOW.and(TagWrappers.BOLD).wrap(setting.displayName())))
                .displayModifier((context, item) -> {
                    boolean state = this.currentBoolean(context.getPlayer(), property);
                    item.setLore(Lists.newList(
                            TagWrappers.GRAY.wrap("State: ") + (state
                                    ? TagWrappers.GREEN.wrap("Enabled")
                                    : TagWrappers.RED.wrap("Disabled")),
                            "",
                            TagWrappers.GRAY.wrap(setting.description()),
                            "",
                            TagWrappers.GREEN.wrap("→ " + TagWrappers.UNDERLINED.wrap("Click to toggle"))));
                })
                .condition(context -> {
                    if (this.currentBoolean(context.getPlayer(), property) != enabled) return false;
                    if (setting.permission() == null) return true;
                    return context.getPlayer().hasPermission(setting.permission());
                })
                .action(context -> {
                    if (setting.permission() != null && !context.getPlayer().hasPermission(setting.permission())) return;
                    SunUser user = this.module.userManager().getOrFetch(context.getPlayer());
                    user.setProperty(property, !user.getPropertyOrDefault(property));
                    user.markDirty();
                    this.module.handleSettingChange(context.getPlayer(), setting);
                    context.getViewer().refresh();
                })
                .build();
    }

    private ItemState buildValueState(SettingDefinition setting, String value) {
        UserProperty<String> property = this.stringProperty(setting);
        return ItemState.builder()
                .icon(NightItem.fromType(setting.icon())
                        .setDisplayName(TagWrappers.YELLOW.and(TagWrappers.BOLD).wrap(setting.displayName())))
                .displayModifier((context, item) -> {
                    String current = this.currentString(context.getPlayer(), property);
                    item.setLore(Lists.newList(
                            TagWrappers.GRAY.wrap("Current: ") + TagWrappers.WHITE.wrap(displayValue(setting, current)),
                            "",
                            TagWrappers.GRAY.wrap(setting.description()),
                            "",
                            TagWrappers.GREEN.wrap("→ " + TagWrappers.UNDERLINED.wrap("Click to change"))));
                })
                .condition(context -> {
                    if (!this.currentString(context.getPlayer(), property).equals(value)) return false;
                    return setting.permission() == null || context.getPlayer().hasPermission(setting.permission());
                    // Value permissions are checked when cycling, not when displaying:
                    // a player who loses access to their current value should still see
                    // which value is active instead of the menu showing an unknown state.
                })
                .action(context -> {
                    if (setting.permission() != null && !context.getPlayer().hasPermission(setting.permission())) return;
                    String current = this.currentString(context.getPlayer(), property);
                    String next = this.nextAllowed(context.getPlayer(), setting, current);
                    SunUser user = this.module.userManager().getOrFetch(context.getPlayer());
                    user.setProperty(property, next);
                    user.markDirty();
                    this.module.handleSettingChange(context.getPlayer(), setting);
                    context.getViewer().refresh();
                })
                .build();
    }

    private ItemState buildValueFallbackState(SettingDefinition setting) {
        UserProperty<String> property = this.stringProperty(setting);
        return ItemState.builder()
                .icon(NightItem.fromType(setting.icon())
                        .setDisplayName(TagWrappers.YELLOW.and(TagWrappers.BOLD).wrap(setting.displayName())))
                .displayModifier((context, item) -> {
                    String current = this.currentString(context.getPlayer(), property);
                    item.setLore(Lists.newList(
                            TagWrappers.GRAY.wrap("Current: ") + TagWrappers.WHITE.wrap(displayValue(setting, current)),
                            "",
                            TagWrappers.GRAY.wrap(setting.description()),
                            "",
                            TagWrappers.GREEN.wrap("→ " + TagWrappers.UNDERLINED.wrap("Click to change"))));
                })
                .condition(context -> setting.permission() == null
                        || context.getPlayer().hasPermission(setting.permission()))
                .action(context -> {
                    if (setting.permission() != null && !context.getPlayer().hasPermission(setting.permission())) return;
                    String current = this.currentString(context.getPlayer(), property);
                    String next = this.nextAllowed(context.getPlayer(), setting, current);
                    SunUser user = this.module.userManager().getOrFetch(context.getPlayer());
                    user.setProperty(property, next);
                    user.markDirty();
                    this.module.handleSettingChange(context.getPlayer(), setting);
                    context.getViewer().refresh();
                })
                .build();
    }

    private ItemState buildActionState(SettingDefinition setting) {
        return ItemState.builder()
                .icon(NightItem.fromType(setting.icon())
                        .setDisplayName(TagWrappers.YELLOW.and(TagWrappers.BOLD).wrap(setting.displayName())))
                .displayModifier((context, item) -> item.setLore(Lists.newList(
                        TagWrappers.GRAY.wrap(setting.description()),
                        "",
                        TagWrappers.GREEN.wrap("→ " + TagWrappers.UNDERLINED.wrap("Click to open")))))
                .condition(context -> setting.permission() == null || context.getPlayer().hasPermission(setting.permission()))
                .action(context -> {
                    if (setting.permission() != null && !context.getPlayer().hasPermission(setting.permission())) return;
                    if (setting.targetPanel() != null) {
                        this.module.openPanel(context.getPlayer(), setting.targetPanel());
                        return;
                    }
                    if (setting.action() != null) {
                        setting.action().accept(context.getPlayer());
                    }
                })
                .build();
    }

    private static int rowsFor(MenuType type) {
        if (type == MenuType.GENERIC_9X1) return 1;
        if (type == MenuType.GENERIC_9X2) return 2;
        if (type == MenuType.GENERIC_9X3) return 3;
        if (type == MenuType.GENERIC_9X4) return 4;
        if (type == MenuType.GENERIC_9X5) return 5;
        if (type == MenuType.GENERIC_9X6) return 6;
        return 5;
    }

    private String nextAllowed(Player player, SettingDefinition setting, String current) {
        List<String> values = setting.values();
        int index = values.indexOf(current);
        if (index < 0) index = 0;
        for (int i = 1; i <= values.size(); i++) {
            String candidate = values.get((index + i) % values.size());
            if (isFreeValue(candidate)) {
                return candidate;
            }
            String permission = setting.valuePermission(candidate);
            if (permission == null || player.hasPermission(permission)) {
                return candidate;
            }
        }
        return current;
    }

    /**
     * Turning a feature off must never be gated behind a value permission,
     * otherwise a player who loses access to every other option is stuck.
     */
    private static boolean isFreeValue(String value) {
        return value.equalsIgnoreCase("none") || value.equalsIgnoreCase("nobody") || value.equals("0");
    }

    private static String displayValue(SettingDefinition setting, String value) {
        if (value == null || value.isBlank()) {
            return "Off";
        }
        if (setting.values().contains("everyone") || setting.values().contains("nobody")) {
            return AudienceMode.byId(value).displayName();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private UserProperty<Boolean> booleanProperty(SettingDefinition setting) {
        return (UserProperty<Boolean>) setting.property();
    }

    @SuppressWarnings("unchecked")
    private UserProperty<String> stringProperty(SettingDefinition setting) {
        return (UserProperty<String>) setting.property();
    }

    private boolean currentBoolean(Player player, UserProperty<Boolean> property) {
        SunUser user = this.module.userManager().getOrFetch(player);
        return user.getPropertyOrDefault(property);
    }

    private String currentString(Player player, UserProperty<String> property) {
        SunUser user = this.module.userManager().getOrFetch(player);
        String value = user.getPropertyOrDefault(property);
        return value == null ? "" : value;
    }

    @Override
    protected void onLoad(FileConfig config) {
    }

    @Override
    protected void onClick(ViewerContext context, InventoryClickEvent event) {
    }

    @Override
    protected void onDrag(ViewerContext context, InventoryDragEvent event) {
    }

    @Override
    protected void onClose(ViewerContext context, InventoryCloseEvent event) {
    }

    @Override
    public void onPrepare(ViewerContext context, InventoryView view, Inventory inventory, List<MenuItem> items) {
    }

    @Override
    public void onReady(ViewerContext context, InventoryView view, Inventory inventory) {
    }

    @Override
    public void onRender(ViewerContext context, InventoryView view, Inventory inventory) {
    }
}