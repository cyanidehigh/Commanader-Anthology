package com.commanderanthology.core.deck;

public record DeckAssignment(
        String id,
        String deckSlotId,
        String inventoryEntryId,
        int assignedQuantity,
        String fromContainerId,
        String toDeckId
) {
    public DeckAssignment {
        requireText(id, "id");
        requireText(deckSlotId, "deckSlotId");
        requireText(inventoryEntryId, "inventoryEntryId");
        requireText(toDeckId, "toDeckId");
        if (assignedQuantity <= 0) {
            throw new IllegalArgumentException("assignedQuantity must be positive");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

