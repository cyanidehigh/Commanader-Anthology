# Cross Platform Sync
#type/area #area/cross-platform-sync #status/draft

Links: [[../DDS]], [[Shared Core]], [[Data Regression]], [[Player Style]], [[../decisions/0006 Serverless Optional Cross Platform Sync]]

## Purpose

Cross-platform sync/linking is how the desktop Anthology app and Android
CCBuilder app share user data without requiring expensive maintained server
infrastructure.

The product must remain useful and affordable. Magic players already carry high
card costs, so the app should not depend on a subscription or paid server just
to be valuable.

## Core Rule

Each platform must work separately.

- Desktop Anthology must work if the user never installs Android CCBuilder.
- Android CCBuilder must work if the user never installs desktop Anthology.
- Linking/sync improves the experience but must not be required for core use.

## Direction

Prefer serverless and user-owned sync:

- local files
- import/export bundles
- user-owned cloud folders
- Google Drive as a likely first cloud-sync candidate
- OneDrive / Dropbox / Syncthing-style folders later
- QR/manual transfer for small pairing or export flows where useful

Avoid:

- maintained app server as a required dependency for initial/core use
- subscription-gated core sync
- central account system as the only way to move data
- silent data overwrite between platforms

## Sync Bundle Schema

Accepted direction: see [[../decisions/0014 Sync Bundle Schema]].

The shared format is a portable sync bundle containing versioned, validated
user-owned data such as:

- user decks
- copied precons
- imported decks
- collection inventory
- containers
- deck assignments
- lightweight player style summaries
- portable settings
- app/schema metadata

Large generated caches, such as Scryfall bulk data or SQLite indexes, should
usually be rebuilt locally rather than synced as user data.

## Requirements

- Sync must be optional.
- Sync must be local-first and offline-safe.
- Each platform must retain its own usable local data.
- Data exports must be versioned and validated on import.
- Conflicts must be visible, not silently overwritten.
- The app should distinguish user data from regenerated reference caches.

## Future Server Option

Anthology may eventually need a server for richer collaboration, easier
multi-device sync, telemetry aggregation, or features that user-owned file sync
cannot support well.

The current policy is not "never server." It is:

- no required maintained server for initial/core use
- no server dependency for standalone desktop or Android use
- provider-neutral bundle format first
- server, if added later, should consume the same sync bundle/domain model where
  practical
- server-backed features must be a deliberate later decision with cost, privacy,
  and sustainability reviewed

## Open Questions

- How should conflicts be shown to nontechnical users?
- Should Google Drive be the first user-owned cloud sync target?
- Should other user-owned cloud locations follow the same sync-bundle contract?

## Google Drive Candidate

Google Drive is a plausible first sync provider because many Android users
already have it, it can store user-owned files, and it avoids Anthology needing
to maintain its own server for ordinary sync.

Important constraints:

- Google Drive sync must be optional.
- Users must be able to use desktop-only or Android-only without Google Drive.
- The app should still support manual import/export of the same sync bundle.
- Google Drive should store versioned Anthology user data, not generated
  Scryfall caches.
- Provider-specific code should sit outside the shared data model so another
  provider can use the same bundle format later.
