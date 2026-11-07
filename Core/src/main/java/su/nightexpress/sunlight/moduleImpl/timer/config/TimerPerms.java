package su.nightexpress.sunlight.moduleImpl.timer.config;

public final class TimerPerms {

    public static final String PREFIX = "sunlight.timer.";
    public static final String COMMAND_PREFIX = PREFIX + "command.";

    public static final String COMMAND_TIMER = COMMAND_PREFIX + "timer";
    public static final String COMMAND_PTIMER = COMMAND_PREFIX + "ptimer";

    public static final String TIMER_LIST = COMMAND_PREFIX + "timer.list";
    public static final String TIMER_INFO = COMMAND_PREFIX + "timer.info";
    public static final String TIMER_START = COMMAND_PREFIX + "timer.start";
    public static final String TIMER_PAUSE = COMMAND_PREFIX + "timer.pause";
    public static final String TIMER_RESUME = COMMAND_PREFIX + "timer.resume";
    public static final String TIMER_RESET = COMMAND_PREFIX + "timer.reset";
    public static final String TIMER_STOP = COMMAND_PREFIX + "timer.stop";
    public static final String TIMER_DELETE = COMMAND_PREFIX + "timer.delete";
    public static final String TIMER_CREATE = COMMAND_PREFIX + "timer.create";
    public static final String TIMER_RELOAD = COMMAND_PREFIX + "timer.reload";
    public static final String TIMER_EDITOR = COMMAND_PREFIX + "timer.editor";
    public static final String TIMER_OTHERS = COMMAND_PREFIX + "timer.others";
    public static final String TIMER_BYPASS_VISIBILITY = PREFIX + "bypass.visibility";

    public static final String PTIMER_START = COMMAND_PREFIX + "ptimer.start";
    public static final String PTIMER_PAUSE = COMMAND_PREFIX + "ptimer.pause";
    public static final String PTIMER_RESUME = COMMAND_PREFIX + "ptimer.resume";
    public static final String PTIMER_RESET = COMMAND_PREFIX + "ptimer.reset";
    public static final String PTIMER_STOP = COMMAND_PREFIX + "ptimer.stop";
    public static final String PTIMER_DELETE = COMMAND_PREFIX + "ptimer.delete";
    public static final String PTIMER_LIST = COMMAND_PREFIX + "ptimer.list";
    public static final String PTIMER_INFO = COMMAND_PREFIX + "ptimer.info";
    public static final String PTIMER_CREATE = COMMAND_PREFIX + "ptimer.create";
    public static final String PTIMER_EDITOR = COMMAND_PREFIX + "ptimer.editor";

    private TimerPerms() {
    }
}
