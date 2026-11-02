package su.nightexpress.sunlight.api.provider.dto;

import java.util.UUID;

import org.bukkit.entity.Player;

import su.nightexpress.nightcore.util.placeholder.PlaceholderResolver;
import su.nightexpress.sunlight.api.SunUser;

public interface FriendRecord {

  PlaceholderResolver placeholders();

  SunUser getOwner();

  SunUser getFriend();

  UUID getOwnerId();

  String getOwnerName();

  boolean isOwnerOnline();

  long ownerLastOnline();

  UUID getFriendId();

  String getFriendName();

  boolean isFriendOnline();

  long friendLastOnline();

  long getCreateDate();

  void setCreateDate(long createDate);

  boolean isFavorite();

  void setFavorite(boolean favorite);

  boolean isOwner(Player player);

  boolean isOwner(UUID playerId);

  boolean isMember(UUID playerId);

  /** Age of the friendship in milliseconds. */
  long getAge();

  /** The other party, or {@code null} when that player is neither of the two. */
  SunUser getOther(UUID playerId);

}