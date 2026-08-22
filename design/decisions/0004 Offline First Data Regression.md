# 0004 Offline First Data Regression
#type/decision #status/accepted #area/data-regression

Links: [[../DDS]], [[../areas/Data Regression]], [[../areas/Card Codex]], [[../areas/Shared Core]]

## Decision

Commander Anthology is offline-first for data access.

The application should use local data whenever possible for speed, reliability,
and deterministic behavior. Live Scryfall API calls are fallback or explicit
refresh mechanisms, not the normal dependency for everyday search, deck
analysis, legality, collection, or simulation workflows.

## Data Lookup Order

1. Validated local Card Codex / cache.
2. Local SQLite or indexed lookup data.
3. Local raw Scryfall bulk data when rebuilding local indexes.
4. Live Scryfall API only when local data is missing, stale, or cannot answer
   confidently.
5. Safe remote results may be cached locally.

## Consequences

- Java shared core must be designed around local data access first.
- Scryfall API calls must be bounded and visible when they fail.
- Bulk data install/update should be an intentional user-visible sync action.
- Remote refreshes must preserve stable card identities.
- Suggestions and legality should not depend on live network access during
  normal use.

