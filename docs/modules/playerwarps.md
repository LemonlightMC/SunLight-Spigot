# Player Warps

Allows players to create and manage their own warps with categories/ranks, claims, moderation tools, safety checks, economy integration, and rich GUI/dialog workflows.

* **Module ID:** `playerwarps`
* **Display name:** `Player Warps`
* **Module directory:** `moduleImpl/playerwarps/`

## Commands

Implemented by `PlayerWarpsCommands` (command provider).

The module provides a large command set for:

* Creating, deleting, and teleporting to player warps
* Listing/browsing/searching warps and categories
* Managing warp settings (name, description, icon, category, visibility, etc.)
* Claiming/owning and moderation/admin actions
* Importing warps from supported sources (if configured)

> See `moduleImpl/playerwarps/command/PlayerWarpsCommands.java` and generated `commands/<providerId>.yml` for the exact command tree, arguments, aliases, and cooldown/cost settings.

## Permissions

Defined in `moduleImpl/playerwarps/core/PlayerWarpsPerms.java`.

Permission areas typically include:

* Player warp command access (create/delete/teleport/list/manage)
* Admin/moderation permissions
* Bypass permissions for limits, costs, cooldowns, safety/moderation checks (as implemented)
* Category/rank/feature access controls

## Configuration

* **Settings:** `moduleImpl/playerwarps/core/PlayerWarpsSettings.java`
* **Language:** `moduleImpl/playerwarps/core/PlayerWarpsLang.java`
* **Placeholders:** `moduleImpl/playerwarps/PlayerWarpsPlaceholders.java`

Configuration covers (module scope):

* Warp limits and rank-based allowances
* Creation/teleport costs and economy behavior
* Cooldowns and usage restrictions
* Categories, sorting, featured/top warp behavior
* Safety/validation rules and moderation options
* GUI/menu behavior and dialogs
* Importer settings/options (if used)

## Features

* Player-owned warps with metadata and categorization
* GUI menus and dialogs for browsing, editing, and management
* Moderation/administration workflows
* Validation and safety checks before creation/teleport
* Visit tracking/statistics and placeholders (as implemented)

## Data and components

* Data repositories/storage classes under `data/`
* Dialogs/menus under `dialog/**` and `menu/**`
* Importer, validation, listeners, and visit/object models in module subpackages

## Notes

* Player Warps is highly configurable; review generated settings and menus after first load.
* Economy/protection/plugin integrations may affect creation and teleport behavior.
* For exact command usage on your server, rely on generated command config and in-game help in addition to this overview.
