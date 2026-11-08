package su.nightexpress.sunlight.moduleImpl.ptp.request;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import su.nightexpress.sunlight.utils.Utils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TeleportRequest {

    private final UUID targetId;
    private final UUID senderId;
    private final String senderName;
    private final String targetName;
    private final TeleportMode mode;

    private long expireDate;

    public TeleportRequest(Player sender, Player target, TeleportMode mode, int timeOut) {
        this(sender.getUniqueId(), sender.getName(), target.getUniqueId(), target.getName(), mode, timeOut);
    }

    public TeleportRequest(UUID senderId, UUID targetId, TeleportMode mode, int timeOut) {
        this(senderId, null, targetId, null, mode, timeOut);
    }

    public TeleportRequest(UUID senderId, String senderName, UUID targetId, String targetName, TeleportMode mode, int timeOut) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.mode = mode;
        this.expireDate = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeOut, TimeUnit.SECONDS);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= this.expireDate;
    }

    public void setExpired() {
        this.expireDate = System.currentTimeMillis();
    }

    public boolean isSender(String name) {
        if (name == null) return false;
        Player online = Utils.getPlayer(name);
        if (online != null) return online.getUniqueId().equals(this.senderId);
        if (this.senderName != null) return this.senderName.equalsIgnoreCase(name.trim());
        Player sender = this.getSender();
        return sender != null && sender.getName().equalsIgnoreCase(name.trim());
    }

    public boolean isTarget(String name) {
        if (name == null) return false;
        Player online = Utils.getPlayer(name);
        if (online != null) return online.getUniqueId().equals(this.targetId);
        if (this.targetName != null) return this.targetName.equalsIgnoreCase(name.trim());
        Player target = this.getTarget();
        return target != null && target.getName().equalsIgnoreCase(name.trim());
    }

    public String getSenderName() {
        if (this.senderName != null) return this.senderName;
        Player sender = this.getSender();
        return sender == null ? "" : sender.getName();
    }

    public String getTargetName() {
        if (this.targetName != null) return this.targetName;
        Player target = this.getTarget();
        return target == null ? "" : target.getName();
    }

    public Player getSender() {
        return Bukkit.getPlayer(this.senderId);
    }

    public Player getTarget() {
        return Bukkit.getPlayer(this.targetId);
    }

    public UUID getSenderId() {
        return this.senderId;
    }

    public UUID getTargetId() {
        return this.targetId;
    }

    public TeleportMode getMode() {
        return this.mode;
    }

    public long getExpireDate() {
        return this.expireDate;
    }
}
