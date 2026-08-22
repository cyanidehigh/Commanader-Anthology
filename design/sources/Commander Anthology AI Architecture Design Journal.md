# Commander Anthology AI Architecture Design Journal
#type/source-note #status/imported #area/ai-architecture

Links: [[../DDS]], [[../areas/AI Architecture]], [[../areas/Knowledge Projection]]

Source file:

- `Commander_Anthology_AI_Architecture_Design_Journal.docx`

## Purpose

This note captures the main design material imported from the AI architecture
design journal so it can be traced in Obsidian.

## Imported Insights

- Do not make the AI memorize every card.
- Model the formal language of Magic.
- Cards should become structured descriptions composed from reusable concepts
  and operations.
- Deterministic truth stays deterministic.
- The AI does not directly mutate game state.
- The AI proposes structured actions or state transitions.
- The deterministic engine validates and executes accepted actions.
- Authoritative game state should not be exposed directly to every consumer.
- Consumers receive permission-appropriate projections.
- The AI should know only what a real player could legally know.
- Inference must remain separate from factual knowledge.
- Long-term AI direction is a transformer over structured game objects,
  relationships, and player knowledge rather than free-form English.
- Actions can be generated as structured tokens.
- Structured autoregressive generation aligns with Magic's branching decisions.

## Suggested Follow-Up Documents

- [[../areas/Knowledge Projection|Knowledge Projection Boundary]]
- Player Knowledge Model
- AI Decision Contract
- Token Vocabulary Specification
- Information Visibility Policy
- Transformer Input Schema
- Structured Token Grammar

