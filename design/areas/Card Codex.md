# Card Codex
#type/area #area/card-codex #status/active

Links: [[../DDS]], [[Shared Core]], [[Data Regression]], [[Card Function Grammar]], [[../decisions/0004 Offline First Data Regression]], [[../decisions/0011 Card Codex Source Of Truth]], [[../decisions/0026 Rules Semantics And Function Tags Split]]

The Card Codex is the shared card identity and card-data layer.

Scryfall `oracle_id` is the primary unique identity for normal official
card/rules identity. The old Commander Sim six-hex ID design is migration and
reference material, not the new primary identity.

It must distinguish:

- oracle/rules identity
- printing/display identity
- physical collection identity
- runtime game object identity

Commander Sim already has a Scryfall-backed Card Codex. CCBuilder has an
existing SQLite Scryfall cache. A future decision must choose whether to port,
bridge, or replace these with one shared Java Card Codex.

Accepted direction: build one shared Java Card Codex around SQLite-backed local
data, `oracle_id` as primary rules identity, printing IDs as separate physical
display identity, and typed Anthology fallback IDs only where Scryfall identity
is unavailable.

The Card Codex should be offline-first:

1. Read validated local data.
2. Read local SQLite/cache indexes.
3. Use live Scryfall API only when local data is missing, stale, or unable to
   answer confidently.
4. Store successful remote results back into local caches when appropriate.

Card Codex must keep exact rules identity separate from strategic function
tags. Exact card semantics feed the rules engine and legal move package.
Function tags, tracked in [[Card Function Grammar]], describe player-facing
strategic roles and must not be treated as gameplay authority.

A proposed card-rules shape under discussion separates a card's printed/rules
structure from shared rules implementations: card name, cost, rules references
including keyword ability references, bespoke rules text, and power/toughness
where applicable. This is not locked. The design pressure is still useful:
keyword behavior should not be duplicated on every card that has that keyword.
