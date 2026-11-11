# Playtime Module

Tracks player playtime, supports goals/rewards/reminders, period-based progress, calendar UI, and placeholders.

- **Module ID:** `playtime`
- **Display name:** `Playtime`
- **Module directory:** `moduleImpl/playtime/`

## Commands

Implemented by `PlaytimeCommandProvider`.

Command functionality includes (as implemented):
- Viewing playtime (self/others)
- Admin management and goal-related operations
- Calendar/editor/UI access for playtime goals and tracking

> See `moduleImpl/playtime/command/PlaytimeCommandProvider.java` and `commands/<providerId>.yml` for exact command names, arguments, and aliases.

## Permissions

Defined in `moduleImpl/playtime/config/PlaytimePerms.java`.

Permission areas typically include:
- Playtime view commands
- Admin/management commands (goals, rewards, edits)
- Bypass or special access permissions where applicable

## Configuration

- **Settings:** `moduleImpl/playtime/PlaytimeSettings.java`
- **Language:** `moduleImpl/playtime/config/PlaytimeLang.java`
- **Placeholders:** `moduleImpl/playtime/PlaytimePlaceholders.java`

Configuration includes:
- Playtime goal definitions per period
- Rewards and reminder windows for goals
- Calendar display/material settings (e.g. empty/low/medium/high day indicators and thresholds)
- Storage/data handling options

Example calendar material keys observed in settings:
- `Playtime.Calendar.empty` (e.g. `GRAY_STAINED_GLASS_PANE`)
- `Playtime.Calendar.low` (e.g. `LIME_STAINED_GLASS_PANE`)
- `Playtime.Calendar.medium` (e.g. `YELLOW_STAINED_GLASS_PANE`)
- Additional high/threshold-related settings

## Placeholders

Registered by `PlaytimePlaceholders` for playtime totals, periods, goals/progress, and related display values.

## GUI and data

- Menus/dialogs for playtime viewing, goal/calendar UI, and editor flows
- Data repositories/classes for tracking and persisting playtime and goal progress

## Notes

- Playtime rewards/reminders can be integrated with player notification preferences (including Settings module reminder toggles where supported).
- Exact placeholder keys and goal config structure should be verified against `PlaytimePlaceholders` and generated `playtime/settings.yml`.
