package su.nightexpress.sunlight.moduleImpl.sign.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.sign.SignsModule;
import su.nightexpress.sunlight.moduleImpl.sign.config.SignsConfig;
import su.nightexpress.sunlight.moduleImpl.sign.config.SignsLang;
import su.nightexpress.sunlight.moduleImpl.sign.config.SignsPerms;
import su.nightexpress.sunlight.moduleImpl.sign.object.ItemSignature;
import su.nightexpress.sunlight.utils.Utils;

import java.util.List;

public class SignsCommandsProvider extends CommandProvider<SignsModule> {

    private static final String COMMAND_ROOT   = "sign";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_LOCK   = "lock";
    private static final String COMMAND_UNLOCK = "unlock";
    private static final String COMMAND_INFO   = "info";

    public SignsCommandsProvider(final SignsModule module) {
        super(module, "sign");
    }

    @Override
    public void setup() {
        this.register(COMMAND_DELETE, command -> command
                .withFullDescription(SignsLang.COMMAND_DELETE_DESC.text())
                .withPermission(SignsPerms.COMMAND_DELETE)
                .withRequirement(sender -> sender instanceof Player)
                .withOptionalArguments(CommandArgumentConstants.string(CommandArgumentConstants.NAME,
                        info -> this.signatureSuggestions(info.sender())))
                .executes(this::deleteSignatures))
            .under(COMMAND_ROOT);

        this.register(COMMAND_LOCK, command -> command
                .withFullDescription(SignsLang.COMMAND_LOCK_DESC.text())
                .withPermission(SignsPerms.COMMAND_LOCK)
                .withRequirement(sender -> sender instanceof Player)
                .executes((sender, arguments) -> {
                    return this.setLocked(sender, true);
                }))
            .under(COMMAND_ROOT);

        this.register(COMMAND_UNLOCK, command -> command
                .withFullDescription(SignsLang.COMMAND_UNLOCK_DESC.text())
                .withPermission(SignsPerms.COMMAND_UNLOCK)
                .withRequirement(sender -> sender instanceof Player)
                .executes((sender, arguments) -> {
                    return this.setLocked(sender, false);
                }))
            .under(COMMAND_ROOT);

        this.register(COMMAND_INFO, command -> command
                .withFullDescription(SignsLang.COMMAND_INFO_DESC.text())
                .withPermission(SignsPerms.COMMAND_INFO)
                .withRequirement(sender -> sender instanceof Player)
                .executes(this::showSignatures))
            .under(COMMAND_ROOT);

        this.registerRoot(COMMAND_ROOT, command -> command
            .withFullDescription(SignsLang.COMMAND_SIGN_DESC.text())
            .withPermission(SignsPerms.COMMAND_SIGN)
            .withRequirement(sender -> sender instanceof Player)
            .withOptionalArguments(new GreedyStringArgument(CommandArgumentConstants.TEXT))
            .executes(this::signItem));
    }

    private List<String> signatureSuggestions(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return CommandArgumentConstants.onlinePlayerNames();
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        return this.module.getSignatures(item).stream()
            .map(ItemSignature::ownerName)
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
    }

    private int signItem(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        String text = null;
        final Object textObj = arguments.getUnchecked(CommandArgumentConstants.TEXT);
        if (textObj instanceof final String rawText && !rawText.isBlank()) {
            if (!player.hasPermission(SignsPerms.COMMAND_SIGN_TEXT)) {
                this.module.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, sender);
                return 0;
            }
            text = rawText;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(SignsLang.ERROR_EMPTY_HAND, player);
            return 0;
        }

        final String itemName = ItemUtil.getItemName(item);

        if (this.isBlacklisted(item) && !player.hasPermission(SignsPerms.BYPASS_BLACKLIST)) {
            this.module.sendPrefixed(SignsLang.ERROR_BLACKLISTED, player,
                builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        final int maxStack = SignsConfig.MAX_STACK_SIZE.get();
        if (maxStack > 0 && item.getAmount() > maxStack) {
            this.module.sendPrefixed(SignsLang.ERROR_MAX_STACK, player,
                builder -> builder.with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(maxStack)));
            return 0;
        }

        final List<String> lore = ItemUtil.getLore(item);
        if (SignsConfig.DENY_ITEMS_WITH_LORE.get() && lore != null && !lore.isEmpty()) {
            this.module.sendPrefixed(SignsLang.ERROR_HAS_LORE, player,
                builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        if (this.module.isLocked(item) && !player.hasPermission(SignsPerms.BYPASS_LOCKED)) {
            this.module.sendPrefixed(SignsLang.ERROR_ITEM_LOCKED, player,
                builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        if (this.module.getSignatures(item).stream().anyMatch(signature -> signature.isOwner(player.getUniqueId()))) {
            this.module.sendPrefixed(SignsLang.ERROR_ALREADY_SIGNED, player,
                builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        final int maxSignatures = SignsConfig.MAX_SIGNATURES.get();
        final int signatures = this.module.getSignatures(item).size();
        if (maxSignatures > 0 && signatures >= maxSignatures) {
            this.module.sendPrefixed(SignsLang.ERROR_MAX_SIGNATURES, player, builder -> builder
                .with(SLPlaceholders.GENERIC_ITEM, () -> itemName)
                .with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(maxSignatures)));
            return 0;
        }

        if (this.hasOffhandRequirement(player)) {
            this.module.sendPrefixed(SignsLang.ERROR_OFFHAND_REQUIRED, player, builder -> builder
                .with(SLPlaceholders.GENERIC_TYPE, () -> SignsConfig.OFFHAND_TYPE.get())
                .with(SLPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(SignsConfig.OFFHAND_AMOUNT.get())));
            return 0;
        }

        final int xpCost = SignsConfig.XP_COST.get();
        if (xpCost > 0 && player.getLevel() < xpCost) {
            this.module.sendPrefixed(SignsLang.ERROR_XP_COST, player,
                builder -> builder.with(SLPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(xpCost)));
            return 0;
        }

        final List<String> signatureLore = this.module.buildSignatureLore(player, text, item);
        this.module.addSignature(player, item, text, signatureLore);

        if (xpCost > 0) {
            player.setLevel(Math.max(player.getLevel() - xpCost, 0));
        }

        if (SignsConfig.OFFHAND_ENABLED.get() && SignsConfig.OFFHAND_CONSUME.get()) {
            this.consumeOffhand(player);
        }

        if (SignsConfig.AUTO_LOCK.get()
            && (!SignsConfig.AUTO_LOCK_PERMISSION.get() || player.hasPermission(SignsPerms.COMMAND_LOCK_AUTO))) {
            this.module.setLocked(item, true);
        }

        this.module.sendPrefixed(SignsLang.COMMAND_SIGN_DONE, player,
            builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
        return 1;
    }

    private int deleteSignatures(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(SignsLang.ERROR_EMPTY_HAND, player);
            return 0;
        }

        final String itemName = ItemUtil.getItemName(item);
        final String targetName = arguments.getOrDefaultUnchecked(CommandArgumentConstants.NAME, (String) null);

        final int amount;
        if (targetName == null || targetName.isBlank()) {
            amount = this.module.removeSignatures(item, signature -> signature.isOwner(player.getUniqueId()));
            if (amount == 0) {
                this.module.sendPrefixed(SignsLang.ERROR_NO_SIGNATURES, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
                return 0;
            }
        }
        else {
            if (!player.hasPermission(SignsPerms.COMMAND_DELETE_OTHERS)) {
                this.module.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, sender);
                return 0;
            }

            amount = this.module.removeSignatures(item, signature -> signature.isOwnerName(targetName));
            if (amount == 0) {
                this.module.sendPrefixed(SignsLang.ERROR_NO_SIGNATURES_OTHER, player, builder -> builder
                    .with(SLPlaceholders.GENERIC_NAME, () -> targetName)
                    .with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
                return 0;
            }
        }

        this.module.sendPrefixed(SignsLang.COMMAND_DELETE_DONE, player, builder -> builder
            .with(SLPlaceholders.GENERIC_ITEM, () -> itemName)
            .with(SLPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(amount)));
        return 1;
    }

    private int setLocked(final CommandSender sender, final boolean locked) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(SignsLang.ERROR_EMPTY_HAND, player);
            return 0;
        }

        this.module.setLocked(item, locked);

        this.module.sendPrefixed(locked ? SignsLang.COMMAND_LOCK_DONE : SignsLang.COMMAND_UNLOCK_DONE, player,
            builder -> builder
                .with(SLPlaceholders.GENERIC_ITEM, () -> ItemUtil.getItemName(item))
                .with(SLPlaceholders.GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(locked)));
        return 1;
    }

    private int showSignatures(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(SignsLang.ERROR_EMPTY_HAND, player);
            return 0;
        }

        final String itemName = ItemUtil.getItemName(item);
        final List<ItemSignature> signatures = this.module.getSignatures(item);

        if (signatures.isEmpty()) {
            this.module.sendPrefixed(SignsLang.COMMAND_INFO_EMPTY, player,
                builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 1;
        }

        this.module.sendPrefixed(SignsLang.COMMAND_INFO_HEADER, player, builder -> builder
            .with(SLPlaceholders.GENERIC_ITEM, () -> itemName)
            .with(SLPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(signatures.size())));

        int slot = 1;
        for (final ItemSignature signature : signatures) {
            final int index = slot++;
            this.module.sendPrefixed(SignsLang.COMMAND_INFO_ENTRY, player, builder -> builder
                .with(SLPlaceholders.GENERIC_SLOT, () -> String.valueOf(index))
                .with(SLPlaceholders.GENERIC_NAME, signature::ownerName)
                .with(SLPlaceholders.GENERIC_TIME, () -> this.module.formatSignatureTime(signature.timestamp())));
        }

        this.module.sendPrefixed(SignsLang.COMMAND_INFO_LOCKED, player, builder -> builder
            .with(SLPlaceholders.GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(this.module.isLocked(item))));
        return 1;
    }

    private boolean isBlacklisted(final ItemStack item) {
        return SignsConfig.ITEM_BLACKLIST.get().stream()
            .map(String::trim)
            .filter(name -> !name.isEmpty())
            .anyMatch(name -> {
                final Material material = Material.matchMaterial(name);
                return material != null && material == item.getType();
            });
    }

    private boolean hasOffhandRequirement(final Player player) {
        if (!SignsConfig.OFFHAND_ENABLED.get()) {
            return false;
        }

        final Material material = Material.matchMaterial(SignsConfig.OFFHAND_TYPE.get());
        if (material == null) {
            return false;
        }

        final int required = Math.max(1, SignsConfig.OFFHAND_AMOUNT.get());
        final ItemStack offhand = player.getInventory().getItemInOffHand();
        return offhand.getType() != material || offhand.getAmount() < required;
    }

    private void consumeOffhand(final Player player) {
        final PlayerInventory inventory = player.getInventory();
        final ItemStack offhand = inventory.getItemInOffHand();
        if (offhand.getType().isAir()) {
            return;
        }

        final int amount = Math.max(1, SignsConfig.OFFHAND_AMOUNT.get());
        if (offhand.getAmount() <= amount) {
            inventory.setItemInOffHand(null);
        }
        else {
            offhand.setAmount(offhand.getAmount() - amount);
        }
    }
}
