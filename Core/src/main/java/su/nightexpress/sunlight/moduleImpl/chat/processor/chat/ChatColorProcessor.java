package su.nightexpress.sunlight.moduleImpl.chat.processor.chat;

import java.util.List;

import org.bukkit.entity.Player;

import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;
import su.nightexpress.sunlight.moduleImpl.chat.ChatModule;
import su.nightexpress.sunlight.moduleImpl.chat.context.ChatContext;
import su.nightexpress.sunlight.moduleImpl.chat.processor.ChatProcessor;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsAccess;
import su.nightexpress.sunlight.moduleImpl.settings.SettingsProperties;
import su.nightexpress.sunlight.moduleImpl.settings.config.SettingsPerms;
import su.nightexpress.sunlight.user.SunUser;

/**
 * Applies the player's chosen chat colour, gradient and formatting to the
 * message body. The filters and format placeholders still run afterwards, so a
 * player cannot use a colour tag to inject anything into the chat format.
 */
public class ChatColorProcessor implements ChatProcessor<ChatContext> {

    @Override
    public void preProcess(ChatModule module, ChatContext context) {
        if (!SettingsAccess.isEnabled(module.plugin())) {
            return;
        }

        Player player = context.getPlayer();
        if (!player.hasPermission(SettingsPerms.SETTING_CHAT_COLOR)) {
            return;
        }

        SunUser user = module.userManager().getOrFetch(player);
        String message = context.getMessage();
        if (message == null || message.isBlank()) {
            return;
        }

        String gradient = this.allowedValue(player, user.getPropertyOrDefault(SettingsProperties.CHAT_COLOR_GRADIENT),
                SettingsProperties.CHAT_GRADIENTS);
        String color = this.allowedValue(player, user.getPropertyOrDefault(SettingsProperties.CHAT_COLOR),
                SettingsProperties.CHAT_COLORS);
        String formatting = this.allowedValue(player, user.getPropertyOrDefault(SettingsProperties.CHAT_FORMATTING),
                SettingsProperties.CHAT_FORMATTING_VALUES);

        if (gradient != null) {
            message = this.applyGradient(gradient, message);
        } else if (color != null) {
            message = TagWrappers.COLOR.with(color).wrap(message);
        }

        message = this.applyFormatting(formatting, message);
        context.setMessage(message);
    }

    @Override
    public void postProcess(ChatModule module, ChatContext context) {
    }

    private String allowedValue(Player player, String value, List<String> knownValues) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("none")) {
            return null;
        }
        if (!knownValues.contains(value)) {
            return null;
        }
        return player.hasPermission(SettingsPerms.SETTING_CHAT_COLOR + "." + value) ? value : null;
    }

    private String applyGradient(String gradient, String message) {
        return switch (gradient.toLowerCase()) {
            case "rainbow" -> TagWrappers.GRADIENT.with("#ff5555", "#5555ff").wrap(message);
            case "sunset" -> TagWrappers.GRADIENT.with("#ff7e5f", "#feb47b").wrap(message);
            case "ocean" -> TagWrappers.GRADIENT.with("#2e3192", "#1bffff").wrap(message);
            default -> TagWrappers.COLOR.with(gradient).wrap(message);
        };
    }

    private String applyFormatting(String formatting, String message) {
        if (formatting == null) {
            return message;
        }
        return switch (formatting.toLowerCase()) {
            case "bold" -> TagWrappers.BOLD.wrap(message);
            case "italic" -> TagWrappers.ITALIC.wrap(message);
            case "underline" -> TagWrappers.UNDERLINED.wrap(message);
            case "bold_italic" -> TagWrappers.BOLD.wrap(TagWrappers.ITALIC.wrap(message));
            default -> message;
        };
    }
}
