# 0005 Sim Into Anthology First Major Job
#type/decision #status/accepted #area/sim-integration

Links: [[../DDS]], [[../areas/Sim Integration]], [[../Commander Sim]], [[../CCBuilder]]

## Decision

The first major Anthology implementation job is transforming Commander Sim into
the Anthology fold.

CCBuilder already has a working deck/collection app spine. Commander Sim is
the larger integration task because it must stop being a separate product and
become the desktop simulation surface of Commander Anthology.

## Scope

This does not mean blindly merging codebases.

It means:

- auditing Commander Sim
- preserving useful tested behavior and lessons
- aligning Sim with shared Java core decisions
- moving toward shared Card Codex, legality, deck import, and data regression
- simplifying Sim to Play and Auto modes
- focusing first on legal move generation
- deferring player-style capture until legal move/event records are stable

## Consequences

- Sim integration becomes a top-level kanban program.
- Shared-core boundaries should be designed with Sim needs in mind, not only
  CCBuilder needs.
- Existing Sim data and tests are reference material, not disposable clutter.
- The project should avoid large UI polish work until the Sim integration path
  is clear.
