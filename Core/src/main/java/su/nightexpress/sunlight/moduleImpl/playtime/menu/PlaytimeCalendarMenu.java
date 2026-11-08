package su.nightexpress.sunlight.moduleImpl.playtime.menu;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GOLD;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GREEN;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.RED;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.UNDERLINED;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.WHITE;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeHistory;
import su.nightexpress.sunlight.moduleImpl.playtime.PlaytimeModule;
import su.nightexpress.sunlight.moduleImpl.playtime.config.PlaytimeLang;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.TimeUtil;

/**
 * One month of a player's daily history. Weeks start on Monday, matching the
 * weekly streak, and each day is coloured by how much was played that day.
 * Previous and next months open a fresh menu, so the viewed month never has
 * to be shared state.
 */
public class PlaytimeCalendarMenu extends AbstractMenu {

        private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
        private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);

        /** First slot of the day grid: row 1, column 0 of a 9-wide inventory. */
        private static final int GRID_ORIGIN = 9;

        private final PlaytimeModule module;
        private final UUID targetId;
        private final YearMonth month;

        public PlaytimeCalendarMenu(final PlaytimeModule module, final UUID targetId, final YearMonth month) {
                super(MenuType.GENERIC_9X6, PlaytimeLang.MENU_TITLE_CALENDAR.text());
                this.module = module;
                this.targetId = targetId;
                this.month = month == null ? java.time.YearMonth.now(TimeUtil.getZoneId()) : month;
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
                this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray());
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
                final SunUser user = this.module.userManager().getOrFetch(this.targetId).orElse(null);
                if (user == null) {
                        list.add(MenuItem.button()
                                        .defaultState(NightItem.fromType(Material.BARRIER)
                                                        .setDisplayName(RED.wrap("Data unavailable"))
                                                        .setLore(List.of(GRAY.wrap("This player has no stored data.")))
                                                        .hideAllComponents())
                                        .slots(22)
                                        .build());
                        return;
                }

                this.addWeekdayHeaders(list);
                this.addDays(list, user);
                this.addNavigation(list, context.getPlayer(), user);
        }

        private void addWeekdayHeaders(final List<MenuItem> list) {
                for (int column = 0; column < 7; column++) {
                        final DayOfWeek weekday = DayOfWeek.MONDAY.plus(column);
                        list.add(MenuItem.button()
                                        .defaultState(NightItem.fromType(Material.WHITE_STAINED_GLASS_PANE)
                                                        .setDisplayName(GRAY.wrap(weekday.getDisplayName(
                                                                        TextStyle.SHORT, Locale.ENGLISH)))
                                                        .hideAllComponents())
                                        .slots(column)
                                        .build());
                }
        }

        /** The day grid of one month, keyed by epoch day. Empty days are absent. */
        public Map<Long, Long> monthGrid(final UUID playerId, final YearMonth month) {
                final PlaytimeHistory history = this.module.historyForRead(playerId);
                synchronized (history) {
                        return history.monthGrid(month);
                }
        }

        private void addDays(final List<MenuItem> list, final SunUser user) {
                final Map<Long, Long> grid = monthGrid(user.getId(), this.month);
                final LocalDate first = this.month.atDay(1);
                // Monday is column 0, so the offset is the weekday's ordinal.
                final int offset = first.getDayOfWeek().getValue() - 1;
                final long today = this.module.currentKeys().day();

                for (int day = 1; day <= this.month.lengthOfMonth(); day++) {
                        final LocalDate date = this.month.atDay(day);
                        final long epochDay = date.toEpochDay();
                        final long millis = grid.getOrDefault(epochDay, 0L);
                        final int slot = GRID_ORIGIN + offset + day - 1;

                        final List<String> lore = new ArrayList<>();
                        lore.add(millis > 0L ? WHITE.wrap(TimeUtil.formatMillis(millis)) : GRAY.wrap("Not played"));
                        if (epochDay == today) {
                                lore.add("");
                                lore.add(GREEN.wrap("Today"));
                        }

                        list.add(MenuItem.button()
                                        .defaultState(NightItem
                                                        .fromType(this.module.getSettings().calendarMaterial(millis))
                                                        .setDisplayName(GOLD.wrap(date.format(DAY)))
                                                        .setLore(lore)
                                                        .setAmount(day)
                                                        .hideAllComponents())
                                        .slots(slot)
                                        .build());
                }
        }

        private void addNavigation(final List<MenuItem> list, final Player viewer, final SunUser user) {
                final YearMonth current = YearMonth.now(TimeUtil.getZoneId());

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.ARROW)
                                                .setDisplayName(GOLD.wrap(PlaytimeLang.MENU_BUTTON_PREVIOUS.text()))
                                                .hideAllComponents(),
                                                actionContext -> this.open(user, this.month.minusMonths(1),
                                                                actionContext.getPlayer()))
                                .slots(7)
                                .build());

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.BOOK)
                                                .setDisplayName(GOLD.wrap(this.month.format(MONTH)))
                                                .setLore(List.of(GRAY.wrap("Viewing: ") + WHITE.wrap(user.getName())))
                                                .hideAllComponents())
                                .slots(16)
                                .build());

                // There is nothing to show past the current month.
                if (this.month.isBefore(current)) {
                        list.add(MenuItem.button()
                                        .defaultState(NightItem.fromType(Material.ARROW)
                                                        .setDisplayName(GOLD
                                                                        .wrap(PlaytimeLang.MENU_BUTTON_NEXT.text()))
                                                        .hideAllComponents(),
                                                        actionContext -> this.open(user, this.month.plusMonths(1),
                                                                        actionContext.getPlayer()))
                                        .slots(8)
                                        .build());
                }

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.BOOK)
                                                .setDisplayName(GOLD.wrap(PlaytimeLang.MENU_BUTTON_BACK.text()))
                                                .setLore(List.of(GRAY.wrap("Back to the statistics."), "",
                                                                GOLD.wrap("→ " + UNDERLINED.wrap("Click to open."))))
                                                .hideAllComponents(),
                                                actionContext -> this.module.openStatsMenu(actionContext.getPlayer(),
                                                                user))
                                .slots(25)
                                .build());

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.BARRIER)
                                                .setDisplayName(GRAY.wrap(PlaytimeLang.MENU_BUTTON_CLOSE.text()))
                                                .hideAllComponents(),
                                                actionContext -> actionContext.getViewer().closeMenu())
                                .slots(34)
                                .build());
        }

        private void open(final SunUser user, final YearMonth month, final Player viewer) {
                new PlaytimeCalendarMenu(this.module, user.getId(), month).show(this.module.plugin(), viewer);
        }

        @Override
        public void onReady(final ViewerContext context, final InventoryView view, final Inventory inventory) {
        }

        @Override
        public void onRender(final ViewerContext context, final InventoryView view, final Inventory inventory) {
        }
}
