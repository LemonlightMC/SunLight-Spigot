package su.nightexpress.sunlight.moduleImpl.playtime2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.api.event.PlaytimeGoalReachedEvent;
import su.nightexpress.sunlight.api.event.PlaytimeMilestoneEvent;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimeGoal;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimeMilestone;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.moduleImpl.playtime2.config.Playtime2Lang;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.property.UserProperty;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

public class Playtime2MilestoneHandler {

    private final Playtime2Module module;
    private volatile Map<String, PlaytimeMilestone> milestones;
    private volatile List<PlaytimeMilestone> milestoneList;

    public Playtime2MilestoneHandler(final Playtime2Module module) {
        this.module = module;
        this.milestones = Map.of();
        this.milestoneList = List.of();
    }

    public void unload() {
        this.milestones = Map.of();
        this.milestoneList = List.of();
    }

    public Map<String, PlaytimeMilestone> getMilestones() {
        return Map.copyOf(this.milestones);
    }

    public List<PlaytimeMilestone> getMilestoneList() {
        return this.milestoneList;
    }

    public void loadMilestones(final FileConfig config) {
        if (config.getConfigurationSection("Playtime.Milestones") == null) {
            config.addMissing("Playtime.Milestones.starter.Name", "Starter");
            config.addMissing("Playtime.Milestones.starter.Minutes", 60L);
            config.addMissing("Playtime.Milestones.starter.Repeatable", false);
            config.addMissing("Playtime.Milestones.starter.Rewards", List.of("eco give %player_name% 500"));
            config.addMissing("Playtime.Milestones.veteran.Name", "Veteran");
            config.addMissing("Playtime.Milestones.veteran.Minutes", 600L);
            config.addMissing("Playtime.Milestones.veteran.Repeatable", false);
            config.addMissing("Playtime.Milestones.veteran.Rewards", List.of("eco give %player_name% 5000"));
            config.addMissing("Playtime.Milestones.grinder.Name", "Grinder");
            config.addMissing("Playtime.Milestones.grinder.Minutes", 120L);
            config.addMissing("Playtime.Milestones.grinder.Repeatable", true);
            config.addMissing("Playtime.Milestones.grinder.Rewards", List.of("eco give %player_name% 250"));
        }

        final ConfigurationSection section = config.getConfigurationSection("Playtime.Milestones");
        if (section == null) {
            this.milestones = Map.of();
            this.milestoneList = List.of();
            return;
        }

        final Map<String, PlaytimeMilestone> parsed = new LinkedHashMap<>();
        for (final String id : section.getKeys(false)) {
            final String key = Utils.lowercase(id);
            if (key == null || key.isBlank())
                continue;
            final String base = "Playtime.Milestones." + id + ".";
            final long minutes = Math.max(0L, config.getLong(base + "Minutes", 0L));
            if (minutes <= 0L)
                continue;
            String name = config.getString(base + "Name", id);
            if (name == null || name.isBlank())
                name = id;
            final boolean repeatable = config.getBoolean(base + "Repeatable", false);
            final List<String> rewards = config.getStringList(base + "Rewards");
            parsed.put(key, new PlaytimeMilestone(key, name, minutes * 60_000L, repeatable, List.copyOf(rewards)));
        }

        final List<PlaytimeMilestone> ordered = new ArrayList<>(parsed.values());
        ordered.sort(Comparator.comparingLong(PlaytimeMilestone::thresholdMs));
        this.milestones = Map.copyOf(parsed);
        this.milestoneList = List.copyOf(ordered);
    }

    public void checkMilestones(final Player player, final SunUser user) {
        final List<PlaytimeMilestone> ordered = this.milestoneList;
        if (ordered.isEmpty())
            return;

        final long total = Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.TOTAL));
        if (total <= 0L)
            return;

        // Read-only fast path: no map copy unless something actually triggers.
        final Map<String, Integer> progress = user.getPropertyOrDefault(Playtime2Properties.MILESTONE_PROGRESS);
        Map<String, Integer> updated = null;

        for (final PlaytimeMilestone milestone : ordered) {
            if (milestone.thresholdMs() <= 0L || total < milestone.thresholdMs())
                continue;

            if (milestone.repeatable()) {
                final int reached = (int) Math.min(Integer.MAX_VALUE, total / milestone.thresholdMs());
                final int claimed = Math.max(0, progress.getOrDefault(milestone.id(), 0));
                if (reached > claimed) {
                    if (updated == null)
                        updated = new HashMap<>(progress);
                    updated.put(milestone.id(), reached);
                    module.sendPrefixed(Playtime2Lang.MILESTONE_REWARD, player, b -> b
                            .with(SLPlaceholders.GENERIC_NAME, milestone::name));
                    module.dispatchRewardCommands(player, milestone.rewards());
                    module.plugin().getPluginManager().callEvent(new PlaytimeMilestoneEvent(player, milestone, reached));
                }
            } else if (!progress.containsKey(milestone.id())
                    && (updated == null || !updated.containsKey(milestone.id()))) {
                if (updated == null)
                    updated = new HashMap<>(progress);
                updated.put(milestone.id(), 1);
                module.sendPrefixed(Playtime2Lang.MILESTONE_REWARD, player, b -> b
                        .with(SLPlaceholders.GENERIC_NAME, milestone::name));
                module.dispatchRewardCommands(player, milestone.rewards());
                module.plugin().getPluginManager().callEvent(new PlaytimeMilestoneEvent(player, milestone, 1));
            }
        }

        if (updated != null) {
            user.setProperty(Playtime2Properties.MILESTONE_PROGRESS, updated);
        }
    }

    public int getMilestonesClaimed(final Player player, final boolean total) {
        if (player == null) {
            return 0;
        }

        final Map<String, PlaytimeMilestone> all = this.milestones;
        if (all.isEmpty()) {
            return 0;
        }
        if (total) {
            int claimed = 0;
            for (final PlaytimeMilestone milestone : all.values()) {
                if (!milestone.repeatable())
                    claimed++;
            }
            return claimed;
        }

        final SunUser user = module.userManager().getOrFetch(player);
        if (user == null) {
            return 0;
        }
        final Map<String, Integer> progress = user.getPropertyOrDefault(Playtime2Properties.MILESTONE_PROGRESS);
        int claimed = 0;
        for (final PlaytimeMilestone milestone : all.values()) {
            if (!milestone.repeatable() && progress.getOrDefault(milestone.id(), 0) > 0) {
                claimed++;
            }
        }
        return claimed;
    }

    public PlaytimeMilestone nextMilestone(final SunUser user) {
        final Map<String, Integer> progress = user.getPropertyOrDefault(Playtime2Properties.MILESTONE_PROGRESS);
        for (final PlaytimeMilestone milestone : this.milestoneList) {
            if (!milestone.repeatable() && progress.getOrDefault(milestone.id(), 0) <= 0) {
                return milestone;
            }
        }
        return null;
    }

    public void checkGoals(final Player player, final SunUser user) {
        this.checkGoal(player, user, PlaytimePeriod.DAY, "Daily",
                Playtime2Properties.DAY, Playtime2Properties.DAY_KEY,
                Playtime2Properties.REWARDED_DAY_KEY, Playtime2Properties.REMINDED_DAY_KEY);
        this.checkGoal(player, user, PlaytimePeriod.WEEK, "Weekly",
                Playtime2Properties.WEEK, Playtime2Properties.WEEK_KEY,
                Playtime2Properties.REWARDED_WEEK_KEY, Playtime2Properties.REMINDED_WEEK_KEY);
        this.checkGoal(player, user, PlaytimePeriod.MONTH, "Monthly",
                Playtime2Properties.MONTH, Playtime2Properties.MONTH_KEY,
                Playtime2Properties.REWARDED_MONTH_KEY, Playtime2Properties.REMINDED_MONTH_KEY);
    }

    private void checkGoal(final Player player, final SunUser user, final PlaytimePeriod period,
            final String goalName, final UserProperty<Long> counter, final UserProperty<Long> periodKey,
            final UserProperty<Long> rewardedKey, final UserProperty<Long> remindedKey) {
        final PlaytimeGoal goal = module.getSettings().getGoal(period);
        if (goal == null) {
            return;
        }
        final long goalMs = goal.duration();
        if (goalMs <= 0L) {
            return;
        }

        final long playtime = Math.max(0L, user.getPropertyOrDefault(counter));
        final long key = user.getPropertyOrDefault(periodKey);

        if (playtime >= goalMs) {
            if (user.getPropertyOrDefault(rewardedKey) != key) {
                user.setProperty(rewardedKey, key);
                this.giveGoalReward(player, goalName, playtime, goal.rewards());
            }
        } else if (module.getSettings().isRemindersEnabled()) {
            if (goalMs - playtime <= goal.remainderWindow()
                    && user.getPropertyOrDefault(remindedKey) != key) {
                user.setProperty(remindedKey, key);
                sendRemainder(player, goalName, goalMs - playtime);
            }
        }
    }

    private void sendRemainder(final Player player, final String goalName, final long remaining) {
        module.sendPrefixed(Playtime2Lang.REMINDER_GOAL, player, b -> b
                .with(SLPlaceholders.GENERIC_NAME, () -> goalName)
                .with(SLPlaceholders.GENERIC_TIME, () -> TimeUtil.formatMillis(remaining)));
    }

    private void giveGoalReward(final Player player, final String goalName, final long playtime,
            final List<String> commands) {
        module.sendPrefixed(Playtime2Lang.REWARD_GOAL, player, b -> b
                .with(SLPlaceholders.GENERIC_NAME, () -> goalName));
        module.dispatchRewardCommands(player, commands);
        module.plugin().getPluginManager().callEvent(new PlaytimeGoalReachedEvent(player, goalName, playtime, commands));
    }
}
