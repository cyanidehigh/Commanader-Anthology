# 0016 Legal Move Package Boundary
#type/decision #status/accepted #area/sim-integration

Links: [[../DDS]], [[../areas/Sim Integration]], [[../areas/Knowledge Projection]], [[../areas/Shared Core]], [[../Commander Sim]]

## Decision

The Legal Move Package is a general engine/simulation boundary, not an AI
boundary.

Rules-legal moves are universal. The AI should, by definition, be able to do
exactly what the human player or any other player could legally do from the same
game state and knowledge position.

The AI does not invent legal moves. It chooses from legal moves produced by the
engine-owned legal move package.

## Ownership

The Legal Move Package is owned by the rules/simulation layer.

It should:

- determine which actions are legal
- produce structured legal move objects
- validate submitted moves
- reject illegal moves
- expose legal moves to consumers through permission-appropriate projections

## Consumers

Consumers may include:

- human UI
- AI player
- replay systems
- tests
- analytics
- coaching/explanation layers

The AI is one consumer. It is not the owner.

## Relationship To Knowledge Projection

Knowledge Projection controls what each player or consumer can legally know.

The Legal Move Package uses the relevant game state and projected knowledge to
produce the legal move set available to that player/consumer.

Simplified flow:

```text
Authoritative Game State
        |
Knowledge Projection
        |
Legal Move Package
        |
Legal Move List
        |
Human UI or AI chooses
        |
Engine validates and applies
```

## Requirements

- The AI cannot create a move outside the legal move package.
- Human UI cannot submit a move that bypasses validation.
- Every submitted move is validated before application.
- Illegal actions fail visibly.
- Legal move objects must be structured, deterministic, and testable.
- Legal move generation must not depend on player style.

## Consequences

- The same rules infrastructure supports humans, AI, tests, replay, analytics,
  and coaching.
- AI strength can improve without changing legality.
- Player style data cannot expand or restrict legal actions.
- The first Sim milestone can focus on making legal move generation reliable
  before advanced AI behavior exists.

