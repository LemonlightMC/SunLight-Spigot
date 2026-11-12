# Profiles

Provides player profiles/scopes with profile switching, warmups/cooldowns, combat restrictions, synchronization, LuckPerms context hooks, and GUI management.

* **Module ID:** `profiles`
* **Display name:** `Profiles`
* **Module directory:** `moduleImpl/profiles/`
* **API/provider:** implements `ProfilesProvider`

## Commands

Implemented by `ProfilesCommandProvider`.

Command functionality includes:

* Listing/viewing profiles
* Switching profiles
* Profile management operations (admin/player flows as implemented)

> See `moduleImpl/profiles/command/ProfilesCommandProvider.java` and generated command config for exact command syntax.

## Permissions

Defined in `moduleImpl/profiles/config/ProfilesPerms.java`.

Permission areas typically include:

* Profile command access (list/switch)
* Admin/management permissions
* Bypass permissions for switch restrictions/cooldowns/costs/warmups/combat checks (as implemented)

## Configuration

* **Settings:** `ProfilesSettings.java`
* **Language:** `ProfilesLang.java`
* **Placeholders:** `ProfilesPlaceholders.java`

Configuration and behavior areas:

* Profile scopes and profile definitions
* Switch guards/validation (affordability, conditions, combat restrictions, etc.)
* Warmups/cooldowns/costs for switching
* Synchronization behavior between profile states/scopes
* UI/menu options

## API behavior (high level)

`ProfilesModule` exposes profile switching/request flows (e.g. `requestSwitch(Player, query)`):

* Validates guards and affordability
* Starts a warmup or executes immediately depending on rules/state

Also provides menu entry points such as:

* `openProfilesMenu`
* `openProfileOptions`

## Components

* Hook integration (LuckPerms context hooks)
* Menus/UI for profile selection and options
* Scope/switch/sync subsystems

## Notes

* Profile switching can interact with Warmups, combat hooks, economy/cost checks, and permission systems.
* Exact scope definitions and synced data depend on generated module configuration.
