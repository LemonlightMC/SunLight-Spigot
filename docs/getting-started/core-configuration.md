# Core Configuration

## Global config (`config.yml` / `config/Config.java`)

Global options defined in `su.nightexpress.sunlight.config.Config`:

| Path | Type | Default | Description |
| --- | --- | --- | --- |
| `General.Time_Zone` | String | `system` | Global timezone for dates/times and day/week/month rollovers. Use `system` or an IANA ID (e.g. `Europe/Berlin`, `America/New_York`). |
| `General.Date_Format` | String | `dd/MM/yyyy HH:mm:ss` | Global date format used by plugin modules. |
| `General.Time_Format` | String | `HH:mm:ss` | Global time format used by plugin modules. |
| `General.ConsoleName` | String | `Console` | Name used for console sender in relevant messages (e.g. private messages, punishments/actions). |
| `Combat.Hook` | String | `auto` | Shared combat-tag source for modules: `auto`, `pvpmanager`, `combatlogx`, or `none`. `auto` prefers PvPManager, otherwise CombatLogX. |

## Global language
   
SunLight provides language/message files for core text such as:

- **Command argument labels** (slot, enchant, level, target, type, time, mob type, time, mode, state, ip, radius, text, command, position, x/y/z, owner)
- **Command cooldown/cost messages** (default cooldown, one-time cooldown, insufficient funds)
- **Plugin help messages** (`/sunlight` help header/entries)
- **Root command descriptions** (reload module, dump, export, import, cleanup)
- **Cleanup messages** (console denied, failed)
- **Teleport and safety messages** (unsafe location feedback/notify, missing offline data support messages)
- **Syntax/error messages** (invalid values, missing required item in hand, unsupported actions, etc.)
- **Common labels** (e.g. free)

Language resource files also exist under `Core/src/main/resources/lang/` (e.g. `messages_es.yml`, `messages_ru.yml`, `lang_zh-cn.yml`), with runtime/lang handling managed by the plugin/framework.

## Root admin command: `/sunlight`

Implemented in `SunlightCommandProvider` (`su.nightexpress.sunlight.SunlightCommandProvider`).

* **Command:** `/sunlight`
* **Alias:** `/sl`
* **Base permission:** `sunlight.admin`
* **Default behavior:** shows plugin help/subcommand list

> **Note:** Most user-facing commands come from enabled modules. The core `/sunlight` command is mainly for administration.

### Subcommands

| Subcommand | Description | Notes / Usage |
| --- | --- | --- |
| `help` | Lists available subcommands | `/sunlight help` |
| `reload` | Reloads the plugin | `/sunlight reload` |
| `reloadmodule <name>` | Reloads a single module | `/sunlight reloadmodule <id>`; module id/name suggestions provided by provider logic |
| `dump` | Prints diagnostics for support | Outputs plugin, server, Java, and module information to the sender and server logs |
| `export [name]` | Exports plugin data to a zip file | Async; writes to `exports/<name>.zip` (safe name handling; defaults to timestamped backup name if omitted/unsafe) |
| `import [name]` | Imports plugin data from a zip and reloads | Async extract from `exports/`, path-safety checks, then reloads plugin |
| `cleanup [--dry-run --include-safe]` | Cleans stale user properties from stored user data | Permission: `sunlight.admin.cleanup`; player-only confirmation dialog flow; async cleanup/prune |

> **Permission note:** `cleanup` explicitly requires `sunlight.admin.cleanup`. Other subcommands are under the `/sunlight` admin command context in this build. Public docs for other versions may list different core permission names.

## Module system

Implemented in `su.nightexpress.sunlight.module.*` (`ModuleManager`, `Module`, `ModuleDefinition`, `LoadCondition`).

### Module enablement (`modules.yml`)

Each module is defined under `Modules.<id>`:

* `Enabled` — whether the module is loaded/enabled (default behavior in code sets to `false` unless already configured)
* `Name` — display name
* `Prefix` — module prefix (default gradient-style prefix)

### Module files

* **Settings:** `<modulePath>/settings.yml`
* **Menus/UI:** `<modulePath>/menu/` (when the module uses GUI files)
* **Data/other files:** module-specific (see each module page)

### Load conditions

`LoadCondition` values used by module definitions include:

* `success`
* `packetLibrary`
* `tab`
* `tabOrBridge` (special handling for TAB/TAB-Bridge; may require PlaceholderAPI in bridge-only scenarios)

### Registered modules (core registration)

`SunLightPlugin` registers modules including: AFK, Bans, Back Location, Texts (custom text), Chat, Death Messages, Essential, Freeze, Friends, Glow, Greetings, Homes, Inventories, Kits, Nerf Phantoms, Links, Nametags (conditional), Nick, Player Warps, Playtime, Profiles, PTP, RTP, Reports, Scheduler, Socials, Spawns, Timer, Vanish, Voting, Settings, Warmups, Warps.

> **Implementation note:** A `moduleImpl/extras` source directory exists, but the module is not registered in `SunLightPlugin.registerModules()`. It is documented separately as unregistered/inactive unless wired by a custom build.

## Command/provider configuration

SunLight's command system is managed by `su.nightexpress.sunlight.command.CommandRegistry` and provider classes.

* Each command provider can persist configuration to `commands/<providerId>.yml`.
* Common per-node fields: `enabled`, `aliases`, `cooldown`, `cost`.
* Root and subcommand nodes are registered by providers (standalone/nested structure depends on provider implementation).
* Registry supports cooldown/cost enforcement with bypass permissions, conflict handling, and saving changes.

See [Commands & Permissions System](commands-and-permissions.md) and each module page for provider IDs, commands, and permission nodes.
