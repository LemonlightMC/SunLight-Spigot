package su.nightexpress.sunlight.moduleImpl.timer.dialog.impl;

import org.bukkit.entity.Player;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.model.DisplayChannel;
import su.nightexpress.sunlight.moduleImpl.timer.model.DisplayRule;
import su.nightexpress.sunlight.moduleImpl.timer.model.RuleConditionType;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;

import java.util.*;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerRuleDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Rule.Title").text("Timer Display Rules");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Rule.Body").dialogElement(400,
            GRAY.wrap("Staged display editor. Format: <add|set|remove|clear> [index] [condition] [value|min] [max] [channels] [text]. Conditions: ALWAYS,RUNNING,PAUSED,REMAINING_LTE,REMAINING_GT,REMAINING_BETWEEN,ELAPSED_GTE,ELAPSED_BETWEEN. Channels comma list: ACTION_BAR,BOSS_BAR,TITLE,SUBTITLE,XP_BAR,CHAT. Example: add 20 REMAINING_GT 5 ACTION_BAR,BOSS_BAR %timer_name%: %timer_formatted% || ||"));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Rule.Input").text("command...");
    private final TimerModule module;
    public TimerRuleDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        String initial = "add 10 REMAINING_BETWEEN 0 5 CHAT %timer_name%: %timer_seconds%s";
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(512).width(450).initial(initial).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String raw = nbt.getText("value").orElse("").trim();
                    String[] parts = raw.split("\\s+");
                    if (parts.length == 0 || raw.isBlank()) { viewer.callback(); return; }
                    String cmd = parts[0].toLowerCase(Locale.ROOT);
                    List<DisplayRule> rules = timer.getDefinition().getDisplayRules();
                    if (cmd.equals("clear")) {
                        rules.clear();
                    }
                    else if (cmd.equals("remove") && parts.length >= 2) {
                        try { int idx = Integer.parseInt(parts[1]); if (idx >= 0 && idx < rules.size()) rules.remove(idx); } catch (Exception ignored) {}
                    }
                    else if ((cmd.equals("add") || cmd.equals("set")) && parts.length >= 6) {
                        try {
                            int priority = Integer.parseInt(parts[1]);
                            RuleConditionType condition = RuleConditionType.valueOf(parts[2].toUpperCase(Locale.ROOT));
                            int cursor = 3;
                            long value = 0, min = 0, max = 0;
                            if (condition == RuleConditionType.REMAINING_BETWEEN || condition == RuleConditionType.ELAPSED_BETWEEN) {
                                min = Long.parseLong(parts[cursor++]); max = Long.parseLong(parts[cursor++]);
                            }
                            else if (condition != RuleConditionType.ALWAYS && condition != RuleConditionType.RUNNING && condition != RuleConditionType.PAUSED) {
                                value = Long.parseLong(parts[cursor++]);
                            }
                            Set<DisplayChannel> channels = new LinkedHashSet<>();
                            for (String c : parts[cursor++].split(",")) {
                                try { channels.add(DisplayChannel.valueOf(c.trim().toUpperCase(Locale.ROOT))); } catch (Exception ignored) {}
                            }
                            String text = String.join(" ", Arrays.copyOfRange(parts, Math.min(cursor, parts.length), parts.length));
                            String[] texts = text.split("\\|\\|", -1);
                            DisplayRule rule = cmd.equals("set") && parts.length > 6 ? null : new DisplayRule();
                            if (rule == null) rule = new DisplayRule();
                            rule.setId("rule_" + rules.size());
                            rule.setPriority(priority);
                            rule.setCondition(condition);
                            rule.setValue(value); rule.setMin(min); rule.setMax(max);
                            rule.setChannels(channels);
                            rule.setActionBar(texts.length > 0 ? texts[0].trim() : "");
                            rule.setBossBar(texts.length > 1 ? texts[1].trim() : (channels.contains(DisplayChannel.BOSS_BAR) ? texts[0].trim() : ""));
                            rule.setTitle(texts.length > 2 ? texts[2].trim() : "");
                            rule.setSubtitle(texts.length > 3 ? texts[3].trim() : "");
                            rule.setChat(channels.contains(DisplayChannel.CHAT) ? (texts.length > 0 ? texts[0].trim() : "") : (texts.length > 4 ? texts[4].trim() : ""));
                            if (channels.contains(DisplayChannel.BOSS_BAR) && rule.getBossBar().isBlank()) rule.setBossBar(rule.getActionBar());
                            rules.add(rule);
                        }
                        catch (Exception ignored) {
                        }
                    }
                    this.module.engine().persist(timer, timer.isGlobal());
                    this.module.engine().displays().clearTimer(timer);
                    viewer.callback();
                }).build();
    }
}
