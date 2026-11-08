package su.nightexpress.sunlight.moduleImpl.timer.model;

import java.util.ArrayList;
import java.util.List;

public class TimerVisibility {

    private VisibilityMode mode = VisibilityMode.ALL;
    private String permission = "";
    private String staffPermission = "";
    private boolean bypassPermissionApplies = true;
    private List<String> worlds = new ArrayList<>();

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

    public String getStaffPermission() {
        return this.staffPermission == null ? "" : this.staffPermission;
    }

    public void setStaffPermission(String staffPermission) {
        this.staffPermission = staffPermission == null ? "" : staffPermission;
    }

    public boolean isBypassPermissionApplies() {
        return this.bypassPermissionApplies;
    }

    public void setBypassPermissionApplies(boolean bypassPermissionApplies) {
        this.bypassPermissionApplies = bypassPermissionApplies;
    }

    public List<String> getWorlds() {
        if (this.worlds == null) this.worlds = new ArrayList<>();
        return this.worlds;
    }

    public void setWorlds(List<String> worlds) {
        this.worlds = worlds == null ? new ArrayList<>() : new ArrayList<>(worlds);
    }
}
