# Deck Catalog
#type/area #area/deck-catalog #status/draft

Links: [[../DDS]], [[Shared Core]], [[Card Codex]], [[Data Regression]], [[Player Style]], [[../decisions/0009 Deck Data And Precon Catalog]]

## Purpose

Deck Catalog defines how Anthology stores, labels, protects, imports, analyzes,
and syncs decks.

Anthology is Commander-only, so deck records do not need a per-deck `format`
field.

The system must distinguish:

- user-created/user-imported decks
- bundled preconstructed decks
- generated/reference deck records
- game/session copies used by Commander Sim

## Existing User Data

There is current input data in both legacy Sim and Builder. This is real user
data entered by the project owner and must be treated as user-owned data during
migration.

It must not be overwritten, deleted, or silently converted into reference data.

## Bundled Precon Decks

Anthology should ship with Commander preconstructed decks by default for the
game/simulation side.

These decks provide useful starting data for:

- immediate play/testing
- deck analysis
- legal move package testing
- early play-pattern analysis
- examples for new users

Bundled precons must be:

- marked as `precon`
- locked against accidental deletion
- visually distinguishable from user decks
- copyable/duplicable so users can modify their own version
- excluded from being mistaken for user-created style data

## Precon Source Direction

Precon deck data should be easy to source because Magic preconstructed decklists
are commonly posted online by Wizards of the Coast, official product pages,
articles, or reputable decklist/reference sites.

Accepted source direction: see [[../decisions/0018 Precon Source Policy]].

The project owner may provide precon lists from public sources. Moxfield export
is a likely practical import format. Anthology Dev Mode should import, resolve,
validate, and store local bundled `precon` records rather than depending on
Moxfield or another source at runtime.

Example official source pattern:

- `https://magic.wizards.com/en/news/announcements/marvel-super-heroes-commander-decklists`
- Page title: `Magic: The Gathering | Marvel Super Heroes Commander Decklists`
- Published by Wizards as an Announcements article.
- Contains product/deck metadata and named decklist sections such as `Avengers
  Assemble`, `Wakanda Forever`, `The Fantastic Four`, and `Doom Prevails`.

Anthology should treat online decklists as source/import material, not as a
runtime dependency:

1. Find the online precon decklist.
2. Import/parse it through Dev Mode into Anthology's deck format.
3. Resolve cards through the local/offline-first Card Codex path.
4. Validate Commander legality and deck counts.
5. Store/update a local bundled precon record with source metadata.
6. Use the local bundled copy during normal app use.

Bundled precon metadata should record:

- precon name
- commander(s)
- product/set/release info when known
- source URL or source citation
- source publisher/domain
- source page title
- source export format
- import date
- Anthology schema version
- whether the decklist has been validated

This keeps the app offline-first while still making precon updates practical.

## User Decks

Users may input many of their own decks.

User decks should be:

- clearly marked as user-owned/imported
- editable and deletable with normal safeguards
- searchable/filterable separately from precons
- eligible for collection/deck-building workflows
- eligible for user play/style analysis when used in Play mode

## Open Questions

- Should precons be stored as immutable bundled files plus user overlays?
- How should modified copies of precons be labeled?
- Which precon deck source/version is authoritative?

## Accepted Metadata Direction

See [[../decisions/0012 Deck Metadata Model]].

Every deck should carry:

- `deckId`
- `name`
- `commanderOracleIds`
- `createdAt`
- `updatedAt`
- `origin`
- `locked`
- `editable`
- `deletable`
- source metadata where available
- `copiedFromDeckId`
- `platformVisibility`
- `styleEligible`
- `syncEligible`
