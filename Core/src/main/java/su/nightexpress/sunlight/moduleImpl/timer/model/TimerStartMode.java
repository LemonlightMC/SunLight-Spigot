package su.nightexpress.sunlight.moduleImpl.timer.model;

import java.util.Locale;

public enum TimerStartMode {
    MANUAL,
    START_NOW,
    SCHEDULED;

    public static TimerStartMode parse(String raw) {
        if (raw == null || raw.isBlank()) return MANUAL;
        try {
            String v = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
            if (v.equals("NOW") || v.equals("START")) return START_NOW;
            if (v.equals("SCHEDULE") || v.equals("DATE") || v.equals("DATETIME")) return SCHEDULED;
            return valueOf(v);
        }
        catch (Exception ignored) {
            return MANUAL;
        }
    }
}
