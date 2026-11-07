package su.nightexpress.sunlight.moduleImpl.timer.service;

import org.bukkit.entity.Player;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.timer.model.*;

import java.util.*;

public class TimerEngine {

    private final SunLightPlugin plugin;
    private final GlobalTimerRepository globalRepository;
    private final PersonalTimerService personalService;
    private final TimerDisplayService displayService;
    private final TimerActionService actionService;
    private final TimerScheduleService scheduleService;
    private long saveTicker;

    public TimerEngine(SunLightPlugin plugin, GlobalTimerRepository globalRepository, PersonalTimerService personalService,
                       TimerDisplayService displayService, TimerActionService actionService, TimerScheduleService scheduleService) {
        this.plugin = plugin;
        this.globalRepository = globalRepository;
        this.personalService = personalService;
        this.displayService = displayService;
        this.actionService = actionService;
        this.scheduleService = scheduleService;
    }

    public void tick() {
        for (TimerInstance instance : new ArrayList<>(this.globalRepository.all())) {
            this.tickSchedule(instance, true);
            this.tickInstance(instance, true);
        }
        for (Map.Entry<UUID, Map<String, TimerInstance>> ownerEntry : this.personalService.runtimeEntries()) {
            for (TimerInstance instance : new ArrayList<>(ownerEntry.getValue().values())) {
                this.tickSchedule(instance, false);
                this.tickInstance(instance, false);
            }
        }
        if (++this.saveTicker % 30 == 0) {
            this.saveAll();
        }
    }

    private void tickSchedule(TimerInstance instance, boolean global) {
        if (this.scheduleService.shouldAutoStart(instance)) {
            instance.setScheduleFired(true);
            this.start(instance, global, false);
        }
    }

    private void tickInstance(TimerInstance instance, boolean global) {
        if (instance.isRunning()) {
            if (instance.getDefinition().getKind() == TimerKind.COUNTDOWN) {
                instance.setRemainingMillis(instance.getRemainingMillis() - 1000L);
                instance.setElapsedMillis(Math.max(0L, instance.getDurationMillis() - instance.getRemainingMillis()));
                if (instance.getRemainingMillis() <= 0L) {
                    instance.setRemainingMillis(0L);
                    this.complete(instance, global, true);
                    return;
                }
            }
            else {
                instance.setElapsedMillis(instance.getElapsedMillis() + 1000L);
                if (instance.getDefinition().hasStopwatchEndTarget() && instance.getElapsedMillis() >= instance.getDurationMillis()) {
                    instance.setElapsedMillis(instance.getDurationMillis());
                    this.complete(instance, global, true);
                    return;
                }
            }
        }
        this.displayService.tick(instance);
    }

    public boolean start(TimerInstance instance, boolean global, boolean fireActions) {
        if (instance == null) return false;
        if (instance.isRunning()) return true;
        if (instance.isCompleted()) instance.resetRuntime();
        if (instance.getDefinition().getKind() == TimerKind.COUNTDOWN && instance.getRemainingMillis() <= 0L) {
            instance.setRemainingMillis(instance.getDurationMillis());
        }
        instance.setState(TimerState.RUNNING);
        instance.setStartedAt(System.currentTimeMillis());
        instance.setStoppedActionsFired(false);
        if (fireActions) this.actionService.execute(instance.getDefinition().getActions().getOnStart(), instance, this.displayService.resolveViewers(instance));
        this.persist(instance, global);
        this.displayService.tick(instance);
        return true;
    }

    public boolean pause(TimerInstance instance, boolean global) {
        if (instance == null || !instance.isRunning()) return false;
        instance.setState(TimerState.PAUSED);
        this.actionService.execute(instance.getDefinition().getActions().getOnPause(), instance, this.displayService.resolveViewers(instance));
        this.persist(instance, global);
        this.displayService.tick(instance);
        return true;
    }

    public boolean resume(TimerInstance instance, boolean global) {
        if (instance == null || !instance.isPaused()) return false;
        instance.setState(TimerState.RUNNING);
        this.actionService.execute(instance.getDefinition().getActions().getOnResume(), instance, this.displayService.resolveViewers(instance));
        this.persist(instance, global);
        this.displayService.tick(instance);
        return true;
    }

    public boolean reset(TimerInstance instance, boolean global) {
        if (instance == null) return false;
        boolean restore = instance.getDefinition().isRestoreXpOnStop();
        instance.resetRuntime();
        if (restore) this.displayService.clearTimer(instance);
        else this.displayService.tick(instance);
        this.persist(instance, global);
        return true;
    }

    public boolean stop(TimerInstance instance, boolean global) {
        if (instance == null) return false;
        if (instance.isStopped()) return true;
        instance.setState(TimerState.STOPPED);
        if (!instance.isStoppedActionsFired()) {
            instance.setStoppedActionsFired(true);
            this.actionService.execute(instance.getDefinition().getActions().getOnStop(), instance, this.displayService.resolveViewers(instance));
        }
        this.displayService.clearTimer(instance);
        this.persist(instance, global);
        return true;
    }

    public boolean complete(TimerInstance instance, boolean global, boolean fireActions) {
        if (instance == null) return false;
        instance.setState(TimerState.COMPLETED);
        Collection<Player> viewers = this.displayService.resolveViewers(instance);
        if (fireActions && !instance.isCompletedActionsFired()) {
            instance.setCompletedActionsFired(true);
            if (instance.getDefinition().getKind() == TimerKind.COUNTDOWN) {
                this.actionService.execute(instance.getDefinition().getActions().getOnComplete(), instance, viewers);
            }
            else {
                ActionBundle bundle = instance.getDefinition().getActions().getOnStopwatchEnd();
                if (bundle == null || bundle.isEmpty()) bundle = instance.getDefinition().getActions().getOnComplete();
                this.actionService.execute(bundle, instance, viewers);
            }
            // Completion messages are handled by action bundles / display rules as configured.

        }
        this.displayService.clearTimer(instance);
        this.persist(instance, global);
        return true;
    }

    public boolean triggerEvent(TimerInstance instance, boolean global, String triggerType, String entityType, UUID involvedPlayer) {
        if (instance == null || instance.isCompleted() || instance.isStopped()) return false;
        for (TimerEventTrigger trigger : instance.getDefinition().getStopEvents()) {
            if (trigger == null) continue;
            String type = trigger.getType() == null ? "" : trigger.getType().trim().toUpperCase(Locale.ROOT);
            if (!type.equals(triggerType)) continue;
            if (type.equals("ENTITY_DEATH")) {
                String wanted = trigger.getEntityType() == null ? "" : trigger.getEntityType().trim().toUpperCase(Locale.ROOT);
                if (!wanted.isBlank() && !wanted.equals(entityType)) continue;
            }
            if (instance.isPersonal() && involvedPlayer != null && !involvedPlayer.equals(instance.getOwnerId())) continue;
            String action = trigger.getAction() == null ? "COMPLETE" : trigger.getAction().trim().toUpperCase(Locale.ROOT);
            return switch (action) {
                case "STOP" -> this.stop(instance, global);
                case "PAUSE" -> this.pause(instance, global);
                case "RESET" -> this.reset(instance, global);
                default -> this.complete(instance, global, true);
            };
        }
        return false;
    }

    public void persist(TimerInstance instance, boolean global) {
        if (global) this.globalRepository.save(instance);
        else if (instance.getOwnerId() != null) this.personalService.save(instance.getOwnerId());
    }

    public void saveAll() {
        for (TimerInstance instance : this.globalRepository.all()) {
            this.globalRepository.save(instance);
        }
        this.personalService.saveAll();
    }

    public GlobalTimerRepository globals() { return this.globalRepository; }
    public PersonalTimerService personals() { return this.personalService; }
    public TimerDisplayService displays() { return this.displayService; }
    public TimerActionService actions() { return this.actionService; }
    public TimerScheduleService schedules() { return this.scheduleService; }
}
