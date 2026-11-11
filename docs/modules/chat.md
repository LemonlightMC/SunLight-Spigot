# Chat Module

Advanced chat management with channels, formatting, private messages/conversations, mail, mentions, spy, roleplay, item display, moderation (anti-caps/flood/profanity), and optional Discord relay integration.

- **Module ID:** `chat`
- **Display name:** `Chat`
- **Module directory:** `moduleImpl/chat/`
- **Provider IDs (key):** `chat-channel`, `chat-clearchat`, `chat-conversations`, `chat-mentions`, `chat-spy`, `chat-roleplay`, `chat-mail` (and related internal handlers)

## Main features

- **Channels:** join/leave channels, channel formats, permissions, cooldown behavior, default/fallback channels, and per-channel chat routing.
- **Chat formatting:** configurable format components and user/rank-based format definitions with fallback format.
- **Conversations / PMs:** private messages (`/tell`, `/msg`), reply (`/reply`), and conversation toggles with sound/format options.
- **Mail:** offline message delivery, inbox limits, expiry, database-backed storage.
- **Mentions:** player and group mentions with limits/cooldowns/formats.
- **Spy:** monitor chat, commands, and social/private messages (with logger and per-type controls).
- **Moderation:** anti-caps, anti-flood, profanity filtering, command/message cache behavior.
- **Item display:** configurable placeholder/format for showing held items in chat.
- **Roleplay:** `/me`-style command support when enabled.
- **Integrations:** DiscordSRV hook support, PlaceholderAPI usage in formats/messages.

## Commands

> Exact command roots/aliases/enabled states can vary by provider config in `commands/*.yml`. Below are key command groups from source providers.

### Channels (`ChannelCommandsProvider`, provider `chat-channel`)
| Command | Permission | Description | Notes |
| --- | --- | --- | --- |
| `/channel` (root, alias `ch`) | `sunlight.chat.command.channel.root` | Channel command root. |  |
| `/channel join <channel>` | `sunlight.chat.command.channel.join` | Join a chat channel. | Player-only; channel suggestions from registered channels. |
| `/channel leave <channel>` | `sunlight.chat.command.channel.leave` | Leave a chat channel. | Player-only. |

### Conversations / private messages (`ConversationCommandProvider`, provider `chat-conversations`)
| Command | Permission | Description | Notes |
| --- | --- | --- | --- |
| `/conversations` (root, alias `convos`) | `sunlight.chat.command.conversations.root` | Conversations command root. |  |
| `/conversations message <player> <text>` (aliases `msg`, `tell`) | `sunlight.chat.command.conversations.send` | Send a private message. | Player-only. |
| `/conversations reply <text>` | `sunlight.chat.command.conversations.reply` | Reply to last conversation partner. | Player-only. |
| `/conversations toggle [target]` | `sunlight.chat.command.conversations.toggle` | Toggle conversations on/off. | Others: `...toggle.others`; syncs with Settings module when enabled. |
| `/conversations on [target]` | `sunlight.chat.command.conversations.toggle` | Enable conversations. | Others: `...toggle.others`. |
| `/conversations off [target]` | `sunlight.chat.command.conversations.toggle` | Disable conversations. | Others: `...toggle.others`. |

### Mentions (`MentionsCommandProvider`)
| Command group | Permissions | Purpose |
| --- | --- | --- |
| Mentions root/toggle controls | `sunlight.chat.command.mentions.root`, `sunlight.chat.command.mentions.toggle`, `sunlight.chat.command.mentions.toggle.others` | Enable/disable/manage mention behavior for self/others (see provider for exact nodes). |

### Spy (`SpyCommandProvider`)
| Command group | Permissions | Purpose |
| --- | --- | --- |
| Spy root/logger/chat/command/social controls | `sunlight.chat.command.spy.root`, `...spy.logger`, `...spy.chat` (+ `.others`), `...spy.command` (+ `.others`), `...spy.social` (+ `.others`) | Toggle/view spy modes and monitored streams. |

### Clear chat (`ClearChatCommandProvider`)
| Command | Permission | Purpose |
| --- | --- | --- |
| Clear chat command(s) | `sunlight.chat.command.clearchat` | Clear chat for players/server scope (implementation-specific). |

### Roleplay (`RoleplayCommands`)
| Command | Permission | Purpose |
| --- | --- | --- |
| `/me <message>` (when enabled) | `sunlight.chat.command.me` | Sends roleplay/action message using configured format. |

### Mail (`MailCommandProvider` / chat mail subsystem)
| Command group | Permissions | Purpose |
| --- | --- | --- |
| Mail root/send/read/clear | `sunlight.chat.command.mail.root`, `...mail.send`, `...mail.read`, `...mail.clear` | Send and manage offline mail messages. |

> Additional channel-specific or dynamic commands may be registered depending on channel configuration.

## Permissions

### Command permissions
| Permission | Description |
| --- | --- |
| `sunlight.chat.command.channel.root` | Channel root command |
| `sunlight.chat.command.channel.join` | Join channels |
| `sunlight.chat.command.channel.leave` | Leave channels |
| `sunlight.chat.command.clearchat` | Clear chat command |
| `sunlight.chat.command.me` | Roleplay `/me` command |
| `sunlight.chat.command.mentions.root` | Mentions root |
| `sunlight.chat.command.mentions.toggle` | Toggle mentions (self) |
| `sunlight.chat.command.mentions.toggle.others` | Toggle mentions for others |
| `sunlight.chat.command.spy.root` | Spy root |
| `sunlight.chat.command.spy.logger` | Spy logger access |
| `sunlight.chat.command.spy.chat` | Spy chat messages |
| `sunlight.chat.command.spy.chat.others` | Spy chat for others/targets as implemented |
| `sunlight.chat.command.spy.command` | Spy commands |
| `sunlight.chat.command.spy.command.others` | Spy commands for others/targets as implemented |
| `sunlight.chat.command.spy.social` | Spy social/private messages |
| `sunlight.chat.command.spy.social.others` | Spy social for others/targets as implemented |
| `sunlight.chat.command.conversations.root` | Conversations root |
| `sunlight.chat.command.conversations.toggle` | Toggle conversations (self) |
| `sunlight.chat.command.conversations.toggle.others` | Toggle conversations for others |
| `sunlight.chat.command.conversations.reply` | Reply to private messages |
| `sunlight.chat.command.conversations.send` | Send private messages (`/msg`/`/tell`) |
| `sunlight.chat.command.mail.root` | Mail root |
| `sunlight.chat.command.mail.send` | Send mail |
| `sunlight.chat.command.mail.read` | Read mail |
| `sunlight.chat.command.mail.clear` | Clear mail |

### Channel access permissions
| Permission pattern | Description |
| --- | --- |
| `sunlight.chat.channel.hear.<channelId>` | Permission to hear/read a specific channel |
| `sunlight.chat.channel.speak.<channelId>` | Permission to speak in a specific channel |

### Bypass permissions
| Permission | Description |
| --- | --- |
| `sunlight.chat.bypass.conversations.disabled` | Bypass disabled conversations restrictions |
| `sunlight.chat.bypass.mention.cooldown` | Bypass mention cooldown |
| `sunlight.chat.bypass.mention.amount` | Bypass mention amount/limit checks |
| `sunlight.chat.bypass.channel.cooldown` | Bypass channel cooldown |
| `sunlight.chat.bypass.moderation.anticaps` | Bypass anti-caps |
| `sunlight.chat.bypass.moderation.antiflood` | Bypass anti-flood |
| `sunlight.chat.bypass.moderation.profanity.filter` | Bypass profanity filter |
| `sunlight.chat.bypass.spy.monitor` | Bypass being monitored by spy (where enforced) |

## Configuration (`settings.yml`)

Source: `moduleImpl/chat/core/ChatSettings.java` (large configuration surface). Major sections:

### Global
- `Global.Chat-Event-Priority` (default `HIGH`) — chat event handler priority.
- `Global.DiscordSRV.Enabled` (default `true`) — experimental DiscordSRV game-to-discord support.
- `Global.Disable-Chat-Reports` (default `true`) — disables chat reporting/verification behavior where supported.

### Roleplay
- `Roleplay-Commands.Enabled` (default `true`)
- `Roleplay-Commands.Me.Format` — `/me` format.

### Chat formats
- `Chat.Format.Components` — reusable named components for chat formatting.
- `Chat.Format.User` — rank/permission-group based format definitions (priority, format, ranks).
- `Chat.Format.Fallback` — fallback format when no user format matches.

### Channels
- `Channels.Enabled` (default `true`)
- `Channels.Default-Channel-Id` (default `local`)
- `Channels.Nobody-Heard-Message` (default `true`)
- Channel definitions/files are managed by the chat channel system (`channels/` files, schemas/defaults).

### Conversations
- `Conversations.Enabled` (default `true`)
- `Conversations.Sound.*` — incoming/outgoing sounds
- `Conversations.Format.Basic` / `Incoming` / `Outgoing` — PM formatting templates

### Mail
- `Mail.Enabled` (default `true`)
- `Mail.Max-Inbox` (default `10`, `0` unlimited)
- `Mail.Expiry-Days` (default `30`, `0` disables expiry)
- `Mail.Table-Prefix` (default `sunlight`)
- `Mail.Format` — delivered mail format

### Mentions
- `Mentions.Enabled` (default `true`)
- `Mentions.Limit-Per-Message` (default `3`, `-1` unlimited)
- `Mentions.Cooldown-Per-Mention` (default `15`, `-1` disabled)
- `Mentions.Prefix` (regex, default `@(\\w+)`)
- `Mentions.Player.Format`
- `Mentions.Special` — group/rank mentions

### Auto-moderation
- `AutoModeration.UserCache.Life-Time` (default `60` seconds)
- `AutoModeration.Anti-Caps.*` — enabled, min length, uppercase threshold, affected commands
- `AutoModeration.Anti-Flood.*` — enabled, similarity thresholds, command cooldown, command whitelist
- `AutoModeration.Profanity-Filter.*` — enabled, rule files, affected commands (rules stored in chat rules directory)

### Item show
- `ItemShow.Enabled` (default `true`)
- `ItemShow.Placeholder` (default `@hand`)
- `ItemShow.Format`

### Spy
- `Spy.Enabled` (default `true`)
- `Spy.Info-Format` — per spy type format (chat/command/social)

## Placeholders and properties

- Chat placeholders are registered by `ChatPlaceholders`.
- User chat properties (e.g. conversations/mentions/spy toggles) are managed via `ChatProperties` and integrated with the Settings module where applicable.
- Format and message templates support built-in placeholders and PlaceholderAPI (where enabled).

## Notes

- Chat is one of the largest modules; generated config files may include additional comments/options beyond this summary.
- Channel/message permissions and moderation bypasses should be configured carefully for staff vs players.
- Discord integration behavior depends on DiscordSRV availability and module settings.
- Mail is database-backed; table prefix can be used for isolation or cross-server sync.
