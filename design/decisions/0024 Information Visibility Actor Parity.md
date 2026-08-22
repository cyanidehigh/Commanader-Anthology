# 0024 Information Visibility Actor Parity
#type/decision #status/accepted #area/knowledge-projection #area/ai-architecture

Links: [[../DDS]], [[../areas/Knowledge Projection]], [[0013 Knowledge Projection Boundary]], [[0022 Validation Strategy]], [[0020 AI Decision Contract]]

## Decision

All game actors use the same information visibility rules.

A human player and an AI player sitting in the same game seat must receive the
same legal public and private information projection for that actor.

The AI does not receive special hidden information. The human UI does not expose
more game truth than that player is legally allowed to know.

## Rules Source

Information visibility is grounded in the Magic rules manual, not an
Anthology-specific AI exception system.

Primary local reference:

- `Commander-Sim/DEV/MagicCompRules 20260227.docx`

Starting anchor:

- Rule `400.2` defines public and hidden zones.

## Actor Projection Rule

Projection is based on actor position, not actor type.

For a given actor seat:

- human UI projection and AI projection must match
- public information is visible
- that actor's legally known private information is visible
- hidden opponent information is not visible
- hidden library order is not visible unless revealed or set by an effect
- inference may be represented separately, but not as known fact

## Consequences

- AI cannot cheat by seeing more than the player could see.
- Human UI cannot accidentally reveal hidden authoritative state.
- Legal Move Package and AI scoring can consume the same actor projection.
- Validation can test projection equality between human and AI consumers for the
  same seat.
- Future spectator, replay, analytics, and coaching views must declare their
  own projection purpose instead of reusing actor projection silently.

