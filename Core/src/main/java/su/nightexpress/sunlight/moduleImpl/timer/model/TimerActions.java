package su.nightexpress.sunlight.moduleImpl.timer.model;

public class TimerActions {

    private ActionBundle onStart = new ActionBundle();
    private ActionBundle onPause = new ActionBundle();
    private ActionBundle onResume = new ActionBundle();
    private ActionBundle onComplete = new ActionBundle();
    private ActionBundle onStopwatchEnd = new ActionBundle();
    private ActionBundle onStop = new ActionBundle();

    public TimerActions() {
    }

    public ActionBundle getOnStart() { if (this.onStart == null) this.onStart = new ActionBundle(); return this.onStart; }
    public void setOnStart(ActionBundle onStart) { this.onStart = onStart; }
    public ActionBundle getOnPause() { if (this.onPause == null) this.onPause = new ActionBundle(); return this.onPause; }
    public void setOnPause(ActionBundle onPause) { this.onPause = onPause; }
    public ActionBundle getOnResume() { if (this.onResume == null) this.onResume = new ActionBundle(); return this.onResume; }
    public void setOnResume(ActionBundle onResume) { this.onResume = onResume; }
    public ActionBundle getOnComplete() { if (this.onComplete == null) this.onComplete = new ActionBundle(); return this.onComplete; }
    public void setOnComplete(ActionBundle onComplete) { this.onComplete = onComplete; }
    public ActionBundle getOnStopwatchEnd() { if (this.onStopwatchEnd == null) this.onStopwatchEnd = new ActionBundle(); return this.onStopwatchEnd; }
    public void setOnStopwatchEnd(ActionBundle onStopwatchEnd) { this.onStopwatchEnd = onStopwatchEnd; }
    public ActionBundle getOnStop() { if (this.onStop == null) this.onStop = new ActionBundle(); return this.onStop; }
    public void setOnStop(ActionBundle onStop) { this.onStop = onStop; }
}
