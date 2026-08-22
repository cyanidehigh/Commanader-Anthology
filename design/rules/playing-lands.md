# Playing Lands
#type/rules #area/commander-sim #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[turn-structure]], [[priority]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Exact rule references: TODO, must be extracted from the source document before
  expanding this slice.

## Current Anthology Scope

Anthology currently supports playing one land from hand during the current
engine's sorcery-window slice.

## Implemented Behavior

- Land must be in the player's hand.
- Player must have priority.
- It must be the active player's main phase in the current slice.
- Stack must be empty.
- Player must not have played a land this turn.
- Land moves to battlefield as a permanent.

## Unsupported / Not Yet Proved

- Effects allowing extra land plays.
- Effects allowing lands at unusual times.
- Modal DFC land/spell choice.
- Lands played from zones other than hand.
- Land drops restricted or modified by card effects.

## Code

- `GameFoundation.playLand`
- `GameFoundation.canPlayLand`
- `GameRules.legalMoves`
- `GameRules.execute`

## Tests

- `GameFoundationSmokeTest`
- `BasicAiGameplaySmokeTest`

