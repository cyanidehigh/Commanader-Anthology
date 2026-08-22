# 0012 Deck Metadata Model
#type/decision #status/accepted #area/deck-catalog

Links: [[../DDS]], [[../areas/Deck Catalog]], [[../areas/Shared Core]], [[0009 Deck Data And Precon Catalog]]

## Decision

Every Anthology deck must carry enough metadata to distinguish user data,
bundled precons, imports, generated/reference records, and copied decks.

Anthology is Commander-only, so deck metadata does not need a `format` field.
Commander is the product assumption, not per-deck metadata.

## Minimum Deck Metadata

Core identity:

- `deckId`
- `name`
- `commanderOracleIds`
- `createdAt`
- `updatedAt`

Origin and protection:

- `origin`
- `locked`
- `editable`
- `deletable`

Source and lineage:

- `sourceName`
- `sourceUrl`
- `sourcePublisher`
- `sourceImportedAt`
- `copiedFromDeckId`

Platform and data behavior:

- `platformVisibility`
- `styleEligible`
- `syncEligible`

## Origin Values

Initial origin values:

- `user`
- `precon`
- `imported`
- `generated`
- `copied_precon`

## Policy

Bundled precons:

- `origin=precon`
- `locked=true`
- `editable=false`
- `deletable=false`
- `styleEligible=false`
- `syncEligible=false` unless copied
- visible as precons

User decks:

- `origin=user`
- editable
- deletable with normal safeguards
- style eligible when used by the human in Play mode
- sync eligible

Imported decks:

- user-owned after import
- editable
- deletable with normal safeguards
- retain source metadata where available
- style eligible when used by the human in Play mode
- sync eligible

Copied precons:

- editable user-owned copies of bundled precons
- retain `copiedFromDeckId`
- style eligible when used by the human in Play mode
- sync eligible

Generated/reference records:

- must be clearly marked
- must not be confused with user decks
- may be locked or read-only depending on generation purpose

## Consequences

- The UI can safely prevent accidental deletion of bundled precons.
- Users can have many decks without confusing them with shipped examples.
- Sync can ignore bundled precons and sync only user-owned/copy data.
- Player style analysis can avoid treating bundled precons as user-owned deck
  preferences unless the user copies or plays them intentionally.

