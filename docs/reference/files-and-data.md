---
description: "SunLight file and data layout reference for Minecraft servers, explaining core config files, module folders, command configs, exports, backups, and database storage."
---
# File & Data Layout

This page summarizes important SunLight files and where module data/configuration is typically stored. Paths are relative to `plugins/SunLight/` unless noted.

## Core files

| Path | Purpose |
| --- | --- |
| `config.yml` (or core config managed by plugin) | Global SunLight settings (`General.*`, `Combat.Hook`, etc.) |
| `lang/` or language resources | Localized messages/text (core and/or resource-provided translations) |
| `modules.yml` | Module enable/disable state and module display name/prefix |
| `commands/<providerId>.yml` | Per-command-provider overrides: enabled, aliases, cooldown, cost |
| `exports/*.zip` | Data export/import archives used by `/sunlight export` and `/sunlight import` |

## Module storage patterns

SunLight modules typically use one or more of the following:

| Path / pattern | Purpose |
| --- | --- |
| `<module>/settings.yml` | Module-specific configuration |
| `<module>/menu/*.yml` | GUI/menu layout files (module-dependent) |
| `<module>/.../*.yml` | Module data files (homes, warps, spawns, kits, texts, announcers, player warps data, etc.) |
| Database tables | Persistent data for modules that use SQL storage (e.g. bans, reports, voting, mail/chat data, friends/profiles/playtime-related storage depending on implementation) |

## Examples by module

- **Scheduler:** announcer files in `scheduler/announcers/*.yml` (default `global` announcer created if missing).
- **Texts (Custom Text):** text definitions in `texts/texts/*.yml` or module texts directory (default `rules` text may be generated).
- **Warps:** warp definitions under the warps module data directory (e.g. `warps/*.yml`).
- **Spawns:** spawn definitions in `spawns/` directory as YAML files.
- **Homes/Kits/Player Warps/Links/etc.:** store data in module folders and/or databases depending on module design.

## Database

SunLight supports **SQLite** and **MySQL** (per project README). Database-backed modules manage their own tables/repositories/queries. Check module source or generated data/config for table prefixes and storage options where exposed (e.g. Voting uses a configurable table prefix in settings).

## Backups and migration

- Use `/sunlight export [name]` to create a zip backup in `exports/`.
- Use `/sunlight import [name]` to restore from `exports/` (path-safe extraction) and reload.
- Always back up both files and database data before major upgrades.

## Generated/managed files

- `modules.yml` and per-provider command configs are managed/generated as needed by the plugin.
- Some modules create default data files (e.g. scheduler announcers, custom texts) when missing.
- Config migrations may occur on load for renamed/legacy settings in specific modules.
