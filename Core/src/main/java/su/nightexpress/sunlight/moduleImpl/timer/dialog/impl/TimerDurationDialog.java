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

import java.util.Locale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerDurationDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Duration.Title").text("Timer Duration / Kind");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Duration.Body").dialogElement(400,
            GRAY.wrap("Format: <kind> <seconds> [useStopwatchTarget:true|false]. Kind: countdown/stopwatch. For stopwatch, target seconds is optional and enables onStopwatchEnd when reached."));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Duration.Input").text("kind seconds [target]");
    private final TimerModule module;
    public TimerDurationDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        String initial = timer.getDefinition().getKind().name().toLowerCase(Locale.ROOT) + " " + timer.getDefinition().getDurationSeconds() + " " + timer.getDefinition().isUseStopwatchTarget();
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(64).width(320).initial(initial).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String[] parts = nbt.getText("value").orElse("").trim().split("\\s+");
                    if (parts.length >= 1) timer.getDefinition().setKind("stopwatch".equalsIgnoreCase(parts[0]) ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN);
                    if (parts.length >= 2) try { timer.getDefinition().setDurationSeconds(Math.max(1L, Long.parseLong(parts[1]))); } catch (Exception ignored) {}
                    if (parts.length >= 3) timer.getDefinition().setUseStopwatchTarget(Boolean.parseBoolean(parts[2]));
                    timer.resetRuntime();
                    this.module.engine().persist(timer, timer.isGlobal());
                    viewer.callback();
                }).build();
    }
}
