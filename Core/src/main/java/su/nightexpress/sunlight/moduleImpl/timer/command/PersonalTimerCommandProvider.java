package su.nightexpress.sunlight.moduleImpl.timer.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerLang;
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerPerms;
import su.nightexpress.sunlight.moduleImpl.timer.dialog.TimerDialogKeys;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerKind;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerScope;
import su.nightexpress.sunlight.moduleImpl.timer.service.GlobalTimerRepository;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerFormat;

import java.util.Locale;

import static su.nightexpress.sunlight.SLPlaceholders.*;

public class PersonalTimerCommandProvider extends CommandProvider<TimerModule> {

    public PersonalTimerCommandProvider(TimerModule module) {
        super(module, "ptimer");
    }

    @Override
    public void setup() {
        this.register("list", c -> c.withPermission(TimerPerms.PTIMER_LIST).executes((ResultingCommandExecutor)(s,a)->this.list(s))).under("ptimer");
        this.register("info", c -> c.withPermission(TimerPerms.PTIMER_INFO).withArguments(new StringArgument("id")).executes(this::info)).under("ptimer");
        this.register("create", c -> c.withPermission(TimerPerms.PTIMER_CREATE).withArguments(new StringArgument("kind"), new IntegerArgument("seconds", 1), new StringArgument("id")).executes(this::create)).under("ptimer");
        for (String action : new String[]{"start","pause","resume","reset","stop"}) {
            String perm = switch (action) { case "start"->TimerPerms.PTIMER_START; case "pause"->TimerPerms.PTIMER_PAUSE; case "resume"->TimerPerms.PTIMER_RESUME; case "reset"->TimerPerms.PTIMER_RESET; default->TimerPerms.PTIMER_STOP; };
            this.register(action, c -> c.withPermission(perm).withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.control(s,a,action))).under("ptimer");
        }
        this.register("delete", c -> c.withPermission(TimerPerms.PTIMER_DELETE).withArguments(new StringArgument("id")).executes(this::delete)).under("ptimer");
        this.register("editor", c -> c.withPermission(TimerPerms.PTIMER_EDITOR).withOptionalArguments(new StringArgument("id")).executes(this::editor)).under("ptimer");

        this.registerRoot("ptimer", c -> c.withFullDescription(TimerLang.COMMAND_PTIMER_DESC.text()).withPermission(TimerPerms.COMMAND_PTIMER)
                .executes((sender, args) -> { if (sender instanceof Player player) this.module.openPersonalList(player); else this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 1; }));
    }

    private Player player(CommandSender sender) {
        if (sender instanceof Player player) return player;
        this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender);
        return null;
    }

    private int list(CommandSender sender) {
        Player player = this.player(sender); if (player == null) return 0;
        var all = this.module.engine().personals().all(player);
        if (all.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_LIST_EMPTY, sender); return 1; }
        this.module.sendPrefixed(TimerLang.COMMAND_LIST_TITLE, sender, b -> b.with(GENERIC_AMOUNT, () -> String.valueOf(all.size())));
        for (TimerInstance t : all) this.module.sendPrefixed(TimerLang.COMMAND_LIST_ENTRY, sender, b -> b.with(GENERIC_NAME, ()->t.getDefinition().getName()).with(GENERIC_TYPE,()->t.getDefinition().getKind().name()).with(GENERIC_STATE,()->t.getState().name()).with(GENERIC_TIME,()-> TimerFormat.format(t)));
        return 1;
    }

    private int info(CommandSender sender, CommandArguments args) {
        Player player = this.player(sender); if (player == null) return 0;
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().personals().get(player, id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        this.module.sendPrefixed(TimerLang.COMMAND_INFO, sender, b -> b.with(GENERIC_VALUE,t::getId).with(GENERIC_NAME,()->t.getDefinition().getName()).with(GENERIC_MESSAGE,()->t.getDefinition().getScope().name()).with(GENERIC_TYPE,()->t.getDefinition().getKind().name()).with(GENERIC_STATE,()->t.getState().name()).with(GENERIC_TIME,()->TimerFormat.format(t)).with(GENERIC_TARGET,()->t.getDefinition().getDurationSeconds()+"s").with(GENERIC_AMOUNT,()->(t.getElapsedMillis()/1000L)+"s"));
        return 1;
    }

    private int create(CommandSender sender, CommandArguments args) {
        Player player = this.player(sender); if (player == null) return 0;
        TimerKind kind = ((String) args.get("kind")).toLowerCase(Locale.ROOT).startsWith("stop") ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN;
        int seconds = (int) args.get("seconds");
        String id = GlobalTimerRepository.normalizeId((String) args.get("id"));
        if (this.module.engine().personals().get(player, id).isPresent()) { this.module.sendPrefixed(TimerLang.COMMAND_ALREADY_EXISTS, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerInstance instance = this.module.engine().personals().create(player, kind, id, seconds);
        this.module.sendPrefixed(TimerLang.COMMAND_CREATED, sender, b -> b.with(GENERIC_VALUE, instance::getId).with(GENERIC_NAME,()->instance.getDefinition().getName()).with(GENERIC_TYPE,()->kind.name()));
        return 1;
    }

    private int control(CommandSender sender, CommandArguments args, String action) {
        Player player = this.player(sender); if (player == null) return 0;
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().personals().get(player, id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        boolean ok = switch (action) { case "start" -> this.module.engine().start(t,false,true); case "pause" -> this.module.engine().pause(t,false); case "resume" -> this.module.engine().resume(t,false); case "reset" -> this.module.engine().reset(t,false); case "stop" -> this.module.engine().stop(t,false); default -> false; };
        this.module.sendPrefixed(switch (action) { case "start" -> TimerLang.COMMAND_STARTED; case "pause" -> TimerLang.COMMAND_PAUSED; case "resume" -> TimerLang.COMMAND_RESUMED; case "reset" -> TimerLang.COMMAND_RESET; default -> TimerLang.COMMAND_STOPPED; }, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return ok ? 1 : 0;
    }

    private int delete(CommandSender sender, CommandArguments args) {
        Player player = this.player(sender); if (player == null) return 0;
        String id = (String) args.get("id");
        var opt = this.module.engine().personals().get(player, id);
        if (opt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerInstance t = opt.get();
        this.module.engine().stop(t,false); this.module.engine().displays().clearTimer(t); this.module.engine().personals().delete(player, id);
        this.module.sendPrefixed(TimerLang.COMMAND_DELETED, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return 1;
    }

    private int editor(CommandSender sender, CommandArguments args) {
        Player player = this.player(sender); if (player == null) return 0;
        Object idObj = args.get("id");
        if (idObj instanceof String id && !id.isBlank()) {
            TimerInstance t = this.module.engine().personals().get(player, id).orElse(null);
            if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
            this.module.openPersonalEditor(player, t); return 1;
        }
        this.plugin.showDialog(player, TimerDialogKeys.PERSONAL_CREATE, TimerScope.PERSONAL, () -> this.module.openPersonalList(player));
        return 1;
    }
}
