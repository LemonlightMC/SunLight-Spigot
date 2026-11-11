# Settings Module

Provides a central player preferences GUI (`/settings`) with panels and toggles that integrate with other SunLight modules (chat, friends, PTP, vanish, essential, voting, etc.).

- **Module ID:** `settings`
- **Display name:** `Settings`
- **Module directory:** `moduleImpl/settings/`

## Commands

| Command | Aliases | Permission | Description |
| --- | --- | --- | --- |
| `/settings` | `setting`, `preferences`, `prefs` | `sunlight.settings.command.settings` | Opens the settings GUI (root panel). |

## Permissions

### Command
| Permission | Description |
| --- | --- |
| `sunlight.settings.command.settings` | Open/use settings command and GUI |

### Panel permissions
| Permission | Panel |
| --- | --- |
| `sunlight.settings.panel.chat` | Chat settings panel |
| `sunlight.settings.panel.chat.color` | Chat color settings panel/access |
| `sunlight.settings.panel.friends` | Friends settings panel |
| `sunlight.settings.panel.tpa` | TPA/PTP settings panel |
| `sunlight.settings.panel.payments` | Payments settings panel |
| `sunlight.settings.panel.general` | General settings panel |
| `sunlight.settings.panel.player` | Player settings panel |
| `sunlight.settings.panel.admin` | Admin settings panel |
| `sunlight.settings.panel.spy` | Spy settings panel |

### Setting permissions
| Permission | Setting area |
| --- | --- |
| `sunlight.settings.setting.pm` | Private messages/conversations toggle |
| `sunlight.settings.setting.mentions` | Mentions toggle |
| `sunlight.settings.setting.spy.chat` | Chat spy setting |
| `sunlight.settings.setting.spy.social` | Social/PM spy setting |
| `sunlight.settings.setting.spy.command` | Command spy setting |
| `sunlight.settings.setting.chat_color` | Chat color/format preferences |
| `sunlight.settings.setting.friend_requests` | Friend requests preference |
| `sunlight.settings.setting.friend_teleport` | Friend teleport preference |
| `sunlight.settings.setting.tpa` | TPA requests preference |
| `sunlight.settings.setting.tpahere` | TPA-here preference |
| `sunlight.settings.setting.tpa_auto` | Auto-accept TPA preference |
| `sunlight.settings.setting.pay` | Payments/pay toggle |
| `sunlight.settings.setting.pay_alerts` | Payment alerts toggle |
| `sunlight.settings.setting.scoreboard` | Scoreboard toggle |
| `sunlight.settings.setting.join_message` | Join message visibility/preference |
| `sunlight.settings.setting.quit_message` | Quit message visibility/preference |
| `sunlight.settings.setting.death_message` | Death message visibility/preference |
| `sunlight.settings.setting.reminders` | Reminder preferences (playtime/voting/ads as implemented) |
| `sunlight.settings.setting.night_vision` | Night vision toggle |
| `sunlight.settings.setting.glow` | Glow action/setting access |
| `sunlight.settings.setting.nametag` | Nametag action/setting access |
| `sunlight.settings.setting.profile` | Profile action/setting access |
| `sunlight.settings.setting.god` | God mode toggle |
| `sunlight.settings.setting.speed` | Speed level setting |
| `sunlight.settings.setting.jump` | Jump level setting |
| `sunlight.settings.setting.vanish` | Vanish toggle |
| `sunlight.settings.setting.invisibility` | Invisibility toggle |

## Panels and UI

The module defines panel/menu types including:
- Root settings
- Chat and chat color
- Friends
- TPA/payments
- General/player/admin/spy

UI files are managed by the module (e.g. `settings.yml` and panel-specific menu files such as `settings-<panel>.yml`).

## Settings model

Source classes: `SettingsProperties`, `SettingDefinition`, `SettingKind`, `SettingsPanel`, `AudienceMode`, `SettingsRegistry`.

### Audience modes
Used by settings that control who can interact/see or receive requests:
- `everyone`
- `team`
- `friends`
- `nobody`

(Available sets may differ per setting: full or partial mode sets.)

### Chat appearance options
- `CHAT_COLORS`: `none, red, blue, green, yellow, aqua, gold, white, gray`
- `CHAT_GRADIENTS`: `none, rainbow, sunset, ocean`
- `CHAT_FORMATTING_VALUES`: `none, bold, italic, underline, bold_italic`

### Other configurable values/toggles
- Scoreboard toggle
- Reminder toggles/categories (module-dependent)
- Night vision toggle
- Speed level: `0..5`
- Jump level: `0..5`
- Invisibility toggle

## Integration behavior

- Syncs/mirrors properties with integrated modules such as Chat, Friends, PTP, Vanish, Essential, and Voting where enabled.
- Can open or control related module features (e.g. glow, nametag, profile, spy actions).
- Applies effects (e.g. speed/potion/night vision-related behavior) on setting changes and player join as implemented.

## Notes

- Available panels/settings depend on which modules are installed/enabled and which permissions the player has.
- Settings acts as a unified interface; authoritative behavior and data for each feature remain in the owning module.
