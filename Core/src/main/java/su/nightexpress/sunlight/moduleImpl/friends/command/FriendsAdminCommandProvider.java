package su.nightexpress.sunlight.moduleImpl.friends.command;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.friends.FriendsModule;
import su.nightexpress.sunlight.moduleImpl.friends.FriendsPlaceholders;
import su.nightexpress.sunlight.moduleImpl.friends.config.FriendsLang;
import su.nightexpress.sunlight.moduleImpl.friends.config.FriendsPerms;
import su.nightexpress.sunlight.user.SunUser;

/**
 * {@code /friendadmin}: the staff surface.
 * <p>
 * Everything resolves to ids rather than to {@link Player} handles, because a
 * friendship exists
 * between two accounts and staff must be able to fix a pair who are both
 * offline.
 */
public class FriendsAdminCommandProvider extends CommandProvider<FriendsModule> {

    private static final String COMMAND_ROOT = "friendadmin";

    private static final String COMMAND_INFO = "info";
    private static final String COMMAND_ADD = "add";
    private static final String COMMAND_REMOVE = "remove";
    private static final String COMMAND_UNBLOCK = "unblock";

    public FriendsAdminCommandProvider(final FriendsModule module) {
        super(module, "friends-admin");
    }

    @Override
    public void setup() {
        this.register(COMMAND_INFO, command -> command
                .withFullDescription(FriendsLang.COMMAND_ADMIN_INFO_DESC.text())
                .withPermission(FriendsPerms.COMMAND_FRIENDS_ADMIN_ROOT)
                .withArguments(CommandArgumentConstants.targetArgument())
                .executes(this::info))
                .aliases("list", "show")
                .under(COMMAND_ROOT);

        this.register(COMMAND_ADD, command -> command
                .withFullDescription(FriendsLang.COMMAND_ADMIN_ADD_DESC.text())
                .withPermission(FriendsPerms.COMMAND_FRIENDS_ADMIN_ROOT)
                .withArguments(CommandArgumentConstants.string(CommandArgumentConstants.PLAYER,
                        info -> this.module.getKnownNames()),
                        CommandArgumentConstants.string(CommandArgumentConstants.TARGET,
                                info -> this.module.getKnownNames()))
                .executes(this::add))
                .under(COMMAND_ROOT);

        this.register(COMMAND_REMOVE, command -> command
                .withFullDescription(FriendsLang.COMMAND_ADMIN_REMOVE_DESC.text())
                .withPermission(FriendsPerms.COMMAND_FRIENDS_ADMIN_ROOT)
                .withArguments(CommandArgumentConstants.string(CommandArgumentConstants.PLAYER,
                        info -> this.module.getKnownNames()),
                        CommandArgumentConstants.string(CommandArgumentConstants.TARGET,
                                info -> this.module.getKnownNames()))
                .executes(this::remove))
                .under(COMMAND_ROOT);

        this.register(COMMAND_UNBLOCK, command -> command
                .withFullDescription(FriendsLang.COMMAND_ADMIN_UNBLOCK_DESC.text())
                .withPermission(FriendsPerms.COMMAND_FRIENDS_ADMIN_ROOT)
                .withArguments(CommandArgumentConstants.string(CommandArgumentConstants.PLAYER,
                        info -> this.module.getKnownNames()),
                        CommandArgumentConstants.string(CommandArgumentConstants.TARGET,
                                info -> this.module.getKnownNames()))
                .executes(this::unblock))
                .under(COMMAND_ROOT);

        this.registerRoot(COMMAND_ROOT, command -> command
                .withFullDescription(FriendsLang.COMMAND_ADMIN_ROOT_DESC.text())
                .withPermission(FriendsPerms.COMMAND_FRIENDS_ADMIN_ROOT))
                .aliases("fadmin");
    }

    private int info(final CommandSender sender, final CommandArguments arguments) {
        final CommandArgumentConstants.Target target = CommandArgumentConstants.target(arguments);
        if (target == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }

        return target.runAs(this.module, sender, FriendsPerms.COMMAND_FRIENDS_ADMIN_ROOT,
                (user, targetPlayer) -> {
                    final int count = this.module.repository().getFriendsCount(targetPlayer.getUniqueId());
                    final int limit = this.module.getFriendLimit(targetPlayer);

                    this.module.sendPrefixed(FriendsLang.ADMIN_INFO_FEEDBACK, sender, builder -> builder
                            .with(CommonPlaceholders.PLAYER.resolver(targetPlayer))
                            .with(FriendsPlaceholders.FRIEND_LIST, () -> String.valueOf(count))
                            .with(SLPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(count))
                            .with(SLPlaceholders.GENERIC_MAX,
                                    () -> limit < 0 ? CoreLang.OTHER_INFINITY.text() : String.valueOf(limit)));

                    this.module.openFriends(targetPlayer, targetPlayer.getUniqueId());
                });
    }

    private int add(final CommandSender sender, final CommandArguments arguments) {
        final SunUser first = this.resolve(arguments, CommandArgumentConstants.PLAYER);
        final SunUser second = this.resolve(arguments, CommandArgumentConstants.TARGET);
        if (first == null || second == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        if (first.getId().equals(second.getId())) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_NOT_YOURSELF, sender);
            return 0;
        }
        if (this.module.isFriend(first.getId(), second.getId())) {
            this.module.sendPrefixed(FriendsLang.ADMIN_ADD_ERROR_ALREADY, sender);
            return 0;
        }

        if (!this.module.addFriend(first.getId(), second.getId())) {
            if (this.module.isFriend(first.getId(), second.getId())) {
                this.module.sendPrefixed(FriendsLang.ADMIN_ADD_ERROR_ALREADY, sender);
            }
            else {
                this.module.sendPrefixed(CoreLang.COMMAND_SYNTAX_GENERIC_ERROR, sender);
            }
            return 0;
        }

        this.module.sendPrefixed(FriendsLang.ADMIN_ADD_FEEDBACK, sender, builder -> builder
                .with(CommonPlaceholders.PLAYER_NAME, first::getName)
                .with(SLPlaceholders.GENERIC_TARGET, second::getName));
        this.module.notifyWatchers(first, second);
        return 1;
    }

    private int remove(final CommandSender sender, final CommandArguments arguments) {
        final SunUser first = this.resolve(arguments, CommandArgumentConstants.PLAYER);
        final SunUser second = this.resolve(arguments, CommandArgumentConstants.TARGET);
        if (first == null || second == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        if (!this.module.isFriend(first.getId(), second.getId())) {
            this.module.sendPrefixed(FriendsLang.ADMIN_REMOVE_ERROR_NOT_FRIEND, sender);
            return 0;
        }

        this.module.removeFriend(first.getId(), second.getId());

        this.module.sendPrefixed(FriendsLang.ADMIN_REMOVE_FEEDBACK, sender, builder -> builder
                .with(CommonPlaceholders.PLAYER_NAME, first::getName)
                .with(SLPlaceholders.GENERIC_TARGET, second::getName));
        return 1;
    }

    private int unblock(final CommandSender sender, final CommandArguments arguments) {
        final SunUser first = this.resolve(arguments, CommandArgumentConstants.PLAYER);
        final SunUser second = this.resolve(arguments, CommandArgumentConstants.TARGET);
        if (first == null || second == null) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }

        // Both directions, because a staff member asking to clear a pair means the
        // pair, not the
        // one arrow they happened to type first.
        this.module.unblock(first.getId(), second.getId());
        this.module.unblock(second.getId(), first.getId());

        this.module.sendPrefixed(FriendsLang.ADMIN_UNBLOCK_FEEDBACK, sender, builder -> builder
                .with(CommonPlaceholders.PLAYER_NAME, first::getName)
                .with(SLPlaceholders.GENERIC_TARGET, second::getName));
        return 1;
    }

    /**
     * Resolves a name to a profile, online or cached, so offline pairs are still
     * reachable.
     */
    private SunUser resolve(final CommandArguments arguments, final String key) {
        final Object raw = arguments.get(key);
        if (!(raw instanceof final String name)) {
            return null;
        }
        final String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        final UUID id = this.module.userManager().getRepository().getAssociatedId(trimmed);
        if (id == null) {
            return null;
        }
        return this.module.userManager().getRepository().getById(id).orElse(null);
    }
}