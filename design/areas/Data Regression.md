# Data Regression
#type/area #area/data-regression #status/draft

Links: [[../DDS]], [[Shared Core]], [[Card Codex]], [[../decisions/0004 Offline First Data Regression]]

## Purpose

Data regression is the policy for how Commander Anthology reads, caches,
refreshes, validates, and falls back across card, deck, collection, legality,
and style data.

The guiding rule is:

> Stay offline as much as possible for speed and reliability. Call Scryfall only
> when local data is missing, stale, or cannot answer confidently.

This follows the current CCBuilder app direction: local cache first,
bounded Scryfall API fallback second.

## Offline-First Order

For card and lookup data, prefer this order:

1. Validated local Card Codex / bundled cache.
2. Local SQLite indexes or small per-card detail caches.
3. Local raw Scryfall bulk data if an indexed cache must be rebuilt.
4. Live Scryfall API fallback.
5. Store useful remote results into local cache when safe.

## Requirements

- Normal search, deck analysis, legality checks, and collection workflows should
  work from local data whenever possible.
- Live API calls must be bounded with timeouts and clear user-visible fallback
  states.
- Missing remote access must not corrupt local data.
- Remote data must not silently overwrite stable card identities.
- Cache/schema versions must be explicit once implemented.
- Bulk data refresh should be an intentional sync/update action, not a hidden
  dependency during normal use.

## Applies To

- [[Card Codex]]
- Scryfall lookup
- card detail views
- deck import resolving
- collection import resolving
- Commander legality
- future suggestion pipelines

## Open Questions

- Which existing cache becomes the first Java source of truth?
- How often should stale local Scryfall metadata be checked?
- Which remote lookup results are safe to persist automatically?
- How should the app report partial offline data to the user?
