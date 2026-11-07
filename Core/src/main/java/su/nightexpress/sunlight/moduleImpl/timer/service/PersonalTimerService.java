package su.nightexpress.sunlight.moduleImpl.timer.service;

import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import su.nightexpress.sunlight.moduleImpl.timer.TimerDefaults;
import su.nightexpress.sunlight.moduleImpl.timer.model.*;
import su.nightexpress.sunlight.user.SunUser;
import su.nightexpress.sunlight.user.UserManager;
import su.nightexpress.sunlight.user.property.UserProperty;

import java.lang.reflect.Type;
import java.util.*;

public class PersonalTimerService {

    public static final Type STORE_TYPE = new TypeToken<PersonalTimerStore>() {}.getType();
    public static final UserProperty<PersonalTimerStore> PERSONAL_TIMERS = UserProperty.create(
            "timer_personal_store",
            STORE_TYPE,
            PersonalTimerStore.class,
            new PersonalTimerStore(),
            true
    );

    private final UserManager userManager;
    private final Map<UUID, Map<String, TimerInstance>> runtime = new HashMap<>();

    public PersonalTimerService(UserManager userManager) {
        this.userManager = userManager;
    }

    public Map<String, TimerInstance> getOrLoad(Player player) {
        return this.runtime.computeIfAbsent(player.getUniqueId(), id -> this.loadFromUser(player));
    }

    public Map<String, TimerInstance> getOrLoad(UUID playerId) {
        return this.runtime.computeIfAbsent(playerId, this::loadFromUser);
    }

    private Map<String, TimerInstance> loadFromUser(Player player) {
        SunUser user = this.userManager.getOrFetch(player);
        return this.loadFromStore(user);
    }

    private Map<String, TimerInstance> loadFromUser(UUID playerId) {
        SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
        if (user == null) return new LinkedHashMap<>();
        return this.loadFromStore(user);
    }

    private Map<String, TimerInstance> loadFromStore(SunUser user) {
        Map<String, TimerInstance> map = new LinkedHashMap<>();
        PersonalTimerStore store = this.getStore(user);
        if (store == null || store.getTimers() == null) return map;
        for (Map.Entry<String, PersonalTimerEntry> entry : store.getTimers().entrySet()) {
            PersonalTimerEntry data = entry.getValue();
            if (data == null || data.getDefinition() == null) continue;
            TimerDefinition def = data.getDefinition();
            def.setId(GlobalTimerRepository.normalizeId(entry.getKey()));
            def.setScope(TimerScope.PERSONAL);
            TimerInstance instance = new TimerInstance(def, user.getId());
            instance.setState(data.getState());
            instance.setElapsedMillis(data.getElapsedMillis());
            instance.setRemainingMillis(data.getRemainingMillis() > 0 ? data.getRemainingMillis() : instance.getRemainingMillis());
            instance.setScheduleFired(data.isScheduleFired());
            instance.setScheduledStartEpoch(data.getScheduledStartEpoch());
            map.put(def.getId(), instance);
        }
        return map;
    }

    public void save(Player player) {
        SunUser user = this.userManager.getOrFetch(player);
        this.save(user, player.getUniqueId());
    }

    public void save(UUID playerId) {
        SunUser user = this.userManager.getOrFetch(playerId).orElse(null);
        if (user != null) this.save(user, playerId);
    }

    private void save(SunUser user, UUID playerId) {
        Map<String, TimerInstance> timers = this.runtime.get(playerId);
        if (timers == null) return;
        PersonalTimerStore store = new PersonalTimerStore();
        Map<String, PersonalTimerEntry> entries = new LinkedHashMap<>();
        for (TimerInstance instance : timers.values()) {
            PersonalTimerEntry entry = new PersonalTimerEntry();
            entry.setDefinition(instance.getDefinition());
            entry.setState(instance.getState());
            entry.setElapsedMillis(instance.getElapsedMillis());
            entry.setRemainingMillis(instance.getRemainingMillis());
            entry.setScheduleFired(instance.isScheduleFired());
            entry.setScheduledStartEpoch(instance.getScheduledStartEpoch());
            entries.put(instance.getId(), entry);
        }
        store.setTimers(entries);
        user.setProperty(PERSONAL_TIMERS, store);
        user.markDirty();
    }

    public PersonalTimerStore getStore(SunUser user) {
        PersonalTimerStore store = user.getPropertyOrDefault(PERSONAL_TIMERS);
        if (store == null) {
            store = new PersonalTimerStore();
        }
        return store;
    }

    public Optional<TimerInstance> get(Player player, String id) {
        return Optional.ofNullable(this.getOrLoad(player).get(GlobalTimerRepository.normalizeId(id)));
    }

    public Collection<TimerInstance> all(Player player) {
        return this.getOrLoad(player).values();
    }

    public TimerInstance create(Player player, TimerKind kind, String id, long seconds) {
        String normalized = GlobalTimerRepository.normalizeId(id);
        Map<String, TimerInstance> timers = this.getOrLoad(player);
        TimerDefinition def = TimerDefaults.createPersonalDefault(normalized, kind, seconds);
        TimerInstance instance = new TimerInstance(def, player.getUniqueId());
        timers.put(normalized, instance);
        this.save(player);
        return instance;
    }

    public TimerInstance put(Player player, TimerInstance instance) {
        this.getOrLoad(player).put(instance.getId(), instance);
        this.save(player);
        return instance;
    }

    public boolean delete(Player player, String id) {
        String normalized = GlobalTimerRepository.normalizeId(id);
        boolean removed = this.getOrLoad(player).remove(normalized) != null;
        if (removed) this.save(player);
        return removed;
    }

    public void unload(UUID playerId) {
        this.save(playerId);
        this.runtime.remove(playerId);
    }

    public void saveAll() {
        for (UUID playerId : new ArrayList<>(this.runtime.keySet())) {
            this.save(playerId);
        }
    }

    public Collection<Map.Entry<UUID, Map<String, TimerInstance>>> runtimeEntries() {
        return this.runtime.entrySet();
    }
}
