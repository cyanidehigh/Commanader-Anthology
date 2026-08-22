# Release Notes
#type/release-notes #status/live

Links: [[DDS]], [[KANBAN]], [[areas/Version Control]], [[decisions/0003 Version Control Policy]]

Commander Anthology uses:

```text
V(release version).(major patch).(minor patch)
```

## In Progress V0.5.x - Rules Foundation Slice And Basic AI Gameplay
#version/V0.5 #status/in-progress #area/commander-sim #area/rules #area/ai

V0.5.0 begins the rules/AI line of work. This is explicitly a narrow rules
foundation slice, not a full Magic: The Gathering rules engine and not an
implementation of the full Comprehensive Rules document. The first slice keeps
to the accepted boundary: AI consumes generated legal moves only and every
selected move still crosses final execution validation.

V0.5.x uses minor patch increments for each landed implementation slice inside
the rules-foundation line.

Card Function Grammar and semantic tags have been documented as future design
work only. They are not part of the current V0.5.0 implementation scope.

Keyword abilities have been documented as a rules-semantics design discussion,
not an accepted implementation model. The current proposal explores cards
referencing keyword abilities while a central rules registry owns how each
keyword affects legal move generation, timing, targeting, combat, damage,
triggers, and state-based actions.

Rules tests should start moving from invented smoke cards toward real-card
fixtures sourced from the legacy Commander Sim Card Codex and real decklists.
This turns rules testing into useful card-structure migration work.

### V0.5.5 Added

- Application/build version is now `V0.5.5`.
- Added root `LICENSE` using GNU GPLv3 and root `NOTICE.md` clarifying that
  Wizards/Scryfall/card/rules/user data are third-party or user-owned materials
  outside Anthology's code license.
- Added licensing decision `0027 Commander Anthology Licensing Policy`.
- Added `ManaCost` as the printed-cost representation for fixed mana symbols.
- Added `ManaPaymentEngine` and `ManaPaymentResult` so payment legality and
  payment execution are explicit instead of hidden behind integer comparisons.
- Fixed printed costs now parse/evaluate generic, colored, and exact colorless
  symbols: `{2}`, `{W}`, `{U}`, `{B}`, `{R}`, `{G}`, and `{C}`.
- `{C}` now follows the rules boundary correctly: it requires colorless mana
  and cannot be paid with colored mana, while generic costs can still be paid
  with any mana type.
- Generic payment now prefers spare colorless mana before colored mana as a
  deterministic default until explicit player/AI payment choices exist.
- Hybrid, Phyrexian, snow, X, and unknown symbols are represented by
  `ManaCost`, but the payment engine rejects them as unsupported rather than
  silently paying them incorrectly.
- Current live spell casting still receives legacy integer generic costs from
  `GameObject`, but those are now routed through `ManaCost.generic(...)` and
  `ManaPaymentEngine`.

### V0.5.4 Added

- Application/build version is now `V0.5.4`.
- Added `ManaType` for white, blue, black, red, green, and colorless mana.
- Added `ManaPool` so player mana is represented as a real typed pool instead
  of a single integer.
- `Player.manaPoolDetails` exposes typed mana, while `Player.manaPool` remains
  as a total compatibility query for current generic-cost UI/AI/rules slices.
- `GameFoundation.addMana(playerId, ManaType, amount)` can add typed mana.
- Current generic payment now spends from the real pool and pool emptying still
  happens when advancing steps/phases.

### V0.5.3 Added

- Application/build version is now `V0.5.3`.
- Added `GameFoundation.isMainPhaseSorceryWindow` as an explicit timing query
  for the current active-player main-phase empty-stack window.
- Added `TimingAndPrioritySmokeTest` proving current upkeep, draw, main-phase,
  stack, instant, land, creature, and sorcery timing behavior.

### V0.5.2 Added

- Application/build version is now `V0.5.2`.
- Added `GameFoundation.addCommander` and a commander marker on `GameObject` so
  Commander setup no longer relies only on "card in command zone" as an
  implicit signal.
- Desktop Commander Sim setup now loads commander deck slots through the core
  commander helper.

### V0.5.1 Added

- Application/build version was `V0.5.1` for the first state-based action
  implementation slice.
- Added the first state-based action slice from rule `704.5a`: a player with
  0 or less life loses the game. In the current 1v1 beta scope, that game-over
  state clears priority and produces no further legal moves.

### V0.5.0 Added

- Application/build version is now `V0.5.0`.
- Added `ACTIVATE_MANA_ABILITY` as a structured move type.
- Added a basic land mana ability path to the rules engine:
  - only the priority player may use it
  - only untapped land permanents controlled by that player are eligible
  - activation taps the land and adds one generic mana
  - the mutation goes through final execution validation like other moves
- Legal move generation now includes generated mana ability moves for eligible
  lands.
- Added automatic active-player draw when the engine enters the draw step.
- Added core library shuffling and desktop game setup now shuffles both selected
  decks before drawing opening hands.
- Added `GameFoundation.prepareOpeningHands`, a core setup helper that shuffles
  every library and draws opening hands through the shared game model.
- Playmat priority controls now use `No response` and `Next` wording. `No
  response` means the player passes priority for the current priority window.
  `Next` means advance toward the next meaningful decision point, with automatic
  steps only skipped when the rules slice can legally and explicitly support
  that behavior.
- Added `BasicAiPlayer`, a first-pass deterministic AI scorer that ranks
  generated legal moves and prefers:
  - playing land
  - making mana when useful
  - casting supported spells
  - passing only when no useful legal action remains
- Added `BasicAiGameDriver`, a simple auto runner that advances steps when no
  priority window is open and chooses/executed AI moves only from the legal move
  package when priority is open.
- Added the first real-card rules fixture slice from the legacy Commander Sim
  Card Codex:
  - `Lightning Greaves`
  - `Swiftfoot Boots`
  - `Akrasan Squire`
  - `Battlegrace Angel`
  - `Rafiq of the Many`
- Added `RealCardFixtureLoader` and `RealCardFixtureSmokeTest` so future rules
  tests can start from real Scryfall-derived mana cost, type line, Oracle text,
  keyword list, power/toughness, and Commander legality fields.

### Explicit Current Limits

- This is not a complete Magic rules engine.
- This is not a complete Commander rules implementation.
- This is not a complete implementation of the Comprehensive Rules.
- Live `GameObject` spell costs still enter the engine as legacy integer
  generic values, even though fixed printed-cost parsing/payment now exists in
  the core mana subsystem.
- Land mana abilities are modeled as a temporary basic generic-mana ability,
  not by parsing Oracle text.
- The supported spell model remains simple creature/instant/sorcery movement
  through stack/graveyard/battlefield.
- Combat, targeting, card text, commander tax payment, triggered abilities,
  replacement effects, layers, state-based action depth, alternate costs,
  additional costs, color payment, multiplayer turn nuances, and player
  interaction, full printed-cost identity on game objects, and advanced
  payment choices are still future work.

### Verified

- Basic AI legal-gameplay slice:
  `.\gradlew.bat --no-daemon :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest :anthology-game-gdx:compileJava :anthology-desktop:compileJava --console=plain`
- Library shuffle/opening-hand fix:
  `.\gradlew.bat --no-daemon :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest :anthology-game-gdx:compileJava :anthology-desktop:compileJava --console=plain`
- Real-card fixture slice:
  `.\gradlew.bat --no-daemon :anthology-core:realCardFixtureSmokeTest :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest :anthology-game-gdx:compileJava :anthology-desktop:compileJava --console=plain`
- State-based action minimum:
  `.\gradlew.bat --no-daemon :anthology-core:stateBasedActionsSmokeTest :anthology-core:realCardFixtureSmokeTest :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest --console=plain`
- Starting-game Commander setup:
  `.\gradlew.bat --no-daemon :anthology-core:startingGameSmokeTest :anthology-core:stateBasedActionsSmokeTest :anthology-core:realCardFixtureSmokeTest :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest :anthology-game-gdx:compileJava :anthology-desktop:compileJava --console=plain`
- Timing and priority slice:
  `.\gradlew.bat --no-daemon :anthology-core:timingAndPrioritySmokeTest :anthology-core:startingGameSmokeTest :anthology-core:stateBasedActionsSmokeTest :anthology-core:realCardFixtureSmokeTest :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest --console=plain`
- Mana pool slice:
  `.\gradlew.bat --no-daemon :anthology-core:manaPoolSmokeTest :anthology-core:timingAndPrioritySmokeTest :anthology-core:startingGameSmokeTest :anthology-core:stateBasedActionsSmokeTest :anthology-core:realCardFixtureSmokeTest :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest :anthology-game-gdx:compileJava :anthology-desktop:compileJava --console=plain`
- Mana cost/payment slice:
  `.\gradlew.bat --no-daemon :anthology-core:manaCostPaymentSmokeTest :anthology-core:manaPoolSmokeTest :anthology-core:timingAndPrioritySmokeTest :anthology-core:startingGameSmokeTest :anthology-core:stateBasedActionsSmokeTest :anthology-core:realCardFixtureSmokeTest :anthology-core:gameFoundationSmokeTest :anthology-core:basicAiGameplaySmokeTest :anthology-game-gdx:compileJava :anthology-desktop:compileJava --console=plain`

## V0.4.0 - Commander Sim Playmat Foundation
#version/V0.4.0 #area/commander-sim #area/game-engine #area/game-ui #area/rules

V0.4.0 promotes the Commander Sim game window from rough board shell to the
first accepted playable-looking playmat foundation. The source-of-truth rules
document for this line is root `MagicCompRules 20260807.docx`, effective
August 7, 2026.

### Added

- Application/build version is now `V0.4.0`.
- Gameplay window now loads the selected player and opponent decks into the
  Java Commander Sim state instead of opening an empty board.
- Deck commander slots load into the command zone.
- Non-commander deck slots load into each player's library.
- Game startup draws up to seven cards from each loaded library into that
  player's hand.
- Command-zone counts now display per player instead of using the shared
  command-zone total for both seats.
- Core game foundation now has draw-card/draw-cards movement primitives.
- Gameplay window now uses a tabletop board layout instead of dashboard metric
  tiles: opponent hand shows hidden card backs, player hand shows card-shaped
  objects by name, command zones render as cards, battlefields are central, and
  stack/exile/priority sit in the shared middle strip.
- Visible game card frames now resolve real card art through the existing
  Scryfall lookup and card-image cache path. Opponent hidden hand cards remain
  card backs.
- Gameplay scene layout has been moved toward a responsive table: card sizes,
  side piles, hand trays, and scene padding scale from the active game-window
  size instead of forcing a fixed oversized board or scroll-only layout.
- Gameplay framing now follows defined play zones: command slots with visible
  tax counters, a vertical utility stack for exile/deck/graveyard, a main
  battlefield, a separate land strip, and hand trays without nested scrollbars.
- Commander Sim gameplay has been split out to a desktop-only libGDX renderer
  module. The Swing Commander Sim tab remains the setup surface; `Start game`
  now launches the libGDX playmat window from the prepared `GameFoundation`
  state instead of using the failed Swing panel board.
- The first libGDX playmat renderer now uses a fixed `1600x900` virtual canvas
  with `FitViewport` so resizing preserves scene composition. The renderer is
  organized around MTGA-style hierarchy: calm playmat, compact public piles,
  visible commanders/tax, separated lands, centered battlefield rows, and a
  prominent player hand.
- Gameplay readability pass started: card names are no longer sprayed across
  the whole board, hidden/public zone counts have stronger placement, public
  piles are moved inside the safe frame, and the player's hand uses larger
  cards with short readable labels. Detailed card and zone reading should move
  to focused click/zoom views instead of making every zone carry tiny text.
- Gameplay layout has been rebuilt around a Magarena-style invariant scaffold:
  a fixed left HUD/action rail for player identity, life totals, command zones,
  public zone counts, stack/turn controls, and a fixed right table with
  opponent hand, opponent battlefield, opponent lands, player battlefield,
  player lands, and player hand lanes. Themes may later change art and texture,
  but this structural contract should remain stable.
- Card art is back in the libGDX game renderer for visible cards. The renderer
  resolves art asynchronously through local Scryfall SQLite by card name and
  stores images in the existing `%APPDATA%\Commander Anthology\card-images`
  cache. Missing or unresolved art falls back to the placeholder card frame.
  This is a temporary bridge until game objects carry Oracle/Scryfall identity
  directly from deck loading.
- Visible card art now preserves image aspect ratio inside card frames instead
  of stretching to fill the frame. Cards with resolved art no longer render name
  text over the frame; hovering a visible card shows a larger full-card preview
  for reading.
- Opponent-side layout now mirrors the player side: opponent public zone counts
  sit above opponent command in the HUD rail, and opponent lands sit above
  opponent battlefield so the opponent battlefield is closer to the shared
  middle of play.
- Left HUD rail spacing has been normalized so player/opponent identity,
  public zones, command zones, and stack controls use consistent gutters. Command
  tax labels and turn/step text now sit on separate text lanes to avoid
  overlap.
- Game HUD typography has been retuned for readability: the base bitmap font is
  rendered at a less smeary scale while important labels, life totals, stack
  text, zone counts, and buttons receive explicit larger scales.
- Game HUD font now uses the supplied `beleren-bold_P1.01.ttf` asset through
  libGDX FreeType instead of the default libGDX bitmap font.
- Hover card previews now prefer Scryfall PNG images cached separately as
  `*-preview.png`, while table cards keep using the lighter normal cached image.
  Until the PNG finishes loading, the preview falls back to the table texture.
- Mouse wheel scrolling while hovering a visible card now zooms the hover
  preview between `0.75x` and `1.75x`. Preview zoom resets when the cursor
  leaves the card or moves to a different card.

### Verified

- Core draw/deck-state compile path:
  `.\gradlew.bat --no-daemon :anthology-core:gameFoundationSmokeTest :anthology-desktop:compileJava --console=plain`
- Gameplay board UI compile path:
  `.\gradlew.bat --no-daemon :anthology-desktop:compileJava --console=plain`
- libGDX game-window boundary:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX readability pass:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX fixed-layout scaffold:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX card-art bridge:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX hover-card preview:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX mirrored-seat layout correction:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX left-HUD spacing polish:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX HUD typography polish:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX Beleren HUD font:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX PNG hover previews:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`
- libGDX hover-preview zoom:
  `.\gradlew.bat --no-daemon :anthology-game-gdx:compileJava :anthology-game-gdx:gdxLauncherSmokeTest :anthology-desktop:compileJava --console=plain`

## V0.3.1 - Deck Builder Commander Identity Correction
#version/V0.3.1 #area/ccbuilder #area/deck-builder #area/card-identity

V0.3.1 is the first minor correction after the Commander Sim spine started. It
locks in the deck-builder identity model needed before deeper game work: deck
intent is Oracle-first, commander comes from deck metadata, and physical
printing matters only when assigning/filling from collection.

### Changed

- Application/build version is now `V0.3.1`.
- Deck Builder import/build flow is now Oracle-first and less noisy:
  - manual add/edit no longer asks for deck section
  - deck tables no longer show section as a primary column
  - import review no longer asks for printing choices
  - imported/manual deck rows no longer store a preferred printing unless the
    user explicitly chooses one
  - commander import rows/sections are skipped because commander identity lives
    on the deck metadata chosen when the deck is created/edited
  - import review now reports identity `Matched`/`Unmatched`; rows are no
    longer mislabeled manual just because a printing was not selected
  - import auto-resolution may use any available print to identify the card, but
    stores only deck intent identity unless a printing is explicitly selected
- Deck creation/editing now validates the Commander field against card lookup
  data and rejects cards that are not legal commander candidates.
- Creating or editing a deck now auto-creates/updates the deck's singleton
  Commander slot from the authoritative Commander field.
- Deck list rows now include a `Set commander` action as a fallback. It
  validates the selected card as commander-legal, updates deck metadata, and
  promotes/removes the source row so the deck still has one commander slot.
- Commander validation now includes the Edge of Eternities rules update:
  legendary Vehicles and legendary Spacecraft with printed power/toughness can
  be chosen as commanders.
- Deck card details now fall back from preferred printing ID to Oracle identity,
  so Oracle-resolved deck intent rows can be viewed even before a physical
  printing is assigned.

### Verified

- Commander/deck identity correction verified with:
  `.\gradlew.bat --no-daemon :anthology-desktop:commanderValidationSmokeTest :anthology-desktop:deckImportParserSmokeTest :anthology-desktop:deckImportReviewedIdentitySmokeTest :anthology-desktop:manualCardSelectionSmokeTest :anthology-desktop:deckAssignmentCopyChoiceSmokeTest :anthology-desktop:compileJava --console=plain`
- EOE commander legality clause verified with:
  `.\gradlew.bat --no-daemon :anthology-desktop:commanderLegalityRulesSmokeTest :anthology-desktop:commanderValidationSmokeTest :anthology-desktop:compileJava --console=plain`

## V0.3.0 - Commander Sim Java Spine
#version/V0.3.0 #area/commander-sim #area/sim-integration #area/game-engine

V0.3.0 starts the Commander Sim fold-in as a Java game spine, not as full AI.
The first slice ports the proven Python foundation/test knowledge into
`anthology-core` so desktop and future Android surfaces can consume the same
rules-legal state model.

### Added

- Application/build version is now `V0.3.0`.
- Added `com.commanderanthology.core.commandersim` as the first shared
  Commander Sim game spine.
- Added Java game-state primitives ported from the Commander Sim foundation:
  players, player order, zones, shared zones, game objects, turn steps, priority
  results, stack resolution metadata, and game events.
- Added turn sequence support for untap, upkeep, draw, precombat main, combat,
  postcombat main, and end.
- Added priority-window support where only the priority player may act/pass, all
  players passing closes an empty-stack window, and all players passing resolves
  the stack top.
- Added legal move generation for the first supported moves:
  - `PLAY_LAND`
  - `CAST_SPELL`
  - `PASS_PRIORITY`
- Added final execution validation: requested moves are matched against the
  currently generated legal move set before the game state mutates.
- Added native `GameEvent` telemetry records for executed Anthology moves.
- Added `MoveType.PLAY_LAND` to the shared structured move contract.
- Replaced the visible Commander Sim tab placeholder with an always-visible game
  setup surface:
  - mode select
  - player deck select
  - opponent deck select with `Random` at the top
  - `Start game` opening a dedicated gameplay window
- The dedicated gameplay window now opens at a real desktop size and shows a
  first-pass board surface with player/opponent zones, life, mana, active-player
  status, turn/step status, and disabled placeholder action controls.

### Current Limits

- This is a spine milestone, not the full Commander engine.
- Combat, state-based actions, commander tax, card text/effects, triggered
  abilities, replacement effects, and full mana/color payment are still future
  migration slices.
- AI remains outside the engine boundary and can only consume legal moves once
  connected.

### Verified

- Commander game foundation smoke test plus desktop compile:
  `.\gradlew.bat --no-daemon :anthology-core:gameFoundationSmokeTest :anthology-desktop:compileJava --console=plain`

## V0.2.2 - Desktop UI Simplification And Migration Hardening
#version/V0.2.2 #area/platform #area/ccbuilder #area/ui #area/migration

### Changed

- Application/build version is now `V0.2.2`.
- Deck slot rows now use migrated CCBuilder status labels instead of broken
  symbol glyphs:
  - Assigned
  - Partial
  - Available
  - Missing
- Deck import review, collection import review, deck slots, and collection rows
  now use shared badge rendering for resolved/manual/available/missing/foil
  states.
- Collection rows now separate resolved/manual status from Oracle identity,
  making physical card state easier to scan.
- Collection add, edit, and import now merge duplicate physical-card rows the
  same way legacy CCBuilder did instead of creating repeated non-modifier rows.
- Collection moves now reject hidden deck containers and assigned deck copies,
  preserving the legacy physical-copy boundary.
- Normal Deck Builder and Collection sidebars no longer expose the one-time
  `Load CCBuilder data` migration action now that CCBuilder is being shelved
  after migration.
- Dashboard is hidden from normal desktop navigation because it was only a
  placeholder status page; the app now opens directly to Deck Builder.
- Deck Builder now keeps only primary actions visible and moves deck/card
  maintenance actions behind `More...` dialogs.
- Card Codex is hidden from normal desktop navigation because it is
  infrastructure/maintenance rather than a normal user workflow. Its lookup and
  cache services remain available to Deck Builder and Collection.
- Visible `Sync` navigation is renamed to `Backup` because the current live
  function is local export/import of user-owned desktop state, not cloud or
  mobile sync.
- `Launch Commander Anthology.bat` now delegates to
  `Launch Commander Anthology.ps1` to discover a usable local Java runtime
  before calling Gradle.
- The launcher accepts Java 17 or newer and searches `JAVA_HOME`, common Windows
  JDK install folders, and PATH.
- The launcher reports the Java executable/version it selected and gives a
  clearer message if no suitable Java install is found.
- Gradle compilation now uses Java 17 source/target compatibility with
  `--release 17` instead of requiring Gradle to locate a separate Java 17
  toolchain installation.
- Desktop runtime now includes a no-op SLF4J binding matching SQLite JDBC's
  SLF4J API dependency, removing startup logger-binding warnings.

### Verified

- Gradle wrapper startup verified with Java 21.0.11 using:
  `.\Launch Commander Anthology.ps1 -GradleArgs '--version'`
- Desktop Java compile verified with Java 21.0.11 using:
  `.\gradlew.bat :anthology-desktop:compileJava --console=plain --no-daemon`
- Runtime classpath verified to include `org.slf4j:slf4j-nop:1.7.36`.
- Desktop status/badge migration verified with:
  `.\gradlew.bat :anthology-desktop:compileJava :anthology-desktop:compileTestJava :anthology-desktop:deckAssignmentCopyChoiceSmokeTest :anthology-desktop:manualCardSelectionSmokeTest --console=plain --no-daemon`
- CCBuilder collection merge behaviour verified with:
  `.\gradlew.bat :anthology-desktop:compileJava :anthology-desktop:compileTestJava :anthology-desktop:collectionMutationSmokeTest --console=plain --no-daemon`

## V0.2.1 - Dashboard List Layout
#version/V0.2.1 #area/platform #area/brand-identity

V0.2.1 is the dashboard/sidebar layout correction pass.

### Added

- Sidebar version display now uses the documented `V0.2.1` format.

### Changed

- Sidebar brand block was rebuilt so the title, subtitle, and Cynful logo sit at
  the top as a coherent header.
- Cynful logo was enlarged and centered in the sidebar.
- Dashboard/workspace navigation was pulled back up under the logo.
- Navigation buttons were given a fixed inner sidebar width so their text can be
  genuinely centered.
- Selected navigation still keeps the gold left marker, but the marker no
  longer shifts the label off-center.
- Card-detail popup no longer shows raw technical fields that are not useful in
  normal inspection:
  - image URL
  - Scryfall card ID
  - Oracle ID

### Verified

- Desktop Java compile passes.

## V0.2.0 - Re-bedding Legacy CCBuilder
#version/V0.2.0 #area/ccbuilder #area/migration #area/card-codex #area/deck-catalog #area/collection

V0.2.0 is the first serious CCBuilder migration pass: the desktop Anthology app
started moving from a shell/skeleton into a usable deck builder and collection
manager backed by the existing legacy data model and Scryfall cache.

### Added

- Restored existing CCBuilder user data as the desktop test/live fixture.
- Added mutable Deck Builder and Collection workspaces using the CCBuilder
  two-pane model.
- Added local desktop persistence for decks, deck slots, deck assignments,
  containers, and collection inventory rows.
- Added CCBuilder user-data import from:
  `%APPDATA%\Commander Analyst\commander-analyst-data.json`
- Added local-first Scryfall card validation using the existing CCBuilder cache.
- Added fallback bundled lookup seed for first-run validation when the full
  Scryfall cache is missing.
- Added local Scryfall API fallback only after local SQLite and seed lookup miss.
- Added collection container create/edit/delete.
- Added collection inventory add/edit/move/delete.
- Added collection import review:
  - pasted quantity-first rows
  - CSV import
  - `Card Name`, `Quantity`, `Set Code`, `Collector Number`, `Finish`, and
    `Scryfall ID` fields
  - selected-printing review for ambiguous rows
  - exact Scryfall-ID import when available
- Added deck import review:
  - section-aware pasted decklist parsing
  - local card validation before import
  - ambiguous printing chooser
  - preservation of selected Scryfall identity on deck slots
- Added manual Add/Edit printing lookup for both deck rows and collection rows.
- Added physical-copy assignment chooser for deck slots.
- Added unassignment flow that returns cards to their source container where
  possible.
- Added local Card Codex workspace:
  - local Scryfall SQLite search
  - card detail display from `details_json`
  - cache status table
  - bulk-data check/install/adopt controls
  - SQLite rebuild from `default_cards.json`
- Added card-detail dialogs from deck and collection rows.
- Added card art loading and local image cache under:
  `%APPDATA%\Commander Anthology\card-images`
- Added local sync bundle export/import for user-owned desktop state.
- Added first legacy CCBuilder/Cynful visual theme pass:
  - shared `AnthologyTheme`
  - warm black/gold/parchment palette
  - themed tables
  - themed buttons
  - themed scroll panes
  - themed import review dialogs
  - themed card-detail dialogs
- Added the legacy `cynful_logo.png` asset to Anthology desktop resources.

### Changed

- Deck and collection screens moved away from static placeholder lists and
  toward the original CCBuilder editing workflow.
- Card identity moved away from dirty text-only matching and toward stored
  Oracle/Scryfall identity where available.
- Deck assignment stopped silently taking the first matching card and now asks
  which physical copy to use.
- Moving collection rows now merges matching physical-card rows in the target
  container.
- Sync bundle export/import now uses Anthology desktop state rather than a
  placeholder flow.

### Data Restored

Live Anthology desktop state was restored from CCBuilder data on 2026-07-29.

- Containers: `59`
- Inventory entries: `2954`
- Decks: `4`
- Deck slots: `366`
- Deck assignments: `26`
- Unresolved card input rows after validation: `0`

### Verified

- Desktop Java compile passes.
- Focused smoke tests were added and run for:
  - persistence
  - deck import parsing
  - legacy CCBuilder data import
  - collection mutation
  - local card lookup validation
  - collection import parsing
  - Card Codex search
  - deck assignment physical-copy choice
  - sync bundle export/import
  - Scryfall cache status
  - Scryfall bulk-data service
  - card image cache
  - Scryfall SQLite build
  - reviewed deck import identity
  - manual card selection identity

## V0.1.0 - Skeleton For Commander Anthology
#version/V0.1.0 #area/platform #area/shared-core #area/design

V0.1.0 is the initial unified Commander Anthology skeleton.

### Added

- Created the unified Java project structure:
  - `anthology-core`
  - `anthology-desktop`
  - `anthology-android`
- Added the root `README`.
- Added the root `HANDOFF`.
- Added desktop launcher batch file:
  `Launch Commander Anthology.bat`
- Added initial desktop shell with workspace routing.
- Added initial shared-core model boundaries for:
  - cards
  - decks
  - collection containers
  - inventory entries
  - deck assignments
  - structured move tokens
  - validation gates
  - AI move score placeholders
  - actor visibility projection types
- Created the Obsidian-ready `design/` system.
- Created the DDS hub.
- Created the kanban document.
- Created initial decision records for Anthology unification, Java platform, and
  version-control policy.

### Changed

- Commander Anthology became the target single project rather than treating
  CCBuilder and Commander Sim as isolated tools.
- Desktop was established as the full-spectrum application.
- Android was established as the companion/light CCBuilder-style app.
- Legacy apps were kept in-tree as source material until deliberate migration or
  retirement.

### Known Gaps

- V0.1.0 did not yet contain CCBuilder parity.
- Commander Sim/AI integration remained ahead.
- Android app remained a skeleton.
- Git repository state still required repair/confirmation before normal Git
  workflows could be trusted.
