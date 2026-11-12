# Requirements & Dependencies

## Server compatibility

| Server Version | Paper | Spigot | Folia | Java |
| -------------- | :---: | :----: | :---: | :--: |
| 26.3           |   ✔️  |   ✔️   |   ❌   |  25  |
| 26.2           |   ✔️  |   ✔️   |   ❌   |  25  |
| 26.1.X         |   ✔️  |   ✔️   |   ❌   |  25  |
| 1.21 - 1.21.11 |   ✔️  |   ✔️   |   ❌   |  25  |

* Versions/platforms not listed above are not officially supported.
* Check the official FAQ for known issues/incompatibilities: [https://nightexpressdev.com/sunlight/faq/](https://nightexpressdev.com/sunlight/faq/)

## Required dependency

* [**NightCore**](https://nightexpressdev.com/nightcore/) — required framework for SunLight to run.

## Optional / soft dependencies

Declared in `plugin.yml` (`softdepend`):

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

* **Nametags** — registered with a TAB/TAB-Bridge load condition (`LoadCondition.tabOrBridge`). It may require PlaceholderAPI when TAB is absent but TAB-Bridge is present (per implementation).
* **Inventories** — portable container commands/features are NMS-gated.
* **Socials** — Discord relay requires DiscordSRV to be installed and hooked.
* **Timer** — scoreboard display channel may require TAB backend support.

## Libraries

* `org.apache.commons:commons-text:1.10.0` (declared in `plugin.yml`)
