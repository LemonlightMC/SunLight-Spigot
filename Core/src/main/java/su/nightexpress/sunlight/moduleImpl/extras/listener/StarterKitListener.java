package su.nightexpress.sunlight.moduleImpl.extras.listener;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasModule;
import su.nightexpress.sunlight.moduleImpl.extras.StarterKitSettings;
import su.nightexpress.sunlight.moduleImpl.spawns.Spawn;
import su.nightexpress.sunlight.moduleImpl.spawns.SpawnsModule;
import su.nightexpress.sunlight.teleport.TeleportContext;
import su.nightexpress.sunlight.teleport.TeleportFlag;
import su.nightexpress.sunlight.teleport.TeleportType;

import java.util.List;
import java.util.Map;

public class StarterKitListener extends su.nightexpress.nightcore.manager.AbstractListener<SunLightPlugin> {

    private final ExtrasModule module;
    private final StarterKitSettings settings;

    public StarterKitListener(final SunLightPlugin plugin, final ExtrasModule module) {
        super(plugin);
        this.module = module;
        this.settings = module.getStarterKitSettings();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!this.settings.isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final SunLightPlugin plugin = this.plugin;

        // Check if this is first join only
        if (this.settings.isFirstJoinOnly() && !plugin.userManager().getOrFetch(player).isFirstTimeJoined()) {
            return;
        }

        // Run all starter kit logic asynchronously to avoid lag
        Bukkit.getScheduler().runTask(plugin, () -> {
            this.giveStarterKit(player);
        });
    }

    private void giveStarterKit(final Player player) {
        final SunLightPlugin plugin = this.plugin;

        // Clear inventory if configured
        if (this.settings.isClearInventory()) {
            player.getInventory().clear();
        }

        // Give starter items
        List<ItemStack> starterItems = this.settings.getStarterItems();
        if (starterItems != null && !starterItems.isEmpty()) {
            for (ItemStack item : starterItems) {
                if (item != null && !item.getType().isAir()) {
                    player.getInventory().addItem(new ItemStack(item));
                }
            }
        }

        // Give potion effects
        if (this.settings.isGiveEffects()) {
            Map<String, Integer> starterEffects = this.settings.getStarterEffects();
            if (starterEffects != null && !starterEffects.isEmpty()) {
                for (Map.Entry<String, Integer> entry : starterEffects.entrySet()) {
                    try {
                        PotionEffectType effectType = PotionEffectType.getByName(entry.getKey().toUpperCase());
                        if (effectType != null) {
                            int amplifier = Math.max(0, entry.getValue() - 1);
                            player.addPotionEffect(
                                    new PotionEffect(effectType, Integer.MAX_VALUE, amplifier, false, false, false));
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Spawn particles
        if (this.settings.isGiveParticles()) {
            Map<String, Integer> starterParticles = this.settings.getStarterParticles();
            if (starterParticles != null && !starterParticles.isEmpty()) {
                Location location = player.getLocation().clone().add(0, 1, 0);
                for (Map.Entry<String, Integer> entry : starterParticles.entrySet()) {
                    try {
                        Particle particle = Particle.valueOf(entry.getKey().toUpperCase());
                        int count = Math.max(1, entry.getValue());
                        player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Launch fireworks
        if (this.settings.isGiveFireworks()) {
            Map<String, Integer> starterFireworks = this.settings.getStarterFireworks();
            if (starterFireworks != null && !starterFireworks.isEmpty()) {
                Location location = player.getLocation().clone().add(0, 2, 0);
                for (Map.Entry<String, Integer> entry : starterFireworks.entrySet()) {
                    try {
                        Firework firework = player.getWorld().spawn(location, Firework.class);
                        FireworkMeta meta = firework.getFireworkMeta();
                        if (meta != null) {
                            FireworkEffect.Builder builder = FireworkEffect.builder();
                            String effectName = entry.getKey().toUpperCase();
                            if ("STAR".equals(effectName)) {
                                builder.with(FireworkEffect.Type.STAR);
                            } else if ("BALL".equals(effectName)) {
                                builder.with(FireworkEffect.Type.BALL);
                            } else if ("BALL_LARGE".equals(effectName)) {
                                builder.with(FireworkEffect.Type.BALL_LARGE);
                            } else if ("CREEPER".equals(effectName)) {
                                builder.with(FireworkEffect.Type.CREEPER);
                            } else if ("BURST".equals(effectName)) {
                                builder.with(FireworkEffect.Type.BURST);
                            }
                            builder.withColor(org.bukkit.Color.RED, org.bukkit.Color.BLUE, org.bukkit.Color.GREEN)
                                    .withFade(org.bukkit.Color.WHITE)
                                    .flicker(true)
                                    .trail(true);
                            meta.addEffect(builder.build());
                            meta.setPower(Math.max(0, entry.getValue() - 1));
                            firework.setFireworkMeta(meta);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Play sounds
        if (this.settings.isGiveSounds()) {
            Map<String, Integer> starterSounds = this.settings.getStarterSounds();
            if (starterSounds != null && !starterSounds.isEmpty()) {
                for (Map.Entry<String, Integer> entry : starterSounds.entrySet()) {
                    try {
                        Sound sound = Sound.valueOf(entry.getKey().toUpperCase());
                        float volume = Math.max(0.1f, Math.min(2.0f, entry.getValue()));
                        player.playSound(player.getLocation(), sound, volume, 1.0f);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Send welcome message
        if (this.settings.isWelcomeMessage()) {
            List<String> welcomeFormat = this.settings.getWelcomeMessageFormat();
            if (welcomeFormat != null && !welcomeFormat.isEmpty()) {
                String message = String.join("\n", welcomeFormat);
                NightComponent component = NightMessage.parse(message);
                Players.sendMessage(player, component);
            }
        }

        // Broadcast global announcement
        if (this.settings.isGlobalAnnouncement()) {
            List<String> globalFormat = this.settings.getGlobalAnnouncementFormat();
            if (globalFormat != null && !globalFormat.isEmpty()) {
                String message = String.join("\n", globalFormat);
                NightComponent component = NightMessage.parse(message);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    Players.sendMessage(onlinePlayer, component);
                }
            }
        }

        // Teleport to spawn if configured
        if (this.settings.isTeleportToSpawn()) {
            SpawnsModule spawnsModule = plugin.moduleManager().getByType(SpawnsModule.class).orElse(null);
            if (spawnsModule != null) {
                Spawn spawn = spawnsModule.getNewbieSpawn(player);
                if (spawn != null && spawn.isActive()) {
                    TeleportContext context = TeleportContext.builder(this.module, player, spawn.getLocation())
                            .withFlag(TeleportFlag.LOOK_FOR_SURFACE)
                            .withFlag(TeleportFlag.AVOID_LAVA)
                            .withFlag(TeleportFlag.CENTERED)
                            .callback(() -> {

                            })
                            .build();

                    this.module.teleportManager().teleport(context, TeleportType.SPAWN);
                }
            }
        }
    }
}