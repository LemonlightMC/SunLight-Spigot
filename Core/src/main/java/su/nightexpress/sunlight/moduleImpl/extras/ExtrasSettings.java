package su.nightexpress.sunlight.moduleImpl.extras;

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

        public boolean isSignColorsEnabled() {
                return this.signColorsEnabled.get();
        }

        public boolean isAnvilColorsEnabled() {
                return this.anvilColorsEnabled.get();
        }

}
