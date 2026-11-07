package su.nightexpress.sunlight.moduleImpl.extras.object;

import java.util.List;
import java.util.UUID;

/**
 * A signature applied to an item. Signatures are stored in the item's
 * {@link org.bukkit.persistence.PersistentDataContainer}, one entry per index.
 *
 * @param ownerUuid The UUID of the player who signed the item.
 * @param ownerName The plain name of the player who signed the item at the
 *                  time of signing.
 * @param timestamp The creation time of the signature in millis.
 * @param text      The raw text input of the signature, empty when signed with
 *                  no text.
 * @param lore      The rendered lore lines this signature has added to the
 *                  item, used for precise lore restore on removal.
 */
public record ItemSignature(UUID ownerUuid,
                            String ownerName,
                            long timestamp,
                            String text,
                            List<String> lore) {

    public boolean isOwner(final UUID uuid) {
        return this.ownerUuid.equals(uuid);
    }

    public boolean isOwnerName(final String name) {
        return this.ownerName.equalsIgnoreCase(name);
    }
}
