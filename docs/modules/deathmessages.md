# Death Messages Module

Customizes player death messages based on damage/entity context.

- **Module ID:** `death_messages`
- **Display name:** `Death Messages`
## Commands

This module does not register dedicated commands.

## Permissions

No dedicated permission nodes were identified in the module package. Behavior is configuration-driven.

## Configuration

The module lets you configure death message mappings for different death causes and situations. Messages can be selected based on the configured context.

> Inspect the module's `settings.yml` for the full list of message keys, contexts, and defaults in your build.

## Behavior notes

- Replaces default death messages according to your configured rules.
- Supports context-aware message selection (for example by killer or damage type, depending on configuration).
- MiniMessage/formatting support depends on your server/build configuration.