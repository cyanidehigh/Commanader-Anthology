package com.commanderanthology.desktop;

record ImportedCollectionRow(
        int quantity,
        String cardName,
        String setCode,
        String collectorNumber,
        String scryfallCardId,
        boolean foil
) {
    ImportedCollectionRow {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (cardName == null || cardName.isBlank()) {
            throw new IllegalArgumentException("cardName is required");
        }
        cardName = cardName.trim();
        setCode = blankToNull(setCode);
        if (setCode != null) {
            setCode = setCode.toUpperCase();
        }
        collectorNumber = blankToNull(collectorNumber);
        scryfallCardId = blankToNull(scryfallCardId);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
