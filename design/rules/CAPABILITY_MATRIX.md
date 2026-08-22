# Rules Capability Matrix
#type/rules #type/matrix #status/live

Links: [[README]], [[comprehensive-rules-inventory]], [[../Release Notes]]

This matrix is the rules implementation control surface. A row marked
`Implemented slice` does not mean the full Comprehensive Rules area is complete.
It means the specific scoped Anthology behavior in the linked note is
implemented and tested.

| Capability | Rule Note | Status | Code | Tests | Current Scope |
|---|---|---|---|---|---|
| Turn structure | [[turn-structure]] | Partial | `GameFoundation.advanceStep` | `GameFoundationSmokeTest`, `BasicAiGameplaySmokeTest` | Linear step sequence, automatic untap/draw behavior started |
| Priority passing | [[priority]] | Implemented slice | `GameFoundation.passPriority`, `GameRules.execute` | `GameFoundationSmokeTest` | Priority window pass/close/stack-resolve shell |
| Timing restrictions | [[timing-and-priority]] | Implemented slice | `GameFoundation.isMainPhaseSorceryWindow`, `GameRules.legalMoves` | `TimingAndPrioritySmokeTest` | Current sorcery-window slice: active main phase, empty stack; instants can be legal outside it for the priority player |
| Draw step | [[draw-step]] | Implemented slice | `GameFoundation.performDrawStep`, `drawCard`, `drawCards` | `GameFoundationSmokeTest`, `BasicAiGameplaySmokeTest` | Active player draws if library is non-empty |
| Starting game setup | [[starting-game]] | Implemented slice | `GameFoundation.addCommander`, `GameFoundation.prepareOpeningHands`, desktop deck loader | `StartingGameSmokeTest`, `GameFoundationSmokeTest` | Commander life total default, commander-marked objects in command zone, libraries shuffled, opening hands drawn; mulligans not implemented |
| Library shuffle | [[starting-game]], [[turn-structure]] | Implemented slice | `GameFoundation.shuffleLibrary`, `Zone.shuffle` | `GameFoundationSmokeTest` | Core library shuffle helper |
| Playing lands | [[playing-lands]] | Implemented slice | `GameFoundation.playLand`, `GameRules.legalMoves` | `GameFoundationSmokeTest`, `BasicAiGameplaySmokeTest` | One land per turn during current sorcery-window slice |
| Mana abilities | [[mana-abilities]] | Partial | `GameFoundation.activateManaAbility`, `GameRules.legalMoves` | `BasicAiGameplaySmokeTest` | Temporary generic one-mana ability for untapped land permanents |
| Costs and mana pool | [[costs-and-mana]] | Implemented slice | `ManaPool`, `ManaType`, `ManaCost`, `ManaPaymentEngine`, `Player.manaPoolDetails`, `GameFoundation.addMana` | `ManaPoolSmokeTest`, `ManaCostPaymentSmokeTest` | Real WUBRG/colorless pool, fixed printed-cost parsing/payment for generic, colored, and `{C}` symbols, generic compatibility path, and pool emptying on step/phase advancement |
| Keyword abilities | [[keyword-abilities]] | Researched | `RealCardFixtureLoader` | `RealCardFixtureSmokeTest` | Proposed model only; first real-card fixture slice can represent keyword-bearing real cards, but no keyword gameplay behavior exists yet |
| Casting spells | [[casting-spells-basic]] | Partial | `GameFoundation.castSpell`, `GameRules.legalMoves` | `GameFoundationSmokeTest`, `BasicAiGameplaySmokeTest` | Simple creature/instant/sorcery movement and stack shell |
| Stack resolution | [[casting-spells-basic]], [[priority]] | Partial | `GameFoundation.resolveTopOfStack` | `GameFoundationSmokeTest` | Top object resolves after all eligible players pass |
| Basic AI move choice | [[ai-legal-move-boundary]] | Implemented slice | `BasicAiPlayer`, `BasicAiGameDriver` | `BasicAiGameplaySmokeTest` | AI scores and executes generated legal moves only |
| Combat | TBD | Not started | TBD | TBD | Not implemented |
| Targeting | TBD | Not started | TBD | TBD | Not implemented |
| Commander tax/payment | TBD | Not started | TBD | TBD | Not implemented |
| Triggered abilities | TBD | Not started | TBD | TBD | Not implemented |
| Replacement effects | TBD | Not started | TBD | TBD | Not implemented |
| State-based actions minimum | [[state-based-actions]] | Implemented slice | `GameFoundation.checkStateBasedActions`, `GameFoundation.changeLife`, `GameRules.legalMoves` | `StateBasedActionsSmokeTest` | `704.5a` only: a player at 0 or less life loses; game-over state stops legal moves |
| State-based actions depth | [[state-based-actions]] | Not started | TBD | TBD | Empty-library draw loss, poison, lethal damage, token/copy cleanup, legend rule, commander zone SBA, and repeated SBA loops are not implemented |
| Layers/continuous effects | TBD | Not started | TBD | TBD | Not implemented |
| Color mana/payment | [[costs-and-mana]] | Partial | `ManaCost`, `ManaPaymentEngine` | `ManaCostPaymentSmokeTest` | Fixed colored and exact colorless costs are parsed and payable; hybrid, Phyrexian, snow, X, variable, alternate, additional, cost-modified, and restriction-bearing payments remain unsupported |

## Inventory Gate

Use [[comprehensive-rules-inventory]] before choosing a new rule slice. The
inventory tracks the full Comprehensive Rules section map so the matrix does
not accidentally hide large unimplemented areas behind a small implemented
slice.

## Status Terms

- `Not started`: no rule research or implementation.
- `Researched`: rules note exists but code is not implemented.
- `Partial`: code exists for a narrow subset; large unsupported cases remain.
- `Implemented slice`: scoped behavior in the note is implemented and tested.
- `Unsupported`: Anthology deliberately rejects or does not expose this behavior.
- `Blocked`: cannot safely proceed without source/decision/tooling.
