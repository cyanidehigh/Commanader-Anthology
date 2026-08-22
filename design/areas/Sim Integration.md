# Sim Integration
#type/area #area/sim-integration #status/active

Links: [[../DDS]], [[../Commander Sim]], [[Shared Core]], [[Card Codex]], [[Data Regression]], [[Player Style]], [[Knowledge Projection]], [[Structured Token Grammar]], [[Validation Strategy]], [[../decisions/0005 Sim Into Anthology First Major Job]], [[../decisions/0016 Legal Move Package Boundary]], [[../decisions/0022 Validation Strategy]]

## Purpose

Sim Integration is the first major Anthology implementation program.

CCBuilder already has enough working deck/collection app shape to guide the
product.
The bigger first job is transforming Commander Sim from a separate project into
the simulation surface of Commander Anthology.

## Goal

Commander Sim should become an Anthology surface that consumes shared platform
truth instead of owning a separate product universe.

It should align with:

- shared Java platform direction
- offline-first data regression
- shared Card Codex/card identity model
- shared deck import and legality rules
- Play and Auto modes only
- legal move package first
- player-style data capture later

The legal move package is engine-owned and general. It is not an AI subsystem.
The AI is only one consumer of structured legal moves.

Validation follows a two-gate rule: legal move generation first, then final
execution validation before state mutation. Normal UI and AI choices should be
legal by construction, but final validation protects against stale, desynced,
replayed, imported, or dev-tool move tokens.

## Integration Principles

- Do not blindly rewrite or delete working Sim behavior.
- Audit the existing Sim code as source material.
- Preserve useful tested mechanics, Card Codex lessons, deck import behavior,
  and launcher/session knowledge.
- Port or bridge only after the shared Anthology boundary is clear.
- Keep all unsupported rules visible. Do not pretend the Sim is full Magic
  until it is.

## Candidate Workstream

1. Inventory existing Commander Sim modules.
2. Identify reusable concepts versus legacy scaffolding.
3. Map Sim Card Codex to shared Anthology Card Codex.
4. Map Sim deck import/legality to shared core.
5. Define and implement the Java legal move package boundary.
6. Define Play and Auto mode contracts.
7. Define player-style event capture from Play mode.
8. Decide what gets ported, bridged, retired, or left as reference.

## Open Questions

- Should the first Sim integration be a Java port, a bridge around the Python
  simulation foundation, or a staged hybrid?
- Which existing Sim tests should be preserved as behavioral reference tests?
- What is the minimum legal move package needed before style capture begins?
