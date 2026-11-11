# AFK Module

Tracks player inactivity, places players into AFK mode, and can run actions such as warnings, kicks, commands, sounds, particles, status bars, and integrations.

- **Module ID:** `afk`
- **Display name:** `AFK`
- **Module directory:** `moduleImpl/afk/`
- **Provider IDs:** `afk`
- **Placeholders:** see [AFK placeholders](../reference/placeholders.md#afk-afkplaceholders)

## Commands

> Command labels, aliases, cooldowns, and costs can be overridden in `commands/afk.yml`.

| Command | Aliases / node | Permission | Description | Usage / notes |
| --- | --- | --- | --- | --- |
| `/afk` | root | `sunlight.command.afk` | Toggles AFK for yourself (default root behavior). | Player/self context by default; target support depends on subcommand. |
| `/afk on` | `afk-on` | `sunlight.command.afk` | Enables AFK mode. | `[target]` (target requires `sunlight.command.afk.others`) |
| `/afk off` | `afk-off` | `sunlight.command.afk` | Disables AFK mode. | `[target]` (target requires `sunlight.command.afk.others`) |
| `/afk toggle` | `toggle` | `sunlight.command.afk` | Toggles AFK mode. | `[target]` (target requires `sunlight.command.afk.others`) |
| `/afk list` | `list` | `sunlight.command.afk.list` | Shows currently AFK players. | Lists AFK players with time since AFK entry. |
| `/afk status` | `status` | `sunlight.command.afk.status` | Shows AFK status summary for a player. | `[target]` (others require `sunlight.command.afk.others`) |
| `/afk info` | `info` | `sunlight.command.afk.info` | Shows detailed AFK info for a player. | `[target]` (others require `sunlight.command.afk.others`) |
| `/afk kick <player>` | `kick` | `sunlight.command.afk.kick` | Kicks a player for idling (module kick flow). | `<player>` (online player suggestion) |
| `/afk gui` | `gui` | `sunlight.command.afk.gui` | Opens AFK admin GUI. | Player-only; shows unavailable message if GUI cannot be opened. |

## Permissions

### Command permissions
| Permission | Description |
| --- | --- |
| `sunlight.command.afk` | Base AFK command access (`/afk`, `on`, `off`, `toggle`) |
| `sunlight.command.afk.others` | Target other players with AFK commands/status/info where supported |
| `sunlight.command.afk.list` | View AFK player list |
| `sunlight.command.afk.status` | View AFK status |
| `sunlight.command.afk.info` | View detailed AFK info |
| `sunlight.command.afk.kick` | Kick players via AFK module command |
| `sunlight.command.afk.gui` | Open AFK admin GUI |

### Module/bypass namespace
| Permission | Description |
| --- | --- |
| `sunlight.afk` | Module permission namespace root |
| `sunlight.afk.bypass` | Bypass namespace root (specific bypass checks are implemented in module logic/hooks where applicable) |

## Configuration (`settings.yml`)

Source: `moduleImpl/afk/config/AfkSettings.java`. Key options:

### Wake-up and activity
| Key | Default | Description |
| --- | --- | --- |
| `WakeUp.Threshold` | `3` | Activity points required to leave AFK mode. |
| `WakeUp.Timeout` | `3` | Seconds to reach the threshold while waking up before points/timer reset. |
| `WakeUp.Activity-Points` | movement/rotation/chat/command/interact/block/item/container/fish/vehicle map | Points granted per activity type. |
| `WakeUp.Ignored-Commands` | `[]` | Commands that do not count as activity (supports prefix wildcard `*`). |
| `WakeUp.Track-Rotation` | `false` | If `false`, head rotation alone does not count as movement activity. |
| `WakeUp.Commands` | `[]` | Commands executed before player leaves AFK mode. |

### AFK status bars
| Key | Default | Description |
| --- | --- | --- |
| `AFK.Status-Bar.Enabled` | `true` | Show AFK status bar while player is AFK. |
| `AFK.Status-Bar.Text` | formatted text with `%time%` | AFK status bar text. |
| `AFK.Status-Bar.Wake-Up-Enabled` | `true` | Show wake-up status bar while player is waking up. |
| `AFK.Status-Bar.Wake-Up-Text` | formatted text with `%remain%`, `%time-left%` | Wake-up status bar text. |

### Idle/AFK timing, warnings, kick
| Key | Default | Description |
| --- | --- | --- |
| `AFK.Cooldown` | `60` | Immunity period (seconds) after leaving AFK before being marked inactive again. |
| `AFK.Kick.Rejoin-Window` | `0` | Re-kick window after idle kick (`-1` until restart, `0` disabled). |
| `AFK.Kick.Warning-Ahead` | `30` | Seconds before idle kick to warn (`0` disabled). |
| `AFK.Kick.Warning-Interval` | `5` | Minimum seconds between repeated warnings. |
| `AFK.Idle_Time` | rank table (default 600, vip 900, admin -1) | Seconds until player enters AFK (`-1` immune). Highest/negative applicable value is used. |
| `AFK.Kick_Time` | rank table (default 1200, vip -1, admin -1) | Total inactivity time before kick (`-1` immune). |
| `AFK.Afk_Kick_Time` | rank table (default -1) | Time spent in AFK before kick (`-1` never). |
| `AFK.Kick_Message` | multi-line message | Kick message for idle/AFK kick. |

### AFK actions and exemptions
| Key | Default | Description |
| --- | --- | --- |
| `AFK.Commands` | `[]` | Commands executed before player enters AFK mode. |
| `AFK.Exempt-Worlds` | empty set | Worlds exempt from AFK detection/kicks. |
| `AFK.Exempt-Regions` | empty set | WorldGuard regions exempt from AFK detection/kicks. |

### Blocked actions while AFK
| Key | Default |
| --- | --- |
| `AFK.Block-Actions.Movement` | `false` |
| `AFK.Block-Actions.Block-Break` | `true` |
| `AFK.Block-Actions.Block-Place` | `true` |
| `AFK.Block-Actions.Interact` | `true` |
| `AFK.Block-Actions.Inventory` | `true` |
| `AFK.Block-Actions.Item-Drop` | `true` |
| `AFK.Block-Actions.Item-Pickup` | `true` |
| `AFK.Block-Actions.Chat` | `false` |
| `AFK.Block-Actions.Commands` | `false` |
| `AFK.Block-Actions.Damage-Incoming` | `true` |
| `AFK.Block-Actions.Damage-Outgoing` | `true` |
| `AFK.Block-Actions.Mob-Targeting` | `true` |

> `Movement` blocking prevents waking by movement; wake-up must come from other allowed activity.

### Sounds and particles
| Key | Default | Description |
| --- | --- | --- |
| `Sound.Enter` | `ENTITY_EXPERIENCE_ORB_PICKUP;0.6` | Sound on AFK enter. |
| `Sound.Exit` | `ENTITY_EXPERIENCE_ORB_PICKUP;0.6;1.5` | Sound on AFK exit. |
| `Particles.Enter.*` | enabled, `CLOUD`, count/offset | Enter particle effect. |
| `Particles.Exit.*` | enabled, `HAPPY_VILLAGER`, count/offset | Exit particle effect. |

### Integrations and anti-AFK bypass detection
| Key area | Description |
| --- | --- |
| `Integration.LuckPerms-Context` | Exposes `afkstate` LuckPerms context (`true/false`). |
| `Integration.DiscordSRV.*` | Discord notifications for enter/exit/kick with channel, embeds, duration, avatar, and message templates. |
| `Integration.Vanish.*` | Ignore vanished players for detection/kicks and optionally suppress broadcasts. |
| `AFK.Ignore-For-Sleep` | Treat AFK players as sleeping-ignored for night skip. |
| `Anti-AFK.*` | Filters fake/suspicious activity (vehicles, water flow, micro movement/rotation thresholds). |

## Placeholders

Registered in `AfkPlaceholders`:
`%afk_state%`, `%afk_mode%`, `%afk_idle_time%`, `%afk_idle_time_formatted%`, `%afk_total_time%`, `%afk_total_time_formatted%`, `%afk_count%`, `%afk_enter_timestamp%`, `%afk_enter_time%`, `%afk_duration%`, `%afk_duration_formatted%`, `%afk_time_until_afk%`, `%afk_time_until_kick%`, `%afk_online_count%`, `%afk_online%`, `%afk_stats_sessions%`, `%afk_stats_total_time%`, `%afk_stats_total_time_formatted%`, `%afk_stats_average_time%`, `%afk_stats_average_time_formatted%`.

## User data properties

- `AFK_TOTAL_TIME`
- `AFK_COUNT`

## Notes

- Rank-based timing/settings use SunLight's rank/permission table mechanism.
- Some behavior depends on optional integrations (LuckPerms, DiscordSRV, WorldGuard regions for exemptions, vanish integration).
- GUI/menu availability depends on module UI setup.
