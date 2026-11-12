---
description: "Guide to the Homes module in SunLight for Minecraft servers, covering commands, permissions, configuration, features, and usage for admins and players."
---

# Homes

Allows players to create and manage personal home locations with limits, costs, cooldowns, permissions, invites/visits, and GUI support.

* **Module ID:** `homes`
* **Display name:** `Homes`
* **Module directory:** `moduleImpl/homes/`
* **Provider IDs:** common/admin command providers (`homes`, `homesadmin`)

## Commands

> Configurable in `commands/<homes-provider>.yml` files.

### Common commands (root `homes` / `home`)

| Subcommand                                       | Permission                              | Description                                                   | Notes                                                 |
| ------------------------------------------------ | --------------------------------------- | ------------------------------------------------------------- | ----------------------------------------------------- |
| `set <home>`                                     | `sunlight.homes.command.homes.set`      | Create/set a home.                                            | Subject to limits, world/protection checks, and cost. |
| `delete <home>`                                  | `sunlight.homes.command.homes.delete`   | Delete own home.                                              |                                                       |
| `list`                                           | `sunlight.homes.command.homes.list`     | List/open homes.                                              | GUI support available.                                |
| `teleport <home>` (or home usage as implemented) | `sunlight.homes.command.homes.teleport` | Teleport to a home.                                           | Warmups/cooldowns/costs may apply.                    |
| `visit <player/home>`                            | `sunlight.homes.command.homes.visit`    | Visit another player's home (invite/guest flow as supported). |                                                       |
| `invite <player> <home>`                         | `sunlight.homes.command.homes.invite`   | Invite a player to a home.                                    |                                                       |

### Admin commands (root `homesadmin`)

| Subcommand   | Permission                                                                 | Description                           |
| ------------ | -------------------------------------------------------------------------- | ------------------------------------- |
| `create ...` | `sunlight.homes.command.homesadmin.create` (or equivalent in `HomesPerms`) | Create/manage homes administratively. |
| `delete ...` | `sunlight.homes.command.homesadmin.delete` (or equivalent)                 | Delete homes administratively.        |

> Refer to `HomeCommonCommandProvider`, `HomeAdminCommandProvider`, and `HomesPerms` for exact argument names and full command tree.

## Permissions

Source: `moduleImpl/homes/config/HomesPerms.java`

Common areas include:

* `sunlight.homes.command.homes.*` — player home commands (set/delete/list/teleport/visit/invite)
* `sunlight.homes.command.homesadmin.*` — admin commands
* Bypass permissions:
  * unsafe location checks
  * world blacklist restrictions
  * protection/build checks (as applicable)
  * teleport cost
  * teleport cooldown

## Configuration

Source: `moduleImpl/homes/config/HomesSettings.java`

Key options include:

* **Save interval** for home data persistence
* **Bed integration:** bed mode/colors and whether bed overrides respawn behavior
* **Home icons:** default icon and icon presets
* **Build/protection access checks** when setting homes
* **World blacklist** for home creation/usage restrictions
* **Rank-based home amount limits** (rank table)
* **Creation cost** and **teleport cost**

## Placeholders

* `%homes_limit%`
* `%homes_amount%`
* `%homes_favorite_home%`

## GUI and dialogs

* `HomeListMenu`, `HomeSettingsMenu`
* Dialogs for home creation/management flows
* Home model/data repositories for storage and player home state

## Important notes and limitations

* Home limits are typically rank/permission based.
* Teleport warmups/cooldowns/costs may be influenced by Homes settings, Warmups module, and command provider configs.
* Protection plugin hooks (WorldGuard/GriefPrevention) may affect where homes can be set.

## Related documentation

- [Warmups module](../modules/warmups.md)
