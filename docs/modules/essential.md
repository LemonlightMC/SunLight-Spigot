# Essential

A large utility module providing core server/player commands such as teleportation, gamemode, healing/feeding, flight/speed, time, XP, renaming/enchanting, player info, and more.

* **Module ID:** `essential`
* **Display name:** `Essential`
* **Module directory:** `moduleImpl/essential/`
* **Key files:** `EssentialModule.java`, `EssentialPerms.java`, `EssentialSettings.java`, `EssentialLang.java`, `EssentialPlaceholders.java`, `command/*`

## Commands

The Essential module is implemented as many command providers. Exact command names/aliases/arguments/cooldowns/costs can be configured via each provider's file in `commands/<providerId>.yml`.

| Provider                         | Purpose / command area                                                          | Notes                                                          |
| -------------------------------- | ------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| `AggroCommandProvider`           | Mob aggro/target-related utility command(s)                                     | See provider for exact command and args.                       |
| `DimensionCommandProvider`       | Dimension/world movement commands (e.g. overworld/nether/end style teleports)   | Implementation-specific command names/args.                    |
| `EnchantCommandsProvider`        | Enchant/disenchant item commands                                                | Item-in-hand and level/target handling per provider logic.     |
| `ExperienceCommandsProvider`     | XP/level management commands                                                    | Add/set/take/view style operations as implemented.             |
| `FlyCommandProvider`             | Toggle flight for self/others                                                   | Commonly supports target and on/off/toggle modes.              |
| `FlySpeedCommandProvider`        | Set flight speed                                                                | Self/others support depends on provider implementation.        |
| `FoodLevelCommandProvider`       | Feed/food-level commands                                                        | Self/others support depends on provider implementation.        |
| `GamemodeCommandProvider`        | Game mode commands (survival/creative/adventure/spectator and/or aliases)       | May register multiple shortcuts; check provider/config.        |
| `GodCommandProvider`             | Toggle god mode                                                                 | Stores state in essential properties (`GOD`).                  |
| `HealthCommandProvider`          | Heal/health-related commands                                                    | Self/others support depends on provider implementation.        |
| `InvulnerabilityCommandProvider` | Invulnerability toggles/commands                                                | May integrate with listeners/state handling.                   |
| `JumpCommandProvider`            | Jump/top-like movement utilities                                                | Implementation-specific.                                       |
| `LookTpCommandProvider`          | Teleport/look/targeted teleport utilities                                       | Implementation-specific.                                       |
| `NearCommandProvider`            | List nearby players/entities                                                    | Radius/format behavior per provider/settings.                  |
| `PlayerInfoCommandProvider`      | Player information display                                                      | Shows player details/stats as implemented.                     |
| `RenameCommandProvider`          | Rename items (and possibly lore-related operations depending on implementation) | Item-in-hand based; permissions may vary.                      |
| `SpeedCommandProvider`           | Walk/fly/general speed commands                                                 | Implementation-specific args and limits.                       |
| `TeleportCommandsProvider`       | Teleport commands (tp/tphere/tppos and related variants)                        | Large command set; see provider for exact subcommands/aliases. |
| `TimeCommandProvider`            | Time control commands (day/night/time/ptime variants)                           | Implementation-specific command names and scopes.              |

## Permissions

Permissions are defined in `EssentialPerms` and/or registered inline by individual command providers (namespace generally under `sunlight.essential...` / `sunlight.essential.command...`).

Common patterns include:

* Per-command permissions (self usage)
* `.others` permissions for targeting other players
* Bypass permissions for costs/cooldowns/restrictions where applicable

> For the exact permission node of a specific essential command, check `EssentialPerms`, the corresponding provider class, and the generated `commands/<providerId>.yml`.

## Configuration

* **Settings:** `EssentialSettings.java` and generated module `settings.yml` (module-wide options)
* **Language:** `EssentialLang.java`
* **Placeholders:** `EssentialPlaceholders.java`
* **Properties:** `EssentialProperties` (e.g. `GOD`)

The Essential module may also include listeners/core helpers (e.g. invulnerability handling) and additional behavior controlled by settings.

## Notes

* Because Essential contains many commands, this wiki documents it as a command-bundle module. Your generated `commands/*.yml` files and in-game help are the best runtime reference for the final enabled command set on your server.
* Some commands may depend on NMS/internals, permissions plugins, or optional integrations.
* Use the Settings module integration where available (e.g. god/speed/jump-related toggles can be surfaced there depending on configuration).
