# Knowledge Projection
#type/area #area/knowledge-projection #status/active

Links: [[../DDS]], [[AI Architecture]], [[Sim Integration]], [[../sources/Commander Anthology AI Architecture Design Journal]], [[../decisions/0013 Knowledge Projection Boundary]], [[../decisions/0024 Information Visibility Actor Parity]]

## Purpose

Knowledge Projection is the boundary that turns authoritative game state into
consumer-specific views.

Authoritative game state must not be handed directly to decision-making or
display systems. Each consumer receives only the information it is allowed to
know.

The projection model should mirror actual Magic information rules rather than
creating an artificial AI-only knowledge model.

Accepted actor-parity rule: projection is based on game seat, not actor type. A
human player and AI player in the same seat receive the same legal information
projection for that actor.

## Rules Reference

Primary rules reference currently lives in:

- `Commander-Sim/DEV/MagicCompRules 20260227.docx`

Important starting anchor:

- Rule `400.2` defines public zones and hidden zones. Graveyard, battlefield,
  stack, exile, ante, and command are public zones. Library and hand are hidden
  zones, even if all cards in one such zone happen to be revealed.

Anthology should map those existing rules into structured projection fields
rather than inventing a separate knowledge system.

## Consumers

Initial consumers:

- human UI
- AI player
- replay
- analytics
- coaching

Future consumers:

- spectator views

## AI Player Knowledge

The AI may know:

- its complete deck list
- its hand
- cards remaining somewhere within its own library
- battlefield
- graveyards
- public exile
- publicly revealed information

The AI must not know:

- opponent hand
- opponent library order
- opponent deck list unless revealed by game mechanics

Inference is separate from factual knowledge. The AI may infer, but inferred
possibilities must not become hidden factual knowledge.

The AI may know what is in its deck, much like a prepared real player, but not
the hidden order of its library unless a card effect reveals or changes that
knowledge.

## Boundary Rule

The rules engine owns truth. Knowledge Projection owns legal visibility. The AI
owns reasoning over its projected view.

The AI must not receive hidden truth that the same player seat would not expose
to a human UI. The human UI must not expose hidden authoritative state either.

## Mapping Work

The remaining implementation design is an information visibility mapping:

- public zones
- hidden zones
- revealed objects
- player-owned deck contents
- unknown library order
- known choices
- inferred possibilities
- consumer-specific projection permissions

Detailed field mapping remains a model task. The accepted decision is that the
mapping follows Magic information rules and applies equally to all actors in the
same seat.
