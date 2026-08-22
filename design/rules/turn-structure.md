# Turn Structure
#type/rules #area/commander-sim #status/partial

Links: [[README]], [[CAPABILITY_MATRIX]], [[priority]], [[draw-step]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Exact rule references: TODO, must be extracted from the source document before
  expanding this slice.

## Current Anthology Scope

Anthology currently models a linear turn-step sequence in
`GameFoundation.advanceStep`:

- untap
- upkeep
- draw
- precombat main
- combat
- postcombat main
- end

The current implementation is a foundation slice only.

## Implemented Behavior

- Advancing from `END` starts the next turn and performs untap behavior.
- Entering draw step performs an active-player draw when the library is not
  empty.
- Desktop setup shuffles both libraries before drawing opening hands.
- Most steps open priority after advancement except untap.

## Unsupported / Not Yet Proved

- Full beginning phase detail.
- Multiple combat substeps.
- Cleanup step.
- Trigger handling during upkeep/draw/end.
- Turn-based actions beyond the current tiny slice.
- Multiplayer turn-order edge cases.

## Code

- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameFoundation.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/TurnStep.java`

## Tests

- `GameFoundationSmokeTest`
- `BasicAiGameplaySmokeTest`

