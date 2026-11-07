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

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerNameDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Name.Title").text("Timer Name");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Name.Body").dialogElement(400, GRAY.wrap("Set the display name for this timer."));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Name.Input").text("Name");
    private final TimerModule module;
    public TimerNameDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(64).width(300).initial(timer.getDefinition().getName()).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String value = nbt.getText("value").orElse("").trim();
                    if (!value.isBlank()) {
                        timer.getDefinition().setName(value);
                        this.module.engine().persist(timer, timer.isGlobal());
                    }
                    viewer.callback();
                }).build();
    }
}
