# 0017 Legacy Folder Merge Policy
#type/decision #status/accepted #area/migration

Links: [[../DDS]], [[../areas/Migration]], [[../areas/Sim Integration]], [[../areas/Shared Core]]

## Decision

The legacy `Commander analyst` and `Commander-Sim` folders are temporary.

Commander Anthology should eventually become one unified project rather than
two legacy project folders sitting beside each other indefinitely.

The legacy folders should be emptied of active product code/data over time as
their useful contents are migrated, ported, replaced, or deliberately retired.

## What This Does Not Mean

This does not mean deleting everything.

It means:

- preserve user data
- preserve useful tested behavior
- preserve design lessons
- port or replace valuable functionality
- retire obsolete scaffolding only after a deliberate migration decision

## Consequences

- Legacy folders are reference/staging areas, not permanent architecture.
- New Anthology work should move toward a unified root project structure.
- Migration work must track what has been ported, preserved, or retired.
- User data must be identified and protected before folder cleanup.

