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
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerLang;
import su.nightexpress.sunlight.moduleImpl.timer.model.ActionBundle;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerActions;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerActionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;
import static su.nightexpress.sunlight.SLPlaceholders.GENERIC_VALUE;

public class TimerActionDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Action.Title").text("Timer Actions");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Action.Body").dialogElement(430,
            GRAY.wrap("Checkpoints: start,pause,resume,complete,stopwatch_end,stop. Format: <checkpoint> <field> <value>. Fields: console(cmd|cmd2), player(...), sound(NAME;v;p), particle(TYPE;count;ox;oy;oz;speed), firework(power;type;color), money(amount), messages(line1||line2), clear(all|field). Invalid sound/particle/firework values are rejected."));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Action.Input").text("checkpoint field value");
    private final TimerModule module;
    public TimerActionDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(512).width(430).initial("complete sound ENTITY_PLAYER_LEVELUP;1;1").build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String raw = nbt.getText("value").orElse("").trim();
                    int first = raw.indexOf(' ');
                    int second = first >= 0 ? raw.indexOf(' ', first + 1) : -1;
                    if (first < 0) { viewer.callback(); return; }
                    String checkpoint = raw.substring(0, first).toLowerCase(Locale.ROOT);
                    String field = second < 0 ? raw.substring(first + 1).trim().toLowerCase(Locale.ROOT) : raw.substring(first + 1, second).toLowerCase(Locale.ROOT);
                    String value = second < 0 ? "" : raw.substring(second + 1).trim();
                    ActionBundle bundle = bundle(timer.getDefinition().getActions(), checkpoint);
                    if ("clear".equals(field)) {
                        if (value.isBlank() || "all".equalsIgnoreCase(value)) {
                            bundle.setConsoleCommands(new ArrayList<>()); bundle.setPlayerCommands(new ArrayList<>()); bundle.setMessages(new ArrayList<>());
                            bundle.setSound(""); bundle.setParticle(""); bundle.setFirework(""); bundle.setMoney(0D);
                        } else applyField(bundle, value.toLowerCase(Locale.ROOT), "");
                        this.module.engine().persist(timer, timer.isGlobal()); viewer.callback(); return;
                    }
                    if (!isValidField(field, value)) {
                        this.module.sendPrefixed(TimerLang.COMMAND_INVALID_ACTION, player, b -> b.with(GENERIC_VALUE, () -> field + " " + value));
                        viewer.callback();
                        return;
                    }
                    applyField(bundle, field, value);
                    TimerActionService.ValidationResult validation = this.module.engine().actions().validate(bundle);
                    if (!validation.ok()) {
                        this.module.sendPrefixed(TimerLang.COMMAND_INVALID_ACTION, player, b -> b.with(GENERIC_VALUE, () -> String.join("; ", validation.errors())));
                    }
                    this.module.engine().persist(timer, timer.isGlobal());
                    viewer.callback();
                }).build();
    }

    private static void applyField(ActionBundle bundle, String field, String value) {
        switch (field) {
            case "console", "consolecommands" -> bundle.setConsoleCommands(split(value, "|", ";;"));
            case "player", "playercommands" -> bundle.setPlayerCommands(split(value, "|", ";;"));
            case "sound" -> bundle.setSound(value);
            case "particle" -> bundle.setParticle(value);
            case "firework" -> bundle.setFirework(value);
            case "money" -> { try { bundle.setMoney(Double.parseDouble(value)); } catch (Exception ignored) {} }
            case "messages" -> bundle.setMessages(split(value, "||", "|"));
            default -> {}
        }
    }

    private static boolean isValidField(String field, String value) {
        if (value == null) return false;
        return switch (field) {
            case "sound" -> value.isBlank() || isValidSoundSpec(value);
            case "particle" -> value.isBlank() || isValidParticleSpec(value);
            case "firework" -> value.isBlank() || isValidFireworkSpec(value);
            case "money" -> { try { Double.parseDouble(value); yield true; } catch (Exception e) { yield false; } }
            default -> true;
        };
    }

    private static boolean isValidSoundSpec(String raw) {
        try { String[] p = raw.split(";"); if (p[0].trim().isBlank()) return false; if (p.length > 1) Float.parseFloat(p[1]); if (p.length > 2) Float.parseFloat(p[2]); return true; } catch (Exception e) { return false; }
    }
    private static boolean isValidParticleSpec(String raw) {
        try { String[] p = raw.split(";"); org.bukkit.Particle.valueOf(p[0].trim().toUpperCase(Locale.ROOT)); for (int i = 1; i < Math.min(p.length, 6); i++) Double.parseDouble(p[i]); return true; } catch (Exception e) { return false; }
    }
    private static boolean isValidFireworkSpec(String raw) {
        try { String[] p = raw.split(";"); if (p.length > 0 && !p[0].isBlank()) Integer.parseInt(p[0].trim()); if (p.length > 1 && !p[1].isBlank()) org.bukkit.FireworkEffect.Type.valueOf(p[1].trim().toUpperCase(Locale.ROOT)); return true; } catch (Exception e) { return false; }
    }

    private static ActionBundle bundle(TimerActions actions, String checkpoint) {
        return switch (checkpoint) {
            case "start" -> actions.getOnStart();
            case "pause" -> actions.getOnPause();
            case "resume" -> actions.getOnResume();
            case "stopwatch_end", "stopwatch" -> actions.getOnStopwatchEnd();
            case "stop" -> actions.getOnStop();
            default -> actions.getOnComplete();
        };
    }

    private static List<String> split(String value, String primary, String secondary) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        String sep = value.contains(primary) ? java.util.regex.Pattern.quote(primary) : java.util.regex.Pattern.quote(secondary);
        return new ArrayList<>(Arrays.stream(value.split(sep)).map(String::trim).filter(s -> !s.isEmpty()).toList());
    }
}
