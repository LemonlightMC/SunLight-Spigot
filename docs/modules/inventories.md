# Inventories

Provides inventory and ender chest management tools for online/offline players, plus portable container/workstation access (NMS-gated where applicable).

* **Module ID:** `inventories`
* **Display name:** `Inventories`
* **Module directory:** `moduleImpl/inventories/`

## Commands

### Inventory commands (`InventoryCommandProvider`)

Root: `/inventory` (alias `inv`) Permission namespace: `sunlight.essential.command.inventory.*`

| Subcommand             | Permission (pattern)              | Description                                   |
| ---------------------- | --------------------------------- | --------------------------------------------- |
| `open [player]`        | `...inventory.open` / `.others`   | Open a player's inventory.                    |
| `clear [player]`       | `...inventory.clear` / `.others`  | Clear inventory contents.                     |
| `copy <from> <to>`     | `...inventory.copy`               | Copy inventory data between players/contexts. |
| `fill <item> [player]` | `...inventory.fill` / `.others`   | Fill inventory with an item.                  |
| `repair [player]`      | `...inventory.repair` / `.others` | Repair items in inventory.                    |

### Ender chest commands (`EnderchestCommandProvider`)

Root: `/enderchest` (alias `ec`) Permission namespace: `sunlight.essential.command.enderchest.*`

| Subcommand             | Permission (pattern)               | Description                  |
| ---------------------- | ---------------------------------- | ---------------------------- |
| `open [player]`        | `...enderchest.open` / `.others`   | Open ender chest.            |
| `clear [player]`       | `...enderchest.clear` / `.others`  | Clear ender chest.           |
| `copy <from> <to>`     | `...enderchest.copy`               | Copy ender chest contents.   |
| `fill <item> [player]` | `...enderchest.fill` / `.others`   | Fill ender chest.            |
| `repair [player]`      | `...enderchest.repair` / `.others` | Repair items in ender chest. |

### Portable containers (`ContainerCommandProvider`, NMS-gated)

Root: `/container` (aliases `containers`, `portablecontainers`) Permission namespace: `sunlight.essential.command.container.*`

* Registers open commands for supported portable containers/workstations (e.g. anvil, crafting, loom, grindstone, etc., depending on implementation/NMS support).
* Availability depends on server internals/NMS handler support.

> Exact subcommand names, arguments, and permission nodes are defined in the command providers.

## Permissions

Permissions are primarily registered inline in command providers under:

* `sunlight.essential.command.inventory.*`
* `sunlight.essential.command.enderchest.*`
* `sunlight.essential.command.container.*`

Common patterns include base command permissions and `.others` variants for targeting other players.

## Configuration

Source: `moduleImpl/inventories/config/InventoriesSettings.java`

| Key                          | Default | Description                                                              |
| ---------------------------- | ------- | ------------------------------------------------------------------------ |
| `Clear.Require_Confirmation` | `true`  | Require confirmation before clearing inventories/ender chests.           |
| `Clear.Confirm_Self_Only`    | `true`  | Restrict confirmation flow behavior to self-target cases as implemented. |

## UI and behavior

* Clear confirmation dialog: `ClearConfirmationDialog`
* Listener/handler classes manage inventory operations and container interactions.
* Supports operations on online players and, where implemented, offline player data.

## Notes

* Portable containers require compatible NMS/internals support and are gated accordingly.
* Use inventory/ender chest management permissions carefully; these are powerful moderation/admin tools.
* No separate module-level `*Lang`/`*Perms`/`*Placeholders` files were identified in the module package; messages and permissions are mostly provider/dialog-driven.
