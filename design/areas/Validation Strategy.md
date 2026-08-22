# Validation Strategy
#type/area #status/draft #area/validation #area/sim-integration

Links: [[../DDS]], [[Sim Integration]], [[AI Architecture]], [[Structured Token Grammar]], [[Knowledge Projection]], [[../decisions/0022 Validation Strategy]], [[../decisions/0016 Legal Move Package Boundary]], [[../decisions/0021 Structured Token Grammar Boundary]]

## Purpose

Validation Strategy defines how Anthology proves a move is allowed before game
state changes.

The goal is to make normal play legal by construction while still protecting
against stale UI state, desync, corrupted move tokens, replay/import data, dev
tools, and future AI mistakes.

## Core Rule

Every move crosses at least two gates:

1. Legal move generation.
2. Final execution validation.

AI scoring, player UI choice, replay selection, and dev tooling sit between
those gates. They never replace either gate.

## Normal Play

In normal Play mode, the player should only be offered moves produced by the
Legal Move Package.

That means human moves should be legal by construction. The UI should not ask
the player to invent a move from text.

The final gate still validates the completed move token before state mutation.

## AI Play

The AI receives legal moves from the same Legal Move Package as the human UI.
It scores or ranks those legal moves, then returns a selected `legalMoveId`.

The engine validates the selected move again before execution.

## Validation Layers

Initial layers:

- schema validation
- legality validation
- knowledge validation
- execution validation
- post-state invariant validation
- regression validation
- reference/scenario validation
- explainability validation

## Fail Closed

If Anthology cannot prove a move is valid, it must reject the move, mark the
case unsupported, or require a rule module.

It must not silently approximate hidden rules behavior.

## Execution Flow

```text
Authoritative State
        |
Knowledge Projection validation
        |
Legal Move Package generation
        |
Structured token schema validation
        |
AI / player / replay / dev-tool selection
        |
Final legality validation
        |
Engine applies move
        |
Post-state invariants
        |
Replay / regression log
```

