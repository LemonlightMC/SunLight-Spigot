package su.nightexpress.sunlight.api.provider;

import org.bukkit.entity.Player;

/**
 * Public surface of the nametags feature.
 */
public interface NametagsProvider {

    boolean hasTag(Player player);

    String getSelectedTagId(Player player);

    boolean selectTag(Player player, String tagId);

    // -----------------------------------------------------
    // Profiles and ranks
    // -----------------------------------------------------

    /** The profile the player has explicitly chosen, or an empty string. */

    String getSelectedProfileId(Player player);

    boolean selectProfile(Player player, String profileId);

    /** The rank the player has explicitly chosen, or an empty string. */

    String getSelectedRankId(Player player);

    boolean selectRank(Player player, String rankId);

    // -----------------------------------------------------
    // Access and entitlements
    // -----------------------------------------------------

    /** Whether the player may currently use the tag, grants included. */
    boolean hasAccess(Player player, String tagId);

    /** Grants the tag permanently or for one subscription period. */
    boolean grantTag(Player player, String tagId);

    boolean revokeTag(Player player, String tagId);

    // -----------------------------------------------------
    // Refresh
    // -----------------------------------------------------

    /** Rebuilds one player's nameplate and pushes it to TAB. */
    void recompute(Player player);

    void recomputeAll();
}
