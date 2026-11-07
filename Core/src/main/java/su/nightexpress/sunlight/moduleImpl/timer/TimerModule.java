package su.nightexpress.sunlight.moduleImpl.timer;

import org.bukkit.entity.Player;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.sunlight.SLPlaceholders;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.api.provider.TimerProvider;
import su.nightexpress.sunlight.hook.placeholder.PlaceholderRegistry;
import su.nightexpress.sunlight.module.Module;
import su.nightexpress.sunlight.module.ModuleDefinition;
import su.nightexpress.sunlight.moduleImpl.timer.command.PersonalTimerCommandProvider;
import su.nightexpress.sunlight.moduleImpl.timer.command.TimerCommandProvider;
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerFiles;
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerLang;
import su.nightexpress.sunlight.moduleImpl.timer.dialog.TimerDialogKeys;
import su.nightexpress.sunlight.moduleImpl.timer.dialog.impl.*;
import su.nightexpress.sunlight.moduleImpl.timer.listener.TimerListener;
import su.nightexpress.sunlight.moduleImpl.timer.menu.PersonalTimerEditorMenu;
import su.nightexpress.sunlight.moduleImpl.timer.menu.PersonalTimerListMenu;
import su.nightexpress.sunlight.moduleImpl.timer.menu.TimerEditorMenu;
import su.nightexpress.sunlight.moduleImpl.timer.menu.TimerListMenu;
import su.nightexpress.sunlight.moduleImpl.timer.model.*;
import su.nightexpress.sunlight.moduleImpl.timer.service.*;
import su.nightexpress.sunlight.user.property.UserPropertyRegistry;
import su.nightexpress.sunlight.utils.Utils;

import java.nio.file.Path;
import java.util.*;

public class TimerModule extends Module implements TimerProvider {

    private GlobalTimerRepository globalRepository;
    private PersonalTimerService personalService;
    private XpBarService xpBarService;
    private TimerActionService actionService;
    private TimerDisplayService displayService;
    private TimerScheduleService scheduleService;
    private TimerEngine engine;

    private TimerListMenu timerListMenu;
    private TimerEditorMenu timerEditorMenu;
    private PersonalTimerListMenu personalListMenu;
    private PersonalTimerEditorMenu personalEditorMenu;

    public TimerModule(ModuleDefinition<TimerModule> definition, SunLightPlugin plugin) {
        super(definition, plugin);
    }

    @Override
    protected void loadModule(FileConfig config) {
        this.plugin.injectLang(TimerLang.class);
        UserPropertyRegistry.register(PersonalTimerService.PERSONAL_TIMERS);

        Path modulePath = Path.of(this.getSystemPath());
        this.globalRepository = new GlobalTimerRepository(modulePath);
        this.globalRepository.loadAll();
        this.personalService = new PersonalTimerService(this.userManager);
        this.xpBarService = new XpBarService();
        this.actionService = new TimerActionService(this.plugin);
        this.displayService = new TimerDisplayService(this.plugin, this.xpBarService);
        this.scheduleService = new TimerScheduleService();
        this.engine = new TimerEngine(this.plugin, this.globalRepository, this.personalService, this.displayService, this.actionService, this.scheduleService);

        this.loadMenus();
        this.registerDialogs();

        this.addListener(new TimerListener(this.plugin, this));
        this.commandRegistry.addProvider(new TimerCommandProvider(this));
        this.commandRegistry.addProvider(new PersonalTimerCommandProvider(this));

        this.addTask(this.engine::tick, 20L);
        this.info("Loaded " + this.globalRepository.all().size() + " global timers.");
    }

    @Override
    protected void unloadModule() {
        if (this.engine != null) {
            this.engine.saveAll();
        }
        if (this.displayService != null) {
            this.displayService.shutdown(this.plugin.getServer().getOnlinePlayers());
        }
        this.timerListMenu = null;
        this.timerEditorMenu = null;
        this.personalListMenu = null;
        this.personalEditorMenu = null;
    }

    private void loadMenus() {
        try {
            this.timerListMenu = new TimerListMenu(this.plugin, this);
            this.timerListMenu.load(this.plugin, FileConfig.load(this.getLocalUIPath(), "timer_list.yml"));
            this.timerEditorMenu = new TimerEditorMenu(this.plugin, this);
            this.timerEditorMenu.load(this.plugin, FileConfig.load(this.getLocalUIPath(), "timer_editor.yml"));
            this.personalListMenu = new PersonalTimerListMenu(this.plugin, this);
            this.personalListMenu.load(this.plugin, FileConfig.load(this.getLocalUIPath(), "personal_timer_list.yml"));
            this.personalEditorMenu = new PersonalTimerEditorMenu(this.plugin, this);
            this.personalEditorMenu.load(this.plugin, FileConfig.load(this.getLocalUIPath(), "personal_timer_editor.yml"));
        }
        catch (Exception exception) {
            this.warn("Could not fully load timer menus: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void registerDialogs() {
        this.dialogRegistry.register(TimerDialogKeys.GLOBAL_CREATE, () -> new TimerCreateDialog(this, TimerScope.GLOBAL));
        this.dialogRegistry.register(TimerDialogKeys.PERSONAL_CREATE, () -> new TimerCreateDialog(this, TimerScope.PERSONAL));
        this.dialogRegistry.register(TimerDialogKeys.TIMER_NAME, () -> new TimerNameDialog(this));
        this.dialogRegistry.register(TimerDialogKeys.TIMER_DURATION, () -> new TimerDurationDialog(this));
        this.dialogRegistry.register(TimerDialogKeys.TIMER_SCHEDULE, () -> new TimerScheduleDialog(this));
        this.dialogRegistry.register(TimerDialogKeys.TIMER_VISIBILITY, () -> new TimerVisibilityDialog(this));
        this.dialogRegistry.register(TimerDialogKeys.TIMER_ACTION, () -> new TimerActionDialog(this));
        this.dialogRegistry.register(TimerDialogKeys.TIMER_RULE, () -> new TimerRuleDialog(this));
        this.dialogRegistry.register(TimerDialogKeys.TIMER_TRIGGER, () -> new TimerTriggerDialog(this));
    }

    @Override
    public void registerPlaceholders(PlaceholderRegistry registry) {
        registry.register("timer_state", (player, payload) -> this.resolveGlobal(payload).map(i -> i.getState().name()).orElse(""));
        registry.register("timer_name", (player, payload) -> this.resolveGlobal(payload).map(i -> i.getDefinition().getName()).orElse(""));
        registry.register("timer_kind", (player, payload) -> this.resolveGlobal(payload).map(i -> i.getDefinition().getKind().name()).orElse(""));
        registry.register("timer_formatted", (player, payload) -> this.resolveGlobal(payload).map(TimerFormat::format).orElse(""));
        registry.register("timer_remaining", (player, payload) -> this.resolveGlobal(payload).map(i -> String.valueOf(i.getRemainingMillis() / 1000L)).orElse("0"));
        registry.register("timer_elapsed", (player, payload) -> this.resolveGlobal(payload).map(i -> String.valueOf(i.getElapsedMillis() / 1000L)).orElse("0"));
        registry.register("timer_seconds", (player, payload) -> this.resolveGlobal(payload).map(i -> String.valueOf((i.getDefinition().getKind() == TimerKind.COUNTDOWN ? i.getRemainingMillis() : i.getElapsedMillis()) / 1000L)).orElse("0"));
        registry.register("timer_progress", (player, payload) -> this.resolveGlobal(payload).map(i -> NumberUtil.format(TimerFormat.progress(i) * 100D)).orElse("0"));
    }

    private Optional<TimerInstance> resolveGlobal(String payload) {
        if (payload == null || payload.isBlank()) return Optional.empty();
        return this.globalRepository.get(payload);
    }

    public String formatText(String text, TimerInstance instance, Player player) {
        String out = TimerFormat.apply(text, instance);
        if (player != null) {
            PlaceholderContext context = PlaceholderContext.builder()
                    .with(CommonPlaceholders.PLAYER.resolver(player))
                    .andThen(SLPlaceholders.forPlayerWithPAPI(player))
                    .build();
            out = context.apply(out);
        }
        return out;
    }

    public TimerEngine engine() { return this.engine; }

    public void openTimerList(Player player) {
        if (this.timerListMenu != null) this.timerListMenu.show(this.plugin, player);
    }

    public void openTimerEditor(Player player, TimerInstance instance) {
        if (this.timerEditorMenu != null) this.timerEditorMenu.show(this.plugin, player, instance);
    }

    public void openPersonalList(Player player) {
        if (this.personalListMenu != null) this.personalListMenu.show(this.plugin, player);
    }

    public void openPersonalEditor(Player player, TimerInstance instance) {
        if (this.personalEditorMenu != null) this.personalEditorMenu.show(this.plugin, player, instance);
    }

    public void reloadTimers() {
        this.engine.saveAll();
        this.globalRepository.loadAll();
    }

    // TimerProvider
    @Override public Set<String> getGlobalTimerIds() { return this.globalRepository.all().stream().map(TimerInstance::getId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)); }
    @Override public Set<String> getPersonalTimerIds(UUID playerId) { return new LinkedHashSet<>(this.personalService.getOrLoad(playerId).keySet()); }
    @Override public boolean hasGlobalTimer(String id) { return this.globalRepository.contains(id); }
    @Override public boolean hasPersonalTimer(UUID playerId, String id) { return this.personalService.getOrLoad(playerId).containsKey(GlobalTimerRepository.normalizeId(id)); }
    @Override public Optional<String> getTimerState(String id) { return this.globalRepository.get(id).map(i -> i.getState().name()); }
    @Override public Optional<String> getPersonalTimerState(UUID playerId, String id) { return Optional.ofNullable(this.personalService.getOrLoad(playerId).get(GlobalTimerRepository.normalizeId(id))).map(i -> i.getState().name()); }
    @Override public boolean startGlobalTimer(String id) { return this.globalRepository.get(id).map(i -> this.engine.start(i, true, true)).orElse(false); }
    @Override public boolean pauseGlobalTimer(String id) { return this.globalRepository.get(id).map(i -> this.engine.pause(i, true)).orElse(false); }
    @Override public boolean resumeGlobalTimer(String id) { return this.globalRepository.get(id).map(i -> this.engine.resume(i, true)).orElse(false); }
    @Override public boolean stopGlobalTimer(String id) { return this.globalRepository.get(id).map(i -> this.engine.stop(i, true)).orElse(false); }
    @Override public boolean resetGlobalTimer(String id) { return this.globalRepository.get(id).map(i -> this.engine.reset(i, true)).orElse(false); }
    @Override public boolean startPersonalTimer(Player player, String id) { return this.personalService.get(player, id).map(i -> this.engine.start(i, false, true)).orElse(false); }
    @Override public boolean pausePersonalTimer(Player player, String id) { return this.personalService.get(player, id).map(i -> this.engine.pause(i, false)).orElse(false); }
    @Override public boolean resumePersonalTimer(Player player, String id) { return this.personalService.get(player, id).map(i -> this.engine.resume(i, false)).orElse(false); }
    @Override public boolean stopPersonalTimer(Player player, String id) { return this.personalService.get(player, id).map(i -> this.engine.stop(i, false)).orElse(false); }
    @Override public boolean resetPersonalTimer(Player player, String id) { return this.personalService.get(player, id).map(i -> this.engine.reset(i, false)).orElse(false); }
}
