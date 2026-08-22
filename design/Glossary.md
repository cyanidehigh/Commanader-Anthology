# Glossary
#type/glossary #status/draft #project/commander-anthology

## Commander Anthology

The unified product direction for [[CCBuilder]] and [[Commander Sim]].

## CCBuilder

The deck-building, collection, search, import, legality, and recommendation
surface. It exists on desktop and Android.

## Commander Sim

The desktop simulation surface. Its first AI target is a legal move package
with Play and Auto modes.

## Deterministic Truth

Facts that must come from structured systems rather than AI or player style:
rules legality, Commander legality, card identity, oracle text, color identity,
ownership, physical inventory movement, prices, and card existence.

## Player Style

Data learned from human decisions in Commander Sim Play mode. It may personalize
CCBuilder recommendations, but it cannot override deterministic truth.

## Card Codex

The shared card identity and card-data layer. It should resolve Scryfall
`oracle_id`, local stable card IDs, oracle identity, and printing identity.

## Data Regression

The offline-first data policy for local cache, local indexes, raw bulk data,
and live Scryfall API fallback.

## Sim Integration

The first major Anthology implementation program: transforming Commander Sim
from a separate project into the desktop simulation surface of Commander
Anthology.

## Cross Platform Sync

Optional serverless linking between desktop Anthology and Android CCBuilder
using local-first, user-owned, versioned data transfer instead of a required
maintained app server.

## Mobile Companion

The Android companion app. It provides game tracking/life counter, limited
multi-user win/loss telemetry, and light deck building. It is not the full
Anthology program.

## Precon

A bundled Commander preconstructed deck shipped with Anthology for simulation,
testing, examples, and analysis. Precons are locked against accidental deletion
and must be clearly marked separately from user decks.

## User Deck

A deck entered, imported, copied, or edited by the user. Existing legacy Sim and
Builder input data is user data and must be preserved during migration.

## Knowledge Projection

The boundary that converts authoritative game state into consumer-specific
views. It prevents the AI or UI from receiving hidden information they should
not legally know.

## Structured Token Grammar

The long-term AI action language where candidate actions are represented by
structured tokens such as action start/end, card instance, target, cost payment,
object movement, and draw operations.

## Telemetry

Session-local tracker/win-loss data shared only between users inside the same
active session. It is not global analytics by default.
