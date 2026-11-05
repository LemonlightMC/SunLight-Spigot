package su.nightexpress.sunlight.moduleImpl.settings.model;

import java.util.List;

import org.bukkit.inventory.MenuType;

public class SettingsPanel {

    private final String id;
    private final String title;
    private final String permission;
    private final MenuType menuType;
    private final List<SettingDefinition> settings;
    private final String parentPanelId;

    public SettingsPanel(String id, String title, String permission, MenuType menuType,
                         List<SettingDefinition> settings, String parentPanelId) {
        this.id = id;
        this.title = title;
        this.permission = permission;
        this.menuType = menuType;
        this.settings = List.copyOf(settings);
        this.parentPanelId = parentPanelId;
    }

    public String id() {
        return this.id;
    }

    public String title() {
        return this.title;
    }

    public String permission() {
        return this.permission;
    }

    public MenuType menuType() {
        return this.menuType;
    }

    public List<SettingDefinition> settings() {
        return this.settings;
    }

    public String parentPanelId() {
        return this.parentPanelId;
    }

    public boolean isRoot() {
        return this.parentPanelId == null;
    }
}