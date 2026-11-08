package su.nightexpress.sunlight.moduleImpl.timer.dialog.impl;

import org.bukkit.entity.Player;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerVisibility;
import su.nightexpress.sunlight.moduleImpl.timer.model.VisibilityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.GRAY;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.SOFT_YELLOW;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.WHITE;

public class TimerVisibilityDialog extends Dialog<TimerInstance> {
    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Visibility.Title").text("Timer Visibility");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Visibility.Body").dialogElement(430,
            GRAY.wrap("Format: ") + SOFT_YELLOW.wrap("<mode> [permission] [world1,world2|*] [bypass:true|false]")
                    + GRAY.wrap("\nModes: ALL, PERMISSION, STAFF, SELF. Worlds '*' means all worlds.")
                    + GRAY.wrap("\nCurrent: "));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Visibility.Input").text("mode [permission] [worlds] [bypass]");
    private final TimerModule module;
    public TimerVisibilityDialog(TimerModule module) { this.module = module; }

    @Override
    public WrappedDialog create(Player player, TimerInstance timer) {
        TimerVisibility v = timer.getDefinition().getVisibility();
        String worlds = v.getWorlds().isEmpty() ? "*" : String.join(",", v.getWorlds());
        String initial = v.getMode().name() + " " + (v.getPermission().isBlank() ? "-" : v.getPermission()) + " " + worlds + " " + v.isBypassPermissionApplies();
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(220).width(420).initial(initial).build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, id, nbt) -> {
                    String[] parts = nbt.getText("value").orElse("").trim().split("\\s+");
                    if (parts.length == 0 || parts[0].isBlank()) { viewer.callback(); return; }
                    TimerVisibility vis = timer.getDefinition().getVisibility();
                    try { vis.setMode(VisibilityMode.valueOf(parts[0].toUpperCase(Locale.ROOT))); } catch (Exception ignored) {}
                    if (parts.length > 1 && !"-".equals(parts[1])) vis.setPermission(parts[1].trim()); else if (parts.length > 1) vis.setPermission("");
                    if (parts.length > 2) {
                        if ("*".equals(parts[2])) vis.setWorlds(new ArrayList<>());
                        else vis.setWorlds(new ArrayList<>(Arrays.stream(parts[2].split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()));
                    }
                    if (parts.length > 3) vis.setBypassPermissionApplies(Boolean.parseBoolean(parts[3]));
                    this.module.engine().persist(timer, timer.isGlobal());
                    viewer.callback();
                }).build();
    }
}
