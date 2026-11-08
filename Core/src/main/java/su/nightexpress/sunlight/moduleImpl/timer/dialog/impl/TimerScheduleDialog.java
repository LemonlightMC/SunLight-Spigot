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
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerStartMode;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerInput;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;

public class TimerScheduleDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Schedule.Title").text("Timer Schedule / Start Mode");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Schedule.Body").dialogElement(430,
            GRAY.wrap("Format: <startMode> [datetime|+30m] [zone] [autoStart] [runMissed]. Start modes: manual, start_now, scheduled. Datetime accepts yyyy-MM-dd HH:mm[:ss] or relative +30m. Example: scheduled 2026-12-31 20:00:00 UTC true false"));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Schedule.Input").text("startMode [datetime/+dur] [zone] [autoStart] [runMissed]");
    private final TimerModule module;
    public TimerScheduleDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        TimerSchedule s = timer.getDefinition().getSchedule();
        TimerStartMode mode = timer.getDefinition().getStartMode();
        String initial = mode.name().toLowerCase() + " " + (s.getStartAt().isBlank() ? "+30m" : s.getStartAt()) + " " + s.getZoneId() + " " + s.isAutoStart() + " " + s.isRunMissedOnLoad();
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(220).width(430).initial(initial).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String raw = nbt.getText("value").orElse("").trim();
                    String[] parts = raw.split("\\s+");
                    if (parts.length == 0 || raw.isBlank()) { viewer.callback(); return; }
                    TimerStartMode startMode = TimerStartMode.parse(parts[0]);
                    timer.getDefinition().setStartMode(startMode);
                    TimerSchedule schedule = timer.getDefinition().getSchedule();
                    schedule.setEnabled(startMode == TimerStartMode.SCHEDULED);
                    if (parts.length >= 2) {
                        // parse datetime tokens; support "yyyy-MM-dd HH:mm[:ss] zone ..." or "+30m zone ..."
                        String dt = parts[1];
                        int zoneIdx = 2;
                        if (parts.length >= 3 && parts[2].contains(":") && !parts[2].startsWith("+")) { dt = parts[1] + " " + parts[2]; zoneIdx = 3; }
                        if (parts.length > zoneIdx) {
                            try { ZoneId.of(parts[zoneIdx]); schedule.setZoneId(parts[zoneIdx]); } catch (Exception ignored) {}
                        }
                        Optional<ZonedDateTime> parsed = TimerInput.parseDateTime(dt, TimerInput.parseZone(schedule.getZoneId()));
                        if (parsed.isPresent()) {
                            schedule.setStartAt(parsed.get().toLocalDateTime().toString().replace('T', ' '));
                        } else if (!dt.startsWith("+") && !dt.equalsIgnoreCase("manual") && !dt.equalsIgnoreCase("now")) {
                            schedule.setStartAt(dt);
                        }
                        if (parts.length > zoneIdx + 1) schedule.setAutoStart(Boolean.parseBoolean(parts[zoneIdx + 1]));
                        if (parts.length > zoneIdx + 2) schedule.setRunMissedOnLoad(Boolean.parseBoolean(parts[zoneIdx + 2]));
                    }
                    timer.setScheduleFired(false);
                    timer.setScheduledStartEpoch(0L);
                    this.module.engine().persist(timer, timer.isGlobal());
                    viewer.callback();
                }).build();
    }
}
