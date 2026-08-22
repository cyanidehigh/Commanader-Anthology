package com.commanderanthology.core.move;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record StructuredMoveToken(
        MoveType type,
        String legalMoveId,
        String actor,
        String sourceObject,
        Optional<String> cardOracleId,
        Optional<String> displayName,
        Optional<String> fromZone,
        Optional<String> toZone,
        List<String> targets,
        List<String> modes,
        Optional<String> costChoice,
        Map<String, Integer> manaPayment,
        Optional<Integer> xValue,
        String timingWindow,
        List<String> reasonCodes,
        Optional<String> scoringVersion
) {
    public StructuredMoveToken {
        Objects.requireNonNull(type, "type");
        requireText(legalMoveId, "legalMoveId");
        requireText(actor, "actor");
        requireText(sourceObject, "sourceObject");
        cardOracleId = Objects.requireNonNull(cardOracleId, "cardOracleId");
        displayName = Objects.requireNonNull(displayName, "displayName");
        fromZone = Objects.requireNonNull(fromZone, "fromZone");
        toZone = Objects.requireNonNull(toZone, "toZone");
        targets = List.copyOf(Objects.requireNonNull(targets, "targets"));
        modes = List.copyOf(Objects.requireNonNull(modes, "modes"));
        costChoice = Objects.requireNonNull(costChoice, "costChoice");
        manaPayment = Map.copyOf(Objects.requireNonNull(manaPayment, "manaPayment"));
        xValue = Objects.requireNonNull(xValue, "xValue");
        requireText(timingWindow, "timingWindow");
        reasonCodes = List.copyOf(Objects.requireNonNull(reasonCodes, "reasonCodes"));
        scoringVersion = Objects.requireNonNull(scoringVersion, "scoringVersion");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

