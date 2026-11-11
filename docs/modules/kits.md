# Kits Module

Provides item kits with cooldowns, costs, previews, GUI editor/list menus, bind items, and admin/player commands.

- **Module ID:** `kits`
- **Display name:** `Kits`
- **Module directory:** `moduleImpl/kits/`
- **Provider ID:** `kits`
- **API/provider:** implements `KitsProvider`

## Commands

> Configurable in `commands/kits.yml`.

Root command: `/kits` (alias `kit`)

| Subcommand | Permission | Description | Notes |
| --- | --- | --- | --- |
| `editor` | `sunlight.kits.command.kits.editor` | Opens kit editor/management UI. | Admin/staff tool. |
| `get <kit>` | `sunlight.kits.command.kits.get` | Claims/receives a kit. | Cooldown/cost/permission checks apply. |
| `give <kit> <player>` | `sunlight.kits.command.kits.give` | Gives a kit to a player. | Bypasses normal claim flow as implemented. |
| `list` | `sunlight.kits.command.kits.list` | Opens/lists available kits. | GUI/list behavior depends on settings. |
| `preview <kit>` | `sunlight.kits.command.kits.preview` | Previews kit contents. |  |
| `reset_cooldown <kit> [player]` | `sunlight.kits.command.kits.resetcooldown` | Resets kit cooldown. | Target handling per implementation. |
| `set_cooldown <kit> <time> [player]` | `sunlight.kits.command.kits.setcooldown` | Sets kit cooldown/time state. |  |

## Permissions

### Command permissions
| Permission | Description |
| --- | --- |
| `sunlight.kits.command.kits.root` (or module command root, if used) | Root access |
| `sunlight.kits.command.kits.editor` | Kit editor |
| `sunlight.kits.command.kits.get` | Claim/get kits |
| `sunlight.kits.command.kits.give` | Give kits to players |
| `sunlight.kits.command.kits.list` | List kits |
| `sunlight.kits.command.kits.preview` | Preview kits |
| `sunlight.kits.command.kits.resetcooldown` | Reset kit cooldowns |
| `sunlight.kits.command.kits.setcooldown` | Set kit cooldowns |

### Bypass and kit access
| Permission | Description |
| --- | --- |
| `sunlight.kits.bypass.cost` | Bypass kit claim cost |
| `sunlight.kits.bypass.cooldown` | Bypass kit cooldown |
| `sunlight.kits.kit.<id>` (namespace pattern) | Access/use a specific kit (as enforced by kit permission checks) |

> See `KitsPerms` for the exact node names present in your build.

## Configuration

Source: `moduleImpl/kits/config/KitsSettings.java`

| Key | Default | Description |
| --- | --- | --- |
| Data/kit save intervals | (see settings class) | Controls how often kit/user kit data is saved. |
| Bind items | (see settings class) | Enables bind-item behavior for kits where configured. |
| Hide no-permission kits in GUI | (see settings class) | Hides kits the player cannot access from GUI/list views. |

Kit definitions include items, cooldowns, costs, display/icon settings, permissions, and other properties managed via editor/data files.

## Placeholders

Source: `KitsPlaceholders`

- `%kits_is_on_cooldown%`
- `%kits_is_available%`
- `%kits_cooldown_raw%`
- `%kits_cooldown%`
- Additional per-kit placeholders may be available (context/payload-based).

## GUI and data

- Menus: kit list menu, kit preview menu, kit editor flows/dialogs
- Data: kit definitions and user cooldown/claim data managed by kit data classes/repositories

## Notes

- Use per-kit permissions to control rank-based access.
- Costs use economy integration when configured.
- Cooldown and cost bypass permissions are intended for staff/admin use.
