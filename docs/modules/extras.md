# Extras Module (Unregistered)

`moduleImpl/extras/` contains an additional "Extras" module implementation in source, but it is **not registered** in `SunLightPlugin.registerModules()` and therefore is not loaded by the standard plugin build.

> If you are using an unmodified SunLight build, treat this module as unavailable. Custom forks/builds may wire it manually.

## Status

- **Source directory:** `moduleImpl/extras/`
- **Registered in core:** No
- **Effect:** No module enable entry/commands/features from this package unless explicitly integrated

## Implemented command providers (source)

The package includes providers such as:
- `BroadcastCommandProvider`
- `CondenseCommandProvider`
- `DisposalCommandProvider`
- `HatCommandProvider`
- `SignsCommandProvider`
- `SkullCommandProvider`
- `SmiteCommandProvider`
- `StaffCommandProvider`
- `SudoCommandProvider`
- `SuicideCommandProvider`

## Source components

- Module class: `ExtrasModule`
- Permissions/settings/lang: `ExtrasPerms`, `ExtrasSettings`, `SignsLang`
- Properties: `ExtrasProperties`
- UI: `DisposalMenu`
- Core/utility classes under `core/`

## Permission namespace note

`ExtrasPerms` uses `sunlight.essential...` namespaces for many command permissions (plus sign/anvil/starterkit/sign-bypass permissions), reflecting its utility/essential-like feature set in source.

## Recommendation

- Do not document this as an active default module in server admin workflows unless you have confirmed it is registered/enabled in your build.
- If you need these features, check your SunLight version/build or consider equivalent functionality in enabled modules (e.g. Essential/Inventories/Chat-related tools where applicable).
