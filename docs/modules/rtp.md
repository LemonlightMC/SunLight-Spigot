# RTP (Random Teleport) Module

Teleports players to random locations with world/region/safety handling, cooldowns, and costs.

- **Module ID:** `rtp`
- **Display name:** `RTP`
- **Module directory:** `moduleImpl/rtp/`
- **API/provider:** implements `RtpProvider`

## Commands

Implemented by `RTPCommandProvider`.

- Command root/nodes, aliases, arguments, and per-world/target behavior are defined in the provider and can be customized in `commands/<rtp-provider>.yml`.
- Typical usage includes random teleport for self and (with permission) for others or to specific configured worlds/regions.

> Check `moduleImpl/rtp/command/RTPCommandProvider.java` and generated command config for the exact command set in your build.

## Permissions

Defined in `moduleImpl/rtp/config/RTPPerms.java`.

Common permission areas:
- RTP command access (self/others)
- Bypass permissions for cooldown/cost
- World/region access permissions (as implemented)

## Configuration

- **Settings:** `moduleImpl/rtp/config/RTPSettings.java` (world ranges, safety checks, redirection/target worlds, cooldown/cost-related options, attempt/search behavior)
- **Language:** `moduleImpl/rtp/config/RTPLang.java`
- **Placeholders:** `moduleImpl/rtp/RTPPlaceholders.java`
- **Engine:** `moduleImpl/rtp/engine/RTPEngine.java` handles location search/generation logic

> Inspect generated `rtp/settings.yml` for exact keys, defaults, and world-specific options.

## Notes

- RTP safety and validity checks are performed before teleporting (chunk/location safety as implemented by engine/settings).
- Cooldowns/costs may be enforced by module logic and/or command provider configuration.
- World-specific rules and redirections depend on your configured RTP settings.
