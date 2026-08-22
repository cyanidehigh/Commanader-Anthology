package com.commanderanthology.core.deck;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record DeckMetadata(
        String deckId,
        String name,
        List<String> commanderOracleIds,
        Instant createdAt,
        Instant updatedAt,
        DeckOrigin origin,
        boolean locked,
        boolean editable,
        boolean deletable,
        boolean styleEligible,
        boolean syncEligible
) {
    public DeckMetadata {
        requireText(deckId, "deckId");
        requireText(name, "name");
        commanderOracleIds = List.copyOf(Objects.requireNonNull(commanderOracleIds, "commanderOracleIds"));
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        Objects.requireNonNull(origin, "origin");
    }

    public static DeckMetadata userDeck(String deckId, String name, List<String> commanderOracleIds, Instant now) {
        return new DeckMetadata(deckId, name, commanderOracleIds, now, now, DeckOrigin.USER, false, true, true, true, true);
    }

    public static DeckMetadata lockedPrecon(String deckId, String name, List<String> commanderOracleIds, Instant now) {
        return new DeckMetadata(deckId, name, commanderOracleIds, now, now, DeckOrigin.PRECON, true, false, false, false, false);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

