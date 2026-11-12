# Friends Module

Provides a friend system with requests, friend lists, favorites, blocking, private messaging shortcuts, teleport/jump features, admin tools, GUIs, and placeholders.

- **Module ID:** `friends`
- **Display name:** `Friends`
## Commands

> Command details are implemented in common/admin providers and configurable via `commands/<provider>.yml`.

### Common commands
Typical friend command actions include:
- `add <player>` — send friend request
- `accept <player/request>` — accept request
- `decline <player/request>` — decline request
- `cancel <player/request>` — cancel outgoing request
- `remove <friend>` — remove friend
- `list` — view friends
- `pending` — view pending requests
- `blocked` — view block list
- `block <player>` / `unblock <player>`
- `favorite <friend>` — toggle favorite
- `toggle` / `on` / `off` — friend system/request/notification preferences (as implemented)
- `jump <friend>` — teleport to friend (if allowed)
- `message <friend> <text>` — friend message shortcut

### Admin commands
Admin provider includes management operations such as:
- `info <player/friendship>`
- `add <player> <player>`
- `remove <player> <player>`
- `unblock <player> <player>`

> Check your server command configuration and in-game help for the exact command tree and arguments.

## Permissions

Common permission areas:
- Friend command usage (requests, list, block, jump, message, toggles)
- Admin management commands
- Bypass/limit-related permissions where applicable
- Privacy/teleport/request controls may also be exposed through Settings integration

## Configuration

Main settings are available in the module's `settings.yml`.

Notable feature areas supported by the module:
- Rank-based friend and pending-request limits
- Request cooldowns and daily/per-target caps
- Block list (including offline block behavior that can remove friendship)
- Privacy toggles (requests, login alerts, friend teleport) integrated with player settings
- Login/logout friend alerts

## GUI and dialogs

- Friend list GUI
- Per-friend options menu
- Pending requests view
- Block list view
- Settings screen/dialogs

## Placeholders

Placeholders are available for this module when it is enabled.

## Notes

- Friends data is persisted via module data/repository classes.
- Some friend preferences can be managed through the Settings module UI when enabled.
- Teleport/jump and messaging features depend on permissions, privacy settings, and module checks.