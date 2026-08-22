# Commander Anthology Handoff

## Current Direction

Commander Analyst / CCBuilder and Commander Sim are now one combined product
direction: **Commander Anthology**.

Detailed design tracking now lives in `design/`, starting from
`design/DDS.md` and `design/KANBAN.md`. The design folder is intended for
default Obsidian using plain Markdown links and tags.

Rules implementation tracking now has its own hub under `design/rules/`. Use
`design/rules/CAPABILITY_MATRIX.md` before adding or expanding rules behavior.
Rules work must stay explicit about source document, rule references,
implemented slice, unsupported cases, code, and tests.

Design decision `design/decisions/0026 Rules Semantics And Function Tags
Split.md` separates exact rules semantics from strategic function tags. The AI
and rules engine need exact card behavior for legal move generation,
validation, and execution. The tagging layer, tracked in
`design/areas/Card Function Grammar.md`, helps the Builder, player-style
analysis, and future LLM-facing explanations talk in familiar Magic terms such
as `Tutor > Creature`, `Mana > Sink`, or `Graveyard > Recursion`. Tags may help
score or explain already-legal moves, but they are not legal authority and must
not be fed human player-style data into the AI opponent.

Card Function Grammar is parked for later implementation. A Mythic Tools deck
page/source review suggests their visible function chips are backed by a
bespoke analysis API, so Anthology should build its own offline-first version
instead of depending on that service. New set support should eventually use
auto-seeded tags from Oracle text/type-line patterns plus a Dev Mode review
queue. Manual tags are the correction path, not the intended default workload.
Do not let this distract the current V0.5.0 rules-engine slice.

For rules testing, prefer real-card fixtures over invented smoke cards wherever
practical. Existing legacy data under `Commander-Sim/PROD/data/library/global/
card_codex/` already contains Scryfall-derived card records with `oracle_id`,
mana cost, type line, Oracle text, keywords, power/toughness, and Commander
legality. Existing decks under `Commander-Sim/PROD/data/library/decks/` and
`Commander-Sim/PROD/import_decks/` provide real Commander samples. This should
let tests migrate useful card structure while proving engine behavior. The
first executable real-card fixture slice lives at
`anthology-core/src/test/resources/real-card-fixtures/keyword-cards.psv`, loads
through `RealCardFixtureLoader`, and is verified by
`:anthology-core:realCardFixtureSmokeTest`. It currently covers
`Lightning Greaves`, `Swiftfoot Boots`, `Akrasan Squire`, `Battlegrace Angel`,
and `Rafiq of the Many`.

The root has started transitioning into the unified software project:

- `anthology-core/`: shared Java core and platform-neutral model boundaries.
- `anthology-desktop/`: Java desktop shell for the full-spectrum app, with
  normal navigation for Deck Builder, Collection, Commander Sim, Player Style,
  and Backup. Dashboard and Card Codex remain available in code as archived/
  infrastructure context but are hidden from normal navigation.
- `anthology-game-gdx/`: desktop-only libGDX gameplay renderer for the
  Commander Sim game window. This is intentionally scoped to the full desktop
  app; mobile does not get the full Commander Sim play mode.
- `anthology-android/`: reserved home for the Android companion app.

Current version: **V0.5.5 in progress**.

Newest rules source in root: `MagicCompRules 20260807.docx` effective August 7,
2026. The older root `MagicCompRules 20260227.docx` remains historical
reference only.

Desktop implementation has started moving beyond shell-only:

- shared Java deck, deck slot, deck assignment, container, and inventory models
  exist in `anthology-core/`
- V0.3.0 started the Commander Sim Java spine in
  `anthology-core/src/main/java/com/commanderanthology/core/commandersim`,
  covering players, zones, objects, turn steps, priority windows, first legal
  move generation, final execution validation, stack resolution for simple
  spells, and native game-event telemetry
- V0.4.0 accepts the first usable Commander Sim playmat foundation. The visible
  desktop Commander Sim tab is now the setup surface itself: mode
  select, player deck select, opponent deck select with `Random`, and a `Start
  game` action. `Start game` now launches the desktop-only libGDX playmat window
  from the prepared Java `GameFoundation` state instead of using a Swing panel
  board. Swing remains for setup/app workflows; libGDX owns the actual gameplay
  scene. The libGDX renderer uses a fixed `1600x900` virtual canvas with
  `FitViewport`; this should preserve composition across window sizes. The
  visual hierarchy should follow MTGA-like principles without cloning MTGA:
  calm playmat, compact public piles, visible commander/tax, separated lands,
  centered battlefield rows, prominent player hand, and focused inspection
  later for graveyard/exile/card zoom details. The first readability correction
  keeps the fixed 16:9 canvas but increases primary text/card scale, moves
  public piles inside safe frame bounds, and reduces full card-name rendering on
  the battlefield so the game window reads as a play surface instead of a
  dashboard. The next layout pass adopts a Magarena-style invariant scaffold:
  fixed left HUD/action rail for readable life totals, command zones, public
  zone counts, stack/turn controls, and fixed right-side table lanes for
  opponent hand, opponent battlefield, opponent lands, player battlefield,
  player lands, and player hand. Future theme work should skin this structure
  rather than changing the structure per theme. Visible card art is currently
  restored through a libGDX-side bridge that resolves card names against local
  Scryfall SQLite and reuses `%APPDATA%\Commander Anthology\card-images`.
  Treat that as a temporary bridge: the better long-term fix is carrying
  Oracle/Scryfall identity from deck loading into `GameObject`. Visible card
  frames preserve art aspect ratio and suppress card-name overlays when art is
  present. Hovering a visible card opens a larger full-card preview for reading.
  Opponent-side HUD/table ordering mirrors the player side: opponent zones sit
  above command in the rail, and opponent lands sit above opponent battlefield
  so battlefield areas face toward the middle of the table. Left HUD rail
  spacing is normalized with consistent gutters; command tax labels and
  turn/step text have separate lanes to prevent overlaps. The current libGDX
  HUD uses the supplied `beleren-bold_P1.01.ttf` asset through libGDX FreeType
  instead of the default bitmap font. Hover card previews prefer a separate
  Scryfall PNG cache entry (`*-preview.png`) and fall back to the normal table
  texture while that PNG loads. Mouse wheel scrolling during card hover zooms
  the preview between `0.75x` and `1.75x`; zoom resets when hover ends or moves
  to a different card.
- V0.5.0 has started the rules-foundation/basic-AI gameplay line. This is
  explicitly a narrow foundation slice, not a complete Magic rules engine, not
  a complete Commander implementation, and not an implementation of the full
  Comprehensive Rules document. Starting-game setup now has a core
  `GameFoundation.addCommander` helper and commander marker on `GameObject`, so
  commander status is explicit instead of inferred only from zone placement.
  Timing now exposes `GameFoundation.isMainPhaseSorceryWindow` and is verified
  by `TimingAndPrioritySmokeTest`: upkeep/draw are not sorcery windows,
  active-player main phase with empty stack is, non-empty stack closes it, and
  instants may be legal for the priority player outside that window.
  Costs/mana now have a real `ManaPool` and `ManaType` model for white, blue,
  black, red, green, and colorless mana. `Player.manaPoolDetails` exposes typed
  mana, while `Player.manaPool` remains a total compatibility query for current
  generic-cost UI/AI/rules slices. V0.5.5 adds `ManaCost`,
  `ManaPaymentEngine`, and `ManaPaymentResult`: fixed printed costs can now be
  parsed and evaluated for generic, colored, and exact colorless symbols.
  `{C}` requires colorless mana; generic costs can be paid with any mana type.
  Hybrid, Phyrexian, snow, X, and unknown symbols are represented but rejected
  as unsupported payment choices until their own rules slices exist. Current
  live casting still receives legacy integer generic costs from `GameObject`,
  but those values now route through the mana payment engine.
  The first rules slice also adds a legal `ACTIVATE_MANA_ABILITY` move for
  untapped lands, automatic draw on entering draw step, `BasicAiPlayer`
  legal-move scoring, and `BasicAiGameDriver` automated stepping. The AI still
  consumes only generated legal moves and
  selected moves still go through final execution validation. Current limits are
  deliberate: mana is generic, land mana is a temporary basic ability rather
  than Oracle-text parsing, spell support is simple movement only, and combat,
  targeting, card text, commander tax payment, triggered abilities, replacement
  effects, layers, state-based action depth, advanced payment choices, full
  printed-cost identity on game objects, and player interaction remain future
  work. Libraries now have a core shuffle operation,
  and desktop game setup shuffles both selected decks before drawing opening
  hands so repeated games do not draw the same deterministic card sequence.
  Playmat controls use `No response` for passing priority in the current
  priority window and `Next` for advancing toward the next meaningful decision
  point. Do not make `Next` skip decisions unless the rules slice explicitly and
  legally supports that automation. The first real-card rules fixture slice is
  now in place so keyword/card-rules tests can start from real Scryfall-derived
  card data instead of invented smoke cards. State-based actions have started
  with the smallest useful `704.5a` slice: a player at 0 or less life loses,
  current 1v1 game-over clears priority, and no further legal moves are
  produced after game over. This does not implement the rest of `704`.
- Gameplay startup now loads selected player/opponent deck slots into actual
  game zones: commander slots to command, other slots to library, then up to
  seven cards are drawn into each opening hand. This is the first step from
  board shell to real game state.
- V0.3.1 locks in the Deck Builder identity correction. Deck intent rows are now
  Oracle-first in normal use. Manual add/edit
  no longer asks the user to choose a section, import review no longer asks for
  printing choices, and preferred printing fields are only stored when the user
  explicitly chooses a printing. Commander is deck metadata from deck
  creation/editing; import should not create or mark Commander-section deck
  rows. Import review should show card identity `Matched`/`Unmatched`, not
  printing-selection state. Import may use any valid print to find the card, but
  should store Oracle/card identity only until the user fills the row with an
  available collection card or explicitly chooses a printing.
- Deck creation/editing treats the Commander field as authoritative metadata.
  It must resolve through card lookup and must be legal as a commander. A real
  card that cannot be a commander, such as Sol Ring, is rejected. That metadata
  also auto-creates/updates the deck's singleton Commander slot so the commander
  appears in the deck surface without importing a Commander section.
- Deck list rows also have a `Set commander` fallback action. It uses the same
  commander legality validation, updates deck metadata, creates/updates the
  singleton Commander slot, and removes/decrements the selected source row.
- Commander legality includes the Edge of Eternities rules update: a legendary
  Vehicle or legendary Spacecraft with printed power/toughness can be the
  commander even if it is not a creature in the command zone. Nonlegendary
  Vehicles/Spacecraft and cards without printed power/toughness remain rejected
  unless another card-specific rule allows them.
- Deck intent rows may be Oracle-resolved without a preferred printing. Details
  views must fall back to Oracle identity/name lookup in that case. A `Missing`
  deck row means no available/assigned collection copy, not unidentified card
  identity.
- `anthology-desktop/` has real Deck Builder, Collection, Card Codex
  infrastructure, and local Backup/restore
  workspaces
- the panels restore existing CCBuilder user data when available, then persist
  desktop state locally
- persistence writes to
  `%APPDATA%\Commander Anthology\anthology-desktop-state.json`
- the older temporary TSV state remains a migration fallback only
- deck and collection import now use CCBuilder-derived review flows
- card input resolves through local Scryfall data where available
- deck assignment asks which available physical copy to assign instead of
  silently taking the first matching inventory row
- local Card Codex search/detail, cache status, bulk-data controls, card-detail
  dialogs, card-art cache, and local backup/export/import are present

An imported AI architecture design journal is tracked under
`design/sources/Commander Anthology AI Architecture Design Journal.md`. Its main
new design pressure is Knowledge Projection: the engine owns authoritative
truth, and AI/UI/replay/analytics/coaching receive permission-appropriate views
rather than raw omniscient game state. The projection must mirror actual Magic
information rules: public information is public, a player/AI may know what is in
their own deck, but hidden library order and opponent hidden information remain
unknown unless revealed by game mechanics.

Forge reference note: `design/sources/Forge Reference.md` tracks
Card-Forge/forge as an architectural source for Java module split, game state,
zones, stack, spell abilities, AI heuristics, cost/target handling, card
scripting, and validation ideas. Forge is GPL-3.0, so treat it as reference
material unless Anthology deliberately chooses compatible licensing.

Accepted Anthology licensing policy: Commander Anthology code is licensed under
GNU GPLv3. The root `LICENSE` file carries the canonical GPLv3 text and
`NOTICE.md` records the third-party IP boundary. GPLv3 covers Anthology's
original code/project files, not Wizards/Scryfall/card/rules data or user-owned
deck and collection data.

Accepted Forge policy: Forge is a comparative reference only. Anthology must not
copy Forge code, clone Forge's product flow, depend on Forge libraries, or
inherit Forge architecture wholesale. Anthology exists to solve a different
problem: collection-aware Commander building, player-style learning, and
offline-first desktop/mobile Commander tooling.

The local rules reference for this mapping is
`Commander-Sim/DEV/MagicCompRules 20260227.docx`; rule `400.2` is the starting
anchor for public zones versus hidden zones.

Accepted information visibility rule: projection is based on actor seat, not
actor type. A human player and AI player in the same seat receive the same legal
information projection. The AI does not get special hidden information, and the
human UI must not reveal hidden authoritative state.

Legacy folder direction: `Commander analyst` and `Commander-Sim` are temporary
staging/reference folders. Anthology should eventually become one unified
project. Empty the legacy folders only after useful code, tests, lessons, and
user data have been ported, preserved, replaced, or deliberately retired.

Accepted migration bias: use as much proven legacy behavior as practical.
CCBuilder is the primary source for Builder behavior and should be ported
aggressively. Commander Sim is the primary source for simulation behavior and
test knowledge; because it is Python, port its behavior, fixtures, flow, and
lessons deliberately rather than line-for-line.

Commander Sim migration has begun with the foundation layer, using
`Commander-Sim/PROD/commander_sim/foundation.py` and the DEV phase tests as the
source behavior. The first Java smoke test mirrors land play, one-land-per-turn,
simple creature casting, priority passing, stack resolution, and game-event
logging. Continue porting in thin verified slices rather than attempting the
whole rules engine at once.

Legacy study note: `design/sources/Legacy Software Study.md` now records the
specific CCBuilder and Commander Sim files/behaviors inspected for migration.
Use that note before choosing the next implementation slice.

Deep CCBuilder study note: `design/sources/CCBuilder Deep Dive.md` records the
actual CCBuilder project structure, shared-core model, desktop app behavior,
Android app behavior, persistence, Scryfall/cache paths, and migration gaps.
Treat this as the gate for CCBuilder work: matching the data shape alone is not
enough. A migrated CCBuilder subsystem must preserve model, mutation behavior,
persistence, local/offline data flow, workflow, and visual/status language.

Current migration lead: the CCBuilder deck builder is the most complete legacy
surface. It already has deck intent slots, printing-aware inventory, Scryfall
identity fields, assignment from collection into deck slots, deck import, and
JSON `SyncBundle` persistence. The next desktop implementation slice should
port that behavior directly instead of growing the temporary Java shell from
guesswork.

Deck builder migration slice completed: Anthology desktop now writes
`anthology-desktop-state.json` under `%APPDATA%\Commander Anthology`, using a
JSON bundle shape aligned with legacy `SyncBundle`. It still reads the earlier
temporary TSV state as a fallback. The Java Deck Builder also has an `Import
decklist` action and a CCBuilder-derived parser for Commander sections,
quantity-first rows, `2x` rows, ignored side/maybe/token sections, and common
printing metadata cleanup.

Existing CCBuilder user data is now wired in for testing. Anthology desktop
looks for
`C:\Users\tarad\AppData\Roaming\Commander Analyst\commander-analyst-data.json`
on first run when no Anthology state exists, imports it, then saves an Anthology
copy under `%APPDATA%\Commander Anthology`. The Deck Builder also has `Load
CCBuilder data` removed from normal UI now that CCBuilder will be shelved after
migration. The importer remains available to migration/test code. The observed
CCBuilder fixture contains 59 containers, 2954 inventory entries, 4 decks, 366
deck slots, and 26 deck assignments.

Brand note: legacy CCBuilder already has a Cynful Studio logo and a matching
desktop/Android dark palette. The logo lives in both the desktop resources and
Android drawable resources as `cynful_logo.png`. The palette uses gold, red,
black, and warm parchment text values from the legacy Compose themes. Preserve
that identity unless a deliberate rebrand decision replaces it; the current
root Java desktop shell should be restyled against this source before it is
treated as visually representative.

They are not separate long-term products. They are two surfaces over one shared
Commander platform:

- **Desktop:** full-spectrum Commander Anthology, including everything
  Anthology has to offer.
- **App/mobile:** companion app only: game tracker/life counter, limited
  win/loss telemetry when two or more players are using it together, and light
  deck building.

Telemetry privacy rule: mobile tracker/win-loss telemetry should only be shared
between users inside the same active session. It is not background/global
analytics by default.

The shared idea is simple:

- CCBuilder uses real card, deck, legality, collection, and Scryfall data to
  teach players how to build real Commander decks.
- Commander Sim plays games, records how the human player actually plays, and
  produces player-style data.
- CCBuilder can later use that player-style data to recommend deck builds,
  cuts, substitutions, and collection-aware paths that fit the player.

## Product Rule

Deterministic truth stays deterministic.

Player style, AI, and recommendation layers may explain or personalize, but
they must not decide:

- rules legality
- Commander legality
- card identity
- oracle text
- color identity
- ownership
- physical inventory movement
- prices
- card existence

When behavior is not implemented, the app must say so clearly instead of
silently approximating.

## User Preferences And Process Rules

- Design first, then build.
- Do not jump from a vague product request straight into implementation.
- Before major implementation work, clarify or document the design decision
  being acted on.
- The active direction is desktop first as the full-spectrum program, with
  app/mobile later as a light companion.
- Do not pivot to web unless explicitly requested.
- Keep implementation scoped and truthful. Do not call scaffolding complete.

## Platform Direction

Preferred shape going forward:

- Java shared core for platform-neutral models and app business rules.
- Java desktop application for the complete Commander Anthology experience.
- Java Android companion app for game tracking/life counter, limited telemetry,
  and light deck building.
- Local-first persistence.
- Offline-first data access: local Card Codex/cache first, Scryfall API fallback
  only when local data is missing, stale, or cannot answer confidently.
- Optional serverless cross-platform sync. Desktop and Android must each work
  independently, and linking should use portable/user-owned sync data before
  any maintained cloud/server idea.
- A maintained server is not banned forever, but it is not an initial/core
  dependency. If needed later, it requires a deliberate cost/privacy/
  sustainability decision.
- User-owned storage is preferred for later sync.

The existing Kotlin/Compose Android and desktop CCBuilder work is a working
legacy app/skeleton and remains useful migration groundwork, but it must not
become the source of truth for the new shared Java business rules.

## Shared Platform Truth

The shared core/platform should own:

- Scryfall-backed card identity and Card Codex data
- offline-first data regression/cache policy
- oracle identity separate from printing identity
- printing-aware physical collection inventory
- Commander legality and deck validation
- decklist parsing and import
- deck catalog metadata that distinguishes user decks from locked bundled
  precons
- deck context, roles, plans, synergies, and suggestions
- collection/deck assignment rules
- player style/profile schema learned from Commander Sim play sessions
- deterministic suggestion candidates
- portable sync bundle boundaries for optional desktop/Android linking

CCBuilder and Commander Sim should consume this shared truth instead of
duplicating their own incompatible versions.

Shared Java Core must be plain Java and must not depend on desktop-only,
Android-only, Python, UI, Google Drive, SQLite implementation, or live Scryfall
HTTP implementation code. Desktop and Android each provide their own storage,
network, sync, and UI implementations and can run separately.

## Card Identity Model

Collection rows are printing-specific. Deck intent rows are oracle-specific by
default.

Important identity rules:

- Oracle/rules identity is hidden truth for legality, matching, rules, and
  learned style data.
- Printing identity is visible and important for physical collection rows.
- Reprints and Universes Beyond/reskin printings should not duplicate rules
  identity.
- Example: `Assaultron Invader` from `PIP` can satisfy a `Walking Ballista`
  deck slot because the hidden oracle identity matches.
- Assigning an owned card to a deck moves a physical copy into that deck. It
  does not duplicate inventory.
- Foil and nonfoil physical rows must not merge.

Commander Sim currently has a Scryfall-backed Card Codex that maps Scryfall
`oracle_id` values to stable six-hex local card IDs. CCBuilder currently has
Scryfall lookup/cache work in the working legacy desktop app. These should
converge into one shared Java Anthology card-data layer.

Accepted future direction: the shared Java Card Codex uses Scryfall `oracle_id`
as the primary normal rules/card identity, with SQLite-backed local storage and
indexes. The old six-hex Sim IDs are migration/reference material only. Anthology
may keep typed fallback IDs for unresolved/manual/custom records, but fallback
IDs must not masquerade as `oracle_id`.

## CCBuilder Surface

CCBuilder is the deck-building, collection, search, import, legality, and
player-guidance surface.

It should help:

- newer Commander players understand what their deck is doing
- experienced players import, analyze, search, filter, and tune quickly
- users build from their real collection
- users see owned, available, missing, assigned, illegal, and suggestion states

CCBuilder should diagnose from deck/card/collection evidence instead of asking
up-front playstyle questionnaires.

AI/model support in CCBuilder is later and should be a coach/explainer layer
only. It can summarize, teach, phrase suggestions, and explain how a
recommendation matches or differs from the player's learned style.

## Commander Sim Surface

Commander Sim is the desktop simulation surface.

The first AI target has been deliberately simplified:

- Build a legal move package that can play Magic effectively.
- Generate and choose from legal moves.
- Play complete games.
- Record useful player-originated behavior data.

Accepted legal move boundary: legal moves are a general engine/simulation
boundary, not an AI boundary. Rules-legal moves are universal. The AI should be
able to do exactly what a human player or any other player could legally do from
the same game state and knowledge position. The AI chooses from engine-produced
structured legal moves and cannot invent or bypass legality.

Accepted AI decision contract: AI receives projected game state and structured
legal moves, may apply a point/weight system to rank those legal moves, and
returns a selected legal move ID. Scores are strategic preference only; they do
not create legality, mutate state, use player style data, or bypass final engine
validation. The scoring model should remain traceable from legal move ID to
score categories, reason codes, tie-break, and selected move.

Accepted structured token grammar boundary: tokens are the shared structured
language for legal moves, chosen options, targets, costs, timing windows,
scoring explanations, logs, and validation. Explicit means every
player-controllable choice required to validate and execute a legal move is a
structured field. It does not mean restating Oracle text or duplicating the
rules engine.

Accepted validation strategy: every move crosses at least two gates: legal move
generation, then final execution validation. Normal player UI and AI choices
should be legal by construction because they are selected from legal moves, but
the final gate protects against stale state, desync, corrupted tokens, replay or
import data, dev tools, and future AI mistakes. If Anthology cannot prove a move
is valid, it must reject it, mark it unsupported, or require a rule module.

Desktop Sim modes are now only:

- **Play:** human plays against the AI. Human-selected moves are recorded as
  player style data. AI-selected moves do not train the style profile.
- **Auto:** agents play/evaluate without changing player style data.

The old supervised good/bad training loop is not the first target.

Player style data comes from what the human actually does in Play mode. That
data may later help CCBuilder recommend decks and changes that fit the player,
but it must never override legality or rules truth.

Accepted player-style boundary: player style is built from the user's actual
gameplay choices and is consumed by CCBuilder. It must not be offered to the Sim
AI as strategic input, must not train the AI opponent, and must not make the AI
play more like the user.

## Deck Data Policy

Current input data in both legacy Sim and Builder is user data and must be
preserved during migration.

Anthology should ship with Commander preconstructed decks by default for the
game/simulation side. These bundled decks should be marked as `precon`, locked
against accidental deletion, and visually distinguishable from user decks.
Users should be able to duplicate a precon into an editable user deck.

Accepted precon source policy: the project owner may provide precon lists from
public/common sources, with Moxfield export as a likely practical import format.
Anthology Dev Mode should import, resolve, validate, and store/update local
bundled `precon` records rather than depending on Moxfield or another source at
runtime.

Dev Mode is project-owner tooling only. It should not appear in the normal
desktop app surface.

Deck metadata does not need a `format` field because Anthology is Commander-only.
Every deck should still record origin, lock/edit/delete flags, commander
`oracle_id` values, source metadata where available, copy lineage, platform
visibility, style eligibility, and sync eligibility.

Accepted sync bundle direction: sync user-owned portable data only. Include user
decks, copied precons, imported decks, containers, inventory entries, deck
assignments, lightweight player style summaries, portable settings, and
validation metadata. Exclude Scryfall bulk data, SQLite caches, generated Card
Codex caches, bundled precons, logs, crash dumps, normal sessions, replay
archives unless separately exported, and provider credentials.

## Commander Sim Current Technical Baseline

Commander Sim was deliberately reduced on June 13, 2026.

The previous rules-first implementation was removed because it contained too
much disconnected rule metadata without one authoritative engine path. The repo
now has a tested Phases 0-5 base mechanics foundation alongside the retained
application shell and heuristic simulator. It must not yet be described as a
complete Magic rules engine.

Current tested base:

- Phase 0: state model
- Phase 1: turn structure
- Phase 2: priority and stack ordering
- Phase 3: basic playing and casting
- Phase 4: base combat
- Phase 5: initial state-based actions

Authoritative foundation module:

- `Commander-Sim/PROD/commander_sim/foundation.py`

Current validation from the old Sim handoff:

- 38 Python source files parsed with 0 syntax errors.
- Key imports passed.
- 76 focused tests passed.
- One-game CLI smoke passed.

This remains a foundation, not full rules-correct Magic.

## Commander Sim Layout

Production:

- `Commander-Sim/PROD/`
- `Commander-Sim/PROD/Open Commander Sim.bat`
- `Commander-Sim/PROD/commander_sim/launcher.py`
- `Commander-Sim/PROD/import_decks/`
- `Commander-Sim/PROD/data/`

Developer:

- `Commander-Sim/DEV/`
- `Commander-Sim/DEV/Open Commander Sim Dev.bat`
- `Commander-Sim/DEV/dev_launcher.py`
- `Commander-Sim/DEV/storage_cli.py`
- `Commander-Sim/DEV/tests/`

Production must not depend on DEV. Developer tools may import and modify PROD.

## Legacy CCBuilder Technical Baseline

The working legacy CCBuilder app/skeleton under `Commander analyst` contains:

- Gradle Kotlin DSL project
- Android module groundwork
- `shared-core` Kotlin/JVM module
- `desktop` Compose Desktop module
- desktop shell with Decks, Collection, Search, and Sync workspaces
- local JSON desktop persistence
- Scryfall lookup and local cache work
- Scryfall bulk-data install/update controls
- SQLite card cache build from `default_cards.json`
- deck import review and row resolving
- collection import with printing selection
- deck/container/card create/edit/delete flows

This should be treated as working legacy software to migrate and preserve while
the combined Anthology direction moves toward one Java desktop app and one Java
Android app. It was not missing core deck/collection functionality; it was
missing the Sim/AI/player-style connection that Anthology now defines.

Desktop data currently saves to:

- `%APPDATA%\Commander Analyst\commander-analyst-data.json`

Project-local Scryfall bulk/cache data currently lives under:

- `Commander analyst/data/scryfall-bulk-data/`

## Immediate Build Direction

Do not smash the two codebases together blindly. Merge the product by creating
shared truth and then moving surfaces onto it.

Prefer porting proven legacy behavior over rewriting from scratch unless it
conflicts with accepted Anthology boundaries, platform layering, or known
correctness requirements.

Recommended next slice:

1. Rename/conceptually frame the combined product as Commander Anthology.
2. Treat Commander Sim integration as the first major implementation job,
   because CCBuilder already has a working deck/collection app spine.
3. Define the shared core boundary:
   - Card identity
   - Card Codex/Scryfall cache
   - Commander legality
   - Deck import
   - Collection/inventory
   - Deck context
   - Player style profile
4. Decide which existing card-data path becomes the shared Java source of truth:
   - Commander Sim's Card Codex
   - CCBuilder's existing SQLite Scryfall cache
   - or a deliberate bridge/migration between them
5. Implement the accepted portable sync bundle schema so desktop and Android can
   link without a required paid/maintained server.
6. Inventory current Sim and Builder user data, then define deck origin/lock
   metadata for user decks and bundled precons.
7. Keep CCBuilder building practical deck/collection features.
8. Keep Commander Sim focused on legal move generation and Play/Auto.
9. Add player-style capture only after the legal move record format is stable
   enough to survive refactors.

## Useful Commands

Build unified Anthology project:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat build
```

Run unified desktop shell:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:run
```

Run desktop persistence smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:persistenceSmokeTest --console=plain --no-daemon
```

Run desktop CCBuilder migration smoke tests:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:deckImportParserSmokeTest :anthology-desktop:legacyCcBuilderDataImporterSmokeTest :anthology-desktop:collectionMutationSmokeTest :anthology-desktop:cardLookupValidatorSmokeTest --console=plain --no-daemon
```

Run collection import parser/review smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:collectionImportParserSmokeTest --console=plain --no-daemon
```

Run Card Codex/search and assignment-copy smoke tests:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:cardCodexSearchSmokeTest :anthology-desktop:deckAssignmentCopyChoiceSmokeTest :anthology-desktop:deckImportReviewedIdentitySmokeTest :anthology-desktop:manualCardSelectionSmokeTest --console=plain --no-daemon
```

Run Sync bundle smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:syncBundleSmokeTest --console=plain --no-daemon
```

Run Scryfall cache status smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:scryfallCacheStatusSmokeTest --console=plain --no-daemon
```

Run Scryfall bulk-data service smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:scryfallBulkDataServiceSmokeTest --console=plain --no-daemon
```

Run card image cache smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:cardImageCacheSmokeTest --console=plain --no-daemon
```

Run Scryfall SQLite build smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:scryfallSqliteBuildSmokeTest --console=plain --no-daemon
```

Restore live Anthology desktop state from CCBuilder user data:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:restoreCcBuilderUserData --console=plain --no-daemon
```

Build CCBuilder desktop:

```powershell
cd "E:\Commanader Anthology\Commander analyst"
.\gradlew.bat --no-daemon :desktop:build --console=plain
```

Run CCBuilder desktop:

```powershell
cd "E:\Commanader Anthology\Commander analyst"
.\gradlew.bat --no-daemon :desktop:run --console=plain
```

Build legacy Android app:

```powershell
cd "E:\Commanader Anthology\Commander analyst"
.\gradlew.bat --no-daemon :app:assembleDebug --console=plain
```

Run Commander Sim tests:

```powershell
cd "E:\Commanader Anthology\Commander-Sim"
& "C:\Program Files\Python310\python.exe" -m unittest discover -s DEV\tests
```

## Operating Notes

- Preferred Python for Commander Sim:
  `C:\Program Files\Python310\python.exe`
- Commander Sim decklists belong in:
  `Commander-Sim/PROD/import_decks/`
- Commander Sim local library:
  `Commander-Sim/PROD/data/library/`
- Commander Sim working sessions:
  `Commander-Sim/PROD/data/sessions/`
- Do not delete user decks, art, library data, session data, or collection data
  without explicit instruction.

## Current Anthology Desktop State

- Current desktop app/build version is `V0.5.5`.
- `V0.3.0 - Commander Sim Java Spine` has landed. Next active game work should
  continue the Commander Sim slices from the Java legal-move/game-state spine,
  not jump to full AI.
- Dashboard is hidden from normal desktop navigation because it was only a
  placeholder status page. The app opens directly to Deck Builder.
- Card Codex is hidden from normal desktop navigation because it is card-data
  infrastructure, not a normal user workflow. Its lookup/cache services remain
  active behind Deck Builder, Collection, import review, and card-detail flows.
- Deck Builder and Collection now follow the CCBuilder two-pane editable model
  instead of static lists.
- Deck Builder primary visible actions are intentionally simple:
  - `New deck`
  - `Add card`
  - `Import`
  - `More...`
  - `Assign selected`
  - `More selected...`
- Deck Builder maintenance actions are still present but moved behind dialogs:
  - deck `More...`: `Edit deck`, `Resolve deck`, `Delete deck`
  - selected-card `More selected...`: `View`, `Edit`, `Unassign`, `Delete`
- Deck Builder no longer exposes migration/dev actions in normal UI:
  `Validate all card input`, `Load legacy test decks`, and `Load CCBuilder data`.
  The underlying state/import helpers remain for tests, migration recovery, and
  controlled maintenance.
- Card input now validates/resolves against the existing CCBuilder Scryfall
  SQLite cache when present. The lookup scans AppData and project-local cache
  locations, including:
  `Commander analyst/data/scryfall-bulk-data/scryfall-cards.sqlite`
- CCBuilder user data source:
  `%APPDATA%\Commander Analyst\commander-analyst-data.json`
- Live Anthology desktop state:
  `%APPDATA%\Commander Anthology\anthology-desktop-state.json`
- Live state was restored from CCBuilder data on 2026-07-29. The previous
  Anthology state was backed up before replacement.
- Current restored counts:
  - containers: `59`
  - inventory entries: `2954`
  - decks: `4`
  - deck slots: `366`
  - deck assignments: `26`
  - unresolved card input rows: `0`
- A minimal bundled lookup seed exists for first-run validation when the full
  cache is missing:
  `anthology-desktop/src/main/resources/com/commanderanthology/desktop/card-lookup-seed.tsv`
- Collection supports container add/edit/delete and inventory add/edit/move/delete.
- Collection import now has a paste/CSV review flow:
  - pasted quantity-first rows
  - CSV with `Card Name`, `Quantity`, `Set Code`, `Collector Number`,
    `Finish`, and `Scryfall ID`
  - local Scryfall cache validation
  - selected-printing review for ambiguous rows
  - exact Scryfall-ID import when available
- Deck import now has a review flow before rows are accepted:
  - pasted decklist rows are parsed by section
  - local Scryfall cache validation runs before import
  - ambiguous rows can choose a selected printing
  - selected Scryfall printing identity is preserved on the deck slot
- Deck and Collection Add/Edit forms now include a local lookup printing chooser,
  so manually entered rows can preserve exact selected Scryfall identity.
- Desktop visual theme now centralizes the legacy CCBuilder/Cynful palette in
  `AnthologyTheme` and applies it across shell navigation, tables, buttons,
  scroll panes, import review dialogs, and card-detail dialogs.
- The legacy `cynful_logo.png` asset is copied into Anthology desktop resources
  and shown in the sidebar brand block.
- Deck assignment now asks which available physical copy to assign instead of
  silently taking the first matching inventory row.
- Card Codex infrastructure can search the local Scryfall SQLite cache and show
  local card details from `details_json`, but it is not exposed as a normal
  live-user sidebar tab.
- Card Codex maintenance can show discovered local Scryfall cache locations and
  whether each contains SQLite/default/oracle/rulings/manifest files.
- Card Codex maintenance has Scryfall bulk-data controls:
  - check remote bulk metadata
  - install/update bulk JSON files into `%APPDATA%\Commander Anthology\scryfall-bulk-data`
  - adopt an existing external/legacy `scryfall-cards.sqlite` into Anthology's
    cache folder
  - build Anthology's own `scryfall-cards.sqlite` from `default_cards.json`
- Card lookup remains offline-first and now falls back to Scryfall API only
  after local SQLite and bundled seed lookup miss.
- Deck and Collection selected rows now open local Scryfall card-detail dialogs
  when a resolved Scryfall ID is present.
- Card detail dialogs attempt to load card art from the stored Scryfall image
  URL without blocking text display.
- Card art is cached under `%APPDATA%\Commander Anthology\card-images` after
  first successful load.
- Backup workspace now exports/imports the current user-owned desktop state JSON
  as a local backup bundle. It does not include generated Scryfall bulk/cache
  files. It is not cloud sync, Google sync, account linking, or mobile linking.
- `Launch Commander Anthology.bat` now delegates to
  `Launch Commander Anthology.ps1` for local Java discovery before invoking
  Gradle. The helper scans `JAVA_HOME`, common Windows JDK install folders, and
  PATH, then uses the first verified Java 17+ install. This avoids launcher
  failures when a moved machine/external-drive workspace has no `JAVA_HOME` or
  PATH points at an older Java shim.
- Gradle no longer requires a separate exact Java 17 toolchain install for
  compilation. The root build uses Java 17 source/target compatibility and
  `--release 17`, allowing the launcher-selected Java 21 JDK to compile Java
  17-compatible output without toolchain auto-download configuration.
- Desktop runtime now includes `slf4j-nop` to satisfy the SLF4J API dependency
  pulled by SQLite JDBC. This removes the noisy "StaticLoggerBinder" warning
  during launcher startup until Anthology deliberately adopts a real logging
  backend.
- Moving a collection row merges matching physical card rows in the target
  container.
- Existing CCBuilder user data is available as an import/test fixture.
- Remaining CCBuilder parity work: broader import polish, search/filter
  refinements, richer deck/collection metrics, and deeper layout polish.
