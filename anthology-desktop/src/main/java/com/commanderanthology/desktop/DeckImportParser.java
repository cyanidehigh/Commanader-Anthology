package com.commanderanthology.desktop;

import com.commanderanthology.core.deck.DeckSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DeckImportParser {
    private static final Pattern QUANTITY_FIRST = Pattern.compile("^(\\d+)\\s+(.+)$");
    private static final Pattern QUANTITY_X_FIRST = Pattern.compile("^(\\d+)x\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    private DeckImportParser() {
    }

    static DeckImportParseResult parse(String rawText) {
        List<ImportedDeckRow> rows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean ignoringSection = false;
        boolean ignoringCommanderSection = false;

        String[] lines = rawText == null ? new String[0] : rawText.split("\\R");
        for (int index = 0; index < lines.length; index++) {
            int lineNumber = index + 1;
            String line = lines[index].trim();
            if (line.isBlank() || line.startsWith("//") || line.startsWith("#")) {
                continue;
            }

            DeckSection headerSection = sectionFromHeader(line);
            if (headerSection != null) {
                ignoringCommanderSection = headerSection == DeckSection.COMMANDER;
                ignoringSection = false;
                continue;
            }
            if (isIgnoredImportHeader(line)) {
                ignoringSection = true;
                ignoringCommanderSection = false;
                continue;
            }
            if (ignoringSection || ignoringCommanderSection) {
                continue;
            }

            if (isNamedCommanderLine(line)) {
                continue;
            }

            ImportedDeckRow parsed = parseDeckCardLine(line);
            if (parsed == null) {
                warnings.add("Line " + lineNumber + ": could not parse '" + line + "'");
            } else {
                rows.add(parsed);
            }
        }

        return new DeckImportParseResult(rows, warnings);
    }

    private static ImportedDeckRow parseDeckCardLine(String line) {
        String cleaned = stripMetadata(line);
        Matcher xFirst = QUANTITY_X_FIRST.matcher(cleaned);
        if (xFirst.matches()) {
            return row(xFirst.group(1), xFirst.group(2));
        }
        Matcher quantityFirst = QUANTITY_FIRST.matcher(cleaned);
        if (quantityFirst.matches()) {
            return row(quantityFirst.group(1), quantityFirst.group(2));
        }
        if (!cleaned.isBlank()) {
            return new ImportedDeckRow(1, cleaned, DeckSection.OTHER);
        }
        return null;
    }

    private static ImportedDeckRow row(String quantityText, String name) {
        int quantity = Integer.parseInt(quantityText);
        return new ImportedDeckRow(quantity, cleanCardName(name), DeckSection.OTHER);
    }

    private static boolean isNamedCommanderLine(String line) {
        String normalized = line.toLowerCase(Locale.ROOT);
        return normalized.startsWith("commander:");
    }

    private static String stripMetadata(String line) {
        String cleaned = line;
        int commentIndex = cleaned.indexOf("//");
        if (commentIndex >= 0) {
            cleaned = cleaned.substring(0, commentIndex);
        }
        cleaned = cleaned.replaceFirst("^\\*+\\s*", "");
        cleaned = cleaned.replaceFirst("\\s+\\*F\\*$", "");
        cleaned = cleaned.replaceFirst("\\s+\\(foil\\)$", "");
        cleaned = cleaned.replaceFirst("\\s+\\[[A-Z0-9]{2,6}[- #]*[A-Z0-9]+]\\s*$", "");
        cleaned = cleaned.replaceFirst("\\s+\\([A-Z0-9]{2,6}\\)\\s+[A-Z0-9-]+\\s*$", "");
        return cleaned.trim();
    }

    private static String cleanCardName(String rawName) {
        return rawName.trim();
    }

    private static DeckSection sectionFromHeader(String line) {
        String normalized = line.trim()
                .replaceFirst(":$", "")
                .replaceFirst("\\s*\\(\\d+\\)$", "")
                .trim()
                .toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "commander", "commanders" -> DeckSection.COMMANDER;
            case "creature", "creatures" -> DeckSection.CREATURE;
            case "artifact", "artifacts" -> DeckSection.ARTIFACT;
            case "enchantment", "enchantments" -> DeckSection.ENCHANTMENT;
            case "instant", "instants" -> DeckSection.INSTANT;
            case "sorcery", "sorceries" -> DeckSection.SORCERY;
            case "planeswalker", "planeswalkers" -> DeckSection.PLANESWALKER;
            case "battle", "battles" -> DeckSection.BATTLE;
            case "land", "lands" -> DeckSection.LAND;
            case "other", "misc" -> DeckSection.OTHER;
            default -> null;
        };
    }

    private static boolean isIgnoredImportHeader(String line) {
        String normalized = line.trim().replaceFirst(":$", "").toLowerCase(Locale.ROOT);
        return normalized.equals("sideboard")
                || normalized.equals("maybeboard")
                || normalized.equals("tokens")
                || normalized.equals("considering");
    }
}
