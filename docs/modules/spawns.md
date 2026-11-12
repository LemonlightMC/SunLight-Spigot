# Spawns

Manages server spawn points with default/newbie/login/respawn rules, teleport commands, and editor UIs.

* **Module ID:** `spawns`
* **Display name:** `Spawn`
* **Module directory:** `moduleImpl/spawns/`
* **Provider ID:** `spawn`
* **API/provider:** implements `SpawnsProvider`

## Commands

> Configurable in `commands/spawn.yml`.

| Command                                      | Aliases                     | Permission                                | Description          | Usage / notes                                                                                         |
| -------------------------------------------- | --------------------------- | ----------------------------------------- | -------------------- | ----------------------------------------------------------------------------------------------------- |
| `/spawns` (root)                             | `spawn`                     | (root context)                            | Spawns command root. |                                                                                                       |
| `/spawns create [name]`                      | `spawn_create`              | `sunlight.spawns.command.spawns.create`   | Create a spawn.      | Name optional depending on implementation/defaults.                                                   |
| `/spawns delete <name>`                      | `spawn_delete`              | `sunlight.spawns.command.spawns.delete`   | Delete a spawn.      |                                                                                                       |
| `/spawns editor`                             | `spawn_editor`, `editspawn` | `sunlight.spawns.command.spawns.editor`   | Open spawn editor.   |                                                                                                       |
| `/spawns teleport [name] [target/-s/-force]` | `spawn_teleport`            | `sunlight.spawns.command.spawns.teleport` | Teleport to a spawn. | Others: `sunlight.spawns.command.spawns.teleport.others`; supports silent/force flags as implemented. |

## Permissions

| Permission                                       | Description                                                   |
| ------------------------------------------------ | ------------------------------------------------------------- |
| `sunlight.spawns.command.spawns.create`          | Create spawns                                                 |
| `sunlight.spawns.command.spawns.delete`          | Delete spawns                                                 |
| `sunlight.spawns.command.spawns.teleport`        | Teleport to spawns                                            |
| `sunlight.spawns.command.spawns.teleport.others` | Teleport others to spawns                                     |
| `sunlight.spawns.command.spawns.editor`          | Open spawn editor                                             |
| `sunlight.spawns.bypass.cost`                    | Bypass spawn teleport cost                                    |
| `sunlight.spawns.bypass.cooldown`                | Bypass spawn teleport cooldown                                |
| `sunlight.spawns.spawn.<id>`                     | Access a specific spawn (checked via child permission access) |

## Configuration

Source: `moduleImpl/spawns/config/SpawnsSettings.java`

| Key                                         | Default   | Description                                        |
| ------------------------------------------- | --------- | -------------------------------------------------- |
| `Settings.Override-Player-Respawn-Location` | `false`   | Override player respawn location with spawn rules. |
| `Settings.Default_Spawn`                    | `default` | Default spawn ID.                                  |
| `Teleport_New_Players.Enabled`              | `true`    | Teleport new players on join.                      |
| `Teleport_New_Players.SpawnId`              | `default` | Spawn used for new players.                        |
| `Teleport.Cost`                             | `0D`      | Cost to teleport to spawn.                         |

Spawn definitions are stored as YAML files in the `spawns/` directory and edited via dialogs/menus.

## Placeholders

* `%spawn_id%`
* `%spawn_name%`
* `%spawn_location_world%`
* `%spawn_location_x%`
* `%spawn_location_y%`
* `%spawn_location_z%`

## GUI, dialogs, and rules

* Editors: spawn list editor, spawn settings editor
* Dialogs for name/priority/login rules/respawn rules
* Spawn rules model supports rank/wildcard matching (see `SpawnRule` and rule editors)
* Event: `PlayerSpawnTeleportEvent`

## Notes

* Spawn access can be restricted per spawn with `sunlight.spawns.spawn.<id>` permissions.
* Login/respawn/new-player behavior depends on configured rules and settings.
* Teleport costs/cooldowns may also be affected by command/provider configuration.
