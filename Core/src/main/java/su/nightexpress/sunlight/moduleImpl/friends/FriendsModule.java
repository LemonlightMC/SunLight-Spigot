package su.nightexpress.sunlight.moduleImpl.friends;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.api.provider.FriendsProvider;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.friends.command.FriendsAdminCommandProvider;
import su.nightexpress.sunlight.moduleImpl.friends.command.FriendsCommandProvider;
import su.nightexpress.sunlight.moduleImpl.friends.command.FriendsUserInterface;
import su.nightexpress.sunlight.moduleImpl.friends.config.FriendsLang;
import su.nightexpress.sunlight.moduleImpl.friends.config.FriendsPerms;
import su.nightexpress.sunlight.moduleImpl.friends.data.FriendsDataManager;
import su.nightexpress.sunlight.moduleImpl.friends.dialog.FriendAddDialog;
import su.nightexpress.sunlight.moduleImpl.friends.dialog.FriendBlockDialog;
import su.nightexpress.sunlight.moduleImpl.friends.dialog.FriendRemoveDialog;
import su.nightexpress.sunlight.moduleImpl.friends.event.PlayerFriendBlockEvent;
import su.nightexpress.sunlight.moduleImpl.friends.event.PlayerFriendChangeEvent;
import su.nightexpress.sunlight.moduleImpl.friends.event.PreFriendBlockEvent;
import su.nightexpress.sunlight.moduleImpl.friends.event.PreFriendChangeEvent;
import su.nightexpress.sunlight.moduleImpl.friends.listener.FriendsListener;
import su.nightexpress.sunlight.moduleImpl.friends.menu.FriendOptionsMenu;
import su.nightexpress.sunlight.moduleImpl.friends.menu.FriendsBlockedMenu;
import su.nightexpress.sunlight.moduleImpl.friends.menu.FriendsListMenu;
import su.nightexpress.sunlight.moduleImpl.friends.menu.FriendsPendingMenu;
import su.nightexpress.sunlight.moduleImpl.friends.menu.FriendsSettingsMenu;
import su.nightexpress.sunlight.moduleImpl.friends.repository.BlockRecord;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsAccess;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsProperties;
import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendRecord;
import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendRequest;
import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendRequestRepository;
import su.nightexpress.sunlight.moduleImpl.friends.repository.FriendsRepository;
import su.nightexpress.sunlight.teleport.TeleportManager;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;
import su.nightexpress.sunlight.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Friends module.
 * <p>
 * The friendship graph and the block list live in the database and are mirrored
 * in {@link FriendsRepository}; pending requests live only in memory, like the
 * PTP request queue.
 * <p>
 * A mutual friendship is two rows, one owned by each side, because the
 * favourite flag is per-owner.
 * Every change goes through {@link #addFriend} or {@link #removeFriend},
 * which write both rows together, so the module never leaves half a friendship
 * behind.
 */
public class FriendsModule extends Module implements FriendsProvider {

    private final TeleportManager teleportManager;
    private final FriendsDataManager dataManager;
    private final FriendsRepository repository;
    private final FriendRequestRepository requestRepository;
    private final FriendsUserInterface userInterface;

    private FriendsListMenu listMenu;
    private FriendsPendingMenu pendingMenu;
    private FriendsBlockedMenu blockedMenu;
    private FriendsSettingsMenu settingsMenu;
    private FriendOptionsMenu optionsMenu;

    public volatile boolean dataLoaded;
    private final AtomicInteger loadGeneration = new AtomicInteger();

    public FriendsModule(final ModuleDefinition<FriendsModule> definition, final SunLightPlugin plugin) {
        super(definition, plugin);
        this.teleportManager = plugin.teleportManager();
        this.dataManager = new FriendsDataManager(this.dataHandler, this);
        this.repository = new FriendsRepository();
        this.requestRepository = new FriendRequestRepository();
        this.userInterface = new FriendsUserInterface(this);
    }

    public FriendsRepository repository() {
        return repository;
    }

    public FriendRequestRepository requestRepository() {
        return requestRepository;
    }

    public FriendsDataManager dataManager() {
        return dataManager;
    }

    public TeleportManager teleportManager() {
        return teleportManager;
    }

    public FriendsUserInterface userInterface() {
        return userInterface;
    }

    @Override
    protected void loadModule(final FileConfig config) {
        config.initializeOptions(FriendsConfig.class);

        // Must happen before any SunUser is deserialised: UserColumns drops any key it
        // cannot find
        // in the registry, so an unregistered property is silently lost on every load.
        UserPropertyRegistry.register(FriendsProperties.REQUESTS_ENABLED);
        UserPropertyRegistry.register(FriendsProperties.LOGIN_ALERTS);
        UserPropertyRegistry.register(FriendsProperties.FRIEND_TELEPORT);
        UserPropertyRegistry.register(FriendsProperties.REQUEST_DAY_KEY);
        UserPropertyRegistry.register(FriendsProperties.REQUEST_DAY_COUNT);
        UserPropertyRegistry.register(FriendsProperties.REQUEST_TARGET_DAY_COUNTS);

        this.plugin.injectLang(FriendsLang.class);

        this.dataManager.init(FriendsConfig.DATA_TABLE_PREFIX.get());

        this.loadMenus();
        this.loadDialogs();
        this.loadData();

        this.addListener(new FriendsListener(this.plugin, this));

        this.commandRegistry.addProvider(new FriendsCommandProvider(this));
        this.commandRegistry.addProvider(new FriendsAdminCommandProvider(this));
    }

    @Override
    protected void unloadModule() {
        this.loadGeneration.incrementAndGet();
        this.repository.clear();
        this.requestRepository.clear();
        this.dataLoaded = false;
        this.listMenu = null;
        this.pendingMenu = null;
        this.blockedMenu = null;
        this.settingsMenu = null;
        this.optionsMenu = null;
    }

    private void loadMenus() {
        this.listMenu = new FriendsListMenu(this.plugin, this);
        this.listMenu.load(FileConfig.load(this.getLocalUIPath(), "friends.yml"));

        this.pendingMenu = new FriendsPendingMenu(this.plugin, this);
        this.pendingMenu.load(FileConfig.load(this.getLocalUIPath(), "friends-pending.yml"));

        this.blockedMenu = new FriendsBlockedMenu(this.plugin, this);
        this.blockedMenu.load(FileConfig.load(this.getLocalUIPath(), "friends-blocked.yml"));

        this.settingsMenu = new FriendsSettingsMenu(this.plugin, this);
        this.settingsMenu.load(FileConfig.load(this.getLocalUIPath(), "friends-settings.yml"));

        this.optionsMenu = new FriendOptionsMenu(this.plugin, this);
        this.optionsMenu.load(FileConfig.load(this.getLocalUIPath(), "friend-options.yml"));
    }

    private void loadDialogs() {
        this.dialogRegistry.register(FriendAddDialog.FRIEND_ADD, () -> new FriendAddDialog(this));
        this.dialogRegistry.register(FriendRemoveDialog.FRIEND_REMOVE, () -> new FriendRemoveDialog(this));
        this.dialogRegistry.register(FriendBlockDialog.FRIEND_BLOCK, () -> new FriendBlockDialog(this));
    }

    private void loadData() {
        this.dataLoaded = false;
        final int generation = this.loadGeneration.incrementAndGet();
        this.plugin.runTaskAsync(() -> {
            try {
                final var friends = this.dataManager.getFriends();
                final var blocks = this.dataManager.getBlocks();
                if (generation != this.loadGeneration.get() || !this.plugin.isEnabled()) {
                    return;
                }
                this.plugin.runTask(() -> {
                    if (generation != this.loadGeneration.get() || !this.plugin.isEnabled()) {
                        return;
                    }
                    this.repository.clear();
                    for (final FriendRecord record : friends) {
                        this.repository.upsert(record);
                    }
                    for (final BlockRecord block : blocks) {
                        this.repository.upsertBlock(block);
                    }
                    this.dataLoaded = true;
                    this.info("Loaded and cached " + this.repository.size() + " friendship rows.");
                });
            }
            catch (Exception exception) {
                this.error("Failed to load friends data: " + exception.getMessage());
                exception.printStackTrace();
                // Keep dataLoaded false on failure to avoid operating on partial/stale cache.
            }
        });
    }

    // Placeholders

    @Override
    public void registerPlaceholders(final PlaceholderRegistry registry) {
        FriendsPlaceholders.registerPlaceholders(this, registry);
    }

    /** Resolves a name against online players, then the user cache. */

    public UUID resolveId(final String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        final String trimmed = name.trim();
        final Player online = Utils.getPlayer(trimmed);
        if (online != null) {
            return online.getUniqueId();
        }
        return this.userManager.getRepository().getAssociatedId(trimmed);
    }

    // Relations

    @Override
    public boolean isFriend(final UUID playerId, final UUID otherId) {
        return this.repository.isFriend(playerId, otherId);
    }

    @Override
    public boolean isBlocked(final UUID playerId, final UUID otherId) {
        return this.repository.isBlocked(playerId, otherId);
    }

    // Lists
    @Override
    public List<FriendRecord> getFriends(final UUID playerId) {
        return this.repository.getFriends(playerId);
    }

    @Override
    public List<FriendRecord> getOnlineFriends(final UUID playerId) {
        return this.repository.getOnlineFriends(playerId);
    }

    @Override
    public int getFriendsCount(final UUID playerId) {
        return this.repository.getFriendsCount(playerId);
    }

    @Override
    public int getOnlineFriendsCount(final UUID playerId) {
        return this.repository.getOnlineFriendsCount(playerId);
    }

    /**
     * That player's own side of the friendship with the other one, or {@code null}.
     */

    @Override
    public FriendRecord getFriend(final UUID playerId, final UUID friendId) {
        return this.repository.getFriend(playerId, friendId);
    }

    @Override
    public FriendRecord getFavoriteFriend(final UUID playerId) {
        return this.repository.getFavorite(playerId);
    }

    @Override
    public List<BlockRecord> getBlocked(final UUID playerId) {
        return this.repository.getBlocks(playerId);
    }

    @Override
    public int getFriendLimit(final Player player) {
        if (player.hasPermission(FriendsPerms.BYPASS_LIMITS)) {
            return -1;
        }
        return FriendsConfig.AMOUNT_PER_RANK.get().getGreatestOrNegative(player).intValue();
    }

    /**
     * The highest number of pending requests that player may keep, from their rank
     * table.
     */
    public int getRequestLimit(final Player player) {
        if (player.hasPermission(FriendsPerms.BYPASS_LIMITS)) {
            return -1;
        }
        return FriendsConfig.REQUESTS_PER_RANK.get().getGreatestOrNegative(player).intValue();
    }

    public boolean canAddMore(final Player player) {
        final int limit = this.getFriendLimit(player);
        return limit < 0 || this.getFriendsCount(player.getUniqueId()) < limit;
    }

    /** Human-readable last-seen text, shared by the menus and the placeholder. */
    public String getLastSeenText(final UUID playerId) {
        final Player online = Utils.getPlayer(playerId);
        if (online != null) {
            return "Online now";
        }
        return FriendsPlaceholders.formatLastSeen(
                this.userManager.getRepository().getById(playerId).map(SunUser::getLastOnline).orElse(0L));
    }

    /**
     * Names of every player this module can resolve, for the admin command's
     * suggestions.
     */
    public Collection<String> getKnownNames() {
        return this.repository.getAll().stream()
                .flatMap(
                        record -> java.util.stream.Stream.of(record.getOwner().getName(), record.getFriend().getName()))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    // Mutations

    @Override
    public boolean addFriend(final UUID playerId, final UUID otherId) {
        if (!this.dataLoaded || playerId == null || otherId == null || playerId.equals(otherId)) {
            return false;
        }
        final SunUser owner = this.infoOf(playerId);
        final SunUser friend = this.infoOf(otherId);
        if (owner == null || friend == null) {
            return false;
        }
        if (this.isFriend(owner.getId(), friend.getId())) {
            return false;
        }

        final FriendRecord first = FriendRecord.create(owner, friend);
        final FriendRecord second = FriendRecord.create(friend, owner);
        final PreFriendChangeEvent preFirst = new PreFriendChangeEvent(first, false);
        final PreFriendChangeEvent preSecond = new PreFriendChangeEvent(second, false);
        this.plugin.getPluginManager().callEvent(preFirst);
        this.plugin.getPluginManager().callEvent(preSecond);
        if (preFirst.isCancelled() || preSecond.isCancelled()) {
            return false;
        }

        this.repository.upsert(first);
        this.repository.upsert(second);
        this.plugin.getPluginManager().callEvent(new PlayerFriendChangeEvent(first, true));
        this.plugin.getPluginManager().callEvent(new PlayerFriendChangeEvent(second, true));

        this.plugin.runTaskAsync(() -> {
            try {
                if (!this.dataManager.existsInDatabase(owner.getId(), friend.getId())) {
                    this.dataManager.insertFriend(first);
                }
                if (!this.dataManager.existsInDatabase(friend.getId(), owner.getId())) {
                    this.dataManager.insertFriend(second);
                }
            }
            catch (Exception exception) {
                this.error("Failed to persist friendship between " + owner.getId() + " and " + friend.getId() + ": " + exception.getMessage());
            }
        });

        return true;
    }

    protected boolean addRow(final SunUser owner, final SunUser friend) {
        if (owner == null || friend == null || owner.getId().equals(friend.getId())) {
            return false;
        }
        if (this.isFriend(owner.getId(), friend.getId())) {
            return false;
        }
        final FriendRecord record = FriendRecord.create(owner, friend);
        final PreFriendChangeEvent pre = new PreFriendChangeEvent(record, false);
        this.plugin.getPluginManager().callEvent(pre);
        if (pre.isCancelled()) {
            return false;
        }

        this.repository.upsert(record);
        this.plugin.getPluginManager().callEvent(new PlayerFriendChangeEvent(record, true));
        this.plugin.runTaskAsync(() -> {
            try {
                if (!this.dataManager.existsInDatabase(record.getOwnerId(), record.getFriendId())) {
                    this.dataManager.insertFriend(record);
                }
            }
            catch (Exception exception) {
                this.error("Failed to persist friend row for " + record.getOwnerId() + ": " + exception.getMessage());
            }
        });
        return true;
    }

    @Override
    public boolean removeFriend(final UUID playerId, final UUID otherId) {
        if (playerId == null || otherId == null) return false;
        final FriendRecord owned = this.getFriend(playerId, otherId);
        final FriendRecord reversed = this.getFriend(otherId, playerId);

        boolean removed = false;
        if (owned != null) {
            removed |= this.removeRow(owned);
        }
        if (reversed != null) {
            removed |= this.removeRow(reversed);
        }
        return removed;
    }

    protected boolean removeRow(final FriendRecord record) {
        if (record == null) return false;
        final PreFriendChangeEvent pre = new PreFriendChangeEvent(record, true);
        this.plugin.getPluginManager().callEvent(pre);
        if (pre.isCancelled()) {
            return false;
        }

        this.repository.remove(record);
        this.plugin.runTaskAsync(() -> this.dataManager.deleteFriend(record));
        this.plugin.getPluginManager().callEvent(new PlayerFriendChangeEvent(record, false));
        return true;
    }

    @Override
    public boolean block(final UUID playerId, final UUID otherId) {
        final SunUser blocker = this.infoOf(playerId);
        final SunUser blocked = this.infoOf(otherId);
        if (blocker == null || blocked == null || blocker.getId().equals(blocked.getId())) {
            return false;
        }

        final PreFriendBlockEvent pre = new PreFriendBlockEvent(blocker, blocked, true);
        this.plugin.getPluginManager().callEvent(pre);
        if (pre.isCancelled() || this.repository.isBlocked(playerId, otherId)) {
            return false;
        }

        // A friendship that survives a block is not a block, so this comes first. Both
        // sides are
        // dropped, and both request directions with them.
        this.removeFriend(playerId, otherId);
        this.requestRepository.removeBetween(playerId, otherId);
        this.requestRepository.removeBetween(otherId, playerId);

        final BlockRecord block = BlockRecord.create(blocker, blocked);
        this.repository.upsertBlock(block);
        this.plugin.runTaskAsync(() -> this.dataManager.insertBlock(block));

        this.plugin.getPluginManager().callEvent(new PlayerFriendBlockEvent(blocker, blocked, true));
        return true;
    }

    @Override
    public boolean unblock(final UUID playerId, final UUID otherId) {
        final SunUser blocker = this.infoOf(playerId);
        final SunUser blocked = this.infoOf(otherId);
        if (blocker == null || blocked == null) {
            return false;
        }

        final PreFriendBlockEvent pre = new PreFriendBlockEvent(blocker, blocked, false);
        this.plugin.getPluginManager().callEvent(pre);
        if (pre.isCancelled() || !this.repository.isBlocked(playerId, otherId)) {
            return false;
        }

        final List<BlockRecord> rows = this.repository.getBlocks(playerId).stream()
                .filter(record -> record.getBlockedId().equals(otherId))
                .toList();
        for (final BlockRecord block : rows) {
            this.repository.removeBlock(block);
            this.plugin.runTaskAsync(() -> this.dataManager.deleteBlock(block));
        }

        this.plugin.getPluginManager().callEvent(new PlayerFriendBlockEvent(blocker, blocked, false));
        return !rows.isEmpty();
    }

    /** Pins one friend and unpins whichever was pinned before. */
    public boolean toggleFavorite(final Player player, final UUID friendId) {
        final FriendRecord record = this.repository.getFriend(player.getUniqueId(), friendId);
        if (record == null) {
            return false;
        }

        // Captured before the flip, because setFavorite clears it in place and the old
        // row would
        // otherwise be written back still marked as the favourite.
        final FriendRecord previous = this.repository.getFavorite(player.getUniqueId());

        final boolean state = !record.isFavorite();
        this.repository.setFavorite(player.getUniqueId(), friendId, state);

        if (previous != null && !previous.getFriendId().equals(friendId)) {
            this.plugin.runTaskAsync(() -> this.dataManager.updateFriend(previous));
        }
        final FriendRecord updated = this.repository.getFriend(player.getUniqueId(), friendId);
        if (updated != null) {
            this.plugin.runTaskAsync(() -> this.dataManager.updateFriend(updated));
        }

        this.sendPrefixed(state ? FriendsLang.FAVORITE_SET : FriendsLang.FAVORITE_UNSET, player,
                builder -> builder.with(CommonPlaceholders.PLAYER_NAME, record::getFriendName));
        return true;
    }

    private SunUser infoOf(final UUID playerId) {
        return this.userManager.getRepository().getById(playerId).orElse(null);
    }

    // Requests

    @Override
    public List<FriendRequest> getIncomingRequests(final UUID playerId) {
        return this.requestRepository.getIncoming(playerId);
    }

    @Override
    public List<FriendRequest> getOutgoingRequests(final UUID playerId) {
        return this.requestRepository.getOutgoing(playerId);
    }

    @Override
    public FriendRequest getIncomingRequest(final UUID playerId,
            final String senderName) {
        final FriendRequest request = this.requestRepository.getIncoming(playerId).stream()
                .filter(entry -> entry.isSender(senderName))
                .findFirst()
                .orElse(null);
        return request;
    }

    @Override
    public boolean sendRequest(final Player sender, final Player target) {
        return userInterface.sendRequest(sender, target);
    }

    @Override
    public boolean acceptRequest(final Player player, final String senderName) {
        return userInterface.acceptIncoming(player, senderName);
    }

    @Override
    public boolean declineRequest(final Player player, final String senderName) {
        return userInterface.declineIncoming(player, senderName);
    }

    @Override
    public void clearRequests(final UUID playerId) {
        this.requestRepository.clearFor(playerId);
    }

    // Toggles

    @Override
    public boolean isRequestsEnabled(final UUID playerId) {
        return this.userManager.getRepository().getById(playerId)
                .map(FriendToggle.REQUESTS::get)
                .orElse(FriendsProperties.REQUESTS_ENABLED.getDefaultValue());
    }

    @Override
    public void setRequestsEnabled(final Player player, final boolean enabled) {
        FriendToggle.REQUESTS.set(this.userManager.getOrFetch(player), enabled);
    }

    @Override
    public boolean isLoginAlertsEnabled(final UUID playerId) {
        return this.userManager.getRepository().getById(playerId)
                .map(FriendToggle.LOGIN_ALERTS::get)
                .orElse(FriendsProperties.LOGIN_ALERTS.getDefaultValue());
    }

    @Override
    public void setLoginAlertsEnabled(final Player player, final boolean enabled) {
        FriendToggle.LOGIN_ALERTS.set(this.userManager.getOrFetch(player), enabled);
    }

    @Override
    public boolean isFriendTeleportEnabled(final UUID playerId) {
        return this.userManager.getRepository().getById(playerId)
                .map(FriendToggle.TELEPORT::get)
                .orElse(FriendsProperties.FRIEND_TELEPORT.getDefaultValue());
    }

    @Override
    public void setFriendTeleportEnabled(final Player player, final boolean enabled) {
        FriendToggle.TELEPORT.set(this.userManager.getOrFetch(player), enabled);
    }

    /**
     * Applies a toggle to a loaded user, used by both the command and the settings
     * menu.
     */
    public void applyToggle(final SunUser user, final FriendToggle toggle, final boolean value) {
        toggle.set(user, value);
        if (!SettingsAccess.isEnabled(this.plugin)) {
            return;
        }
        if (toggle == FriendToggle.REQUESTS) {
            SettingsAccess.setEnabled(user, SettingsProperties.FRIEND_REQUEST_MODE, value);
        } else if (toggle == FriendToggle.TELEPORT) {
            SettingsAccess.setEnabled(user, SettingsProperties.FRIEND_TELEPORT_MODE, value);
        }
    }

    // Notifications

    public void notifyFriends(final Player player, final MessageLocale message) {
        if (!FriendsConfig.NOTIFY_LOGIN_ENABLED.get()) {
            return;
        }

        for (final FriendRecord record : this.getOnlineFriends(player.getUniqueId())) {
            final Player viewer = Utils.getPlayer(record.getOwnerId());
            if (viewer == null || !this.isLoginAlertsEnabled(viewer.getUniqueId())) {
                continue;
            }
            this.sendPrefixed(message, viewer, builder -> builder.with(CommonPlaceholders.PLAYER.resolver(player)));
        }
    }

    public void notifyWatchers(final Player first, final Player second) {
        final String permission = FriendsConfig.NOTIFY_STAFF_PERMISSION.get();
        if (permission == null || permission.isBlank()) {
            return;
        }

        for (final Player candidate : Bukkit.getOnlinePlayers()) {
            if (!candidate.hasPermission(permission)) {
                continue;
            }
            // The two parties are never told about this; only third parties are.
            if (candidate.getUniqueId().equals(first.getUniqueId())
                    || candidate.getUniqueId().equals(second.getUniqueId())) {
                continue;
            }
            this.sendPrefixed(FriendsLang.NOTIFY_ADMIN_ADD, candidate, builder -> builder
                    .with(CommonPlaceholders.PLAYER_NAME, first::getName)
                    .with(SLPlaceholders.GENERIC_TARGET, second::getName));
        }
    }

    public void notifyWatchers(final SunUser first, final SunUser second) {
        final String permission = FriendsConfig.NOTIFY_STAFF_PERMISSION.get();
        if (permission == null || permission.isBlank()) {
            return;
        }

        for (final Player candidate : Bukkit.getOnlinePlayers()) {
            if (!candidate.hasPermission(permission)) {
                continue;
            }
            // The two parties are never told about this; only third parties are.
            if (candidate.getUniqueId().equals(first.getId()) || candidate.getUniqueId().equals(second.getId())) {
                continue;
            }
            this.sendPrefixed(FriendsLang.NOTIFY_ADMIN_ADD, candidate, builder -> builder
                    .with(CommonPlaceholders.PLAYER_NAME, first::getName)
                    .with(SLPlaceholders.GENERIC_TARGET, second::getName));
        }
    }

    // Menus

    public void openAddDialog(final Player player) {
        this.plugin.showDialog(player, FriendAddDialog.FRIEND_ADD, "", () -> this.openFriends(player));
    }

    public boolean openFriends(final Player player) {
        return this.listMenu.show(player, player.getUniqueId());
    }

    public boolean openFriends(final Player player, final UUID targetId) {
        return this.listMenu.show(player, targetId);
    }

    public boolean openPending(final Player player) {
        return this.pendingMenu.show(player);
    }

    public boolean openBlocked(final Player player) {
        return this.blockedMenu.show(player);
    }

    public boolean openSettings(final Player player) {
        return this.settingsMenu.show(player);
    }

    public boolean openOptions(final Player player, final UUID friendId) {
        return this.optionsMenu.show(player, friendId);
    }

    /**
     * Closes the viewer's inventory before an action opens another one, when
     * configured to.
     * <p>
     * Without the close the player is left looking at a menu whose rows the action
     * already
     * invalidated, which reads as a bug even when everything worked.
     */
    public void closeInventoryOnAction(final Player player) {
        if (FriendsConfig.COMMAND_MENU_CLOSES_INVENTORY.get()) {
            player.closeInventory();
        }
    }
}