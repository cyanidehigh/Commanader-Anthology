# 0010 Shared Java Core Boundary
#type/decision #status/accepted #area/shared-core

Links: [[../DDS]], [[../areas/Shared Core]], [[../areas/Platform]], [[../Mobile Companion]], [[../Commander Sim]]

## Decision

Shared Java Core owns Anthology's deterministic domain models, validation
rules, schemas, and service interfaces.

Platform applications provide storage, network, sync-provider, bridge, and UI
implementations.

The shared core must be plain Java and must not depend on desktop-only,
Android-only, Python, UI, Google Drive, SQLite implementation, or live Scryfall
HTTP implementation code.

## Platform Independence

Desktop and Android must be able to run separately while using the same shared
core concepts.

Desktop Anthology:

```text
Desktop UI
Desktop local storage/files
Desktop Scryfall fallback implementation
Desktop sync/export implementation
        |
        v
Shared Java Core
```

Android companion:

```text
Android UI
Android local storage
Android Scryfall fallback implementation if allowed/needed
Android sync/export implementation
        |
        v
Shared Java Core
```

Neither platform requires the other to function.

## Shared Core Owns

- card identity model
- oracle identity vs printing identity
- deck metadata model
- user deck vs locked precon rules
- collection/inventory model
- deck intent and deck assignment model
- Commander legality interfaces and deterministic validation
- deck import/parser model
- sync bundle schema/data objects
- player style data schema
- legal move/event record types
- offline-first repository interfaces

## Shared Core Does Not Own

- desktop UI
- Android UI
- life counter screen logic
- Google Drive implementation
- direct Scryfall HTTP calls
- SQLite implementation details
- Python Sim bridge code
- app settings screens
- rendering/card image loading
- telemetry transport/provider code

## Interface Pattern

Shared core defines interfaces such as:

```text
CardRepository
DeckRepository
CollectionRepository
SyncBundleRepository
PlayerStyleRepository
LegalityService
```

Platform implementations provide concrete storage, network, sync, and file
import/export behavior.

## Consequences

- Business rules stay portable across desktop and Android.
- Desktop can be full-spectrum Anthology without forcing Android to be.
- Android can remain a light companion while speaking the same data language.
- Sync moves compatible Anthology data between platforms when the user wants it.
- Platform-specific providers can be swapped without changing core models.
