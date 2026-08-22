# Commander Anthology

Commander Anthology is the combined direction for CCBuilder and Commander Sim.

It is one Commander platform with two surfaces:

- **Desktop:** the full-spectrum Commander Anthology program, including
  everything Anthology has to offer.
- **Android app:** a light companion with game tracker/life counter, limited
  win/loss telemetry when two or more players are using it together, and light
  deck building.

Tracker telemetry is session-local by default: shared only between users inside
the same active session.

The project is moving toward Java for both the desktop application and the
Android companion app, with shared Java business rules underneath both
surfaces.

## What It Is

CCBuilder is the deck-building and collection surface. On desktop it can use
the full Anthology toolset. On mobile it should be a light deck-building
companion, not the full desktop program.

Commander Sim is the desktop simulation surface. Its first AI target is a
legal move package that can play Magic effectively, run Play and Auto sessions,
and record how the human player actually plays.

Legal move generation belongs to the engine/simulation layer. AI and human UI
consume the same legal move truth.

The AI may score or weight legal moves, but only after the engine has produced
the legal move list. Scores rank preferences; they do not define legality.
The first scoring model should keep move points inspectable by category and
reason code so AI choices can be debugged later.

Legal move tokens should be explicit enough for validation to reject bad choices
without guessing. Explicit means player-controllable choices are structured
fields; card rules consequences remain owned by the engine.

Every move crosses two validation gates: legal move generation and final
execution validation. Normal UI and AI choices should be legal by construction,
but final validation guards against stale, desynced, replayed, imported, or
dev-tool moves.

AI and human players use the same information rules: public information is
visible, own deck contents can be known, and hidden library order or opponent
hidden information stays hidden unless revealed.
Projection is based on game seat, not actor type, so a human and AI in the same
seat receive the same legal information view.

Together, the long-term product loop is:

1. CCBuilder understands the user's collection and decks.
2. Commander Sim observes the user's real Play-mode decisions.
3. Anthology builds a player style profile from that data.
4. CCBuilder uses that profile to recommend builds, cuts, substitutions, and
   collection-aware paths that fit the player.

Player style is for Builder personalization. It is not fed back into the Sim AI
as strategic input.

## Core Rules

Deterministic truth stays deterministic.

AI, player style, and recommendation layers may personalize and explain, but
they must not decide rules legality, Commander legality, card identity, oracle
text, ownership, physical inventory movement, prices, or card existence.

Unsupported behavior should fail visibly instead of being silently
approximated.

## Target Stack

Going forward:

- Java shared core
- Java desktop app for full-spectrum Commander Anthology
- Java Android companion app for tracker/life counter, limited telemetry, and
  light deck building
- Local-first persistence
- Offline-first data access, with local cache first and Scryfall API fallback
- Shared Scryfall/Card Codex data
- Optional serverless sync through portable/user-owned data before any
  maintained server
- Google Drive is a candidate for optional user-owned sync, not a requirement

Existing Kotlin/Compose CCBuilder work is a working legacy app/skeleton, and
existing Python Commander Sim work is a working legacy simulation foundation.
Both remain valuable migration sources, but the combined product direction is
Java.

The legacy `Commander analyst` and `Commander-Sim` folders are temporary
staging/reference areas. The long-term direction is one unified Anthology
project after useful code, tests, and user data are migrated or retired
deliberately.

Migration should preserve proven behavior. CCBuilder is the primary source for
Builder workflows. Commander Sim is the primary source for simulation behavior
and test knowledge, even though its Python code must be ported deliberately into
Java.

Card-Forge/forge may be used as comparative reference for hard Magic-engine
problems, but Commander Anthology is not a Forge clone and should not copy Forge
code or depend on Forge.

## License

Commander Anthology source code is licensed under the GNU General Public
License version 3.0. See [LICENSE](LICENSE).

Commander Anthology is unofficial Fan Content and is not approved or endorsed
by Wizards of the Coast. Magic: The Gathering card names, mana symbols, Oracle
text, card images, Comprehensive Rules text, Scryfall data, and related
third-party materials are not relicensed by this project's GPLv3 license. See
[NOTICE.md](NOTICE.md) for the project-specific boundary.

## Current Folders

```text
anthology-core/
  Shared Java core for Anthology models and platform-neutral rules boundaries.

anthology-desktop/
  Java desktop shell for the full-spectrum Anthology application: Builder,
  Collection, Card Codex, Sim, Player Style, and Sync.

anthology-android/
  Reserved home for the Java Android companion app.

design/
  Obsidian-ready design system: DDS, kanban, areas, and decisions.

Commander analyst/
  Working legacy CCBuilder app/skeleton with desktop, Android, Scryfall cache,
  collection, deck import, and local persistence work.

Commander-Sim/
  Existing Commander Sim project with production/developer split, Card Codex
  work, deck import, heuristic simulator, and tested Phases 0-5 foundation.

HANDOFF.md
  Single source of truth for current direction, decisions, and next steps.
```

## Current Focus

Do not blindly merge the old codebases. Merge the product by creating shared
truth first:

- card identity
- Scryfall/Card Codex cache
- offline-first data regression
- Commander legality
- deck import
- deck catalog metadata for user decks and locked bundled precons
- collection inventory
- deck context
- player style profile
- optional desktop/Android sync bundle boundaries

Then move CCBuilder and Commander Sim onto that shared Java foundation.

The first major integration job is Commander Sim: CCBuilder already has a
working deck/collection app spine, while Sim needs to be transformed into
Anthology's desktop simulation surface.

The desktop app now has real Deck Builder, Collection, Card Codex, and Sync
workspaces backed by shared Java models and CCBuilder-derived behavior. It can
restore existing CCBuilder user data, persist local desktop state as JSON,
import decklists and collection rows through review flows, resolve card
identity through local Scryfall data, choose exact physical copies for deck
assignment, inspect local card details, cache card art, and export/import a
portable user-owned sync bundle.

Existing deck/input data in the legacy Sim and Builder folders is user data and
must be preserved. Anthology should also ship Commander preconstructed decks for
the game side, marked as locked `precon` decks and separable from user decks.
Precon lists may be provided from public/common sources, with Moxfield export as
a likely practical import path, then stored locally after validation through Dev
Mode.

The sync bundle is for user-owned portable data only. Generated caches and
bundled reference data are rebuilt or shipped locally rather than synced.

Start design work from:

- [[design/DDS]]
- [[design/KANBAN]]

Build the unified Anthology Java project:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat build
```

Run the desktop shell:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:run
```

Run the persistence smoke test:

```powershell
cd "E:\Commanader Anthology"
.\gradlew.bat :anthology-desktop:persistenceSmokeTest --console=plain --no-daemon
```
