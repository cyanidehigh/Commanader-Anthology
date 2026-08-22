# AI Legal Move Boundary
#type/rules #area/commander-sim #area/ai #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[../DDS]]

## Source

- Product design source: [[../DDS]]
- Rules source: `MagicCompRules 20260807.docx`

## Current Anthology Scope

AI does not create legality. AI consumes the same generated legal moves a human
seat would receive and returns a selected move.

## Implemented Behavior

- `BasicAiPlayer` scores legal moves.
- `BasicAiGameDriver` advances the game when no priority window is open.
- When priority is open, the driver asks `GameRules.legalMoves` for the priority
  player and executes only the selected legal move through `GameRules.execute`.
- Selected moves still cross final execution validation.

## Unsupported / Not Yet Proved

- Strategic Commander AI.
- Hidden-information projection.
- Combat heuristics.
- Targeting choices.
- Player-style input to AI. This remains explicitly forbidden by the accepted
  player-style boundary.

## Code

- `anthology-core/src/main/java/com/commanderanthology/core/ai/BasicAiPlayer.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/BasicAiGameDriver.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameRules.java`

## Tests

- `BasicAiGameplaySmokeTest`

