package su.nightexpress.sunlight.moduleImpl.sign.config;

import org.bukkit.Sound;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.sunlight.SLPlaceholders.*;

public class SignsLang implements LangContainer {

    public static final TextLocale COMMAND_SIGN_DESC = LangEntry.builder("Command.Sign.Desc")
        .text("Sign the item in your main hand.");

    public static final TextLocale COMMAND_DELETE_DESC = LangEntry.builder("Command.Sign.Delete.Desc")
        .text("Remove your signature from the item in your main hand.");

    public static final TextLocale COMMAND_LOCK_DESC = LangEntry.builder("Command.Sign.Lock.Desc")
        .text("Lock the item in your main hand against further signatures.");

    public static final TextLocale COMMAND_UNLOCK_DESC = LangEntry.builder("Command.Sign.Unlock.Desc")
        .text("Unlock the item in your main hand to allow signatures again.");

    public static final TextLocale COMMAND_INFO_DESC = LangEntry.builder("Command.Sign.Info.Desc")
        .text("Show signatures and the lock state of the item in your main hand.");

    public static final MessageLocale COMMAND_SIGN_DONE = LangEntry.builder("Command.Sign.Done").chatMessage(
        Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
        GRAY.wrap("You have signed " + WHITE.wrap(GENERIC_ITEM) + "."));

    public static final MessageLocale COMMAND_DELETE_DONE = LangEntry.builder("Command.Sign.Delete.Done").chatMessage(
        Sound.BLOCK_FIRE_EXTINGUISH,
        GRAY.wrap("Removed " + GREEN.wrap(GENERIC_AMOUNT) + " signature(s) from " + WHITE.wrap(GENERIC_ITEM) + "."));

    public static final MessageLocale COMMAND_LOCK_DONE = LangEntry.builder("Command.Sign.Lock.Done").chatMessage(
        Sound.BLOCK_CHEST_LOCKED,
        GRAY.wrap("Lock state of " + WHITE.wrap(GENERIC_ITEM) + " is " + YELLOW.wrap(GENERIC_STATE) + "."));

    public static final MessageLocale COMMAND_UNLOCK_DONE = LangEntry.builder("Command.Sign.Unlock.Done").chatMessage(
        Sound.BLOCK_CHEST_OPEN,
        GRAY.wrap("Lock state of " + WHITE.wrap(GENERIC_ITEM) + " is " + YELLOW.wrap(GENERIC_STATE) + "."));

    public static final MessageLocale COMMAND_INFO_HEADER = LangEntry.builder("Command.Sign.Info.Header").chatMessage(
        GRAY.wrap("Signatures on " + WHITE.wrap(GENERIC_ITEM) + ": " + YELLOW.wrap(GENERIC_AMOUNT)));

    public static final MessageLocale COMMAND_INFO_ENTRY = LangEntry.builder("Command.Sign.Info.Entry").chatMessage(
        GRAY.wrap(YELLOW.wrap(GENERIC_SLOT) + ". " + GREEN.wrap(GENERIC_NAME) + GRAY.wrap(" on ")
            + YELLOW.wrap(GENERIC_TIME)));

    public static final MessageLocale COMMAND_INFO_LOCKED = LangEntry.builder("Command.Sign.Info.Locked").chatMessage(
        GRAY.wrap("Lock state: " + YELLOW.wrap(GENERIC_STATE)));

    public static final MessageLocale COMMAND_INFO_EMPTY = LangEntry.builder("Command.Sign.Info.Empty").chatMessage(
        GRAY.wrap(WHITE.wrap(GENERIC_ITEM) + " has no signatures."));

    public static final MessageLocale ERROR_EMPTY_HAND = LangEntry.builder("Command.Sign.Error.EmptyHand").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You must hold an item in your main hand!"));

    public static final MessageLocale ERROR_BLACKLISTED = LangEntry.builder("Command.Sign.Error.Blacklisted").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(WHITE.wrap(GENERIC_ITEM) + " can not be signed."));

    public static final MessageLocale ERROR_MAX_STACK = LangEntry.builder("Command.Sign.Error.MaxStack").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("Items can only be signed in stacks of " + WHITE.wrap(GENERIC_MAX) + "."));

    public static final MessageLocale ERROR_HAS_LORE = LangEntry.builder("Command.Sign.Error.HasLore").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(WHITE.wrap(GENERIC_ITEM) + " already has lore and can not be signed."));

    public static final MessageLocale ERROR_ITEM_LOCKED = LangEntry.builder("Command.Sign.Error.Locked").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(WHITE.wrap(GENERIC_ITEM) + " is locked against signatures."));

    public static final MessageLocale ERROR_ALREADY_SIGNED = LangEntry
            .builder("Command.Sign.Error.AlreadySigned").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You already have a signature on " + WHITE.wrap(GENERIC_ITEM) + "."));

    public static final MessageLocale ERROR_MAX_SIGNATURES = LangEntry
            .builder("Command.Sign.Error.MaxSignatures").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(WHITE.wrap(GENERIC_ITEM) + " reached its limit of " + WHITE.wrap(GENERIC_MAX) + " signature(s)."));

    public static final MessageLocale ERROR_OFFHAND_REQUIRED = LangEntry
            .builder("Command.Sign.Error.OffhandRequired").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You must hold " + YELLOW.wrap(GENERIC_AMOUNT) + "x " + WHITE.wrap(GENERIC_TYPE)
            + GRAY.wrap(" in your offhand!")));

    public static final MessageLocale ERROR_XP_COST = LangEntry.builder("Command.Sign.Error.XpCost").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You need " + YELLOW.wrap(GENERIC_AMOUNT) + " XP level(s) to sign this item."));

    public static final MessageLocale ERROR_NO_SIGNATURES = LangEntry
            .builder("Command.Sign.Error.NoSignatures").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap("You have no signature on " + WHITE.wrap(GENERIC_ITEM) + "."));

    public static final MessageLocale ERROR_NO_SIGNATURES_OTHER = LangEntry
            .builder("Command.Sign.Error.NoSignatures.Other").chatMessage(
        Sound.ENTITY_VILLAGER_NO,
        GRAY.wrap(WHITE.wrap(GENERIC_NAME) + GRAY.wrap(" has no signature on " + WHITE.wrap(GENERIC_ITEM) + ".")));
}
