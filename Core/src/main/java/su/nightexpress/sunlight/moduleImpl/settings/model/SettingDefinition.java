package su.nightexpress.sunlight.moduleImpl.settings.model;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import su.nightexpress.sunlight.user.property.UserProperty;

public class SettingDefinition {

    private final String id;
    private final String permission;
    private final SettingKind kind;
    private final UserProperty<?> property;
    private final List<String> values;
    private final String targetPanel;
    private final Consumer<Player> action;
    private final Material icon;
    private final String displayName;
    private final String description;

    private SettingDefinition(Builder builder) {
        this.id = builder.id;
        this.permission = builder.permission;
        this.kind = builder.kind;
        this.property = builder.property;
        this.values = List.copyOf(builder.values);
        this.targetPanel = builder.targetPanel;
        this.action = builder.action;
        this.icon = builder.icon;
        this.displayName = builder.displayName;
        this.description = builder.description;
    }

    public static Builder builder(String id, SettingKind kind) {
        return new Builder(id, kind);
    }

    public String id() {
        return this.id;
    }

    public String permission() {
        return this.permission;
    }

    public String valuePermission(String value) {
        return this.permission == null ? null : this.permission + "." + value;
    }

    public SettingKind kind() {
        return this.kind;
    }

    public UserProperty<?> property() {
        return this.property;
    }

    public List<String> values() {
        return this.values;
    }

    public String targetPanel() {
        return this.targetPanel;
    }

    public Consumer<Player> action() {
        return this.action;
    }

    public Material icon() {
        return this.icon;
    }

    public String displayName() {
        return this.displayName;
    }

    public String description() {
        return this.description;
    }

    public static class Builder {
        private final String id;
        private final SettingKind kind;
        private String permission;
        private UserProperty<?> property;
        private List<String> values = List.of();
        private String targetPanel;
        private Consumer<Player> action;
        private Material icon = Material.PAPER;
        private String displayName;
        private String description = "";

        private Builder(String id, SettingKind kind) {
            this.id = id;
            this.kind = kind;
            this.displayName = id;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder property(UserProperty<?> property) {
            this.property = property;
            return this;
        }

        public Builder values(String... values) {
            this.values = List.of(values);
            return this;
        }

        public Builder values(List<String> values) {
            this.values = values;
            return this;
        }

        public Builder targetPanel(String targetPanel) {
            this.targetPanel = targetPanel;
            return this;
        }

        public Builder action(Consumer<Player> action) {
            this.action = action;
            return this;
        }

        public Builder icon(Material icon) {
            this.icon = icon;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public SettingDefinition build() {
            return new SettingDefinition(this);
        }
    }
}