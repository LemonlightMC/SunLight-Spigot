package su.nightexpress.sunlight.moduleImpl.extras;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.sunlight.SLPlaceholders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class StarterKitSettings extends AbstractConfig {

    private final ConfigProperty<Boolean> enabled = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.Enabled",
            true,
            "Controls whether the Starter Kit feature is enabled.");

    private final ConfigProperty<Boolean> firstJoinOnly = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.FirstJoinOnly",
            true,
            "If enabled, Starter Kit is only given to players on their first join.");

    private final ConfigProperty<Boolean> teleportToSpawn = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.TeleportToSpawn",
            true,
            "If enabled, players receive Starter Kit at spawn location.");

    private final ConfigProperty<Boolean> clearInventory = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.ClearInventory",
            true,
            "If enabled, player's inventory is cleared before giving Starter Kit.");

    private final ConfigProperty<Boolean> giveEffects = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.GiveEffects",
            true,
            "If enabled, players receive configured effects with Starter Kit.");

    private final ConfigProperty<Boolean> giveParticles = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.GiveParticles",
            true,
            "If enabled, players receive configured particles with Starter Kit.");

    private final ConfigProperty<Boolean> giveFireworks = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.GiveFireworks",
            true,
            "If enabled, players receive configured fireworks with Starter Kit.");

    private final ConfigProperty<Boolean> giveSounds = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.GiveSounds",
            true,
            "If enabled, players receive configured sounds with Starter Kit.");

    private final ConfigProperty<Boolean> welcomeMessage = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.WelcomeMessage",
            true,
            "If enabled, players receive a welcome message with Starter Kit.");

    private final ConfigProperty<Boolean> globalAnnouncement = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.GlobalAnnouncement",
            true,
            "If enabled, a global announcement is made when a player receives Starter Kit.");

    private final ConfigProperty<List<String>> welcomeMessageFormat = this.addProperty(ConfigTypes.STRING_LIST,
            "StarterKit.WelcomeMessageFormat",
            List.of(
                    DARK_GRAY.and(STRIKETHROUGH).wrap("-".repeat(32)),
                    GRAY.wrap("Welcome " + WHITE.wrap(SLPlaceholders.PLAYER_DISPLAY_NAME) + " to the server!"),
                    GRAY.wrap("You have received your Starter Kit!"),
                    DARK_GRAY.and(STRIKETHROUGH).wrap("-".repeat(32))
            ),
            "Format for the welcome message.");

    private final ConfigProperty<List<String>> globalAnnouncementFormat = this.addProperty(ConfigTypes.STRING_LIST,
            "StarterKit.GlobalAnnouncementFormat",
            List.of(
                    DARK_GRAY.and(STRIKETHROUGH).wrap("-".repeat(32)),
                    GRAY.wrap(WHITE.wrap(SLPlaceholders.PLAYER_DISPLAY_NAME) + " has joined the server and received their Starter Kit!"),
                    DARK_GRAY.and(STRIKETHROUGH).wrap("-".repeat(32))
            ),
            "Format for the global announcement.");

    private static final ConfigType<List<ItemStack>> STARTER_ITEMS_TYPE = ConfigType.of(
            StarterKitSettings::readStarterItems,
            StarterKitSettings::writeStarterItems);

    private final ConfigProperty<List<ItemStack>> starterItems = this.addProperty(STARTER_ITEMS_TYPE,
            "StarterKit.Items",
            List.of(
                    NightItem.fromType(Material.WOODEN_SWORD).getItemStack(),
                    NightItem.fromType(Material.BREAD).setAmount(5).getItemStack(),
                    NightItem.fromType(Material.WOODEN_PICKAXE).getItemStack(),
                    NightItem.fromType(Material.WOODEN_AXE).getItemStack()
            ),
            "List of items to give to players with Starter Kit.");

    private static List<ItemStack> readStarterItems(FileConfig config, String path) {
        List<ItemStack> result = new ArrayList<>();
        Set<String> keys = config.getSection(path);
        if (keys != null && !keys.isEmpty()) {
            List<String> sorted = new ArrayList<>(keys);
            sorted.sort(Comparator.comparingInt(key -> {
                try {
                    return Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE;
                }
            }));
            for (String key : sorted) {
                try {
                    NightItem nightItem = NightItem.read(config, path + "." + key);
                    ItemStack itemStack = nightItem.getItemStack();
                    if (itemStack != null && !itemStack.getType().isAir()) {
                        result.add(itemStack);
                    }
                } catch (Exception ignored) {
                }
            }
            return result;
        }

        List<?> list = config.getList(path);
        if (list != null) {
            for (Object entry : list) {
                if (entry instanceof ItemStack itemStack && !itemStack.getType().isAir()) {
                    result.add(itemStack);
                }
            }
        }
        return result;
    }

    private static void writeStarterItems(FileConfig config, String path, List<ItemStack> items) {
        config.set(path, null);
        if (items == null) {
            return;
        }
        int index = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            NightItem.fromItemStack(item).write(config, path + "." + index);
            index++;
        }
    }

    private final ConfigProperty<Map<String, Integer>> starterEffects = this.addProperty(
            ConfigTypes.forMapWithLowerKeys(ConfigTypes.INT),
            "StarterKit.Effects",
            Map.of(
                    "SPEED", 2,
                    "JUMP_BOOST", 1
            ),
            "List of effects to give to players with Starter Kit.");

    private final ConfigProperty<Map<String, Integer>> starterParticles = this.addProperty(
            ConfigTypes.forMapWithLowerKeys(ConfigTypes.INT),
            "StarterKit.Particles",
            Map.of(
                    "FLAME", 1
            ),
            "List of particles to give to players with Starter Kit.");

    private final ConfigProperty<Map<String, Integer>> starterFireworks = this.addProperty(
            ConfigTypes.forMapWithLowerKeys(ConfigTypes.INT),
            "StarterKit.Fireworks",
            Map.of(
                    "STAR", 1
            ),
            "List of fireworks to give to players with Starter Kit.");

    private final ConfigProperty<Map<String, Integer>> starterSounds = this.addProperty(
            ConfigTypes.forMapWithLowerKeys(ConfigTypes.INT),
            "StarterKit.Sounds",
            Map.of(
                    "ENTITY_PLAYER_LEVELUP", 1
            ),
            "List of sounds to give to players with Starter Kit.");

    public boolean isEnabled() {
        return this.enabled.get();
    }

    public boolean isFirstJoinOnly() {
        return this.firstJoinOnly.get();
    }

    public boolean isTeleportToSpawn() {
        return this.teleportToSpawn.get();
    }

    public boolean isClearInventory() {
        return this.clearInventory.get();
    }

    public boolean isGiveEffects() {
        return this.giveEffects.get();
    }

    public boolean isGiveParticles() {
        return this.giveParticles.get();
    }

    public boolean isGiveFireworks() {
        return this.giveFireworks.get();
    }

    public boolean isGiveSounds() {
        return this.giveSounds.get();
    }

    public boolean isWelcomeMessage() {
        return this.welcomeMessage.get();
    }

    public boolean isGlobalAnnouncement() {
        return this.globalAnnouncement.get();
    }

    public List<String> getWelcomeMessageFormat() {
        return this.welcomeMessageFormat.get();
    }

    public List<String> getGlobalAnnouncementFormat() {
        return this.globalAnnouncementFormat.get();
    }

    public List<ItemStack> getStarterItems() {
        return this.starterItems.get();
    }

    public Map<String, Integer> getStarterEffects() {
        return this.starterEffects.get();
    }

    public Map<String, Integer> getStarterParticles() {
        return this.starterParticles.get();
    }

    public Map<String, Integer> getStarterFireworks() {
        return this.starterFireworks.get();
    }

    public Map<String, Integer> getStarterSounds() {
        return this.starterSounds.get();
    }
}