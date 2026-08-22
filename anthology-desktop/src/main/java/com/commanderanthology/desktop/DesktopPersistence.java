package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.CardIdentityStatus;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckAssignment;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class DesktopPersistence {
    private static final String NULL = "-";
    private static final int SCHEMA_VERSION = 1;
    private static final String TSV_VERSION = "ANTHOLOGY_DESKTOP_STATE_V1";

    private final Path dataFile;

    DesktopPersistence() {
        this(defaultDataFile());
    }

    DesktopPersistence(Path dataFile) {
        this.dataFile = dataFile;
    }

    Optional<DesktopSnapshot> load() {
        if (!Files.exists(dataFile)) {
            return Optional.empty();
        }

        try {
            String text = Files.readString(dataFile, StandardCharsets.UTF_8);
            if (text.stripLeading().startsWith("{")) {
                return Optional.of(readJsonSnapshot(text));
            }
            return readLegacyTsv(text);
        } catch (RuntimeException | IOException error) {
            System.err.println("Could not load desktop state: " + error.getMessage());
            return Optional.empty();
        }
    }

    void save(DesktopSnapshot snapshot) {
        try {
            Files.createDirectories(dataFile.getParent());
            Path temp = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
            Files.writeString(temp, writeJsonSnapshot(snapshot), StandardCharsets.UTF_8);
            Files.move(temp, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveError) {
            try {
                Path temp = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
                Files.move(temp, dataFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException fallbackError) {
                throw new IllegalStateException("Could not save desktop state", fallbackError);
            }
        }
    }

    Path dataFile() {
        return dataFile;
    }

    private static String writeJsonSnapshot(DesktopSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schemaVersion", Integer.toString(SCHEMA_VERSION), false);
        field(builder, "exportedAtEpochMillis", Long.toString(System.currentTimeMillis()), false);
        array(builder, "containers", snapshot.containers(), DesktopPersistence::containerJson);
        array(builder, "inventoryEntries", snapshot.inventoryEntries(), DesktopPersistence::entryJson);
        array(builder, "decks", snapshot.decks(), DesktopPersistence::deckJson);
        array(builder, "deckSlots", snapshot.deckSlots(), DesktopPersistence::slotJson);
        array(builder, "deckAssignments", snapshot.deckAssignments(), DesktopPersistence::assignmentJson);
        trimTrailingComma(builder);
        builder.append("\n}\n");
        return builder.toString();
    }

    private static DesktopSnapshot readJsonSnapshot(String text) {
        Object root = new JsonReader(text).read();
        if (!(root instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Desktop state root must be a JSON object");
        }

        return new DesktopSnapshot(
                objects(map, "decks").stream().map(DesktopPersistence::deckFromJson).toList(),
                objects(map, "deckSlots").stream().map(DesktopPersistence::slotFromJson).toList(),
                objects(map, "deckAssignments").stream().map(DesktopPersistence::assignmentFromJson).toList(),
                objects(map, "containers").stream().map(DesktopPersistence::containerFromJson).toList(),
                objects(map, "inventoryEntries").stream().map(DesktopPersistence::entryFromJson).toList()
        );
    }

    private static String deckJson(Deck deck) {
        return object(
                pair("id", deck.id()),
                pair("name", deck.name()),
                pair("commanderName", deck.commanderName()),
                pair("containerId", deck.containerId())
        );
    }

    private static Deck deckFromJson(Map<String, Object> map) {
        return new Deck(text(map, "id"), text(map, "name"), nullableText(map, "commanderName"), nullableText(map, "containerId"));
    }

    private static String slotJson(DeckSlot slot) {
        return object(
                pair("id", slot.id()),
                pair("deckId", slot.deckId()),
                pair("cardName", slot.cardName()),
                pair("desiredQuantity", slot.desiredQuantity()),
                pair("section", slot.section().name()),
                pair("identityStatus", slot.identityStatus().name()),
                pair("oracleId", slot.oracleId()),
                pair("oracleName", slot.oracleName()),
                pair("preferredScryfallCardId", slot.preferredScryfallCardId()),
                pair("preferredPrintingName", slot.preferredPrintingName()),
                pair("preferredSetCode", slot.preferredSetCode()),
                pair("preferredCollectorNumber", slot.preferredCollectorNumber())
        );
    }

    private static DeckSlot slotFromJson(Map<String, Object> map) {
        return new DeckSlot(
                text(map, "id"),
                text(map, "deckId"),
                text(map, "cardName"),
                integer(map, "desiredQuantity"),
                enumValue(DeckSection.class, text(map, "section")),
                enumValue(CardIdentityStatus.class, text(map, "identityStatus")),
                nullableText(map, "oracleId"),
                nullableText(map, "oracleName"),
                nullableText(map, "preferredScryfallCardId"),
                nullableText(map, "preferredPrintingName"),
                nullableText(map, "preferredSetCode"),
                nullableText(map, "preferredCollectorNumber")
        );
    }

    private static String assignmentJson(DeckAssignment assignment) {
        return object(
                pair("id", assignment.id()),
                pair("deckSlotId", assignment.deckSlotId()),
                pair("inventoryEntryId", assignment.inventoryEntryId()),
                pair("assignedQuantity", assignment.assignedQuantity()),
                pair("fromContainerId", assignment.fromContainerId()),
                pair("toDeckId", assignment.toDeckId())
        );
    }

    private static DeckAssignment assignmentFromJson(Map<String, Object> map) {
        return new DeckAssignment(
                text(map, "id"),
                text(map, "deckSlotId"),
                text(map, "inventoryEntryId"),
                integer(map, "assignedQuantity"),
                nullableText(map, "fromContainerId"),
                text(map, "toDeckId")
        );
    }

    private static String containerJson(CardContainer container) {
        return object(
                pair("id", container.id()),
                pair("name", container.name()),
                pair("type", container.type().name())
        );
    }

    private static CardContainer containerFromJson(Map<String, Object> map) {
        return new CardContainer(text(map, "id"), text(map, "name"), enumValue(ContainerType.class, text(map, "type")));
    }

    private static String entryJson(InventoryEntry entry) {
        return object(
                pair("id", entry.id()),
                pair("containerId", entry.containerId()),
                pair("cardName", entry.cardName()),
                pair("quantity", entry.quantity()),
                pair("identityStatus", entry.identityStatus().name()),
                pair("oracleId", entry.oracleId()),
                pair("oracleName", entry.oracleName()),
                pair("scryfallCardId", entry.scryfallCardId()),
                pair("printingName", entry.printingName()),
                pair("setCode", entry.setCode()),
                pair("collectorNumber", entry.collectorNumber()),
                pair("foil", entry.foil())
        );
    }

    private static InventoryEntry entryFromJson(Map<String, Object> map) {
        return new InventoryEntry(
                text(map, "id"),
                text(map, "containerId"),
                text(map, "cardName"),
                integer(map, "quantity"),
                enumValue(CardIdentityStatus.class, text(map, "identityStatus")),
                nullableText(map, "oracleId"),
                nullableText(map, "oracleName"),
                nullableText(map, "scryfallCardId"),
                nullableText(map, "printingName"),
                nullableText(map, "setCode"),
                nullableText(map, "collectorNumber"),
                bool(map, "foil", "isFoil")
        );
    }

    private static Optional<DesktopSnapshot> readLegacyTsv(String text) {
        List<Deck> decks = new ArrayList<>();
        List<DeckSlot> deckSlots = new ArrayList<>();
        List<DeckAssignment> assignments = new ArrayList<>();
        List<CardContainer> containers = new ArrayList<>();
        List<InventoryEntry> entries = new ArrayList<>();

        List<String> lines = text.lines().toList();
        if (lines.isEmpty() || !TSV_VERSION.equals(lines.get(0))) {
            return Optional.empty();
        }
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\t", -1);
            switch (parts[0]) {
                case "DECK" -> decks.add(readDeck(parts));
                case "SLOT" -> deckSlots.add(readDeckSlot(parts));
                case "ASSIGN" -> assignments.add(readAssignment(parts));
                case "CONTAINER" -> containers.add(readContainer(parts));
                case "ENTRY" -> entries.add(readEntry(parts));
                default -> throw new IllegalArgumentException("Unknown state row: " + parts[0]);
            }
        }
        return Optional.of(new DesktopSnapshot(decks, deckSlots, assignments, containers, entries));
    }

    private static void field(StringBuilder builder, String name, String value, boolean quoted) {
        builder.append("  \"").append(name).append("\": ");
        if (quoted) {
            builder.append(quote(value));
        } else {
            builder.append(value);
        }
        builder.append(",\n");
    }

    private static <T> void array(StringBuilder builder, String name, List<T> values, JsonWriter<T> writer) {
        builder.append("  \"").append(name).append("\": [");
        if (!values.isEmpty()) {
            builder.append('\n');
            for (T value : values) {
                builder.append("    ").append(writer.write(value)).append(",\n");
            }
            trimTrailingComma(builder);
            builder.append('\n').append("  ");
        }
        builder.append("],\n");
    }

    private static String object(JsonPair... pairs) {
        StringBuilder builder = new StringBuilder("{");
        for (JsonPair pair : pairs) {
            builder.append('"').append(pair.name()).append("\": ").append(pair.jsonValue()).append(", ");
        }
        if (pairs.length > 0) {
            builder.setLength(builder.length() - 2);
        }
        builder.append('}');
        return builder.toString();
    }

    private static JsonPair pair(String name, String value) {
        return new JsonPair(name, value == null ? "null" : quote(value));
    }

    private static JsonPair pair(String name, int value) {
        return new JsonPair(name, Integer.toString(value));
    }

    private static JsonPair pair(String name, boolean value) {
        return new JsonPair(name, Boolean.toString(value));
    }

    private static String quote(String value) {
        StringBuilder builder = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(character);
            }
        }
        return builder.append('"').toString();
    }

    private static void trimTrailingComma(StringBuilder builder) {
        int comma = builder.lastIndexOf(",");
        if (comma >= 0) {
            builder.deleteCharAt(comma);
        }
    }

    private static List<Map<String, Object>> objects(Map<?, ?> map, String name) {
        Object value = map.get(name);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> objects = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> itemMap) {
                Map<String, Object> typed = new LinkedHashMap<>();
                itemMap.forEach((key, itemValue) -> typed.put(String.valueOf(key), itemValue));
                objects.add(typed);
            }
        }
        return objects;
    }

    private static String text(Map<String, Object> map, String name) {
        Object value = map.get(name);
        if (value instanceof String text) {
            return text;
        }
        throw new IllegalArgumentException("Missing text field: " + name);
    }

    private static String nullableText(Map<String, Object> map, String name) {
        Object value = map.get(name);
        return value == null ? null : String.valueOf(value);
    }

    private static int integer(Map<String, Object> map, String name) {
        Object value = map.get(name);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Missing integer field: " + name);
    }

    private static boolean bool(Map<String, Object> map, String... names) {
        for (String name : names) {
            Object value = map.get(name);
            if (value instanceof Boolean bool) {
                return bool;
            }
        }
        throw new IllegalArgumentException("Missing boolean field: " + String.join("/", names));
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value) {
        String normalized = value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(java.util.Locale.ROOT);
        return Enum.valueOf(type, normalized);
    }

    private static String writeDeck(Deck deck) {
        return row("DECK", deck.id(), deck.name(), deck.commanderName(), deck.containerId());
    }

    private static Deck readDeck(String[] parts) {
        requireParts(parts, 5);
        return new Deck(value(parts[1]), value(parts[2]), nullable(parts[3]), nullable(parts[4]));
    }

    private static String writeDeckSlot(DeckSlot slot) {
        return row(
                "SLOT",
                slot.id(),
                slot.deckId(),
                slot.cardName(),
                Integer.toString(slot.desiredQuantity()),
                slot.section().name(),
                slot.identityStatus().name(),
                slot.oracleId(),
                slot.oracleName(),
                slot.preferredScryfallCardId(),
                slot.preferredPrintingName(),
                slot.preferredSetCode(),
                slot.preferredCollectorNumber()
        );
    }

    private static DeckSlot readDeckSlot(String[] parts) {
        requireParts(parts, 13);
        return new DeckSlot(
                value(parts[1]),
                value(parts[2]),
                value(parts[3]),
                Integer.parseInt(value(parts[4])),
                DeckSection.valueOf(value(parts[5])),
                CardIdentityStatus.valueOf(value(parts[6])),
                nullable(parts[7]),
                nullable(parts[8]),
                nullable(parts[9]),
                nullable(parts[10]),
                nullable(parts[11]),
                nullable(parts[12])
        );
    }

    private static String writeAssignment(DeckAssignment assignment) {
        return row(
                "ASSIGN",
                assignment.id(),
                assignment.deckSlotId(),
                assignment.inventoryEntryId(),
                Integer.toString(assignment.assignedQuantity()),
                assignment.fromContainerId(),
                assignment.toDeckId()
        );
    }

    private static DeckAssignment readAssignment(String[] parts) {
        requireParts(parts, 7);
        return new DeckAssignment(
                value(parts[1]),
                value(parts[2]),
                value(parts[3]),
                Integer.parseInt(value(parts[4])),
                nullable(parts[5]),
                value(parts[6])
        );
    }

    private static String writeContainer(CardContainer container) {
        return row("CONTAINER", container.id(), container.name(), container.type().name());
    }

    private static CardContainer readContainer(String[] parts) {
        requireParts(parts, 4);
        return new CardContainer(value(parts[1]), value(parts[2]), ContainerType.valueOf(value(parts[3])));
    }

    private static String writeEntry(InventoryEntry entry) {
        return row(
                "ENTRY",
                entry.id(),
                entry.containerId(),
                entry.cardName(),
                Integer.toString(entry.quantity()),
                entry.identityStatus().name(),
                entry.oracleId(),
                entry.oracleName(),
                entry.scryfallCardId(),
                entry.printingName(),
                entry.setCode(),
                entry.collectorNumber(),
                Boolean.toString(entry.foil())
        );
    }

    private static InventoryEntry readEntry(String[] parts) {
        requireParts(parts, 13);
        return new InventoryEntry(
                value(parts[1]),
                value(parts[2]),
                value(parts[3]),
                Integer.parseInt(value(parts[4])),
                CardIdentityStatus.valueOf(value(parts[5])),
                nullable(parts[6]),
                nullable(parts[7]),
                nullable(parts[8]),
                nullable(parts[9]),
                nullable(parts[10]),
                nullable(parts[11]),
                Boolean.parseBoolean(value(parts[12]))
        );
    }

    private static String row(String type, String... values) {
        StringBuilder builder = new StringBuilder(type);
        for (String value : values) {
            builder.append('\t').append(encoded(value));
        }
        return builder.toString();
    }

    private static String encoded(String value) {
        if (value == null) {
            return NULL;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String value(String encoded) {
        if (NULL.equals(encoded)) {
            throw new IllegalArgumentException("Required value was null");
        }
        return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    private static String nullable(String encoded) {
        if (NULL.equals(encoded)) {
            return null;
        }
        return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    private static void requireParts(String[] parts, int count) {
        if (parts.length != count) {
            throw new IllegalArgumentException("Expected " + count + " fields but found " + parts.length);
        }
    }

    private static Path defaultDataFile() {
        String appData = System.getenv("APPDATA");
        Path root = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), "AppData", "Roaming")
                : Path.of(appData);
        return root.resolve("Commander Anthology").resolve("anthology-desktop-state.json");
    }

    private record JsonPair(String name, String jsonValue) {
    }

    @FunctionalInterface
    private interface JsonWriter<T> {
        String write(T value);
    }

    private static final class JsonReader {
        private final String text;
        private int index;

        JsonReader(String text) {
            this.text = text;
        }

        Object read() {
            Object value = readValue();
            skipWhitespace();
            if (index != text.length()) {
                throw new IllegalArgumentException("Unexpected JSON content at " + index);
            }
            return value;
        }

        private Object readValue() {
            skipWhitespace();
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            char character = text.charAt(index);
            return switch (character) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't', 'f' -> readBoolean();
                case 'n' -> readNull();
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                expect('}');
                return map;
            }
            while (true) {
                String key = readString();
                skipWhitespace();
                expect(':');
                map.put(key, readValue());
                skipWhitespace();
                if (peek('}')) {
                    expect('}');
                    return map;
                }
                expect(',');
            }
        }

        private List<Object> readArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                expect(']');
                return list;
            }
            while (true) {
                list.add(readValue());
                skipWhitespace();
                if (peek(']')) {
                    expect(']');
                    return list;
                }
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < text.length()) {
                char character = text.charAt(index++);
                if (character == '"') {
                    return builder.toString();
                }
                if (character == '\\') {
                    char escaped = text.charAt(index++);
                    switch (escaped) {
                        case '"' -> builder.append('"');
                        case '\\' -> builder.append('\\');
                        case '/' -> builder.append('/');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        default -> throw new IllegalArgumentException("Unsupported JSON escape: " + escaped);
                    }
                } else {
                    builder.append(character);
                }
            }
            throw new IllegalArgumentException("Unterminated JSON string");
        }

        private Boolean readBoolean() {
            if (text.startsWith("true", index)) {
                index += 4;
                return true;
            }
            if (text.startsWith("false", index)) {
                index += 5;
                return false;
            }
            throw new IllegalArgumentException("Invalid boolean at " + index);
        }

        private Object readNull() {
            if (!text.startsWith("null", index)) {
                throw new IllegalArgumentException("Invalid null at " + index);
            }
            index += 4;
            return null;
        }

        private Number readNumber() {
            int start = index;
            while (index < text.length()) {
                char character = text.charAt(index);
                if (!Character.isDigit(character) && character != '-') {
                    break;
                }
                index++;
            }
            return Long.parseLong(text.substring(start, index));
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private boolean peek(char expected) {
            skipWhitespace();
            return index < text.length() && text.charAt(index) == expected;
        }

        private void expect(char expected) {
            skipWhitespace();
            if (index >= text.length() || text.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' at " + index);
            }
            index++;
        }
    }
}
