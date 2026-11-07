package su.nightexpress.sunlight.moduleImpl.timer.dialog;

import su.nightexpress.nightcore.ui.dialog.wrap.DialogKey;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerScope;

public final class TimerDialogKeys {

    public static final DialogKey<TimerScope> GLOBAL_CREATE = new DialogKey<>("timer_global_create");
    public static final DialogKey<TimerScope> PERSONAL_CREATE = new DialogKey<>("timer_personal_create");
    public static final DialogKey<TimerInstance> TIMER_NAME = new DialogKey<>("timer_name");
    public static final DialogKey<TimerInstance> TIMER_DURATION = new DialogKey<>("timer_duration");
    public static final DialogKey<TimerInstance> TIMER_SCHEDULE = new DialogKey<>("timer_schedule");
    public static final DialogKey<TimerInstance> TIMER_VISIBILITY = new DialogKey<>("timer_visibility");
    public static final DialogKey<TimerInstance> TIMER_ACTION = new DialogKey<>("timer_action");
    public static final DialogKey<TimerInstance> TIMER_RULE = new DialogKey<>("timer_rule");
    public static final DialogKey<TimerInstance> TIMER_TRIGGER = new DialogKey<>("timer_trigger");

    private TimerDialogKeys() {
    }
}
