---
description: "SunLight PlaceholderAPI placeholders reference for Minecraft servers, listing global and module placeholders for AFK, homes, warps, chat, voting, and more."
---
# Placeholder Reference

SunLight provides PlaceholderAPI placeholders through core and module placeholder registrations. Exact availability depends on which modules are enabled and which integrations are active.

## General notes

- Placeholder availability depends on which modules are enabled and which integrations are active.
- Some placeholders accept dynamic values (for example per-kit, per-vote, per-warp, per-player-warp, or profile/tag/rank values). Check the relevant module's configuration and in-game behavior for the exact format in your setup.
- Boolean-like placeholders commonly render as localized yes/no or state text depending on language/config.

## Core / global placeholders

Core placeholders are registered by SunLight's core placeholder/hook integration and command argument/context systems. In addition, many modules expose their own namespaces.

## Module placeholders (by module)

### AFK
- `%afk_state%` — AFK state (yes/no)
- `%afk_mode%` — formatted AFK mode text
- `%afk_idle_time%` — idle time in seconds
- `%afk_idle_time_formatted%` — formatted idle time
- `%afk_total_time%` — total AFK time
- `%afk_total_time_formatted%` — formatted total AFK time
- `%afk_count%` — AFK count
- `%afk_enter_timestamp%` — AFK enter timestamp
- `%afk_enter_time%` — formatted time since AFK enter
- `%afk_duration%` — current AFK duration (seconds)
- `%afk_duration_formatted%` — formatted current AFK duration
- `%afk_time_until_afk%` — seconds until auto-AFK (clamped at 0)
- `%afk_time_until_kick%` — seconds until kick (clamped at 0)
- `%afk_online_count%` — number of AFK players online
- `%afk_online%` — comma-separated AFK player names
- `%afk_stats_sessions%` — AFK statistics sessions
- `%afk_stats_total_time%` — statistics total AFK time
- `%afk_stats_total_time_formatted%` — formatted statistics total AFK time
- `%afk_stats_average_time%` — statistics average AFK time
- `%afk_stats_average_time_formatted%` — formatted statistics average AFK time

### Back Location
- `%backlocation_has_previous%`
- `%backlocation_has_death%`
- `%backlocation_previous_expire_in%`
- `%backlocation_death_expire_in%`

### Bans
- Punishment/history/list-related placeholders are registered by the Bans module.

### Chat
- Chat/channel/conversation/spy/mail/mention-related placeholders are registered by the Chat module.

### Essential
- Essential utility placeholders are registered by the Essential module.

### Glow
- `%glow_state%`
- `%glow_color%`

### Homes
- `%homes_limit%`
- `%homes_amount%`
- `%homes_favorite_home%`

### Kits
- `%kits_is_on_cooldown%` (kit context/payload)
- `%kits_is_available%`
- `%kits_cooldown_raw%`
- `%kits_cooldown%`
- Additional per-kit placeholders may be available.

### Links
- Link-specific placeholders registered by module (IDs/links data; legacy `socials_<id>` placeholders are also re-registered by Socials when applicable).

### Nametags
- `%nametags_prefix%`
- `%nametags_suffix%`
- Additional tag/rank/profile placeholders with payload-aware resolution (see Nametags placeholder registration).

### Nerf Phantoms
- `%phantoms_antiphantom_state%`

### Nick
- `%nick_name%`
- `%essential_custom_name%` (legacy compatibility placeholder registered by Nick module)

### Player Warps
- Player warp placeholders (warp info/status/listing-related); check your server/build for the exact keys.

### Playtime
- Playtime tracking, goal, period, and calendar-related placeholders.

### Profiles
- Profile/scope-related placeholders.

### PTP
- `%ptp_requests_enabled%`

### Reports
- Report status/count/info placeholders.

### RTP
- RTP-related placeholders.

### Spawns
- `%spawn_id%`
- `%spawn_name%`
- `%spawn_location_world%`
- `%spawn_location_x%`
- `%spawn_location_y%`
- `%spawn_location_z%`

### Timer
- Global timer placeholders under `timer_*` (state, name, kind, formatted, remaining, elapsed, seconds, progress, duration, count, etc.)
- Personal timer placeholders under `timer_personal_*` (same style for personal timers)

### Vanish
- `%vanish_state%`

### Voting
- Vote placeholders (examples/areas): `%vote_name%`, `%vote_key%`, `%vote_status%`, times, outcome, options, secret/silent/recurring flags, totals/participants/score/percent/count (context-dependent)
- Additional dynamic vote/option placeholders are registered in.

### Warps
- `%warp_id%`
- `%warp_name%`
- `%warp_description%`
- `%warp_location_world%`
- `%warp_location_x%`
- `%warp_location_y%`
- `%warp_location_z%`

## Finding placeholders on your server

- Enable the relevant module and, if needed, install PlaceholderAPI to use placeholders in other plugins.
- Check the module page in this documentation for the most common placeholders and any supported dynamic forms.
- If a placeholder is not listed here, test it in-game with your placeholder parsing tools to confirm it is available in your build.
