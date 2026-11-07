package su.nightexpress.sunlight.moduleImpl.essential.command;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.text.tag.TagPool;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialModule;
import su.nightexpress.sunlight.moduleImpl.essential.EssentialPerms;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.WHITE;

public class RenameCommandProvider extends CommandProvider<EssentialModule> {

    private static final TextLocale DESCRIPTION = LangEntry
            .builder("Command.Rename.Desc").text("Rename the item in your main hand.");

    private static final String PERMISSION            = EssentialPerms.COMMAND_RENAME;
    private static final String PERMISSION_COLORS     = EssentialPerms.COMMAND_RENAME_COLORS;

    private static final MessageLocale MESSAGE_RENAMED = LangEntry
            .builder("Command.Rename.Done").chatMessage(
            Sound.ENTITY_ITEM_PICKUP,
            GRAY.wrap("Item renamed to " + WHITE.wrap("%text%") + "."));

    private static final MessageLocale MESSAGE_EMPTY_HAND = LangEntry
            .builder("Command.Rename.EmptyHand").chatMessage(
            Sound.ENTITY_VILLAGER_NO,
            GRAY.wrap("You must hold an item in your main hand to rename it!"));

    public RenameCommandProvider(final EssentialModule module) {
        super(module, "rename");
    }

    @Override
    public void setup() {
        this.register("rename", command -> command
                .withFullDescription(DESCRIPTION.text())
                .withPermission(PERMISSION)
                .withRequirement(sender -> sender instanceof Player)
                .withOptionalArguments(new GreedyStringArgument(CommandArgumentConstants.TEXT))
                .executes((sender, arguments) -> {
                    return this.renameItem(sender, arguments);
                }));
    }

    private int renameItem(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final Object textObj = arguments.get(CommandArgumentConstants.TEXT);
        if (!(textObj instanceof final String text) || text.isBlank()) {
            return 0;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(MESSAGE_EMPTY_HAND, player);
            return 0;
        }

        final String name = this.hasColors(player)
                ? NightMessage.from(text, TagPool.ALL_COLORS_AND_STYLES).toLegacy()
                : NightMessage.stripTags(text);

        ItemUtil.editMeta(item, meta -> meta.setDisplayName(name));

        this.module.sendPrefixed(MESSAGE_RENAMED, player, builder -> builder
            .with(CommandArgumentConstants.TEXT, () -> NightMessage.stripTags(text)));
        return 1;
    }

    private boolean hasColors(final Player player) {
        return player.hasPermission(PERMISSION_COLORS);
    }
}
