package com.commanderanthology.core.deck;

public record Deck(
        String id,
        String name,
        String commanderName,
        String containerId
) {
    public Deck {
        requireText(id, "id");
        requireText(name, "name");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

