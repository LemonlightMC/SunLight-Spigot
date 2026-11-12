---
description: "Guide to the Voting module in SunLight for Minecraft servers, covering commands, permissions, configuration, features, and usage for admins and players."
---

# Voting

Advanced in-game voting/polling system with templates, wizard-based creation, weighted voting, paid/gated choices, schedules/recurrence, runoff handling, history, and rich GUIs.

* **Module ID:** `voting`
* **Display name:** `Voting`
* **Module directory:** `moduleImpl/voting/`

## Commands

Implemented by command providers:

* `VotingCastCommandProvider` — player voting/casting commands (e.g. vote choices, shortcuts)
* `VotingStaffCommandProvider` — staff/admin poll management
* `VotingTemplateCommandProvider` — template-based poll operations
* `VotingWizardCommandProvider` — wizard-driven creation/edit flows

Capabilities include (as implemented):

* Opening and managing polls
* Casting votes (`/yes`, `/no`, `/neutral` and additional choices)
* Closing/extending/cloning/tuning polls
* Viewing active polls and history
* Player reminders/notifications preferences integration

> See provider classes and generated command configs for exact command trees and arguments.

## Permissions

Defined in `moduleImpl/voting/config/VotingPerms.java`.

Permission areas typically include:

* Voting/casting permissions
* Staff/admin poll management permissions
* Template/wizard access
* Exemptions, notifications, and vote multiplier-related permissions (referenced by settings/features)

## Configuration

Source: `moduleImpl/voting/VotingSettings.java` (extensive configuration).

Key areas:

### Data and maintenance

* `Voting.Data.Table-Prefix` (default `sunlight_voting`)
* `Sweep-Seconds` (default `60`)
* `Keep-Concluded-Days` (default `30`)

### Choices and icons

* Built-in choices: `yes`, `no`, `neutral`
* Choice icons (e.g. `LIME_DYE`, `RED_DYE`, `GRAY_DYE`, default `PAPER`)

### Limits and participation

* `Max-Active-Votes` (default `10`)
* `Max-Votes-Per-Creator` (default `3`)
* `Max-Participating-Votes` (default `3`)
* `Min-Options` (default `2`), `Max-Options` (default `15`)
* `Min-Playtime-Hours` (default `0`)
* World filtering mode (`BLACKLIST` by default) and world lists
* Exempt permissions support

### Voting rules

* Multi-vote support (`Max-Votes`, default `3` when enabled)
* Money/payment gates and weighting modes (`NONE/GATE/WEIGHT/BOTH`, combine modes such as `HIGHEST`/`ADDITIVE`)
* Resolution/tie-break options
* Secret ballot and silent poll options
* Revote/change rules and late-lock behavior
* Runoff behavior and pass thresholds
* Recurrence/scheduling/spawn limits/delays

### Notifications and outcomes

* Reminders and join nudges (players can opt out via settings/commands where supported)
* Result broadcasts
* Outcome command hooks (pass/fail/cancel/vote/runoff and per-vote actions)

## Placeholders

Source: `VotingPlaceholders` and module registrations.

Common placeholder areas:

* Vote metadata: `%vote_name%`, `%vote_key%`, `%vote_status%`, times, outcome
* Vote flags: secret/silent/recurring
* Option/tally values: totals, participants, score, percent, count (context-dependent)
* Additional dynamic vote/option placeholders

## GUI, dialogs, and data

* Menus: votes menu, vote view menu, vote wizard menu
* Wizard dialogs for poll creation/editing and rule configuration
* Database-backed storage: `VotingDataManager`, `VoteRepository`, `BallotRepository`
* Models/enums for rules, tallies, statuses, options, templates, and outcomes

## Important notes and limitations

* Voting supports complex governance/event polling scenarios; start with templates or simple polls before enabling advanced rules (weighting, runoff, recurrence, paid choices).
* Payment/refund behavior for gated choices is handled by module logic according to configured rules and vote outcomes.
* Use permission scoping for staff poll management vs player voting.
