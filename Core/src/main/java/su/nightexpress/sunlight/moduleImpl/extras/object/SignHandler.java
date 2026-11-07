package su.nightexpress.sunlight.moduleImpl.extras.object;

import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.clip.placeholderapi.PlaceholderAPI;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.text.tag.TagPool;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasModule;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasPerms;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.sunlight.utils.Utils;

public class SignHandler {
    private final NamespacedKey keyLocked;
    private final NamespacedKey keyCount;
    private final ExtrasModule module;

    public SignHandler(final ExtrasModule module) {
        this.module = module;
        this.keyLocked = new NamespacedKey(module.plugin(), "sign.locked");
        this.keyCount = new NamespacedKey(module.plugin(), "sign.signature.count");
    }

    public boolean isLocked(final ItemStack item) {
        return PDCUtil.getBoolean(item, this.keyLocked).orElse(false);
    }

    public void setLocked(final ItemStack item, final boolean locked) {
        if (locked) {
            PDCUtil.set(item, this.keyLocked, true);
        } else {
            PDCUtil.remove(item, this.keyLocked);
        }
    }

    public List<ItemSignature> getSignatures(final ItemStack item) {
        final int amount = PDCUtil.getInt(item, this.keyCount).orElse(0);
        final List<ItemSignature> signatures = new ArrayList<>(amount);

        for (int index = 1; index <= amount; index++) {
            final ItemSignature signature = this.getSignature(item, index);
            if (signature != null) {
                signatures.add(signature);
            }
        }
        return signatures;
    }

    private ItemSignature getSignature(final ItemStack item, final int index) {
        final UUID ownerUuid = PDCUtil.getUUID(item, this.ownerKey(index)).orElse(null);
        if (ownerUuid == null) {
            return null;
        }

        final String ownerName = PDCUtil.getString(item, this.ownerNameKey(index)).orElse("Unknown");
        final long timestamp = PDCUtil.getLong(item, this.timestampKey(index)).orElse(0L);
        final String text = PDCUtil.getString(item, this.textKey(index)).orElse("");
        final List<String> lore = new ArrayList<>(
                List.of(PDCUtil.getString(item, this.loreKey(index)).orElse("").split("\n", -1)));

        return new ItemSignature(ownerUuid, ownerName, timestamp, text, lore);
    }

    public void addSignature(final Player player, final ItemStack item, final String text,
            final List<String> loreLines) {
        final long timestamp = System.currentTimeMillis();
        final int index = this.nextIndex(item);
        final ItemMeta meta = item.getItemMeta();

        final List<String> oldLore = meta == null || !meta.hasLore() ? new ArrayList<>()
                : meta.getLore();
        final boolean onTop = module.settings().SIGN_ADD_TO_TOP.get();
        final List<String> newLore = new ArrayList<>(oldLore.size() + loreLines.size());
        if (onTop) {
            newLore.addAll(loreLines);
            newLore.addAll(oldLore);
        } else {
            newLore.addAll(oldLore);
            newLore.addAll(loreLines);
        }

        ItemUtil.editMeta(item, meta2 -> {
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(this.ownerKey(index), UUID, player.getUniqueId());
            container.set(this.ownerNameKey(index), PersistentDataType.STRING, player.getName());
            container.set(this.timestampKey(index), PersistentDataType.LONG, timestamp);
            container.set(this.textKey(index), PersistentDataType.STRING, text == null ? "" : text);
            container.set(this.loreKey(index), PersistentDataType.STRING, String.join("\n", loreLines));
            container.set(this.keyCount, PersistentDataType.INTEGER, index);

            meta2.setLore(newLore);
        });

        PDCUtil.set(item, this.ownerKey(index), player.getUniqueId());
        PDCUtil.set(item, this.ownerNameKey(index), player.getName());
        PDCUtil.set(item, this.timestampKey(index), timestamp);
        PDCUtil.set(item, this.textKey(index), text == null ? "" : text);
        PDCUtil.set(item, this.loreKey(index), String.join("\n", loreLines));
    }

    /**
     * Removes all signatures of the item that match the given filter and
     * restores the item lore accordingly.
     *
     * @return The amount of removed signatures.
     */
    public int removeSignatures(final ItemStack item, final Predicate<ItemSignature> filter) {
        final List<ItemSignature> signatures = this.getSignatures(item);
        final List<ItemSignature> removed = signatures.stream().filter(filter).collect(Collectors.toList());
        if (removed.isEmpty()) {
            return 0;
        }
        final ItemMeta meta = item.getItemMeta();

        final List<String> oldLore = meta == null || !meta.hasLore() ? new ArrayList<>()
                : meta.getLore();
        for (final ItemSignature signature : removed) {
            for (final String line : signature.lore()) {
                final Iterator<String> iterator = oldLore.iterator();
                while (iterator.hasNext()) {
                    final String loreLine = iterator.next();
                    if (loreLine.equals(line)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        ItemUtil.editMeta(item, meta2 -> {
            if (oldLore.isEmpty()) {
                meta2.setLore(null);
            } else {
                meta2.setLore(oldLore);
            }
        });

        final List<ItemSignature> kept = signatures.stream().filter(signature -> !removed.contains(signature))
                .collect(Collectors.toList());
        this.rewriteSignatures(item, signatures.size(), kept);
        return removed.size();
    }

    private void rewriteSignatures(final ItemStack item, final int oldAmount, final List<ItemSignature> signatures) {
        for (int index = 1; index <= oldAmount; index++) {
            PDCUtil.remove(item, this.ownerKey(index));
            PDCUtil.remove(item, this.ownerNameKey(index));
            PDCUtil.remove(item, this.timestampKey(index));
            PDCUtil.remove(item, this.textKey(index));
            PDCUtil.remove(item, this.loreKey(index));
        }

        int index = 1;
        for (final ItemSignature signature : signatures) {
            PDCUtil.set(item, this.ownerKey(index), signature.ownerUuid());
            PDCUtil.set(item, this.ownerNameKey(index), signature.ownerName());
            PDCUtil.set(item, this.timestampKey(index), signature.timestamp());
            PDCUtil.set(item, this.textKey(index), signature.text());
            PDCUtil.set(item, this.loreKey(index), String.join("\n", signature.lore()));
            index++;
        }

        if (signatures.isEmpty()) {
            PDCUtil.remove(item, this.keyCount);
        } else {
            PDCUtil.set(item, this.keyCount, signatures.size());
        }
    }

    private int nextIndex(final ItemStack item) {
        return PDCUtil.getInt(item, this.keyCount).orElse(0) + 1;
    }

    private NamespacedKey ownerKey(final int index) {
        return new NamespacedKey(module.plugin(), "sign.signature." + index + ".owner");
    }

    private NamespacedKey ownerNameKey(final int index) {
        return new NamespacedKey(module.plugin(), "sign.signature." + index + ".owner_name");
    }

    private NamespacedKey timestampKey(final int index) {
        return new NamespacedKey(module.plugin(), "sign.signature." + index + ".time");
    }

    private NamespacedKey textKey(final int index) {
        return new NamespacedKey(module.plugin(), "sign.signature." + index + ".text");
    }

    private NamespacedKey loreKey(final int index) {
        return new NamespacedKey(module.plugin(), "sign.signature." + index + ".lore");
    }

    public List<String> buildSignatureLore(final Player player, final String text, final ItemStack item) {
        final long timestamp = System.currentTimeMillis();
        final List<String> template = text == null || text.isBlank()
                ? module.settings().SIGN_CONTENT_SHORT.get()
                : module.settings().SIGN_CONTENT_DEFAULT.get();

        final String date = DateTimeFormatter.ofPattern(
                module.settings().SIGN_FORMAT_DATE.get())
                .format(TimeUtil.getLocalDateTimeOf(timestamp));
        final String time = DateTimeFormatter.ofPattern(
                module.settings().SIGN_FORMAT_TIME.get())
                .format(TimeUtil.getLocalDateTimeOf(timestamp));

        final String parsedText = this.isColorsAllowed(player)
                ? text
                : NightMessage.stripTags(text);

        final List<String> lore = new ArrayList<>();
        for (final String rawLine : template) {
            String line = rawLine
                    .replace("%text%", parsedText == null ? "" : parsedText)
                    .replace("%player%", player.getName())
                    .replace("%date%", date)
                    .replace("%time%", time);

            line = NightMessage.from(line, TagPool.ALL_COLORS_AND_STYLES).toLegacy();

            if (Utils.hasPlaceholderAPI()) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            lore.add(line);
        }
        return lore;
    }

    public boolean isColorsAllowed(final Player player) {
        return module.settings().SIGN_ALLOW_COLORS.get() && player.hasPermission(ExtrasPerms.SIGN_COMMAND_COLORS);
    }

    public String formatSignatureDate(final long timestamp) {
        return DateTimeFormatter.ofPattern(
                module.settings().SIGN_FORMAT_DATE.get())
                .format(TimeUtil.getLocalDateTimeOf(timestamp));
    }

    public String formatSignatureTime(final long timestamp) {
        return DateTimeFormatter.ofPattern(
                module.settings().SIGN_FORMAT_TIME.get())
                .format(TimeUtil.getLocalDateTimeOf(timestamp));
    }

    public static final PersistentDataType<byte[], UUID> UUID = new UUIDDataType();

    public static class UUIDDataType implements PersistentDataType<byte[], UUID> {

        @Override
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte[] toPrimitive(final UUID complex, final PersistentDataAdapterContext context) {
            final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(complex.getMostSignificantBits());
            bb.putLong(complex.getLeastSignificantBits());
            return bb.array();
        }

        @Override
        public UUID fromPrimitive(final byte[] primitive, final PersistentDataAdapterContext context) {
            final ByteBuffer bb = ByteBuffer.wrap(primitive);
            return new UUID(bb.getLong(), bb.getLong());
        }
    }
}
