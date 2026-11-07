package su.nightexpress.sunlight.moduleImpl.timer.model;

import java.util.ArrayList;
import java.util.List;

public class ActionBundle {

    private List<String> consoleCommands = new ArrayList<>();
    private List<String> playerCommands = new ArrayList<>();
    private String sound = ""; // NAME;volume;pitch
    private String particle = ""; // TYPE;count;offsetX;offsetY;offsetZ;speed
    private String firework = ""; // POWER;TYPE;COLOR1,COLOR2
    private double money = 0D;
    private List<String> messages = new ArrayList<>();

    public ActionBundle() {
    }

    public boolean isEmpty() {
        return (this.consoleCommands == null || this.consoleCommands.isEmpty())
                && (this.playerCommands == null || this.playerCommands.isEmpty())
                && (this.sound == null || this.sound.isBlank())
                && (this.particle == null || this.particle.isBlank())
                && (this.firework == null || this.firework.isBlank())
                && this.money == 0D
                && (this.messages == null || this.messages.isEmpty());
    }

    public List<String> getConsoleCommands() {
        if (this.consoleCommands == null) this.consoleCommands = new ArrayList<>();
        return this.consoleCommands;
    }
    public void setConsoleCommands(List<String> consoleCommands) { this.consoleCommands = consoleCommands == null ? new ArrayList<>() : new ArrayList<>(consoleCommands); }
    public List<String> getPlayerCommands() {
        if (this.playerCommands == null) this.playerCommands = new ArrayList<>();
        return this.playerCommands;
    }
    public void setPlayerCommands(List<String> playerCommands) { this.playerCommands = playerCommands == null ? new ArrayList<>() : new ArrayList<>(playerCommands); }
    public String getSound() { return this.sound == null ? "" : this.sound; }
    public void setSound(String sound) { this.sound = sound; }
    public String getParticle() { return this.particle == null ? "" : this.particle; }
    public void setParticle(String particle) { this.particle = particle; }
    public String getFirework() { return this.firework == null ? "" : this.firework; }
    public void setFirework(String firework) { this.firework = firework; }
    public double getMoney() { return this.money; }
    public void setMoney(double money) { this.money = money; }
    public List<String> getMessages() {
        if (this.messages == null) this.messages = new ArrayList<>();
        return this.messages;
    }
    public void setMessages(List<String> messages) { this.messages = messages == null ? new ArrayList<>() : new ArrayList<>(messages); }
}
