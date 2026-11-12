# Warmups

Adds configurable warmup delays before actions (primarily teleports), with cancellation conditions and progress indicators.

* **Module ID:** `warmups`
* **Display name:** `Warmups`
* **Module directory:** `moduleImpl/warmups/`
* **API/provider:** implements `WarmupsProvider`

## Commands

This module does not register dedicated player commands. It hooks into teleport/warmup flows used by other modules.

## Permissions

Source: `moduleImpl/warmups/config/WarmupsPerms.java`

| Permission                         | Description                                       |
| ---------------------------------- | ------------------------------------------------- |
| `sunlight.warmups.bypass.teleport` | Bypass teleport warmups                           |
| `sunlight.warmups.bypass.command`  | Bypass command-related warmups (where applicable) |

## Configuration

Source: `moduleImpl/warmups/config/WarmupsConfig.java`

| Key                         | Default                                                     | Description                                              |
| --------------------------- | ----------------------------------------------------------- | -------------------------------------------------------- |
| `Warmup.Tick_Interval`      | `2L`                                                        | Warmup tick interval.                                    |
| `CancelOnMove`              | `true`                                                      | Cancel warmup when player moves.                         |
| `CancelOnInteract`          | `true`                                                      | Cancel warmup on interaction.                            |
| `CancelOnDamage`            | `true`                                                      | Cancel warmup when player takes/deals damage as handled. |
| `MovementThreshold`         | `0.5`                                                       | Movement distance threshold for cancellation.            |
| `Particles.*`               | enabled, left/right redstone particle effects               | Visual warmup effects.                                   |
| `Teleport.HandledTypes`     | all `TeleportType` values except `OTHER`                    | Teleport types intercepted for warmup handling.          |
| `Teleport.Warmups_By_Rank`  | default `5`, `vip=4`, `admin=0`                             | Warmup duration (seconds) by rank.                       |
| Boss bar indicator settings | enabled, teleport title/color/style (`GREEN`, `NOTCHED_10`) | Progress indicator for warmups.                          |

## Behavior notes

* Intercepts SunLight teleport events for handled teleport types (non-forced and when not bypassed), starts warmups, and cancels on configured triggers.
* Warmup ticking/processing is handled by module services/listeners and implementation classes (`Warmup`, `TeleportWarmup`, `WarmupType`).
* Rank-based warmup durations use the configured rank map; `0` effectively means no delay for matching ranks.
