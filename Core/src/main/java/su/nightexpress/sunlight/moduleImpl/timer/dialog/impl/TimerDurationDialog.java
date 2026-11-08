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
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerKind;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerInput;

import java.util.Locale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerDurationDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Duration.Title").text("Timer Duration / Kind");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Duration.Body").dialogElement(400,
            GRAY.wrap("Format: <kind> <duration> [stopwatchTarget:true|false] [startMode]. Kind: countdown/stopwatch. Duration accepts 1h30m, 90s, 10:00. Stopwatch target fires onStopwatchEnd at duration. Start modes: manual, start_now, scheduled."));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Duration.Input").text("kind duration [target] [startMode]");
    private final TimerModule module;
    public TimerDurationDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        String initial = timer.getDefinition().getKind().name().toLowerCase(Locale.ROOT) + " " + TimerInput.formatForEdit(timer.getDefinition().getDurationSeconds()) + " " + timer.getDefinition().isUseStopwatchTarget() + " " + timer.getDefinition().getStartMode().name().toLowerCase(Locale.ROOT);
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(64).width(320).initial(initial).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String[] parts = nbt.getText("value").orElse("").trim().split("\\s+");
                    if (parts.length >= 1) timer.getDefinition().setKind("stopwatch".equalsIgnoreCase(parts[0]) ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN);
                    if (parts.length >= 2) TimerInput.parsePositiveSeconds(parts[1]).ifPresent(sec -> timer.getDefinition().setDurationSeconds(sec));
                    if (parts.length >= 3) timer.getDefinition().setUseStopwatchTarget(Boolean.parseBoolean(parts[2]) || "target".equalsIgnoreCase(parts[2]));
                    if (parts.length >= 4) timer.getDefinition().setStartMode(su.nightexpress.sunlight.moduleImpl.timer.model.TimerStartMode.parse(parts[3]));
                    timer.resetRuntime();
                    this.module.engine().persist(timer, timer.isGlobal());
                    viewer.callback();
                }).build();
    }
}
