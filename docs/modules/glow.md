# Glow Module

Gives players a glowing outline with static or phased (animated) color effects, GUI controls, presets/phases management, and nametag integration via TAB.

- **Module ID:** `glow`
- **Display name:** `Glow`
- **Provider ID:** `glow`

## Commands

> Configurable in `commands/glow.yml`.

| Command | Permission | Description | Usage / notes |
| --- | --- | --- | --- |
| `/glow` (root) | `sunlight.glow.command.glow.root` | Glow command root. | |
| `/glow color <color/effect> [target]` | `sunlight.glow.command.glow.color` | Set glow color/effect. | `.others` variant available for targeting others. |
| `/glow clear [target]` (unglow) | `sunlight.glow.command.glow.clear` | Remove glow. | `.others` variant available. |
| `/glow list` (glowlist) | `sunlight.glow.command.glow.list` | List available glow colors/effects. | |
| `/glow on [target]` | `sunlight.glow.command.glow.on` | Enable glow state. | `.others` variant available. |
| `/glow off [target]` | `sunlight.glow.command.glow.off` | Disable glow state. | `.others` variant available. |
| `/glow phase...` | `sunlight.glow.command.glow.phase` | Manage phased effects (`create`, `add`, `remove`, `duration`, `clear`, `list`). | Admin/management command group. |
| `/glow preset...` | `sunlight.glow.command.glow.preset` | Manage presets (`load`, `save`, `delete`, `list`). | Admin/management command group. |

> Check in-game help for the exact subcommand arguments in your build.

## Permissions

| Permission | Description |
| --- | --- |
| `sunlight.glow.command.glow.root` | Glow root command |
| `sunlight.glow.command.glow.color` | Set glow color/effect |
| `sunlight.glow.command.glow.clear` | Clear glow |
| `sunlight.glow.command.glow.list` | List glow effects |
| `sunlight.glow.command.glow.phase` | Manage glow phases |
| `sunlight.glow.command.glow.preset` | Manage glow presets |
| `sunlight.glow.command.glow.on` | Enable glow |
| `sunlight.glow.command.glow.off` | Disable glow |
| `...others` variants | Target other players (where registered by provider) |

## Configuration

- **Update interval:** controls how often phased/animated glow effects update.
- **Restore on join:** restores previous glow state/effect on player join.
- **Effects map:** defines configured effects, including:
 - `STATIC` effects
 - `PHASED` effects with phase sequences/timings

## Placeholders

- `%glow_state%`
- `%glow_color%`

## GUI and implementation

- GUI/menu:
- Dialogs and handlers/listeners manage color selection, effect application, and state updates.
- Nametag color rendering integration is handled through TAB-related nametag systems (Glow/Nametags ecosystem).

## Notes

- Glow appearance in nametags depends on TAB/Nametags integration being present and configured.
- Presets/phases allow reusable and animated glow styles for ranks, events, or cosmetics.