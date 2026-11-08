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
import java.util.stream.Collectors;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerRuleDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Rule.Title").text("Timer Display Rules");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Rule.Body").dialogElement(430,
            GRAY.wrap("Commands: list | clear | remove <index> | add <priority> <condition> [value|min max] <channels> <actionbar||bossbar||title||subtitle||chat||scoreTitle||scoreLine1\\nscoreLine2>. Conditions: ALWAYS,RUNNING,PAUSED,REMAINING_LTE,REMAINING_GT,REMAINING_BETWEEN,ELAPSED_GTE,ELAPSED_BETWEEN. Channels: ACTION_BAR,BOSS_BAR,TITLE,SUBTITLE,XP_BAR,CHAT,SCOREBOARD. Example 5->0 chat: add 20 REMAINING_BETWEEN 0 5 CHAT,TITLE %timer_seconds%s|| ||%timer_seconds%||Starting..."));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Rule.Input").text("list|clear|remove|add ...");
    private final TimerModule module;
    public TimerRuleDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        String ids = timer.getDefinition().getDisplayRules().stream().map(r -> r.getId() + "[" + r.getCondition() + "/" + r.getPriority() + "]").collect(Collectors.joining(", "));
        String initial = ids.isBlank() ? "add 10 ALWAYS 0 ACTION_BAR %timer_name%: %timer_formatted%" : "list(" + timer.getDefinition().getDisplayRules().size() + ": " + ids + ")";
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(700).width(460).initial(initial.length() > 600 ? initial.substring(0, 600) : initial).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String raw = nbt.getText("value").orElse("").trim();
                    String[] parts = raw.split("\\s+");
                    if (parts.length == 0 || raw.isBlank() || raw.toLowerCase(Locale.ROOT).startsWith("list")) { viewer.callback(); return; }
                    String cmd = parts[0].toLowerCase(Locale.ROOT);
                    List<DisplayRule> rules = timer.getDefinition().getDisplayRules();
                    if (cmd.equals("clear")) {
                        rules.clear();
                    }
                    else if ((cmd.equals("remove") || cmd.equals("del") || cmd.equals("delete")) && parts.length >= 2) {
                        try {
                            int idx = Integer.parseInt(parts[1]);
                            if (idx >= 0 && idx < rules.size()) rules.remove(idx);
                            else rules.removeIf(r -> r.getId().equalsIgnoreCase(parts[1]));
                        } catch (Exception ignored) {
                            rules.removeIf(r -> r.getId().equalsIgnoreCase(parts[1]));
                        }
                    }
                    else if ((cmd.equals("add") || cmd.equals("set") || cmd.equals("insert")) && parts.length >= 5) {
                        try {
                            int index = cmd.equals("insert") && parts.length >= 7 ? Integer.parseInt(parts[1]) : -1;
                            int offset = index >= 0 ? 1 : 0;
                            int priority = Integer.parseInt(parts[1 + offset]);
                            RuleConditionType condition = RuleConditionType.valueOf(parts[2 + offset].toUpperCase(Locale.ROOT));
                            int cursor = 3 + offset;
                            long value = 0, min = 0, max = 0;
                            if (condition == RuleConditionType.REMAINING_BETWEEN || condition == RuleConditionType.ELAPSED_BETWEEN) {
                                min = Long.parseLong(parts[cursor++]); max = Long.parseLong(parts[cursor++]);
                            }
                            else if (condition != RuleConditionType.ALWAYS && condition != RuleConditionType.RUNNING && condition != RuleConditionType.PAUSED) {
                                value = Long.parseLong(parts[cursor++]);
                            }
                            Set<DisplayChannel> channels = new LinkedHashSet<>();
                            if (cursor < parts.length) {
                                for (String c : parts[cursor++].split(",")) {
                                    try { channels.add(DisplayChannel.valueOf(c.trim().toUpperCase(Locale.ROOT))); } catch (Exception ignored) {}
                                }
                            }
                            String text = cursor <= parts.length ? String.join(" ", Arrays.copyOfRange(parts, Math.min(cursor, parts.length), parts.length)) : "";
                            String[] texts = text.split("\\|\\|", -1);
                            DisplayRule rule = new DisplayRule();
                            rule.setId("rule_" + (rules.size() + 1));
                            rule.setPriority(priority);
                            rule.setCondition(condition);
                            rule.setValue(value); rule.setMin(min); rule.setMax(max);
                            rule.setChannels(channels);
                            rule.setActionBar(texts.length > 0 ? texts[0].trim() : "");
                            rule.setBossBar(texts.length > 1 ? texts[1].trim() : (channels.contains(DisplayChannel.BOSS_BAR) ? texts[0].trim() : ""));
                            rule.setTitle(texts.length > 2 ? texts[2].trim() : "");
                            rule.setSubtitle(texts.length > 3 ? texts[3].trim() : "");
                            rule.setChat(channels.contains(DisplayChannel.CHAT) ? (texts.length > 4 && !texts[4].trim().isBlank() ? texts[4].trim() : texts[0].trim()) : (texts.length > 4 ? texts[4].trim() : ""));
                            if (channels.contains(DisplayChannel.SCOREBOARD)) {
                                rule.setScoreboardTitle(texts.length > 5 ? texts[5].trim() : (texts[0].trim().isBlank() ? "%timer_name%" : texts[0].trim()));
                                if (texts.length > 6) {
                                    List<String> lines = new ArrayList<>();
                                    for (int i = 6; i < texts.length; i++) {
                                        String block = texts[i].replace("\\n", "\n");
                                        for (String line : block.split("\n")) lines.add(line);
                                    }
                                    rule.setScoreboardLines(lines);
                                } else {
                                    rule.setScoreboardLines(new ArrayList<>(List.of("%timer_name%", "%timer_formatted%")));
                                }
                            }
                            if (channels.contains(DisplayChannel.BOSS_BAR) && rule.getBossBar().isBlank()) rule.setBossBar(rule.getActionBar());
                            if (index >= 0 && index <= rules.size()) rules.add(index, rule); else rules.add(rule);
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
