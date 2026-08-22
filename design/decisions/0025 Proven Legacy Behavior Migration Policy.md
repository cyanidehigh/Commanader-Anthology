# 0025 Proven Legacy Behavior Migration Policy
#type/decision #status/accepted #area/migration #area/ccbuilder #area/sim-integration

Links: [[../DDS]], [[../areas/Migration]], [[../CCBuilder]], [[../Commander Sim]], [[0017 Legacy Folder Merge Policy]], [[0005 Sim Into Anthology First Major Job]]

## Decision

Commander Anthology should use as much proven legacy behavior as practical.

The goal is not to convert two pieces of software and then ignore their working
parts. The goal is to absorb useful behavior into one unified Anthology app.

## CCBuilder

CCBuilder is the primary source for Builder behavior.

Because the legacy CCBuilder app is already JVM/Kotlin/Compose, its deck,
collection, assignment, import, persistence, Scryfall lookup/cache, and SQLite
bulk-cache behavior should be ported aggressively into the new Java Anthology
structure.

## Commander Sim

Commander Sim is the primary source for simulation behavior and test knowledge.

Because it is Python, it should not be copied line-for-line into Java. Its state
model, phase flow, priority handling, combat behavior, Card Codex lessons, deck
import behavior, tests, fixtures, and known limitations should be preserved as
migration references and ported deliberately.

## Rule

Prefer porting proven legacy behavior over rewriting from scratch.

Rewrite only when:

- the existing behavior conflicts with accepted Anthology boundaries
- the existing behavior depends on the wrong platform layer
- the existing behavior is known to be broken or incomplete
- the shared Java source of truth requires a different model

Once behavior is ported, verified, and documented in Anthology, Anthology
becomes the new source of truth for that behavior.

