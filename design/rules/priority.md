# Priority
#type/rules #area/commander-sim #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[turn-structure]], [[casting-spells-basic]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Exact rule references: TODO, must be extracted from the source document before
  expanding this slice.

## Current Anthology Scope

Priority is modeled as a single priority-player pointer plus a consecutive pass
counter.

## Implemented Behavior

- Only the priority player may pass priority.
- Passing moves priority to the next eligible player.
- If all eligible players pass with an empty stack, the priority window closes.
- If all eligible players pass with a non-empty stack, the top stack object
  resolves and priority reopens with the active player.
- `No response` on the playmat means passing priority for the current priority
  window.

## Unsupported / Not Yet Proved

- Full active-player/nonactive-player ordering beyond the current two-player
  slice.
- Priority around triggered abilities.
- Special actions.
- Mana abilities outside the current basic generated move.
- Shortcut handling.
- Tournament communication shortcuts.

## Code

- `GameFoundation.passPriority`
- `GameFoundation.openPriority`
- `GameRules.legalMoves`
- `GameRules.execute`

## Tests

- `GameFoundationSmokeTest`

