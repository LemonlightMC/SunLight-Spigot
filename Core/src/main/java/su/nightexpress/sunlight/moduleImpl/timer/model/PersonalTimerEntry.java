package su.nightexpress.sunlight.moduleImpl.timer.model;

public class PersonalTimerEntry {

    private TimerDefinition definition = new TimerDefinition();
    private TimerState state = TimerState.STOPPED;
    private long elapsedMillis = 0L;
    private long remainingMillis = 0L;
    private long scheduledStartEpoch = 0L;
    private boolean scheduleFired = false;

    public PersonalTimerEntry() {
    }

    public TimerDefinition getDefinition() { if (this.definition == null) this.definition = new TimerDefinition(); return this.definition; }
    public void setDefinition(TimerDefinition definition) { this.definition = definition; }
    public TimerState getState() { return this.state == null ? TimerState.STOPPED : this.state; }
    public void setState(TimerState state) { this.state = state; }
    public long getElapsedMillis() { return Math.max(0L, this.elapsedMillis); }
    public void setElapsedMillis(long elapsedMillis) { this.elapsedMillis = elapsedMillis; }
    public long getRemainingMillis() { return Math.max(0L, this.remainingMillis); }
    public void setRemainingMillis(long remainingMillis) { this.remainingMillis = remainingMillis; }
    public long getScheduledStartEpoch() { return this.scheduledStartEpoch; }
    public void setScheduledStartEpoch(long scheduledStartEpoch) { this.scheduledStartEpoch = scheduledStartEpoch; }
    public boolean isScheduleFired() { return this.scheduleFired; }
    public void setScheduleFired(boolean scheduleFired) { this.scheduleFired = scheduleFired; }
}
