package su.nightexpress.sunlight.moduleImpl.timer.model;

import java.util.UUID;

public class TimerInstance {

    private final TimerDefinition definition;
    private final UUID ownerId; // null for global
    private TimerState state = TimerState.STOPPED;
    private long durationMillis;
    private long elapsedMillis;
    private long remainingMillis;
    private long lastTickAt;
    private long startedAt;
    private boolean completedActionsFired;
    private boolean stoppedActionsFired;
    private boolean scheduleFired;
    private long scheduledStartEpoch;
    private long chatMarkerSeconds = -1L;

    public TimerInstance(TimerDefinition definition, UUID ownerId) {
        this.definition = definition;
        this.ownerId = ownerId;
        this.durationMillis = Math.max(0L, definition.getDurationSeconds() * 1000L);
        this.resetRuntime();
    }

    public final void resetRuntime() {
        this.durationMillis = Math.max(0L, this.definition.getDurationSeconds() * 1000L);
        this.elapsedMillis = 0L;
        this.remainingMillis = this.definition.getKind() == TimerKind.COUNTDOWN ? this.durationMillis : this.durationMillis;
        this.state = TimerState.STOPPED;
        this.lastTickAt = 0L;
        this.startedAt = 0L;
        this.completedActionsFired = false;
        this.stoppedActionsFired = false;
        this.chatMarkerSeconds = -1L;
    }

    public String getId() { return this.definition.getId(); }
    public TimerDefinition getDefinition() { return this.definition; }
    public UUID getOwnerId() { return this.ownerId; }
    public boolean isGlobal() { return this.ownerId == null; }
    public boolean isPersonal() { return this.ownerId != null; }
    public TimerState getState() { return this.state; }
    public void setState(TimerState state) { this.state = state; }
    public long getDurationMillis() { return this.durationMillis; }
    public void setDurationMillis(long durationMillis) { this.durationMillis = Math.max(0L, durationMillis); }
    public long getElapsedMillis() { return Math.max(0L, this.elapsedMillis); }
    public void setElapsedMillis(long elapsedMillis) { this.elapsedMillis = Math.max(0L, elapsedMillis); }
    public long getRemainingMillis() {
        if (this.definition.getKind() == TimerKind.STOPWATCH) {
            if (this.definition.hasStopwatchEndTarget()) return Math.max(0L, this.durationMillis - this.elapsedMillis);
            return Math.max(0L, this.elapsedMillis);
        }
        return Math.max(0L, this.remainingMillis);
    }
    public void setRemainingMillis(long remainingMillis) { this.remainingMillis = Math.max(0L, remainingMillis); }
    public long getLastTickAt() { return this.lastTickAt; }
    public void setLastTickAt(long lastTickAt) { this.lastTickAt = lastTickAt; }
    public long getStartedAt() { return this.startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }
    public boolean isCompletedActionsFired() { return this.completedActionsFired; }
    public void setCompletedActionsFired(boolean completedActionsFired) { this.completedActionsFired = completedActionsFired; }
    public boolean isStoppedActionsFired() { return this.stoppedActionsFired; }
    public void setStoppedActionsFired(boolean stoppedActionsFired) { this.stoppedActionsFired = stoppedActionsFired; }
    public boolean isScheduleFired() { return this.scheduleFired; }
    public void setScheduleFired(boolean scheduleFired) { this.scheduleFired = scheduleFired; }
    public long getScheduledStartEpoch() { return this.scheduledStartEpoch; }
    public void setScheduledStartEpoch(long scheduledStartEpoch) { this.scheduledStartEpoch = scheduledStartEpoch; }
    public long getChatMarkerSeconds() { return this.chatMarkerSeconds; }
    public void setChatMarkerSeconds(long chatMarkerSeconds) { this.chatMarkerSeconds = chatMarkerSeconds; }

    public boolean isRunning() { return this.state == TimerState.RUNNING; }
    public boolean isPaused() { return this.state == TimerState.PAUSED; }
    public boolean isStopped() { return this.state == TimerState.STOPPED; }
    public boolean isCompleted() { return this.state == TimerState.COMPLETED; }
}
