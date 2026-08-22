# CCBuilder Deep Dive
#type/source #status/active #area/ccbuilder #area/migration #project/commander-anthology

Links: [[../DDS]], [[Legacy Software Study]], [[../areas/Migration]], [[../areas/Deck Catalog]], [[../areas/Platform]]

## Purpose

This note records a deeper read of the working CCBuilder / Commander Analyst
software. It is the migration reference for Anthology deck-builder and
collection work.

## Project Shape

Root: `Commander analyst/`

Modules:

- `shared-core`: Kotlin/JVM shared serializable model module.
- `desktop`: Kotlin Compose Desktop app. This is the most complete CCBuilder
  implementation.
- `app`: Android Compose app. This has Room-backed local CRUD but is lighter
  than desktop.

Build direction:

- root uses Gradle Kotlin DSL and version catalog aliases.
- desktop uses Compose Multiplatform, Material3, Kotlin serialization, and
  SQLite JDBC.
- Android uses Compose Material3, Navigation Compose, Room, WorkManager,
  Retrofit, OkHttp, Kotlin serialization, and Coil.

## Shared Core

Source folder:

- `Commander analyst/shared-core/src/main/kotlin/com/commanderanalyst/core/model/`

Files:

- `DeckModels.kt`
- `CollectionModels.kt`
- `CardModels.kt`
- `SyncModels.kt`

Important shared model:

- `Deck`: `id`, `name`, `commanderName`, `containerId`
- `DeckSlot`: deck intent row with `desiredQuantity`, `section`, identity
  status, oracle identity, and preferred printing fields
- `DeckAssignment`: link between a deck slot and a physical inventory entry
- `Container`: physical or logical place cards live
- `InventoryEntry`: printing-aware physical card row with quantity and foil
- `SyncBundle`: `schemaVersion`, export time, containers, inventory entries,
  decks, deck slots, deck assignments

Migration rule:

- Anthology should preserve this model boundary before adding new fields.
- Deck intent is not a plain list of text. It is a mutable model with physical
  assignment and resolved identity.

## Desktop App

Source folder:

- `Commander analyst/desktop/src/main/kotlin/com/commanderanalyst/desktop/`

Files:

- `Main.kt`: Compose UI and UI-local parsing/dialog helpers.
- `DesktopAppState.kt`: mutable application state and all deck/collection
  mutation rules.
- `DesktopPersistence.kt`: JSON `SyncBundle` load/save.
- `ScryfallClient.kt`: lookup, local SQLite cache, detail cache, bulk index,
  card detail model.
- `ScryfallBulkDataClient.kt`: bulk-data update/install workflow.

Desktop tabs:

- Decks
- Collection
- Search
- Sync

Desktop deck builder behavior:

- left pane lists decks as selectable cards.
- each deck row has edit/delete actions.
- right pane shows selected deck detail.
- selected deck detail has add card, import, edit deck, delete deck.
- selected deck detail shows wanted/assigned/available/missing stats.
- deck rows show status symbols:
  - assigned complete
  - partially assigned
  - available in collection
  - missing
- deck rows show count, title, subtitle, badges, edit/delete actions.
- clicking the row status assigns or unassigns depending on state.
- title opens card details.
- assignment opens a choose-copy dialog, not just "first match".

Desktop collection behavior:

- left pane lists containers with type and card count.
- container rows have edit/delete actions.
- right pane shows selected container detail.
- selected container detail has add card, import, edit container, delete
  container.
- card rows show count, printing/oracle identity, badges, edit/move/delete.
- collection import supports pasted text and CSV file loading.

Desktop import behavior:

- deck import is a two-column dialog: pasted decklist on the left, review rows
  on the right.
- deck import supports section headers, ignored side/maybe/token sections,
  quantity-first rows, named commander lines, and Scryfall resolution.
- imported rows may be unresolved, resolved, or resolved to a preferred
  printing.
- collection import has a similar review flow, including ambiguous printing
  choices.

Desktop Scryfall/cache behavior:

- local-first lookup is already present.
- SQLite cache can be built from `default_cards.json`.
- detail cache stores card detail JSON by Scryfall id.
- bulk card cache has an index snapshot.
- Scryfall API is fallback when local cache cannot answer.
- card details include image URL, type line, oracle text, legalities, prices,
  and related URIs.

Desktop persistence:

- default file:
  `C:\Users\tarad\AppData\Roaming\Commander Analyst\commander-analyst-data.json`
- format is JSON `SyncBundle`.
- existing observed fixture:
  - containers: `59`
  - inventory entries: `2954`
  - decks: `4`
  - deck slots: `366`
  - deck assignments: `26`

## Android App

Source folder:

- `Commander analyst/app/src/main/java/com/commanderanalyst/`

Android is useful, but it is not as complete as desktop.

Persistence:

- Room database: `commander-analyst.db`
- schema version: `3`
- entities: containers, inventory entries, decks, deck slots
- migrations add inventory in v2 and decks/deck slots in v3

Android repositories:

- `ContainerRepository`: containers and manual inventory CRUD
- `DeckRepository`: decks and deck slot CRUD

Android ViewModels:

- `CollectionViewModel`
- `DeckViewModel`

Android UI:

- `CommanderAnalystApp.kt`
- tabs: Decks, Collection, Search, Settings
- top bar uses Cynful logo and Commander Analyst branding
- deck and collection screens are editable Compose screens

Android gaps versus desktop:

- no deck assignment model in Room schema
- no resolved oracle/printing identity in Room entities
- no Scryfall lookup/cache workflow in Android UI
- no JSON sync bundle import/export path visible in Android app
- no collection/deck physical assignment flow

## Anthology Gap From This Study

The current Anthology Java desktop work imported pieces of the data model, but
the UI surface is not yet CCBuilder-equivalent.

Corrected Anthology migration slices now present:

- deck builder uses a two-pane deck/detail layout instead of a plain list.
- deck builder exposes create/import/edit/delete for decks and slots.
- deck builder shows wanted/assigned/available/missing stats.
- deck and collection input now resolve card identity through the existing
  CCBuilder Scryfall SQLite cache when it is present.
- card lookup now cleans dirty CCBuilder deck-slot names with appended
  collector/set/foil suffixes before resolving, e.g. `Sol Ring 12 *F*` and
  `Lantern of Insight 5DN-135`.
- deck builder supports assignment and unassignment against physical inventory
  rows.
- deck assignment now presents matching physical copies for explicit selection.
- Card Codex workspace now exposes local Scryfall cache search and card detail
  inspection.
- Card Codex workspace now exposes local Scryfall cache status.
- Card Codex workspace now includes bulk-data check/install/update controls and
  an adopt-existing-SQLite action for migrating the CCBuilder cache into
  Anthology AppData.
- Card Codex workspace can build Anthology's own SQLite cache from
  `default_cards.json`.
- card lookup now has API fallback after local SQLite and bundled seed miss.
- deck and collection rows can open local Scryfall detail dialogs for resolved
  card IDs.
- card detail dialogs attempt image rendering from stored Scryfall image URLs.
- card images are cached in Anthology AppData after first successful load.
- Sync workspace can export/import the user-owned desktop state JSON as a local
  bundle.
- collection uses a two-pane container/detail layout instead of a plain list.
- collection exposes create/edit/delete for containers.
- collection exposes add/edit/move/delete for inventory rows.
- collection rows show quantity, card name, identity, printing, and foil.
- collection import now supports pasted rows and CSV review against the local
  Scryfall cache.
- collection import can preserve exact Scryfall IDs from CSV exports.
- collection import has selected-printing review for rows with multiple local
  cache matches.
- collection move behavior merges matching physical card rows in the target
  container.
- a bundled card lookup seed exists for minimal validation before the full local
  card cache is available.
- existing CCBuilder user data can be imported as a desktop test fixture.
- live Anthology desktop state has been restored from CCBuilder data and
  validated to zero unresolved card input rows.

Remaining specific gaps:

- status symbols/badges must be represented in the UI.
- import must have a review step, not only paste-and-accept.
- cache management still needs hardening on full-size runs and UI progress
  polish, but the major local/offline paths are present.
- collection import review still needs CCBuilder-level polish and richer
  per-row visual badges.
- collection rows still need richer badges/card-detail access from the
  Scryfall/detail cache.

## Migration Rule

For CCBuilder migration, do not treat a storage-compatible model as enough.

A migrated subsystem needs:

- the shared model shape
- the mutation behavior
- the persistence path
- the local/offline data path
- the user workflow
- the visual/status language
- enough verification to prove the behavior survived the move
