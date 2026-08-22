package com.commanderanthology.core.deck;

public record DeckSlot(
        String id,
        String deckId,
        String cardName,
        int desiredQuantity,
        DeckSection section,
        CardIdentityStatus identityStatus,
        String oracleId,
        String oracleName,
        String preferredScryfallCardId,
        String preferredPrintingName,
        String preferredSetCode,
        String preferredCollectorNumber
) {
    public DeckSlot {
        requireText(id, "id");
        requireText(deckId, "deckId");
        requireText(cardName, "cardName");
        if (desiredQuantity <= 0) {
            throw new IllegalArgumentException("desiredQuantity must be positive");
        }
        if (section == null) {
            throw new IllegalArgumentException("section is required");
        }
        if (identityStatus == null) {
            identityStatus = CardIdentityStatus.UNRESOLVED;
        }
    }

    public static DeckSlot unresolved(String id, String deckId, String cardName, int desiredQuantity, DeckSection section) {
        return new DeckSlot(id, deckId, cardName, desiredQuantity, section, CardIdentityStatus.UNRESOLVED, null, null, null, null, null, null);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

