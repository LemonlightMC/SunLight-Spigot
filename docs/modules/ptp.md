# PTP (Player Teleports) Module

Player-to-player teleport request and invite system with accept/decline flow, toggles, timeout handling, and optional costs/auto-accept integration.

- **Module ID:** `ptp`
- **Display name:** `PTP`
- **Module directory:** `moduleImpl/ptp/`
- **Provider ID:** `ptp`
- **Implementation:** `PTPCommands`, `PTPModule`, `TeleportRequest`

## Commands

> Configurable in `commands/ptp.yml`.

| Command | Aliases | Permission | Description | Usage / notes |
| --- | --- | --- | --- | --- |
| `/ptp` (root) | — | `sunlight.ptp.command.root` | PTP command root. |  |
| `/ptp accept [player]` | standalone `tpyes` | `sunlight.ptp.command.accept` | Accept a pending teleport request/invite. | `[player]` suggests pending request senders. |
| `/ptp decline [player]` | standalone `tpno` | `sunlight.ptp.command.decline` | Decline a pending teleport request/invite. | `[player]` suggests pending request senders. |
| `/ptp request <player>` | — | `sunlight.ptp.command.request` | Send a teleport-to-player request. | Target must be online/valid per checks. |
| `/ptp invite <player>` | — | `sunlight.ptp.command.invite` | Invite a player to teleport to you. |  |
| `/ptp toggle [target]` | — | `sunlight.ptp.command.toggle` | Toggle receiving PTP requests. | Others: `sunlight.ptp.command.toggle.others`. |
| `/ptp on [target]` | — | `sunlight.ptp.command.toggle` | Enable receiving PTP requests. | Others: `sunlight.ptp.command.toggle.others`. |
| `/ptp off [target]` | — | `sunlight.ptp.command.toggle` | Disable receiving PTP requests. | Others: `sunlight.ptp.command.toggle.others`. |

## Permissions

| Permission | Description |
| --- | --- |
| `sunlight.ptp.command.root` | PTP root command |
| `sunlight.ptp.command.accept` | Accept requests/invites |
| `sunlight.ptp.command.decline` | Decline requests/invites |
| `sunlight.ptp.command.invite` | Send teleport invites |
| `sunlight.ptp.command.request` | Send teleport requests |
| `sunlight.ptp.command.toggle` | Toggle PTP requests for self |
| `sunlight.ptp.command.toggle.others` | Toggle PTP requests for others |
| `sunlight.ptp.bypass.requests.disabled` | Bypass target having requests disabled |
| `sunlight.ptp.bypass.cost` | Bypass teleport cost |
| `sunlight.ptp.bypass.cooldown` | Bypass cooldown restrictions |

## Configuration

Source: `moduleImpl/ptp/config/PTPSettings.java`

| Key | Default | Description |
| --- | --- | --- |
| `Request_Timeout` | `60` | Request timeout in seconds. |
| `Teleport.Cost` | `0D` | Teleport cost charged (to the teleported player as implemented) when configured. |

## Placeholder

- `%ptp_requests_enabled%`

## Behavior notes

- Stores pending requests per target UUID and expires them based on timeout.
- Supports accept/decline events and integrates with Settings module audience/auto-accept behavior where available.
- Requests are cleared on player quit.
- Costs/cooldowns can be bypassed with permissions.
