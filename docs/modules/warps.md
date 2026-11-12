# Warps

Server-wide public warps with permissions, costs, GUI management, and optional per-warp shortcut commands.

* **Module ID:** `warps`
* **Display name:** `Warps`
* **Module directory:** `moduleImpl/warps/`
* **Provider ID:** `warps`
* **API/provider:** implements `WarpsProvider`

## Commands

> Configurable in `commands/warps.yml`.

| Command                                 | Permission                                             | Description                               | Usage / notes                                                                              |
| --------------------------------------- | ------------------------------------------------------ | ----------------------------------------- | ------------------------------------------------------------------------------------------ |
| `/warps` (root, alias `warp`)           | `sunlight.warps.command.warps.root`                    | Warps root command.                       |                                                                                            |
| `/warps create <name>`                  | `sunlight.warps.command.warps.create`                  | Create a warp.                            |                                                                                            |
| `/warps update <warp>`                  | `sunlight.warps.command.warps.update`                  | Update warp location/data as implemented. |                                                                                            |
| `/warps delete <warp>`                  | `sunlight.warps.command.warps.delete`                  | Delete a warp.                            |                                                                                            |
| `/warps edit <warp>`                    | `sunlight.warps.command.warps.edit`                    | Open/edit warp options.                   | May require editor-related permissions.                                                    |
| `/warps menu [target]`                  | `sunlight.warps.command.warps.list` / `...list.others` | Open warps list/menu.                     | Targeting others requires `list.others` (per provider logic).                              |
| `/warps jump <warp> [target/-s/-force]` | `sunlight.warps.command.warps.teleport`                | Teleport to a warp.                       | Others/silent/force flags depend on implementation and permissions (`...teleport.others`). |

### Per-warp dynamic commands

When a warp has `commandEnabled=true` and a configured `commandLabel`, the module registers a dynamic command for that warp.

* **Permission:** `sunlight.warps.warp.<id>` (warp access permission)
* **Behavior:** teleports the executing player to the warp (subject to checks/costs/rules)

## Permissions

| Permission                                     | Description                                                 |
| ---------------------------------------------- | ----------------------------------------------------------- |
| `sunlight.warps.command.warps.root`            | Root command access                                         |
| `sunlight.warps.command.warps.create`          | Create warps                                                |
| `sunlight.warps.command.warps.update`          | Update warps                                                |
| `sunlight.warps.command.warps.delete`          | Delete warps                                                |
| `sunlight.warps.command.warps.teleport`        | Teleport to warps                                           |
| `sunlight.warps.command.warps.teleport.others` | Teleport others to warps                                    |
| `sunlight.warps.command.warps.list`            | Open/list warps menu for self                               |
| `sunlight.warps.command.warps.list.others`     | Open/list warps menu for others                             |
| `sunlight.warps.command.warps.edit`            | Edit warps                                                  |
| `sunlight.warps.editor`                        | Warp editor access (where enforced)                         |
| `sunlight.warps.warp`                          | Warp permission namespace                                   |
| `sunlight.warps.warp.<id>`                     | Access/use a specific warp (including dynamic warp command) |
| `sunlight.warps.bypass.cost`                   | Bypass warp costs                                           |
| `sunlight.warps.bypass.cooldown`               | Bypass warp cooldowns                                       |

## Configuration

Source: `moduleImpl/warps/core/WarpsSettings.java`

| Key                   | Default       | Description                                           |
| --------------------- | ------------- | ----------------------------------------------------- |
| `Warps.Save-Interval` | `60`          | Periodic save interval for dirty warp data (seconds). |
| `Warps.Default-Icon`  | `ENDER_PEARL` | Default warp icon material.                           |
| `Warps.Creation.Cost` | `0D`          | Cost to create a warp.                                |
| `Warps.Teleport.Cost` | `0D`          | Cost to teleport to a warp.                           |

Warp data is stored in module warp files (e.g. `warps/*.yml`) and includes properties such as name, description, location, icon, command label/enabled state, and access-related options.

## Placeholders

* `%warp_id%`
* `%warp_name%`
* `%warp_description%`
* `%warp_location_world%`
* `%warp_location_x%`
* `%warp_location_y%`
* `%warp_location_z%`

## GUI and dialogs

* Menus: warp list, warp options
* Dialogs: warp name/description/command/slots and related editor flows
* Event: `WarpTeleportEvent`

## Notes

* Per-warp permissions allow fine-grained access control.
* Costs/cooldowns may be enforced via module settings and command/provider config.
* World activation tracking and save-dirty tasks are handled by the module implementation.
