# Costs And Mana
#type/rules #area/commander-sim #status/implemented-slice

Links: [[README]], [[CAPABILITY_MATRIX]], [[comprehensive-rules-inventory]], [[mana-abilities]], [[casting-spells-basic]], [[timing-and-priority]]

## Source

- Rules source: `MagicCompRules 20260807.docx`
- Effective date: August 7, 2026

## Relevant Rule References

- `106` Mana
- `106.1a` Five colors of mana: white, blue, black, red, green
- `106.1b` Six types of mana: white, blue, black, red, green, colorless
- `500` Mana pools empty at the end of each step and phase
- `118` Costs
- `202` Mana cost and color
- `601` Casting spells and paying costs

## Current Anthology Scope

Anthology now has a coherent mana subsystem for fixed printed costs: typed
mana pool, printed cost parser, payment evaluation, and payment execution.
Existing `GameObject` spell costs still enter the game engine as integer
generic values, so current live casting routes those legacy values through
`ManaCost.generic(...)` until game objects carry real printed mana costs.

Implemented mana types:

- white
- blue
- black
- red
- green
- colorless

Fixed parsed cost support:

- numeric generic symbols such as `{2}`
- colored exact symbols `{W}`, `{U}`, `{B}`, `{R}`, `{G}`
- exact colorless `{C}`

The payment engine treats generic and exact colorless correctly: generic costs
can be paid with any mana type, but `{C}` requires colorless mana. Generic
payment currently spends spare colorless first, then colored mana, as a
deterministic default until player/AI payment choices exist.

The temporary basic land mana ability still adds one colorless mana.

## Implemented Behavior

- `ManaType` represents the six mana types.
- `ManaPool` stores separate counts for each mana type.
- `Player.manaPoolDetails` exposes the real pool.
- `Player.manaPool` remains as a total compatibility query for current UI, AI,
  and generic-cost rules.
- `GameFoundation.addMana(playerId, ManaType, amount)` can add typed mana.
- Existing `GameFoundation.addMana(playerId, amount)` still adds colorless mana
  for the current generic slice.
- `ManaCost.parse` represents fixed printed costs and records unsupported
  choice symbols explicitly.
- `ManaPaymentEngine` checks and pays fixed generic, colored, and colorless
  costs.
- Generic payment spends from the real pool, preferring spare colorless before
  colored mana.
- Current live spell casting routes legacy integer costs through
  `ManaCost.generic(...)`.
- The pool empties when advancing steps/phases through the existing turn
  advancement path.

## Unsupported / Not Yet Proved

- Live `GameObject` printed mana-cost identity. Current game objects still store
  only integer generic costs.
- Hybrid, Phyrexian, snow, X, variable, alternate, or additional costs. These
  symbols are represented by `ManaCost`, but `ManaPaymentEngine` rejects them as
  unsupported instead of silently treating them as normal costs.
- Cost increases/reductions.
- Commander tax payment.
- Mana restrictions such as "spend this mana only to cast..."
- Mana source tracking.
- Mana that does not empty.
- Floating mana warnings or UI detail.
- Mana abilities during the casting/payment procedure.

## Code

- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/ManaType.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/ManaPool.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/ManaCost.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/ManaPaymentEngine.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/ManaPaymentResult.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/Player.java`
- `anthology-core/src/main/java/com/commanderanthology/core/commandersim/GameFoundation.java`

## Tests

- `ManaPoolSmokeTest`
- `ManaCostPaymentSmokeTest`
