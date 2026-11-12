---
description: "Guide to the Nametags module in SunLight for Minecraft servers, covering commands, permissions, configuration, features, and usage for admins and players."
---

# Nametags

Provides customizable player nametags (prefix/suffix/tag/rank/profile display) rendered through TAB/TAB-Bridge, with GUI/dialog management and dynamic placeholder support.

* **Module ID:** `nametags`
* **Display name:** `Nametags`
* **Module directory:** `moduleImpl/nametags/`
* **Load condition:** `LoadCondition.tabOrBridge` (module is only registered/loaded when the condition is satisfied)

## Requirements

* **TAB** or **TAB-Bridge** integration (required by load condition)
* **PlaceholderAPI** may be required in TAB-Bridge-only scenarios (per module load condition implementation)

## Commands

Implemented by `NametagsCommandProvider`.

* Provides nametag/tag/rank/profile management and UI commands (exact command tree, aliases, and arguments are defined in the provider).
* Command configuration can be customized via the provider's `commands/<providerId>.yml` file.

## Permissions

Defined in `moduleImpl/nametags/config/NametagsPerms.java`.

Permission areas typically include:

* Command access for nametag management/UI
* Administrative editing/management permissions
* Player-facing tag/profile selection permissions (as implemented)

> Refer to `NametagsPerms` and `NametagsCommandProvider` for the exact permission nodes.

## Configuration

* **Settings:** `NametagsSettings.java`
* **Language:** `NametagsLang.java`
* **Placeholders:** `NametagsPlaceholders.java`

The module supports (as implemented by its subsystems):

* Rank and team prefixes
* Per-player tags
* Purchasable/subscription tags (where configured)
* Named per-world profiles
* GUI/dialog-based editing and selection
* Player options to suppress rank/tag/team parts or reduce nametag display

## Placeholders

* `%nametags_prefix%`
* `%nametags_suffix%`
* Additional tag/rank/profile placeholders with payload-aware resolution (registered in module placeholder handling)

## Implementation components

* Backends: TAB/TAB-Bridge integration layers (`backend/*`)
* Core logic: nametag composition/profile/tag/rank handling (`core/*`)
* UI: menus and dialogs for editing/selection (`menu/*`, `dialog/*`)
* Sources/listeners/events: data sources, updates, and `NametagUpdateEvent`

## Important notes and limitations

* Nametags rendering ownership depends on TAB integration; if required dependencies are missing, the module will not load due to its load condition.
* If nametags do not update as expected, verify TAB/TAB-Bridge compatibility, PlaceholderAPI availability (if needed), and module/profile configuration.

## Related documentation

- [Glow module](../modules/glow.md)
