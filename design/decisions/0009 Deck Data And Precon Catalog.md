# 0009 Deck Data And Precon Catalog
#type/decision #status/accepted #area/deck-catalog

Links: [[../DDS]], [[../areas/Deck Catalog]], [[../Commander Sim]], [[../CCBuilder]]

## Decision

Current deck/input data in both legacy Sim and Builder is user data.

Anthology should also ship with Commander preconstructed decks by default for
the game/simulation side. These bundled precons should provide enough starting
deck data to begin analysis and play testing immediately.

## Requirements

- Existing user-entered deck/data must be preserved during migration.
- Bundled precon decks must be marked as `precon`.
- Bundled precon decks must be locked against accidental deletion.
- Users must be able to duplicate/copy a precon into an editable user deck.
- User decks must be clearly distinguishable from bundled precons because users
  may input many decks themselves.
- Precon deck data should support early simulation and analysis, but it must not
  be mistaken for the user's own deck/style data.
- Online precon decklists may be used as source/import material, but Anthology
  should store validated local bundled copies so normal use does not depend on
  live web access.
- Official Wizards decklist pages are preferred sources when available.

## Consequences

- Deck metadata needs an origin/source field.
- The UI needs visible precon labeling.
- Delete/edit behavior must respect locked bundled decks.
- Migration logic must preserve current Sim and Builder input data as
  user-owned data.
- Precon import tooling should retain source metadata such as source URL,
  source publisher/domain, source page title, product/set info, import date,
  validation status, and schema version.
