# Welcome to SunLight

**SunLight** is a modern, lightweight, all-in-one core plugin for Minecraft servers. It provides the essential tools every server needs through a modular system, so you can enable only the features your server actually uses.

This documentation is based on the **current source in this repository**. Public releases and the official project wiki may describe a different version or module set.

## Core features

- **Database support** — works with SQLite and MySQL
- **Modular design** — toggle features on or off in `modules.yml`
- **Lightweight** — focused, maintainable implementation
- **Highly customizable** — commands, GUIs, messages, and module settings can be tailored to your server
- **Command cooldowns & costs** — configurable per command/provider
- **Offline data support** — many actions can target players even when offline
- **MiniMessage support** — rich text formatting where supported
- **PlaceholderAPI support** — placeholders for modules and integrations where available

## Included modules (this build)

SunLight in this source tree registers **31 modules**:

| Category | Modules |
| --- | --- |
| Moderation & safety | [Bans](modules/bans.md), [Reports](modules/reports.md), [Freeze](modules/freeze.md), [Vanish](modules/vanish.md) |
| Chat & communication | [Chat](modules/chat.md), [Socials](modules/socials.md), [Links](modules/links.md), [Greetings](modules/greetings.md), [Texts](modules/texts.md) |
| Teleportation & travel | [Homes](modules/homes.md), [Warps](modules/warps.md), [Player Warps](modules/playerwarps.md), [Spawns](modules/spawns.md), [RTP](modules/rtp.md), [Back Location](modules/backlocation.md), [PTP](modules/ptp.md), [Warmups](modules/warmups.md) |
| Utilities & server tools | [Essential](modules/essential.md), [Inventories](modules/inventories.md), [Kits](modules/kits.md), [Scheduler](modules/scheduler.md), [Timer](modules/timer.md) |
| Player customization & experience | [Nametags](modules/nametags.md), [Glow](modules/glow.md), [Nick](modules/nick.md), [Settings](modules/settings.md) |
| Quality of life & systems | [AFK](modules/afk.md), [Playtime](modules/playtime.md), [Friends](modules/friends.md), [Profiles](modules/profiles.md), [Voting](modules/voting.md), [Nerf Phantoms](modules/nerfphantoms.md), [Death Messages](modules/deathmessages.md) |

> Some modules have extra requirements or load conditions (for example, Nametags depends on TAB/TAB-Bridge). See [Requirements & Dependencies](getting-started/requirements.md) and [Modules Overview](modules/README.md).

## Requirements (quick overview)

- **Required dependency:** [NightCore](https://nightexpressdev.com/nightcore/)
- **Supported platforms/versions:** see [Requirements & Dependencies](getting-started/requirements.md) for the compatibility table and notes
- **Optional integrations:** PlaceholderAPI, LuckPerms, TAB/TAB-Bridge, ProtocolLib/packetevents, DiscordSRV, WorldGuard, GriefPrevention, and others depending on the module

## Get started

- [Installation](getting-started/installation.md) — install NightCore, add SunLight, and enable modules
- [Core Configuration](getting-started/core-configuration.md) — global settings, `/sunlight` admin command, and module management
- [Commands & Permissions System](getting-started/commands-and-permissions.md) — provider configs, command flags, permissions, and bypasses
- [Modules Overview](modules/README.md) — browse every module and its documentation

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
