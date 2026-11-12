# Settings Module

Provides a central player preferences GUI (`/settings`) with panels and toggles that integrate with other SunLight modules (chat, friends, PTP, vanish, essential, voting, etc.).

- **Module ID:** `settings`
- **Display name:**
## Commands

| Command | Aliases | Permission | Description |
| --- | --- | --- | --- |
| `/settings` | `setting`, `preferences`, `prefs` | `sunlight.settings.command.settings` | Opens the settings GUI (root panel). |

## Permissions

| Permission | Category | Description |
| --- | --- | --- |
| `sunlight.settings.command.settings` | Command | Open/use settings command and GUI |
| `sunlight.settings.panel.chat` | Panel | Chat settings panel |
| `sunlight.settings.panel.chat.color` | Panel | Chat color settings panel/access |
| `sunlight.settings.panel.friends` | Panel | Friends settings panel |
| `sunlight.settings.panel.tpa` | Panel | TPA/PTP settings panel |
| `sunlight.settings.panel.payments` | Panel | Payments settings panel |
| `sunlight.settings.panel.general` | Panel | General settings panel |
| `sunlight.settings.panel.player` | Panel | Player settings panel |
| `sunlight.settings.panel.admin` | Panel | Admin settings panel |
| `sunlight.settings.panel.spy` | Panel | Spy settings panel |
| `sunlight.settings.setting.pm` | Setting | Private messages/conversations toggle |
| `sunlight.settings.setting.mentions` | Setting | Mentions toggle |
| `sunlight.settings.setting.spy.chat` | Setting | Chat spy setting |
| `sunlight.settings.setting.spy.social` | Setting | Social/PM spy setting |
| `sunlight.settings.setting.spy.command` | Setting | Command spy setting |
| `sunlight.settings.setting.chat_color` | Setting | Chat color/format preferences |
| `sunlight.settings.setting.friend_requests` | Setting | Friend requests preference |
| `sunlight.settings.setting.friend_teleport` | Setting | Friend teleport preference |
| `sunlight.settings.setting.tpa` | Setting | TPA requests preference |
| `sunlight.settings.setting.tpahere` | Setting | TPA-here preference |
| `sunlight.settings.setting.tpa_auto` | Setting | Auto-accept TPA preference |
| `sunlight.settings.setting.pay` | Setting | Payments/pay toggle |
| `sunlight.settings.setting.pay_alerts` | Setting | Payment alerts toggle |
| `sunlight.settings.setting.scoreboard` | Setting | Scoreboard toggle |
| `sunlight.settings.setting.join_message` | Setting | Join message visibility/preference |
| `sunlight.settings.setting.quit_message` | Setting | Quit message visibility/preference |
| `sunlight.settings.setting.death_message` | Setting | Death message visibility/preference |
| `sunlight.settings.setting.reminders` | Setting | Reminder preferences (playtime/voting/ads as implemented) |
| `sunlight.settings.setting.night_vision` | Setting | Night vision toggle |
| `sunlight.settings.setting.glow` | Setting | Glow action/setting access |
| `sunlight.settings.setting.nametag` | Setting | Nametag action/setting access |
| `sunlight.settings.setting.profile` | Setting | Profile action/setting access |
| `sunlight.settings.setting.god` | Setting | God mode toggle |
| `sunlight.settings.setting.speed` | Setting | Speed level setting |
| `sunlight.settings.setting.jump` | Setting | Jump level setting |
| `sunlight.settings.setting.vanish` | Setting | Vanish toggle |
| `sunlight.settings.setting.invisibility` | Setting | Invisibility toggle |

## Panels and UI

The module defines panel/menu types including:
- Root settings
- Chat and chat color
- Friends
- TPA/payments
- General/player/admin/spy

UI files are managed by the module (e.g. `settings.yml` and panel-specific menu files such as `settings-<panel>.yml`).

## Settings model

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