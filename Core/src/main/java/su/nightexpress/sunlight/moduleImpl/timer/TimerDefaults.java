package su.nightexpress.sunlight.moduleImpl.timer;

import su.nightexpress.sunlight.moduleImpl.timer.model.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class TimerDefaults {

    private TimerDefaults() {
    }

    public static List<TimerDefinition> createDefaults() {
        List<TimerDefinition> list = new ArrayList<>();
        list.add(exampleCountdown());
        return list;
    }

    public static TimerDefinition exampleCountdown() {
        TimerDefinition def = new TimerDefinition();
        def.setId("event_start");
        def.setName("Event Start");
        def.setScope(TimerScope.GLOBAL);
        def.setKind(TimerKind.COUNTDOWN);
        def.setDurationSeconds(30L);
        def.setVisibility(new TimerVisibility(VisibilityMode.ALL, ""));

        DisplayRule main = new DisplayRule();
        main.setId("main");
        main.setPriority(10);
        main.setCondition(RuleConditionType.REMAINING_GT);
        main.setValue(5L);
        main.setChannels(new LinkedHashSet<>(List.of(DisplayChannel.BOSS_BAR, DisplayChannel.ACTION_BAR)));
        main.setBossBar("<yellow>Event starts in <white>%timer_formatted%</white></yellow>");
        main.setActionBar("<gray>Starting in <yellow>%timer_formatted%</yellow></gray>");
        main.setBossBarColor("YELLOW");
        main.setBossBarOverlay("PROGRESS");

        DisplayRule finalCount = new DisplayRule();
        finalCount.setId("final");
        finalCount.setPriority(20);
        finalCount.setCondition(RuleConditionType.REMAINING_BETWEEN);
        finalCount.setMin(0L);
        finalCount.setMax(5L);
        finalCount.setChannels(new LinkedHashSet<>(List.of(DisplayChannel.CHAT, DisplayChannel.TITLE, DisplayChannel.SUBTITLE)));
        finalCount.setChat("<yellow>Event starts in <white>%timer_seconds%s</white>!</yellow>");
        finalCount.setChatIntervalSeconds(1);
        finalCount.setTitle("<gold><bold>%timer_seconds%</bold></gold>");
        finalCount.setSubtitle("<gray>Event starting...</gray>");
        finalCount.setUpdateIntervalTicks(20);

        def.setDisplayRules(new ArrayList<>(List.of(main, finalCount)));

        ActionBundle complete = new ActionBundle();
        complete.setMessages(new ArrayList<>(List.of("<green><bold>GO!</bold></green> <gray>The event has started.</gray>")));
        complete.setSound("ENTITY_PLAYER_LEVELUP;1;1");
        def.getActions().setOnComplete(complete);
        return def;
    }

    public static TimerDefinition createPersonalDefault(String id, TimerKind kind, long seconds) {
        TimerDefinition def = new TimerDefinition();
        def.setId(id);
        def.setName(id);
        def.setScope(TimerScope.PERSONAL);
        def.setKind(kind);
        def.setDurationSeconds(Math.max(1L, seconds));
        def.setVisibility(new TimerVisibility(VisibilityMode.SELF, ""));

        DisplayRule rule = new DisplayRule();
        rule.setId("personal");
        rule.setPriority(10);
        rule.setCondition(RuleConditionType.ALWAYS);
        rule.setChannels(new LinkedHashSet<>(List.of(DisplayChannel.ACTION_BAR)));
        if (kind == TimerKind.COUNTDOWN) {
            rule.setActionBar("<gray>%timer_name%: <yellow>%timer_formatted%</yellow></gray>");
        }
        else {
            rule.setActionBar("<gray>%timer_name%: <green>%timer_formatted%</green></gray>");
        }
        def.setDisplayRules(new ArrayList<>(List.of(rule)));
        return def;
    }
}
