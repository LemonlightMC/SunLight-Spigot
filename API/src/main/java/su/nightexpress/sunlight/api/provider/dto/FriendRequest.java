package su.nightexpress.sunlight.api.provider.dto;

import java.util.UUID;

import su.nightexpress.sunlight.api.SunUser;

public interface FriendRequest {

  SunUser getSender();

  SunUser getTarget();

  UUID getSenderId();

  UUID getTargetId();

  String getSenderName();

  String getTargetName();

  long getExpireDate();

  boolean isExpired();

  /** Whether this request runs from that sender to that target. */
  boolean isFrom(UUID senderId, UUID targetId);

  /** Whether that player is the one who sent it. */
  boolean isSentBy(UUID playerId);

  /** Whether that player is the one who receives it. */
  boolean isSentTo(UUID playerId);

  /** Whether this request is the same pair as that one, ignoring direction. */
  boolean isBetween(UUID playerId, UUID otherId);

  /** Whether that name matches the sender. */
  boolean isSender(String name);

  /** Whether that name matches the receiver. */
  boolean isTarget(String name);

  FriendRequestHandle toHandle();

}