# Keyword Abilities
#type/rules #status/researched

Links: [[README]], [[CAPABILITY_MATRIX]], [[comprehensive-rules-inventory]], [[../areas/Card Codex]], [[../areas/Card Function Grammar]]

## Source

- Source document: root `MagicCompRules 20260807.docx`
- Effective date: August 7, 2026

## Relevant Rule References

- `702` Keyword Abilities
- `702.1` Keyword abilities are named rules shortcuts
- `113` Abilities
- `207` Text Box
- `601` Casting Spells
- `602` Activating Activated Abilities
- `603` Handling Triggered Abilities
- `604` Handling Static Abilities
- `605` Mana Abilities

## Plain-English Summary

Keyword abilities are rules-level shortcuts. A card may print only the keyword
name, but the rules engine must understand the full rule behind that keyword.

Anthology should not duplicate keyword behavior per card. A card should
reference keyword abilities, and the engine should resolve those references
through a central keyword rules registry.

## Proposed Card Rules Shape

Possible Anthology card-rules shape under discussion:

```text
<CardName>
<Cost>
<Rules {keyword}, {keyword}, {bespoke rules}>
<Power/Toughness>
```

Example:

```text
Serra Angel
{3}{W}{W}
Rules {Flying}, {Vigilance}
4/4
```

The idea is that the card record says which keyword abilities are present
without carrying a copy of the implementation for flying or vigilance.

This is not locked. It is a design candidate for discussion.

## Keyword Reference Model

Cards and runtime objects should hold compact keyword references:

```text
KeywordAbility
- keyword: FLYING
- parameters: none

KeywordAbility
- keyword: WARD
- parameters:
  - cost: {2}

KeywordAbility
- keyword: PROTECTION
- parameters:
  - quality: black
```

The rules engine should hold the behavior:

```text
KeywordRuleRegistry
- FLYING -> modifies blocking legality
- HASTE -> modifies summoning-sickness checks
- DEATHTOUCH -> modifies lethal-damage evaluation
- LIFELINK -> modifies damage results
- FLASH -> modifies cast timing permission
- CYCLING -> creates activated ability from hand
- KICKER -> adds optional additional casting cost
- WARD -> creates triggered target tax
```

This keeps thousands of cards from becoming thousands of repeated rule
implementations.

## Engine Hook Direction

Keyword rules should plug into engine hooks instead of becoming one huge
hardcoded switch.

Initial hook categories:

- cast timing permission
- legal move generation
- target legality
- cost modification or additional cost
- activated ability generation
- triggered ability generation
- blocking legality
- combat damage assignment
- damage result modification
- state-based action checks
- replacement/prevention effects

Each keyword rule should declare which hooks it participates in.

## Boundary With Function Tags

Keyword abilities are exact rules semantics.

They are different from [[../areas/Card Function Grammar]] tags. For example,
`Flying` is a rules capability, not just a descriptive deck-building tag.

Function tags may explain that a deck has evasion, but the engine must use the
keyword reference and keyword registry to decide actual legality.

## Possible First Implementation Direction

Do not start by implementing all keyword abilities.

A possible safe implementation slice could be:

1. Use real card records from existing legacy data as fixtures.
2. `GameKeyword` or equivalent enum/model.
3. `KeywordAbility` references on card/runtime objects.
4. A central keyword registry shape.
5. One tiny tested rule hook, likely haste versus summoning sickness, or a
   placeholder registry test with no gameplay effect.

Combat-facing keywords such as flying, reach, first strike, double strike,
trample, deathtouch, and lifelink should wait until the combat model exists.

## Real Card Fixture Direction

Keyword and card-rules tests should prefer real card data over invented toy
cards where practical.

Existing source candidates:

- `Commander-Sim/PROD/data/library/global/card_codex/card_records.json`
- `Commander-Sim/PROD/data/library/global/card_codex/card_id_map.json`
- `Commander-Sim/PROD/data/library/decks/*/decklist.txt`
- `Commander-Sim/PROD/import_decks/*.txt`

The legacy card codex already includes Scryfall-derived fields useful for
fixtures:

- `oracle_id`
- `name`
- `oracle.mana_cost`
- `oracle.type_line`
- `oracle.oracle_text`
- `oracle.keywords`
- `oracle.power`
- `oracle.toughness`
- `oracle.color_identity`
- `oracle.legalities.commander`

Using real fixtures means rules tests also help migrate real card structure
into Anthology, rather than creating throwaway smoke cards.

First executable fixture slice:

- Fixture file:
  `anthology-core/src/test/resources/real-card-fixtures/keyword-cards.psv`
- Loader:
  `RealCardFixtureLoader`
- Verification:
  `:anthology-core:realCardFixtureSmokeTest`
- Source:
  legacy Commander Sim Card Codex Scryfall-derived records

The first fixture set covers `Lightning Greaves`, `Swiftfoot Boots`,
`Akrasan Squire`, `Battlegrace Angel`, and `Rafiq of the Many`. These cards
exercise real mana cost, type line, Oracle text, keyword lists, creature
power/toughness, noncreature empty power/toughness, and Commander legality.

Initial fixture candidates from existing decks:

- `Rafiq of the Many` deck for combat/evasion/equipment keywords.
- `Karn, Legacy Reforged` deck for artifacts, activated abilities, and
  colorless mana patterns.
- `Sliver Overlord` deck for shared creature abilities and tribal pressure.
- `Lightning Greaves` / `Swiftfoot Boots` as later equipment-granting keyword
  cases, once attachments exist.

Fixture tests must still be explicit about what behavior is expected and what
is not implemented yet.

## Explicit Unsupported Cases

Until a keyword has a focused rule note and tests, it is not implemented even
if it can be represented in data.

Parameterized keyword parsing is not implemented yet.

Oracle-text parsing into keyword references is not implemented yet.

Continuous effects that grant or remove keyword abilities are not implemented
yet.

Multiple instances of keywords are not normalized yet.

## Current Status

Researched and documented as a proposed model only. The first real-card fixture
loader/test exists to ground future rules work in real card data. No keyword
gameplay behavior is implemented by this note, and the card-rules shape is not
accepted yet.
