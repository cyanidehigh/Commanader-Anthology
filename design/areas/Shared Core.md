# Shared Core
#type/area #area/shared-core #status/active

Links: [[../DDS]], [[Platform]], [[Card Codex]], [[Data Regression]], [[Player Style]], [[../decisions/0010 Shared Java Core Boundary]]

The shared Java core owns deterministic business rules, schemas, service
interfaces, and data models used by desktop Anthology, Android companion, and
Commander Sim.

It must be plain Java and must not depend on desktop-only, Android-only,
Python, UI, Google Drive, SQLite implementation, or live Scryfall HTTP
implementation code.

Initial candidates:

- card identity
- Scryfall/Card Codex access
- Commander legality
- deck import and parsing
- collection inventory
- deck intent and assignment
- deck context
- player style profile model

Shared core should prefer local/offline data paths first and use remote APIs
only as explicit fallback or refresh mechanisms. See [[Data Regression]].

## Platform Independence

Desktop and Android both use the shared core, but neither platform requires the
other to run.

Platform apps provide concrete implementations for storage, network fallback,
sync providers, file import/export, and UI.
