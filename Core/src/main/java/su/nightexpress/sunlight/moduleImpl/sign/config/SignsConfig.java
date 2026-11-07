package su.nightexpress.sunlight.moduleImpl.sign.config;

import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.Lists;

import java.util.List;

/*
 * Placeholders usable in the signature templates:
 *     %text%    -   the text input (only in 'DEFAULT' templates)
 *     %player%  -   the signer's name
 *     %date%    -   the date of the signature
 *     %time%    -   the time of the signature
 *
 * PlaceholderAPI placeholders (installed) are supported as well.
 */
public class SignsConfig {

    public static final ConfigValue<List<String>> CONTENT_DEFAULT = ConfigValue.create("Signature.Content.DEFAULT",
        List.of(
            "<white>%text%",
            "",
            "<gray>Signed by <green><bold>%player%<reset><gray> on <yellow><bold>%date% <yellow><bold>%time%"
        ),
        "Signature lore lines of the '/sign <text>' command.",
        "Placeholders: " + "%text%, %player%, %date%, %time%."
    );

    public static final ConfigValue<List<String>> CONTENT_SHORT = ConfigValue.create("Signature.Content.SHORT",
        List.of(
            "<gray>Signed by <green><bold>%player%<reset><gray> on <yellow><bold>%date% <yellow><bold>%time%"
        ),
        "Signature lore lines of the plain '/sign' command.",
        "Placeholders: " + "%player%, %date%, %time%."
    );

    public static final ConfigValue<String> FORMAT_DATE = ConfigValue.create("Signature.Formats.Date",
        "dd/MM/yyyy",
        "Date format for the '" + "%date%" + "' placeholder.",
        "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html"
    );

    public static final ConfigValue<String> FORMAT_TIME = ConfigValue.create("Signature.Formats.Time",
        "HH:mm:ss",
        "Time format for the '" + "%time%" + "' placeholder.",
        "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html"
    );

    public static final ConfigValue<Boolean> ALLOW_COLORS = ConfigValue.create("Signature.Allow_Colors",
        true,
        "Sets whether players (with '" + SignsPerms.COMMAND_COLORS + "' permission) are allowed",
        "to use color tags in the signature text."
    );

    public static final ConfigValue<Integer> MAX_SIGNATURES = ConfigValue.create("Signature.Max",
        0,
        "Maximum amount of signatures allowed on a single item.",
        "Set to '0' for unlimited."
    );

    public static final ConfigValue<Boolean> ADD_TO_TOP = ConfigValue.create("Signature.Add_To.Top",
        false,
        "Sets whether new signature lore is added on 'top' (above the existing",
        "item lore) instead of below it."
    );

    public static final ConfigValue<Integer> MAX_STACK_SIZE = ConfigValue.create("Restrictions.Max_Stack_Size",
        1,
        "Maximum amount of items in a stack that can be signed."
    );

    public static final ConfigValue<List<String>> ITEM_BLACKLIST = ConfigValue.create("Restrictions.Item_Blacklist",
        Lists.newList(),
        "List of materials that can not be signed.",
        "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html"
    );

    public static final ConfigValue<Boolean> DENY_ITEMS_WITH_LORE = ConfigValue.create("Restrictions.Deny_Items_With_Lore",
        false,
        "Sets whether items with custom lore can not be signed."
    );

    public static final ConfigValue<Boolean> AUTO_LOCK = ConfigValue.create("Auto_Lock.Enabled",
        false,
        "Sets whether new signatures will automatically lock the item.",
        "Locked items can not receive more signatures."
    );

    public static final ConfigValue<Boolean> AUTO_LOCK_PERMISSION = ConfigValue.create("Auto_Lock.Permission_Based",
        true,
        "Sets whether auto-locking requires the '" + SignsPerms.COMMAND_LOCK_AUTO + "' permission."
    );

    public static final ConfigValue<Boolean> OFFHAND_ENABLED = ConfigValue.create("Requirement.Offhand.Enabled",
        false,
        "Sets whether players must hold a certain item in the offhand to sign items."
    );

    public static final ConfigValue<String> OFFHAND_TYPE = ConfigValue.create("Requirement.Offhand.Type",
        "GLOW_INK_SAC",
        "The offhand item material."
    );

    public static final ConfigValue<Integer> OFFHAND_AMOUNT = ConfigValue.create("Requirement.Offhand.Amount",
        1,
        "The offhand item amount required."
    );

    public static final ConfigValue<Boolean> OFFHAND_CONSUME = ConfigValue.create("Requirement.Offhand.Consume",
        true,
        "Sets whether the offhand item is consumed on a successful signature."
    );

    public static final ConfigValue<Integer> XP_COST = ConfigValue.create("Cost.XP",
        0,
        "Amount of XP **levels** to charge for a signature.",
        "Set to '0' to disable."
    );
}
