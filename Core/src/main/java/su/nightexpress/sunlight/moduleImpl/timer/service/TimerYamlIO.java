package su.nightexpress.sunlight.moduleImpl.timer.service;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.sunlight.moduleImpl.timer.model.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class TimerYamlIO {

    private TimerYamlIO() {
    }

    public static TimerDefinition readDefinition(FileConfig config, String path, String fallbackId) {
        TimerDefinition def = new TimerDefinition();
        def.setId(config.getString(path + ".Id", fallbackId));
        def.setName(config.getString(path + ".Name", def.getId()));
        def.setScope(parseEnum(config.getString(path + ".Scope", TimerScope.GLOBAL.name()), TimerScope.class, TimerScope.GLOBAL));
        def.setKind(parseEnum(config.getString(path + ".Kind", TimerKind.COUNTDOWN.name()), TimerKind.class, TimerKind.COUNTDOWN));
        def.setDurationSeconds(config.getLong(path + ".DurationSeconds", 60L));
        def.setUseStopwatchTarget(config.getBoolean(path + ".UseStopwatchTarget", false));
        def.setRestoreXpOnStop(config.getBoolean(path + ".RestoreXpOnStop", true));

        TimerSchedule schedule = new TimerSchedule();
        schedule.setEnabled(config.getBoolean(path + ".Schedule.Enabled", false));
        schedule.setStartAt(config.getString(path + ".Schedule.StartAt", ""));
        schedule.setZoneId(config.getString(path + ".Schedule.ZoneId", "UTC"));
        schedule.setAutoStart(config.getBoolean(path + ".Schedule.AutoStart", true));
        schedule.setRunMissedOnLoad(config.getBoolean(path + ".Schedule.RunMissedOnLoad", false));
        def.setSchedule(schedule);

        TimerVisibility visibility = new TimerVisibility();
        visibility.setMode(parseEnum(config.getString(path + ".Visibility.Mode", VisibilityMode.ALL.name()), VisibilityMode.class, VisibilityMode.ALL));
        visibility.setPermission(config.getString(path + ".Visibility.Permission", ""));
        def.setVisibility(visibility);

        def.setDisplayRules(readRules(config, path + ".DisplayRules"));
        def.setStopEvents(readTriggers(config, path + ".StopEvents"));
        def.setActions(readActions(config, path + ".Actions"));
        return def;
    }

    public static void writeDefinition(FileConfig config, String path, TimerDefinition def) {
        config.set(path + ".Id", def.getId());
        config.set(path + ".Name", def.getName());
        config.set(path + ".Scope", def.getScope().name());
        config.set(path + ".Kind", def.getKind().name());
        config.set(path + ".DurationSeconds", def.getDurationSeconds());
        config.set(path + ".UseStopwatchTarget", def.isUseStopwatchTarget());
        config.set(path + ".RestoreXpOnStop", def.isRestoreXpOnStop());

        config.set(path + ".Schedule.Enabled", def.getSchedule().isEnabled());
        config.set(path + ".Schedule.StartAt", def.getSchedule().getStartAt());
        config.set(path + ".Schedule.ZoneId", def.getSchedule().getZoneId());
        config.set(path + ".Schedule.AutoStart", def.getSchedule().isAutoStart());
        config.set(path + ".Schedule.RunMissedOnLoad", def.getSchedule().isRunMissedOnLoad());

        config.set(path + ".Visibility.Mode", def.getVisibility().getMode().name());
        config.set(path + ".Visibility.Permission", def.getVisibility().getPermission());

        writeRules(config, path + ".DisplayRules", def.getDisplayRules());
        writeTriggers(config, path + ".StopEvents", def.getStopEvents());
        writeActions(config, path + ".Actions", def.getActions());
    }

    public static void readRuntime(FileConfig config, String path, TimerInstance instance) {
        instance.setState(parseEnum(config.getString(path + ".State", TimerState.STOPPED.name()), TimerState.class, TimerState.STOPPED));
        instance.setElapsedMillis(config.getLong(path + ".ElapsedMillis", 0L));
        instance.setRemainingMillis(config.getLong(path + ".RemainingMillis", instance.getRemainingMillis()));
        instance.setDurationMillis(config.getLong(path + ".DurationMillis", instance.getDurationMillis()));
        instance.setStartedAt(config.getLong(path + ".StartedAt", 0L));
        instance.setCompletedActionsFired(config.getBoolean(path + ".CompletedActionsFired", false));
        instance.setScheduleFired(config.getBoolean(path + ".ScheduleFired", false));
        instance.setScheduledStartEpoch(config.getLong(path + ".ScheduledStartEpoch", 0L));
    }

    public static void writeRuntime(FileConfig config, String path, TimerInstance instance) {
        config.set(path + ".State", instance.getState().name());
        config.set(path + ".ElapsedMillis", instance.getElapsedMillis());
        config.set(path + ".RemainingMillis", instance.getRemainingMillis());
        config.set(path + ".DurationMillis", instance.getDurationMillis());
        config.set(path + ".StartedAt", instance.getStartedAt());
        config.set(path + ".CompletedActionsFired", instance.isCompletedActionsFired());
        config.set(path + ".ScheduleFired", instance.isScheduleFired());
        config.set(path + ".ScheduledStartEpoch", instance.getScheduledStartEpoch());
    }

    private static List<DisplayRule> readRules(FileConfig config, String path) {
        List<DisplayRule> rules = new ArrayList<>();
        for (String key : config.getSection(path)) {
            String p = path + "." + key;
            DisplayRule rule = new DisplayRule();
            rule.setId(config.getString(p + ".Id", key));
            rule.setPriority(config.getInt(p + ".Priority", 0));
            rule.setCondition(parseEnum(config.getString(p + ".Condition", "ALWAYS"), RuleConditionType.class, RuleConditionType.ALWAYS));
            rule.setValue(config.getLong(p + ".Value", 0L));
            rule.setMin(config.getLong(p + ".Min", 0L));
            rule.setMax(config.getLong(p + ".Max", 0L));
            rule.setChannels(readChannels(config.getStringList(p + ".Channels")));
            rule.setActionBar(config.getString(p + ".ActionBar", ""));
            rule.setBossBar(config.getString(p + ".BossBar", ""));
            rule.setTitle(config.getString(p + ".Title", ""));
            rule.setSubtitle(config.getString(p + ".Subtitle", ""));
            rule.setChat(config.getString(p + ".Chat", ""));
            rule.setChatIntervalSeconds(config.getInt(p + ".ChatIntervalSeconds", 1));
            rule.setUpdateIntervalTicks(config.getInt(p + ".UpdateIntervalTicks", 20));
            rule.setBossBarColor(config.getString(p + ".BossBarColor", "YELLOW"));
            rule.setBossBarOverlay(config.getString(p + ".BossBarOverlay", "PROGRESS"));
            rules.add(rule);
        }
        return rules;
    }

    private static void writeRules(FileConfig config, String path, List<DisplayRule> rules) {
        config.remove(path);
        int index = 0;
        for (DisplayRule rule : rules) {
            String key = sanitizeKey(rule.getId(), "rule_" + index++);
            String p = path + "." + key;
            config.set(p + ".Id", rule.getId());
            config.set(p + ".Priority", rule.getPriority());
            config.set(p + ".Condition", rule.getCondition().name());
            config.set(p + ".Value", rule.getValue());
            config.set(p + ".Min", rule.getMin());
            config.set(p + ".Max", rule.getMax());
            config.set(p + ".Channels", rule.getChannels().stream().map(Enum::name).toList());
            config.set(p + ".ActionBar", rule.getActionBar());
            config.set(p + ".BossBar", rule.getBossBar());
            config.set(p + ".Title", rule.getTitle());
            config.set(p + ".Subtitle", rule.getSubtitle());
            config.set(p + ".Chat", rule.getChat());
            config.set(p + ".ChatIntervalSeconds", rule.getChatIntervalSeconds());
            config.set(p + ".UpdateIntervalTicks", rule.getUpdateIntervalTicks());
            config.set(p + ".BossBarColor", rule.getBossBarColor());
            config.set(p + ".BossBarOverlay", rule.getBossBarOverlay());
        }
    }

    private static List<TimerEventTrigger> readTriggers(FileConfig config, String path) {
        List<TimerEventTrigger> triggers = new ArrayList<>();
        for (String key : config.getSection(path)) {
            String p = path + "." + key;
            TimerEventTrigger trigger = new TimerEventTrigger();
            trigger.setType(config.getString(p + ".Type", "NONE"));
            trigger.setEntityType(config.getString(p + ".EntityType", ""));
            trigger.setAction(config.getString(p + ".Action", "COMPLETE"));
            triggers.add(trigger);
        }
        return triggers;
    }

    private static void writeTriggers(FileConfig config, String path, List<TimerEventTrigger> triggers) {
        config.remove(path);
        int index = 0;
        for (TimerEventTrigger trigger : triggers) {
            String p = path + ".trigger_" + (index++);
            config.set(p + ".Type", trigger.getType());
            config.set(p + ".EntityType", trigger.getEntityType());
            config.set(p + ".Action", trigger.getAction());
        }
    }

    private static TimerActions readActions(FileConfig config, String path) {
        TimerActions actions = new TimerActions();
        actions.setOnStart(readBundle(config, path + ".OnStart"));
        actions.setOnPause(readBundle(config, path + ".OnPause"));
        actions.setOnResume(readBundle(config, path + ".OnResume"));
        actions.setOnComplete(readBundle(config, path + ".OnComplete"));
        actions.setOnStopwatchEnd(readBundle(config, path + ".OnStopwatchEnd"));
        actions.setOnStop(readBundle(config, path + ".OnStop"));
        return actions;
    }

    private static void writeActions(FileConfig config, String path, TimerActions actions) {
        writeBundle(config, path + ".OnStart", actions.getOnStart());
        writeBundle(config, path + ".OnPause", actions.getOnPause());
        writeBundle(config, path + ".OnResume", actions.getOnResume());
        writeBundle(config, path + ".OnComplete", actions.getOnComplete());
        writeBundle(config, path + ".OnStopwatchEnd", actions.getOnStopwatchEnd());
        writeBundle(config, path + ".OnStop", actions.getOnStop());
    }

    private static ActionBundle readBundle(FileConfig config, String path) {
        ActionBundle bundle = new ActionBundle();
        bundle.setConsoleCommands(config.getStringList(path + ".ConsoleCommands"));
        bundle.setPlayerCommands(config.getStringList(path + ".PlayerCommands"));
        bundle.setSound(config.getString(path + ".Sound", ""));
        bundle.setParticle(config.getString(path + ".Particle", ""));
        bundle.setFirework(config.getString(path + ".Firework", ""));
        bundle.setMoney(config.getDouble(path + ".Money", 0D));
        bundle.setMessages(config.getStringList(path + ".Messages"));
        return bundle;
    }

    private static void writeBundle(FileConfig config, String path, ActionBundle bundle) {
        config.set(path + ".ConsoleCommands", bundle.getConsoleCommands());
        config.set(path + ".PlayerCommands", bundle.getPlayerCommands());
        config.set(path + ".Sound", bundle.getSound());
        config.set(path + ".Particle", bundle.getParticle());
        config.set(path + ".Firework", bundle.getFirework());
        config.set(path + ".Money", bundle.getMoney());
        config.set(path + ".Messages", bundle.getMessages());
    }

    private static Set<DisplayChannel> readChannels(List<String> names) {
        Set<DisplayChannel> set = new LinkedHashSet<>();
        if (names == null) return set;
        for (String name : names) {
            DisplayChannel channel = parseEnum(name, DisplayChannel.class, null);
            if (channel != null) set.add(channel);
        }
        return set;
    }

    private static <E extends Enum<E>> E parseEnum(String raw, Class<E> type, E fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        }
        catch (Exception ignored) {
            return fallback;
        }
    }

    private static String sanitizeKey(String raw, String fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        String key = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
        return key.isBlank() ? fallback : key;
    }
}
