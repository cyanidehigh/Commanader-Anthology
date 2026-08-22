package com.commanderanthology.core.collection;

import com.commanderanthology.core.deck.CardIdentityStatus;

public record InventoryEntry(
        String id,
        String containerId,
        String cardName,
        int quantity,
        CardIdentityStatus identityStatus,
        String oracleId,
        String oracleName,
        String scryfallCardId,
        String printingName,
        String setCode,
        String collectorNumber,
        boolean foil
) {
    public InventoryEntry {
        requireText(id, "id");
        requireText(containerId, "containerId");
        requireText(cardName, "cardName");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (identityStatus == null) {
            identityStatus = CardIdentityStatus.UNRESOLVED;
        }
    }

    public static InventoryEntry unresolved(String id, String containerId, String cardName, int quantity, boolean foil) {
        return new InventoryEntry(id, containerId, cardName, quantity, CardIdentityStatus.UNRESOLVED, null, null, null, null, null, null, foil);
    }

    public boolean matchesDeckSlot(String slotCardName, String slotOracleId) {
        if (slotOracleId != null && oracleId != null) {
            return slotOracleId.equals(oracleId);
        }
        return normalize(cardName).equals(normalize(slotCardName));
    }

    public boolean samePhysicalCardAs(InventoryEntry other) {
        if (scryfallCardId != null && other.scryfallCardId != null) {
            return scryfallCardId.equals(other.scryfallCardId) && foil == other.foil;
        }
        return normalize(cardName).equals(normalize(other.cardName))
                && same(setCode, other.setCode)
                && same(collectorNumber, other.collectorNumber)
                && foil == other.foil;
    }

    private static boolean same(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

