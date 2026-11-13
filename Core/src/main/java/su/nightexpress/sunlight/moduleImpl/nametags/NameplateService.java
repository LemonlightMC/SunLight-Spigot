package su.nightexpress.sunlight.moduleImpl.nametags;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.moduleImpl.nametags.hook.GroupSource;
import su.nightexpress.sunlight.moduleImpl.nametags.hook.TeamInfo;
import su.nightexpress.sunlight.moduleImpl.nametags.hook.TeamSource;
import su.nightexpress.sunlight.moduleImpl.nametags.model.Profile;
import su.nightexpress.sunlight.moduleImpl.nametags.model.RankDefinition;
import su.nightexpress.sunlight.moduleImpl.nametags.model.TagDefinition;
import su.nightexpress.sunlight.moduleImpl.nametags.render.Nameplate;
import su.nightexpress.sunlight.moduleImpl.nametags.render.Part;
import su.nightexpress.sunlight.moduleImpl.nametags.render.PrefixComposer;
import su.nightexpress.sunlight.moduleImpl.nametags.render.TabNameTagBackend;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.UserManager;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single entry point for building and applying a player's nameplate.
 * <p>
 * TAB owns the actual rendering, so this class never writes packets. It
 * resolves the four contributing parts (team, rank, tag, glow), composes them
 * and hands the result to the backend, which drops redundant writes.
 * <p>
 * Group and team lookups are asynchronous, so recomputation returns a future
 * and only ever touches Bukkit state on the main thread.
 */
public class NameplateService {

    private final Module module;
    private final NametagsSettings settings;
    private final NametagsCatalog catalog;
    private final AccessService access;
    private final UserManager userManager;
    private final TabNameTagBackend backend;

    private volatile GroupSource groupSource;
    private volatile TeamSource teamSource;
    private final GlowColorSource glowSource;

    /**
     * Last composed nameplate per player, kept so placeholder requests stay cheap.
     */
    private final Map<UUID, Nameplate> lastComposed = new ConcurrentHashMap<>();

    /** Last resolved team per player, for the team placeholder. */
    private final Map<UUID, TeamInfo> lastTeam = new ConcurrentHashMap<>();

    public NameplateService(Module module,
            NametagsSettings settings,
            NametagsCatalog catalog,
            AccessService access,
            UserManager userManager,
            TabNameTagBackend backend,
            GroupSource groupSource,
            TeamSource teamSource,
            GlowColorSource glowSource) {
        this.module = module;
        this.settings = settings;
        this.catalog = catalog;
        this.access = access;
        this.userManager = userManager;
        this.backend = backend;
        this.groupSource = groupSource;
        this.teamSource = teamSource;
        this.glowSource = glowSource;
    }

    /**
     * Swaps the group and team sources, used when a reload re-resolves them from
     * the settings. Both are read per recompute, so no cached nameplate goes stale.
     */
    public void setSources(GroupSource groupSource, TeamSource teamSource) {
        this.groupSource = groupSource;
        this.teamSource = teamSource;
    }

    /**
     * Supplies the player's current glow colour name. Implemented by the glow
     * module so the
     * nametags module does not depend on it.
     */
    public interface GlowColorSource {

        String getGlowColor(Player player);
    }

    // -----------------------------------------------------
    // Computation
    // -----------------------------------------------------

    /**
     * Recomputes a player's nameplate and pushes it to TAB.
     * <p>
     * Group and team lookups may be async, so the chain returns futures and always
     * lands back on the main thread before touching Bukkit or TAB state.
     */
    public void recompute(Player player) {
        UUID id = player.getUniqueId();
        if (!player.isOnline()) {
            this.lastComposed.remove(id);
            return;
        }

        GroupSource groupSource = this.groupSource;
        if (groupSource == null)
            return;

        groupSource.resolveGroups(player)
                .thenCombine(this.resolveTeam(player), (groups, team) -> new Resolved(groups, team))
                .exceptionally(throwable -> new Resolved(Set.of(), TeamInfo.EMPTY))
                .thenAccept(resolved -> this.module.plugin().runTask(
                        () -> this.apply(player, resolved.groups(), resolved.team())));
    }

    private record Resolved(Set<String> groups, TeamInfo team) {
    }

    private java.util.concurrent.CompletableFuture<TeamInfo> resolveTeam(Player player) {
        TeamSource teamSource = this.teamSource;
        if (teamSource == null || !this.settings.isUseUltimateTeams() || !teamSource.isAvailable()) {
            return java.util.concurrent.CompletableFuture.completedFuture(TeamInfo.EMPTY);
        }
        return teamSource.resolveTeam(player)
                .thenApply(info -> info == null ? TeamInfo.EMPTY : info)
                .exceptionally(throwable -> TeamInfo.EMPTY);
    }

    private void apply(Player player, Set<String> groups, TeamInfo team) {
        if (!player.isOnline())
            return;

        SunUser user = this.userManager.getOrFetch(player);
        long now = System.currentTimeMillis();

        Profile profile = this.catalog.resolveProfile(player, user.getPropertyOrDefault(NametagsProperties.PROFILE));

        RankDefinition rank = this.resolveRank(player, user, profile, groups);
        TagDefinition tag = this.resolveTag(player, user, profile, now);

        Part teamPart = Part.of(team.prefix(), team.suffix(), team.color());
        Part rankPart = rank == null
                ? Part.EMPTY
                : Part.of(rank.getPrefix(), rank.getSuffix(), rank.getColor());
        Part tagPart = tag == null
                ? Part.EMPTY
                : Part.of(tag.getPrefix(), tag.getSuffix(), tag.getColor());

        String glow = this.resolveGlow(player, user, tag, now);

        Nameplate composed = PrefixComposer.compose(teamPart, rankPart, tagPart, glow,
                this.settings.isTagColorOverridesRank());
        composed = this.resolvePlaceholders(player, composed);

        this.lastComposed.put(player.getUniqueId(), composed);
        this.lastTeam.put(player.getUniqueId(), team);
        this.backend.apply(player, composed);
    }

    private RankDefinition resolveRank(Player player,
            SunUser user,
            Profile profile,
            Set<String> groups) {
        // A profile may force a rank, overriding both the stored pick and group
        // resolution.
        if (profile != null && profile.hasRank()) {
            RankDefinition forced = this.catalog.getRank(profile.getRankId());
            if (forced != null)
                return forced;
        }

        String stored = user.getPropertyOrDefault(NametagsProperties.RANK);
        if (stored != null && !stored.isBlank()) {
            RankDefinition chosen = this.catalog.getRank(stored);
            if (chosen != null)
                return chosen;
        }

        return this.catalog.resolveRank(player, groups);
    }

    private TagDefinition resolveTag(Player player,
            SunUser user,
            Profile profile,
            long nowMillis) {
        String id = null;
        if (profile != null && profile.hasTag()) {
            id = profile.getTagId();
        } else {
            id = user.getPropertyOrDefault(NametagsProperties.TAG);
        }
        if (id == null || id.isBlank())
            return null;

        TagDefinition tag = this.catalog.getTag(id);
        if (tag == null)
            return null;
        return this.access.hasAccess(player, user, tag, nowMillis) ? tag : null;
    }

    private String resolveGlow(Player player,
            SunUser user,
            TagDefinition tag,
            long nowMillis) {
        if (!this.settings.isResolveGlowColor() || this.glowSource == null)
            return null;

        String color = this.glowSource.getGlowColor(player);
        if (color == null || color.isBlank())
            return null;

        // A tag that opted out of glow keeps its own colour instead.
        if (tag != null && !this.access.allowsGlow(user, tag, nowMillis))
            return null;

        return color;
    }

    private Nameplate resolvePlaceholders(Player player, Nameplate nameplate) {
        String prefix = this.applyPlaceholders(player, nameplate.prefix());
        String suffix = this.applyPlaceholders(player, nameplate.suffix());
        return new Nameplate(prefix, suffix, nameplate.color(), nameplate.glow());
    }

    private String applyPlaceholders(Player player, String text) {
        if (text.isEmpty())
            return text;
        return PlaceholderContext.builder()
                .with(CommonPlaceholders.PLAYER.resolver(player))
                .andThen(SLPlaceholders.forPlaceholderAPI(player))
                .build()
                .apply(text);
    }

    // -----------------------------------------------------
    // Placeholder source
    // -----------------------------------------------------

    /** Feeds TAB's registered placeholder, which runs on the TAB refresh thread. */
    public String getPrefixFor(UUID playerId) {
        Nameplate nameplate = this.lastComposed.get(playerId);
        return nameplate == null ? "" : nameplate.prefix();
    }

    public String getSuffixFor(UUID playerId) {
        Nameplate nameplate = this.lastComposed.get(playerId);
        return nameplate == null ? "" : nameplate.suffix();
    }

    public Nameplate getLastComposed(UUID playerId) {
        return this.lastComposed.getOrDefault(playerId, Nameplate.EMPTY);
    }

    /** The team behind the last composed nameplate, or {@link TeamInfo#EMPTY}. */
    public TeamInfo getLastTeam(UUID playerId) {
        return this.lastTeam.getOrDefault(playerId, TeamInfo.EMPTY);
    }

    /** Recomputes every online player, e.g. after a reload or a rank change. */
    public void recomputeAll() {
        for (Player player : Bukkit.getOnlinePlayers())
            this.recompute(player);
    }

    public void forget(UUID playerId) {
        this.lastComposed.remove(playerId);
        this.lastTeam.remove(playerId);
        this.backend.remove(playerId);
        this.access.invalidate(playerId);
    }

    public void clear() {
        this.lastComposed.clear();
        this.lastTeam.clear();
        this.backend.clearCache();
        this.access.clearCache();
    }
}
