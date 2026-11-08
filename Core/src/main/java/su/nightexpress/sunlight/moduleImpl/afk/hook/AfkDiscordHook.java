package su.nightexpress.sunlight.moduleImpl.afk.hook;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.sunlight.hook.HookId;
import su.nightexpress.sunlight.moduleImpl.afk.config.AfkSettings;

import java.awt.Color;
import java.util.Locale;

public class AfkDiscordHook {

    private final AfkSettings settings;

    public AfkDiscordHook(@NotNull AfkSettings settings) {
        this.settings = settings;
    }

    public boolean isAvailable() {
        return this.settings.discordEnabled.get() && HookId.hasDiscordSRV();
    }

    public void sendEnter(@NotNull Player player, int idleSeconds) {
        this.send(player, this.settings.discordMessageEnter.get(), "AFK", 0xF4B942, idleSeconds, 0L);
    }

    public void sendExit(@NotNull Player player, long afkDurationMs) {
        this.send(player, this.settings.discordMessageExit.get(), "Back", 0x57F287, 0, afkDurationMs);
    }

    public void sendKick(@NotNull Player player, @Nullable String idleTime) {
        String message = this.settings.discordMessageKick.get();
        if (idleTime != null) message = message.replace("%idle_time%", NightMessage.stripTags(idleTime));
        this.send(player, message, "AFK Kick", 0xED4245, 0, 0L);
    }

    private void send(@NotNull Player player, @NotNull String template, @NotNull String title, int color, int idleSeconds, long afkDurationMs) {
        if (!this.isAvailable()) return;
        try {
            this.sendInternal(player, template, title, color, idleSeconds, afkDurationMs);
        }
        catch (NoClassDefFoundError | Exception exception) {
            exception.printStackTrace();
        }
    }

    private void sendInternal(@NotNull Player player, @NotNull String template, @NotNull String title, int color, int idleSeconds, long afkDurationMs) {
        if (!DiscordSRV.isReady) return;

        TextChannel channel = this.resolveChannel();
        if (channel == null) return;

        String message = this.applyPlaceholders(player, template, idleSeconds, afkDurationMs);
        if (message.isBlank()) return;

        if (this.settings.discordUseEmbeds.get()) {
            try {
                MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(title, null, this.resolveAvatar(player))
                        .setDescription(message)
                        .setColor(new Color(color))
                        .build();
                channel.sendMessageEmbeds(embed).queue();
                return;
            }
            catch (Throwable ignored) {
                // Fall back to plain text if embed classes/API are unavailable.
            }
        }

        DiscordUtil.sendMessage(channel, NightMessage.stripTags(message));
    }

    private TextChannel resolveChannel() {
        String id = this.settings.discordChannelId.get();
        if (id != null && !id.isBlank()) {
            try {
                if (DiscordUtil.getJda() != null) {
                    TextChannel channel = DiscordUtil.getJda().getTextChannelById(id.trim());
                    if (channel != null) return channel;
                }
            }
            catch (Exception ignored) {
            }
        }
        return DiscordSRV.getPlugin().getMainTextChannel();
    }

    private String resolveAvatar(@NotNull Player player) {
        String template = this.settings.discordAvatarUrl.get();
        if (template == null || template.isBlank()) return null;
        return template.replace("%player_name%", player.getName()).replace("%player_uuid%", player.getUniqueId().toString());
    }

    private String applyPlaceholders(@NotNull Player player, @NotNull String template, int idleSeconds, long afkDurationMs) {
        String result = template
                .replace("%player_name%", player.getName())
                .replace("%player_display_name%", NightMessage.stripTags(player.getDisplayName()))
                .replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%idle_time%", this.formatSeconds(idleSeconds))
                .replace("%afk_time%", this.formatDuration(afkDurationMs));

        if (!this.settings.discordShowDuration.get()) {
            // Keep messages readable; templates control content, this flag mainly documents intent.
        }
        return result;
    }

    private String formatSeconds(int seconds) {
        if (seconds <= 0) return "0s";
        long minutes = seconds / 60L;
        long secs = seconds % 60L;
        if (minutes <= 0) return secs + "s";
        return String.format(Locale.ROOT, "%dm %ds", minutes, secs);
    }

    private String formatDuration(long durationMs) {
        if (durationMs <= 0L) return "0s";
        long total = durationMs / 1000L;
        long minutes = total / 60L;
        long seconds = total % 60L;
        if (minutes <= 0) return seconds + "s";
        return String.format(Locale.ROOT, "%dm %ds", minutes, seconds);
    }
}
