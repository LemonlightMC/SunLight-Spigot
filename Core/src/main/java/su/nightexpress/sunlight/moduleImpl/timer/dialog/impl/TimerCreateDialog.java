package su.nightexpress.sunlight.moduleImpl.timer.dialog.impl;

import org.bukkit.entity.Player;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.sunlight.moduleImpl.timer.TimerDefaults;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.model.*;
import su.nightexpress.sunlight.moduleImpl.timer.service.GlobalTimerRepository;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerInput;

import java.util.OptionalLong;

import java.util.Locale;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class TimerCreateDialog extends Dialog<TimerScope> {

    private static final TextLocale TITLE = LangEntry.builder("Timer.Dialog.Create.Title").text("Create Timer");
    private static final DialogElementLocale BODY = LangEntry.builder("Timer.Dialog.Create.Body").dialogElement(400,
            GRAY.wrap("Enter timer id, kind (countdown/stopwatch), and duration. Examples: " + SOFT_YELLOW.wrap("event_start countdown 1h30m") + GRAY.wrap(", ") + SOFT_YELLOW.wrap("dragon stopwatch 10m") + GRAY.wrap(", ") + SOFT_YELLOW.wrap("sale countdown 90s")));
    private static final TextLocale INPUT = LangEntry.builder("Timer.Dialog.Create.Input").text("id kind duration");

    private final TimerModule module;
    private final TimerScope scope;

    public TimerCreateDialog(TimerModule module, TimerScope scope) {
        this.module = module;
        this.scope = scope;
    }

    @Override
    public WrappedDialog create(Player player, TimerScope scope) {
        return Dialogs.builder()
                .base(DialogBases.builder(TITLE).body(DialogBodies.plainMessage(BODY)).inputs(DialogInputs.text("value", INPUT).maxLength(128).width(300).initial("").build()).build())
                .type(DialogTypes.multiAction(DialogButtons.ok()).exitAction(DialogButtons.back()).build())
                .handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                    String raw = nbtHolder.getText("value").orElse("").trim();
                    String[] parts = raw.split("\\s+");
                    if (parts.length < 3) return;
                    String id = GlobalTimerRepository.normalizeId(parts[0]);
                    TimerKind kind = "stopwatch".equalsIgnoreCase(parts[1]) ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN;
                    OptionalLong parsed = TimerInput.parsePositiveSeconds(parts[2]);
                    if (parsed.isEmpty()) return;
                    long seconds = parsed.getAsLong();

                    if (this.scope == TimerScope.GLOBAL) {
                        if (this.module.engine().globals().contains(id)) return;
                        TimerDefinition def = TimerDefaults.exampleCountdown();
                        def.setId(id);
                        def.setName(id);
                        def.setKind(kind);
                        def.setDurationSeconds(seconds);
                        def.setScope(TimerScope.GLOBAL);
                        TimerInstance instance = new TimerInstance(def, null);
                        this.module.engine().globals().put(instance);
                        this.module.engine().globals().save(instance);
                    }
                    else {
                        this.module.engine().personals().create(player, kind, id, seconds);
                    }
                    viewer.callback();
                })
                .build();
    }
}
