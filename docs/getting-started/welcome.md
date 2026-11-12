# Welcome to SunLight

**SunLight** is a modern, batteries-included, all-in-one core plugin with customizability and configurablity in mind. It provides the essential tools every server needs through a modular system, so you can enable only the features your server actually uses.

## Core features

- **Database support** — works with SQLite and MySQL
- **Modular design** — toggle features on or off in `modules.yml`
- **Lightweight** — focused, maintainable implementation
- **Highly customizable** — commands, GUIs, messages, and module settings can be tailored to your server
- **Command cooldowns & costs** — configurable per command/provider
- **Offline data support** — many actions can target players even when offline
- **MiniMessage support** — rich text formatting where supported
- **PlaceholderAPI support** — placeholders for modules and integrations where available

## Get started

- [Installation](getting-started/installation.md) — install NightCore, add SunLight, and enable modules
- [Core Configuration](getting-started/core-configuration.md) — global settings, `/sunlight` admin command, and module management
- [Commands & Permissions System](getting-started/commands-and-permissions.md) — provider configs, command flags, permissions, and bypasses
- [Modules Overview](modules/README.md) — browse every module and its documentation

## Included modules

| Category                          | Modules                                                                                                                                                                                                                                   |
| --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Moderation                        | [Bans](modules/bans.md), [Reports](modules/reports.md), [Freeze](modules/freeze.md), [Vanish](modules/vanish.md)                                                                                                                          |
| Chat & Communication              | [Chat](modules/chat.md), [Socials](modules/socials.md), [Links](modules/links.md), [Greetings](modules/greetings.md), [Texts](modules/texts.md)                                                                                           |
| Teleportation & Travel            | [Homes](modules/homes.md), [Warps](modules/warps.md), [Player Warps](modules/playerwarps.md), [Spawns](modules/spawns.md), [RTP](modules/rtp.md), [Back Location](modules/backlocation.md), [PTP](modules/ptp.md)                         |
| Utilities & Server tools          | [Essential](modules/essential.md) (basic server commands), [Inventories](modules/inventories.md) (Invsee), [Kits](modules/kits.md), [Scheduler](modules/scheduler.md), [Timer](modules/timer.md) (Stopwatch, Countdown)                   |
| Player Customization & Experience | [Nametags](modules/nametags.md), [Glow](modules/glow.md), [Nick](modules/nick.md), [Settings](modules/settings.md)                                                                                                                        |
| Quality of life & Systems         | [AFK](modules/afk.md), [Playtime](modules/playtime.md), [Friends](modules/friends.md), [Profiles](modules/profiles.md), [Voting](modules/voting.md), [Nerf Phantoms](modules/nerfphantoms.md), [Death Messages](modules/deathmessages.md) |

> Some modules have extra requirements or load conditions (for example, Nametags depends on TAB/TAB-Bridge). See [Requirements & Dependencies](getting-started/requirements.md) and [Modules Overview](modules/README.md).

<br>

## Compatibility

The following versions and platforms are supported:

| Server Version | Paper | Spigot | Folia | Java |
| -------------- | :---: | :----: | :---: | :--: |
| 26.3           |  ✔️   |   ✔️   |  ❌   |  25  |
| 26.2           |  ✔️   |   ✔️   |  ❌   |  25  |
| 26.1.X         |  ✔️   |   ✔️   |  ❌   |  25  |
| 1.21 - 1.21.11 |  ✔️   |   ✔️   |  ❌   |  25  |

Anything not listed in the compatibility table is NOT supported.
Make sure to check out all known issues and incompatibilities [here](https://nightexpressdev.com/sunlight/faq/).

## Required dependencies

- [**NightCore**](https://nightexpressdev.com/nightcore/) — required framework for SunLight to run.

## Optional / soft dependencies

| Plugin              | Purpose / related features                                                                         |
| ------------------- | -------------------------------------------------------------------------------------------------- |
| **TAB**             | Nametag rendering integration; required for Nametags/Glow-related rendering paths where applicable |
| **TAB-Bridge**      | Alternative/support integration for TAB-related nametag handling                                   |
| **UltimateTeams**   | Can contribute team prefix/name color to nametags                                                  |
| **LuckPerms**       | Rank/group resolution and contexts (e.g. AFK/profile-related integrations)                         |
| **PlaceholderAPI**  | Placeholder support and expansion integration                                                      |
| **ProtocolLib**     | Packet-related features for some modules/features (alternative to PacketEvents)                    |
| **packetevents**    | Packet-related features for some modules/features (alternative to ProtocolLib)                     |
| **DiscordSRV**      | Discord relay/integration (e.g. Socials, AFK/chat-related hooks where configured)                  |
| **WorldGuard**      | Protection/region-aware behavior in modules that check protected areas                             |
| **GriefPrevention** | Protection-aware behavior in modules that check claims                                             |

## Module-specific conditions

Some modules have additional load/runtime requirements:

- **Nametags** — registered with a TAB/TAB-Bridge load condition (`LoadCondition.tabOrBridge`). It may require PlaceholderAPI when TAB is absent but TAB-Bridge is present (per implementation).
- **Inventories** — portable container commands/features are NMS-gated.
- **Socials** — Discord relay requires DiscordSRV to be installed and hooked.
- **Timer** — scoreboard display channel may require TAB backend support.

## Downloads & support

- **Project page:** [nightexpressdev.com/sunlight](https://nightexpressdev.com/sunlight/)
- **Official downloads:** [SpigotMC](https://www.spigotmc.org/resources/67733/), [BuiltByBit](https://builtbybit.com/resources/89968/), [Modrinth](https://modrinth.com/plugin/sunlightcore/), [Hangar](https://hangar.papermc.io/NightExpress/SunLight/)
- **Source repository (this build):** [LemonlightMC/SunLight-Spigot](https://github.com/LemonlightMC/SunLight-Spigot)
- **Community/support:** [Discord](https://discord.gg/EwNFGsnGaW)
- **FAQ:** [nightexpressdev.com/sunlight/faq](https://nightexpressdev.com/sunlight/faq/)

## Support the project

SunLight is created and maintained by NightExpress. If you enjoy the plugin and want to support development, consider donating via [Ko-fi](https://ko-fi.com/nightexpress).

---

Continue with [Requirements & Dependencies](getting-started/requirements.md) or jump straight to the [Modules Overview](modules/README.md).
