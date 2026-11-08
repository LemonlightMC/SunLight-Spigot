package su.nightexpress.sunlight.moduleImpl.timer.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DisplayRule {

    private String id = "rule";
    private int priority = 0;
    private RuleConditionType condition = RuleConditionType.ALWAYS;
    private long value = 0L;
    private long min = 0L;
    private long max = 0L;
    private Set<DisplayChannel> channels = new LinkedHashSet<>();
    private String actionBar = "";
    private String bossBar = "";
    private String title = "";
    private String subtitle = "";
    private String chat = "";
    private String scoreboardTitle = "";
    private List<String> scoreboardLines = new ArrayList<>();
    private int chatIntervalSeconds = 1;
    private int updateIntervalTicks = 20;
    private String bossBarColor = "YELLOW";
    private String bossBarOverlay = "PROGRESS";
    private String viewPermission = ""; // optional per-rule audience override (empty = use timer visibility)

    public DisplayRule() {
    }

    public boolean matches(TimerInstance instance) {
        if (instance == null) return false;
        if (this.condition == null || this.condition == RuleConditionType.ALWAYS) return true;
        if (this.condition == RuleConditionType.RUNNING) return instance.getState() == TimerState.RUNNING;
        if (this.condition == RuleConditionType.PAUSED) return instance.getState() == TimerState.PAUSED;

        long remainingSec = Math.max(0L, instance.getRemainingMillis() / 1000L);
        long elapsedSec = Math.max(0L, instance.getElapsedMillis() / 1000L);
        return switch (this.condition) {
            case REMAINING_LTE -> remainingSec <= this.value;
            case REMAINING_GT -> remainingSec > this.value;
            case REMAINING_BETWEEN -> remainingSec >= this.min && remainingSec <= this.max;
            case ELAPSED_GTE -> elapsedSec >= this.value;
            case ELAPSED_BETWEEN -> elapsedSec >= this.min && elapsedSec <= this.max;
            default -> true;
        };
    }

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }
    public int getPriority() { return this.priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public RuleConditionType getCondition() { return this.condition; }
    public void setCondition(RuleConditionType condition) { this.condition = condition; }
    public long getValue() { return this.value; }
    public void setValue(long value) { this.value = value; }
    public long getMin() { return this.min; }
    public void setMin(long min) { this.min = min; }
    public long getMax() { return this.max; }
    public void setMax(long max) { this.max = max; }
    public Set<DisplayChannel> getChannels() {
        if (this.channels == null) this.channels = new LinkedHashSet<>();
        return this.channels;
    }
    public void setChannels(Set<DisplayChannel> channels) { this.channels = channels == null ? new LinkedHashSet<>() : new LinkedHashSet<>(channels); }
    public String getActionBar() { return this.actionBar == null ? "" : this.actionBar; }
    public void setActionBar(String actionBar) { this.actionBar = actionBar; }
    public String getBossBar() { return this.bossBar == null ? "" : this.bossBar; }
    public void setBossBar(String bossBar) { this.bossBar = bossBar; }
    public String getTitle() { return this.title == null ? "" : this.title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return this.subtitle == null ? "" : this.subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getChat() { return this.chat == null ? "" : this.chat; }
    public void setChat(String chat) { this.chat = chat; }
    public String getScoreboardTitle() { return this.scoreboardTitle == null ? "" : this.scoreboardTitle; }
    public void setScoreboardTitle(String scoreboardTitle) { this.scoreboardTitle = scoreboardTitle; }
    public List<String> getScoreboardLines() { if (this.scoreboardLines == null) this.scoreboardLines = new ArrayList<>(); return this.scoreboardLines; }
    public void setScoreboardLines(List<String> scoreboardLines) { this.scoreboardLines = scoreboardLines == null ? new ArrayList<>() : new ArrayList<>(scoreboardLines); }
    public int getChatIntervalSeconds() { return Math.max(1, this.chatIntervalSeconds); }
    public void setChatIntervalSeconds(int chatIntervalSeconds) { this.chatIntervalSeconds = chatIntervalSeconds; }
    public int getUpdateIntervalTicks() { return Math.max(1, this.updateIntervalTicks); }
    public void setUpdateIntervalTicks(int updateIntervalTicks) { this.updateIntervalTicks = updateIntervalTicks; }
    public String getBossBarColor() { return this.bossBarColor == null ? "YELLOW" : this.bossBarColor; }
    public void setBossBarColor(String bossBarColor) { this.bossBarColor = bossBarColor; }
    public String getBossBarOverlay() { return this.bossBarOverlay == null ? "PROGRESS" : this.bossBarOverlay; }
    public void setBossBarOverlay(String bossBarOverlay) { this.bossBarOverlay = bossBarOverlay; }
    public String getViewPermission() { return this.viewPermission == null ? "" : this.viewPermission; }
    public void setViewPermission(String viewPermission) { this.viewPermission = viewPermission == null ? "" : viewPermission; }
}
