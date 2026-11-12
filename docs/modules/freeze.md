---
description: "Guide to the Freeze module in SunLight for Minecraft servers, covering commands, permissions, configuration, features, and usage for admins and players."
---

# Freeze

Allows staff to freeze players in place with configurable restrictions on movement, interaction, combat, inventory, chat, commands, and more.

* **Module ID:** `freeze`
* **Display name:** `Freeze`
* **Module directory:** `moduleImpl/freeze/`
* **Provider ID:** `freeze`

## Commands

> Configurable in `commands/freeze.yml`.

| Command                           | Permission                       | Description                                   | Usage / notes                                   |
| --------------------------------- | -------------------------------- | --------------------------------------------- | ----------------------------------------------- |
| `/freeze` (root, alias `freezes`) | `sunlight.freeze.command.freeze` | Toggles freeze state (default root behavior). | `[target]`                                      |
| `/freeze toggle [target]`         | `sunlight.freeze.command.freeze` | Toggles freeze state.                         | Others: `sunlight.freeze.command.freeze.others` |
| `/freeze on [target]`             | `sunlight.freeze.command.freeze` | Freezes target/self.                          | Others: `...freeze.others`                      |
| `/freeze off [target]`            | `sunlight.freeze.command.freeze` | Unfreezes target/self.                        | Others: `...freeze.others`                      |

## Permissions

| Permission                              | Description                                                                                    |
| --------------------------------------- | ---------------------------------------------------------------------------------------------- |
| `sunlight.freeze.command.freeze`        | Use freeze commands                                                                            |
| `sunlight.freeze.command.freeze.others` | Freeze/unfreeze other players                                                                  |
| `sunlight.freeze.bypass.immune`         | Immune to freeze actions (and immune check is enforced unless sender also has this permission) |

## Configuration

Source: `moduleImpl/freeze/config/FreezeConfig.java`

The Freeze module provides configurable restrictions applied while a player is frozen (as indicated by module behavior and settings class), such as:

* Movement/teleport restrictions
* Damage dealing/receiving restrictions
* Block break/place restrictions
* Interaction restrictions
* Inventory/item use/drop restrictions
* Chat restrictions
* Command restrictions and allowed-command exceptions
* Unfreeze-on-quit behavior

> Check the generated `freeze/settings.yml` for exact keys, defaults, and any additional options in your build.

## Important notes and limitations

* Targeting other players requires `sunlight.freeze.command.freeze.others`.
* Players with `sunlight.freeze.bypass.immune` are protected from being frozen by non-immune senders (per command implementation).
* Language/feedback messages are defined in `FreezeLang`.
