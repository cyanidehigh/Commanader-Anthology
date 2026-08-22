# Draw Step
#type/rules #area/commander-sim #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[turn-structure]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Exact rule references: TODO, must be extracted from the source document before
  expanding this slice.

## Current Anthology Scope

The current slice draws one card for the active player when the engine enters
the draw step, if that player's library is not empty.

## Implemented Behavior

- `drawCard` moves the top library object to hand.
- `drawCards` repeats `drawCard`.
- `performDrawStep` draws for the active player when entering draw step.

## Unsupported / Not Yet Proved

- Losing for drawing from an empty library.
- Replacement effects.
- Additional draws.
- Skipping draws.
- Draw-step triggered abilities.

## Code

- `GameFoundation.drawCard`
- `GameFoundation.drawCards`
- `GameFoundation.performDrawStep`

## Tests

- `GameFoundationSmokeTest`
- `BasicAiGameplaySmokeTest`

