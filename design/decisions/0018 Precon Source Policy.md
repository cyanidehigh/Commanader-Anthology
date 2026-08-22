# 0018 Precon Source Policy
#type/decision #status/accepted #area/deck-catalog

Links: [[../DDS]], [[../areas/Deck Catalog]], [[0009 Deck Data And Precon Catalog]]

## Decision

Precon decklists may be provided manually from common public sources.

The project owner is happy to provide precon lists from a source. These lists
are common knowledge across many public decklist/reference locations. Moxfield
export is a likely practical source format for importing the lists.

Anthology should treat Moxfield or any other public decklist source as
source/import material, not as a runtime dependency.

## Source Direction

Preferred source flow:

1. User/project owner obtains a precon decklist from a public source.
2. Moxfield export may be used as the practical import format.
3. Anthology Dev Mode imports/parses the list.
4. Anthology Dev Mode resolves cards through the local/offline-first Card Codex.
5. Anthology Dev Mode validates Commander legality and deck counts.
6. Anthology Dev Mode stores or updates a local bundled `precon` record with
   source metadata.
7. Normal app use reads the local bundled copy.

## Source Metadata

Bundled precon records should preserve source metadata where available:

- precon name
- commander(s)
- product/set/release info
- source name
- source URL or citation
- source publisher/domain
- source export format
- import date
- validation status
- Anthology schema version

## Requirements

- Anthology must not depend on Moxfield at runtime.
- Precon creation/update belongs in Dev Mode, not normal user mode.
- Precon decklists must be stored locally once imported and validated.
- Precons remain locked bundled reference decks.
- Users may copy precons into editable user decks.
- Source differences or updates must not overwrite user copies.

## Consequences

- Precon ingestion can be practical and low-friction.
- Dev Mode becomes the maintenance surface for bundled precon updates.
- The app remains offline-first.
- Public decklist sources can change without breaking normal app use.
- Moxfield is a likely source/export path, not a platform dependency.
