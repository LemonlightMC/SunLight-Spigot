package su.nightexpress.sunlight.moduleImpl.afk;

import su.nightexpress.sunlight.moduleImpl.afk.config.AfkSettings;
import su.nightexpress.sunlight.utils.TimeUtil;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;

public class ActivityTracker {

    private final AfkSettings settings;

    private BlockPos lastPos;
    private double lastX;
    private double lastY;
    private double lastZ;
    private float lastYaw;
    private float lastPitch;

    private int idleTime;
    private int idleThreshold;

    private long afkEnterTimestamp;

    private int wakeUpThreshold;
    private long wakeUpEndTimestamp;

    private long lastKickWarningSent;

    public ActivityTracker(AfkSettings settings) {
        this.settings = settings;
        this.resetCounters();
    }

    public void resetCounters() {
        this.resetIdleCounter();
        this.resetIdleThreshold();
        this.resetWakeUpCounter();
        this.resetAfkTimestamp();
        this.lastKickWarningSent = 0L;
    }

    public void tick() {
        if (this.isAfk()) {
            if (this.isWakingUp() && this.isWakeUpTimeout()) {
                this.resetWakeUpCounter();
            }
        }

        if (!this.isAfk() && this.idleThreshold > 0) {
            this.idleThreshold--;
            return;
        }

        this.countIdleTime();
    }

    public void updatePosition(BlockPos newPos, double x, double y, double z, float yaw, float pitch) {
        this.updatePosition(newPos, x, y, z, yaw, pitch, true, this.settings.movementTrackRotation.get());
    }

    public void updatePosition(BlockPos newPos, double x, double y, double z, float yaw, float pitch,
            boolean allowMovement, boolean allowRotation) {
        if (this.lastPos == null) {
            this.lastPos = newPos;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            return;
        }

        final boolean blockChanged = !newPos.equals(this.lastPos);
        final double dx = x - this.lastX;
        final double dy = y - this.lastY;
        final double dz = z - this.lastZ;
        final double moveDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        final float yawDelta = Math.abs(this.lastYaw - yaw);
        final float pitchDelta = Math.abs(this.lastPitch - pitch);
        final boolean rotationChanged = yawDelta > 0F || pitchDelta > 0F;
        final boolean antiAfk = this.settings.bypassDetectionEnabled.get() && this.settings.bypassIgnoreMicroMovement.get();

        this.lastPos = newPos;
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastYaw = yaw;
        this.lastPitch = pitch;

        if (blockChanged) {
            if (allowMovement) {
                if (antiAfk) {
                    final double microMove = Math.max(0D, this.settings.bypassMicroMoveDistance.get());
                    if (microMove > 0D && moveDistance < microMove) {
                        return;
                    }
                }
                this.countActivity(ActivityType.MOVEMENT);
            }
            return;
        }

        if (rotationChanged && allowRotation) {
            if (antiAfk) {
                final float micro = (float) Math.max(0D, this.settings.bypassMicroRotation.get());
                if (micro > 0F && yawDelta <= micro && pitchDelta <= micro) {
                    return;
                }
            }
            this.countActivity(ActivityType.ROTATION);
        }
    }

    public void countActivity(ActivityType type) {
        this.countActivity(this.settings.getActivityPoints(type));
    }

    public void countActivity(int amount) {
        if (amount <= 0)
            return;

        if (!this.isAfk()) {
            this.resetIdleCounter();
            this.resetIdleThreshold();
            return;
        }

        if (this.isWakingUp() && this.isWakeUpTimeout()) {
            this.resetWakeUpCounter();
        }

        this.wakeUpEndTimestamp = TimeUtil.createFutureTimestamp(this.settings.wakeUpTimer.get());
        this.wakeUpThreshold -= amount;
    }

    public void countIdleTime() {
        this.idleTime++;
    }

    private void resetWakeUpCounter() {
        this.wakeUpThreshold = this.settings.wakeUpThreshold.get();
        this.wakeUpEndTimestamp = 0L;
    }

    private void resetIdleCounter() {
        this.idleTime = 0;
    }

    private void resetIdleThreshold() {
        this.idleThreshold = this.settings.idleThreshold.get();
    }

    public void setAfkTimestamp() {
        this.afkEnterTimestamp = System.currentTimeMillis();
    }

    public void resetAfkTimestamp() {
        this.afkEnterTimestamp = 0L;
    }

    public boolean isAfk() {
        return this.afkEnterTimestamp > 0L;
    }

    public boolean isWakingUp() {
        return this.wakeUpEndTimestamp > 0L;
    }

    public boolean isWakeUpTimeout() {
        return TimeUtil.isPassed(this.wakeUpEndTimestamp);
    }

    public boolean isEnoughActivity() {
        return this.wakeUpThreshold <= 0;
    }

    public int getWakeUpProgress() {
        return this.wakeUpThreshold;
    }

    public long getWakeUpEndTimestamp() {
        return this.wakeUpEndTimestamp;
    }

    public int getWakeUpTimeLeft() {
        if (this.wakeUpEndTimestamp <= 0L)
            return 0;

        long left = this.wakeUpEndTimestamp - System.currentTimeMillis();
        return (int) Math.max(0D, Math.ceil(left / 1000D));
    }

    public long getLastKickWarningSent() {
        return this.lastKickWarningSent;
    }

    public void setLastKickWarningSent(long timestamp) {
        this.lastKickWarningSent = timestamp;
    }

    public long getAfkEnterTimestamp() {
        return this.afkEnterTimestamp;
    }

    public int getIdleTime() {
        return this.idleTime;
    }

    public long duration() {
        if (this.afkEnterTimestamp <= 0L) {
            return 0;
        }
        return System.currentTimeMillis() - this.afkEnterTimestamp;
    }
}