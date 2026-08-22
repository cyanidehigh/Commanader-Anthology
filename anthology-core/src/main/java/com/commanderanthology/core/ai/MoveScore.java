package com.commanderanthology.core.ai;

import java.util.Map;
import java.util.Objects;

public record MoveScore(
        String legalMoveId,
        int baseScore,
        Map<String, Integer> categoryScores,
        int totalScore,
        String tieBreak,
        String scoringVersion
) {
    public MoveScore {
        requireText(legalMoveId, "legalMoveId");
        categoryScores = Map.copyOf(Objects.requireNonNull(categoryScores, "categoryScores"));
        requireText(tieBreak, "tieBreak");
        requireText(scoringVersion, "scoringVersion");
    }

    public static MoveScore of(String legalMoveId, int baseScore, Map<String, Integer> categoryScores, String tieBreak, String scoringVersion) {
        int total = baseScore + categoryScores.values().stream().mapToInt(Integer::intValue).sum();
        return new MoveScore(legalMoveId, baseScore, categoryScores, total, tieBreak, scoringVersion);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

