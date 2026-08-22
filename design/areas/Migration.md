# Migration
#type/area #area/migration #status/active

Links: [[../DDS]], [[../decisions/0017 Legacy Folder Merge Policy]], [[Shared Core]], [[Sim Integration]]

Accepted policy: [[../decisions/0025 Proven Legacy Behavior Migration Policy]]
sets the bias toward porting proven legacy behavior instead of rewriting from
scratch.

## Purpose

Migration defines how the legacy `Commander analyst` and `Commander-Sim`
folders move into one unified Commander Anthology project.

## Direction

The current legacy folders are temporary staging/reference areas.

Eventually, they should be emptied of active product code and data as useful
behavior, user data, tests, and design lessons are migrated into one unified
Anthology project structure.

## Guardrails

- Do not delete user data.
- Do not delete useful tested behavior before it is ported, replaced, or
  deliberately retired.
- Do not keep legacy folders as permanent product architecture.
- Preserve source context until migration is complete.
- Move deliberately by subsystem: data, shared core, Card Codex, decks,
  collection, Sim foundation, UI, tests.

## Migration States

Suggested status labels:

- `reference`
- `pending_migration`
- `migrating`
- `ported`
- `retired`
- `preserved_user_data`

## Open Questions

- What is the final root project layout?
- Which legacy data is user-owned and must be migrated first?
- Which legacy tests become Java reference tests?
- Which Python/Kotlin code is ported versus retired?
