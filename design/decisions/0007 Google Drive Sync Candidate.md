# 0007 Google Drive Sync Candidate
#type/decision #status/draft #area/cross-platform-sync

Links: [[../DDS]], [[../areas/Cross Platform Sync]], [[0006 Serverless Optional Cross Platform Sync]]

## Decision

Google Drive is a candidate for the first user-owned cloud sync target.

This is not a decision to require Google Drive. It is a candidate direction for
optional sync because many Android users already have access to Google Drive,
and it can store portable Anthology sync bundles without Anthology maintaining a
central server.

## Requirements

- Desktop and Android must continue to work without Google Drive.
- Manual import/export must remain possible.
- Google Drive should use the same portable sync bundle format as local file
  sync.
- Generated Scryfall caches and SQLite indexes should not be synced as user
  data.
- Conflicts must be detected and shown rather than silently overwritten.

## Status

Draft candidate. Not yet accepted as the first provider.

