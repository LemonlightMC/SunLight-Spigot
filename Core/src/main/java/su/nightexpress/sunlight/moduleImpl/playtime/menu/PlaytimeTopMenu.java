package su.nightexpress.sunlight.moduleImpl.playtime.menu;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GOLD;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GREEN;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.ORANGE;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.UNDERLINED;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.WHITE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
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
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeModule;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeTopHandler.TopEntry;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.moduleImpl.playtime.config.PlaytimeLang;
import su.nightexpress.sunlight.utils.TimeUtil;

/**
 * Paginated leaderboard with a period switcher on top.
 * <p>
 * One instance per open carries the selected period, so concurrent viewers
 * never share state.
 */
public class PlaytimeTopMenu extends AbstractMenu {

    private static final int PAGE_SIZE = 7;
    private static final int FIRST_SLOT = 19;

    private final PlaytimeModule module;
    private final PlaytimePeriod period;
    private final int initialPage;
    private boolean initialPageApplied;

    public PlaytimeTopMenu(final PlaytimeModule module, final PlaytimePeriod period) {
        this(module, period, 1);
    }

    public PlaytimeTopMenu(final PlaytimeModule module, final PlaytimePeriod period, final int initialPage) {
        super(MenuType.GENERIC_9X5, PlaytimeLang.MENU_TITLE_TOP.text());
        this.module = module;
        this.period = period == null ? PlaytimePeriod.ALLTIME : period;
        this.initialPage = Math.max(1, initialPage);
    }

    public boolean open(final SunLightPlugin plugin, final Player viewer) {
        return this.show(plugin, viewer);
    }

    @Override
    public void registerActions() {
    }

    @Override
    public void registerConditions() {
    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(0, 9).toArray());
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(36, 45).toArray());
        this.addNextPageButton(41);
        this.addPreviousPageButton(39);
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
        final Player viewer = context.getPlayer();
        final MenuViewer menuViewer = context.getViewer();

        final List<TopEntry> entries = new ArrayList<>(this.module.topHandler().getTop(this.period));
        menuViewer.setTotalPages(Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE));
        if (!this.initialPageApplied && this.initialPage > 1) {
            menuViewer.setCurrentPage(Math.min(this.initialPage, menuViewer.getTotalPages()));
            this.initialPageApplied = true;
        }

        this.addPeriodButton(list, PlaytimePeriod.ALLTIME, Material.NETHER_STAR, 1);
        this.addPeriodButton(list, PlaytimePeriod.YEAR, Material.ENDER_EYE, 2);
        this.addPeriodButton(list, PlaytimePeriod.MONTH, Material.MAP, 3);
        this.addPeriodButton(list, PlaytimePeriod.WEEK, Material.CLOCK, 4);
        this.addPeriodButton(list, PlaytimePeriod.DAY, Material.SUNFLOWER, 5);

        final int fromIndex = (menuViewer.getCurrentPage() - 1) * PAGE_SIZE;
        if (fromIndex < entries.size()) {
            final int toIndex = Math.min(entries.size(), fromIndex + PAGE_SIZE);
            for (int index = fromIndex; index < toIndex; index++) {
                final TopEntry entry = entries.get(index);
                list.add(this.entryItem(entry, index + 1, viewer.getUniqueId(), FIRST_SLOT + index - fromIndex));
            }
        } else {
            list.add(MenuItem.button()
                    .defaultState(NightItem.fromType(Material.BARRIER)
                            .setDisplayName(GRAY.wrap("No entries yet"))
                            .setLore(List.of(GRAY.wrap("Play for a while and come back!")))
                            .hideAllComponents())
                    .slots(22)
                    .build());
        }

        list.add(MenuItem.button()
                .defaultState(NightItem.fromType(Material.BOOK)
                        .setDisplayName(GOLD.wrap(PlaytimeLang.MENU_BUTTON_STATS.text()))
                        .setLore(List.of(GRAY.wrap("Back to your statistics."), "",
                                GOLD.wrap("→ " + UNDERLINED.wrap("Click to open."))))
                        .hideAllComponents(),
                        actionContext -> {
                            final Player clicker = actionContext.getPlayer();
                            final SunUser user = this.module.userManager().getOrFetch(clicker);
                            if (user != null) {
                                this.module.openStatsMenu(clicker, user);
                            }
                        })
                .slots(40)
                .build());

        list.add(MenuItem.button()
                .defaultState(NightItem.fromType(Material.BARRIER)
                        .setDisplayName(GRAY.wrap(PlaytimeLang.MENU_BUTTON_CLOSE.text()))
                        .hideAllComponents(),
                        actionContext -> actionContext.getViewer().closeMenu())
                .slots(44)
                .build());
    }

    private void addPeriodButton(final List<MenuItem> list, final PlaytimePeriod value, final Material icon,
            final int slot) {
        final boolean selected = this.period == value;
        final String label = value.displayname();
        final List<String> lore = new ArrayList<>();
        lore.add(GRAY.wrap("Sort the board by " + label.toLowerCase() + " playtime."));
        lore.add("");
        lore.add(selected ? GREEN.wrap("Currently selected.") : GOLD.wrap("→ " + UNDERLINED.wrap("Click to select.")));

        list.add(MenuItem.button()
                .defaultState(NightItem.fromType(icon)
                        .setDisplayName((selected ? GOLD : GRAY).wrap(label))
                        .setLore(lore)
                        .hideAllComponents(),
                        actionContext -> new PlaytimeTopMenu(this.module, value).show(this.module.plugin(),
                                actionContext.getPlayer()))
                .slots(slot)
                .build());
    }

    private MenuItem entryItem(final TopEntry entry, final int rank, final UUID viewerId, final int slot) {
        final String medal = switch (rank) {
            case 1 -> GOLD.wrap("#" + rank + " ");
            case 2 -> WHITE.wrap("#" + rank + " ");
            case 3 -> ORANGE.wrap("#" + rank + " ");
            default -> GRAY.wrap("#" + rank + " ");
        };
        final boolean self = entry.id().equals(viewerId);

        final List<String> lore = new ArrayList<>();
        lore.add(GRAY.wrap("Playtime: ") + WHITE.wrap(TimeUtil.formatMillis(entry.value())));
        lore.add(GRAY.wrap("Period: ") + WHITE.wrap(this.period.displayname()));
        if (self) {
            lore.add("");
            lore.add(GREEN.wrap("That's you!"));
        }

        final NightItem icon = NightItem.fromType(Material.PLAYER_HEAD)
                .setDisplayName(medal + (self ? GREEN.wrap(entry.name()) : WHITE.wrap(entry.name())))
                .setLore(lore)
                .hideAllComponents();
        try {
            icon.setSkullOwner(Bukkit.getOfflinePlayer(entry.id()));
        } catch (final Exception ignored) {
        }
        return MenuItem.button().defaultState(icon).slots(slot).build();
    }

    @Override
    public void onReady(final ViewerContext context, final InventoryView view, final Inventory inventory) {
    }

    @Override
    public void onRender(final ViewerContext context, final InventoryView view, final Inventory inventory) {
    }
}
