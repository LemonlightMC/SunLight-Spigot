package su.nightexpress.sunlight.moduleImpl.playtime.model;

import java.util.List;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.codec.ConfigCodec;
import su.nightexpress.sunlight.utils.TimeUtil;

/**
 * A playtime goal from {@code Playtime.Goals.Periods.<period>}: the duration a
 * player must reach within the period, a reminder window during which the
 * player is notified about the approaching goal, and the reward commands.
 */
public record PlaytimeGoal(long duration, long remainderWindow, List<String> rewards) {

  public PlaytimeGoal(long duration, long remainderWindow, List<String> rewards) {
    this.duration = duration < 0 ? 0 : duration;
    this.remainderWindow = remainderWindow < 0 ? 0 : remainderWindow;
    this.rewards = rewards == null || rewards.isEmpty() ? List.of() : List.copyOf(rewards);
  }

  public PlaytimeGoal(String duration, String remainderWindow, List<String> rewards) {
    this(TimeUtil.parseDurationMillis(duration),
        TimeUtil.parseDurationMillis(remainderWindow), rewards);
  }

  public long duration() {
    return this.duration;
  }

  public long remainderWindow() {
    return this.remainderWindow;
  }

  public PlaytimeGoal withMinutes(final long minutes) {
    return new PlaytimeGoal(minutes * 60 * 1000, this.remainderWindow, this.rewards);
  }

  public static final class PlaytimeGoalCodec implements ConfigCodec<PlaytimeGoal> {

    @Override
    public PlaytimeGoal read(final FileConfig config, final String path) {
      return new PlaytimeGoal(
          config.getString(path + ".duration", "0"),
          config.getString(path + ".remainder-window", "0"),
          config.getStringList(path + ".rewards"));
    }

    @Override
    public void write(final FileConfig config, final String path, final PlaytimeGoal value) {
      config.set(path + ".duration", String.valueOf(value.duration() / 1000L) + "s");
      config.set(path + ".remainder-window", String.valueOf(value.remainderWindow() / 1000L) + "s");
      config.set(path + ".rewards", value.rewards());
    }
  }
}
