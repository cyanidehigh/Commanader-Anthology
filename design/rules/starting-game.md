# Starting Game
#type/rules #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[comprehensive-rules-inventory]], [[turn-structure]]

## Source

- Source document: root `MagicCompRules 20260807.docx`
- Effective date: August 7, 2026

## Relevant Rule References

- `103` Starting the Game
- `103.2c` Commander setup puts each commander into the command zone
- `103.3` players shuffle decks and libraries are established
- `103.4c` Commander starting life total is 40
- `103.5` players draw opening hands and may mulligan
- `903` Commander
- `408` Command zone

## Plain-English Summary

For Anthology's current Commander-only line, a game starts with Commander life
totals, commanders in the command zone, randomized libraries, and opening
hands.

## Implemented Slice

- `GameFoundation.buildGame` defaults players to 40 life.
- Game startup has command, battlefield, stack, and exile as shared zones.
- `GameFoundation.addCommander` creates a commander-marked game object directly
  in the shared command zone.
- Desktop setup loads commander slots into the command zone and other deck
  cards into the library.
- `GameFoundation.prepareOpeningHands` shuffles each player's library and draws
  up to the requested opening hand size.
- Commander objects stay in the command zone while libraries shuffle and
  opening hands are drawn.
- `GameFoundationSmokeTest` covers library shuffle and opening-hand movement.
- `StartingGameSmokeTest` covers Commander starting life, commander-marked
  objects in the command zone, and initial no-priority untap setup.

## Explicit Unsupported Cases

- Mulligans are not implemented.
- Pre-game player choice order is not implemented.
- Companion, sideboard, stickers, conspiracy, attraction, planechase, and other
  supplementary setup objects are not implemented.
- Commander replacement behavior and commander tax are not implemented in this
  note.
- This note does not validate deck construction or commander legality; those
  are handled by Deck Builder/Card Codex boundaries.

## Code

- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameFoundation.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameObject.java`
- `anthology-desktop/src/main/java/com/commanderanthology/desktop/CommanderSimPanel.java`

## Tests

- `anthology-core/src/test/java/com/commanderanthology/core/commandersim/GameFoundationSmokeTest.java`
- `anthology-core/src/test/java/com/commanderanthology/core/commandersim/StartingGameSmokeTest.java`
