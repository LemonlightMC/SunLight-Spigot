package su.nightexpress.sunlight.moduleImpl.playtime2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import su.nightexpress.sunlight.api.provider.PlaytimeProvider;
import su.nightexpress.sunlight.moduleImpl.playtime.model.PlaytimePeriod;
import su.nightexpress.sunlight.user.SunUser;

public class PlaytimeProviderImpl implements PlaytimeProvider {

  private final Playtime2Module module;

  public PlaytimeProviderImpl(final Playtime2Module module) {
    this.module = module;
  }

  @Override
  public long getPlaytime(final UUID playerId, final String period) {
    final SunUser user = module.userManager().getOrFetch(playerId).orElse(null);
    return user == null ? 0L : module.getEffectivePlaytime(user, PlaytimePeriod.fromString(period));
  }

  @Override
  public long getTotalPlaytime(final UUID playerId) {
    return this.getPlaytime(playerId, PlaytimePeriod.ALLTIME.getConfigName());
  }

  @Override
  public long getSessionPlaytime(final UUID playerId) {
    final SunUser user = module.userManager().getOrFetch(playerId).orElse(null);
    return user == null ? 0L : Math.max(0L, user.getPropertyOrDefault(Playtime2Properties.SESSION));
  }

  @Override
  public int getDailyStreak(final UUID playerId) {
    final SunUser user = module.userManager().getOrFetch(playerId).orElse(null);
    return user == null ? 0 : module.getDailyStreak(user);
  }

  @Override
  public int getWeeklyStreak(final UUID playerId) {
    final SunUser user = module.userManager().getOrFetch(playerId).orElse(null);
    return user == null ? 0 : module.getWeeklyStreak(user);
  }

  @Override
  public long getGoalDuration(final UUID playerId, final String scope) {
    return module.getSettings().getGoalMs(PlaytimePeriod.fromString(scope));
  }

  @Override
  public List<PlaytimeProvider.TopEntry> getTop(final String period, final int limit) {
    final List<Playtime2TopHandler.TopEntry> board = this.module.topHandler()
        .getTop(PlaytimePeriod.fromString(period));
    final int size = Math.max(0, Math.min(limit, board.size()));
    final List<PlaytimeProvider.TopEntry> result = new ArrayList<>(size);
    for (int index = 0; index < size; index++) {
      final Playtime2TopHandler.TopEntry entry = board.get(index);
      result.add(new PlaytimeProvider.TopEntry(entry.id(), entry.name(), entry.value()));
    }
    return result;
  }

}
