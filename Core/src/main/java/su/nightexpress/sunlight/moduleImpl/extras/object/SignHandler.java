package su.nightexpress.sunlight.moduleImpl.extras.object;

import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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

    public boolean hasSignatures(final ItemStack item) {
        return PDCUtil.getInt(item, this.keyCount).orElse(0) > 0;
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
        final int index = this.nextIndex(item);
        final ItemMeta meta = item.getItemMeta();

        final List<String> oldLore = meta == null || !meta.hasLore() ? new ArrayList<>()
                : meta.getLore();
        final boolean separator = module.settings().SIGN_SEPARATOR_ENABLED.get() && !oldLore.isEmpty();
        final String separatorLine = separator
                ? su.nightexpress.nightcore.util.text.NightMessage.from(
                        module.settings().SIGN_SEPARATOR_LINE.get(),
                        su.nightexpress.nightcore.util.text.tag.TagPool.ALL_COLORS_AND_STYLES).toLegacy()
                : null;

        final List<String> newLore = new ArrayList<>(oldLore.size() + loreLines.size() + 1);
        if (module.settings().SIGN_ADD_TO_TOP.get()) {
            newLore.addAll(loreLines);
            if (separator) {
                newLore.add(separatorLine);
            }
            newLore.addAll(oldLore);
        } else {
            newLore.addAll(oldLore);
            if (separator) {
                newLore.add(separatorLine);
            }
            newLore.addAll(loreLines);
        }

        ItemUtil.editMeta(item, meta2 -> {
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(this.ownerKey(index), UUID, player.getUniqueId());
            container.set(this.ownerNameKey(index), PersistentDataType.STRING, player.getName());
            container.set(this.timestampKey(index), PersistentDataType.LONG, System.currentTimeMillis());
            container.set(this.textKey(index), PersistentDataType.STRING, text == null ? "" : text);
            container.set(this.loreKey(index), PersistentDataType.STRING, String.join("\n", loreLines));
            container.set(this.keyCount, PersistentDataType.INTEGER, index);

            meta2.setLore(newLore);
        });
    }

    /**
     * Removes all signatures of the item that match the given filter and
     * restores the item lore accordingly.
     *
     * @return The amount of removed signatures.
     */
    public int removeSignatures(final ItemStack item, final Predicate<ItemSignature> filter) {
        final List<ItemSignature> signatures = this.getSignatures(item);
        final List<ItemSignature> removed = signatures.stream().filter(filter).toList();
        if (removed.isEmpty()) {
            return 0;
        }

        ItemUtil.editMeta(item, meta -> {
            if (meta == null || !meta.hasLore()) {
                return;
            }
            final List<String> oldLore = meta.getLore();
            for (final ItemSignature signature : removed) {
                oldLore.removeAll(signature.lore());
            }
            meta.setLore(oldLore.isEmpty() ? null : oldLore);
        });

        final List<ItemSignature> kept = signatures.stream().filter(signature -> !removed.contains(signature)).toList();
        this.rewriteSignatures(item, signatures.size(), kept);
        return removed.size();
    }

    public void editSignature(final ItemStack item, final int index, final Player player,
            final String text, final List<String> newLoreLines) {
        final List<ItemSignature> signatures = this.getSignatures(item);
        if (index < 1 || index > signatures.size()) {
            return;
        }

        final ItemSignature old = signatures.get(index - 1);
        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = meta == null || !meta.hasLore() ? new ArrayList<>() : new ArrayList<>(meta.getLore());

        int start = -1;
        if (!old.lore().isEmpty() && lore.size() >= old.lore().size()) {
            for (int i = 0; i <= lore.size() - old.lore().size(); i++) {
                boolean match = true;
                for (int j = 0; j < old.lore().size(); j++) {
                    if (!lore.get(i + j).equals(old.lore().get(j))) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    start = i;
                    break;
                }
            }
        }

        if (start >= 0) {
            for (int j = 0; j < old.lore().size(); j++) {
                lore.remove(start);
            }
            lore.addAll(start, newLoreLines);
        } else {
            for (final String line : old.lore()) {
                final Iterator<String> iterator = lore.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().equals(line)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            lore.addAll(newLoreLines);
        }

        ItemUtil.editMeta(item, meta2 -> {
            if (lore.isEmpty()) {
                meta2.setLore(null);
            } else {
                meta2.setLore(lore);
            }
        });

        PDCUtil.set(item, this.textKey(index), text == null ? "" : text);
        PDCUtil.set(item, this.loreKey(index), String.join("\\n", newLoreLines));
        PDCUtil.set(item, this.ownerNameKey(index), player.getName());
    }

    public void moveSignature(final ItemStack item, final int from, final int to) {
        final List<ItemSignature> signatures = this.getSignatures(item);
        if (from < 1 || from > signatures.size() || to < 1 || to > signatures.size() || from == to) {
            return;
        }

        final ItemSignature moved = signatures.remove(from - 1);
        signatures.add(to - 1, moved);

        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = meta == null || !meta.hasLore() ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        final List<String> baseLore = new ArrayList<>();
        final String separatorLine = su.nightexpress.nightcore.util.text.NightMessage.from(
                module.settings().SIGN_SEPARATOR_LINE.get(),
                su.nightexpress.nightcore.util.text.tag.TagPool.ALL_COLORS_AND_STYLES).toLegacy();
        for (final String line : lore) {
            boolean isSignatureLine = false;
            for (final ItemSignature signature : signatures) {
                if (signature.lore().contains(line)) {
                    isSignatureLine = true;
                    break;
                }
            }
            if (!isSignatureLine && !line.equals(separatorLine)) {
                baseLore.add(line);
            }
        }

        final List<String> newLore = new ArrayList<>(baseLore);
        if (!baseLore.isEmpty() && !signatures.isEmpty() && module.settings().SIGN_SEPARATOR_ENABLED.get()) {
            newLore.add(separatorLine);
        }
        for (final ItemSignature signature : signatures) {
            newLore.addAll(signature.lore());
        }

        ItemUtil.editMeta(item, meta2 -> {
            if (newLore.isEmpty()) {
                meta2.setLore(null);
            } else {
                meta2.setLore(newLore);
            }
        });

        this.rewriteSignatures(item, signatures.size() + 1, signatures);
    }

    public void spawnSignEffects(final Player player) {
        if (module.settings().SIGN_SOUNDS_ENABLED.get()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
        if (module.settings().SIGN_PARTICLES_ENABLED.get()) {
            try {
                final org.bukkit.Particle particle = org.bukkit.Particle.valueOf(
                        module.settings().SIGN_PARTICLE_TYPE.get().toUpperCase());
                player.spawnParticle(particle, player.getLocation().add(0, 1, 0),
                        module.settings().SIGN_PARTICLE_COUNT.get());
            } catch (final IllegalArgumentException ignored) {
            }
        }
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
