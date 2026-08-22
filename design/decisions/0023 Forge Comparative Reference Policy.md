# 0023 Forge Comparative Reference Policy
#type/decision #status/accepted #area/sim-integration #area/ai-architecture #area/licensing

Links: [[../DDS]], [[../sources/Forge Reference]], [[../areas/Sim Integration]], [[../areas/AI Architecture]], [[0022 Validation Strategy]], [[0021 Structured Token Grammar Boundary]], [[0020 AI Decision Contract]]

## Decision

Forge is a comparative reference only.

Commander Anthology will not become a Forge clone, will not copy Forge code, and
will not inherit Forge architecture wholesale.

Forge may be studied to understand hard Magic-engine problems, but Anthology's
solution must remain its own product and architecture.

## Why

Commander Anthology exists because existing tools, including Forge, do not solve
the desired product problem:

- collection-aware Commander deck building
- player-style learning from real Play-mode decisions
- Builder recommendations based on actual collection and actual play behavior
- Commander-first product design
- desktop full-spectrum program plus light Android companion
- offline-first data
- explicit legal move tokens
- two-gate validation strategy

Forge can reveal useful lessons, but it does not define Anthology's identity.

## Allowed Use

Allowed:

- compare module boundaries
- study public architecture patterns
- study docs and card-scripting concepts
- identify categories of rules complexity
- identify validation and testing ideas
- use it as a sanity check for Sim integration questions

Not allowed without a later explicit licensing and architecture decision:

- copy Forge source code
- port Forge implementation details directly
- make Anthology depend on Forge libraries
- clone Forge UI/product flow
- treat Forge's architecture as Anthology's required shape

## License Note

Forge is GPL-3.0 licensed.

Until Commander Anthology has an explicit licensing policy, Forge remains
reference material only.

