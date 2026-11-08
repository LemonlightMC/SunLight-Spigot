package su.nightexpress.sunlight.command.dialog;

import org.bukkit.entity.Player;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.DialogActions;
import su.nightexpress.nightcore.ui.dialog.build.DialogBases;
import su.nightexpress.nightcore.ui.dialog.build.DialogBodies;
import su.nightexpress.nightcore.ui.dialog.build.DialogButtons;
import su.nightexpress.nightcore.ui.dialog.build.DialogTypes;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogKey;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class SunlightCleanupDialog extends Dialog<SunlightCleanupDialog.Request> {

    public static final DialogKey<Request> KEY = new DialogKey<>("sunlight_cleanup");

    public record Request(boolean dryRun, boolean includeSafe) {
    }

    private static final TextLocale TITLE = LangEntry.builder("Sunlight.Dialog.Cleanup.Title")
            .text(TagWrappers.RED.and(TagWrappers.BOLD).wrap("Cleanup User Data"));

    @Override
    public WrappedDialog create(Player viewer, Request request) {
        String dryRun = request.dryRun() ? TagWrappers.GREEN.wrap("yes") : TagWrappers.GRAY.wrap("no");
        String includeSafe = request.includeSafe() ? TagWrappers.GREEN.wrap("yes") : TagWrappers.GRAY.wrap("no");

        DialogElementLocale body = LangEntry.builder("Sunlight.Dialog.Cleanup.Body").dialogElement(420,
                "This will clean user properties in " + TagWrappers.WHITE.wrap("all stored user data") + ".",
                "",
                TagWrappers.GRAY.wrap("• Removes properties not registered in the current server state."),
                TagWrappers.GRAY.wrap("• With --include-safe: also removes statically marked safe-to-wipe properties."),
                "",
                TagWrappers.YELLOW.wrap("Dry run: " + dryRun),
                TagWrappers.YELLOW.wrap("Include safe: " + includeSafe),
                "",
                TagWrappers.SOFT_RED.wrap("Confirm to continue. Non-dry-run changes cannot be undone."));

        return Dialogs.builder()
                .base(DialogBases.builder(TITLE)
                        .body(DialogBodies.plainMessage(body))
                        .build())
                .type(DialogTypes.multiAction(DialogButtons.confirm())
                        .exitAction(DialogButtons.cancel())
                        .build())
                .handleResponse(DialogActions.CONFIRM, (v, identifier, nbtHolder) -> v.callback())
                .build();
    }
}
