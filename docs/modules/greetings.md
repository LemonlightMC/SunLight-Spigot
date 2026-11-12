# Greetings

Sends customizable join, first-join, and quit messages (and optional join commands) based on configured greeting rules.

* **Module ID:** `greetings`
* **Display name:** `Greetings`
* **Module directory:** `moduleImpl/greetings/`

## Commands

This module does not register dedicated commands.

## Permissions

No dedicated command/permission classes were identified in the module package. Greeting selection and behavior are configuration-driven.

## Configuration

Source: `moduleImpl/greetings/GreetingsSettings.java`

The module supports rank/priority-based greeting configuration through `GreetingMessage` entries and settings, typically including:

* Join messages
* First-join messages
* Quit messages
* Optional commands to execute on join (per greeting/message entry, as supported by implementation)

> See the generated `greetings/settings.yml` for the exact structure, keys, placeholders, and defaults.

## Behavior notes

* `GreetingsListener` applies greeting behavior on player join/quit events.
* Greeting selection can use priority/rank-based matching (as described in project README and implemented in greeting models).
* Integrates with other modules/features that respect greeting messages where applicable (e.g. vanish fake join/quit handling may interact with greeting behavior).
