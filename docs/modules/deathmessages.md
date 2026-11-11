# Death Messages Module

Customizes player death messages based on damage/entity context.

- **Module ID:** `death_messages`
- **Display name:** `Death Messages`
- **Module directory:** `moduleImpl/deathmessages/`

## Commands

This module does not register dedicated commands.

## Permissions

No dedicated permission nodes were identified in the module package. Behavior is configuration-driven.

## Configuration

Source: `moduleImpl/deathmessages/DeathMessagesSettings.java`

The module defines configurable death message mappings for different contexts (damage causes/entities and related death scenarios). Messages can be randomized/selected based on configured entries and context resolution logic implemented in `DeathMessagesModule`, `DeathContext`, `DeathMessage`, and `DeathMessagesListener`.

> Inspect the generated module `settings.yml` for the full list of message keys, contexts, and defaults in your build.

## Behavior notes

- Listens to death events and replaces/handles death message output according to configured rules.
- Supports context-aware message selection (e.g. by killer/damage type as implemented in source classes).
- MiniMessage/formatting support depends on the message handling pipeline used by your build/configuration.
