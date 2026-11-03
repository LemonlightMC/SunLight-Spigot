package su.nightexpress.sunlight.moduleImpl.friends.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.config.Lang;
import su.nightexpress.sunlight.moduleImpl.friends.FriendToggle;
import su.nightexpress.sunlight.moduleImpl.friends.FriendsConfig;
import su.nightexpress.sunlight.moduleImpl.friends.FriendsModule;
import su.nightexpress.sunlight.moduleImpl.friends.config.FriendsLang;
import su.nightexpress.sunlight.moduleImpl.friends.config.FriendsPerms;
import su.nightexpress.sunlight.moduleImpl.friends.event.PlayerFriendRequestEvent;
import su.nightexpress.sunlight.moduleImpl.friends.event.PreFriendRequestEvent;
import su.nightexpress.sunlight.moduleImpl.friends.model.FriendRequest;
import su.nightexpress.sunlight.moduleImpl.friends.request.FriendRequestCounters;
import su.nightexpress.sunlight.teleport.TeleportContext;
import su.nightexpress.sunlight.teleport.TeleportFlag;
import su.nightexpress.sunlight.teleport.TeleportType;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.utils.EconomyUtils;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

public class FriendsUserInterface {

  private final FriendsModule module;

  /**
   * Cooldown key for {@code /friend add}, stored in the user's command cooldown
   * data.
   */
  private static final su.nightexpress.sunlight.command.CommandKey REQUEST_COOLDOWN_KEY = new su.nightexpress.sunlight.command.CommandKey(
      "friends", "add");

  public FriendsUserInterface(final FriendsModule module) {
    this.module = module;
  }

  /** Blocking as the command and menu drive it, with the usual feedback. */
  public boolean blockCommand(final Player player, final Player target) {
    if (player.getUniqueId().equals(target.getUniqueId())) {
      module.sendPrefixed(FriendsLang.BLOCK_ERROR_SELF, player);
      return false;
    }
    if (module.repository().isBlocked(player.getUniqueId(), target.getUniqueId())) {
      module.sendPrefixed(FriendsLang.BLOCK_ERROR_ALREADY, player,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }

    final int limit = FriendsConfig.BLOCK_LIMIT.get();
    if (limit >= 0 && module.repository().getBlocksCount(player.getUniqueId()) >= limit
        && !player.hasPermission(FriendsPerms.BYPASS_LIMITS)) {
      module.sendPrefixed(FriendsLang.BLOCK_ERROR_LIMIT, player,
          builder -> builder.with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(limit)));
      return false;
    }

    final boolean wasFriend = module.isFriend(player.getUniqueId(), target.getUniqueId());
    if (!module.block(player.getUniqueId(), target.getUniqueId())) {
      return false;
    }

    module.sendPrefixed(FriendsLang.BLOCK_DONE_FEEDBACK, player,
        builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
    if (wasFriend) {
      module.sendPrefixed(FriendsLang.BLOCK_DONE_NOTIFY, target,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(player)));
    }
    return true;
  }

  /** Unblocking by name, so it works for a player who is offline. */
  public boolean unblockCommand(final Player player, final String name) {
    final UUID otherId = module.resolveId(name);
    if (otherId == null) {
      module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, player);
      return false;
    }
    if (!module.repository().isBlocked(player.getUniqueId(), otherId)) {
      module.sendPrefixed(FriendsLang.UNBLOCK_ERROR_NOT_BLOCKED, player,
          builder -> builder.with(CommonPlaceholders.PLAYER_NAME, () -> name));
      return false;
    }
    if (!module.unblock(player.getUniqueId(), otherId)) {
      return false;
    }

    module.sendPrefixed(FriendsLang.UNBLOCK_DONE, player,
        builder -> builder.with(CommonPlaceholders.PLAYER_NAME, () -> name));
    return true;
  }

  /**
   * Removal as the command and menu drive it: one message to the player, one to
   * the friend.
   */
  public boolean removeFriendCommand(final Player player, final Player friend) {
    if (!module.isFriend(player.getUniqueId(), friend.getUniqueId())) {
      module.sendPrefixed(FriendsLang.REMOVE_ERROR_NOT_FRIEND, player,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(friend)));
      return false;
    }

    final boolean removed = module.removeFriend(player.getUniqueId(), friend.getUniqueId());
    if (!removed) {
      return false;
    }

    module.sendPrefixed(FriendsLang.REMOVE_FEEDBACK, player,
        builder -> builder.with(CommonPlaceholders.PLAYER.resolver(friend)));
    module.sendPrefixed(FriendsLang.REMOVE_NOTIFY, friend,
        builder -> builder.with(CommonPlaceholders.PLAYER.resolver(player)));
    return true;
  }

  public boolean sendRequest(final Player sender, final Player target) {
    if (!module.dataLoaded) {
      module.sendPrefixed(FriendsLang.ERROR_DATA_NOT_LOADED, sender);
      return false;
    }
    if (sender.getUniqueId().equals(target.getUniqueId())) {
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_SELF, sender);
      return false;
    }
    if (module.isFriend(sender.getUniqueId(), target.getUniqueId())) {
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_ALREADY_FRIEND, sender,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }
    if (module.repository().isBlockedEitherWay(sender.getUniqueId(), target.getUniqueId())) {
      // Deliberately says nothing about which side blocked: the other player does not
      // need
      // to learn that a block exists in order to be refused by it.
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_BLOCKED, sender,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }
    if (!module.isRequestsEnabled(target.getUniqueId())
        && !sender.hasPermission(FriendsPerms.BYPASS_REQUESTS_DISABLED)) {
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_DISABLED, sender,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }
    if (module.requestRepository().hasBetween(sender.getUniqueId(), target.getUniqueId())) {
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_PENDING, sender,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }

    final SunUser senderUser = module.userManager().getOrFetch(sender);

    if (!checkRequestCooldown(sender, senderUser)) {
      return false;
    }
    if (!checkDailyLimits(sender, senderUser, target)) {
      return false;
    }
    if (!checkAddLimit(sender)) {
      return false;
    }
    if (!checkTargetLimit(sender, target)) {
      return false;
    }

    final SunUser targetUser = module.userManager().getOrFetch(target);

    final PreFriendRequestEvent pre = new PreFriendRequestEvent(senderUser, targetUser);
    module.plugin().getPluginManager().callEvent(pre);
    if (pre.isCancelled()) {
      return false;
    }

    final FriendRequest request = FriendRequest.create(
        senderUser,
        targetUser,
        FriendsConfig.REQUEST_TIMEOUT_SECONDS.get());
    module.requestRepository().add(request);

    FriendRequestCounters.increment(senderUser, target.getUniqueId());

    if (FriendsConfig.NOTIFY_REQUESTS_ENABLED.get()) {
      module.sendPrefixed(FriendsLang.REQUEST_SENT_FEEDBACK, sender,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      module.sendPrefixed(FriendsLang.REQUEST_SENT_NOTIFY, target,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(sender)));
    }

    module.plugin().getPluginManager().callEvent(new PlayerFriendRequestEvent(
        senderUser,
        targetUser,
        request.getExpireDate()));
    return true;
  }

  private boolean checkRequestCooldown(final Player sender, final SunUser senderUser) {
    final int cooldown = FriendsConfig.REQUEST_COOLDOWN_SECONDS.get();
    if (cooldown <= 0 || EconomyUtils.hasCooldownBypass(sender, FriendsPerms.BYPASS_COOLDOWN)) {
      return true;
    }

    final Long expireDate = senderUser.getCommandCooldown(REQUEST_COOLDOWN_KEY);
    if (expireDate != null && !TimeUtil.isPassed(expireDate)) {
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_COOLDOWN, sender,
          builder -> builder.with(SLPlaceholders.GENERIC_TIME,
              () -> TimeFormats.formatDuration(expireDate, TimeFormatType.LITERAL)));
      return false;
    }

    senderUser.setCommandCooldown(REQUEST_COOLDOWN_KEY, TimeUtil.createFutureTimestamp(cooldown));
    return true;
  }

  private boolean checkDailyLimits(final Player sender, final SunUser senderUser, final Player target) {
    if (sender.hasPermission(FriendsPerms.BYPASS_LIMITS)) {
      return true;
    }

    final int perDay = FriendsConfig.REQUESTS_PER_DAY.get();
    if (perDay > 0 && FriendRequestCounters.getTotal(senderUser) >= perDay) {
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_DAILY_LIMIT, sender,
          builder -> builder.with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(perDay)));
      return false;
    }

    final int perTarget = FriendsConfig.REQUESTS_PER_TARGET_PER_DAY.get();
    if (perTarget > 0 && FriendRequestCounters.getForTarget(senderUser, target.getUniqueId()) >= perTarget) {
      module.sendPrefixed(FriendsLang.REQUEST_ERROR_TARGET_DAILY_LIMIT, sender, builder -> builder
          .with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(perTarget))
          .with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }

    return true;
  }

  /** Refuses when the sender is already at their own cap. */
  private boolean checkAddLimit(final Player player) {
    if (module.canAddMore(player)) {
      return true;
    }
    module.sendPrefixed(FriendsLang.REQUEST_ERROR_LIMIT, player,
        builder -> builder.with(SLPlaceholders.GENERIC_MAX,
            () -> String.valueOf(module.getRequestLimit(player))));
    return false;
  }

  /**
   * Refuses when the target is already at their cap, which would otherwise only
   * be discovered
   * when they accepted and silently could not.
   */
  private boolean checkTargetLimit(final Player sender, final Player target) {
    if (module.canAddMore(target)) {
      return true;
    }
    module.sendPrefixed(FriendsLang.REQUEST_ERROR_OTHER_LIMIT, sender,
        builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
    return false;
  }

  /** Declines an incoming request addressed to that player. */
  public boolean declineIncoming(final Player player, final String senderName) {
    final List<FriendRequest> incoming = module.requestRepository().getIncoming(player.getUniqueId());
    final FriendRequest request = senderName == null
        ? (incoming.isEmpty() ? null : incoming.getLast())
        : incoming.stream().filter(entry -> entry.isSender(senderName)).findFirst().orElse(null);

    if (request == null) {
      module.sendPrefixed(FriendsLang.REQUEST_NOTHING_PENDING, player, builder -> builder
          .with(CommonPlaceholders.PLAYER_NAME, () -> senderName == null ? "" : senderName));
      return false;
    }

    module.requestRepository().remove(request);

    module.sendPrefixed(FriendsLang.REQUEST_DECLINE_FEEDBACK, player,
        builder -> builder.with(CommonPlaceholders.PLAYER_NAME, request::getSenderName));

    final Player sender = Utils.getPlayer(request.getSenderId());
    if (sender != null) {
      module.sendPrefixed(FriendsLang.REQUEST_DECLINE_NOTIFY, sender,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(player)));
    }
    return true;
  }

  /** Accepts an incoming request addressed to that player. */
  public boolean acceptIncoming(final Player player, final String senderName) {
    final List<FriendRequest> incoming = module.requestRepository().getIncoming(player.getUniqueId());
    final FriendRequest request = senderName == null
        ? (incoming.isEmpty() ? null : incoming.getLast())
        : incoming.stream().filter(entry -> entry.isSender(senderName)).findFirst().orElse(null);

    if (request == null) {
      module.sendPrefixed(FriendsLang.REQUEST_NOTHING_PENDING, player, builder -> builder
          .with(CommonPlaceholders.PLAYER_NAME, () -> senderName == null ? "" : senderName));
      return false;
    }

    // Re-checked rather than trusted from the send: this player's own list may have
    // grown while
    // the request sat pending, and the sender's may have shrunk by then too.
    if (!checkAddLimit(player)) {
      return false;
    }

    module.requestRepository().remove(request);
    module.addFriend(player.getUniqueId(), request.getSenderId());

    module.sendPrefixed(FriendsLang.REQUEST_ACCEPT_FEEDBACK, player,
        builder -> builder.with(CommonPlaceholders.PLAYER_NAME, request::getSenderName));

    final Player sender = Utils.getPlayer(request.getSenderId());
    if (sender != null) {
      module.sendPrefixed(FriendsLang.REQUEST_ACCEPT_NOTIFY, sender,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(player)));
      module.notifyWatchers(player, sender);
    } else {
      // Accepted while they are offline, which is the common case: the friendship is
      // real,
      // they are told about it when they next log in through the login alert.
      sendStoredAccept(request.getSenderId(), request.getSenderName());
    }
    return true;
  }

  /**
   * Tells an offline sender their request was accepted.
   * <p>
   * There is no offline message channel in SunLight, so this is a best-effort
   * send to a cached
   * player object; when that fails the friendship itself is unaffected and they
   * are told by the
   * login alert instead.
   */
  private void sendStoredAccept(final UUID playerId, final String senderName) {
    final Player offline = Utils.getPlayer(playerId);
    if (offline != null && offline.isOnline()) {
      module.sendPrefixed(FriendsLang.REQUEST_ACCEPT_NOTIFY, offline, builder -> builder
          .with(CommonPlaceholders.PLAYER_NAME, () -> senderName));
    }
  }

  // Messages

  /**
   * Points a player at the private-message command for one friend.
   * <p>
   * A menu cannot ask for free text, so this does not fake a message: it names
   * the command that
   * does the job, which is also the form that still works when the Chat module is
   * disabled.
   */
  public boolean promptMessage(final Player player, final String friendName) {
    final Player target = Utils.getPlayer(friendName);
    if (target == null) {
      module.sendPrefixed(FriendsLang.JUMP_ERROR_OFFLINE, player,
          builder -> builder.with(CommonPlaceholders.PLAYER_NAME, () -> friendName));
      return false;
    }

    module.sendPrefixed(FriendsLang.MESSAGE_PROMPT, player,
        builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
    return module.plugin().chatProvider().isPresent();
  }

  /** Sends a private message to a friend, resolved by name. */
  public boolean messageFriend(final Player player, final String friendName, final String message) {
    final Player target = Utils.getPlayer(friendName);
    if (target == null) {
      module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, player);
      return false;
    }

    final var chat = module.plugin().chatProvider();
    if (chat.isEmpty()) {
      module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, player);
      return false;
    }

    return chat.get().sendPrivateMessage(player, target, message);
  }

  /**
   * Confirms a toggle to the player, naming the setting so a bare "set to
   * enabled" is never sent
   * on its own.
   */
  public void sendToggle(final Player player, final FriendToggle toggle, final boolean state,
      final CommandSender executor) {
    if (executor != null && executor != player) {
      module.sendPrefixed(FriendsLang.TOGGLE_FEEDBACK, executor, builder -> builder
          .with(CommonPlaceholders.PLAYER.resolver(player))
          .with(SLPlaceholders.GENERIC_NAME, toggle::displayName)
          .with(SLPlaceholders.GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(state)));
    }
    module.sendPrefixed(FriendsLang.TOGGLE_NOTIFY, player, builder -> builder
        .with(SLPlaceholders.GENERIC_NAME, toggle::displayName)
        .with(SLPlaceholders.GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(state)));
  }

  // Jump

  /**
   * Teleports one friend to another.
   * <p>
   * Routed through the teleport manager with the PTP type, so the Warmups
   * module's boss bar
   * applies here exactly as it does to a plain teleport request.
   */
  public boolean jumpToFriend(final Player player, final String friendName) {
    final Player target = Utils.getPlayer(friendName);
    if (target == null) {
      module.sendPrefixed(FriendsLang.JUMP_ERROR_OFFLINE, player,
          builder -> builder.with(CommonPlaceholders.PLAYER_NAME, () -> friendName));
      return false;
    }
    if (player.getUniqueId().equals(target.getUniqueId())) {
      module.sendPrefixed(CoreLang.COMMAND_EXECUTION_NOT_YOURSELF, player);
      return false;
    }
    if (!module.isFriend(player.getUniqueId(), target.getUniqueId())) {
      module.sendPrefixed(FriendsLang.JUMP_ERROR_NOT_FRIEND, player,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }
    if (!module.isFriendTeleportEnabled(target.getUniqueId())
        && !FriendsConfig.TELEPORT_BYPASSES_REQUESTS_DISABLED.get()) {
      module.sendPrefixed(FriendsLang.JUMP_ERROR_DISABLED, player,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
      return false;
    }
    if (!player.hasPermission(FriendsPerms.BYPASS_WORLD_BLACKLIST)
        && FriendsConfig.JUMP_WORLD_BLACKLIST.get().stream()
            .anyMatch(world -> world.equalsIgnoreCase(player.getWorld().getName()))) {
      module.sendPrefixed(FriendsLang.JUMP_ERROR_WORLD, player);
      return false;
    }

    final double cost = FriendsConfig.JUMP_COST.get();
    final boolean charge = cost > 0D && EconomyUtils.hasCurrency()
        && !EconomyUtils.hasBypass(player, FriendsPerms.BYPASS_COST);
    if (charge && !EconomyUtils.canAfford(player, cost)) {
      module.sendPrefixed(Lang.COST_ERROR_NOT_ENOUGH_FUNDS, player,
          builder -> builder.with(SLPlaceholders.GENERIC_AMOUNT, () -> EconomyUtils.format(cost)));
      return false;
    }

    final TeleportContext context = TeleportContext.builder(module, player, target.getLocation())
        .withFlag(TeleportFlag.LOOK_FOR_SURFACE)
        .withFlag(TeleportFlag.AVOID_LAVA)
        .callback(() -> {
          if (charge) {
            EconomyUtils.withdraw(player, cost);
          }
          module.sendPrefixed(FriendsLang.JUMP_SUCCESS, player,
              builder -> builder.with(CommonPlaceholders.PLAYER.resolver(target)));
        })
        .build();

    return module.teleportManager().teleport(context, TeleportType.PTP);
  }

  /** Withdraws a request this player has sent. */
  public boolean cancelRequest(final Player player, final String targetName) {
    final List<FriendRequest> outgoing = module.requestRepository().getOutgoing(player.getUniqueId());
    final FriendRequest request = targetName == null
        ? (outgoing.isEmpty() ? null : outgoing.getLast())
        : outgoing.stream().filter(entry -> entry.isTarget(targetName)).findFirst().orElse(null);

    if (request == null) {
      module.sendPrefixed(FriendsLang.REQUEST_NOTHING_SENT, player, builder -> builder
          .with(CommonPlaceholders.PLAYER_NAME, () -> targetName == null ? "" : targetName));
      return false;
    }

    module.requestRepository().remove(request);

    module.sendPrefixed(FriendsLang.REQUEST_CANCEL_FEEDBACK, player,
        builder -> builder.with(CommonPlaceholders.PLAYER_NAME, request::getTargetName));

    final Player target = Utils.getPlayer(request.getTargetId());
    if (target != null) {
      module.sendPrefixed(FriendsLang.REQUEST_CANCEL_NOTIFY, target,
          builder -> builder.with(CommonPlaceholders.PLAYER.resolver(player)));
    }
    return true;
  }

}
