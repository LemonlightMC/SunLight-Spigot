# Timer Module

Provides global and personal timers/stopwatches with schedules, visibility rules, display channels, actions, persistence, and editor GUIs.

- **Module ID:** `timer`
- **Display name:** `Timer`
- **Module directory:** `moduleImpl/timer/`
- **Provider IDs:** `timer`, `ptimer` (personal)
- **API/provider:** implements `TimerProvider`

## Commands

> Exact aliases/arguments may be customized in `commands/timer.yml` and `commands/ptimer.yml`.

### Global timers (`/timer`, provider `TimerCommandProvider`)
- **Root:** `/timer`
- **Permission:** `sunlight.timer.command.timer`
- **Behavior:** root opens timer list for players (as implemented)

Common subcommands:
- `list [filter]`
- `info <id>`
- `create <id> <countdown|stopwatch> <duration>`
- `start <id>` / `pause <id>` / `resume <id>` / `reset <id>` / `stop <id>`
- `set <id> <time>` / `add <id> <time>` / `remove <id> <time>`
- `pauseall` / `resumeall` / `stopall`
- `copy <id> <newId>`
- `delete <id> [confirm]`
- `editor [id]`
- `reload`
- Nested `personal ...` subcommands (where exposed)

### Personal timers (`/ptimer`, provider `PersonalTimerCommandProvider`)
- **Root:** `/ptimer`
- **Permission:** `sunlight.timer.command.ptimer`
- **Behavior:** root opens personal timer list for players

Common subcommands:
- `list`
- `info <id>`
- `create <kind> <duration> <id>`
- `start|pause|resume|reset|stop <id>`
- `set|add|remove <id> <time>`
- `delete <id> [confirm]`
- `editor [id]`

## Permissions

Source: `moduleImpl/timer/config/TimerPerms.java` (namespace `sunlight.timer.*`)

### Global command permissions
| Permission | Description |
| --- | --- |
| `sunlight.timer.command.timer` | Access `/timer` root |
| `sunlight.timer.command.timer.list` | List timers |
| `sunlight.timer.command.timer.info` | View timer info |
| `sunlight.timer.command.timer.start` | Start timer |
| `sunlight.timer.command.timer.pause` | Pause timer |
| `sunlight.timer.command.timer.resume` | Resume timer |
| `sunlight.timer.command.timer.reset` | Reset timer |
| `sunlight.timer.command.timer.stop` | Stop timer |
| `sunlight.timer.command.timer.delete` | Delete timer |
| `sunlight.timer.command.timer.create` | Create timer |
| `sunlight.timer.command.timer.reload` | Reload timer module/data |
| `sunlight.timer.command.timer.editor` | Open timer editor |
| `sunlight.timer.command.timer.set` | Set timer time |
| `sunlight.timer.command.timer.add` | Add time |
| `sunlight.timer.command.timer.remove` | Remove time |
| `sunlight.timer.command.timer.copy` | Copy timer |
| `sunlight.timer.command.timer.controlall` | Pause/resume/stop all timers |
| `sunlight.timer.command.timer.others` | Operate on others' timers/contexts where supported |
| `sunlight.timer.command.timer.viewpersonal` | View personal timers in global/admin contexts where supported |

### Personal timer permissions
| Permission | Description |
| --- | --- |
| `sunlight.timer.command.ptimer` | Access `/ptimer` root |
| `sunlight.timer.command.ptimer.list` | List personal timers |
| `sunlight.timer.command.ptimer.info` | View personal timer info |
| `sunlight.timer.command.ptimer.create` | Create personal timer |
| `sunlight.timer.command.ptimer.start` | Start personal timer |
| `sunlight.timer.command.ptimer.pause` | Pause personal timer |
| `sunlight.timer.command.ptimer.resume` | Resume personal timer |
| `sunlight.timer.command.ptimer.reset` | Reset personal timer |
| `sunlight.timer.command.ptimer.stop` | Stop personal timer |
| `sunlight.timer.command.ptimer.delete` | Delete personal timer |
| `sunlight.timer.command.ptimer.editor` | Edit personal timer |
| `sunlight.timer.command.ptimer.set` | Set personal timer time |
| `sunlight.timer.command.ptimer.add` | Add personal timer time |
| `sunlight.timer.command.ptimer.remove` | Remove personal timer time |

### Bypass
| Permission | Description |
| --- | --- |
| `sunlight.timer.bypass.visibility` | Bypass timer visibility restrictions |

## Features and configuration

The timer module is feature-rich and includes:

- **Timer kinds:** countdown and stopwatch
- **States:** start/pause/resume/reset/stop lifecycle
- **Scheduling/start modes:** immediate and scheduled start options
- **Visibility:** modes/permissions/world-based visibility rules
- **Display channels:** boss bar, action bar, title, subtitle, chat, scoreboard (TAB scoreboard backend when available)
- **Rules and triggers:** display rules, event triggers, action bundles
- **Persistence:** YAML-based runtime persistence (`TimerYamlIO`) for global timers and personal timer storage/services
- **Editors/UI:** list/editor menus and dialogs for creation, naming, duration, schedule, visibility, actions, rules, and triggers
- **XP bar handling:** optional XP bar display behavior (as implemented)

> See `timer/settings.yml`, timer files, and editor dialogs for all configurable options and defaults.

## Placeholders

- **Global:** `timer_*` placeholders (state, name, kind, formatted time, remaining, elapsed, seconds, progress, duration, count, etc.)
- **Personal:** `timer_personal_*` placeholders with similar fields for personal timers

## Notes

- Scoreboard display channel may require TAB integration/backend support.
- Personal timers are stored per-user (service/properties-based implementation).
- Use visibility permissions and bypass carefully for staff/admin-only timers.
