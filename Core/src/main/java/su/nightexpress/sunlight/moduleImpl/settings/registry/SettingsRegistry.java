package su.nightexpress.sunlight.moduleImpl.settings.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import su.nightexpress.sunlight.moduleImpl.settings.model.SettingsPanel;

public class SettingsRegistry {

    private final Map<String, SettingsPanel> panels = new LinkedHashMap<>();
    private String rootPanelId;

    public void register(SettingsPanel panel) {
        this.panels.put(panel.id(), panel);
        if (panel.isRoot()) {
            this.rootPanelId = panel.id();
        }
    }

    public SettingsPanel get(String id) {
        return this.panels.get(id);
    }

    public SettingsPanel root() {
        return this.rootPanelId == null ? null : this.panels.get(this.rootPanelId);
    }

    public Collection<SettingsPanel> panels() {
        return this.panels.values();
    }

    public void clear() {
        this.panels.clear();
        this.rootPanelId = null;
    }
}