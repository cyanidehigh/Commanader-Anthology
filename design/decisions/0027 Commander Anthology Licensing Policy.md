# 0027 Commander Anthology Licensing Policy
#type/decision #status/accepted #area/licensing #area/platform #area/distribution

Links: [[../DDS]], [[../KANBAN]], [[0023 Forge Comparative Reference Policy]], [[../areas/Version Control]]

## Decision

Commander Anthology code is licensed under the GNU General Public License
version 3.0.

The root `LICENSE` file carries the canonical GPLv3 text. The root `NOTICE.md`
records the project-specific third-party IP boundary.

## Why

Commander Anthology is intended to stay free for Magic players and to remain
open when redistributed. GPLv3 matches that goal while fitting the current
desktop-first project shape.

AGPLv3 was discussed as a possible future-server-protective option, but the
accepted choice is GPLv3.

## Scope

GPLv3 applies to Commander Anthology's original source code and project files
unless a file states a different license.

GPLv3 does not apply to third-party material that Anthology references, caches,
imports, displays, or documents.

Third-party material includes:

- Magic: The Gathering card names
- mana symbols
- Oracle text
- card images
- Comprehensive Rules text
- Scryfall card data and image URLs
- user-owned decklists and collection data

## Distribution Boundary

Bundled or cached third-party card data must remain clearly marked as
third-party reference material. The project license must not claim ownership of
that material.

If Anthology later adds a maintained server or hosted sync service, revisit
licensing and service terms before launch. GPLv3 remains the accepted license
unless a later decision changes it.
