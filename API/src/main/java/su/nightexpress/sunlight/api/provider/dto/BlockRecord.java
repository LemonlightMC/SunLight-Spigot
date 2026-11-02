package su.nightexpress.sunlight.api.provider.dto;

import java.util.UUID;

import org.bukkit.entity.Player;

import su.nightexpress.sunlight.api.SunUser;

public interface BlockRecord {

  SunUser getBlocker();

  UUID getBlockerId();

  String getBlockerName();

  SunUser getBlocked();

  UUID getBlockedId();

  String getBlockedName();

  long getCreateDate();

  /**
   * Whether this block stands between those two players in either direction.
   * Blocking is not
   * symmetric in storage but it is in effect: if either has blocked the other,
   * neither may send
   * a request.
   */
  boolean isBetween(UUID playerId, UUID otherId);

  boolean involves(UUID playerId);

  boolean isBlocking(UUID playerId);

  /** Whether the blocked player is online right now. */
  boolean isBlockedOnline();

  Player getBlockedPlayer();

}