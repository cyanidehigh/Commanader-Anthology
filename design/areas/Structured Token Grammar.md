# Structured Token Grammar
#type/model #status/draft #area/ai-architecture #area/sim-integration

Links: [[../DDS]], [[AI Architecture]], [[AI Move Scoring]], [[Sim Integration]], [[../decisions/0021 Structured Token Grammar Boundary]], [[../decisions/0020 AI Decision Contract]]

## Purpose

Structured Token Grammar defines the stable language used to represent legal
moves, chosen options, costs, targets, timing, and debug explanations.

It exists so the engine, UI, AI, logs, replay tools, and validation tests can
refer to the same move without relying on vague text.

## Explicit Definition

Explicit means every player-controllable choice required to validate and execute
a legal move is represented as structured fields.

Explicit does not mean restating Oracle text or duplicating the rules engine.
Rules consequences remain owned by the engine.

The grammar should be exact enough that the engine can reject invalid choices
without guessing.

## Core Fields

Initial legal move tokens should support:

```text
type
legalMoveId
actor
sourceObject
cardOracleId
displayName
fromZone
toZone
targets
modes
costChoice
manaPayment
xValue
timingWindow
reasonCodes
scoringVersion
```

Not every move needs every field. Missing fields must be intentional and
schema-valid, not implied by free text.

## Example Cast Spell Token

```text
type=CAST_SPELL
legalMoveId=move:98271
actor=player:ai-1
sourceObject=gameObject:hand:abc123
cardOracleId=6ad8011d-3471-4369-9d68-b264cc027487
displayName="Sol Ring"
fromZone=HAND
targets=[]
modes=[]
costChoice=normal
manaPayment={red:1}
timingWindow=MAIN_PHASE_PRIORITY_STACK_EMPTY
```

## Rejected Example

```text
type=CAST_SPELL:Sol_Ring
actor=player
sourceObject=object:hand
cardOracleId=6ad8011d-3471-4369-9d68-b264cc027487
targets=[self]
costChoice=normal
manaPayment={red:1, green:1}
```

This is human-readable, but not engine-explicit.

Problems:

- action type and card identity are mixed together
- actor is not an exact player ID
- source object is not an exact game object
- Sol Ring has no cast target
- the mana payment overpays the normal cost

## First Milestone Rule

In the first milestone, the Legal Move Package creates structured legal move
tokens. The AI scores and selects from those tokens. The AI does not generate
free-form token strings from scratch.

Later, structured generation can be explored, but validation still owns truth.

