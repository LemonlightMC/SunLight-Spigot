---
description: "Overview of all SunLight modules for Minecraft servers, including module IDs, configuration locations, command setup, and links to detailed module documentation."
---
# Modules Overview

SunLight's functionality is split into modules. Each module can be enabled/disabled independently in `modules.yml`.

## How modules work

* **Module registry:** modules are registered in `SunLightPlugin`.
* **Enable/disable:** `Modules.<id>.Enabled` in `modules.yml`.
* **Display metadata:** `Modules.<id>.Name` and `Modules.<id>.Prefix`.
* **Settings:** each module stores settings in its own `settings.yml` (path managed by the module).
* **Menus/UI:** GUI layouts (when used) are stored under the module's `menu/` folder.
* **Command config:** module command providers use `commands/<providerId>.yml` for aliases/enabled/cooldown/cost overrides.

## Module list

| Module              | ID (registered)  | Directory       | Notes                                                         |
| ------------------- | ---------------- | --------------- | ------------------------------------------------------------- |
| AFK                 | `afk`            | `afk`           | AFK detection, states, actions, placeholders                  |
| Back Location       | `back_location`  | `backlocation`  | `/back` and death return support                              |
| Bans                | `bans`           | `bans`          | Punishments, history, lists, alts                             |
| Chat                | `chat`           | `chat`          | Channels, PM/mail/mentions/spy/moderation                     |
| Death Messages      | `death_messages` | `deathmessages` | Custom death message handling                                 |
| Essential           | `essential`      | `essential`     | General utility command bundle                                |
| Freeze              | `freeze`         | `freeze`        | Freeze players with restrictions                              |
| Friends             | `friends`        | `friends`       | Friend system with GUI and requests                           |
| Glow                | `glow`           | `glow`          | Glow colors/effects                                           |
| Greetings           | `greetings`      | `greetings`     | Join/quit/first-join messages                                 |
| Homes               | `homes`          | `homes`         | Player homes, limits, GUI                                     |
| Inventories         | `inventories`    | `inventories`   | Inventory/enderchest/container tools; some features NMS-gated |
| Kits                | `kits`           | `kits`          | Kits with GUI/editor/cooldowns/costs                          |
| Links               | `links`          | `links`         | Server links with commands/editor                             |
| Nametags            | `nametags`       | `nametags`      | Requires TAB/TAB-Bridge load condition                        |
| Nerf Phantoms       | `nerf_phantoms`  | `nerfphantoms`  | Phantom spawn/damage/health controls                          |
| Nick                | `nick`           | `nick`          | Nickname system with validation                               |
| Player Warps        | `playerwarps`    | `playerwarps`   | Player-created warps with management/moderation               |
| Playtime            | `playtime`       | `playtime`      | Playtime tracking, goals, rewards, calendar/UI                |
| Profiles            | `profiles`       | `profiles`      | Player profile scopes/switching                               |
| PTP                 | `ptp`            | `ptp`           | Player teleport requests/invites                              |
| Reports             | `reports`        | `reports`       | Player reporting and staff workflow                           |
| RTP                 | `rtp`            | `rtp`           | Random teleportation                                          |
| Scheduler           | `scheduler`      | `scheduler`     | Announcer/broadcast scheduler                                 |
| Settings            | `settings`       | `settings`      | Player preferences GUI/panels                                 |
| Socials             | `socials`        | `socials`       | Discord relay via DiscordSRV; legacy socials placeholders     |
| Spawns              | `spawns`         | `spawns`        | Server spawns and spawn rules                                 |
| Texts (Custom Text) | `custom_text`    | `texts`         | Configurable text pages and dynamic commands                  |
| Timer               | `timer`          | `timer`         | Global/personal timers and stopwatches                        |
| Vanish              | `vanish`         | `vanish`        | Vanish with visibility/bossbar/fake messages                  |
| Voting              | `voting`         | `voting`        | Polls/votes with advanced rules and workflows                 |
| Warmups             | `warmups`        | `warmups`       | Warmup delays/cancellation for teleports                      |
| Warps               | `warps`          | `warps`         | Server warps with GUI and permissions                         |

## Version differences (official wiki vs this source)

The official SunLight wiki/downloads may document a different release than this repository. For example, public docs for some versions list modules such as **Scoreboard**, **TAB**, **Worlds**, and **Items**, or describe **Extras** as an active module with different features. In this source tree:

* **Registered modules** are the ones listed above.
* **Extras** code exists in `moduleImpl/extras/` but is not registered by default (see [Extras (Unregistered)](extras.md)).
* If a module appears on the public wiki but not in this list, it is not part of this build unless added/registered in source.

## Unregistered module code

* **Extras** (`moduleImpl/extras/`) exists in source but is **not registered** in `SunLightPlugin.registerModules()`. See [Extras (Unregistered)](extras.md).

## Documentation notes

* **Commands:** documented from command providers/source where available. Exact runtime aliases/labels may be overridden in `commands/<providerId>.yml`.
* **Permissions:** listed from permission classes and/or command registration code.
* **Configuration:** key options and defaults are documented from settings/config classes; generated files may include additional comments or migrated keys.
* **Placeholders:** module placeholders and dynamic patterns are documented where discoverable in source.
* **Core behavior:** SunLight core provides minimal commands/placeholders by itself; most functionality comes from modules.
* Some modules are large (Chat, Essential, Reports, Voting, etc.). This wiki aims to cover all primary commands, permission namespaces/nodes, config areas, and important behavior.
