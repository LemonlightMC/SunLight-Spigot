# Links Module

Manages server links (website, Discord, store, map, etc.) with player commands, an admin editor, optional click commands, and click tracking.

- **Module ID:** `links`
- **Display name:** `Links`
- **Provider IDs:** `links` (player), `linksadmin` (admin)

## Commands

> Configurable in `commands/links.yml` and `commands/linksadmin.yml`.

### Player commands
| Command | Permission | Description | Usage / notes |
| --- | --- | --- | --- |
| `/links` | `sunlight.links.command.links` | Links root command (opens/shows links UI/list behavior as implemented). | |
| `/links open` | `sunlight.links.command.links` | Opens the links GUI/menu of enabled actionable links. | Menu can be enabled/disabled in settings. |
| `/links show <id>` | `sunlight.links.command.links` | Shows/activates a specific link. | `<id>` suggests available link IDs. |

### Admin commands
| Command | Permission | Description | Usage |
| --- | --- | --- | --- |
| `/linksadmin create <id>` | `sunlight.links.command.linksadmin.create` | Creates a new link entry. | `<id>` is the link identifier. |
| `/linksadmin delete <id>` | `sunlight.links.command.linksadmin.delete` | Deletes a link. | |
| `/linksadmin edit <id>` | `sunlight.links.command.linksadmin.edit` | Opens link editor for a link. | |
| `/linksadmin reload` | `sunlight.links.command.linksadmin.reload` | Reloads links module/config data. | |

## Permissions

| Permission | Description |
| --- | --- |
| `sunlight.links.command.links` | Player links command access (including `show`/`open` behavior) |
| `sunlight.links.command.linksadmin` | Admin links command root |
| `sunlight.links.command.linksadmin.create` | Create links |
| `sunlight.links.command.linksadmin.delete` | Delete links |
| `sunlight.links.command.linksadmin.edit` | Edit links |
| `sunlight.links.command.linksadmin.reload` | Reload links |

## Configuration

| Key | Default | Description |
| --- | --- | --- |
| `Links.Menu.Enabled` | `true` | Enables the links GUI/menu for players. |
| `Links.Menu.Title` | `"Links"` | Title of the links menu. |

Link entries are stored in module configuration/data managed by /`Link` models. Each link can include:

- Link ID and display properties
- URL and/or command action
- Enabled/actionable state
- Click counter/statistics
- Optional command execution context (console or player), as supported by implementation

## Placeholders

- Link placeholders are registered by.
- Socials module may re-register legacy `socials_<id>` placeholders that delegate to Links data for compatibility.

## Editor and UI

- provides in-game editing/management workflow for admins.
- Player list/menu shows enabled, actionable links sorted by ID (misconfigured links may be hidden until repaired).

## Notes

- Use per-link commands/actions carefully, especially if commands run as console.
- Click counters can be reviewed/reset through editor/management features where available.
- After structural/config changes, use `/linksadmin reload` or module reload.