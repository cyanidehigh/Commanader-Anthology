# Version Control
#type/area #area/version-control #status/draft

Links: [[../DDS]], [[../KANBAN]], [[../decisions/0003 Version Control Policy]]

## Purpose

Version control must protect the Anthology transition while keeping the design
system traceable in default Obsidian.

It should track:

- source code
- handoff and README docs
- `design/` decision notes
- small deterministic fixtures
- schema definitions
- migration scripts

It should not track:

- generated build output
- downloaded Scryfall bulk JSON
- local SQLite caches
- app runtime data
- user private collection saves
- sessions, logs, and crash dumps unless explicitly curated as test fixtures

## Current Workspace Finding

There is a `.git` directory at the workspace root, but `git status` currently
reports:

```text
fatal: not a git repository (or any of the parent directories): .git
```

This means the repository state needs confirmation before relying on normal Git
workflows. Do not run destructive Git repair commands without explicit
instruction.

## Desired Policy

- One repository should own the combined Commander Anthology workspace.
- Root docs are canonical: [[../../README|README]] and [[../../HANDOFF|HANDOFF]].
- The design system lives under `design/` and is versioned like source code.
- Legacy apps/foundations stay in-tree until a deliberate migration/retirement
  decision says otherwise.
- Large generated data must be ignored or stored outside Git.
- Root `.gitignore` owns the combined ignore policy going forward. Project-local
  legacy `.gitignore` files may remain until folder migration is settled.
- Every major architectural change should update the relevant design note and,
  when appropriate, add a decision note.

## Version Numbering

Commander Anthology uses classic three-part versioning:

```text
V(release version).(major patch).(minor patch)
```

- **Release version** increments for a full release version or complete product
  generation.
- **Major patch** increments for a major change, compatibility-affecting update,
  or show-stopper bug fix.
- **Minor patch** increments for a minor change, compatible improvement, or
  ordinary bug fix.
- During an in-progress major workstream, each landed implementation slice
  should increment the minor patch number. Example: `V0.5.0` starts the
  rules-foundation line, `V0.5.1` lands the next verified rules slice, and
  `V0.5.2` lands the slice after that.

Examples:

- `V1.0.0`: first release version.
- `V1.1.0`: major update or show-stopper fix within release 1.
- `V1.1.1`: minor update or ordinary bug fix after `V1.1.0`.

Every distributed build should identify the application version and, once those
schemas exist, the engine, data, and move/style schema versions it expects.

## Open Questions

- Should the current invalid `.git` folder be repaired, replaced, or ignored?
- Should legacy folders keep their own historical Git metadata if any exists?
