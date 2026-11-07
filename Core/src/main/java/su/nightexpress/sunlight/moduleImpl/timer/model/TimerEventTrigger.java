package su.nightexpress.sunlight.moduleImpl.timer.model;

public class TimerEventTrigger {

    private String type = "NONE"; // PLAYER_DEATH, PLAYER_QUIT, ENTITY_DEATH
    private String entityType = ""; // ENDER_DRAGON, etc.
    private String action = "COMPLETE"; // COMPLETE, STOP, PAUSE, RESET

    public TimerEventTrigger() {
    }

    public String getType() { return this.type == null ? "NONE" : this.type; }
    public void setType(String type) { this.type = type; }
    public String getEntityType() { return this.entityType == null ? "" : this.entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getAction() { return this.action == null ? "COMPLETE" : this.action; }
    public void setAction(String action) { this.action = action; }
}
