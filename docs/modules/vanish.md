# Vanish Module

Hides players from others with a persistent vanish state, boss bar indicator, metadata flag support, and optional fake join/quit messages.

- **Module ID:** `vanish`
- **Display name:** `Vanish`
- **Module directory:** `moduleImpl/vanish/`
- **Provider ID:** `vanish`

## Commands

> Configurable in `commands/vanish.yml`.

| Command | Permission | Description | Usage / notes |
| --- | --- | --- | --- |
| `/vanish` (root, alias `v`) | `sunlight.vanish.command.vanish` | Vanish command root / toggle behavior (provider-defined). |  |
| `/vanish toggle [target]` | `sunlight.vanish.command.vanish` | Toggles vanish state. | Others: `sunlight.vanish.command.vanish.others` |

## Permissions

| Permission | Description |
| --- | --- |
| `sunlight.vanish.command.vanish` | Use vanish command/toggle |
| `sunlight.vanish.command.vanish.others` | Toggle vanish for other players |
| `sunlight.vanish.bypass.see` | See vanished players (bypass visibility hiding) |

## Configuration

Source: `moduleImpl/vanish/config/VanishConfig.java`

| Key | Default | Description |
| --- | --- | --- |
| `BarIndicator.Enabled` | `true` | Show boss bar indicator while vanished. |
| `Vanished.Title` | `"You're vanished!"` | Boss bar title for vanished players. |
| `Color` | `GREEN` | Boss bar color. |
| `Style` | `SEGMENTED_10` | Boss bar style. |
| `Vanish.Metadata-Key` | `"vanished"` | Metadata key set on vanished players (empty disables). |
| `Vanish.Suppress-Join-Quit-Messages` | `true` | Suppress real join/quit messages for vanish transitions where applicable. |
| `Fake-Messages.Enabled` | `true` | Enable fake join/quit messages on vanish toggle. |
| `Fake-Messages.On-Vanish` | `true` | Send fake quit message when vanishing. |
| `Fake-Messages.On-Unvanish` | `true` | Send fake join message when unvanishing. |

> Check generated `vanish/settings.yml` for exact key paths/nesting as written by the config system.

## Placeholder

- `%vanish_state%`

## Behavior notes

- Vanished players are hidden from viewers who do not have bypass permission.
- Boss bar and metadata are applied/removed with vanish state.
- Fake join/leave broadcasts can be emitted on toggle and may interact with Greetings module behavior.
- Integrates with other modules/systems that check vanish state (e.g. AFK, Settings).
