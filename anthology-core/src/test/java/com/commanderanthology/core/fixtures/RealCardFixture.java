package com.commanderanthology.core.fixtures;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record RealCardFixture(
        String name,
        String oracleId,
        String manaCost,
        String typeLine,
        String oracleText,
        List<String> keywords,
        Optional<String> power,
        Optional<String> toughness,
        String commanderLegality
) {
    public boolean hasKeyword(String keyword) {
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return keywords.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(normalized::equals);
    }
}
