package com.commanderanthology.desktop;

enum DesktopWorkspace {
    DASHBOARD(
            "Dashboard",
            "Full Anthology command surface.",
            """
            Current desktop branch purpose:

            - Host the full Commander Anthology program.
            - Bring CCBuilder and Commander Sim into one desktop workflow.
            - Keep shared truth in anthology-core.
            - Keep Android as the lighter companion, not the full app.

            Immediate desktop milestones:

            1. Port CCBuilder deck and collection workflows into Java.
            2. Add local Card Codex and Scryfall cache interfaces.
            3. Audit Commander Sim for Java legal move package migration.
            4. Add Play and Auto simulation workspaces.
            5. Add player-style capture after move records stabilize.
            """
    ),
    DECKS(
            "Deck Builder",
            "Collection-aware Commander deck construction.",
            """
            Desktop Builder owns the full deck construction experience.

            Required direction:

            - User decks, imported decks, generated decks, copied precons.
            - Locked bundled precon catalog.
            - Commander-only metadata.
            - Oracle identity for deck intent.
            - Printing identity for physical inventory.
            - Collection-aware missing/owned/assigned states.
            - Future recommendations shaped by player-style data.

            First port target:

            Move the useful CCBuilder deck model and import behavior into Java,
            then connect it to the shared DeckMetadata boundary.
            """
    ),
    COLLECTION(
            "Collection",
            "Physical card ownership and deck assignment.",
            """
            Collection tracks real physical cards.

            Required direction:

            - Containers: binders, boxes, sets, decks, ordered piles, proxy areas.
            - Printing-specific inventory rows.
            - Foil and nonfoil rows remain separate.
            - Assigning a card to a deck moves a physical copy.
            - Deck intent remains oracle-specific by default.

            First port target:

            Move the CCBuilder inventory and assignment rules into Java and add
            validation around physical copy movement.
            """
    ),
    CARD_CODEX(
            "Card Codex",
            "Offline-first card identity and Scryfall-backed data.",
            """
            Card Codex is the shared source of card truth.

            Required direction:

            - Scryfall oracle_id as primary rules/card identity.
            - Typed fallback IDs for unresolved/manual/custom records.
            - SQLite-backed local card store and indexes.
            - Scryfall API only when local data is missing or explicitly refreshed.
            - Old Sim six-hex IDs remain migration/reference only.

            First port target:

            Define Java interfaces for local card lookup, printing lookup,
            bulk import, and API fallback.
            """
    ),
    SIM(
            "Commander Sim",
            "Play and Auto simulation surface.",
            """
            Desktop Sim is the first major integration job.

            Required direction:

            - Play and Auto modes only.
            - Legal Move Package first.
            - Human and AI choices consume the same legal move list.
            - AI scoring ranks legal moves but never defines legality.
            - Final execution validation happens before mutation.
            - Player style is captured only from human Play-mode decisions.

            First port target:

            Audit Commander-Sim PROD/DEV modules and identify which tested
            mechanics can become Java legal move package fixtures.
            """
    ),
    PLAYER_STYLE(
            "Player Style",
            "Human play data for Builder personalization.",
            """
            Player style belongs to the user, not the Sim AI.

            Required direction:

            - Built from actual human Play-mode decisions.
            - Not fed into AI opponent strategy.
            - Used by Builder for recommendations, cuts, substitutions, and
              explanations.
            - Portable as lightweight sync-bundle summary data.

            First port target:

            Define the player style summary schema after legal move records are
            stable enough to survive refactors.
            """
    ),
    SYNC(
            "Backup",
            "Local export/import for user-owned Anthology data.",
            """
            Backup is the current local data bridge.

            Current behavior:

            - Export the current desktop state JSON.
            - Import a previously exported desktop state JSON.
            - Include user-owned portable data only.
            - Exclude generated caches and bundled card data.

            Future sync direction:

            Desktop and Android should still work separately. Google Drive,
            user-owned storage, or any server option remains a later explicit
            sync decision, not what this tab currently does.
            """
    );

    private final String label;
    private final String summary;
    private final String body;

    static DesktopWorkspace[] visible() {
        return new DesktopWorkspace[] {
                DECKS,
                COLLECTION,
                SIM,
                PLAYER_STYLE,
                SYNC
        };
    }

    DesktopWorkspace(String label, String summary, String body) {
        this.label = label;
        this.summary = summary;
        this.body = body;
    }

    String label() {
        return label;
    }

    String summary() {
        return summary;
    }

    String body() {
        return body;
    }
}
