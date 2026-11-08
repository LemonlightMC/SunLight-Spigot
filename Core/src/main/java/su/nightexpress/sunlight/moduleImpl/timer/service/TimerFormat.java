package su.nightexpress.sunlight.moduleImpl.timer.service;

import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerKind;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class TimerFormat {

    private TimerFormat() {
    }

    public static String format(TimerInstance instance) {
        long millis = instance.getDefinition().getKind() == TimerKind.COUNTDOWN ? instance.getRemainingMillis() : instance.getElapsedMillis();
        return formatMillis(millis);
    }

    public static String formatMillis(long millis) {
        long total = Math.max(0L, millis / 1000L);
        long days = TimeUnit.SECONDS.toDays(total);
        long hours = TimeUnit.SECONDS.toHours(total) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(total) % 60;
        long seconds = total % 60;
        if (days > 0) return String.format(Locale.ROOT, "%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        if (hours > 0) return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    public static String apply(String text, TimerInstance instance) {
        if (text == null || text.isEmpty()) return "";
        long remaining = Math.max(0L, instance.getRemainingMillis() / 1000L);
        long elapsed = Math.max(0L, instance.getElapsedMillis() / 1000L);
        long main = instance.getDefinition().getKind() == TimerKind.COUNTDOWN ? remaining : elapsed;
        long days = main / 86400L;
        long hours = (main % 86400L) / 3600L;
        long minutes = (main % 3600L) / 60L;
        long seconds = main % 60L;
        double progress = progress(instance);
        String out = text;
        out = out.replace("%timer_id%", instance.getId());
        out = out.replace("%timer_name%", instance.getDefinition().getName());
        out = out.replace("%timer_kind%", instance.getDefinition().getKind().name());
        out = out.replace("%timer_scope%", instance.getDefinition().getScope().name());
        out = out.replace("%timer_state%", instance.getState().name());
        out = out.replace("%timer_formatted%", format(instance));
        out = out.replace("%timer_remaining%", String.valueOf(remaining));
        out = out.replace("%timer_elapsed%", String.valueOf(elapsed));
        out = out.replace("%timer_days%", String.valueOf(days));
        out = out.replace("%timer_hours%", String.valueOf(hours));
        out = out.replace("%timer_minutes%", String.valueOf(minutes));
        out = out.replace("%timer_seconds%", String.valueOf(seconds));
        out = out.replace("%timer_progress%", NumberUtil.format(progress * 100D));
        out = out.replace("%timer_progress_raw%", String.valueOf(progress));
        out = out.replace("%timer_duration%", formatMillis(instance.getDurationMillis()));
        out = out.replace("%timer_duration_raw%", String.valueOf(Math.max(0L, instance.getDurationMillis() / 1000L)));
        out = out.replace("%timer_max%", String.valueOf(Math.max(0L, instance.getDurationMillis() / 1000L)));
        out = out.replace("%timer_target%", String.valueOf(instance.getDefinition().getDurationSeconds()));
        out = out.replace("%timer_has_target%", String.valueOf(instance.getDefinition().hasStopwatchEndTarget()));
        out = out.replace("%timer_start_mode%", instance.getDefinition().getStartMode().name());
        out = out.replace("%timer_owner%", instance.getOwnerId() == null ? "" : instance.getOwnerId().toString());
        out = out.replace("%timer_schedule_enabled%", String.valueOf(instance.getDefinition().getSchedule().isEnabled()));
        out = out.replace("%timer_schedule_start%", instance.getDefinition().getSchedule().getStartAt() == null ? "" : instance.getDefinition().getSchedule().getStartAt());
        out = out.replace("%timer_schedule_zone%", instance.getDefinition().getSchedule().getZoneId() == null ? "UTC" : instance.getDefinition().getSchedule().getZoneId());
        return out;
    }

    public static double progress(TimerInstance instance) {
        long duration = Math.max(1L, instance.getDurationMillis());
        if (instance.getDefinition().getKind() == TimerKind.COUNTDOWN) {
            return Math.max(0D, Math.min(1D, (double) instance.getRemainingMillis() / (double) duration));
        }
        if (instance.getDefinition().hasStopwatchEndTarget()) {
            return Math.max(0D, Math.min(1D, (double) instance.getElapsedMillis() / (double) duration));
        }
        return 1D;
    }
}
