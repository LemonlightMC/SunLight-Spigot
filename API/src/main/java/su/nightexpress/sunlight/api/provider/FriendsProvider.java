package su.nightexpress.sunlight.api.provider;

import org.bukkit.entity.Player;

import su.nightexpress.sunlight.api.provider.dto.BlockRecord;
import su.nightexpress.sunlight.api.provider.dto.FriendRecord;
import su.nightexpress.sunlight.api.provider.dto.FriendRequest;

import java.util.List;
import java.util.UUID;

/**
 * View of the Friends module: the friendship graph, the pending request queue
 * and the per-player
 * privacy toggles.
 * <p>
 * Obtained via {@code SunLightPlugin#friendsProvider()}, which returns an empty
 * {@link java.util.Optional} when the module is disabled, so callers must treat
 * this as a soft
 * dependency.
 * <p>
 * Friendship rows are stored one per owner, so a mutual friendship is two rows
 * and
 * {@link #isFriend(UUID, UUID)} is true from either side. Removing one side
 * removes both: the
 * module never leaves half a friendship behind.
 * <p>
 * Every type here is a primitive, a JDK type, a Bukkit type, or a type declared
 * in this package,
 * because the API module must not depend on Core.
 */
public interface FriendsProvider {

    // Relations

    /**
     * Whether the two players are friends. True from either side of a mutual
     * friendship.
     */
    boolean isFriend(UUID playerId, UUID otherId);

    /** Whether that player has the other one blocked. */
    boolean isBlocked(UUID playerId, UUID otherId);

    /**
     * That player's own side of the friendship with the other one, or {@code null}.
     */
    FriendRecord getFriend(UUID playerId, UUID friendId);
    // Lists

    /** That player's friends, online first, then alphabetically. */
    List<? extends FriendRecord> getFriends(UUID playerId);

    /** That player's friends that are online right now. */
    List<? extends FriendRecord> getOnlineFriends(UUID playerId);

    int getFriendsCount(UUID playerId);

    int getOnlineFriendsCount(UUID playerId);

    /** That player's pinned friend, or {@code null} when there is none. */
    FriendRecord getFavoriteFriend(UUID playerId);

    /** That player's blocked players. */
    List<? extends BlockRecord> getBlocked(UUID playerId);

    /**
     * The highest number of friends that player may hold, by their configured rank
     * table.
     *
     * @return {@code -1} for unlimited
     */
    int getFriendLimit(Player player);

    // Requests

    /** Requests sent to that player, expired ones already removed. */
    List<? extends FriendRequest> getIncomingRequests(UUID playerId);

    /** Requests that player has sent, expired ones already removed. */
    List<? extends FriendRequest> getOutgoingRequests(UUID playerId);

    /**
     * The request from a specific sender to that player, or {@code null} when there
     * is none.
     */

    FriendRequest getIncomingRequest(UUID playerId, String senderName);

    /**
     * Sends a request or invite, firing the module's cancellable event and sending
     * the usual
     * localized feedback to both players.
     *
     * @return {@code false} when they are already friends, either party has blocked
     *         the other, the
     *         target has requests disabled, a request is already pending, a limit
     *         is reached, or the event
     *         was cancelled
     */
    boolean sendRequest(Player sender, Player target);

    /**
     * Accepts a pending request, making the friendship mutual.
     *
     * @param senderName the sender to accept, or {@code null} for the most recent
     *                   one
     * @return {@code false} when there was nothing to accept
     */
    boolean acceptRequest(Player player, String senderName);

    /**
     * Declines or cancels a pending request. Sends the usual localized feedback
     * either way.
     *
     * @param senderName the sender to act on, or {@code null} for the most recent
     *                   one
     * @return {@code false} when there was nothing to act on
     */
    boolean declineRequest(Player player, String senderName);

    /** Drops every request that player has sent or received. */
    void clearRequests(UUID playerId);

    // Mutations

    /**
     * Makes two players friends regardless of any pending request, as the admin
     * command does.
     * Fails silently, so a caller driving this should check
     * {@link #isFriend(UUID, UUID)} first.
     *
     * @return {@code false} when they were already friends
     */
    boolean addFriend(UUID playerId, UUID otherId);

    /**
     * Removes the friendship from both sides.
     *
     * @return {@code false} when they were not friends
     */
    boolean removeFriend(UUID playerId, UUID otherId);

    /**
     * Blocks that player, dropping any friendship in both directions first, because
     * a friendship
     * that survives a block is not a block.
     *
     * @return {@code false} when they were already blocked
     */
    boolean block(UUID playerId, UUID otherId);

    /**
     * Lifts a block.
     *
     * @return {@code false} when they were not blocked
     */
    boolean unblock(UUID playerId, UUID otherId);

    // Toggles

    /** Whether that player accepts incoming friend requests. */
    boolean isRequestsEnabled(UUID playerId);

    /** Records the player's friend-request preference, persisting the choice. */
    void setRequestsEnabled(Player player, boolean enabled);

    /** Whether that player wants to be told when a friend logs in or out. */
    boolean isLoginAlertsEnabled(UUID playerId);

    /**
     * Records the player's friend-login-alert preference, persisting the choice.
     */
    void setLoginAlertsEnabled(Player player, boolean enabled);

    /** Whether that player lets friends teleport to them. */
    boolean isFriendTeleportEnabled(UUID playerId);

    /** Records the player's friend-teleport preference, persisting the choice. */
    void setFriendTeleportEnabled(Player player, boolean enabled);

}