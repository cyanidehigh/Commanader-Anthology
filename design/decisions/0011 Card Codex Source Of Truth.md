# 0011 Card Codex Source Of Truth
#type/decision #status/accepted #area/card-codex

Links: [[../DDS]], [[../areas/Card Codex]], [[../areas/Data Regression]], [[../areas/Shared Core]], [[0010 Shared Java Core Boundary]]

## Decision

The shared Java Card Codex is the source of truth for card identity and
validated card facts across Commander Anthology.

Scryfall `oracle_id` is the primary unique identity for normal card/rules
identity. Anthology should not continue using the old Commander Sim six-hex card
ID as the main identity now that the project is moving toward SQLite-backed
storage and indexes.

The old six-hex ID design existed mainly to support compact CSV-style move
identifiers for the AI. That constraint is no longer driving the architecture.

## Primary Identity

For normal official cards:

- `oracle_id` is the primary rules/card identity.
- Reprints share the same `oracle_id`.
- Reskins and alternate printings that Scryfall maps to the same `oracle_id`
  share the same rules identity.
- Deck intent, Commander legality, card rules behavior, player-style card
  references, and suggestion logic should use `oracle_id` for card identity.

## Printing Identity

Printing identity remains separate from oracle identity.

Collection and display workflows need printing-specific fields such as:

- Scryfall card/printing ID
- printed/display name
- set code
- collector number
- finish/foil state
- image references
- printed text/display metadata when relevant

Example:

`Assaultron Invader` from `PIP` may be the physical/display printing, while its
`oracle_id` ties it to the shared rules identity for `Walking Ballista`.

## Anthology Fallback Identity

Anthology may maintain a fallback internal identity, but it is not the normal
primary card key.

Fallback IDs are allowed for:

- unknown/manual cards before resolution
- custom/local test cards
- damaged or incomplete imported records
- future non-Scryfall objects if needed
- migration safety when historical data cannot be resolved immediately

Fallback IDs must be clearly typed and must not masquerade as Scryfall
`oracle_id` values.

## SQLite Direction

SQLite is the preferred local store/index direction for the Card Codex.

The Card Codex should keep normalized/indexed data for:

- oracle records keyed by `oracle_id`
- printing records keyed by Scryfall card/printing ID
- name lookup indexes
- set/collector lookup indexes
- legality fields
- type-line parsed fields
- source/update metadata
- local cache/build metadata

The Java shared core should define Card Codex models and repository interfaces.
Platform/storage layers provide concrete SQLite implementations.

## Offline-First Rule

Card Codex lookup follows the accepted offline-first data regression policy:

1. Validated local Card Codex / cache.
2. Local SQLite or indexed lookup data.
3. Local raw Scryfall bulk data when rebuilding local indexes.
4. Live Scryfall API only when local data is missing, stale, or cannot answer
   confidently.
5. Safe remote results may be cached locally.

Normal search, deck validation, collection matching, simulation, and suggestions
must not depend on live Scryfall access.

## Update And Migration Rules

- Remote updates must not silently corrupt identity.
- Existing resolved `oracle_id` references must remain stable.
- If Scryfall changes card facts, Anthology records update metadata and applies
  deterministic refresh rules.
- If an imported user record cannot be resolved, it remains a typed unresolved
  fallback record until the user or a later resolver fixes it.
- Existing Commander Sim six-hex IDs are migration/reference data only. They may
  help map old Sim records, but they are not the new primary identity.

## Consequences

- CCBuilder and Commander Sim converge on `oracle_id` as rules identity.
- Player-style records can refer to `oracle_id` instead of compact hex IDs.
- Move/event records no longer need CSV-friendly card IDs as the core storage
  identity.
- The old Sim Card Codex remains valuable reference material, especially for
  identity lessons and migration, but its six-hex IDs are not the main future
  key.
- The CCBuilder SQLite/cache work remains valuable performance reference.

