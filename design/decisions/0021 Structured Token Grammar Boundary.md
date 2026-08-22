# 0021 Structured Token Grammar Boundary
#type/decision #status/accepted #area/ai-architecture #area/sim-integration

Links: [[../DDS]], [[../areas/Structured Token Grammar]], [[../areas/AI Architecture]], [[../areas/AI Move Scoring]], [[0020 AI Decision Contract]], [[0016 Legal Move Package Boundary]]

## Decision

Structured Token Grammar is the shared structured language for legal moves,
chosen options, targets, costs, timing windows, scoring explanations, logs, and
validation.

The first version is an interchange and debug format for engine-produced legal
moves. It is not a free-form AI generation language.

## Meaning Of Explicit

Explicit means every player-controllable choice required to validate and execute
a legal move is represented as structured fields.

Explicit does not mean restating the card's Oracle text, predicting every rules
consequence, or duplicating the rules engine.

The grammar must be exact enough that the engine can reject invalid choices
without guessing.

## Boundary

The engine and Legal Move Package own legal move creation.

The AI may:

- receive structured legal move tokens
- score legal move tokens
- rank legal move tokens
- return the selected `legalMoveId`
- return optional reason/debug fields

The AI must not:

- invent legal move tokens outside the legal move list
- rely on vague fields such as `actor=player`
- hide game-relevant choices in display text
- use token grammar to bypass legality
- use token grammar to mutate game state directly

## Consequences

- Invalid AI choices are inspectable and rejectable.
- UI, logs, tests, and replay can share one move representation.
- The grammar can stay small while the rules engine owns card behavior.
- Future structured AI generation can be added later without weakening the
  first milestone boundary.

