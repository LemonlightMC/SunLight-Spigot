package su.nightexpress.sunlight.moduleImpl.playtime2.menu;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.DARK_GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GOLD;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GREEN;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.RED;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.SOFT_RED;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.UNDERLINED;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.WHITE;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimeMilestone;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.moduleImpl.playtime2.Playtime2History;
import su.nightexpress.sunlight.moduleImpl.playtime2.Playtime2Module;
import su.nightexpress.sunlight.moduleImpl.playtime2.Playtime2Properties;
import su.nightexpress.sunlight.moduleImpl.playtime2.config.Playtime2Lang;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.TimeUtil;

/**
 * Player-facing statistics overview: totals per period, session, streaks,
 * goal progress bars, milestone progress and a strip of the recent days.
 */
public class Playtime2StatsMenu extends AbstractMenu {

        private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd MMM");
        private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

        /** Slots of the recent-days strip, oldest on the left. */
        private static final int[] RECENT_SLOTS = { 19, 20, 21, 22, 23, 24, 25 };

        private final Playtime2Module module;
        private final UUID targetId;

        public Playtime2StatsMenu(final Playtime2Module module, final UUID targetId) {
                super(MenuType.GENERIC_9X5, Playtime2Lang.MENU_TITLE_STATS.text());
                this.module = module;
                this.targetId = targetId;
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

                list.add(this.headItem(user, 4));

                list.add(PlaytimePeriod.YEAR.item(Material.ENDER_EYE, 10,
                                this.module.getEffectivePlaytime(user, PlaytimePeriod.YEAR)));
                list.add(PlaytimePeriod.MONTH.item(Material.MAP, 11,
                                this.module.getEffectivePlaytime(user, PlaytimePeriod.MONTH)));
                list.add(PlaytimePeriod.WEEK.item(Material.CLOCK, 12,
                                this.module.getEffectivePlaytime(user, PlaytimePeriod.WEEK)));
                list.add(PlaytimePeriod.DAY.item(Material.SUNFLOWER, 13,
                                this.module.getEffectivePlaytime(user, PlaytimePeriod.DAY)));
                list.add(PlaytimePeriod.SESSION.item(Material.FEATHER, 14,
                                this.module.getEffectivePlaytime(user, PlaytimePeriod.SESSION)));

                list.add(this.streakItem(Material.FIREWORK_STAR, 28, "Daily streak",
                                this.module.getDailyStreak(user), "day"));
                list.add(this.streakItem(Material.FIREWORK_STAR, 29, "Weekly streak",
                                this.module.getWeeklyStreak(user), "week"));
                list.add(this.milestoneItem(user, 30));

                list.add(this.goalItem(32, "Daily", this.module.getEffectivePlaytime(user, PlaytimePeriod.DAY),
                                this.module.getSettings().getGoalMs(PlaytimePeriod.DAY)));
                list.add(this.goalItem(33, "Weekly", this.module.getEffectivePlaytime(user, PlaytimePeriod.WEEK),
                                this.module.getSettings().getGoalMs(PlaytimePeriod.WEEK)));
                list.add(this.goalItem(34, "Monthly", this.module.getEffectivePlaytime(user, PlaytimePeriod.MONTH),
                                this.module.getSettings().getGoalMs(PlaytimePeriod.MONTH)));

                this.addRecentDays(list, user);

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.GOLD_INGOT)
                                                .setDisplayName(GOLD.wrap(Playtime2Lang.MENU_BUTTON_TOP.text()))
                                                .setLore(List.of(GRAY.wrap("View the leaderboard."), "",
                                                                GOLD.wrap("→ " + UNDERLINED.wrap("Click to open."))))
                                                .hideAllComponents(),
                                                actionContext -> this.module.openTopMenu(actionContext.getPlayer(),
                                                                PlaytimePeriod.ALLTIME))
                                .slots(38)
                                .build());

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.CLOCK)
                                                .setDisplayName(GOLD.wrap(Playtime2Lang.MENU_BUTTON_CALENDAR.text()))
                                                .setLore(List.of(GRAY.wrap("Every day of the month."), "",
                                                                GOLD.wrap("→ " + UNDERLINED.wrap("Click to open."))))
                                                .hideAllComponents(),
                                                actionContext -> this.module.openCalendarMenu(actionContext.getPlayer(),
                                                                user,
                                                                YearMonth.now(TimeUtil.getZoneId())))
                                .slots(39)
                                .build());

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.SUNFLOWER)
                                                .setDisplayName(GRAY.wrap("Refresh"))
                                                .setLore(List.of(GRAY.wrap("Update all numbers."), "",
                                                                GOLD.wrap("→ " + UNDERLINED.wrap("Click to refresh."))))
                                                .hideAllComponents(),
                                                actionContext -> this.module.openStatsMenu(actionContext.getPlayer(),
                                                                user))
                                .slots(40)
                                .build());

                list.add(MenuItem.button()
                                .defaultState(NightItem.fromType(Material.BARRIER)
                                                .setDisplayName(GRAY.wrap(Playtime2Lang.MENU_BUTTON_CLOSE.text()))
                                                .hideAllComponents(),
                                                actionContext -> actionContext.getViewer().closeMenu())
                                .slots(41)
                                .build());

                if (!viewer.getUniqueId().equals(user.getId())) {
                        list.add(MenuItem.button()
                                        .defaultState(NightItem.fromType(Material.NAME_TAG)
                                                        .setDisplayName(GRAY.wrap("Viewing: ")
                                                                        + WHITE.wrap(user.getName()))
                                                        .hideAllComponents())
                                        .slots(0)
                                        .build());
                }
        }

        private MenuItem headItem(final SunUser user, final int slot) {
                final List<String> lore = new ArrayList<>();
                lore.add(GRAY.wrap("Total: ") + WHITE.wrap(
                                TimeUtil.formatMillis(this.module.getEffectivePlaytime(user, PlaytimePeriod.ALLTIME))));
                lore.add(GRAY.wrap("Session: ") + WHITE.wrap(
                                TimeUtil.formatMillis(this.module.getEffectivePlaytime(user, PlaytimePeriod.SESSION))));
                lore.add("");
                lore.add(GRAY.wrap("Days played: ") + WHITE.wrap(String.valueOf(this.module.getDaysPlayed(user))));

                final long first = this.module.getFirstPlayedDay(user);
                final long last = this.module.getLastPlayedDay(user);
                if (first > 0L) {
                        lore.add(GRAY.wrap("First played: ") + WHITE.wrap(LocalDate.ofEpochDay(first).format(DATE)));
                }
                if (last > 0L) {
                        lore.add(GRAY.wrap("Last played: ") + WHITE.wrap(LocalDate.ofEpochDay(last).format(DATE)));
                }

                final NightItem icon = NightItem.fromType(Material.PLAYER_HEAD)
                                .setDisplayName(GOLD.wrap(user.getName()))
                                .setLore(lore)
                                .hideAllComponents();
                try {
                        icon.setSkullOwner(Bukkit.getOfflinePlayer(user.getId()));
                } catch (final Exception ignored) {
                }
                return MenuItem.button().defaultState(icon).slots(slot).build();
        }

        /** The last seven days, ending today, coloured by how much was played. */
        private void addRecentDays(final List<MenuItem> list, final SunUser user) {
                // The calendar menu already loads the history on demand for offline
                // players; without the same here the strip would claim every recent
                // day was unplayed for anyone who is not currently online.
                final Playtime2History history = this.module.historyOrLoad(user.getId());
                final long today = this.module.currentKeys().day();
                for (int index = 0; index < RECENT_SLOTS.length; index++) {
                        final long day = today - (RECENT_SLOTS.length - 1 - index);
                        final long millis = history.getMillis(day);
                        final LocalDate date = LocalDate.ofEpochDay(day);

                        final List<String> lore = new ArrayList<>();
                        lore.add(millis > 0L ? WHITE.wrap(TimeUtil.formatMillis(millis)) : GRAY.wrap("Not played"));
                        if (day == today) {
                                lore.add("");
                                lore.add(GREEN.wrap("Today"));
                        }

                        list.add(MenuItem.button()
                                        .defaultState(NightItem
                                                        .fromType(this.module.getSettings().calendarMaterial(millis))
                                                        .setDisplayName(GOLD.wrap(date.format(DAY)))
                                                        .setLore(lore)
                                                        .hideAllComponents())
                                        .slots(RECENT_SLOTS[index])
                                        .build());
                }
        }

        private MenuItem streakItem(final Material material, final int slot, final String name, final int value,
                        final String unit) {
                final List<String> lore = new ArrayList<>();
                lore.add(WHITE.wrap(String.valueOf(value)) + GRAY.wrap(" " + unit + (value == 1 ? "" : "s")));
                lore.add("");
                lore.add(value > 1 ? GREEN.wrap("Keep it going!")
                                : GRAY.wrap("Play consecutive " + unit + "s to grow it."));
                return MenuItem.button()
                                .defaultState(NightItem.fromType(material)
                                                .setDisplayName(GOLD.wrap(name))
                                                .setLore(lore)
                                                .hideAllComponents())
                                .slots(slot)
                                .build();
        }

        private MenuItem goalItem(final int slot, final String name, final long current, final long goal) {
                final List<String> lore = new ArrayList<>();
                if (goal <= 0L) {
                        lore.add(SOFT_RED.wrap("No goal set"));
                } else {
                        final long percent = current <= 0L ? 0L : Math.min(100L, current * 100L / Math.max(1L, goal));
                        lore.add(WHITE.wrap(TimeUtil.formatMillis(Math.max(0L, current))) + GRAY.wrap(" / ")
                                        + WHITE.wrap(TimeUtil.formatMillis(goal)));
                        lore.add(this.progressBar(percent) + GRAY.wrap(" " + percent + "%"));
                }
                return MenuItem.button()
                                .defaultState(NightItem.fromType(Material.EXPERIENCE_BOTTLE)
                                                .setDisplayName(GOLD.wrap(name + " goal"))
                                                .setLore(lore)
                                                .hideAllComponents())
                                .slots(slot)
                                .build();
        }

        private MenuItem milestoneItem(final SunUser user, final int slot) {
                final Map<String, PlaytimeMilestone> all = this.module.milestoneHandler().getMilestones();
                final Map<String, Integer> progress = user.getPropertyOrDefault(Playtime2Properties.MILESTONE_PROGRESS);
                final long total = this.module.getEffectivePlaytime(user, PlaytimePeriod.ALLTIME);

                final List<String> lore = new ArrayList<>();
                if (all.isEmpty()) {
                        lore.add(GRAY.wrap("No milestones configured."));
                } else {
                        final long claimed = all.values().stream()
                                        .filter(m -> !m.repeatable() && progress.getOrDefault(m.id(), 0) > 0)
                                        .count();
                        final long oneShots = all.values().stream().filter(m -> !m.repeatable()).count();
                        lore.add(GRAY.wrap("Claimed: ") + WHITE.wrap(claimed + "/" + oneShots));

                        final PlaytimeMilestone next = this.module.milestoneHandler().nextMilestone(user);
                        if (next != null) {
                                final long percent = Math.min(100L, total * 100L / Math.max(1L, next.thresholdMs()));
                                lore.add("");
                                lore.add(GRAY.wrap("Next: ") + WHITE.wrap(next.name()));
                                lore.add(WHITE.wrap(TimeUtil.formatMillis(total)) + GRAY.wrap(" / ")
                                                + WHITE.wrap(TimeUtil.formatMillis(next.thresholdMs())));
                                lore.add(this.progressBar(percent) + GRAY.wrap(" " + percent + "%"));
                        } else {
                                lore.add("");
                                lore.add(GREEN.wrap("All milestones complete!"));
                        }
                }
                return MenuItem.button()
                                .defaultState(NightItem.fromType(Material.NETHER_STAR)
                                                .setDisplayName(GOLD.wrap("Milestones"))
                                                .setLore(lore)
                                                .hideAllComponents())
                                .slots(slot)
                                .build();
        }

        private String progressBar(final long percent) {
                final int filled = (int) Math.min(20L, Math.max(0L, percent / 5L));
                final StringBuilder builder = new StringBuilder();
                for (int index = 0; index < 20; index++) {
                        builder.append(index < filled ? "▓" : "░");
                }
                return GREEN.wrap(builder.substring(0, filled)) + DARK_GRAY.wrap(builder.substring(filled));
        }

        @Override
        public void onReady(final ViewerContext context, final InventoryView view, final Inventory inventory) {
        }

        @Override
        public void onRender(final ViewerContext context, final InventoryView view, final Inventory inventory) {
        }
}
