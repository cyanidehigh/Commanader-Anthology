package com.commanderanthology.core.collection;

public record CardContainer(
        String id,
        String name,
        ContainerType type
) {
    public CardContainer {
        requireText(id, "id");
        requireText(name, "name");
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}

