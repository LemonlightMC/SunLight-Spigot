---
description: "Guide to the Scheduler module in SunLight for Minecraft servers, covering commands, permissions, configuration, features, and usage for admins and players."
---

# Scheduler

Runs periodic broadcaster/announcer messages from YAML announcer files.

* **Module ID:** `scheduler`
* **Display name:** `Scheduler`
* **Module directory:** `moduleImpl/scheduler/`

## Commands

This module does not register dedicated commands.

## Permissions

No dedicated command/permission classes were identified for this module.

## Data files

* **Announcers directory:** `scheduler/announcers/`
* **File format:** `announcers/*.yml`
* A default `global` announcer is created automatically when the announcers directory is missing.

## Announcer schema

Source: `moduleImpl/scheduler/announcer/AnnouncerSchema.java`

| Key           | Type                      | Default          | Description                                      |
| ------------- | ------------------------- | ---------------- | ------------------------------------------------ |
| `Interval`    | int                       | `120`            | Broadcast interval in seconds.                   |
| `RandomOrder` | boolean                   | `false`          | Broadcast messages in random order when enabled. |
| `Ranks`       | string set (lower-case)   | `{"*"}`          | Rank/group visibility filter. `*` allows all.    |
| `Texts`       | map\<string, string list> | default messages | Named message groups/lists to broadcast.         |

### Placeholder support in texts

Announcer messages can use placeholders such as:

* `%player_name%`
* `%player_display_name%`
* `%player_prefix%`
* `%player_suffix%`
* PlaceholderAPI placeholders (if available)

## Default announcer

Source: `moduleImpl/scheduler/SchedulerDefaults.java`

* **ID:** `global`
* **Interval:** `120`
* **Random order:** `true`
* **Ranks:** `*`
* **Sample message keys:** `discord`, `vote`, `store`

## Behavior, compatibility, and integration notes

* Broadcast tasks are handled by the module's announcer scheduler logic (main-thread-safe message dispatch as implemented).
* Rank visibility checks use player groups/inheritance and wildcard matching.
* Add multiple announcer files to run different announcement streams with different intervals/audiences.
