# Back Location

Stores previous teleport locations and death locations, allowing players to return with `/back` and `/deathback`.

* **Module ID:** `back_location`
* **Display name:** `Back`
* **Module directory:** `moduleImpl/backlocation/`
* **Provider IDs:** `back`, `deathback`

## Commands

> Command labels/aliases/cooldowns/costs can be overridden in `commands/back.yml` and `commands/deathback.yml`.

| Command               | Provider / node           | Permission                                | Description                                | Usage / notes                                                                                  |
| --------------------- | ------------------------- | ----------------------------------------- | ------------------------------------------ | ---------------------------------------------------------------------------------------------- |
| `/back [target]`      | `back` / `back`           | `sunlight.backlocation.command.back`      | Teleports to the previous cached location. | Optional target uses `...back.others`; silent flag supported by target argument handling.      |
| `/deathback [target]` | `deathback` / `deathback` | `sunlight.backlocation.command.deathback` | Teleports to the cached death location.    | Optional target uses `...deathback.others`; silent flag supported by target argument handling. |

## Permissions

| Permission                                       | Description                                                  |
| ------------------------------------------------ | ------------------------------------------------------------ |
| `sunlight.backlocation.command.back`             | Use `/back`                                                  |
| `sunlight.backlocation.command.back.others`      | Use `/back` on other players                                 |
| `sunlight.backlocation.command.deathback`        | Use `/deathback`                                             |
| `sunlight.backlocation.command.deathback.others` | Use `/deathback` on other players                            |
| `sunlight.backlocation.bypass.previous.worlds`   | Bypass previous-location world blacklist                     |
| `sunlight.backlocation.bypass.previous.causes`   | Bypass ignored teleport causes for previous-location caching |
| `sunlight.backlocation.bypass.death.worlds`      | Bypass death-location world blacklist                        |
| `sunlight.backlocation.bypass.cost`              | Bypass teleport cost                                         |
| `sunlight.backlocation.bypass.cooldown`          | Bypass teleport cooldown/restrictions where enforced         |

## Configuration (`settings.yml`)

Source: `moduleImpl/backlocation/config/BackLocationSettings.java`.

### Previous location (`/back`)

| Key                                         | Default             | Description                                                                                                              |
| ------------------------------------------- | ------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `Location.Previous.Enabled`                 | `true`              | Track teleports and cache pre-teleport locations. Disabling prevents `/back` usage.                                      |
| `Location.Previous.Expire_Time`             | `3600`              | Cache lifetime in seconds.                                                                                               |
| `Location.Previous.Min_Distance_Difference` | `10`                | Minimum teleport distance to update cache (cross-world teleports ignore this).                                           |
| `Location.Previous.Disabled_Worlds`         | `[my_custom_world]` | Worlds where returning to previous location is blocked unless bypassed.                                                  |
| `Location.Previous.Ignored_Causes`          | `[ENDER_PEARL]`     | Teleport causes that do not update previous-location cache. Values are Bukkit `PlayerTeleportEvent.TeleportCause` names. |
| `Location.Previous.Cost`                    | `0D`                | Vault currency cost to teleport to previous location (`0` disables).                                                     |

### Death location (`/deathback`)

| Key                              | Default             | Description                                                                    |
| -------------------------------- | ------------------- | ------------------------------------------------------------------------------ |
| `Location.Death.Enabled`         | `true`              | Track deaths and cache death locations. Disabling prevents `/deathback` usage. |
| `Location.Death.ResetOnUse`      | `true`              | If enabled, death location can be used once per death.                         |
| `Location.Death.Expire_Time`     | `3600`              | Cache lifetime in seconds.                                                     |
| `Location.Death.Disabled_Worlds` | `[my_custom_world]` | Worlds where returning to death location is blocked unless bypassed.           |
| `Location.Death.Cost`            | `0D`                | Vault currency cost to teleport to death location (`0` disables).              |

## Placeholders

* `%backlocation_has_previous%`
* `%backlocation_has_death%`
* `%backlocation_previous_expire_in%`
* `%backlocation_death_expire_in%`

## Notes

* Feedback messages for success/failure/targeting are defined in `BackLocationLang`.
* Teleport costs use Vault economy integration when cost > 0.
* Actual command cooldown behavior can also be controlled via command provider config (`commands/back.yml`, `commands/deathback.yml`).
