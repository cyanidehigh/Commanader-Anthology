# 0014 Sync Bundle Schema
#type/decision #status/accepted #area/cross-platform-sync

Links: [[../DDS]], [[../areas/Cross Platform Sync]], [[../areas/Shared Core]], [[0006 Serverless Optional Cross Platform Sync]]

## Decision

Anthology sync uses a portable, provider-neutral sync bundle for user-owned
data.

The bundle is the same whether it is moved by manual import/export, local file
copy, Google Drive, a future user-owned cloud/storage provider, or a later
server-backed transport if one is deliberately added.

## First Bundle Shape

Initial bundle root fields:

- `bundleVersion`
- `createdAt`
- `sourceApp`
- `sourceDeviceId`
- `userDataRevision`
- `decks`
- `containers`
- `inventoryEntries`
- `deckAssignments`
- `playerStyleSummary`
- `settings`

## Bundle Includes

The sync bundle may include:

- user decks
- copied precons
- imported decks
- user-created containers
- collection inventory entries
- deck assignment records
- lightweight player style summaries
- portable settings that matter across devices
- schema/app metadata needed to validate imports

## Bundle Excludes

The sync bundle must not include:

- Scryfall bulk data
- SQLite cache or index files
- generated Card Codex caches
- bundled precon deck data that ships with the app
- logs
- crash dumps
- normal simulation sessions
- full replay archives unless explicitly exported through a separate workflow
- provider credentials or authentication secrets

## Precon Rule

Bundled precons are reference data, not user sync data.

If a user copies a precon, the copied deck becomes user-owned data and may sync.
The copied deck should retain lineage metadata such as `copiedFromDeckId`.

## Player Style Rule

The first sync schema should sync lightweight player style summaries, not raw
high-volume decision logs.

Raw decision/session data may be exported separately later if needed, but it is
not part of the normal cross-platform sync bundle.

## Conflict Rule

Conflicts must be represented and shown. They must not be silently overwritten.

The initial schema must carry enough metadata for conflict detection:

- `sourceDeviceId`
- `userDataRevision`
- per-record IDs
- per-record `updatedAt` values where applicable

The exact conflict-resolution UI is a later design decision.

## Consequences

- Sync stays cheap to operate because Anthology does not need to host user data.
- Google Drive can use the same bundle format as manual export/import.
- A future server can use the same bundle/domain model if server-backed sync is
  deliberately accepted later.
- Desktop and Android can remain standalone.
- Generated offline caches can be rebuilt locally instead of synced.
- The shared Java core should define bundle models and validation rules.
