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
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerSchedule;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerScheduleDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Schedule.Title").text("Timer Schedule");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Schedule.Body").dialogElement(400,
            GRAY.wrap("Start-at-datetime only (no recurring). Format: <enabled> <yyyy-MM-dd HH:mm[:ss]> <zone> <autoStart> <runMissed>. Example: true 2026-12-31 20:00:00 UTC true false"));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Schedule.Input").text("enabled datetime zone autoStart runMissed");
    private final TimerModule module;
    public TimerScheduleDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        TimerSchedule s = timer.getDefinition().getSchedule();
        String initial = s.isEnabled() + " " + (s.getStartAt().isBlank() ? "2026-01-01 00:00:00" : s.getStartAt()) + " " + s.getZoneId() + " " + s.isAutoStart() + " " + s.isRunMissedOnLoad();
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(160).width(420).initial(initial).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String raw = nbt.getText("value").orElse("").trim();
                    String[] parts = raw.split("\\s+");
                    if (parts.length >= 5) {
                        TimerSchedule schedule = timer.getDefinition().getSchedule();
                        schedule.setEnabled(Boolean.parseBoolean(parts[0]));
                        String dt = parts[1];
                        if (parts.length >= 6 && parts[2].contains(":")) { dt = parts[1] + " " + parts[2]; schedule.setZoneId(parts[3]); schedule.setAutoStart(Boolean.parseBoolean(parts[4])); schedule.setRunMissedOnLoad(Boolean.parseBoolean(parts[5])); }
                        else { schedule.setZoneId(parts.length > 2 ? parts[2] : "UTC"); schedule.setAutoStart(Boolean.parseBoolean(parts.length > 3 ? parts[3] : "true")); schedule.setRunMissedOnLoad(Boolean.parseBoolean(parts.length > 4 ? parts[4] : "false")); }
                        schedule.setStartAt(dt);
                        timer.setScheduleFired(false);
                        this.module.engine().persist(timer, timer.isGlobal());
                    }
                    viewer.callback();
                }).build();
    }
}
