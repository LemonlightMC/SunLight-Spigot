package su.nightexpress.sunlight.moduleImpl.timer.service;

import su.nightexpress.sunlight.utils.TimeUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

public final class TimerInput {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    private TimerInput() {
    }

    public static OptionalLong parseSeconds(String raw) {
        if (raw == null || raw.isBlank()) return OptionalLong.empty();
        String input = raw.trim().toLowerCase(Locale.ROOT);
        // Support hh:mm:ss and mm:ss
        if (input.matches("\\d+:\\d{1,2}(:\\d{1,2})?")) {
            String[] parts = input.split(":");
            try {
                long total = 0L;
                if (parts.length == 3) total = Long.parseLong(parts[0]) * 3600L + Long.parseLong(parts[1]) * 60L + Long.parseLong(parts[2]);
                else total = Long.parseLong(parts[0]) * 60L + Long.parseLong(parts[1]);
                return total >= 0L ? OptionalLong.of(total) : OptionalLong.empty();
            }
            catch (Exception ignored) {
                return OptionalLong.empty();
            }
        }
        long seconds = TimeUtil.parseDurationSeconds(raw);
        if (seconds < 0L) return OptionalLong.empty();
        return OptionalLong.of(seconds);
    }

    public static OptionalLong parsePositiveSeconds(String raw) {
        OptionalLong parsed = parseSeconds(raw);
        if (parsed.isEmpty() || parsed.getAsLong() <= 0L) return OptionalLong.empty();
        return parsed;
    }

    public static Optional<ZonedDateTime> parseDateTime(String raw, ZoneId zone) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String input = raw.trim();
        if (input.startsWith("+") || input.toLowerCase(Locale.ROOT).startsWith("in ")) {
            String rel = input.startsWith("+") ? input.substring(1) : input.substring(3);
            OptionalLong seconds = parsePositiveSeconds(rel);
            if (seconds.isPresent()) return Optional.of(ZonedDateTime.now(zone).plusSeconds(seconds.getAsLong()));
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                LocalDateTime local = LocalDateTime.parse(input, formatter);
                return Optional.of(local.atZone(zone));
            }
            catch (DateTimeParseException ignored) {
            }
        }
        return Optional.empty();
    }

    public static ZoneId parseZone(String raw) {
        if (raw == null || raw.isBlank()) return ZoneId.of("UTC");
        try { return ZoneId.of(raw.trim()); } catch (Exception ignored) { return ZoneId.of("UTC"); }
    }

    public static String formatForEdit(long seconds) {
        if (seconds % 3600L == 0L && seconds >= 3600L) return (seconds / 3600L) + "h";
        if (seconds % 60L == 0L && seconds >= 60L) return (seconds / 60L) + "m";
        return seconds + "s";
    }
}
