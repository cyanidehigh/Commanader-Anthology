# 0020 AI Decision Contract
#type/decision #status/accepted #area/ai-architecture

Links: [[../DDS]], [[../areas/AI Architecture]], [[../areas/AI Move Scoring]], [[../areas/Sim Integration]], [[../areas/Knowledge Projection]], [[0016 Legal Move Package Boundary]], [[0015 Player Style Data Boundary]]

## Decision

The AI chooses from engine-produced legal moves.

The AI Decision Contract defines what the AI receives, what it may score or
rank, and what it may return. The AI does not own game truth, legality,
knowledge projection, state mutation, or execution.

The AI may use a point/weight system to score legal moves, as long as scoring
only applies to moves already produced by the Legal Move Package.

## Input

The AI may receive:

- `playerId`
- projected game state for that player
- structured `legalMoves`
- deck context for the AI's deck
- public game context
- permitted memory/strategy data for the AI
- model/system version metadata

The AI must not receive:

- raw omniscient game state
- opponent hidden information
- hidden library order unless revealed by game mechanics
- player style data
- illegal move candidates

## Output

The AI may return:

- `selectedLegalMoveId`
- optional ranked legal alternatives
- optional score/weight values for legal moves
- optional explanation/debug notes

The AI must not return:

- direct game-state mutations
- invented moves outside the legal move list
- hidden-information claims
- rule/legality overrides

## Move Points And Weights

The AI may score legal moves using a point/weight system.

Requirements:

- every scored move must correspond to an engine-produced legal move ID
- scores rank preferences, not legality
- score format must be deterministic enough for debugging and tests
- ties must have a deterministic tie-break rule or recorded random seed
- scoring must not use player style data
- scoring must not modify player style data
- scoring must not bypass final engine validation

The point/weight system is strategic preference only.

The detailed scoring shape is tracked separately in [[../areas/AI Move Scoring]]
so the contract can stay stable while the first AI weights are tuned.

## Execution Flow

```text
Authoritative Game State
        |
Knowledge Projection
        |
Legal Move Package
        |
Legal Move List
        |
AI scores/ranks legal moves
        |
AI returns selectedLegalMoveId
        |
Engine validates again
        |
Engine applies move
```

## Consequences

- AI strategy can improve without changing rules legality.
- Debugging can inspect why a legal move was preferred.
- Future learning/weights can attach to legal move choices without becoming
  rules truth.
- Player style remains Builder-facing and is not fed into the AI opponent.
