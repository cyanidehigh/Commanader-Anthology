# State-Based Actions
#type/rules #area/commander-sim #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[comprehensive-rules-inventory]], [[priority]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Effective date: August 7, 2026

## Relevant Rule References

- `704.1` State-based actions happen automatically when their listed
  conditions are met.
- `704.2` State-based actions are checked throughout the game and are not
  controlled by any player.
- `704.3` State-based actions are checked whenever a player would get priority.
- `704.5` Lists state-based actions.
- `704.5a` A player with 0 or less life loses the game.

## Current Anthology Scope

Anthology currently implements only the minimum game-ending state-based action:

- if a player has 0 or less life, that player loses
- in the current 1v1 beta scope, once one player has lost, the game is treated
  as over
- game-over state clears priority
- lost players and game-over states produce no further legal moves

This is deliberately tiny. It exists so later damage, combat, and card-effect
slices have a real game-ending rule to land on.

## Implemented Behavior

- `GameFoundation.changeLife` adjusts a player's life total and checks
  state-based actions.
- `GameFoundation.checkStateBasedActions` marks players with 0 or less life as
  lost.
- `GameFoundation.gameOver` reports true when one or fewer players remain.
- `GameRules.legalMoves` returns no legal moves for lost players or once the
  game is over.
- Final move execution runs state-based actions after the move mutates game
  state.

## Unsupported / Not Yet Proved

- Empty-library draw loss from `704.5b`.
- Poison counter loss from `704.5c`.
- Token/copy cleanup.
- Creature lethal damage, deathtouch damage, and 0-or-less toughness checks.
- Planeswalker, battle, Role, Aura, Equipment, Fortification, legend rule, and
  other permanent/object state-based actions.
- Commander-specific graveyard/exile command-zone state-based action from
  `704.6d`.
- Full repeated state-based action loops until no actions apply.
- Trigger handling after state-based actions.
- Multiplayer winner determination beyond the current 1v1 game-over shell.

## Code

- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameFoundation.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameRules.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/Player.java`

## Tests

- `StateBasedActionsSmokeTest`

