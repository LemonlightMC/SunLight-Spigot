package su.nightexpress.sunlight.moduleImpl.timer.service;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.FileUtil;
import su.nightexpress.nightcore.util.Strings;
import su.nightexpress.sunlight.moduleImpl.timer.TimerDefaults;
import su.nightexpress.sunlight.moduleImpl.timer.config.TimerFiles;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerDefinition;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerInstance;
import su.nightexpress.sunlight.moduleImpl.timer.model.TimerScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class GlobalTimerRepository {

    private final Path directory;
    private final Map<String, TimerInstance> timers = new LinkedHashMap<>();

    public GlobalTimerRepository(Path modulePath) {
        this.directory = modulePath.resolve(TimerFiles.DIR_TIMERS);
    }

    public void loadAll() {
        this.timers.clear();
        try {
            if (!Files.exists(this.directory)) {
                Files.createDirectories(this.directory);
                for (TimerDefinition def : TimerDefaults.createDefaults()) {
                    this.saveDefinition(def, null);
                }
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        for (var file : FileUtil.findYamlFiles(this.directory.toString())) {
            String name = FileUtil.getNameWithoutExtension(file);
            String id = Strings.varStyle(name).orElse(name).toLowerCase(Locale.ROOT);
            FileConfig config = FileConfig.load(file);
            TimerDefinition definition = TimerYamlIO.readDefinition(config, "Timer", id);
            definition.setId(id);
            definition.setScope(TimerScope.GLOBAL);
            TimerInstance instance = new TimerInstance(definition, null);
            TimerYamlIO.readRuntime(config, "Runtime", instance);
            this.timers.put(id, instance);
        }
    }

    public void save(TimerInstance instance) {
        this.saveDefinition(instance.getDefinition(), instance);
    }

    public void saveDefinition(TimerDefinition definition, TimerInstance instance) {
        try {
            Files.createDirectories(this.directory);
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return;
        }
        String id = normalizeId(definition.getId());
        definition.setId(id);
        Path file = this.directory.resolve(FileConfig.withExtension(id));
        FileConfig config = FileConfig.load(file);
        TimerYamlIO.writeDefinition(config, "Timer", definition);
        if (instance != null) {
            TimerYamlIO.writeRuntime(config, "Runtime", instance);
        }
        config.saveChanges();
    }

    public boolean delete(String id) {
        String key = normalizeId(id);
        TimerInstance removed = this.timers.remove(key);
        Path file = this.directory.resolve(FileConfig.withExtension(key));
        try {
            Files.deleteIfExists(file);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return removed != null;
    }

    public Optional<TimerInstance> get(String id) {
        return Optional.ofNullable(this.timers.get(normalizeId(id)));
    }

    public TimerInstance put(TimerInstance instance) {
        this.timers.put(normalizeId(instance.getId()), instance);
        return instance;
    }

    public boolean contains(String id) {
        return this.timers.containsKey(normalizeId(id));
    }

    public Collection<TimerInstance> all() {
        return this.timers.values();
    }

    public static String normalizeId(String raw) {
        if (raw == null) return "timer";
        String id = Strings.varStyle(raw).orElse(raw).trim().toLowerCase(Locale.ROOT);
        return id.isBlank() ? "timer" : id;
    }
}
