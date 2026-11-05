package su.nightexpress.sunlight.moduleImpl.chat.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.executors.CommandArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.api.provider.mode.ToggleMode;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.chat.ChatModule;
import su.nightexpress.sunlight.moduleImpl.chat.ChatProperties;
import su.nightexpress.sunlight.moduleImpl.chat.core.ChatLang;
import su.nightexpress.sunlight.moduleImpl.chat.core.ChatPerms;
import su.nightexpress.sunlight.moduleImpl.chat.spy.SpyType;
import su.nightexpress.sunlight.user.property.UserProperty;
import su.nightexpress.sunlight.utils.Utils;

public class SpyCommandProvider extends CommandProvider<ChatModule> {

    private static final String COMMAND_ROOT = "spy";

    private static final List<String> TYPE_SUGGESTIONS = Arrays.asList("chat", "command", "social", "all");
    private static final List<String> STATE_SUGGESTIONS = Arrays.asList("on", "off", "toggle");

    public SpyCommandProvider(ChatModule module) {
        super(module, "chat-spy");
    }

    @Override
    public void setup() {
        // /spy logger <type> <target> [on/off]
        this.register("logger", command -> command
                .withFullDescription(ChatLang.COMMAND_SPY_LOGGER_DESC.text())
                .withPermission(ChatPerms.COMMAND_SPY_LOGGER)
                .withArguments(
                        CommandArgumentConstants.string(CommandArgumentConstants.TYPE,
                                info -> TYPE_SUGGESTIONS),
                        CommandArgumentConstants.string(CommandArgumentConstants.PLAYER,
                                info -> CommandArgumentConstants.onlinePlayerNames()))
                .withOptionalArguments(
                        CommandArgumentConstants.string(CommandArgumentConstants.STATE,
                                info -> STATE_SUGGESTIONS))
                .executes((sender, arguments) -> {
                    return this.toggleLogger(sender, arguments);
                }))
                .aliases("logger")
                .under(COMMAND_ROOT);

        // /spy chat|command|social|all <target> [on/off]
        this.registerTypeSubcommand("chat", SpyType.CHAT, ChatPerms.COMMAND_SPY_CHAT);
        this.registerTypeSubcommand("command", SpyType.COMMAND, ChatPerms.COMMAND_SPY_COMMAND);
        this.registerTypeSubcommand("social", SpyType.SOCIAL, ChatPerms.COMMAND_SPY_SOCIAL);
        this.registerAllSubcommand();

        this.registerRoot(COMMAND_ROOT, command -> command
                .withFullDescription(ChatLang.COMMAND_SPY_ROOT_DESC.text())
                .withPermission(ChatPerms.COMMAND_SPY_ROOT)
                .executes((sender, arguments) -> {
                    if (sender instanceof org.bukkit.entity.Player player) {
                        this.module.openSpyDialog(player);
                        return 1;
                    }
                    return 0;
                }));
    }

    private void registerTypeSubcommand(String id, SpyType spyType, String permission) {
        this.register(id, command -> command
                .withFullDescription(ChatLang.COMMAND_SPY_MODE_TOGGLE_DESC.text().replace(
                        SLPlaceholders.GENERIC_TYPE, ChatLang.SPY_TYPE.getLocalized(spyType)))
                .withPermission(permission)
                .withArguments(
                        CommandArgumentConstants.string(CommandArgumentConstants.PLAYER,
                                info -> CommandArgumentConstants.onlinePlayerNames()))
                .withOptionalArguments(
                        CommandArgumentConstants.string(CommandArgumentConstants.STATE,
                                info -> STATE_SUGGESTIONS))
                .executes((sender, arguments) -> {
                    ToggleMode mode = this.parseMode((String) arguments.get(CommandArgumentConstants.STATE));
                    return this.toggleTypes(sender, arguments, List.of(spyType), mode);
                }))
                .under(COMMAND_ROOT);
    }

    private void registerAllSubcommand() {
        this.register("all", command -> command
                .withFullDescription(ChatLang.COMMAND_SPY_MODE_TOGGLE_DESC.text().replace(
                        SLPlaceholders.GENERIC_TYPE, "all"))
                .withPermission(ChatPerms.COMMAND_SPY_ROOT)
                .withArguments(
                        CommandArgumentConstants.string(CommandArgumentConstants.PLAYER,
                                info -> CommandArgumentConstants.onlinePlayerNames()))
                .withOptionalArguments(
                        CommandArgumentConstants.string(CommandArgumentConstants.STATE,
                                info -> STATE_SUGGESTIONS))
                .executes((sender, arguments) -> {
                    ToggleMode mode = this.parseMode((String) arguments.get(CommandArgumentConstants.STATE));
                    return this.toggleTypes(sender, arguments,
                            Arrays.asList(SpyType.CHAT, SpyType.COMMAND, SpyType.SOCIAL), mode);
                }))
                .under(COMMAND_ROOT);
    }

    private ToggleMode parseMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return ToggleMode.TOGGLE;
        }
        return switch (raw.toLowerCase()) {
            case "on", "enable", "true", "1" -> ToggleMode.ON;
            case "off", "disable", "false", "0" -> ToggleMode.OFF;
            default -> ToggleMode.TOGGLE;
        };
    }

    private List<SpyType> typesFromRaw(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("all")) {
            return Arrays.asList(SpyType.CHAT, SpyType.COMMAND, SpyType.SOCIAL);
        }
        SpyType type = Utils.enumValueOf(raw.toUpperCase(), SpyType.class);
        return type == null ? List.of() : List.of(type);
    }

    private int toggleTypes(CommandSender sender, CommandArguments arguments, List<SpyType> types,
            ToggleMode mode) {
        if (types.isEmpty()) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }
        int result = 0;
        for (SpyType spyType : types) {
            final String othersPermission = switch (spyType) {
                case CHAT -> ChatPerms.COMMAND_SPY_CHAT_OTHERS;
                case COMMAND -> ChatPerms.COMMAND_SPY_COMMAND_OTHERS;
                case SOCIAL -> ChatPerms.COMMAND_SPY_SOCIAL_OTHERS;
            };
            final SpyType captured = spyType;
            String playerName = (String) arguments.get(CommandArgumentConstants.PLAYER);
            CommandArgumentConstants.Target target = new CommandArgumentConstants.Target(playerName, false);
            int r = target.runAs(this.module, sender, othersPermission, (user, targetPlayer) -> {
                UserProperty<Boolean> property = ChatProperties.getSpyInfoProperty(captured);
                boolean state = mode.apply(user.getPropertyOrDefault(property));

                user.setProperty(property, state);
                user.markDirty();

                if (sender != targetPlayer) {
                    this.module.sendPrefixed(ChatLang.SPY_MODE_TOGGLE_FEEDBACK, sender,
                            replacer -> replacer
                                    .with(CommonPlaceholders.PLAYER.resolver(targetPlayer))
                                    .with(SLPlaceholders.GENERIC_TYPE,
                                            () -> ChatLang.SPY_TYPE.getLocalized(captured))
                                    .with(SLPlaceholders.GENERIC_STATE,
                                            () -> CoreLang.STATE_ENABLED_DISALBED.get(state)));
                }

                if (!target.silent()) {
                    this.module.sendPrefixed(ChatLang.SPY_MODE_TOGGLE_NOTIFY, targetPlayer,
                            replacer -> replacer
                                    .with(SLPlaceholders.GENERIC_TYPE,
                                            () -> ChatLang.SPY_TYPE.getLocalized(captured))
                                    .with(SLPlaceholders.GENERIC_STATE,
                                            () -> CoreLang.STATE_ENABLED_DISALBED.get(state)));
                }
            });
            result += r;
        }
        return result > 0 ? 1 : 0;
    }

    private int toggleLogger(CommandSender sender, CommandArguments arguments) {
        String typeRaw = (String) arguments.get(CommandArgumentConstants.TYPE);
        ToggleMode mode = this.parseMode((String) arguments.get(CommandArgumentConstants.STATE));
        List<SpyType> types = this.typesFromRaw(typeRaw);
        if (types.isEmpty()) {
            this.module.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, sender);
            return 0;
        }

        boolean dispatched = this.loadPlayerWithDataAndRunInMainThread(sender, arguments, (user, target) -> {
            for (SpyType spyType : types) {
                UserProperty<Boolean> property = ChatProperties.getSpyLogProperty(spyType);
                boolean state = mode.apply(user.getPropertyOrDefault(property));

                user.setProperty(property, state);
                user.markDirty();

                this.module.sendPrefixed(ChatLang.SPY_LOGGER_TOGGLE_FEEDBACK, sender,
                        replacer -> replacer
                                .with(CommonPlaceholders.PLAYER.resolver(target))
                                .with(SLPlaceholders.GENERIC_TYPE,
                                        () -> ChatLang.SPY_TYPE.getLocalized(spyType))
                                .with(SLPlaceholders.GENERIC_STATE,
                                        () -> CoreLang.STATE_ENABLED_DISALBED.get(state)));
            }
        });
        return dispatched ? 1 : 0;
    }
}
