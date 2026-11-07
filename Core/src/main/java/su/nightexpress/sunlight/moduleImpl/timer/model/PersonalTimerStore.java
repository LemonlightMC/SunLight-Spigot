package su.nightexpress.sunlight.moduleImpl.timer.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class PersonalTimerStore {

    private Map<String, PersonalTimerEntry> timers = new LinkedHashMap<>();

    public PersonalTimerStore() {
    }

    public Map<String, PersonalTimerEntry> getTimers() {
        if (this.timers == null) this.timers = new LinkedHashMap<>();
        return this.timers;
    }

    public void setTimers(Map<String, PersonalTimerEntry> timers) {
        this.timers = timers;
    }
}
