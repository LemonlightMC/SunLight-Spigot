# Bans Module

Comprehensive punishment system for bans, temporary bans, IP bans, mutes, warns, kicks, pardons, punishment history/lists, and alt detection.

- **Module ID:** `bans`
- **Display name:** `Bans`
- **Provider IDs:** `bans-punish`, `bans-pardon`, `bans-history`, `bans-list`, `bans-alts` (based on command provider classes)

## Commands

> Exact aliases/enabled/cooldown/cost can be configured in the corresponding `commands/<providerId>.yml` files.

### Punishment commands

| Command | Permission | Description | Usage |
| --- | --- | --- | --- |
| `/ban <player> [reason]` | `sunlight.bans.command.ban` | Permanently bans a player. | `[reason]` can be a configured reason id/text; supports silent flag tokens in reason parsing (`-s`, `--silent`) where applicable. |
| `/tempban <player> <amount> <unit> [reason]` | `sunlight.bans.command.ban` | Temporarily bans a player. | `<unit>` uses configured time unit aliases. |
| `/mute <player> [reason]` | `sunlight.bans.command.mute` | Permanently mutes a player. | Same reason/silent parsing behavior. |
| `/tempmute <player> <amount> <unit> [reason]` | `sunlight.bans.command.mute` | Temporarily mutes a player. | Time-based mute. |
| `/warn <player> [reason]` | `sunlight.bans.command.warn` | Issues a warning. | Permanent warn flow in command builder. |
| `/tempwarn <player> <amount> <unit> [reason]` | `sunlight.bans.command.warn` | Issues a temporary warning. | Time-based warn. |
| `/banip <address> [reason]` | `sunlight.bans.command.banip` | Bans an IP address. | Permanent IP ban; address parsed with `InetAddress.getByName`. |
| `/kick <player> [reason]` | `sunlight.bans.command.kick` | Kicks an online player. | Uses parsed reason/silent flags. |

### Pardon commands

| Command | Permission | Description | Usage |
| --- | --- | --- | --- |
| `/unban <player> [target/flags]` | `sunlight.bans.command.unban` | Removes an active ban. | Suggestions from active banned players; silent flag supported via target argument parsing. |
| `/unmute <player> [target/flags]` | `sunlight.bans.command.unmute` | Removes an active mute. | Suggestions from active muted players. |
| `/unwarn <player> [target/flags]` | `sunlight.bans.command.unwarn` | Removes an active warning. | Suggestions from active warned players. |
| `/unbanip <address> [target/flags]` | `sunlight.bans.command.unbanip` | Removes an active IP ban. | Suggestions from active IP bans. |

### History, lists, and alts

Available commands include:

| Command group | Provider | Permission(s) | Purpose |
| --- | --- | --- | --- |
| Ban/Mute/Warn history | `bans-history` | `sunlight.bans.command.banhistory`, `...mutehistory`, `...warnhistory` | View punishment history (typically with GUI/menu support). |
| Ban/Mute/Warn lists | `bans-list` | `sunlight.bans.command.banlist`, `...mutelist`, `...warnlist` | View active punishment lists. |
| Alts | `bans-alts` | `sunlight.bans.command.alts` (+ notify permission) | Inspect possible alternate accounts. |

## Permissions

| Permission | Description |
| --- | --- |
| `sunlight.bans.command.ban` | Ban/tempban commands |
| `sunlight.bans.command.banip` | IP ban command |
| `sunlight.bans.command.mute` | Mute/tempmute commands |
| `sunlight.bans.command.warn` | Warn/tempwarn commands |
| `sunlight.bans.command.kick` | Kick command |
| `sunlight.bans.command.unban` | Unban command |
| `sunlight.bans.command.unbanip` | Unban IP command |
| `sunlight.bans.command.unmute` | Unmute command |
| `sunlight.bans.command.unwarn` | Unwarn command |
| `sunlight.bans.command.banhistory` | Ban history command |
| `sunlight.bans.command.mutehistory` | Mute history command |
| `sunlight.bans.command.warnhistory` | Warn history command |
| `sunlight.bans.command.banlist` | Ban list command |
| `sunlight.bans.command.mutelist` | Mute list command |
| `sunlight.bans.command.warnlist` | Warn list command |
| `sunlight.bans.command.alts` | Alts command |
| `sunlight.bans.alts.notify` | Receive alt-detection notifications |
| `sunlight.bans.punishment.delete` | Delete punishments (GUI/management flows where checked) |
| `sunlight.bans.punishment.toggle` | Toggle punishment state (GUI/management flows where checked) |
| `sunlight.bans.bypass.duration.limit` | Bypass punishment duration limits |
| `sunlight.bans.bypass.alts.detection` | Bypass alt detection |

## Configuration (`settings.yml`)

### Data and storage
| Key | Default | Description |
| --- | --- | --- |
| `Data.Table-Prefix` | `sunlight_bans` | Database table prefix (use different prefixes to isolate servers, same prefix to sync). |
| `Data.Save-Interval` | `10` | Seconds between writes of punishment data changes (e.g. GUI status changes). |

### Priority and authority
| Key | Default | Description |
| --- | --- | --- |
| `Priority.Console` | `10000` | Console punishment priority. |
| `Priority.Ranks` | `moderator:50, admin:100, owner:999` | Rank-based punishment priorities; higher value wins. Best with LuckPerms; offline resolution may be limited without it. |

### Time units and duration limits
| Key | Default | Description |
| --- | --- | --- |
| `Time.Unit-Aliases` | aliases for seconds/minutes/hours/days/weeks/months/years | Custom aliases for punishment command time units. |
| `Time.Duration-Limits.Ranks` | example limits for moderator/helper | Max allowed punishment duration per rank and punishment type. `-1` removes a limit; delete group section to remove all limits for it. |
| `Time.Duration-Limits.Include-For-Pardon` | `true` | Apply duration-limit logic to pardon commands (`/unban`, `/unmute`, `/unwarn`). |

### Security and reasons
| Key | Default | Description |
| --- | --- | --- |
| `Security.Immunity-List` | placeholder entries (`add_your_names_or_uuids_or_ips`, `127.0.0.1`, `0.0.0.0`) | Names/IPs/UUIDs immune to punishments. |
| `General.Reasons` | `default`, `advertisement`, `grief`, `toxic`, `cheating` | Predefined punishment reasons used by commands. |

### Disconnect screens
| Key | Description |
| --- | --- |
| `Disconnect-Screen.Kick` | Message shown to kicked players. |
| `Disconnect-Screen.Permanent-Ban` | Message shown for permanent bans. |
| `Disconnect-Screen.Temp-Ban` | Message shown for temporary bans. |
| `Disconnect-Screen.IP-Ban` | Message shown for IP bans. |

Supports punishment placeholders in message content (reason, target, dates, expiry, etc.).

### Alt detector
| Key | Default | Description |
| --- | --- | --- |
| `AltDetector.Enabled` | `true` | Enables alt detection features. |

### Mute restrictions
| Key | Default | Description |
| --- | --- | --- |
| `Mute.Block-Commands.Enabled` | `true` | Restrict muted players from using selected commands. |
| `Mute.Block-Commands.List` | `[me, broadcast]` | Blocked commands for muted players (without `/`). |

### Warn automation
| Key | Default | Description |
| --- | --- | --- |
| `Warn.Max-Amount-To-Reset` | `5` | Active warn limit before reset (`-1` disables). Also important for auto-command thresholds. |
| `Warn.Commands-For-Active-Warns` | `3 -> smite <player>`, `5 -> kick <player>` | Console commands executed at specific active warn counts. |

## Placeholders

Bans module registers placeholders (punishment-related values for active/history/list contexts).

## Menus / UI

- , , and sorting modes are implemented for punishment browsing/management.
- GUI actions may require `sunlight.bans.punishment.delete` / `sunlight.bans.punishment.toggle`.

## Notes

- Punishment commands support configured reason IDs and silent flags (`-s` / `--silent`) in reason parsing.
- Temporary punishment commands require amount + time unit; time unit aliases are configurable.
- Data is persisted via the bans data manager/repositories/queries; cross-server sync is possible with shared DB/prefix strategy.