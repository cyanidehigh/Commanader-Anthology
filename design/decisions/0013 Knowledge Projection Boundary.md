# 0013 Knowledge Projection Boundary
#type/decision #status/accepted #area/knowledge-projection

Links: [[../DDS]], [[../areas/Knowledge Projection]], [[../areas/AI Architecture]], [[../Commander Sim]], [[0024 Information Visibility Actor Parity]]

## Decision

Knowledge Projection should become a first-class Anthology boundary.

The deterministic engine owns authoritative game state. Consumers do not receive
raw omniscient state; they receive projected views appropriate to their role and
permissions.

Knowledge Projection mirrors actual Magic information rules. The AI and players
know public information. A prepared player can know or track what cards are in
their own deck, but not the hidden order of their library unless an effect
reveals or changes that knowledge.

Actor parity is accepted in [[0024 Information Visibility Actor Parity]]: human
and AI actors in the same seat receive the same information projection.

The AI should have the same kind of legal knowledge a real player could have. It
must not receive cheating knowledge.

## Rules

- The AI receives only what a real player could legally know.
- Human UI receives the human player's legal view.
- Replay and analytics can receive broader views according to their purpose.
- Coaching views must distinguish known facts from inferred possibilities.
- Hidden information must not leak into AI decision input.
- Public information is visible to all relevant consumers.
- A player/AI may know their own deck contents, but not hidden library order
  unless revealed by game mechanics.
- Opponent hand, opponent library order, and unrevealed hidden zones remain
  hidden.
- Inference is allowed, but inferred possibilities are not treated as facts.

## Rules Reference

The implementation should map Magic's existing information rules rather than
inventing Anthology-specific knowledge exceptions.

Primary local reference:

- `Commander-Sim/DEV/MagicCompRules 20260227.docx`

Starting anchor:

- Rule `400.2` defines public and hidden zones.

## Consequences

- AI and human players operate under the same information rules.
- Legal move generation can use projected knowledge without leaking hidden
  information.
- Analytics/replay/coaching may have separate projections, but those projections
  must be explicitly scoped.
- The AI should not be weaker or stronger because of information access. It
  should simply know what a real player could legally know.
