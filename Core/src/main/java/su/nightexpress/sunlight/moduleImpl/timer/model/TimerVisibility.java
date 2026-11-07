package su.nightexpress.sunlight.moduleImpl.timer.model;

public class TimerVisibility {

    private VisibilityMode mode = VisibilityMode.ALL;
    private String permission = "";

    public TimerVisibility() {
    }

    public TimerVisibility(VisibilityMode mode, String permission) {
        this.mode = mode == null ? VisibilityMode.ALL : mode;
        this.permission = permission == null ? "" : permission;
    }

    public VisibilityMode getMode() {
        return this.mode == null ? VisibilityMode.ALL : this.mode;
    }

    public void setMode(VisibilityMode mode) {
        this.mode = mode;
    }

    public String getPermission() {
        return this.permission == null ? "" : this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
