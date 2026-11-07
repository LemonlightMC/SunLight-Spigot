package su.nightexpress.sunlight.moduleImpl.extras;

import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;

import java.util.List;

import static su.nightexpress.nightcore.util.Placeholders.GENERIC_ENTRY;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_DISPLAY_NAME;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_NAME;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_PREFIX;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_SUFFIX;
import static su.nightexpress.nightcore.util.Placeholders.URL_WIKI_TEXT;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.sunlight.SLPlaceholders.*;

public class ExtrasSettings extends AbstractConfig {

        public final ConfigProperty<List<String>> broadcastFormat = this.addProperty(ConfigTypes.STRING_LIST,
                        "Broadcast.Format",
                        List.of("", YELLOW.and(BOLD).wrap("BROADCAST:"), "", GRAY.wrap(GENERIC_MESSAGE), ""),
                        "Text Formations: " + URL_WIKI_TEXT);

        public final ConfigProperty<String> disposalTitle = this.addProperty(ConfigTypes.STRING, "Disposal.Title", BLACK
                        .wrap("Disposal"));
        public final ConfigProperty<Integer> disposalSize = this.addProperty(ConfigTypes.INT, "Disposal.InventorySize",
                        36);

        public final ConfigProperty<Boolean> signColorsEnabled = this.addProperty(ConfigTypes.BOOLEAN,
                        "Sign_Colors.Enabled",
                        false,
                        "Controls whether the Sign Colors feature is available to use.",
                        "Players with '" + ExtrasPerms.SIGNS_COLOR
                                        + "' permission will be able to use colors on signs.");

        public final ConfigProperty<Boolean> anvilColorsEnabled = this.addProperty(ConfigTypes.BOOLEAN,
                        "Anvil_Colors.Enabled",
                        true,
                        "Controls whether the Anvil Colors feature is available to use.",
                        "Players with '" + ExtrasPerms.ANVILS_COLOR
                                        + "' permission will be able to use colors on anvils.");

        public final ConfigProperty<List<String>> staffRanks = this.addProperty(ConfigTypes.STRING_LIST,
                        "Staff.IncludedRanks",
                        List.of("admin", "moderator", "helper"),
                        "Player ranks considered as staff.");

        public final ConfigProperty<List<String>> staffFormat = this.addProperty(ConfigTypes.STRING_LIST,
                        "Staff.Format.List",
                        List.of(
                                        DARK_GRAY.and(STRIKETHROUGH).wrap("-".repeat(32)),
                                        GRAY.wrap("There are " + SOFT_RED.and(UNDERLINED).wrap(GENERIC_AMOUNT)
                                                        + " staff players online:"),
                                        " ",
                                        GENERIC_ENTRY,
                                        DARK_GRAY.and(STRIKETHROUGH).wrap("-".repeat(32))),
                        "[*] Text Formations Guide: " + URL_WIKI_TEXT,
                        "[*] Placeholders: '%s'".formatted(GENERIC_AMOUNT));

        public final ConfigProperty<String> staffEntryFormat = this.addProperty(ConfigTypes.STRING,
                        "Staff.Format.Entry",
                        HEAD_HAT.apply(PLAYER_NAME) + " "
                                        + GRAY.wrap(PLAYER_PREFIX + SUGGEST_COMMAND.with("/msg " + PLAYER_NAME + " ")
                                                        .wrap(SHOW_TEXT.with(
                                                                        GRAY.wrap("Click to write private message."))
                                                                        .wrap(WHITE.wrap(
                                                                                        PLAYER_DISPLAY_NAME)))
                                                        + PLAYER_SUFFIX),
                        "[*] Text Formations Guide: " + URL_WIKI_TEXT,
                        "[*] Placeholders: '%s', '%s', 'PlaceholderAPI'".formatted(PLAYER_PREFIX, PLAYER_SUFFIX));

        public final ConfigValue<List<String>> SIGN_CONTENT_DEFAULT = ConfigValue.create("Signature.Content.DEFAULT",
                        List.of(
                                        "<white>%text%",
                                        "",
                                        "<gray>Signed by <green><bold>%player%<reset><gray> on <yellow><bold>%date% <yellow><bold>%time%"),
                        "Signature lore lines of the '/sign <text>' command.",
                        "Placeholders: " + "%text%, %player%, %date%, %time%.");

        public final ConfigValue<List<String>> SIGN_CONTENT_SHORT = ConfigValue.create("Signature.Content.SHORT",
                        List.of(
                                        "<gray>Signed by <green><bold>%player%<reset><gray> on <yellow><bold>%date% <yellow><bold>%time%"),
                        "Signature lore lines of the plain '/sign' command.",
                        "Placeholders: " + "%player%, %date%, %time%.");

        public final ConfigValue<String> SIGN_FORMAT_DATE = ConfigValue.create("Signature.Formats.Date",
                        "dd/MM/yyyy",
                        "Date format for the '" + "%date%" + "' placeholder.",
                        "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html");

        public final ConfigValue<String> SIGN_FORMAT_TIME = ConfigValue.create("Signature.Formats.Time",
                        "HH:mm:ss",
                        "Time format for the '" + "%time%" + "' placeholder.",
                        "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html");

        public final ConfigValue<Boolean> SIGN_ALLOW_COLORS = ConfigValue.create("Signature.Allow_Colors",
                        true,
                        "Sets whether players (with '" + ExtrasPerms.SIGN_COMMAND_COLORS + "' permission) are allowed",
                        "to use color tags in the signature text.");

        public final ConfigValue<Integer> SIGN_MAX_SIGNATURES = ConfigValue.create("Signature.Max",
                        0,
                        "Maximum amount of signatures allowed on a single item.",
                        "Set to '0' for unlimited.");

        public final ConfigValue<Boolean> SIGN_ADD_TO_TOP = ConfigValue.create("Signature.Add_To.Top",
                        false,
                        "Sets whether new signature lore is added on 'top' (above the existing",
                        "item lore) instead of below it.");

        public final ConfigValue<Integer> SIGN_MAX_STACK_SIZE = ConfigValue.create("Restrictions.Max_Stack_Size",
                        1,
                        "Maximum amount of items in a stack that can be signed.");

        public final ConfigValue<List<String>> SIGN_ITEM_BLACKLIST = ConfigValue.create("Restrictions.Item_Blacklist",
                        List.of(),
                        "List of materials that can not be signed.",
                        "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");

        public final ConfigValue<Boolean> SIGN_DENY_ITEMS_WITH_LORE = ConfigValue.create(
                        "Restrictions.Deny_Items_With_Lore",
                        false,
                        "Sets whether items with custom lore can not be signed.");

        public final ConfigValue<Boolean> SIGN_AUTO_LOCK = ConfigValue.create("Auto_Lock.Enabled",
                        false,
                        "Sets whether new signatures will automatically lock the item.",
                        "Locked items can not receive more signatures.");

        public final ConfigValue<Boolean> SIGN_AUTO_LOCK_PERMISSION = ConfigValue.create("Auto_Lock.Permission_Based",
                        true,
                        "Sets whether auto-locking requires the '" + ExtrasPerms.SIGN_COMMAND_LOCK_AUTO
                                        + "' permission.");

        public final ConfigValue<Boolean> SIGN_OFFHAND_ENABLED = ConfigValue.create("Requirement.Offhand.Enabled",
                        false,
                        "Sets whether players must hold a certain item in the offhand to sign items.");

        public final ConfigValue<String> SIGN_OFFHAND_TYPE = ConfigValue.create("Requirement.Offhand.Type",
                        "GLOW_INK_SAC",
                        "The offhand item material.");

        public final ConfigValue<Integer> SIGN_OFFHAND_AMOUNT = ConfigValue.create("Requirement.Offhand.Amount",
                        1,
                        "The offhand item amount required.");

        public final ConfigValue<Boolean> SIGN_OFFHAND_CONSUME = ConfigValue.create("Requirement.Offhand.Consume",
                        true,
                        "Sets whether the offhand item is consumed on a successful signature.");

        public final ConfigValue<Integer> SIGN_XP_COST = ConfigValue.create("Cost.XP",
                        0,
                        "Amount of XP **levels** to charge for a signature.",
                        "Set to '0' to disable.");

        public final ConfigValue<Boolean> SIGN_SEPARATOR_ENABLED = ConfigValue.create("Signature.Separator.Enabled",
                        true,
                        "Sets whether a separator line is added between the item lore and signatures.");

        public final ConfigValue<String> SIGN_SEPARATOR_LINE = ConfigValue.create("Signature.Separator.Line",
                        "<dark_gray>────────────────────",
                        "The separator line between the item lore and signatures.");

        public final ConfigValue<Boolean> SIGN_SOUNDS_ENABLED = ConfigValue.create("Signature.Sounds.Enabled",
                        true,
                        "Sets whether sounds are played on a successful signature.");

        public final ConfigValue<Boolean> SIGN_PARTICLES_ENABLED = ConfigValue.create("Signature.Particles.Enabled",
                        true,
                        "Sets whether particles are spawned on a successful signature.");

        public final ConfigValue<String> SIGN_PARTICLE_TYPE = ConfigValue.create("Signature.Particles.Type",
                        "ENCHANT",
                        "The particle spawned on a successful signature.",
                        "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");

        public final ConfigValue<Integer> SIGN_PARTICLE_COUNT = ConfigValue.create("Signature.Particles.Count",
                        15,
                        "The amount of particles spawned on a successful signature.");

        public final ConfigValue<Boolean> SIGN_ANVIL_BLOCK_RENAME = ConfigValue.create("Anvil.Block_Rename",
                        false,
                        "Sets whether signed items can not be renamed in an anvil.");

        public final ConfigValue<Boolean> SIGN_ANVIL_STRIP_ON_RENAME = ConfigValue.create("Anvil.Strip_On_Rename",
                        false,
                        "Sets whether renaming a signed item in an anvil strips its signatures.");

        public final ConfigValue<Boolean> SIGN_CRAFTING_KEEP_SIGNATURES = ConfigValue.create("Crafting.Keep_Signatures",
                        true,
                        "Sets whether signatures are kept when a signed item is used in a recipe.");

        public final ConfigValue<Boolean> SIGN_CRAFTING_ALLOW_SIGNED = ConfigValue.create("Crafting.Allow_Signed_Items",
                        true,
                        "Sets whether signed items may be used as ingredients in crafting.");

        public final ConfigValue<Boolean> SIGN_BOOKS_AUTO_SIGN = ConfigValue.create("Written_Books.Auto_Sign",
                        false,
                        "Sets whether signing a written book also sets its author.");

        public boolean isSignColorsEnabled() {
                return this.signColorsEnabled.get();
        }

        public boolean isAnvilColorsEnabled() {
                return this.anvilColorsEnabled.get();
        }

}
