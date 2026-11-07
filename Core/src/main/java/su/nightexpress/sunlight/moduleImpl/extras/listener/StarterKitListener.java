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
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.sunlight.SunLightPlugin;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasModule;
import su.nightexpress.sunlight.moduleImpl.extras.ExtrasPerms;
import su.nightexpress.sunlight.moduleImpl.extras.StarterKitEffect;
import su.nightexpress.sunlight.moduleImpl.extras.StarterKitSettings;
import su.nightexpress.sunlight.moduleImpl.extras.StarterKitSoundAudience;
import su.nightexpress.sunlight.moduleImpl.spawns.Spawn;
import su.nightexpress.sunlight.moduleImpl.spawns.SpawnsModule;
import su.nightexpress.sunlight.teleport.TeleportContext;
import su.nightexpress.sunlight.teleport.TeleportFlag;
import su.nightexpress.sunlight.teleport.TeleportType;

import java.util.List;
import java.util.Locale;
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

        if (player.hasPermission(ExtrasPerms.STARTER_KIT_BYPASS)) {
            return;
        }

        if (this.settings.isRequirePermission() && !player.hasPermission(ExtrasPerms.STARTER_KIT)) {
            return;
        }

        if (this.settings.isFirstJoinOnly() && !this.plugin.userManager().getOrFetch(player).isFirstTimeJoined()) {
            return;
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (!player.isOnline())
                return;
            this.giveStarterKit(player);
        });
    }

    private void giveStarterKit(final Player player) {
        if (this.settings.isClearInventory()) {
            player.getInventory().clear();
        }

        final List<ItemStack> starterItems = this.settings.getStarterItems();
        if (starterItems != null && !starterItems.isEmpty()) {
            for (final ItemStack item : starterItems) {
                if (item != null && !item.getType().isAir()) {
                    player.getInventory().addItem(new ItemStack(item));
                }
            }
        }

        if (this.settings.isGiveXP()) {
            final int amount = this.settings.getXPAmount();
            final int levels = this.settings.getXPLevels();
            if (amount > 0) {
                player.giveExp(amount);
            }
            if (levels > 0) {
                player.giveExpLevels(levels);
            }
        }

        if (this.settings.isGiveEffects()) {
            final List<StarterKitEffect> starterEffects = this.settings.getStarterEffects();
            if (starterEffects != null && !starterEffects.isEmpty()) {
                for (final StarterKitEffect entry : starterEffects) {
                    if (entry == null)
                        continue;
                    try {
                        final PotionEffect effect = entry.createEffect();
                        if (effect != null) {
                            player.addPotionEffect(effect);
                        }
                    } catch (final Exception ignored) {
                    }
                }
            }
        }

        if (this.settings.isGiveParticles()) {
            final Map<String, Integer> starterParticles = this.settings.getStarterParticles();
            if (starterParticles != null && !starterParticles.isEmpty()) {
                final Location location = player.getLocation().clone().add(0, 1, 0);
                for (final Map.Entry<String, Integer> entry : starterParticles.entrySet()) {
                    try {
                        final Particle particle = Particle.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
                        final int count = Math.max(1, entry.getValue());
                        player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
                    } catch (final Exception ignored) {
                    }
                }
            }
        }

        if (this.settings.isGiveFireworks()) {
            final Map<String, Integer> starterFireworks = this.settings.getStarterFireworks();
            if (starterFireworks != null && !starterFireworks.isEmpty()) {
                final Location location = player.getLocation().clone().add(0, 2, 0);
                for (final Map.Entry<String, Integer> entry : starterFireworks.entrySet()) {
                    try {
                        final Firework firework = player.getWorld().spawn(location, Firework.class);
                        final FireworkMeta meta = firework.getFireworkMeta();
                        if (meta != null) {
                            final FireworkEffect.Builder builder = FireworkEffect.builder();
                            final String effectName = entry.getKey().toUpperCase(Locale.ROOT);
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
                    } catch (final Exception ignored) {
                    }
                }
            }
        }

        if (this.settings.isGiveSounds()) {
            final Map<String, Integer> starterSounds = this.settings.getStarterSounds();
            if (starterSounds != null && !starterSounds.isEmpty()) {
                final StarterKitSoundAudience audience = this.settings.getSoundAudience();
                for (final Map.Entry<String, Integer> entry : starterSounds.entrySet()) {
                    try {
                        final Sound sound = Sound.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
                        final float volume = Math.max(0.1f, Math.min(2.0f, entry.getValue()));
                        audience.play(player, sound, volume, 1.0f);
                    } catch (final Exception ignored) {
                    }
                }
            }
        }

        if (this.settings.isRunCommands()) {
            final List<String> commands = this.settings.getCommands();
            if (commands != null && !commands.isEmpty()) {
                Players.dispatchCommands(player, commands);
            }
        }

        if (this.settings.isWelcomeMessage()) {
            final List<String> welcomeFormat = this.settings.getWelcomeMessageFormat();
            if (welcomeFormat != null && !welcomeFormat.isEmpty()) {
                final String message = String.join("\n", welcomeFormat);
                final NightComponent component = NightMessage.parse(message);
                Players.sendMessage(player, component);
            }
        }

        if (this.settings.isGlobalAnnouncement()) {
            final List<String> globalFormat = this.settings.getGlobalAnnouncementFormat();
            if (globalFormat != null && !globalFormat.isEmpty()) {
                final String message = String.join("\n", globalFormat);
                final NightComponent component = NightMessage.parse(message);
                for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    Players.sendMessage(onlinePlayer, component);
                }
            }
        }

        if (this.settings.isTeleportToSpawn()) {
            final SpawnsModule spawnsModule = plugin.moduleManager().getByType(SpawnsModule.class).orElse(null);
            if (spawnsModule != null) {
                final Spawn spawn = spawnsModule.getNewbieSpawn(player);
                if (spawn != null && spawn.isActive()) {
                    final TeleportContext context = TeleportContext.builder(this.module, player, spawn.getLocation())
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
