# 0006 Serverless Optional Cross Platform Sync
#type/decision #status/accepted #area/cross-platform-sync

Links: [[../DDS]], [[../areas/Cross Platform Sync]], [[../CCBuilder]], [[../Commander Sim]]

## Decision

Commander Anthology should initially link desktop and Android without requiring
expensive server upkeep.

The app should be free or as close to free as practical for users. MTG players
already deal with high card costs, and the product should not require a paid
subscription or maintained central server to be useful for core workflows.

## Platform Independence

Each platform must work separately:

- Desktop Anthology works by itself.
- Android CCBuilder works by itself.
- Linking/sync is optional enhancement, not a requirement.

## Preferred Sync Direction

Use serverless, user-owned, local-first data transfer:

- portable sync bundles
- import/export
- local files
- user-owned cloud folders
- future provider integrations only when they do not become required

## Consequences

- No required central account system for core use.
- No maintained app server as an initial/core hard dependency.
- Sync data must be versioned, validated, and conflict-aware.
- Generated reference caches should not be treated as user sync data.
- Product features should degrade gracefully when only one platform is present.

## Future Server Caveat

A maintained server may be considered later if user-owned sync is not enough for
collaboration, telemetry aggregation, or smoother multi-device use.

That would require a separate accepted decision covering cost, privacy,
sustainability, and whether server-backed features remain optional.
