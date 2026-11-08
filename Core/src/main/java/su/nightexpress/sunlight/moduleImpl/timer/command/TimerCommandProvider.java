package su.nightexpress.sunlight.moduleImpl.timer.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.sunlight.command.CommandProvider;
import su.nightexpress.sunlight.moduleImpl.timer.TimerDefaults;
import su.nightexpress.sunlight.moduleImpl.timer.TimerModule;
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerLang;
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerPerms;
import su.nightexpress.sunlight.moduleImpl.timer.dialog.TimerDialogKeys;
import su.nightexpress.sunlight.moduleImpl.timer.model.*;
import su.nightexpress.sunlight.moduleImpl.timer.service.GlobalTimerRepository;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerFormat;
import su.nightexpress.sunlight.moduleImpl.timer.service.TimerInput;

import java.util.*;
import java.util.stream.Collectors;

import static su.nightexpress.sunlight.SLPlaceholders.*;

public class TimerCommandProvider extends CommandProvider<TimerModule> {

    public TimerCommandProvider(TimerModule module) {
        super(module, "timer");
    }

    @Override
    public void setup() {
        this.register("list", c -> c.withPermission(TimerPerms.TIMER_LIST)
                .withOptionalArguments(new StringArgument("filter").replaceSuggestions(ArgumentSuggestions.strings("all", "running", "paused", "stopped", "completed", "countdown", "stopwatch")))
                .executes(this::listGlobal)).under("timer");
        this.register("info", c -> c.withPermission(TimerPerms.TIMER_INFO).withArguments(this.globalIdArg()).executes(this::infoGlobal)).under("timer");
        this.register("create", c -> c.withPermission(TimerPerms.TIMER_CREATE)
                .withArguments(new StringArgument("id"), new StringArgument("kind").replaceSuggestions(ArgumentSuggestions.strings("countdown", "stopwatch")), new StringArgument("duration"))
                .executes(this::createGlobal)).under("timer");
        this.register("start", c -> c.withPermission(TimerPerms.TIMER_START).withArguments(this.globalIdArg()).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"start"))).under("timer");
        this.register("pause", c -> c.withPermission(TimerPerms.TIMER_PAUSE).withArguments(this.globalIdArg()).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"pause"))).under("timer");
        this.register("resume", c -> c.withPermission(TimerPerms.TIMER_RESUME).withArguments(this.globalIdArg()).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"resume"))).under("timer");
        this.register("reset", c -> c.withPermission(TimerPerms.TIMER_RESET).withArguments(this.globalIdArg()).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"reset"))).under("timer");
        this.register("stop", c -> c.withPermission(TimerPerms.TIMER_STOP).withArguments(this.globalIdArg()).executes((ResultingCommandExecutor)(s,a)->this.controlGlobal(s,a,"stop"))).under("timer");
        this.register("set", c -> c.withPermission(TimerPerms.TIMER_SET).withArguments(this.globalIdArg(), new StringArgument("time")).executes((ResultingCommandExecutor)(s,a)->this.adjustGlobal(s,a,"set"))).under("timer");
        this.register("add", c -> c.withPermission(TimerPerms.TIMER_ADD).withArguments(this.globalIdArg(), new StringArgument("time")).executes((ResultingCommandExecutor)(s,a)->this.adjustGlobal(s,a,"add"))).under("timer");
        this.register("remove", c -> c.withPermission(TimerPerms.TIMER_REMOVE).withArguments(this.globalIdArg(), new StringArgument("time")).executes((ResultingCommandExecutor)(s,a)->this.adjustGlobal(s,a,"remove"))).under("timer");
        this.register("pauseall", c -> c.withPermission(TimerPerms.TIMER_CONTROL_ALL).executes((ResultingCommandExecutor)(s,a)->this.controlAll(s,"pause"))).under("timer");
        this.register("resumeall", c -> c.withPermission(TimerPerms.TIMER_CONTROL_ALL).executes((ResultingCommandExecutor)(s,a)->this.controlAll(s,"resume"))).under("timer");
        this.register("stopall", c -> c.withPermission(TimerPerms.TIMER_CONTROL_ALL).executes((ResultingCommandExecutor)(s,a)->this.controlAll(s,"stop"))).under("timer");
        this.register("copy", c -> c.withPermission(TimerPerms.TIMER_COPY).withArguments(this.globalIdArg(), new StringArgument("newId")).executes(this::copyGlobal)).under("timer");
        this.register("delete", c -> c.withPermission(TimerPerms.TIMER_DELETE)
                .withArguments(this.globalIdArg())
                .withOptionalArguments(new StringArgument("confirm").replaceSuggestions(ArgumentSuggestions.strings("confirm")))
                .executes(this::deleteGlobal)).under("timer");
        this.register("editor", c -> c.withPermission(TimerPerms.TIMER_EDITOR)
                .withOptionalArguments(this.globalIdArgOptional())
                .executes(this::editorGlobal)).under("timer");
        this.register("reload", c -> c.withPermission(TimerPerms.TIMER_RELOAD).executes(this::reload)).under("timer");

        // /timer personal ...
        this.register("personal", command -> command
                .withPermission(TimerPerms.COMMAND_PTIMER)
                .withSubcommand(new CommandAPICommand("list").executes((ResultingCommandExecutor)(s,a)->this.listPersonal(s)))
                .withSubcommand(new CommandAPICommand("info").withArguments(new StringArgument("id")).executes(this::infoPersonal))
                .withSubcommand(new CommandAPICommand("create").withArguments(new StringArgument("kind").replaceSuggestions(ArgumentSuggestions.strings("countdown","stopwatch")), new StringArgument("duration"), new StringArgument("id")).executes(this::createPersonal))
                .withSubcommand(new CommandAPICommand("start").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"start")))
                .withSubcommand(new CommandAPICommand("pause").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"pause")))
                .withSubcommand(new CommandAPICommand("resume").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"resume")))
                .withSubcommand(new CommandAPICommand("reset").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"reset")))
                .withSubcommand(new CommandAPICommand("stop").withArguments(new StringArgument("id")).executes((ResultingCommandExecutor)(s,a)->this.controlPersonal(s,a,"stop")))
                .withSubcommand(new CommandAPICommand("set").withArguments(new StringArgument("id"), new StringArgument("time")).executes((ResultingCommandExecutor)(s,a)->this.adjustPersonal(s,a,"set")))
                .withSubcommand(new CommandAPICommand("add").withArguments(new StringArgument("id"), new StringArgument("time")).executes((ResultingCommandExecutor)(s,a)->this.adjustPersonal(s,a,"add")))
                .withSubcommand(new CommandAPICommand("remove").withArguments(new StringArgument("id"), new StringArgument("time")).executes((ResultingCommandExecutor)(s,a)->this.adjustPersonal(s,a,"remove")))
                .withSubcommand(new CommandAPICommand("delete").withArguments(new StringArgument("id")).withOptionalArguments(new StringArgument("confirm").replaceSuggestions(ArgumentSuggestions.strings("confirm"))).executes(this::deletePersonal))
                .withSubcommand(new CommandAPICommand("editor").withOptionalArguments(new StringArgument("id")).executes(this::editorPersonal))
                .executes((ResultingCommandExecutor)(s,a)->this.listPersonal(s))
        ).under("timer");

        this.registerRoot("timer", c -> c.withFullDescription(TimerLang.COMMAND_TIMER_DESC.text()).withPermission(TimerPerms.COMMAND_TIMER)
                .executes((sender, args) -> {
                    if (sender instanceof Player player) this.module.openTimerList(player);
                    else this.listGlobal(sender, args);
                    return 1;
                }));
    }

    private StringArgument globalIdArg() {
        return (StringArgument) new StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> this.module.engine().globals().all().stream().map(TimerInstance::getId).toList()));
    }

    private StringArgument globalIdArgOptional() {
        return (StringArgument) new StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> this.module.engine().globals().all().stream().map(TimerInstance::getId).toList()));
    }

    private int listGlobal(CommandSender sender, CommandArguments args) {
        String filter = args.getOptional("filter").map(String::valueOf).orElse("all").toLowerCase(Locale.ROOT);
        List<TimerInstance> all = this.module.engine().globals().all().stream().filter(t -> matchesFilter(t, filter)).collect(Collectors.toList());
        if (all.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_LIST_EMPTY, sender); return 1; }
        this.module.sendPrefixed(TimerLang.COMMAND_LIST_TITLE, sender, b -> b.with(GENERIC_AMOUNT, () -> String.valueOf(all.size())).with(GENERIC_TYPE, () -> filter));
        int limit = 20;
        for (int i = 0; i < Math.min(limit, all.size()); i++) this.sendEntry(sender, all.get(i));
        if (all.size() > limit) this.module.sendPrefixed(TimerLang.COMMAND_LIST_TITLE, sender, b -> b.with(GENERIC_AMOUNT, () -> (all.size() - limit) + " more").with(GENERIC_TYPE, () -> "page"));
        return 1;
    }

    private static boolean matchesFilter(TimerInstance t, String filter) {
        if (filter == null || filter.isBlank() || filter.equals("all")) return true;
        return switch (filter) {
            case "running" -> t.isRunning();
            case "paused" -> t.isPaused();
            case "stopped" -> t.isStopped();
            case "completed" -> t.isCompleted();
            case "countdown" -> t.getDefinition().getKind() == TimerKind.COUNTDOWN;
            case "stopwatch" -> t.getDefinition().getKind() == TimerKind.STOPWATCH;
            default -> true;
        };
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
        OptionalLong secondsOpt = TimerInput.parsePositiveSeconds(String.valueOf(args.get("duration")));
        if (secondsOpt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_INVALID_TIME, sender, b -> b.with(GENERIC_VALUE, () -> String.valueOf(args.get("duration")))); return 0; }
        long seconds = secondsOpt.getAsLong();
        if (this.module.engine().globals().contains(id)) { this.module.sendPrefixed(TimerLang.COMMAND_ALREADY_EXISTS, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerKind kind = kindRaw.startsWith("stop") ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN;
        TimerDefinition def = TimerDefaults.exampleCountdown();
        def.setId(id); def.setName(id); def.setScope(TimerScope.GLOBAL); def.setKind(kind); def.setDurationSeconds(seconds);
        if (kind == TimerKind.STOPWATCH) {
            def.setUseStopwatchTarget(false);
            DisplayRule rule = new DisplayRule(); rule.setId("sw"); rule.setCondition(RuleConditionType.ALWAYS); rule.setChannels(new LinkedHashSet<>(Set.of(DisplayChannel.ACTION_BAR))); rule.setActionBar("<gray>%timer_name%: <green>%timer_formatted%</green></gray>");
            def.setDisplayRules(new ArrayList<>(List.of(rule)));
        }
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

    private int adjustGlobal(CommandSender sender, CommandArguments args, String action) {
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().globals().get(id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        return this.adjust(sender, t, true, action, String.valueOf(args.get("time")));
    }

    private int adjust(CommandSender sender, TimerInstance t, boolean global, String action, String rawTime) {
        OptionalLong parsed = TimerInput.parsePositiveSeconds(rawTime);
        if (parsed.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_INVALID_TIME, sender, b -> b.with(GENERIC_VALUE, () -> rawTime)); return 0; }
        long deltaMs = parsed.getAsLong() * 1000L;
        if (t.getDefinition().getKind() == TimerKind.COUNTDOWN) {
            long next = t.getRemainingMillis();
            if ("set".equals(action)) next = deltaMs;
            else if ("add".equals(action)) next += deltaMs;
            else next -= deltaMs;
            t.setRemainingMillis(Math.max(0L, next));
            t.setElapsedMillis(Math.max(0L, t.getDurationMillis() - t.getRemainingMillis()));
            if (t.getRemainingMillis() <= 0L && t.isRunning()) { this.module.engine().complete(t, global, true); return 1; }
        } else {
            long next = t.getElapsedMillis();
            if ("set".equals(action)) next = deltaMs;
            else if ("add".equals(action)) next += deltaMs;
            else next -= deltaMs;
            t.setElapsedMillis(Math.max(0L, next));
            if (t.getDefinition().hasStopwatchEndTarget() && t.getElapsedMillis() >= t.getDurationMillis() && t.isRunning()) { this.module.engine().complete(t, global, true); return 1; }
        }
        this.module.engine().persist(t, global);
        this.module.engine().displays().tick(t);
        var msg = switch (action) { case "set" -> TimerLang.COMMAND_TIME_SET; case "add" -> TimerLang.COMMAND_TIME_ADDED; default -> TimerLang.COMMAND_TIME_REMOVED; };
        this.module.sendPrefixed(msg, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()).with(GENERIC_TIME, () -> TimerFormat.formatMillis(deltaMs)));
        return 1;
    }

    private int controlAll(CommandSender sender, String action) {
        int count = 0;
        for (TimerInstance t : new ArrayList<>(this.module.engine().globals().all())) {
            boolean ok = switch (action) { case "pause" -> this.module.engine().pause(t,true); case "resume" -> this.module.engine().resume(t,true); case "stop" -> this.module.engine().stop(t,true); default -> false; };
            if (ok) count++;
        }
        int finalCount = count;
        this.module.sendPrefixed(TimerLang.COMMAND_ALL_AFFECTED, sender, b -> b.with(GENERIC_TYPE, () -> action).with(GENERIC_AMOUNT, () -> String.valueOf(finalCount)));
        return 1;
    }

    private int copyGlobal(CommandSender sender, CommandArguments args) {
        String sourceId = (String) args.get("id");
        String newId = GlobalTimerRepository.normalizeId((String) args.get("newId"));
        var srcOpt = this.module.engine().globals().get(sourceId);
        if (srcOpt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> sourceId)); return 0; }
        if (this.module.engine().globals().contains(newId)) { this.module.sendPrefixed(TimerLang.COMMAND_ALREADY_EXISTS, sender, b -> b.with(GENERIC_VALUE, () -> newId)); return 0; }
        TimerDefinition copy = srcOpt.get().getDefinition().copyAs(newId, TimerScope.GLOBAL);
        TimerInstance instance = new TimerInstance(copy, null);
        this.module.engine().globals().put(instance);
        this.module.engine().globals().save(instance);
        this.module.sendPrefixed(TimerLang.COMMAND_COPIED, sender, b -> b.with(GENERIC_VALUE, srcOpt.get()::getId).with(GENERIC_NAME, instance::getId));
        return 1;
    }

    private int deleteGlobal(CommandSender sender, CommandArguments args) {
        String id = (String) args.get("id");
        var opt = this.module.engine().globals().get(id);
        if (opt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        String confirm = args.getOptional("confirm").map(String::valueOf).orElse("");
        if (!"confirm".equalsIgnoreCase(confirm)) {
            this.module.sendPrefixed(TimerLang.COMMAND_CONFIRM_DELETE, sender, b -> b.with(GENERIC_NAME, () -> opt.get().getDefinition().getName()).with(GENERIC_VALUE, () -> "timer delete " + opt.get().getId()));
            return 0;
        }
        TimerInstance t = opt.get();
        this.module.engine().stop(t, true);
        this.module.engine().displays().clearTimer(t);
        this.module.engine().globals().delete(id);
        this.module.sendPrefixed(TimerLang.COMMAND_DELETED, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return 1;
    }

    private int editorGlobal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        Object idObj = args.getOptional("id").orElse(null);
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
        this.module.sendPrefixed(TimerLang.COMMAND_LIST_TITLE, sender, b -> b.with(GENERIC_AMOUNT, () -> String.valueOf(all.size())).with(GENERIC_TYPE, () -> "personal"));
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
        OptionalLong secondsOpt = TimerInput.parsePositiveSeconds(String.valueOf(args.get("duration")));
        if (secondsOpt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_INVALID_TIME, sender, b -> b.with(GENERIC_VALUE, () -> String.valueOf(args.get("duration")))); return 0; }
        String id = GlobalTimerRepository.normalizeId((String) args.get("id"));
        if (this.module.engine().personals().get(player, id).isPresent()) { this.module.sendPrefixed(TimerLang.COMMAND_ALREADY_EXISTS, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        TimerKind kind = kindRaw.startsWith("stop") ? TimerKind.STOPWATCH : TimerKind.COUNTDOWN;
        TimerInstance instance = this.module.engine().personals().create(player, kind, id, secondsOpt.getAsLong());
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

    private int adjustPersonal(CommandSender sender, CommandArguments args, String action) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        String id = (String) args.get("id");
        TimerInstance t = this.module.engine().personals().get(player, id).orElse(null);
        if (t == null) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        return this.adjust(sender, t, false, action, String.valueOf(args.get("time")));
    }

    private int deletePersonal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        String id = (String) args.get("id");
        var opt = this.module.engine().personals().get(player, id);
        if (opt.isEmpty()) { this.module.sendPrefixed(TimerLang.COMMAND_NOT_FOUND, sender, b -> b.with(GENERIC_VALUE, () -> id)); return 0; }
        String confirm = args.getOptional("confirm").map(String::valueOf).orElse("");
        if (!"confirm".equalsIgnoreCase(confirm)) {
            this.module.sendPrefixed(TimerLang.COMMAND_CONFIRM_DELETE, sender, b -> b.with(GENERIC_NAME, () -> opt.get().getDefinition().getName()).with(GENERIC_VALUE, () -> "ptimer delete " + opt.get().getId()));
            return 0;
        }
        TimerInstance t = opt.get();
        this.module.engine().stop(t, false);
        this.module.engine().displays().clearTimer(t);
        this.module.engine().personals().delete(player, id);
        this.module.sendPrefixed(TimerLang.COMMAND_DELETED, sender, b -> b.with(GENERIC_NAME, () -> t.getDefinition().getName()));
        return 1;
    }

    private int editorPersonal(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player player)) { this.module.sendPrefixed(CoreLang.COMMAND_EXECUTION_PLAYER_ONLY, sender); return 0; }
        Object idObj = args.getOptional("id").orElse(null);
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
                .with(GENERIC_NAME, () -> t.getDefinition().getName() + " [" + t.getId() + "]")
                .with(GENERIC_TYPE, () -> t.getDefinition().getKind().name())
                .with(GENERIC_STATE, () -> t.getState().name())
                .with(GENERIC_TIME, () -> TimerFormat.format(t)));
    }

    private void sendInfo(CommandSender sender, TimerInstance t) {
        this.module.sendPrefixed(TimerLang.COMMAND_INFO, sender, b -> b
                .with(GENERIC_VALUE, t::getId)
                .with(GENERIC_NAME, () -> t.getDefinition().getName())
                .with(GENERIC_MESSAGE, () -> t.getDefinition().getScope().name() + " start=" + t.getDefinition().getStartMode().name() + (t.getDefinition().hasStopwatchEndTarget() ? " target=" + t.getDefinition().getDurationSeconds() + "s" : ""))
                .with(GENERIC_TYPE, () -> t.getDefinition().getKind().name())
                .with(GENERIC_STATE, () -> t.getState().name())
                .with(GENERIC_TIME, () -> TimerFormat.format(t))
                .with(GENERIC_TARGET, () -> TimerFormat.formatMillis(t.getDurationMillis()))
                .with(GENERIC_AMOUNT, () -> (t.getElapsedMillis() / 1000L) + "s"));
    }
}
