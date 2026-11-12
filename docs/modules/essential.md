# Essential Module

A large utility module providing core server/player commands such as teleportation, gamemode, healing/feeding, flight/speed, time, XP, renaming/enchanting, player info, and more.

- **Module ID:** `essential`
- **Display name:** `Essential`
## Commands

Essential provides a wide range of utility commands. Exact command names, aliases, arguments, cooldowns, and costs can be configured in `commands/<providerId>.yml`.

Common command areas include:

- **Movement & teleportation:** teleport players, go to worlds/dimensions, jump to targeted locations, and related movement utilities.
- **Player state:** toggle flight, set fly/walk speed, toggle god mode or invulnerability, heal, and feed players.
- **Gamemode & world:** change gamemodes and control time-related options.
- **Items & inventory:** rename items and manage enchantments or XP/levels.
- **Information:** view nearby players/entities and player information.

Use in-game help and your generated command configuration files for the exact commands available on your server.

## Permissions

Essential permissions generally use the `sunlight.essential...` namespace.

Common patterns include:
- Per-command permissions for using a command yourself
- `.others` permissions for targeting other players
- Bypass permissions for cooldowns, costs, or restrictions where applicable

> Check the module's command configuration and in-game permission checks for the exact permission nodes in your build.

## Configuration

- **Settings:** module `settings.yml` (module-wide options)

## Notes

- Because Essential contains many commands, this page summarizes the main command areas. Your generated `commands/*.yml` files and in-game help are the best reference for the exact commands enabled on your server.
- Some commands may depend on installed integrations or server compatibility.
- Where available, related toggles may also appear in the Settings module.