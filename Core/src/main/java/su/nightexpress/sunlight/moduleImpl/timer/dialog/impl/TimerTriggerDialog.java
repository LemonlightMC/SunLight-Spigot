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
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerEventTrigger;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;

import java.util.Locale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerTriggerDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Trigger.Title").text("Timer Stop/Event Triggers");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Trigger.Body").dialogElement(400,
            GRAY.wrap("Format: <add|clear> [type] [entityType] [action]. Types: PLAYER_DEATH,PLAYER_QUIT,ENTITY_DEATH. Actions: COMPLETE,STOP,PAUSE,RESET. Example: add ENTITY_DEATH ENDER_DRAGON COMPLETE"));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Trigger.Input").text("add/clear type entity action");
    private final TimerModule module;
    public TimerTriggerDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(128).width(360).initial("add ENTITY_DEATH ENDER_DRAGON COMPLETE").build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String[] parts = nbt.getText("value").orElse("").trim().split("\\s+");
                    if (parts.length == 0) { viewer.callback(); return; }
                    if ("clear".equalsIgnoreCase(parts[0])) {
                        timer.getDefinition().getStopEvents().clear();
                    }
                    else if ("add".equalsIgnoreCase(parts[0]) && parts.length >= 2) {
                        TimerEventTrigger trigger = new TimerEventTrigger();
                        trigger.setType(parts[1].toUpperCase(Locale.ROOT));
                        trigger.setEntityType(parts.length >= 3 ? parts[2].toUpperCase(Locale.ROOT) : "");
                        trigger.setAction(parts.length >= 4 ? parts[3].toUpperCase(Locale.ROOT) : (parts.length == 3 && !isAction(parts[2]) ? "COMPLETE" : parts.length >= 3 ? parts[2].toUpperCase(Locale.ROOT) : "COMPLETE"));
                        if (parts.length == 3 && isAction(parts[2])) { trigger.setEntityType(""); trigger.setAction(parts[2].toUpperCase(Locale.ROOT)); }
                        timer.getDefinition().getStopEvents().add(trigger);
                    }
                    this.module.engine().persist(timer, timer.isGlobal());
                    viewer.callback();
                }).build();
    }

    private static boolean isAction(String raw) {
        return raw != null && switch (raw.toUpperCase(Locale.ROOT)) { case "COMPLETE","STOP","PAUSE","RESET" -> true; default -> false; };
    }
}
