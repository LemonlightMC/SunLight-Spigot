# Nick Module

Allows players to set custom nicknames with validation rules, color permissions, cooldown/cost controls, and optional tablist/display application.

- **Module ID:** `nick`
- **Display name:** `Nick`
- **Provider ID:** `nickname`

## Commands

> Configurable in `commands/nickname.yml`.

| Command | Permission | Description | Usage / notes |
| --- | --- | --- | --- |
| `/nickname` (root, aliases `nick`) | `sunlight.nick.command.nick.root` | Nick command root. | |
| `/nickname change <name>` | `sunlight.nick.command.nick.change` | Set your own nickname. | Player-only; `<name>` is greedy (can include spaces as supported). |
| `/nickname clear [target]` | `sunlight.nick.command.nick.clear` | Clear nickname. | Others: `sunlight.nick.command.nick.clear.others`. |
| `/nickname set <player> <name>` | `sunlight.nick.command.nick.set` | Set another player's nickname. | Supports silent flag parsing in name/arguments as implemented. |

## Permissions

| Permission | Category | Description |
| --- | --- | --- |
| `sunlight.nick.command.nick.root` | Command | Nick root command |
| `sunlight.nick.command.nick.change` | Command | Change own nickname |
| `sunlight.nick.command.nick.set` | Command | Set nickname for others |
| `sunlight.nick.command.nick.clear` | Command | Clear own nickname |
| `sunlight.nick.command.nick.clear.others` | Command | Clear nickname for others |
| `sunlight.nick.command.nick.colors` | Command | Use colors/formatting in nicknames (as enforced by module logic) |
| `sunlight.nick.bypass.nick.words` | Bypass | Bypass banned words filter |
| `sunlight.nick.bypass.nick.regex` | Bypass | Bypass regex validation |
| `sunlight.nick.bypass.nick.length` | Bypass | Bypass length limits |
| `sunlight.nick.bypass.change.cost` | Bypass | Bypass nickname change cost |
| `sunlight.nick.bypass.change.cooldown` | Bypass | Bypass nickname change cooldown |

## Configuration

| Key | Default | Description |
| --- | --- | --- |
| `Nick.Length.Min` | `3` | Minimum nickname length. |
| `Nick.Length.Max` | `16` | Maximum nickname length. |
| `Nick.Banned-Words` | `[admin, ass, shit]` | Banned words list. |
| `Nick.Banned-Words-Exact` | `false` | Exact-match behavior for banned words (otherwise matching behavior as implemented). |
| `Nick.Regex-Pattern` | `[a-zA-Zа-яА-Я0-9_\s]*` | Allowed nickname pattern; invalid regex disables check with warning. |
| `Nick.Tablist.Enabled` | `true` | Apply nickname in tablist/display contexts. (Migrated from legacy key `Nick.Apply_To_Tablist`.) |
| `Nick.Change.Cooldown` | `0` | Cooldown for nickname changes (`-1` one-time, `0` disabled). |
| `Nick.Change.Cost` | `0D` | Cost to change nickname. |

## Placeholders

- `%nick_name%`
- `%essential_custom_name%` (legacy compatibility placeholder)

## Events

- `PlayerNickChangeEvent` (fired on nickname changes)

## Notes

- Nick validation includes length, banned words, regex, and permission checks.
- Cooldown/cost can be bypassed with dedicated permissions.
- Display/tablist application depends on module settings and server integration behavior.