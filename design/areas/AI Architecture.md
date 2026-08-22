# AI Architecture
#type/area #area/ai-architecture #status/draft

Links: [[../DDS]], [[Knowledge Projection]], [[Sim Integration]], [[AI Move Scoring]], [[Structured Token Grammar]], [[Card Function Grammar]], [[../sources/Commander Anthology AI Architecture Design Journal]], [[../decisions/0020 AI Decision Contract]], [[../decisions/0021 Structured Token Grammar Boundary]], [[../decisions/0026 Rules Semantics And Function Tags Split]]

## Purpose

AI Architecture defines how Commander Anthology uses AI without letting AI own
rules truth, hidden information, or game mutation.

## Direction

Anthology should not try to hard-code bespoke behavior for every card inside
the AI.

The long-term direction is to model the language of Magic:

- structured game objects
- zones
- relationships
- legal actions
- card capabilities
- costs
- targets
- state transitions
- player knowledge projections

The AI should reason over structured data and produce structured candidate
actions. The deterministic engine validates and executes those actions.

## Current Boundary

- The rules engine owns authoritative game state and legality.
- Knowledge Projection creates permission-appropriate views.
- The AI receives a player-legal view, not omniscient game truth.
- The AI knows public information and its own deck contents, but not hidden
  library order unless revealed by game mechanics.
- The legal move package produces structured legal actions.
- The AI chooses from legal actions. It does not invent or own legal moves.
- The execution layer validates and applies legal actions.

The AI may use a point/weight system to rank legal moves, but those points are
strategic preference only. They do not create legality, mutate state, or consume
player style data.

Strategic function tags, tracked in [[Card Function Grammar]], may help explain
or score legal moves only after the legal move package has produced those moves.
They are not exact rules semantics and must never replace rules validation.

The scoring model starts in [[AI Move Scoring]]. It should remain transparent
enough that a move choice can be traced from legal move ID to score categories
to final selected move.

Structured legal move representation starts in [[Structured Token Grammar]].
Explicit means every player-controllable choice required to validate and execute
a legal move is represented as structured fields, while card rules consequences
remain owned by the engine.

## Not First Milestone

The first Sim milestone is still the legal move package. Transformer-driven
structured token generation is a long-term direction, not the immediate build
target.
