# Dev Mode
#type/area #area/dev-mode #status/draft

Links: [[../DDS]], [[Deck Catalog]], [[Card Codex]], [[../decisions/0018 Precon Source Policy]]

## Purpose

Dev Mode is the maintenance surface for project-owner/developer actions that
should not appear as normal user workflows.

## Initial Responsibilities

- bundled precon import/update
- precon source metadata maintenance
- precon validation
- Card Codex/cache maintenance controls where appropriate
- diagnostics and migration checks

## Precon Maintenance

Precon creation/update belongs in Dev Mode.

Normal users should see locked bundled precons and may copy them into editable
user decks, but they should not accidentally mutate the bundled precon catalog.

