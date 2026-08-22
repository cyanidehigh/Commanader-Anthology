# 0015 Player Style Data Boundary
#type/decision #status/accepted #area/player-style

Links: [[../DDS]], [[../areas/Player Style]], [[../Commander Sim]], [[../CCBuilder]], [[0014 Sync Bundle Schema]]

## Decision

Player style is built from the user's actual gameplay choices.

Commander Sim collects player-style data from human actions in Play mode. The
Builder portion uses that collected data to guide deck-building recommendations,
cuts, substitutions, explanations, and collection-aware choices.

Player style data must not be offered to the Sim AI as strategic input. It is
not a training signal for the AI opponent and must not be used to make the AI
play more like the user.

## Allowed Inputs

Player style may be learned from:

- human-selected game actions in Play mode
- game context around those human actions
- deck identity and card identities involved in those actions
- public/known game state visible to the human at the time of action

## Disallowed Inputs

Player style must not be learned from:

- Auto mode
- AI-selected moves
- speculative AI reasoning
- hidden information the player could not legally know
- bundled precon presence alone
- CCBuilder suggestions merely being shown
- generated recommendations unless the user later acts on them in real play

CCBuilder editing choices may be useful product feedback later, but they are not
the first player-style source. The first style model is based on actual play.

## Allowed Consumers

Player style may be consumed by:

- CCBuilder recommendations
- deck-building explanations
- cut/add/substitution ranking
- collection-aware deck paths
- user-facing style summaries

## Disallowed Consumers

Player style must not be consumed by:

- Sim AI strategic decision-making
- rules legality
- Commander legality
- Card Codex identity resolution
- ownership or physical inventory movement
- hidden-information access

## Sync Rule

Normal sync uses lightweight player style summaries only.

Raw high-volume decision logs are not part of the normal sync bundle. They may
be exported through a separate explicit workflow later if needed.

## Consequences

- The Sim learns about the player without letting the AI copy or exploit the
  player.
- CCBuilder becomes more personal over time.
- Deterministic truth remains untouched.
- The first implementation can capture play events first and derive summaries
  later.

