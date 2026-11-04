package su.nightexpress.sunlight.moduleImpl.chat.dialog;

import org.bukkit.entity.Player;

import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.DialogActions;
import su.nightexpress.nightcore.ui.dialog.build.DialogBases;
import su.nightexpress.nightcore.ui.dialog.build.DialogBodies;
import su.nightexpress.nightcore.ui.dialog.build.DialogButtons;
import su.nightexpress.nightcore.ui.dialog.build.DialogInputs;
import su.nightexpress.nightcore.ui.dialog.build.DialogTypes;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogKey;
import su.nightexpress.sunlight.moduleImpl.chat.ChatModule;

import su.nightexpress.sunlight.moduleImpl.chat.ChatProperties;
import su.nightexpress.sunlight.user.SunUser;

import java.util.List;

public class SpyDialog extends Dialog<String> {
    public static final DialogKey<String> SPY = new DialogKey<>("chat_spy");

    private static final String JSON_SCOPE = "scope";
    private static final String JSON_CHAT = "chat";
    private static final String JSON_COMMAND = "command";
    private static final String JSON_SOCIAL = "social";

    private static final TextLocale TITLE = LangEntry.builder("Chat.Dialog.Spy.Title").text(title("Chat", "Spy"));

    private static final DialogElementLocale BODY = LangEntry.builder("Chat.Dialog.Spy.Body").dialogElement(400,
            "Choose who you want to spy on and which spy modes to enable.");

    private static final TextLocale INPUT_SCOPE = LangEntry.builder("Chat.Dialog.Spy.Input.Scope").text("Scope");
    private static final TextLocale INPUT_CHAT = LangEntry.builder("Chat.Dialog.Spy.Input.Chat").text("Chat Spy");
    private static final TextLocale INPUT_COMMAND = LangEntry.builder("Chat.Dialog.Spy.Input.Command")
            .text("Command Spy");
    private static final TextLocale INPUT_SOCIAL = LangEntry.builder("Chat.Dialog.Spy.Input.Social").text("Social Spy");

    private final ChatModule module;

    public SpyDialog(ChatModule module) {
        this.module = module;
    }

    @Override
    public WrappedDialog create(Player player, String unused) {
        List<WrappedSingleOptionEntry> scopes = List.of(
                new WrappedSingleOptionEntry("player", "Player", true),
                new WrappedSingleOptionEntry("team", "Team", false),
                new WrappedSingleOptionEntry("everyone", "Everyone", false));

        SunUser user = this.module.userManager().getOrFetch(player);
        boolean chat = user != null ? user.getPropertyOrDefault(ChatProperties.CHAT_SPY_INFO) : false;
        boolean command = user != null ? user.getPropertyOrDefault(ChatProperties.COMMAND_SPY_INFO) : false;
        boolean social = user != null ? user.getPropertyOrDefault(ChatProperties.SOCIAL_SPY_INFO) : false;

        return Dialogs.builder()
                .base(DialogBases.builder(TITLE)
                        .body(DialogBodies.plainMessage(BODY))
                        .inputs(
                                DialogInputs.singleOption(JSON_SCOPE, INPUT_SCOPE, scopes).build(),
                                DialogInputs.bool(JSON_CHAT, INPUT_CHAT).initial(chat).build(),
                                DialogInputs.bool(JSON_COMMAND, INPUT_COMMAND).initial(command).build(),
                                DialogInputs.bool(JSON_SOCIAL, INPUT_SOCIAL).initial(social).build())
                        .build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                    if (nbtHolder == null)
                        return;

                    boolean newChat = nbtHolder.getBoolean(JSON_CHAT, chat);
                    boolean newCommand = nbtHolder.getBoolean(JSON_COMMAND, command);
                    boolean newSocial = nbtHolder.getBoolean(JSON_SOCIAL, social);

                    SunUser targetUser = this.module.userManager().getOrFetch(player);
                    if (user != null) {
                        targetUser.setProperty(ChatProperties.CHAT_SPY_INFO, newChat);
                        targetUser.setProperty(ChatProperties.COMMAND_SPY_INFO, newCommand);
                        targetUser.setProperty(ChatProperties.SOCIAL_SPY_INFO, newSocial);
                        targetUser.markDirty();
                    }

                    viewer.callback();
                })
                .build();
    }
}
