# Card Function Grammar
#type/area #area/card-function-grammar #status/draft

Links: [[../DDS]], [[Card Codex]], [[AI Architecture]], [[AI Move Scoring]], [[Player Style]], [[../decisions/0026 Rules Semantics And Function Tags Split]]

## Purpose

Card Function Grammar is the human-facing semantic layer that describes what a
card is useful for in play and deck construction.

It is separate from exact rules semantics.

The rules engine needs precise card behavior so it can generate, validate, and
execute legal moves. Function tags help Anthology explain deck shape,
recommendations, AI reasoning, and player style in language a player already
understands.

## Boundary

Card Function Grammar may describe strategic roles such as:

- `Mana > Sink`
- `Tutor > Creature`
- `Tutor > To Hand`
- `Steal > Creature`
- `Draw > Cards`
- `Removal > Creature`
- `Ramp > Land`
- `Sacrifice > Outlet`
- `Graveyard > Recursion`
- `Token > Creature`

These tags are not rules text.

They must not decide:

- whether a spell or ability can be cast or activated
- whether a target is legal
- whether a cost can be paid
- whether timing permission exists
- whether a triggered or replacement effect applies
- whether game state changes

## Authority Model

```text
Card Codex / Oracle Identity
        |
Exact Rules Semantics
        |
Rules Engine + Legal Move Package
        |
Validated Legal Moves
        |
AI Scoring
        |
Function Tags For Explanation And Style
```

Function tags may influence scoring only after the engine has produced legal
moves. They can explain why a move is attractive, but they cannot create the
move.

## Mythic Tools Reference Note

A Mythic Tools deck page was reviewed as a design reference. The visible UI
showed relationship chips such as:

- `Mana > Sink`
- `Steal > Creature`
- `Tutor > Creature`
- `Tutor > To Hand`

The pasted Nuxt/Vite source did not contain those labels as static strings.
Instead, it exposed product-specific API client paths such as:

- `/deck/{id}/platform-meta`
- `/deck/{id}/ai-analysis`
- `/deck/{id}/card-rankings`
- `/deck/{id}/combos`
- `/deck/{id}/combos/smart-suggestions`
- `/deck/{id}/cf-recommendations`
- `/deck/{id}/gap-recommendations`
- `/deck/{id}/card/{cardId}/explain`

Conclusion: this appears to be a bespoke Mythic Tools analysis backend, not a
public reusable MTG API. Anthology should not depend on it or copy its data.
The useful takeaway is the product pattern: deck/card data plus a separate
analysis layer for tags, rankings, gaps, recommendations, and explanations.

## Player Style Use

Player style should use function tags to summarize familiar patterns:

- the user likes tutoring for creatures
- the user often leaves mana sinks available
- the user prefers theft effects
- the user plays recursion-heavy lines
- the user values card draw over board pressure

This lets the Builder and future LLM-facing explanation layer talk to the
player naturally without feeding player style into the AI opponent.

## Data Shape Direction

Function tags should be keyed by `oracle_id` where possible.

Each tag should eventually track:

- source card identity
- action family
- object or target family
- destination or result where relevant
- confidence
- source of annotation

Possible annotation sources:

- curated dev entry
- deterministic Oracle parsing
- inferred analysis
- imported legacy knowledge

Uncertain or inferred tags must be marked as such.

## New Set Maintenance Direction

New set support must not require hand-coding every tag from scratch.

The intended workflow is:

```text
New Scryfall data imported
        |
Auto-seed obvious function tags from type line, oracle text, keywords, costs,
and known phrase patterns
        |
Assign confidence values
        |
Open a Dev Mode review queue for uncertain or risky cards
        |
Store approved Anthology-owned tags by oracle_id
```

Examples of auto-seed patterns:

- `Search your library` plus `creature card` -> `Tutor > Creature`
- `put it into your hand` -> `Tutor > To Hand`
- `destroy target creature` -> `Removal > Creature`
- `return target card from your graveyard` -> `Graveyard > Recursion`
- `create a ... token` -> `Token > Creature`
- repeatable activated abilities or `{X}` costs -> possible `Mana > Sink`
- `add {` in an activated mana ability context -> possible `Mana > Produce`

Hand-authored tags are still allowed, but they are the correction path, not the
default workload.

## Current Scope

This area is documented for later work.

It is not part of the current V0.5.0 rules-engine implementation slice.
V0.5.0 remains focused on explicit rules foundations, legal move generation,
validation, turn progression, and basic AI gameplay.

## Open Questions

- What is the first controlled vocabulary?
- Which tags are useful enough for V0.5.x and which belong later?
- How much should tags be manually curated before Oracle parsing exists?
- Should tags live in the local SQLite Card Codex or in Anthology-owned tables
  beside it?
- What minimum confidence score is acceptable before a tag can be shown to a
  normal user?
- Which low-confidence tags must be hidden until reviewed in Dev Mode?
