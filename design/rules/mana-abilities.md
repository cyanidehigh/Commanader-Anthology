# Mana Abilities
#type/rules #area/commander-sim #status/partial

Links: [[README]], [[CAPABILITY_MATRIX]], [[playing-lands]], [[casting-spells-basic]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Exact rule references: TODO, must be extracted from the source document before
  expanding this slice.

## Current Anthology Scope

Current support is intentionally temporary and basic: any untapped land
permanent controlled by the priority player can be tapped for one generic mana.

This is not Oracle-text parsing.

## Implemented Behavior

- Generated legal move: `ACTIVATE_MANA_ABILITY`.
- Only the priority player can activate this current basic ability.
- Object must be an untapped land permanent controlled by that player.
- Activation taps the land and adds one generic mana.
- Final execution validation still checks the generated legal move.

## Unsupported / Not Yet Proved

- Colored mana.
- Multiple mana options.
- Costs beyond tapping.
- Nonland mana abilities.
- Mana abilities usable outside priority windows.
- Mana abilities during casting/payment.
- Restrictions, replacement effects, and triggered mana abilities.

## Code

- `MoveType.ACTIVATE_MANA_ABILITY`
- `GameFoundation.activateManaAbility`
- `GameFoundation.canActivateManaAbility`
- `GameRules.legalMoves`
- `GameRules.execute`

## Tests

- `BasicAiGameplaySmokeTest`

