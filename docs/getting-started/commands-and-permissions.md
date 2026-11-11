# Commands & Permissions System

SunLight combines a global admin command, module command providers, and permission nodes for access control, bypasses, and feature toggles.

## Root command

- `/sunlight` (alias `/sl`) — plugin administration command
  - Base permission: `sunlight.admin`
  - Subcommands include `help`, `reload`, `reloadmodule`, `dump`, `export`, `import`, `cleanup`
  - `cleanup` requires `sunlight.admin.cleanup`

See [Core Configuration](core-configuration.md#root-admin-command-sunlight).

## Module/provider command configuration

Most module commands are registered through command providers and can be configured in:

- `commands/<providerId>.yml`

Typical configurable fields per command node:

| Field | Description |
| --- | --- |
| `enabled` | Enables/disables the command node |
| `aliases` | Command aliases for the node/root |
| `cooldown` | Cooldown applied by SunLight's command system (provider/node-specific behavior) |
| `cost` | Command usage cost (economy integration/handling depends on module/command implementation) |

> The exact node names and available entries depend on each provider/module. Check the generated file for that provider after startup/reload.

## Permission conventions

Permissions are module/feature-specific and usually follow namespaces such as:

- `sunlight.command.*` or `sunlight.admin*` for core/admin features
- `sunlight.<module>.command.*` for module command access
- `sunlight.<module>.bypass.*` for cooldown/cost/restriction bypasses
- `sunlight.<feature>.*` for feature access, panel/settings toggles, or per-object access (e.g. per-warp/per-spawn/per-text)

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
- **Dependency-gated features:** some commands/features only register when required dependencies or NMS/hooks are available.
