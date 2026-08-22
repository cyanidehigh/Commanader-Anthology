# Legacy Software Study
#type/source #status/active #area/migration #area/ccbuilder #area/sim-integration

Links: [[../DDS]], [[../areas/Migration]], [[../CCBuilder]], [[../Commander Sim]], [[../decisions/0025 Proven Legacy Behavior Migration Policy]]

Related detailed source: [[CCBuilder Deep Dive]]

## Purpose

This note records what has actually been inspected in the two legacy software
trees before further migration work.

The migration rule is simple: study first, then port proven behavior into
Anthology.

## CCBuilder / Commander Analyst

Root folder: `Commander analyst/`

CCBuilder is working legacy software, not disposable prototype material.

### Structure Found

- `app/`: Android app module
- `desktop/`: Compose Desktop app module
- `shared-core/`: Kotlin/JVM shared model module
- `data/scryfall-bulk-data/`: local Scryfall bulk data, lookup indexes, details
  cache, and SQLite cache

### Desktop Behavior Found

Important files:

- `Commander analyst/desktop/src/main/kotlin/com/commanderanalyst/desktop/Main.kt`
- `Commander analyst/desktop/src/main/kotlin/com/commanderanalyst/desktop/DesktopAppState.kt`
- `Commander analyst/desktop/src/main/kotlin/com/commanderanalyst/desktop/DesktopPersistence.kt`
- `Commander analyst/desktop/src/main/kotlin/com/commanderanalyst/desktop/ScryfallClient.kt`
- `Commander analyst/desktop/src/main/kotlin/com/commanderanalyst/desktop/ScryfallBulkDataClient.kt`

Useful behavior already present:

- desktop shell with Decks, Collection, Search, and Sync workspaces
- deck create, edit, delete
- deck slot create, edit, delete
- collection container create, edit, delete
- inventory entry create, edit, move, delete
- physical card assignment from collection to deck slots
- duplicate physical inventory merge rules
- local desktop persistence
- deck import parsing
- collection text import parsing
- collection CSV import parsing
- Scryfall lookup by name and set
- Scryfall printing options
- Scryfall card detail cache
- Scryfall bulk-data install/update workflow
- SQLite cache build from `default_cards.json`

### Deck Builder Migration Priority

The CCBuilder deck builder is currently the most complete legacy product
surface and should lead the Anthology desktop migration.

Why it leads:

- it already models deck intent separately from physical inventory
- it tracks deck slots by section and desired quantity
- it resolves card identity through Scryfall data when available
- it keeps printing preferences separately from oracle identity
- it assigns physical collection copies into deck slots
- it distinguishes assigned, available, and missing deck cards in the UI
- it imports decklists from pasted text
- it persists through a JSON `SyncBundle`
- it already matches the accepted offline-first and sync-bundle direction more
  closely than the temporary root Java shell does

Legacy shared-core model files inspected:

- `Commander analyst/shared-core/src/main/kotlin/com/commanderanalyst/core/model/DeckModels.kt`
- `Commander analyst/shared-core/src/main/kotlin/com/commanderanalyst/core/model/CollectionModels.kt`
- `Commander analyst/shared-core/src/main/kotlin/com/commanderanalyst/core/model/CardModels.kt`
- `Commander analyst/shared-core/src/main/kotlin/com/commanderanalyst/core/model/SyncModels.kt`

Migration rule:

- Treat the CCBuilder deck builder as executable source truth for the first
  desktop feature port.
- Port its model semantics and workflows into Java deliberately.
- Replace temporary Anthology TSV persistence with a JSON/SQLite-backed shape
  compatible with the legacy `SyncBundle` direction.
- Do not extend the new Java shell's deck builder from guesses where the legacy
  code already has proven behavior.

Port progress:

- Anthology desktop now persists desktop state as JSON using the same top-level
  shape as the legacy `SyncBundle`: schema version, export time, containers,
  inventory entries, decks, deck slots, and deck assignments.
- The temporary TSV reader remains as a migration fallback for earlier local
  Anthology shell state.
- Anthology desktop now has a deck import parser based on the legacy CCBuilder
  import rules: quantity-first rows, `2x` rows, Commander/section headers,
  ignored maybeboard/sideboard/token/considering sections, comments, and common
  printing metadata cleanup.
- The Deck Builder panel now exposes an `Import decklist` action that imports
  parsed rows into the selected deck.
- Smoke checks exist for JSON persistence and deck import parsing.

### CCBuilder User Data Fixture

Existing user data was found at:

- `C:\Users\tarad\AppData\Roaming\Commander Analyst\commander-analyst-data.json`

This is a legacy CCBuilder JSON `SyncBundle` and should be the primary desktop
test fixture before the broader Commander Sim deck corpus.

Observed contents:

- containers: `59`
- inventory entries: `2954`
- decks: `4`
- deck slots: `366`
- deck assignments: `26`

Known deck names observed:

- `Aminatou, the Fateshifter`
- `Yurlok the Scorch Thrash`
- `Rafiq of the Many`
- `Inspirit, Flagship Vessel`

Anthology desktop now imports this bundle on first run when no Anthology state
file exists. It can also be reloaded manually from the Deck Builder via `Load
CCBuilder data`.

Loader compatibility notes:

- CCBuilder writes enum values in Kotlin-style title case, such as `Set`,
  `Deck`, `Commander`, and `Resolved`.
- Anthology Java enums use uppercase constants.
- The desktop JSON loader accepts both forms.
- CCBuilder inventory rows use `isFoil`; Anthology's new writer uses `foil`.
  The desktop JSON loader accepts both fields.

### Important Anchors

- Assignment logic:
  - `DesktopAppState.kt`, `availableEntriesFor`
  - `DesktopAppState.kt`, `assignInventoryEntry`
- Import parsing:
  - `Main.kt`, `parseDeckImport`
  - `Main.kt`, `parseCollectionImport`
  - `Main.kt`, `parseDeckCardLine`
  - `Main.kt`, `parseCollectionCardLine`
  - `Main.kt`, `chooseCsvFile`
- Scryfall:
  - `ScryfallClient.kt`, `lookupCard`
  - `ScryfallClient.kt`, `lookupCardOptions`
  - `ScryfallClient.kt`, `ScryfallSqliteCardCache`
  - `ScryfallClient.kt`, `ScryfallBulkCardCache`
  - `ScryfallBulkDataClient.kt`, `installAll`

### Android Behavior Found

Important files:

- `Commander analyst/app/src/main/java/com/commanderanalyst/data/local/CommanderAnalystDatabase.kt`
- `Commander analyst/app/src/main/java/com/commanderanalyst/data/DeckRepository.kt`
- `Commander analyst/app/src/main/java/com/commanderanalyst/data/ContainerRepository.kt`
- Room schema files under:
  - `Commander analyst/app/schemas/com.commanderanalyst.data.local.CommanderAnalystDatabase/`

Android already has Room-backed entities/DAOs for:

- containers
- inventory entries
- decks
- deck slots

Schema version observed: `3`.

### Brand / Marketing / Visual Identity

The legacy CCBuilder work already has a deliberate Cynful Studio visual
identity. Anthology should preserve this as source material instead of inventing
a new temporary look.

Logo assets inspected:

- `Commander analyst/desktop/src/main/resources/cynful_logo.png`
- `Commander analyst/app/src/main/res/drawable-nodpi/cynful_logo.png`
- `Commander analyst/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `Commander analyst/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

Logo description:

- black field
- gold moon disc
- black armored samurai figure
- gold `CYNFUL` wordmark
- red `STUDIO` text

Palette source files:

- `Commander analyst/desktop/src/main/kotlin/com/commanderanalyst/desktop/Main.kt`
- `Commander analyst/app/src/main/java/com/commanderanalyst/ui/theme/Theme.kt`

Shared palette values found in desktop and Android Compose theme code:

- primary: `#E0A52F`
- secondary: `#D2B06D`
- tertiary: `#C12A1D`
- background: `#050505`
- surface: `#111111`
- surfaceVariant: `#1C1712`
- onPrimary: `#140D02`
- onSecondary: `#140D02`
- onTertiary: `#FFFFFF`
- onBackground: `#F4E8D0`
- onSurface: `#F4E8D0`
- onSurfaceVariant: `#D8C49A`
- outline: `#7B6340`

Android resource notes:

- `strings.xml` app name is `Commander Analyst`
- adaptive launcher icons use `@drawable/cynful_logo` as foreground
- `themes.xml` sets a no-action-bar Android theme with navigation bar
  `#111111` and status bar `#F8F4EC`
- Commander Sim also contains many card-art image files under
  `Commander-Sim/PROD/data/library/global/art/`; these are card/media cache
  assets, not a separate Anthology brand system.

Migration rule:

- Preserve the logo asset and palette as Anthology brand source unless a
  deliberate rebrand decision replaces them.
- Restyle the new Java desktop shell from these actual legacy values.
- Do not treat the current root Java shell colors as final; they were a first
  slice and must be corrected against the legacy identity.

## Commander Sim

Root folder: `Commander-Sim/`

Commander Sim is working legacy simulation software/foundation. It is Python, so
behavior and tests are the main migration assets.

### Structure Found

- `PROD/commander_sim/`: production sim modules
- `PROD/import_decks/`: many imported Commander decklists
- `PROD/data/library/`: deck libraries, model/support/spellbook files, art, and
  Card Codex data
- `PROD/data/sessions/`: session data and generated deck/session material
- `DEV/tests/`: focused behavioral tests
- `DEV/card_codex.py`: Card Codex development tooling
- `DEV/storage_cli.py`: storage tooling

### Simulation Behavior Found

Important files:

- `Commander-Sim/PROD/commander_sim/foundation.py`
- `Commander-Sim/PROD/commander_sim/engine.py`
- `Commander-Sim/PROD/commander_sim/mechanics.py`
- `Commander-Sim/PROD/commander_sim/actions.py`
- `Commander-Sim/PROD/commander_sim/agents.py`
- `Commander-Sim/PROD/commander_sim/models.py`
- `Commander-Sim/PROD/commander_sim/commander_legality.py`
- `Commander-Sim/PROD/commander_sim/deckio.py`
- `Commander-Sim/PROD/commander_sim/card_codex.py`

Useful behavior already present:

- game state foundation with players, zones, objects, stack, priority, combat,
  and state-based actions
- turn sequence and phase advancement
- priority passing and stack resolution
- land play and simple casting flow
- base combat declaration and damage flow
- state-based actions for lethal damage, zero toughness, player loss, and legend
  rule
- heuristic game runner
- action enumeration and application
- agent action choice boundary
- commander deck legality tests
- decklist parsing with printing metadata
- Card Codex grouping by `oracle_id` and stable local ID lessons

### Important Anchors

- Clean Java-port target:
  - `foundation.py`
- Current heuristic runner:
  - `engine.py`
  - `mechanics.py`
  - `actions.py`
  - `agents.py`
- Test fixture source:
  - `DEV/tests/test_phase_0_state_model.py`
  - `DEV/tests/test_phase_1_turn_structure.py`
  - `DEV/tests/test_phase_2_priority.py`
  - `DEV/tests/test_phase_3_casting.py`
  - `DEV/tests/test_phase_4_combat.py`
  - `DEV/tests/test_phase_5_state_based_actions.py`
  - `DEV/tests/test_commander_legality.py`
  - `DEV/tests/test_deck_printing_metadata.py`
  - `DEV/tests/test_card_codex.py`

## Migration Order Recommendation

Do not continue random scaffolding.

Recommended next implementation order:

1. Port CCBuilder import parsing into Java.
2. Port CCBuilder Scryfall local cache interfaces and lookup flow.
3. Replace temporary desktop TSV persistence with a real local store that can
   evolve toward the accepted sync bundle and SQLite cache direction.
4. Audit and port Commander Sim `foundation.py` into Java with matching tests.
5. Use Commander Sim DEV tests as Java fixture specifications.
6. Only then expand AI/legal-move/token work.
