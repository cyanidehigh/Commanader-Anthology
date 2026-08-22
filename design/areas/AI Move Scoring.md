# AI Move Scoring
#type/model #status/draft #area/ai-architecture #area/sim-integration

Links: [[../DDS]], [[AI Architecture]], [[Sim Integration]], [[../decisions/0020 AI Decision Contract]], [[../decisions/0016 Legal Move Package Boundary]]

## Purpose

AI Move Scoring defines the point/weight system the Sim AI can use to choose
between legal moves.

This model is not rules legality. It is strategic preference layered on top of
the legal move list produced by the engine.

## Core Rule

No move can receive a score unless the Legal Move Package already produced it.

Scores answer:

> Of the legal things I can do, which one looks best from my current knowledge
> position?

Scores do not answer:

> Am I allowed to do this?

## Scoring Shape

Each scored move should be traceable as structured data:

```text
legalMoveId
baseScore
categoryScores
totalScore
tieBreak
reasonCodes
debugNotes
scoringVersion
```

## First Scoring Categories

Initial categories should be broad and easy to debug:

- mana efficiency
- board development
- card advantage
- tempo
- threat pressure
- protection and survival
- removal value
- commander plan support
- synergy with current board
- risk from public information
- end-step or phase timing value

These are starting categories, not permanent limits.

## Weight Rules

- category weights may change by AI personality or deck archetype
- category weights must be versioned
- weights must be deterministic for tests unless a random seed is recorded
- weights must not use player style data
- weights must not change rules legality
- weights must not directly mutate game state

## Example

```text
legalMoveId: cast:oracle:1234-target:player-2
baseScore: 0
categoryScores:
  manaEfficiency: 2
  boardDevelopment: 4
  threatPressure: 3
  removalValue: 0
  survival: 1
totalScore: 10
tieBreak: lowerManaWaste
reasonCodes:
  - develops_board
  - supports_commander_plan
  - spends_available_mana
scoringVersion: ai-score-v0
```

## Design Notes

The first version can be simple and hand-tuned. It only needs to play Magic
effectively enough to make legal, understandable choices.

Later versions can become more sophisticated, but they must keep the same
boundary: score legal moves, explain the preference, return a selected legal
move ID, and let the engine validate again before applying it.

