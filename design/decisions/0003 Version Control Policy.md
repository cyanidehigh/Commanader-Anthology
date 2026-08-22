# 0003 Version Control Policy
#type/decision #status/accepted #area/version-control

Links: [[../DDS]], [[../KANBAN]], [[../areas/Version Control]]

## Decision

Commander Anthology should use one root version-control repository for the
combined workspace.

The repository should track source code, design docs, schemas, migrations, and
small deterministic fixtures. It should not track generated build output,
downloaded Scryfall bulk data, local SQLite caches, private user saves, runtime
sessions, or logs.

A root `.gitignore` defines the combined ignore policy for the Anthology
workspace.

Commander Anthology uses classic three-part versioning:

```text
V(release version).(major patch).(minor patch)
```

- Release version: full release version or complete product generation.
- Major patch: major change, compatibility-affecting update, or show-stopper bug fix.
- Minor patch: minor change, compatible improvement, or ordinary bug fix.
- During an in-progress major workstream, every landed implementation slice
  increments the minor patch number so the build/release notes can point at an
  exact verified state.

## Current Finding

A `.git` directory exists at the workspace root, but Git does not currently
recognize it as a valid repository. This needs explicit repair or replacement
before normal Git workflows can be trusted.

## Status

Accepted for policy. Repository repair/reinitialization has not been performed.
