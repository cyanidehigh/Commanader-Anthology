# Timing And Priority
#type/rules #area/commander-sim #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[comprehensive-rules-inventory]], [[priority]], [[turn-structure]], [[casting-spells-basic]], [[playing-lands]], [[mana-abilities]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Effective date: August 7, 2026

## Relevant Rule References

- `116` Special Actions
- `117` Timing and Priority
- `500-505` Turn structure through main phase
- `601` Casting Spells
- `602` Activating Activated Abilities
- `605` Mana Abilities

## Current Anthology Scope

Anthology currently implements a small timing shell:

- no one has priority during initial untap setup
- upkeep opens a priority window for the active player
- draw step performs the current draw action, then opens priority
- the current "sorcery window" means:
  - active player
  - precombat or postcombat main phase
  - stack is empty
- land play, sorcery spells, and creature spells require that sorcery window
- instant spells may be cast by the priority player outside that sorcery window
  if generic mana is available
- once the stack is non-empty, the sorcery window closes until the stack clears
- the current basic generated land mana ability remains legal only for the
  priority player in the supported priority-window slice

## Implemented Behavior

- `GameFoundation.isMainPhaseSorceryWindow` exposes the engine timing query.
- `canPlayLand` and non-instant `canCastSpell` use the explicit timing query.
- `TimingAndPrioritySmokeTest` proves:
  - no legal moves without priority
  - upkeep is not a sorcery window
  - instants can be legal during upkeep priority
  - lands, creatures, and sorceries are not legal during upkeep
  - priority passes only to the current priority player
  - draw step opens priority after the draw action
  - active player's main phase with empty stack is a sorcery window
  - non-empty stack closes the sorcery window
  - instants remain legal while the stack is non-empty

## Unsupported / Not Yet Proved

- Full special-action set beyond land play.
- Casting permissions changed by card text, such as flash.
- Mana abilities during casting/payment.
- Full active-player/nonactive-player ordering beyond the current two-player
  slice.
- Triggered abilities entering priority windows.
- Cleanup-step priority exceptions.
- Shortcuts and tournament communication shortcuts.
- Full combat-step priority sequence.

## Code

- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameFoundation.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameRules.java`

## Tests

- `TimingAndPrioritySmokeTest`

