package su.nightexpress.sunlight.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.TimeZone;

import org.bukkit.Bukkit;

import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;

public class TimeUtil {

  /**
   * The zone every date, time and rollover in SunLight is computed in. It is set
   * from the global 'General.Time_Zone' setting and defaults to the server zone,
   * so a plugin that never touches the setting behaves exactly like before.
   */
  private static volatile ZoneId zoneId = ZoneId.systemDefault();

  /**
   * Applies the global zone. Accepts 'system' (or an empty value) for the server
   * zone and any id {@link ZoneId#of(String)} understands. An unparseable id is
   * rejected with a warning instead of silently becoming GMT, which is what
   * {@link TimeZone#getTimeZone(String)} would do.
   */
  public static void setZoneId(String name) {
    if (name == null || name.isBlank() || name.equalsIgnoreCase("system")) {
      zoneId = ZoneId.systemDefault();
      return;
    }

    try {
      zoneId = ZoneId.of(name.trim());
    } catch (Exception exception) {
      zoneId = ZoneId.systemDefault();
      Bukkit.getLogger().warning("[SunLight] Unknown timezone '" + name
          + "' in General.Time_Zone, falling back to the server timezone (" + zoneId + ").");
    }
  }

  /**
   * Resolves a module-level zone name against the global zone, so a module left
   * on 'system' follows the global setting rather than the JVM default.
   *
   * @param name     the configured id, may be null, blank or 'system'
   * @param fallback the zone to use when {@code name} is absent or invalid
   */
  public static ZoneId resolveZoneId(String name, ZoneId fallback) {
    if (name == null || name.isBlank() || name.equalsIgnoreCase("system")) {
      return fallback;
    }

    try {
      return ZoneId.of(name.trim());
    } catch (Exception exception) {
      return fallback;
    }
  }

  public static ZoneId getZoneId() {
    return zoneId;
  }

  public static boolean isPassed(long timestamp) {
    return timestamp >= 0 && System.currentTimeMillis() > timestamp;
  }

  public static long createFutureTimestamp(double seconds) {
    return seconds < 0 ? -1L : System.currentTimeMillis() + (long) (Math.abs(seconds) * 1000D);
  }

  public static LocalDateTime getCurrentDateTime() {
    return LocalDateTime.now(zoneId);
  }

  public static LocalDate getCurrentDate() {
    return LocalDate.now(zoneId);
  }

  public static LocalTime getCurrentTime() {
    return LocalTime.now(zoneId);
  }

  public static LocalDateTime getLocalDateTimeOf(long ms) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zoneId);
  }

  public static long toEpochMillis(LocalDateTime dateTime) {
    return dateTime.atZone(zoneId).toInstant().toEpochMilli();
  }

  /**
   * Formats a timestamp with NightCore's configured date-time pattern, but in
   * SunLight's global zone. NightCore's own {@code TimeFormats#formatDateTime}
   * converts with its engine-level zone, which would ignore this setting.
   */
  public static String formatDateTime(long ms) {
    return TimeFormats.formatDateTime(getLocalDateTimeOf(ms));
  }

  public static String formatMillis(final long ms) {
    if (ms <= 0L)
      return TimeFormats.formatAmount(0L, TimeFormatType.LITERAL);
    return TimeFormats.formatAmount(ms, TimeFormatType.LITERAL);
  }

  public static long parseDurationMillis(final String duration) {
    if (duration == null) {
      return 0L;
    }
    final long seconds = TimeUtil.parseDurationSeconds(duration);
    return seconds <= 0L ? 0L : seconds * 1000L;
  }

  /**
   * Parses a duration typed by a player into seconds.
   * <p>
   * Every configured duration in SunLight is a plain number of seconds, so that
   * stays valid here
   * and means nothing at all. On top of that a suffix may be used — {@code 30s},
   * {@code 5m},
   * {@code 1h}, {@code 7d} — and several may be combined as {@code 1h30m}.
   * <p>
   * 
   * @return the duration in seconds, or {@code -1} when the input is not a
   *         duration at all. The
   *         caller turns that into a usage message rather than a parse error,
   *         because a player
   *         who mistyped needs to be told what is expected.
   */
  public static long parseDurationSeconds(String input) {
    if (input == null) {
      return -1L;
    }

    String text = input.trim().toLowerCase(java.util.Locale.ROOT);
    if (text.isEmpty()) {
      return -1L;
    }

    if (!text.chars().anyMatch(Character::isLetter)) {
      try {
        return Long.parseLong(text);
      } catch (NumberFormatException exception) {
        return -1L;
      }
    }

    long total = 0L;
    long number = 0L;
    boolean sawDigit = false;
    boolean sawUnit = false;

    try {
      for (int i = 0; i < text.length(); i++) {
        char symbol = text.charAt(i);
        if (symbol >= '0' && symbol <= '9') {
          number = Math.addExact(Math.multiplyExact(number, 10L), (symbol - '0'));
          sawDigit = true;
          continue;
        }

        if (!sawDigit) {
          return -1L;
        }

        long unit;
        switch (symbol) {
          case 's' -> unit = 1L;
          case 'm' -> unit = 60L;
          case 'h' -> unit = 3600L;
          case 'd' -> unit = 86400L;
          case 'w' -> unit = 604800L;
          default -> {
            return -1L;
          }
        }

        total = Math.addExact(total, Math.multiplyExact(number, unit));
        number = 0L;
        sawDigit = false;
        sawUnit = true;
      }
    } catch (ArithmeticException overflow) {
      // A value that does not fit in a long is not a usable duration, whatever it was
      // meant as.
      // Without this a huge input wraps around to a small positive number and creates
      // a
      // garbage-duration poll instead of being rejected.
      return -1L;
    }

    // A trailing number with no unit, as in "1h30", is a typo rather than a bare
    // value: mixing
    // the two notations in one string is far more likely to be a mistake than an
    // intention.
    if (sawDigit) {
      return -1L;
    }
    return sawUnit ? total : -1L;
  }
}