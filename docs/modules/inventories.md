# Inventories Module

Provides inventory and ender chest management tools for online and offline players, plus quick access to supported workstations/containers on compatible servers.

- **Module ID:** `inventories`
- **Display name:** `Inventories`

## Commands

### Inventory commands
Root: `/inventory` (alias `inv`)
Permission namespace: `sunlight.essential.command.inventory.*`

| Subcommand | Permission (pattern) | Description |
| --- | --- | --- |
| `open [player]` | `...inventory.open` / `.others` | Open a player's inventory. |
| `clear [player]` | `...inventory.clear` / `.others` | Clear inventory contents. |
| `copy <from> <to>` | `...inventory.copy` | Copy inventory data between players/contexts. |
| `fill <item> [player]` | `...inventory.fill` / `.others` | Fill inventory with an item. |
| `repair [player]` | `...inventory.repair` / `.others` | Repair items in inventory. |

### Ender chest commands
Root: `/enderchest` (alias `ec`)
Permission namespace: `sunlight.essential.command.enderchest.*`

| Subcommand | Permission (pattern) | Description |
| --- | --- | --- |
| `open [player]` | `...enderchest.open` / `.others` | Open ender chest. |
| `clear [player]` | `...enderchest.clear` / `.others` | Clear ender chest. |
| `copy <from> <to>` | `...enderchest.copy` | Copy ender chest contents. |
| `fill <item> [player]` | `...enderchest.fill` / `.others` | Fill ender chest. |
| `repair [player]` | `...enderchest.repair` / `.others` | Repair items in ender chest. |

### Portable containers
Root: `/container` (aliases `containers`, `portablecontainers`)
Permission namespace: `sunlight.essential.command.container.*`

- Provides commands to open supported workstations/containers (for example anvil, crafting table, loom, or grindstone), depending on what your server version supports.
- Available commands may vary by server version and setup.

> Check your server's command configuration and in-game help for the exact commands and permissions in your build.

## Permissions

Permissions are primarily registered inline in command providers under:
- `sunlight.essential.command.inventory.*`
- `sunlight.essential.command.enderchest.*`
- `sunlight.essential.command.container.*`

Common patterns include base command permissions and `.others` variants for targeting other players.

## Configuration

| Key | Default | Description |
| --- | --- | --- |
| `Clear.Require_Confirmation` | `true` | Require confirmation before clearing inventories/ender chests. |
| `Clear.Confirm_Self_Only` | `true` | Restrict confirmation flow behavior to self-target cases as implemented. |

## UI and behavior

- Clear confirmation dialog:
- Listener/handler classes manage inventory operations and container interactions.
- Supports operations on online players and, where implemented, offline player data.

## Notes

- Portable container commands depend on server compatibility and may not be available on every setup.
- Use inventory and ender chest management permissions carefully; these are powerful moderation/admin tools.