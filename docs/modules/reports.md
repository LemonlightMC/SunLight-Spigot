# Reports Module

Provides a player reporting system with submission flow, staff triage/management, claim/release/resolve workflows, notifications, cooldowns, and data persistence.

- **Module ID:** `reports`
- **Display name:** `Reports`
## Commands

Implemented by multiple command providers:
- — player report submission
- — staff/admin report management

Command capabilities typically include:
- Creating/submitting reports
- Listing/viewing reports
- Claiming/releasing reports
- Resolving/closing/deleting reports
- Teleporting to reporter/target or reviewing report details (as implemented)

> See command provider classes and generated `commands/*.yml` files for exact command names, aliases, and arguments.

## Permissions

Permission areas typically include:
- Report submission permissions
- Staff viewing/management permissions (list, claim, release, resolve, delete)
- Notification/alert permissions
- Bypass permissions for cooldowns/limits/restrictions where applicable

## Configuration

- **Config:**

Configuration areas include:
- Report categories and submission rules
- Cooldowns, open-report caps, daily limits, duplicate prevention, length limits
- Staff workflow options (claiming, notes, resolution behavior)
- Notifications/announcements and reward handling
- Auto-close/stale report behavior and history/statistics settings
- Storage/database options

## Events and workflow

The module emits/handles report-related events such as:
- `PlayerReportEvent`
- Claim/release/resolve/delete report events (API/core events as implemented)

Workflow features (from module implementation scope):
- Staff triage GUI and report views
- Case grouping/repeat report handling
- Staff notes/trails
- Reward payout on justified outcomes (staff resolution or punishment outcomes, as implemented)

## UI and data

- Menus/dialogs for submission, staff review, and management
- Models/repositories/data managers for report storage and queries
- Utilities such as report cooldown handling

## Placeholders

Placeholders are available for this module when it is enabled.

## Notes

- Reports can integrate with Bans/Socials/notification flows depending on configuration.
- Database-backed storage enables cross-server/report persistence scenarios when configured appropriately.
- Review staff permissions carefully to separate report submission from moderation actions.