# Modules Overview

SunLight's functionality is split into modules. Each module can be enabled/disabled independently in `modules.yml`.

## How modules work

- **Module system:** each feature is provided by a module.
- **Enable/disable:** `Modules.<id>.Enabled` in `modules.yml`.
- **Display metadata:** `Modules.<id>.Name` and `Modules.<id>.Prefix`.
- **Settings:** each module stores settings in its own `settings.yml` (path managed by the module).
- **Menus/UI:** GUI layouts (when used) are stored under the module's `menu/` folder.
- **Command config:** module command providers use `commands/<providerId>.yml` for aliases/enabled/cooldown/cost overrides.

## Module list

| Module | ID (registered) | Directory | Notes |
| --- | --- | --- | --- |
| AFK | `afk` | `afk` | AFK detection, states, actions, placeholders |
| Back Location | `back_location` | `backlocation` | `/back` and death return support |
| Bans | `bans` | `bans` | Punishments, history, lists, alts |
| Chat | `chat` | `chat` | Channels, PM/mail/mentions/spy/moderation |
| Death Messages | `death_messages` | `deathmessages` | Custom death message handling |
| Essential | `essential` | `essential` | General utility command bundle |
| Freeze | `freeze` | `freeze` | Freeze players with restrictions |
| Friends | `friends` | `friends` | Friend system with GUI and requests |
| Glow | `glow` | `glow` | Glow colors/effects |
| Greetings | `greetings` | `greetings` | Join/quit/first-join messages |
| Homes | `homes` | `homes` | Player homes, limits, GUI |
| Inventories | `inventories` | `inventories` | Inventory/enderchest/container tools; some features depend on server support |
| Kits | `kits` | `kits` | Kits with GUI/editor/cooldowns/costs |
| Links | `links` | `links` | Server links with commands/editor |
| Nametags | `nametags` | `nametags` | Requires TAB/TAB-Bridge load condition |
| Nerf Phantoms | `nerf_phantoms` | `nerfphantoms` | Phantom spawn/damage/health controls |
| Nick | `nick` | `nick` | Nickname system with validation |
| Player Warps | `playerwarps` | `playerwarps` | Player-created warps with management/moderation |
| Playtime | `playtime` | `playtime` | Playtime tracking, goals, rewards, calendar/UI |
| Profiles | `profiles` | `profiles` | Player profile scopes/switching |
| PTP | `ptp` | `ptp` | Player teleport requests/invites |
| Reports | `reports` | `reports` | Player reporting and staff workflow |
| RTP | `rtp` | `rtp` | Random teleportation |
| Scheduler | `scheduler` | `scheduler` | Announcer/broadcast scheduler |
| Settings | `settings` | `settings` | Player preferences GUI/panels |
| Socials | `socials` | `socials` | Discord relay via DiscordSRV; legacy socials placeholders |
| Spawns | `spawns` | `spawns` | Server spawns and spawn rules |
| Texts (Custom Text) | `custom_text` | `texts` | Configurable text pages and dynamic commands |
| Timer | `timer` | `timer` | Global/personal timers and stopwatches |
| Vanish | `vanish` | `vanish` | Vanish with visibility/bossbar/fake messages |
| Voting | `voting` | `voting` | Polls/votes with advanced rules and workflows |
| Warmups | `warmups` | `warmups` | Warmup delays/cancellation for teleports |
| Warps | `warps` | `warps` | Server warps with GUI and permissions |

## Version differences

The official SunLight wiki or downloads for other versions may list different modules or features (for example Scoreboard, TAB, Worlds, or Items). The modules listed above are the ones documented for this build.

- **Extras** is not available in standard builds (see [Extras (Not available in standard builds)](extras.md)).
- If a module appears on another wiki page but not in this list, it may not be available in your build.

## Documentation notes

- **Commands:** exact command names, aliases, cooldowns, and costs can be customized in `commands/<providerId>.yml`.
- **Permissions:** refer to each module page for the most relevant permission nodes.
- **Configuration:** generated config files may include additional options or comments beyond what is summarized here.
- **Placeholders:** placeholder availability depends on enabled modules and integrations.
- Some modules are large (Chat, Essential, Reports, Voting, etc.). This documentation focuses on the main commands, permissions, settings, and behavior that admins and players need.