package su.nightexpress.sunlight.moduleImpl.timer.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.sunlight.command.CommandArgumentConstants;
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

public class TimerCommandProvider extends CommandProvider<TimerModule> {

    public TimerCommandProvider(TimerModule module) {
        super(module, "timer");
    }

    @Override
    public void setup() {
        this.register("list", c -> c.withPermission(TimerPerms.TIMER_LIST).executes((ResultingCommandExecutor)(s, a) -> this.listGlobal(s))).under("timer");
        this.register("info", c -> c.withPermission(TimerPerms.TIMER_INFO).withArguments(new StringArgument("id")).executes(this::infoGlobal)).under("timer");
        this.register("create", c -> c.withPermission(TimerPerms.TIMER_CREATE)
                .withArguments(new StringArgument("id"), new StringArgument("kind"), new IntegerArgument("seconds", 1))
                .executes(this::createGlobal)).under("timer");
        this.register("start", c -> c.withPermission(TimerPerms.TIMER_START).withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"start"))).under("timer");
        this.register("pause", c -> c.withPermission(TimerPerms.TIMER_PAUSE).withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"pause"))).under("timer");
        this.register("resume", c -> c.withPermission(TimerPerms.TIMER_RESUME).withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"resume"))).under("timer");
        this.register("reset", c -> c.withPermission(TimerPerms.TIMER_RESET).withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"reset"))).under("timer");
        this.register("stop", c -> c.withPermission(TimerPerms.TIMER_STOP).withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"stop"))).under("timer");
        this.register("delete", c -> c.withPermission(TimerPerms.TIMER_DELETE).withArguments(new StringArgument("id")).executes(this::deleteGlobal)).under("timer");
        this.register("editor", c -> c.withPermission(TimerPerms.TIMER_EDITOR)
                .withOptionalArguments(new StringArgument("id"))
                .executes(this::editorGlobal)).under("timer");
        this.register("reload", c -> c.withPermission(TimerPerms.TIMER_RELOAD).executes(this::reload)).under("timer");

        // /timer personal ...
        this.register("personal", command -> command
                .withPermission(TimerPerms.COMMAND_PTIMER)
                .withSubcommand(new CommandAPICommand("list").executes((ResultingCommandExecutor)(s,a)->this.listPersonal(s)))
                .withSubcommand(new CommandAPICommand("info").withArguments(new StringArgument("id")).executes(this::infoPersonal))
                .withSubcommand(new CommandAPICommand("create").withArguments(new StringArgument("kind"), new IntegerArgument("seconds", 1), new StringArgument("id")).executes(this::createPersonal))
                .withSubcommand(new CommandAPICommand("start").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"start")))
                .withSubcommand(new CommandAPICommand("pause").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"pause")))
                .withSubcommand(new CommandAPICommand("resume").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"resume")))
                .withSubcommand(new CommandAPICommand("reset").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"reset")))
                .withSubcommand(new CommandAPICommand("stop").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"stop")))
                .withSubcommand(new CommandAPICommand("delete").withArguments(new StringArgument("id")).executes(this::deletePersonal))
                .withSubcommand(new CommandAPICommand("editor").withOptionalArguments(new StringArgument("id")).executes(this::editorPersonal))
                .executes((ResultingCommandExecutor)(s,a)->this.listPersonal(s))
        ).under("timer");

        this.registerRoot("timer", c -> c.withFullDescription(TimerLang.COMMAND_TIMER_DESC.text()).withPermission(TimerPerms.COMMAND_TIMER)
                .executes((sender, args) -> {
                    if (sender instanceof Player player) this.module.openTimerList(player);
                    else this.listGlobal(sender);
                    return 1;
                }));
    }

    private int listGlobal(CommandSender sender) {
        var all = this.module.engine().globals().all();
        if (all.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_LIST_EMPTY, sender); return 1; }
        this.module.sendPrefixed(TimerLang.COMMAND_LIST_TITLE, sender, b -> b.with(GENERIC_AMOUNT, () -> String.valueOf(all.size())));
        for (TimerInstance t : all) this.sendEntry(sender, t);
        return 1;
    }

    private int infoGlobal(CommandSender sender, CommandArguments args) {
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().globals().get(id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        this.sendInfo(sender, t);
        return 1;
    }

    private int createGlobal(CommandSender sender, CommandArguments args) {
        String id = GlobalTimerRepository.normalizeId((String) args.get("id"));
        String kindRaw = ((String) args.get("kind")).toLowerCase(Locale.ROOT);
        int seconds = (int) args.get("seconds");
        if (this.module.engine().globals().contains(id)) { this.module.sendPrefixed(TimerLang.COMMAND_ALREADY_EXISTS, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerKind kind = kindRaw.startsWith("stop") ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN;
        var def = su.nightexpress.sunlight.moduleImpl.timer.TimerDefaults.exampleCountdown();
        def.setId(id); def.setName(id); def.setScope(TimerScope.GLOBAL); def.setKind(kind); def.setDurationSeconds(Math.max(1, seconds));
        if (kind == TimerKind.STOPWATCH) def.getDisplayRules().clear();
        TimerInstance instance = new TimerInstance(def, null);
        this.module.engine().globals().put(instance);
        this.module.engine().globals().save(instance);
        this.module.sendPrefixed(TimerLang.COMMAND_CREATED, sender, b -> b.with(GENERIC_VALUE, instance::getId).with(GENERIC_NAME, () -> def.getName()).with(GENERIC_TYPE, () -> kind.name()));
        return 1;
    }

    private int controlGlobal(CommandSender sender, CommandArguments args, String action) {
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().globals().get(id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        boolean ok = switch (action) { case "start" -> this.module.engine().start(t,true,true); case "pause" -> this.module.engine().pause(t,true); case "resume" -> this.module.engine().resume(t,true); case "reset" -> this.module.engine().reset(t,true); case "stop" -> this.module.engine().stop(t,true); default -> false; };
        this.module.sendPrefixed(switch (action) { case "start" -> TimerLang.COMMAND_STARTED; case "pause" -> TimerLang.COMMAND_PAUSED; case "resume" -> TimerLang.COMMAND_RESUMED; case "reset" -> TimerLang.COMMAND_RESET; default -> TimerLang.COMMAND_STOPPED; }, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return ok ? 1 : 0;
    }

    private int deleteGlobal(CommandSender sender, CommandArguments args) {
        String id = (String) args.get("id");
        var opt = this.module.engine().globals().get(id);
        if (opt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerInstance t = opt.get();
        this.module.engine().stop(t, true);
        this.module.engine().displays().clearTimer(t);
        this.module.engine().globals().delete(id);
        this.module.sendPrefixed(TimerLang.COMMAND_DELETED, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return 1;
    }

    private int editorGlobal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        Object idObj = args.get("id");
        if (idObj instanceof String id && !id.isBlank()) {
            TimerInstance t = this.module.engine().globals().get(id).orElse(null);
            if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
            this.module.openTimerEditor(player, t);
            return 1;
        }
        this.plugin.showDialog(player, TimerDialogKeys.GLOBAL_CREATE, TimerScope.GLOBAL, () -> this.module.openTimerList(player));
        return 1;
    }

    private int reload(CommandSender sender, CommandArguments args) {
        this.module.reloadTimers();
        this.module.sendPrefixed(TimerLang.COMMAND_RELOADED, sender, b -> b.with(GENERIC_AMOUNT, () -> String.valueOf(this.module.engine().globals().all().size())));
        return 1;
    }

    private int listPersonal(CommandSender sender) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        var all = this.module.engine().personals().all(player);
        if (all.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_LIST_EMPTY, sender); return 1; }
        this.module.sendPrefixed(TimerLang.COMMAND_LIST_TITLE, sender, b -> b.with(GENERIC_AMOUNT, () -> String.valueOf(all.size())));
        for (TimerInstance t : all) this.sendEntry(sender, t);
        return 1;
    }

    private int infoPersonal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().personals().get(player, id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        this.sendInfo(sender, t); return 1;
    }

    private int createPersonal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        String kindRaw = ((String) args.get("kind")).toLowerCase(Locale.ROOT);
        int seconds = (int) args.get("seconds");
        String id = GlobalTimerRepository.normalizeId((String) args.get("id"));
        if (this.module.engine().personals().get(player, id).isPresent()) { this.module.sendPrefixed(TimerLang.COMMAND_ALREADY_EXISTS, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerKind kind = kindRaw.startsWith("stop") ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN;
        TimerInstance instance = this.module.engine().personals().create(player, kind, id, seconds);
        this.module.sendPrefixed(TimerLang.COMMAND_CREATED, sender, b -> b.with(GENERIC_VALUE, instance::getId).with(GENERIC_NAME, () -> instance.getDefinition().getName()).with(GENERIC_TYPE, () -> kind.name()));
        return 1;
    }

    private int controlPersonal(CommandSender sender, CommandArguments args, String action) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().personals().get(player, id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        boolean ok = switch (action) { case "start" -> this.module.engine().start(t,false,true); case "pause" -> this.module.engine().pause(t,false); case "resume" -> this.module.engine().resume(t,false); case "reset" -> this.module.engine().reset(t,false); case "stop" -> this.module.engine().stop(t,false); default -> false; };
        this.module.sendPrefixed(switch (action) { case "start" -> TimerLang.COMMAND_STARTED; case "pause" -> TimerLang.COMMAND_PAUSED; case "resume" -> TimerLang.COMMAND_RESUMED; case "reset" -> TimerLang.COMMAND_RESET; default -> TimerLang.COMMAND_STOPPED; }, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return ok ? 1 : 0;
    }

    private int deletePersonal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        String id = (String) args.get("id");
        var opt = this.module.engine().personals().get(player, id);
        if (opt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerInstance t = opt.get();
        this.module.engine().stop(t, false);
        this.module.engine().displays().clearTimer(t);
        this.module.engine().personals().delete(player, id);
        this.module.sendPrefixed(TimerLang.COMMAND_DELETED, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return 1;
    }

    private int editorPersonal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        Object idObj = args.get("id");
        if (idObj instanceof String id && !id.isBlank()) {
            TimerInstance t = this.module.engine().personals().get(player, id).orElse(null);
            if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
            this.module.openPersonalEditor(player, t);
            return 1;
        }
        this.plugin.showDialog(player, TimerDialogKeys.PERSONAL_CREATE, TimerScope.PERSONAL, () -> this.module.openPersonalList(player));
        return 1;
    }

    private void sendEntry(CommandSender sender, TimerInstance t) {
        this.module.sendPrefixed(TimerLang.COMMAND_LIST_ENTRY, sender, b -> b
                .with(GENERIC_NAME, () -> t.getDefinition().getName())
                .with(GENERIC_TYPE, () -> t.getDefinition().getKind().name())
                .with(GENERIC_STATE, () -> t.getState().name())
                .with(GENERIC_TIME, () -> TimerFormat.format(t)));
    }

    private void sendInfo(CommandSender sender, TimerInstance t) {
        this.module.sendPrefixed(TimerLang.COMMAND_INFO, sender, b -> b
                .with(GENERIC_VALUE, t::getId)
                .with(GENERIC_NAME, () -> t.getDefinition().getName())
                .with(GENERIC_MESSAGE, () -> t.getDefinition().getScope().name())
                .with(GENERIC_TYPE, () -> t.getDefinition().getKind().name())
                .with(GENERIC_STATE, () -> t.getState().name())
                .with(GENERIC_TIME, () -> TimerFormat.format(t))
                .with(GENERIC_TARGET, () -> t.getDefinition().getDurationSeconds() + "s")
                .with(GENERIC_AMOUNT, () -> (t.getElapsedMillis() / 1000L) + "s"));
    }
}
