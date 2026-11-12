# Installation

## 1. Install dependencies

1. Download and install **NightCore** (required).
2. Install any optional plugins you need for integrations (e.g. PlaceholderAPI, LuckPerms, TAB, DiscordSRV, packet library).

## 2. Download and install SunLight

- **Official project downloads:** [SpigotMC](https://www.spigotmc.org/resources/67733/), [BuiltByBit](https://builtbybit.com/resources/89968/), [Modrinth](https://modrinth.com/plugin/sunlightcore/), [Hangar](https://hangar.papermc.io/NightExpress/SunLight/)
- **This repository:** contains the current source/build artifacts for this codebase version; release channels may differ from the source state.

1. Place the SunLight `.jar` into your server's `plugins/` folder.
2. Start (or restart) the server to generate configuration files.
3. Stop the server if you want to adjust core/module settings before players join.

## 3. Enable the modules you want

SunLight is modular. Modules are managed in `modules.yml`:

- Each module has an `Enabled` flag.
- Each module can have a custom display `Name` and `Prefix`.
- Per-module runtime settings are stored in the module's own `settings.yml`.
- Module GUI/menu files (where applicable) are stored under the module's `menu/` folder.

After enabling modules and editing configs, use `/sunlight reload` or `/sunlight reloadmodule <id>` (see [Core Configuration](core-configuration.md)).

## 4. Configure permissions and commands

- SunLight uses its own command/provider system with per-provider command config files under `commands/<providerId>.yml` (aliases, enabled state, cooldown, cost).
- Permission nodes are documented per module in this wiki.
- Grant only the permissions your ranks/groups need.

## 5. Verify

- Check console for startup/module load errors.
- Run `/sunlight help` (or `/sl help`) in-game/console (with permission) to confirm the root command is available.
- Test enabled module commands on a test account/rank.

## Updating

1. Back up your `plugins/SunLight` data/config folders.
2. Replace the plugin jar with the new version.
3. Review changelogs and config changes; regenerate or merge new options as needed.
4. Restart the server and test critical modules (teleports, bans, chat, data-backed modules).
