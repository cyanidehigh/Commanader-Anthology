# Player Style
#type/area #area/player-style #status/active

Links: [[../DDS]], [[Shared Core]], [[Commander Sim]], [[CCBuilder]], [[Card Function Grammar]], [[../decisions/0015 Player Style Data Boundary]], [[../decisions/0026 Rules Semantics And Function Tags Split]]

Player style is learned from human decisions in Commander Sim Play mode.

CCBuilder uses this collected data to personalize recommendations, cuts,
substitutions, deck-building explanations, and collection-aware choices.

Player style must not be offered to the Sim AI as strategic input. It is not a
training signal for the AI opponent and must not make the AI play more like the
user.

It must not override deterministic truth:

- rules legality
- Commander legality
- card identity
- oracle text
- ownership
- physical inventory movement

## First Source

The first player-style source is actual gameplay:

- human-selected game actions in Play mode
- the visible/known context around those actions
- relevant deck and card `oracle_id` references

Auto mode and AI-selected moves do not update player style.

Strategic function tags may be used to explain player style in familiar Magic
terms, such as preferring tutors, mana sinks, recursion, theft effects, or card
draw. These tags are descriptive only. They do not become rules authority and
they are not made available to the AI opponent as player-style input.
