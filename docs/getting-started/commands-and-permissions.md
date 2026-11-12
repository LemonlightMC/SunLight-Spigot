# Commands & Permissions System

SunLight combines a global admin command, module command providers, and permission nodes for access control, bypasses, and feature toggles.

## Core command and permissions

### Root command
- `/sunlight` (alias `/sl`) — plugin administration command
 - **Base permission (this build):** `sunlight.admin`
 - **Subcommands:** `help`, `reload`, `reloadmodule`, `dump`, `export`, `import`, `cleanup`
 - **`cleanup` permission:** `sunlight.admin.cleanup`

> Some public documentation for other SunLight versions may reference different core permission nodes (for example `sunlight.command.reload`). Always use the permissions that match your build.

### Common core/global permission patterns
- `sunlight.admin` — access to SunLight root/admin command in this build
- `sunlight.admin.cleanup` — access to `/sunlight cleanup`
- Module/provider commands may additionally use command-system cooldown/cost bypass checks where implemented

## Command flags

SunLight commands commonly support flags (availability depends on the specific command/provider):

| Flag | Meaning |
| --- | --- |
| `-s` | Silent execution (target player is not notified, when supported) |
| `-f` | Force execution / bypass certain restrictions (when supported by the command) |

## Module/provider command configuration

Most module commands are registered through command providers and can be configured in `commands/<providerId>.yml`.

### Provider structure (guide)

A provider config can define **literal/standalone** command nodes and an optional **root** command that groups child subcommands. Exact key names and layout may vary by SunLight version/build, but the common structure is:

- **`LiteralNodes`**: standalone command entries (direct commands like `/home`, `/sethome`, `/kit`, etc.)
 - `Enabled` — enable/disable the standalone command
 - `Aliases` — command name(s)/aliases
 - `Cooldown` — cooldown in seconds
 - `Cost` (where supported) — usage cost
- **`RootNodes`** (optional): a parent command that exposes selected provider commands as children (e.g. `/homes set`, `/kits give`)
 - `Enabled` — enable/disable the root command
 - `Aliases` — root command aliases
 - `Name` — display name (used in help/UI where applicable)
 - `Childrens` / child mappings — maps provider command nodes to root subcommand aliases

#### Example (illustrative)

```yaml
LiteralNodes:
 preview:
 Enabled: true
 Aliases: viewkit
 Cooldown: 0
 give:
 Enabled: true
 Aliases: givekit
 Cooldown: 0

RootNodes:
 kits:
 Enabled: true
 Aliases: kits
 Name: Kits
 Childrens:
 preview: preview
 give: grant
```

> This example shows the concept (standalone commands + grouped root command). Use the generated `commands/<providerId>.yml` in your server as the authoritative source for available nodes, keys, and defaults.

## Permission conventions

Permissions are module/feature-specific and usually follow namespaces such as:

- `sunlight.admin`, `sunlight.admin.*` — core/admin features (build-dependent)
- `sunlight.<module>.command.*` — module command access
- `sunlight.<module>.bypass.*` — cooldown/cost/restriction bypasses
- `sunlight.<feature>.*` — feature access, panel/settings toggles, or per-object access (e.g. per-warp/per-spawn/per-text)

Always refer to the module's page for the authoritative list.

## Bypasses and restrictions

Many modules implement bypass permissions for:

- Command cooldowns
- Command/teleport costs
- World/region/protection restrictions
- Safety checks or access requirements
- Immunity/protection from staff actions (e.g. freeze/punishment-related where implemented)

These are powerful permissions—grant them carefully.

## Admin and "others" variants

Common patterns you will see across modules:

- `...command.<name>` — use the command for yourself
- `...command.<name>.others` — target other players
- `...command.<module>admin...` or separate admin roots — administrative management commands

## Targeting, suggestions, and GUI access

- Some commands accept optional targets and require `.others` permissions.
- Some modules provide GUI editors/menus; command access and feature permissions may be separate.
- Command argument names/syntax messages are localized via core language keys (`Command.Argument.Name.*`, syntax/error messages).

## Troubleshooting

- **Command not found:** ensure the module is enabled in `modules.yml`, the command node is enabled in `commands/<providerId>.yml`, and the server was reloaded/restarted as needed.
- **Permission denied:** verify the exact node from the module page and check inherited group permissions.
- **Command conflicts:** SunLight's registry handles conflicts/unregistration in its command setup; check logs if aliases overlap with other plugins.
- **Dependency-gated features:** some commands or features may only be available when required plugins/integrations are installed or when your server version supports them.