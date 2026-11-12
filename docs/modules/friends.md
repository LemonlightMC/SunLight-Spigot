---
description: "Guide to the Friends module in SunLight for Minecraft servers, covering commands, permissions, configuration, features, and usage for admins and players."
---

# Friends

Provides a friend system with requests, friend lists, favorites, blocking, private messaging shortcuts, teleport/jump features, admin tools, GUIs, and placeholders.

* **Module ID:** `friends`
* **Display name:** `Friends`
* **Module directory:** `moduleImpl/friends/`
* **Provider IDs:** `FriendCommonCommandProvider`, `FriendAdminCommandProvider`

## Commands

> Command details are implemented in common/admin providers and configurable via `commands/<provider>.yml`.

### Common commands

Typical friend command actions include:

* `add <player>` — send friend request
* `accept <player/request>` — accept request
* `decline <player/request>` — decline request
* `cancel <player/request>` — cancel outgoing request
* `remove <friend>` — remove friend
* `list` — view friends
* `pending` — view pending requests
* `blocked` — view block list
* `block <player>` / `unblock <player>`
* `favorite <friend>` — toggle favorite
* `toggle` / `on` / `off` — friend system/request/notification preferences (as implemented)
* `jump <friend>` — teleport to friend (if allowed)
* `message <friend> <text>` — friend message shortcut

### Admin commands

Admin provider includes management operations such as:

* `info <player/friendship>`
* `add <player> <player>`
* `remove <player> <player>`
* `unblock <player> <player>`

> See `FriendCommonCommandProvider` and `FriendAdminCommandProvider` for exact command tree, aliases, and arguments.

## Permissions

Defined in `moduleImpl/friends/config/FriendsPerms.java`.

Common permission areas:

* Friend command usage (requests, list, block, jump, message, toggles)
* Admin management commands
* Bypass/limit-related permissions where applicable
* Privacy/teleport/request controls may also be exposed through Settings integration

## Configuration

* **Settings:** `FriendsSettings.java` (limits, cooldowns, caps, privacy defaults/behavior, GUI options, etc.)
* **Language:** `FriendsLang.java`
* **Placeholders:** `FriendsPlaceholders.java`
* **Properties:** `FriendsProperties.java`

Notable feature areas supported by the module:

* Rank-based friend and pending-request limits
* Request cooldowns and daily/per-target caps
* Block list (including offline block behavior that can remove friendship)
* Privacy toggles (requests, login alerts, friend teleport) integrated with player settings
* Login/logout friend alerts

## GUI and dialogs

* Friend list GUI
* Per-friend options menu
* Pending requests view
* Block list view
* Settings screen/dialogs

## Placeholders

Registered by `FriendsPlaceholders` (friend counts/status/relationship-related placeholders as implemented).

## Important notes and limitations

* Friends data is persisted via module data/repository classes.
* Some friend preferences can be managed through the Settings module UI when enabled.
* Teleport/jump and messaging features depend on permissions, privacy settings, and module checks.
