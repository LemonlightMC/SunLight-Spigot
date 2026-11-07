package su.nightexpress.sunlight.moduleImpl.timer.model;

public class TimerSchedule {

    private boolean enabled = false;
    private String startAt = ""; // yyyy-MM-dd HH:mm:ss or yyyy-MM-dd HH:mm
    private String zoneId = "UTC";
    private boolean autoStart = true;
    private boolean runMissedOnLoad = false;

    public TimerSchedule() {
    }

    public boolean isEnabled() { return this.enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getStartAt() { return this.startAt == null ? "" : this.startAt; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public String getZoneId() { return this.zoneId == null || this.zoneId.isBlank() ? "UTC" : this.zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public boolean isAutoStart() { return this.autoStart; }
    public void setAutoStart(boolean autoStart) { this.autoStart = autoStart; }
    public boolean isRunMissedOnLoad() { return this.runMissedOnLoad; }
    public void setRunMissedOnLoad(boolean runMissedOnLoad) { this.runMissedOnLoad = runMissedOnLoad; }
}
