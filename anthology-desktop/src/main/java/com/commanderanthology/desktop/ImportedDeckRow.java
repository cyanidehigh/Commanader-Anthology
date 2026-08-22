package com.commanderanthology.desktop;

import com.commanderanthology.core.deck.DeckSection;

record ImportedDeckRow(
        int quantity,
        String cardName,
        DeckSection section,
        String scryfallCardId,
        String setCode,
        String collectorNumber
) {
    ImportedDeckRow(int quantity, String cardName, DeckSection section) {
        this(quantity, cardName, section, null, null, null);
    }

    ImportedDeckRow {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (cardName == null || cardName.isBlank()) {
            throw new IllegalArgumentException("cardName is required");
        }
        if (section == null) {
            section = DeckSection.OTHER;
        }
        cardName = cardName.trim();
        scryfallCardId = blankToNull(scryfallCardId);
        setCode = blankToNull(setCode);
        collectorNumber = blankToNull(collectorNumber);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
