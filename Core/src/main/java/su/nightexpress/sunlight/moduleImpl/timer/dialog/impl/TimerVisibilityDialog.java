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
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerVisibility;
import su.nightexpress.sunlight.moduleImpl.timer.model.VisibilityMode;

import java.util.Locale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerVisibilityDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Visibility.Title").text("Timer Visibility");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Visibility.Body").dialogElement(400,
            GRAY.wrap("Format: <ALL|PERMISSION|SELF> [permission]. Personal timers are usually SELF."));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Visibility.Input").text("mode [permission]");
    private final TimerModule module;
    public TimerVisibilityDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        TimerVisibility v = timer.getDefinition().getVisibility();
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(128).width(320).initial(v.getMode().name() + (v.getPermission().isBlank() ? "" : " " + v.getPermission())).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String[] parts = nbt.getText("value").orElse("").trim().split("\\s+", 2);
                    VisibilityMode mode = VisibilityMode.ALL;
                    try { mode = VisibilityMode.valueOf(parts[0].toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
                    timer.getDefinition().setVisibility(new TimerVisibility(mode, parts.length > 1 ? parts[1].trim() : ""));
                    this.module.engine().persist(timer, timer.isGlobal());
                    viewer.callback();
                }).build();
    }
}
