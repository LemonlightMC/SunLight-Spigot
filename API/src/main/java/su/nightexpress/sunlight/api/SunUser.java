package su.nightexpress.sunlight.api;

import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface SunUser {

  Map<String, Object> getProperties();

  Map<String, Object> getPropertiesToSave();

  void setFirstTimeJoined(boolean firstTimeJoined);

  boolean isFirstTimeJoined();

  boolean hasPlayedBefore();

  long getDateCreated();

  long getLastOnline();

  void setLastOnline(long lastOnline);

  Optional<InetAddress> getLatestAddress();

  void setLatestAddress(InetAddress address);

  public boolean isOnline();

  public boolean isHolder(CommandSender sender);

  public boolean isHolder(Player player);

  public boolean isHolder(OfflinePlayer player);

  public boolean isHolder(UUID playerId);

  public OfflinePlayer offlinePlayer();

  public Optional<Player> player();

  public UUID getId();

  public String getName();

  public void setName(String name);
}