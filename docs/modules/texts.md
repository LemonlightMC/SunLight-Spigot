# Texts (Custom Text) Module

Maps configurable text files/pages to custom commands, allowing you to display formatted information (rules, help pages, announcements, etc.) to players.

- **Module ID:** `custom_text`
- **Display name:** `Custom Text`
- **Provider ID:** `texts-text`

## Commands

### Static command
| Command | Permission | Description | Usage |
| --- | --- | --- | --- |
| `/customtext <name>` | `sunlight.customtext.command.text` | Displays a configured custom text by name/ID. | Suggestions are filtered to texts the sender can access. |

### Dynamic per-text commands
Each loaded text registers its own dynamic command:

- **Command name:** text ID
- **Permission:** `sunlight.customtext.text.<id>`
- **Description:** taken from the text definition
- **Behavior:** displays the text content to the player (with supported placeholders/formatting)

## Permissions

| Permission | Description |
| --- | --- |
| `sunlight.customtext` | Module permission namespace root |
| `sunlight.customtext.command.text` | Use `/customtext <name>` |
| `sunlight.customtext.text` | Text permission namespace root |
| `sunlight.customtext.text.<id>` | Access/display a specific text and its dynamic command |

## Configuration / data files

- Text files are stored under the module texts directory (e.g. `texts/*.yml`).
- A default `rules` text may be generated when the directory is missing.
- Text definition fields include:
 - `Description` — command/text description
 - `Text` — list of message lines/content

### Formatting and placeholders

- Supports player placeholders when displaying text (as implemented by `Text` display logic).
- MiniMessage and PlaceholderAPI support are available in SunLight text/message handling where applicable.

## Language

- Command descriptions and feedback messages can be customized in the module language/messages.

## Notes

- Custom texts are a simple way to publish server information pages without creating separate plugins/commands.
- Restrict sensitive texts using per-text permissions.
- After adding/editing text files, reload the module/plugin as needed so dynamic commands and loaded texts are refreshed.