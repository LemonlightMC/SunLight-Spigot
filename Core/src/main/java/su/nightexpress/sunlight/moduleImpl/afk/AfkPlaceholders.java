package su.nightexpress.sunlight.moduleImpl.afk;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkLang;
import org.bukkit.entity.Player;
import su.nightexpress.nightcore.core.config.CoreLang;

public class AfkPlaceholders {

    public static void registerPlaceholders(final AfkModule module, final PlaceholderRegistry registry) {
        registry.register("afk_state", (player, payload) -> {
            return CoreLang.STATE_YES_NO.get(module.handler().isAfk(player));
        });

        registry.register("afk_mode", (player, payload) -> {
            return PlaceholderContext.builder()
                    .with(SLPlaceholders.GENERIC_TIME,
                            () -> TimeFormats.formatSince(module.handler().getAfkEnterTimestamp(player),
                                    TimeFormatType.LITERAL))
                    .build()
                    .apply(AfkLang.PLACEHOLDER_MODE.get(module.isAfk(player)));
        });

        registry.register("afk_idle_time", (player, payload) -> {
            return String.valueOf(module.handler().getIdleTime(player));
        });

        registry.register("afk_idle_time_formatted", (player, payload) -> {
            return TimeFormats.formatAmount(
                    TimeUnit.MILLISECONDS.convert(module.handler().getIdleTime(player), TimeUnit.SECONDS),
                    TimeFormatType.LITERAL);
        });

        registry.register("afk_total_time", (player, payload) -> {
            return String.valueOf(module.handler().getTotalAfkTime(player));
        });

        registry.register("afk_total_time_formatted", (player, payload) -> {
            return TimeFormats.formatAmount(module.handler().getTotalAfkTime(player), TimeFormatType.LITERAL);
        });

        registry.register("afk_count", (player, payload) -> {
            return String.valueOf(module.handler().getAfkCount(player));
        });

        registry.register("afk_enter_timestamp", (player, payload) -> {
            return String.valueOf(module.handler().getAfkEnterTimestamp(player));
        });

        registry.register("afk_enter_time", (player, payload) -> {
            final long entered = module.handler().getAfkEnterTimestamp(player);
            return entered <= 0L ? "0s" : TimeFormats.formatSince(entered, TimeFormatType.LITERAL);
        });

        registry.register("afk_duration", (player, payload) -> String.valueOf(module.handler().getAfkDuration(player)));
        registry.register("afk_duration_formatted",
                (player, payload) -> TimeFormats.formatAmount(
                        TimeUnit.MILLISECONDS.convert(module.handler().getAfkDuration(player), TimeUnit.SECONDS),
                        TimeFormatType.LITERAL));
        registry.register("afk_time_until_afk",
                (player, payload) -> String
                        .valueOf(Math.max(0,
                                module.handler().getTimeToAfk(player) - module.handler().getIdleTime(player))));
        registry.register("afk_time_until_kick",
                (player, payload) -> String.valueOf(Math.max(0, module.handler().getSecondsUntilKick(player))));
        registry.register("afk_online_count",
                (player, payload) -> String.valueOf(module.handler().getAfkPlayerCount()));
        registry.register("afk_online",
                (player, payload) -> module.handler().getAfkPlayers().stream().map(Player::getName)
                        .collect(Collectors.joining(", ")));
        registry.register("afk_stats_sessions",
                (player, payload) -> String.valueOf(module.handler().getStatisticsSessions()));
        registry.register("afk_stats_total_time",
                (player, payload) -> String.valueOf(module.handler().getStatisticsTotalAfkTime()));
        registry.register("afk_stats_total_time_formatted", (player, payload) -> TimeFormats
                .formatAmount(module.handler().getStatisticsTotalAfkTime(), TimeFormatType.LITERAL));
        registry.register("afk_stats_average_time",
                (player, payload) -> String.valueOf(module.handler().getStatisticsAverageAfkTime()));
        registry.register("afk_stats_average_time_formatted", (player, payload) -> TimeFormats
                .formatAmount(module.handler().getStatisticsAverageAfkTime(), TimeFormatType.LITERAL));
    }

}
