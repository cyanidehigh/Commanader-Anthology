# Forge Reference
#type/source #status/reference #area/sim-integration #area/ai-architecture #area/validation

Links: [[../DDS]], [[../areas/Sim Integration]], [[../areas/AI Architecture]], [[../areas/AI Move Scoring]], [[../areas/Structured Token Grammar]], [[../areas/Validation Strategy]], [[../decisions/0023 Forge Comparative Reference Policy]]

Source: https://github.com/Card-Forge/forge

## Why It Matters

Forge is a mature Java Magic rules-engine project with separate modules for
core card data, game logic, AI, desktop UI, Android UI, and mobile UI.

It is useful as an architectural and conceptual reference for Commander
Anthology because Anthology is also aiming for Java, desktop, Android, rules
simulation, AI move choice, and offline-friendly card data.

## Useful Reference Areas

- `forge-core`: paper cards, static data, storage helpers, precon deck concepts
- `forge-game`: game state, zones, phases, stack, costs, targets, spell
  abilities, replacement effects, triggers, combat, player controllers
- `forge-ai`: AI controllers, spell/ability picking, combat evaluation, mana
  and cost heuristics, target choice helpers
- `forge-gui/res/cardsfolder`: card scripting examples
- `docs/Card-scripting-API`: ability scripting grammar, costs, targets,
  restrictions, conditions, and AI hints
- desktop and Android GUI modules: cross-platform Java project organization

## DDS Relevance

Forge reinforces several accepted Anthology decisions:

- keep rules/game truth separate from UI surfaces
- keep AI as a consumer of legal game actions
- represent costs, targets, restrictions, and modes explicitly
- use inspectable AI heuristics before more advanced model behavior
- build validation around game state, costs, targets, and timing
- keep desktop and Android surfaces over shared game/core logic

## License Caution

Forge is GPL-3.0 licensed.

Anthology can study Forge's public architecture and concepts, but copying code
or derived implementation details would require a deliberate licensing decision.
Until that decision exists, Forge should be treated as reference material, not a
dependency or source-code donor.

Accepted policy: [[../decisions/0023 Forge Comparative Reference Policy]].

## Initial Takeaway

Forge can help most with the hard Sim questions:

- Legal Move Package design
- Structured Token Grammar vocabulary
- AI move scoring categories
- cost and target validation
- combat evaluation
- card scripting/import boundaries
- regression fixture ideas
