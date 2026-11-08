package su.nightexpress.sunlight.moduleImpl.timer.model;

import java.util.ArrayList;
import java.util.List;

public class TimerDefinition {

    private String id = "timer";
    private String name = "Timer";
    private TimerScope scope = TimerScope.GLOBAL;
    private TimerKind kind = TimerKind.COUNTDOWN;
    private long durationSeconds = 60L; // countdown duration / stopwatch target (0 = no target)
    private boolean useStopwatchTarget = false; // stopwatch ends when reaching durationSeconds
    private TimerStartMode startMode = TimerStartMode.MANUAL;
    private TimerSchedule schedule = new TimerSchedule();
    private TimerVisibility visibility = new TimerVisibility();
    private List<DisplayRule> displayRules = new ArrayList<>();
    private List<TimerEventTrigger> stopEvents = new ArrayList<>();
    private TimerActions actions = new TimerActions();
    private boolean restoreXpOnStop = true;

    public TimerDefinition() {
    }

    public TimerDefinition copyAs(String newId, TimerScope scope) {
        TimerDefinition copy = new TimerDefinition();
        copy.id = newId;
        copy.name = this.name;
        copy.scope = scope;
        copy.kind = this.kind;
        copy.durationSeconds = this.durationSeconds;
        copy.useStopwatchTarget = this.useStopwatchTarget;
        copy.startMode = this.startMode;
        copy.schedule = this.schedule; // shallow ok for copy template
        copy.visibility = this.visibility;
        copy.displayRules = new ArrayList<>(this.displayRules);
        copy.stopEvents = new ArrayList<>(this.stopEvents);
        copy.actions = this.actions;
        copy.restoreXpOnStop = this.restoreXpOnStop;
        return copy;
    }

    public String getId() { return this.id == null ? "timer" : this.id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return this.name == null || this.name.isBlank() ? this.getId() : this.name; }
    public void setName(String name) { this.name = name; }
    public TimerScope getScope() { return this.scope == null ? TimerScope.GLOBAL : this.scope; }
    public void setScope(TimerScope scope) { this.scope = scope; }
    public TimerKind getKind() { return this.kind == null ? TimerKind.COUNTDOWN : this.kind; }
    public void setKind(TimerKind kind) { this.kind = kind; }
    public long getDurationSeconds() { return Math.max(0L, this.durationSeconds); }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    public boolean isUseStopwatchTarget() { return this.useStopwatchTarget; }
    public void setUseStopwatchTarget(boolean useStopwatchTarget) { this.useStopwatchTarget = useStopwatchTarget; }
    public TimerStartMode getStartMode() { if (this.startMode == null) this.startMode = this.getSchedule().isEnabled() ? TimerStartMode.SCHEDULED : TimerStartMode.MANUAL; return this.startMode; }
    public void setStartMode(TimerStartMode startMode) { this.startMode = startMode == null ? TimerStartMode.MANUAL : startMode; }
    public TimerSchedule getSchedule() { if (this.schedule == null) this.schedule = new TimerSchedule(); return this.schedule; }
    public void setSchedule(TimerSchedule schedule) { this.schedule = schedule; }
    public TimerVisibility getVisibility() { if (this.visibility == null) this.visibility = new TimerVisibility(); return this.visibility; }
    public void setVisibility(TimerVisibility visibility) { this.visibility = visibility; }
    public List<DisplayRule> getDisplayRules() { if (this.displayRules == null) this.displayRules = new ArrayList<>(); return this.displayRules; }
    public void setDisplayRules(List<DisplayRule> displayRules) { this.displayRules = displayRules; }
    public List<TimerEventTrigger> getStopEvents() { if (this.stopEvents == null) this.stopEvents = new ArrayList<>(); return this.stopEvents; }
    public void setStopEvents(List<TimerEventTrigger> stopEvents) { this.stopEvents = stopEvents; }
    public TimerActions getActions() { if (this.actions == null) this.actions = new TimerActions(); return this.actions; }
    public void setActions(TimerActions actions) { this.actions = actions; }
    public boolean isRestoreXpOnStop() { return this.restoreXpOnStop; }
    public void setRestoreXpOnStop(boolean restoreXpOnStop) { this.restoreXpOnStop = restoreXpOnStop; }

    public boolean hasStopwatchEndTarget() {
        return this.getKind() == TimerKind.STOPWATCH && this.isUseStopwatchTarget() && this.getDurationSeconds() > 0L;
    }
}
