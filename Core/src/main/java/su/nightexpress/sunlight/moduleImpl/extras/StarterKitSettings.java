package su.nightexpress.sunlight.moduleImpl.extras;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.Numbers;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.sunlight.SLPlaceholders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class StarterKitSettings extends AbstractConfig {

    private final ConfigProperty<Boolean> enabled = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.Enabled",
            true,
            "Controls whether the Starter Kit feature is enabled.");

    private final ConfigProperty<Boolean> requirePermission = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.RequirePermission",
            false,
            "If enabled, only players with the '" + ExtrasPerms.STARTER_KIT + "' permission receive the Starter Kit.",
            "Players with '" + ExtrasPerms.STARTER_KIT_BYPASS + "' never receive the Starter Kit.");

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

    private final ConfigProperty<Boolean> giveXP = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.GiveXP.Enabled",
            true,
            "If enabled, players receive configured XP with Starter Kit.");

    private final ConfigProperty<Integer> xpAmount = this.addProperty(ConfigTypes.INT,
            "StarterKit.GiveXP.Amount",
            0,
            "Amount of XP points to give to players with Starter Kit.");

    private final ConfigProperty<Integer> xpLevels = this.addProperty(ConfigTypes.INT,
            "StarterKit.GiveXP.Levels",
            0,
            "Amount of XP levels to give to players with Starter Kit.");

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

    private final ConfigProperty<String> soundAudience = this.addProperty(ConfigTypes.STRING,
            "StarterKit.SoundsAudience",
            StarterKitSoundAudience.PLAYER.name(),
            "Who hears the Starter Kit sounds: " + String.join(", ", java.util.Arrays.stream(StarterKitSoundAudience.values()).map(Enum::name).toList()) + ".");

    private final ConfigProperty<Boolean> runCommands = this.addProperty(ConfigTypes.BOOLEAN,
            "StarterKit.RunCommands.Enabled",
            true,
            "If enabled, configured commands are executed when a player receives the Starter Kit.");

    private final ConfigProperty<List<String>> commands = this.addProperty(ConfigTypes.STRING_LIST,
            "StarterKit.RunCommands.Commands",
            List.of(),
            "Commands to execute when a player receives the Starter Kit.",
            "Use '" + su.nightexpress.nightcore.util.Players.PLAYER_COMMAND_PREFIX + "' prefix to run a command as the player.",
            "Commands without that prefix run as the console.",
            "Placeholders from PlaceholderAPI are supported where available.");

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

    private static final ConfigType<List<StarterKitEffect>> STARTER_EFFECTS_TYPE = ConfigType.of(
            StarterKitSettings::readStarterEffects,
            StarterKitSettings::writeStarterEffects);

    private final ConfigProperty<List<StarterKitEffect>> starterEffects = this.addProperty(STARTER_EFFECTS_TYPE,
            "StarterKit.Effects",
            List.of(
                    new StarterKitEffect(org.bukkit.potion.PotionEffectType.SPEED, 30, 2),
                    new StarterKitEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST, 30, 1)
            ),
            "Potion effects to give to players with Starter Kit.",
            "Format: 'TYPE:duration_seconds:amplifier' (e.g. 'SPEED:120:1').",
            "Use duration '-1' for an infinite effect. Legacy 'TYPE: amplifier' map format is also read.");

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

    @NotNull
    private static List<StarterKitEffect> readStarterEffects(@NotNull FileConfig config, @NotNull String path) {
        List<StarterKitEffect> result = new ArrayList<>();

        List<String> list = config.getStringList(path);
        if (!list.isEmpty() || config.contains(path + ".0") || config.get(path) instanceof List<?>) {
            for (String raw : list) {
                if (raw == null || raw.isBlank()) continue;
                StarterKitEffect effect = StarterKitEffect.parse(raw);
                if (effect != null) {
                    result.add(effect);
                }
            }
            if (!result.isEmpty() || config.get(path) instanceof List<?>) {
                return result;
            }
        }

        Set<String> keys = config.getSection(path);
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                int amplifier = config.getInt(path + "." + key, 1);
                StarterKitEffect effect = StarterKitEffect.parse(key + ":-1:" + amplifier);
                if (effect != null) {
                    result.add(effect);
                }
            }
        }

        return result;
    }

    private static void writeStarterEffects(@NotNull FileConfig config, @NotNull String path, @NotNull List<StarterKitEffect> effects) {
        config.set(path, effects.stream().map(StarterKitEffect::serialize).toList());
    }

    public boolean isEnabled() {
        return this.enabled.get();
    }

    public boolean isRequirePermission() {
        return this.requirePermission.get();
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

    public boolean isGiveXP() {
        return this.giveXP.get();
    }

    public int getXPAmount() {
        return this.xpAmount.get();
    }

    public int getXPLevels() {
        return this.xpLevels.get();
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

    @NotNull
    public StarterKitSoundAudience getSoundAudience() {
        return StarterKitSoundAudience.byName(this.soundAudience.get());
    }

    public boolean isRunCommands() {
        return this.runCommands.get();
    }

    @NotNull
    public List<String> getCommands() {
        return this.commands.get();
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

    @NotNull
    public List<StarterKitEffect> getStarterEffects() {
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