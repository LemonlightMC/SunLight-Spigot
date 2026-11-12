# Core Configuration

## Global config (`config.yml`)

Main global options:

| Path                  | Type   | Default               | Description                                                                                                                          |
| --------------------- | ------ | --------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `General.Time_Zone`   | String | `system`              | Global timezone for dates/times and day/week/month rollovers. Use `system` or an IANA ID (e.g. `Europe/Berlin`, `America/New_York`). |
| `General.Date_Format` | String | `dd/MM/yyyy HH:mm:ss` | Global date format used by plugin modules.                                                                                           |
| `General.Time_Format` | String | `HH:mm:ss`            | Global time format used by plugin modules.                                                                                           |
| `General.ConsoleName` | String | `Console`             | Name used for console sender in relevant messages (e.g. private messages, punishments/actions).                                      |
| `Combat.Hook`         | String | `auto`                | Shared combat-tag source for modules: `auto`, `pvpmanager`, `combatlogx`, or `none`. `auto` prefers PvPManager                       |

## Global language

SunLight provides language/message files for core text such as:

- **Command argument labels** (slot, enchant, level, target, type, time, mode, state, position, and more)
- **Command cooldown/cost messages** (default cooldown, one-time cooldown, insufficient funds)
- **Plugin help messages** (`/sunlight` help header/entries)
- **Root command descriptions** (reload module, dump, export, import, cleanup)
- **Cleanup messages** (console denied, failed)
- **Teleport and safety messages** (unsafe location feedback/notify, missing offline data support messages)
- **Syntax/error messages** (invalid values, missing required item in hand, unsupported actions, etc.)
- **Common labels** (e.g. free)

Language files may be provided for multiple locales depending on your build. You can edit them to match your server's style.

## Root admin command: `/sunlight`

- **Command:** `/sunlight`
- **Alias:** `/sl`
- **Base permission:** `sunlight.admin`
- **Default behavior:** shows plugin help/subcommand list

> **Note:** Most user-facing commands come from enabled modules. The `/sunlight` command is mainly for administration.

### Subcommands

| Subcommand                           | Description                                        | Notes / Usage                                                                                                     |
| ------------------------------------ | -------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `help`                               | Lists available subcommands                        | `/sunlight help`                                                                                                  |
| `reload`                             | Reloads the plugin                                 | `/sunlight reload`                                                                                                |
| `reloadmodule <name>`                | Reloads a single module                            | `/sunlight reloadmodule <id>`; module id/name suggestions provided by provider logic                              |
| `dump`                               | Prints diagnostics for support                     | Outputs plugin, server, Java, and module information to the sender and server logs                                |
| `export [name]`                      | Exports plugin data to a zip file                  | Async; writes to `exports/<name>.zip` (safe name handling; defaults to timestamped backup name if omitted/unsafe) |
| `import [name]`                      | Imports plugin data from a zip and reloads         | Async extract from `exports/`, path-safety checks, then reloads plugin                                            |
| `cleanup [--dry-run --include-safe]` | Cleans stale user properties from stored user data | Permission: `sunlight.admin.cleanup`; player-only confirmation dialog flow; async cleanup/prune                   |

## Module system

### Module enablement (`modules.yml`)

Each module is defined under `Modules.<id>`:

- `Enabled` — whether the module is loaded/enabled
- `Name` — display name
- `Prefix` — module prefix

### Module files

- **Settings:** `<modulePath>/settings.yml`
- **Menus/UI:** `<modulePath>/menu/` (when the module uses GUI files)
- **Data/other files:** module-specific (see each module page)

### Module requirements

Some modules only load when required dependencies or integrations are available (for example TAB/TAB-Bridge for nametag features, or packet-related plugins for certain features). Check the module's documentation for details.

### Available modules

Common modules include: AFK, Bans, Back Location, Texts (Custom Text), Chat, Death Messages, Essential, Freeze, Friends, Glow, Greetings, Homes, Inventories, Kits, Nerf Phantoms, Links, Nametags, Nick, Player Warps, Playtime, Profiles, PTP, RTP, Reports, Scheduler, Socials, Spawns, Timer, Vanish, Voting, Settings, Warmups, and Warps. Availability can vary by build.

## Command configuration

SunLight stores command configuration in `commands/<providerId>.yml` files.

- Common per-command fields: `enabled`, `aliases`, `cooldown`, `cost`.
- Some modules provide standalone commands and/or grouped root commands with subcommands.
- Cooldowns and costs can be configured per command, and bypass permissions may apply.

See [Commands & Permissions System](commands-and-permissions.md) and each module page for provider IDs, commands, and permission nodes.
