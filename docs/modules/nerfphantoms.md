---
description: "Guide to the Nerf Phantoms module in SunLight for Minecraft servers, covering commands, permissions, configuration, features, and usage for admins and players."
---

# Nerf Phantoms

Controls phantom behavior by disabling spawns, modifying damage/health, and allowing players to reset insomnia (anti-phantom) individually.

* **Module ID:** `nerf_phantoms`
* **Display name:** `Nerf Phantoms`
* **Module directory:** `moduleImpl/nerfphantoms/`
* **Implementation:** `PhantomsModule`
* **Provider ID:** `nophantom`

## Commands

> Configurable in `commands/nophantom.yml`.

| Command                     | Aliases                | Permission                                      | Description                               | Usage                                                                      |
| --------------------------- | ---------------------- | ----------------------------------------------- | ----------------------------------------- | -------------------------------------------------------------------------- |
| `/phantoms` (root)          | `nophantom`, `phantom` | `sunlight.nerfphantoms.command.phantoms.root`   | Phantoms command root.                    |                                                                            |
| `/phantoms toggle [target]` | (under root)           | `sunlight.nerfphantoms.command.phantoms.toggle` | Toggle anti-phantom mode for self/target. | `[target]` requires `sunlight.nerfphantoms.command.phantoms.toggle.others` |

## Permissions

| Permission                                             | Description                                |
| ------------------------------------------------------ | ------------------------------------------ |
| `sunlight.nerfphantoms.command.phantoms.root`          | Root command access                        |
| `sunlight.nerfphantoms.command.phantoms.toggle`        | Toggle anti-phantom mode                   |
| `sunlight.nerfphantoms.command.phantoms.toggle.others` | Toggle anti-phantom mode for other players |

## Configuration

Source: `moduleImpl/nerfphantoms/config/PhantomsConfig.java`

| Key                      | Default              | Description                                                |
| ------------------------ | -------------------- | ---------------------------------------------------------- |
| `DisableSpawn.Enabled`   | `false`              | Disable phantom spawning.                                  |
| `DisableSpawn.Sources`   | `[NATURAL, DEFAULT]` | Spawn reasons to block (`CreatureSpawnEvent.SpawnReason`). |
| `DamageModifier.Enabled` | `false`              | Enable phantom damage modification.                        |
| `DamageModifier.Value`   | `1D`                 | Damage multiplier/value used by modifier logic.            |
| `HealthModifier.Enabled` | `false`              | Enable phantom health modification.                        |
| `HealthModifier.Value`   | `20D`                | Health value used by modifier logic.                       |

## Placeholder

* `%phantoms_antiphantom_state%` — yes/no state for anti-phantom mode.

## Behavior, compatibility, and integration notes

* When anti-phantom is enabled for a player, the module periodically resets `Statistic.TIME_SINCE_REST` (task runs on main thread as implemented) to prevent phantom spawning due to insomnia.
* Spawn/damage/health modifications apply according to configured toggles and values.
