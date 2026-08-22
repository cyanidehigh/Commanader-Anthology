# Casting Spells Basic
#type/rules #area/commander-sim #status/partial

Links: [[README]], [[CAPABILITY_MATRIX]], [[priority]], [[mana-abilities]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Exact rule references: TODO, must be extracted from the source document before
  expanding this slice.

## Current Anthology Scope

Current spell casting is a simple movement model for supported card kinds:

- creature
- instant
- sorcery

The engine does not yet implement the full casting process.

## Implemented Behavior

- Spell must be in hand.
- Player must have priority.
- Non-instant spells require the current sorcery-window slice.
- Player must have enough generic mana.
- Casting pays generic mana and moves the spell to stack.
- On stack resolution:
  - creature resolves to battlefield
  - instant/sorcery resolve to graveyard

## Unsupported / Not Yet Proved

- Colored mana.
- Targets.
- Modes.
- X costs.
- Alternate/additional costs.
- Cost reductions/increases.
- Replacement effects.
- Triggered abilities.
- Countering spells.
- Card text execution.
- Commander tax/payment.

## Code

- `GameFoundation.castSpell`
- `GameFoundation.canCastSpell`
- `GameFoundation.resolveTopOfStack`
- `GameRules.legalMoves`
- `GameRules.execute`

## Tests

- `GameFoundationSmokeTest`
- `BasicAiGameplaySmokeTest`

