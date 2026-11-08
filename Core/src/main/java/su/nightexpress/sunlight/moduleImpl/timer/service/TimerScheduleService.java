package su.nightexpress.sunlight.moduleImpl.timer.service;

import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerSchedule;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerStartMode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TimerScheduleService {

    private static final List<DateTimeFormatter> FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    public Long resolveStartEpoch(TimerSchedule schedule) {
        if (schedule == null || !schedule.isEnabled()) return null;
        String raw = schedule.getStartAt();
        if (raw == null || raw.isBlank()) return null;
        ZoneId zone;
        try {
            zone = ZoneId.of(schedule.getZoneId());
        }
        catch (Exception ignored) {
            zone = ZoneId.of("UTC");
        }
        for (DateTimeFormatter formatter : FORMATS) {
            try {
                LocalDateTime local = LocalDateTime.parse(raw.trim(), formatter);
                ZonedDateTime zoned = local.atZone(zone);
                return zoned.toInstant().toEpochMilli();
            }
            catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public boolean shouldAutoStart(TimerInstance instance) {
        TimerStartMode mode = instance.getDefinition().getStartMode();
        if (mode == TimerStartMode.MANUAL) return false;
        if (instance.isScheduleFired()) return false;
        if (instance.isRunning() || instance.isPaused() || instance.isCompleted()) return false;

        if (mode == TimerStartMode.START_NOW) {
            return true;
        }

        TimerSchedule schedule = instance.getDefinition().getSchedule();
        if (schedule == null || !schedule.isEnabled() || !schedule.isAutoStart()) return false;
        Long epoch = this.resolveStartEpoch(schedule);
        if (epoch == null) return false;
        instance.setScheduledStartEpoch(epoch);
        long now = System.currentTimeMillis();
        if (now < epoch) return false;
        return schedule.isRunMissedOnLoad() || now - epoch < 60_000L || now >= epoch;
    }
}
