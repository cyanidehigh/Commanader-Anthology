# Comprehensive Rules Inventory
#type/rules #type/inventory #status/live

Links: [[README]], [[CAPABILITY_MATRIX]], [[turn-structure]], [[priority]], [[ai-legal-move-boundary]]

## Source

- Source document: root `MagicCompRules 20260807.docx`
- Effective date: August 7, 2026
- Purpose: provide the rules map Anthology uses before choosing
  implementation slices.

This inventory is not an implementation claim. It is the checklist that keeps
V0.5.0 and later rules work tied to the actual Comprehensive Rules structure.

## Top-Level Structure

| Section | Title | Anthology Relevance | Current Treatment |
|---|---|---|---|
| 1 | Game Concepts | Core engine truth | Slice by slice |
| 2 | Parts of a Card | Card Codex / rules identity | Needed before real Oracle semantics |
| 3 | Card Types | Legal move generation and resolution | Needed early |
| 4 | Zones | Core game state | Started |
| 5 | Turn Structure | Turn engine and priority windows | Started |
| 6 | Spells, Abilities, and Effects | Main rules-engine body | High priority |
| 7 | Additional Rules | Actions, keywords, SBA, copies, shortcuts | High priority but staged |
| 8 | Multiplayer Rules | Commander multiplayer future | Defer for beta 1v1 except public model shape |
| 9 | Casual Variants | Commander rules live here | Commander subset is high priority |
| Glossary | Definitions | Needed for term mapping | Reference as needed |

## Section 1 - Game Concepts

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 100 | General | High: Commander deck/game assumptions |
| 101 | Magic Golden Rules | High: card text can override base rules |
| 102 | Players | High: actor model |
| 103 | Starting the Game | High: setup, commanders, shuffle, opening hand, life total |
| 104 | Ending the Game | Medium: win/loss/draw tracking |
| 105 | Colors | High: color identity and mana payment |
| 106 | Mana | High: mana pool/payment |
| 107 | Numbers and Symbols | High: costs, X, symbols, counters |
| 108 | Cards | High: card identity |
| 109 | Objects | High: runtime object identity |
| 110 | Permanents | High: battlefield objects |
| 111 | Tokens | Medium: needed after first creature/combat slices |
| 112 | Spells | High: stack objects |
| 113 | Abilities | High: activated/triggered/static split |
| 114 | Emblems | Low for first playable slice |
| 115 | Targets | High: required before real removal/combat tricks |
| 116 | Special Actions | High: playing lands; future face-down/suspend/etc. |
| 117 | Timing and Priority | Critical |
| 118 | Costs | Critical before real spell casting |
| 119 | Life | High |
| 120 | Damage | High before combat |
| 121 | Drawing a Card | Started |
| 122 | Counters | Medium |
| 123 | Stickers | Low / probably unsupported initially |

## Section 2 - Parts of a Card

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 200 | General | Medium |
| 201 | Name | High: identity, interchangeable names |
| 202 | Mana Cost and Color | Critical |
| 203 | Illustration | Display only |
| 204 | Color Indicator | Medium |
| 205 | Type Line | Critical |
| 206 | Expansion Symbol | Display/collection |
| 207 | Text Box | Critical for future Oracle semantics |
| 208 | Power/Toughness | High for creatures and commander legality |
| 209 | Loyalty | Later |
| 210 | Defense | Later |
| 211 | Hand Modifier | Low |
| 212 | Life Modifier | Low |
| 213 | Information Below the Text Box | Low/display |

## Section 3 - Card Types

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 300 | General | High |
| 301 | Artifacts | Medium |
| 302 | Creatures | Critical |
| 303 | Enchantments | Medium |
| 304 | Instants | High |
| 305 | Lands | Critical |
| 306 | Planeswalkers | Later |
| 307 | Sorceries | High |
| 308 | Kindreds | Medium |
| 309-315 | Dungeons, Battles, Planes, Phenomena, Vanguards, Schemes, Conspiracies | Defer unless card/deck requires |

## Section 4 - Zones

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 400 | General | Critical |
| 401 | Library | Critical |
| 402 | Hand | Critical |
| 403 | Battlefield | Critical |
| 404 | Graveyard | High |
| 405 | Stack | Critical |
| 406 | Exile | Medium |
| 407 | Ante | Unsupported |
| 408 | Command | Critical for Commander |

## Section 5 - Turn Structure

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 500 | General | Critical |
| 501 | Beginning Phase | High |
| 502 | Untap Step | Started |
| 503 | Upkeep Step | High |
| 504 | Draw Step | Started |
| 505 | Main Phase | Critical |
| 506 | Combat Phase | Critical next major gameplay body |
| 507 | Beginning of Combat Step | High |
| 508 | Declare Attackers Step | Critical |
| 509 | Declare Blockers Step | Critical |
| 510 | Combat Damage Step | Critical |
| 511 | End of Combat Step | High |
| 512 | Ending Phase | High |
| 513 | End Step | High |
| 514 | Cleanup Step | High |

## Section 6 - Spells, Abilities, and Effects

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 600 | General | Critical |
| 601 | Casting Spells | Critical |
| 602 | Activating Activated Abilities | Critical |
| 603 | Handling Triggered Abilities | High |
| 604 | Handling Static Abilities | High |
| 605 | Mana Abilities | Started |
| 606 | Loyalty Abilities | Later |
| 607 | Linked Abilities | Later |
| 608 | Resolving Spells and Abilities | Critical |
| 609 | Effects | Critical |
| 610 | One-Shot Effects | High |
| 611 | Continuous Effects | High |
| 612 | Text-Changing Effects | Later |
| 613 | Interaction of Continuous Effects | High/later: layers are difficult |
| 614 | Replacement Effects | High |
| 615 | Prevention Effects | Medium |
| 616 | Interaction of Replacement and/or Prevention Effects | Later |

## Section 7 - Additional Rules

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 700 | General | Medium |
| 701 | Keyword Actions | High |
| 702 | Keyword Abilities | High but incremental |
| 703 | Turn-Based Actions | Critical |
| 704 | State-Based Actions | Critical |
| 705 | Flipping a Coin | Later |
| 706 | Rolling a Die | Later |
| 707 | Copying Objects | Later/high complexity |
| 708-722 | Specialty mechanics | Defer until needed |
| 723 | Controlling Another Player | Defer |
| 724 | Ending Turns and Phases | Medium |
| 725-731 | Monarch, initiative, restart, rad counters, subgames, merge, day/night | Defer until needed |
| 732 | Taking Shortcuts | UI/automation relevance |
| 733 | Handling Illegal Actions | High for validation/fallback |

## Section 8 - Multiplayer Rules

Beta gameplay is 1v1, but the model should not make multiplayer impossible.

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 800 | General | Medium model reference |
| 801-805 | Multiplayer options | Defer |
| 806 | Free-for-All Variant | Commander future |
| 807-811 | Other multiplayer variants | Defer |

## Section 9 - Casual Variants

| Rule Range | Title | Anthology Priority |
|---|---|---|
| 900 | General | Medium |
| 901 | Planechase | Defer |
| 902 | Vanguard | Defer |
| 903 | Commander | Critical |
| 904 | Archenemy | Defer |
| 905 | Conspiracy Draft | Defer |

## Immediate V0.5.0 Slice Candidates

Use these before expanding the engine:

1. Starting game audit: `103`, `903`, `408`. Started with
   [[starting-game]] commander-marked command-zone setup.
2. Priority and timing audit: `116`, `117`, `500-505`, `601`, `602`, `605`.
   Started with [[timing-and-priority]] for the current sorcery-window and
   instant-timing slice.
3. Costs and mana audit: `106`, `107`, `118`, `202`, `601`. Started with
   [[costs-and-mana]] for real WUBRG/colorless mana-pool representation and
   current generic-cost payment.
4. Stack resolution audit: `112`, `405`, `608`, `609`.
5. State-based action minimum: `704`. Started with [[state-based-actions]]
   for `704.5a`.
6. Combat foundation: `506-511`, `120`, `302`.

## Rule For New Implementation

Before adding a new game behavior, create or update a focused note for the rule
range and link it from [[CAPABILITY_MATRIX]]. The focused note must say what is
implemented, what is explicitly unsupported, and which tests prove the slice.
