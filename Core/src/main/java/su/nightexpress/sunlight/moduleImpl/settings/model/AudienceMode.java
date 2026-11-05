package su.nightexpress.sunlight.moduleImpl.settings.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import su.nightexpress.sunlight.SunLightPlugin;

public enum AudienceMode {

    EVERYONE("everyone", "Everyone"),
    TEAM("team", "Team"),
    FRIENDS("friends", "Friends"),
    NOBODY("nobody", "Nobody");

    private final String id;
    private final String displayName;

    AudienceMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return displayName;
    }

    public static AudienceMode byId(String id) {
        if (id == null) return EVERYONE;
        String key = id.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(mode -> mode.id.equals(key)).findFirst().orElse(EVERYONE);
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(AudienceMode::id).toList();
    }

    public boolean allows(SunLightPlugin plugin, Player receiver, Player sender) {
        if (receiver.getUniqueId().equals(sender.getUniqueId())) {
            return true;
        }

        return switch (this) {
            case EVERYONE -> true;
            case NOBODY -> false;
            case FRIENDS -> plugin.friendsProvider()
                    .map(provider -> provider.isFriend(receiver.getUniqueId(), sender.getUniqueId()))
                    .orElse(false);
            case TEAM -> sameTeam(receiver, sender);
        };
    }

    private static boolean sameTeam(Player receiver, Player sender) {
        Team team = receiver.getScoreboard().getEntryTeam(receiver.getName());
        return team != null && team.hasEntry(sender.getName());
    }
}