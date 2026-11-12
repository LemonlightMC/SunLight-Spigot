# Timer Module

Provides global and personal timers/stopwatches with schedules, visibility rules, display channels, actions, persistence, and editor GUIs.

- **Module ID:** `timer`
- **Display name:** `Timer`
- **Provider IDs:** `timer`, `ptimer` (personal)

## Commands

> Exact aliases/arguments may be customized in `commands/timer.yml` and `commands/ptimer.yml`.

### Global timers (`/timer`, provider )
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
- Nested `personal...` subcommands (where exposed)

### Personal timers (`/ptimer`, provider )
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

timer.*`)

| Permission | Category | Description |
| --- | --- | --- |
| `sunlight.timer.command.timer` | Global Command | Access `/timer` root |
| `sunlight.timer.command.timer.list` | Global Command | List timers |
| `sunlight.timer.command.timer.info` | Global Command | View timer info |
| `sunlight.timer.command.timer.start` | Global Command | Start timer |
| `sunlight.timer.command.timer.pause` | Global Command | Pause timer |
| `sunlight.timer.command.timer.resume` | Global Command | Resume timer |
| `sunlight.timer.command.timer.reset` | Global Command | Reset timer |
| `sunlight.timer.command.timer.stop` | Global Command | Stop timer |
| `sunlight.timer.command.timer.delete` | Global Command | Delete timer |
| `sunlight.timer.command.timer.create` | Global Command | Create timer |
| `sunlight.timer.command.timer.reload` | Global Command | Reload timer module/data |
| `sunlight.timer.command.timer.editor` | Global Command | Open timer editor |
| `sunlight.timer.command.timer.set` | Global Command | Set timer time |
| `sunlight.timer.command.timer.add` | Global Command | Add time |
| `sunlight.timer.command.timer.remove` | Global Command | Remove time |
| `sunlight.timer.command.timer.copy` | Global Command | Copy timer |
| `sunlight.timer.command.timer.controlall` | Global Command | Pause/resume/stop all timers |
| `sunlight.timer.command.timer.others` | Global Command | Operate on others' timers/contexts where supported |
| `sunlight.timer.command.timer.viewpersonal` | Global Command | View personal timers in global/admin contexts where supported |
| `sunlight.timer.command.ptimer` | Personal Command | Access `/ptimer` root |
| `sunlight.timer.command.ptimer.list` | Personal Command | List personal timers |
| `sunlight.timer.command.ptimer.info` | Personal Command | View personal timer info |
| `sunlight.timer.command.ptimer.create` | Personal Command | Create personal timer |
| `sunlight.timer.command.ptimer.start` | Personal Command | Start personal timer |
| `sunlight.timer.command.ptimer.pause` | Personal Command | Pause personal timer |
| `sunlight.timer.command.ptimer.resume` | Personal Command | Resume personal timer |
| `sunlight.timer.command.ptimer.reset` | Personal Command | Reset personal timer |
| `sunlight.timer.command.ptimer.stop` | Personal Command | Stop personal timer |
| `sunlight.timer.command.ptimer.delete` | Personal Command | Delete personal timer |
| `sunlight.timer.command.ptimer.editor` | Personal Command | Edit personal timer |
| `sunlight.timer.command.ptimer.set` | Personal Command | Set personal timer time |
| `sunlight.timer.command.ptimer.add` | Personal Command | Add personal timer time |
| `sunlight.timer.command.ptimer.remove` | Personal Command | Remove personal timer time |
| `sunlight.timer.bypass.visibility` | Bypass | Bypass timer visibility restrictions |

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