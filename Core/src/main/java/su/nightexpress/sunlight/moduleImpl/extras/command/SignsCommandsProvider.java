package su.nightexpress.sunlight.moduleImpl.extras.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasModule;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasPerms;
import su.nightexpress.sunlight.moduleImpl.extras.SignsLang;
import su.nightexpress.sunlight.moduleImpl.extras.object.ItemSignature;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SignsCommandsProvider extends CommandProvider<ExtrasModule> {

    private static final String COMMAND_ROOT = "sign";

    private final Map<UUID, Long> pendingDelete = new ConcurrentHashMap<>();

    public SignsCommandsProvider(final ExtrasModule module) {
        super(module, "sign");
    }

    @Override
    public void setup() {
        this.register("delete", command -> command
                .withFullDescription(SignsLang.COMMAND_DELETE_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_DELETE)
                .withRequirement(sender -> sender instanceof Player)
                .withOptionalArguments(CommandArgumentConstants.string(CommandArgumentConstants.NAME,
                        info -> this.signatureSuggestions(info.sender())))
                .executes(this::deleteSignatures))
                .under(COMMAND_ROOT);

        this.register("lock", command -> command
                .withFullDescription(SignsLang.COMMAND_LOCK_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_LOCK)
                .withRequirement(sender -> sender instanceof Player)
                .executes((sender, arguments) -> {
                    return this.setLocked(sender, true);
                }))
                .under(COMMAND_ROOT);

        this.register("unlock", command -> command
                .withFullDescription(SignsLang.COMMAND_UNLOCK_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_UNLOCK)
                .withRequirement(sender -> sender instanceof Player)
                .executes((sender, arguments) -> {
                    return this.setLocked(sender, false);
                }))
                .under(COMMAND_ROOT);

        this.register("info", command -> command
                .withFullDescription(SignsLang.COMMAND_INFO_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_INFO)
                .withRequirement(sender -> sender instanceof Player)
                .executes(this::showSignatures))
                .under(COMMAND_ROOT);

        this.register("edit", command -> command
                .withFullDescription(SignsLang.COMMAND_EDIT_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_EDIT)
                .withRequirement(sender -> sender instanceof Player)
                .withArguments(new IntegerArgument("index"),
                        new GreedyStringArgument(CommandArgumentConstants.TEXT))
                .executes(this::editSignature))
                .under(COMMAND_ROOT);

        this.register("move", command -> command
                .withFullDescription(SignsLang.COMMAND_MOVE_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_MOVE)
                .withRequirement(sender -> sender instanceof Player)
                .withArguments(new IntegerArgument("from"), new IntegerArgument("to"))
                .executes(this::moveSignatures))
                .under(COMMAND_ROOT);

        this.register("wipe", command -> command
                .withFullDescription(SignsLang.COMMAND_WIPE_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_WIPE)
                .withRequirement(sender -> sender instanceof Player)
                .withOptionalArguments(CommandArgumentConstants.string(CommandArgumentConstants.NAME,
                        info -> CommandArgumentConstants.onlinePlayerNames()))
                .executes(this::wipeSignatures))
                .under(COMMAND_ROOT);

        this.registerRoot(COMMAND_ROOT, command -> command
                .withFullDescription(SignsLang.COMMAND_SIGN_DESC.text())
                .withPermission(ExtrasPerms.SIGN_COMMAND_SIGN)
                .withRequirement(sender -> sender instanceof Player)
                .withOptionalArguments(new GreedyStringArgument(CommandArgumentConstants.TEXT))
                .executes(this::signItem));
    }

    private List<String> signatureSuggestions(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return CommandArgumentConstants.onlinePlayerNames();
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        return this.module.signHandler().getSignatures(item).stream()
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
            if (!player.hasPermission(ExtrasPerms.SIGN_COMMAND_SIGN_TEXT)) {
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

        final String itemName = ItemUtil.getNameSerialized(item);

        if (this.isBlacklisted(item) && !player.hasPermission(ExtrasPerms.SIGN_BYPASS_BLACKLIST)) {
            this.module.sendPrefixed(SignsLang.ERROR_BLACKLISTED, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        final int maxStack = module.settings().SIGN_MAX_STACK_SIZE.get();
        if (maxStack > 0 && item.getAmount() > maxStack) {
            this.module.sendPrefixed(SignsLang.ERROR_MAX_STACK, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(maxStack)));
            return 0;
        }

        final ItemMeta meta = item.getItemMeta();
        final List<Component> lore = (meta == null || !meta.hasLore()) ? List.of() : meta.lore();
        if (module.settings().SIGN_DENY_ITEMS_WITH_LORE.get() && lore != null && !lore.isEmpty()) {
            this.module.sendPrefixed(SignsLang.ERROR_HAS_LORE, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        if (this.module.signHandler().isLocked(item) && !player.hasPermission(ExtrasPerms.SIGN_BYPASS_LOCKED)) {
            this.module.sendPrefixed(SignsLang.ERROR_ITEM_LOCKED, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        if (this.module.signHandler().getSignatures(item).stream()
                .anyMatch(signature -> signature.isOwner(player.getUniqueId()))) {
            this.module.sendPrefixed(SignsLang.ERROR_ALREADY_SIGNED, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        final int maxSignatures = module.settings().SIGN_MAX_SIGNATURES.get();
        final int signatures = this.module.signHandler().getSignatures(item).size();
        if (maxSignatures > 0 && signatures >= maxSignatures) {
            this.module.sendPrefixed(SignsLang.ERROR_MAX_SIGNATURES, player, builder -> builder
                    .with(SLPlaceholders.GENERIC_ITEM, () -> itemName)
                    .with(SLPlaceholders.GENERIC_MAX, () -> String.valueOf(maxSignatures)));
            return 0;
        }

        if (this.hasOffhandRequirement(player)) {
            this.module.sendPrefixed(SignsLang.ERROR_OFFHAND_REQUIRED, player, builder -> builder
                    .with(SLPlaceholders.GENERIC_TYPE, () -> module.settings().SIGN_OFFHAND_TYPE.get())
                    .with(SLPlaceholders.GENERIC_AMOUNT,
                            () -> String.valueOf(module.settings().SIGN_OFFHAND_AMOUNT.get())));
            return 0;
        }

        final int xpCost = module.settings().SIGN_XP_COST.get();
        if (xpCost > 0 && player.getLevel() < xpCost) {
            this.module.sendPrefixed(SignsLang.ERROR_XP_COST, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(xpCost)));
            return 0;
        }

        final List<String> signatureLore = this.module.signHandler().buildSignatureLore(player, text, item);
        this.module.signHandler().addSignature(player, item, text, signatureLore);

        if (xpCost > 0) {
            player.setLevel(Math.max(player.getLevel() - xpCost, 0));
        }

        if (module.settings().SIGN_OFFHAND_ENABLED.get() && module.settings().SIGN_OFFHAND_CONSUME.get()) {
            this.consumeOffhand(player);
        }

        if (module.settings().SIGN_AUTO_LOCK.get()
                && (!module.settings().SIGN_AUTO_LOCK_PERMISSION.get()
                        || player.hasPermission(ExtrasPerms.SIGN_COMMAND_LOCK_AUTO))) {
            this.module.signHandler().setLocked(item, true);
        }

        if (module.settings().SIGN_BOOKS_AUTO_SIGN.get() && item.getType().name().equals("WRITTEN_BOOK")) {
            try {
                if (item.getItemMeta() instanceof org.bukkit.inventory.meta.BookMeta bm) {
                    bm.setAuthor(player.getName());
                    item.setItemMeta(bm);
                }
            } catch (Throwable ignored) {
            }
        }

        this.module.signHandler().spawnSignEffects(player);

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

        final String itemName = ItemUtil.getNameSerialized(item);
        final String targetName = arguments.getOrDefaultUnchecked(CommandArgumentConstants.NAME, (String) null);

        final int amount;
        if (targetName == null || targetName.isBlank()) {
            amount = this.module.signHandler().removeSignatures(item,
                    signature -> signature.isOwner(player.getUniqueId()));
            if (amount == 0) {
                this.module.sendPrefixed(SignsLang.ERROR_NO_SIGNATURES, player,
                        builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
                return 0;
            }
        } else {
            if (!player.hasPermission(ExtrasPerms.SIGN_COMMAND_DELETE_OTHERS)) {
                this.module.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, sender);
                return 0;
            }

            amount = this.module.signHandler().removeSignatures(item, signature -> signature.isOwnerName(targetName));
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

        this.module.signHandler().setLocked(item, locked);

        this.module.sendPrefixed(locked ? SignsLang.COMMAND_LOCK_DONE : SignsLang.COMMAND_UNLOCK_DONE, player,
                builder -> builder
                        .with(SLPlaceholders.GENERIC_ITEM, () -> ItemUtil.getNameSerialized(item))
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

        final String itemName = ItemUtil.getNameSerialized(item);
        final List<ItemSignature> signatures = this.module.signHandler().getSignatures(item);

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
                    .with(SLPlaceholders.GENERIC_TIME,
                            () -> this.module.signHandler().formatSignatureTime(signature.timestamp())));
        }

        this.module.sendPrefixed(SignsLang.COMMAND_INFO_LOCKED, player, builder -> builder
                .with(SLPlaceholders.GENERIC_STATE,
                        () -> CoreLang.STATE_ENABLED_DISALBED.get(this.module.signHandler().isLocked(item))));
        return 1;
    }

    private int editSignature(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final Object indexObj = arguments.get("index");
        final Object textObj = arguments.get(CommandArgumentConstants.TEXT);
        if (!(indexObj instanceof final Integer index) || !(textObj instanceof final String text) || text.isBlank()) {
            return 0;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(SignsLang.ERROR_EMPTY_HAND, player);
            return 0;
        }

        final String itemName = ItemUtil.getNameSerialized(item);
        final List<ItemSignature> signatures = this.module.signHandler().getSignatures(item);
        if (index < 1 || index > signatures.size()) {
            this.module.sendPrefixed(SignsLang.ERROR_INVALID_INDEX, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_SLOT, () -> String.valueOf(index)));
            return 0;
        }

        final ItemSignature signature = signatures.get(index - 1);
        if (!signature.isOwner(player.getUniqueId()) && !player.hasPermission(ExtrasPerms.SIGN_COMMAND_DELETE_OTHERS)) {
            this.module.sendPrefixed(SignsLang.ERROR_NOT_OWNER, player);
            return 0;
        }

        final List<String> newLore = this.module.signHandler().buildSignatureLore(player, text, item);
        this.module.signHandler().editSignature(item, index, player, text, newLore);

        this.module.sendPrefixed(SignsLang.COMMAND_EDIT_DONE, player, builder -> builder
                .with(SLPlaceholders.GENERIC_ITEM, () -> itemName)
                .with(SLPlaceholders.GENERIC_SLOT, () -> String.valueOf(index)));
        return 1;
    }

    private int moveSignatures(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final Object fromObj = arguments.get("from");
        final Object toObj = arguments.get("to");
        if (!(fromObj instanceof final Integer from) || !(toObj instanceof final Integer to)) {
            return 0;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(SignsLang.ERROR_EMPTY_HAND, player);
            return 0;
        }

        final List<ItemSignature> signatures = this.module.signHandler().getSignatures(item);
        if (from < 1 || from > signatures.size() || to < 1 || to > signatures.size()) {
            this.module.sendPrefixed(SignsLang.ERROR_INVALID_INDEX, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_SLOT, () -> String.valueOf(from)));
            return 0;
        }

        this.module.signHandler().moveSignature(item, from, to);

        this.module.sendPrefixed(SignsLang.COMMAND_MOVE_DONE, player, builder -> builder
                .with(SLPlaceholders.GENERIC_SLOT, () -> String.valueOf(from))
                .with(SLPlaceholders.GENERIC_VALUE, () -> String.valueOf(to)));
        return 1;
    }

    private int wipeSignatures(final CommandSender sender, final CommandArguments arguments) {
        if (!(sender instanceof final Player player)) {
            this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
            return 0;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.module.sendPrefixed(SignsLang.ERROR_EMPTY_HAND, player);
            return 0;
        }

        final String itemName = ItemUtil.getNameSerialized(item);
        final String targetName = arguments.getOrDefaultUnchecked(CommandArgumentConstants.NAME, (String) null);

        final int amount;
        if (targetName == null || targetName.isBlank()) {
            amount = this.module.signHandler().removeSignatures(item, signature -> true);
        } else {
            if (!player.hasPermission(ExtrasPerms.SIGN_COMMAND_DELETE_OTHERS)) {
                this.module.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, sender);
                return 0;
            }
            amount = this.module.signHandler().removeSignatures(item, signature -> signature.isOwnerName(targetName));
        }

        if (amount == 0) {
            this.module.sendPrefixed(SignsLang.ERROR_NO_SIGNATURES, player,
                    builder -> builder.with(SLPlaceholders.GENERIC_ITEM, () -> itemName));
            return 0;
        }

        this.module.sendPrefixed(SignsLang.COMMAND_WIPE_DONE, player, builder -> builder
                .with(SLPlaceholders.GENERIC_ITEM, () -> itemName)
                .with(SLPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(amount)));
        return 1;
    }

    private boolean isBlacklisted(final ItemStack item) {
        return module.settings().SIGN_ITEM_BLACKLIST.get().stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .anyMatch(name -> {
                    final Material material = Material.matchMaterial(name);
                    return material != null && material == item.getType();
                });
    }

    private boolean hasOffhandRequirement(final Player player) {
        if (!module.settings().SIGN_OFFHAND_ENABLED.get()) {
            return false;
        }

        final Material material = Material.matchMaterial(module.settings().SIGN_OFFHAND_TYPE.get());
        if (material == null) {
            return false;
        }

        final int required = Math.max(1, module.settings().SIGN_OFFHAND_AMOUNT.get());
        final ItemStack offhand = player.getInventory().getItemInOffHand();
        return offhand.getType() != material || offhand.getAmount() < required;
    }

    private void consumeOffhand(final Player player) {
        final PlayerInventory inventory = player.getInventory();
        final ItemStack offhand = inventory.getItemInOffHand();
        if (offhand.getType().isAir()) {
            return;
        }

        final int amount = Math.max(1, module.settings().SIGN_OFFHAND_AMOUNT.get());
        if (offhand.getAmount() <= amount) {
            inventory.setItemInOffHand(null);
        } else {
            offhand.setAmount(offhand.getAmount() - amount);
        }
    }
}
