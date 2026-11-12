# Placeholders

SunLight provides PlaceholderAPI placeholders through core and module placeholder registrations. Exact availability depends on which modules are enabled and which integrations are active.

## General notes

* Placeholder identifiers and outputs are based on source registrations (`*Placeholders` classes, module `registerPlaceholders`, and provider-specific handlers).
* Some placeholders accept dynamic payloads/suffixes (e.g. per-kit, per-vote, per-warp, per-player-warp, profile/tag/rank payloads). Check the module implementation/config if you need a specific dynamic form.
* Boolean-like placeholders commonly render as localized yes/no or state text depending on language/config.

## Core / global placeholders

Core placeholders are registered by SunLight's core placeholder/hook integration and command argument/context systems. In addition, many modules expose their own namespaces.

## Module placeholders (by module)

### AFK (`AfkPlaceholders`)

* `%afk_state%` — AFK state (yes/no)
* `%afk_mode%` — formatted AFK mode text
* `%afk_idle_time%` — idle time in seconds
* `%afk_idle_time_formatted%` — formatted idle time
* `%afk_total_time%` — total AFK time (raw value from handler)
* `%afk_total_time_formatted%` — formatted total AFK time
* `%afk_count%` — AFK count
* `%afk_enter_timestamp%` — AFK enter timestamp
* `%afk_enter_time%` — formatted time since AFK enter
* `%afk_duration%` — current AFK duration (seconds)
* `%afk_duration_formatted%` — formatted current AFK duration
* `%afk_time_until_afk%` — seconds until auto-AFK (clamped at 0)
* `%afk_time_until_kick%` — seconds until kick (clamped at 0)
* `%afk_online_count%` — number of AFK players online
* `%afk_online%` — comma-separated AFK player names
* `%afk_stats_sessions%` — AFK statistics sessions
* `%afk_stats_total_time%` — statistics total AFK time
* `%afk_stats_total_time_formatted%` — formatted statistics total AFK time
* `%afk_stats_average_time%` — statistics average AFK time
* `%afk_stats_average_time_formatted%` — formatted statistics average AFK time

### Back Location (`BackLocationPlaceholders`)

* `%backlocation_has_previous%`
* `%backlocation_has_death%`
* `%backlocation_previous_expire_in%`
* `%backlocation_death_expire_in%`

### Bans (`BansPlaceholders`)

* Punishment/history/list-related placeholders are registered by the Bans module (see `moduleImpl/bans/BansPlaceholders.java` for exact keys and payloads).

### Chat (`ChatPlaceholders`)

* Chat/channel/conversation/spy/mail/mention-related placeholders are registered by the Chat module (see `moduleImpl/chat/ChatPlaceholders.java` and chat properties).

### Essential (`EssentialPlaceholders`)

* Essential utility placeholders are registered by the Essential module (see `moduleImpl/essential/EssentialPlaceholders.java`).

### Glow (`GlowPlaceholders`)

* `%glow_state%`
* `%glow_color%`

### Homes

* `%homes_limit%`
* `%homes_amount%`
* `%homes_favorite_home%`

### Kits (`KitsPlaceholders`)

* `%kits_is_on_cooldown%` (kit context/payload)
* `%kits_is_available%`
* `%kits_cooldown_raw%`
* `%kits_cooldown%`
* Additional per-kit placeholders may be available (see `KitsPlaceholders`).

### Links (`LinksPlaceholders`)

* Link-specific placeholders registered by module (IDs/links data; legacy `socials_<id>` placeholders are also re-registered by Socials when applicable).

### Nametags (`NametagsPlaceholders`)

* `%nametags_prefix%`
* `%nametags_suffix%`
* Additional tag/rank/profile placeholders with payload-aware resolution (see Nametags placeholder registration).

### Nerf Phantoms

* `%phantoms_antiphantom_state%`

### Nick

* `%nick_name%`
* `%essential_custom_name%` (legacy compatibility placeholder registered by Nick module)

### Player Warps (`PlayerWarpsPlaceholders`)

* Player warp placeholders (warp info/status/listing-related); see `PlayerWarpsPlaceholders` for exact keys and payload forms.

### Playtime (`PlaytimePlaceholders`)

* Playtime tracking, goal, period, and calendar-related placeholders (see `PlaytimePlaceholders`).

### Profiles (`ProfilesPlaceholders`)

* Profile/scope-related placeholders (see `ProfilesPlaceholders`).

### PTP

* `%ptp_requests_enabled%`

### Reports (`ReportsPlaceholders`)

* Report status/count/info placeholders (see `ReportsPlaceholders`).

### RTP (`RTPPlaceholders`)

* RTP-related placeholders (see `RTPPlaceholders`).

### Spawns (`SpawnsPlaceholders`)

* `%spawn_id%`
* `%spawn_name%`
* `%spawn_location_world%`
* `%spawn_location_x%`
* `%spawn_location_y%`
* `%spawn_location_z%`

### Timer

* Global timer placeholders under `timer_*` (state, name, kind, formatted, remaining, elapsed, seconds, progress, duration, count, etc.)
* Personal timer placeholders under `timer_personal_*` (same style for personal timers)

### Vanish

* `%vanish_state%`

### Voting (`VotingPlaceholders`)

* Vote placeholders (examples/areas): `%vote_name%`, `%vote_key%`, `%vote_status%`, times, outcome, options, secret/silent/recurring flags, totals/participants/score/percent/count (context-dependent)
* Additional dynamic vote/option placeholders are registered in `VotingModule`.

### Warps (`WarpsPlaceholders`)

* `%warp_id%`
* `%warp_name%`
* `%warp_description%`
* `%warp_location_world%`
* `%warp_location_x%`
* `%warp_location_y%`
* `%warp_location_z%`

## Where to find exact keys

For the most precise and up-to-date placeholder list in your build:

1. Check the module's `*Placeholders.java` class in source.
2. Search for `register("...")` or placeholder registration calls in the module package.
3. Verify with PlaceholderAPI in-game (e.g. `/papi ecloud ...` not required for internal placeholders; use parse commands/listing methods available on your server) and enabled modules.
