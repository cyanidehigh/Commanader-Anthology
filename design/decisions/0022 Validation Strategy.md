# 0022 Validation Strategy
#type/decision #status/accepted #area/validation #area/sim-integration

Links: [[../DDS]], [[../areas/Validation Strategy]], [[../areas/Sim Integration]], [[../areas/Structured Token Grammar]], [[../areas/Knowledge Projection]], [[0016 Legal Move Package Boundary]], [[0020 AI Decision Contract]], [[0021 Structured Token Grammar Boundary]]

## Decision

Every move crosses at least two gates:

1. Legal move generation.
2. Final execution validation.

AI scoring sits between those gates and never replaces either one. Player UI
selection, replay/import playback, and dev tooling also sit between those gates.

## Legal By Construction

In normal play, human and AI choices should be legal by construction because
they are selected from the Legal Move Package.

The human UI should only offer legal moves. The AI should only receive legal
moves.

Final execution validation still exists because a move may become stale,
desynced, corrupted, replayed from old data, imported externally, or produced by
dev tooling.

## Validation Layers

Anthology validation should include:

- schema validation: token shape and required fields
- legality validation: move legality against authoritative state
- knowledge validation: actor only uses allowed information
- execution validation: selected move is still legal immediately before mutation
- post-state invariant validation: state remains coherent after mutation
- regression validation: fixed scenarios keep expected behavior
- reference/scenario validation: curated fixtures anchored to rules and card data
- explainability validation: AI-selected moves remain traceable to legal move IDs

## Fail Closed

If Anthology cannot prove a move is valid, it must reject the move, mark it
unsupported, or require a rule module.

Anthology must not silently approximate uncertain game rules.

## Consequences

- Normal UI and AI play can stay ergonomic because users choose from legal
  options.
- Final validation protects against stale state, sync issues, replay/import
  problems, and future AI mistakes.
- Debugging has a clear path from offered move to selected token to executed
  mutation.
- Unsupported Magic behavior remains visible instead of becoming fake
  confidence.

