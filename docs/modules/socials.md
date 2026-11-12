# Socials Module

Relays selected in-game events to Discord via DiscordSRV and provides compatibility placeholders for legacy socials link data.

- **Module ID:** `socials`
- **Display name:** `Socials`
## Commands

This module does not register dedicated commands.

## Permissions

| Permission | Description |
| --- | --- |
| `sunlight.socials` | Module permission constant (defined in ) |

## Configuration

### Discord relay
| Key | Default | Description |
| --- | --- | --- |
| `Socials.Discord.Relay.Join` | `true` | Relay player join events. |
| `Socials.Discord.Relay.Quit` | `true` | Relay player quit events. |
| `Socials.Discord.Relay.Death` | `true` | Relay player death events. |
| `Socials.Discord.Relay.Advancement` | `false` | Relay advancement events. |
| `Socials.Discord.Relay.Chat` | `false` | Relay chat messages (warns to avoid duplicates with DiscordSRV). |
| `Socials.Discord.Relay.ChannelId` | `""` | Target Discord channel ID; empty uses DiscordSRV main channel. |

### Relay formats
| Key | Default |
| --- | --- |
| `Format.Join` | `%player% joined the server` |
| `Format.Quit` | `%player% left the server` |
| `Format.Death` | `%player% died` |
| `Format.Advancement` | `%player% earned %advancement%` |
| `Format.Chat` | `<%player%> %message%` |

### Announcements
| Key | Default | Description |
| --- | --- | --- |
| `AnnounceReports` | `true` | Announce/report relay for report events (as configured/implemented). |
| `AnnouncePunishments` | `true` | Announce punishment events. |
| `Format.Report` | formatted report template | Report announcement format (`%reporter_name%`, `%target_name%`, `%category%`, `%report_id%`, etc.). |

> Verify exact key nesting in generated `settings.yml` for your build.

## Integration and behavior

- Requires **DiscordSRV** for Discord relay functionality; module hooks DiscordSRV when present.
- Relays configured events (join/quit/death/advancement/chat/reports) to Discord.
- Sends plain/stripped-formatted messages for Discord output where applicable.

## Compatibility placeholders

- Re-registers legacy `socials_<id>` placeholders by delegating to the **Links** module (resolved dynamically when Links data is available).

## Notes

- This module is a Discord bridge/relay, not the primary place to manage server links (use the Links module for link content/commands/editor).
- Enable chat relay carefully if DiscordSRV already handles chat to avoid duplicate messages.