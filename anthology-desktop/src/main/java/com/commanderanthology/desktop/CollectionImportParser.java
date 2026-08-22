package com.commanderanthology.desktop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CollectionImportParser {
    private static final Pattern QUANTITY_FIRST = Pattern.compile("^(\\d+)\\s*x?\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLLECTOR_SUFFIX = Pattern.compile("(?:^|\\s)#([^\\s]+)\\s*$");
    private static final Pattern SET_SUFFIX = Pattern.compile("\\(([A-Za-z0-9]{2,8})\\)\\s*$");

    private CollectionImportParser() {
    }

    static CollectionImportParseResult parse(String rawText) {
        if (looksLikeCsv(rawText)) {
            return parseCsv(rawText);
        }
        List<ImportedCollectionRow> rows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String[] lines = rawText == null ? new String[0] : rawText.split("\\R");
        for (int index = 0; index < lines.length; index++) {
            int lineNumber = index + 1;
            String line = lines[index].trim();
            if (line.isBlank() || line.startsWith("//") || line.startsWith("#") || looksLikeHeader(line)) {
                continue;
            }
            ImportedCollectionRow row = parseLine(line);
            if (row == null) {
                warnings.add("Line " + lineNumber + ": could not parse '" + line + "'");
            } else {
                rows.add(row);
            }
        }
        return new CollectionImportParseResult(rows, warnings);
    }

    private static ImportedCollectionRow parseLine(String line) {
        Matcher match = QUANTITY_FIRST.matcher(line);
        if (!match.matches()) {
            return null;
        }
        int quantity = Integer.parseInt(match.group(1));
        String body = match.group(2).trim();
        FoilStrip foilStrip = stripFoilMarker(body);
        body = foilStrip.text();

        String collectorNumber = null;
        Matcher collector = COLLECTOR_SUFFIX.matcher(body);
        if (collector.find()) {
            collectorNumber = collector.group(1);
            body = body.substring(0, collector.start()).trim();
        }

        String setCode = null;
        Matcher set = SET_SUFFIX.matcher(body);
        if (set.find()) {
            setCode = set.group(1).toUpperCase(Locale.ROOT);
            body = body.substring(0, set.start()).trim();
        }

        if (body.isBlank()) {
            return null;
        }
        return new ImportedCollectionRow(quantity, body, setCode, collectorNumber, null, foilStrip.foil());
    }

    private static CollectionImportParseResult parseCsv(String rawText) {
        List<List<String>> records = parseCsvRecords(rawText);
        if (records.isEmpty()) {
            return new CollectionImportParseResult(List.of(), List.of());
        }
        Map<String, Integer> headers = new HashMap<>();
        List<String> first = records.get(0);
        for (int index = 0; index < first.size(); index++) {
            headers.put(normalizedCsvHeader(first.get(index)), index);
        }

        List<ImportedCollectionRow> rows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (int index = 1; index < records.size(); index++) {
            int lineNumber = index + 1;
            List<String> record = records.get(index);
            String cardName = field(record, headers, "Card Name");
            Integer quantity = integer(field(record, headers, "Quantity"));
            if (cardName == null || quantity == null || quantity <= 0) {
                warnings.add("Line " + lineNumber + ": missing card name or quantity");
                continue;
            }
            rows.add(new ImportedCollectionRow(
                    quantity,
                    cardName,
                    field(record, headers, "Set Code"),
                    field(record, headers, "Collector Number"),
                    field(record, headers, "Scryfall ID"),
                    isFoilFinish(field(record, headers, "Finish"))
            ));
        }
        return new CollectionImportParseResult(rows, warnings);
    }

    private static String field(List<String> record, Map<String, Integer> headers, String name) {
        Integer index = headers.get(normalizedCsvHeader(name));
        if (index == null || index >= record.size()) {
            return null;
        }
        String value = record.get(index).trim();
        return value.isBlank() ? null : value;
    }

    private static List<List<String>> parseCsvRecords(String rawText) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < rawText.length(); index++) {
            char value = rawText.charAt(index);
            if (value == '"' && inQuotes && index + 1 < rawText.length() && rawText.charAt(index + 1) == '"') {
                field.append('"');
                index++;
            } else if (value == '"') {
                inQuotes = !inQuotes;
            } else if (value == ',' && !inQuotes) {
                record.add(field.toString());
                field.setLength(0);
            } else if ((value == '\n' || value == '\r') && !inQuotes) {
                if (value == '\r' && index + 1 < rawText.length() && rawText.charAt(index + 1) == '\n') {
                    index++;
                }
                record.add(field.toString());
                field.setLength(0);
                if (record.stream().anyMatch(text -> !text.isBlank())) {
                    records.add(List.copyOf(record));
                }
                record.clear();
            } else {
                field.append(value);
            }
        }
        if (!field.isEmpty() || !record.isEmpty()) {
            record.add(field.toString());
            if (record.stream().anyMatch(text -> !text.isBlank())) {
                records.add(List.copyOf(record));
            }
        }
        return records;
    }

    private static boolean looksLikeCsv(String rawText) {
        if (rawText == null) {
            return false;
        }
        String firstLine = rawText.lines().filter(line -> !line.isBlank()).findFirst().orElse("");
        List<List<String>> records = parseCsvRecords(firstLine);
        if (records.isEmpty()) {
            return false;
        }
        List<String> headers = records.get(0).stream().map(CollectionImportParser::normalizedCsvHeader).toList();
        return headers.contains("cardname") && headers.contains("quantity");
    }

    private static boolean looksLikeHeader(String line) {
        String normalized = line.trim().replaceFirst(":$", "").toLowerCase(Locale.ROOT);
        return normalized.equals("cards")
                || normalized.equals("collection")
                || normalized.equals("inventory")
                || normalized.equals("main")
                || normalized.equals("owned");
    }

    private static FoilStrip stripFoilMarker(String value) {
        String stripped = value.trim();
        boolean foil = false;
        String[] patterns = {
                "\\s*\\*F\\*\\s*$",
                "\\s*\\[F]\\s*$",
                "\\s*\\(F\\)\\s*$",
                "\\s+foil\\s*$",
                "^foil\\s+"
        };
        for (String pattern : patterns) {
            String replacement = stripped.replaceFirst("(?i)" + pattern, " ").trim();
            if (!replacement.equals(stripped)) {
                stripped = replacement;
                foil = true;
            }
        }
        return new FoilStrip(stripped, foil);
    }

    private static boolean isFoilFinish(String value) {
        if (value == null) {
            return false;
        }
        String finish = value.trim().toLowerCase(Locale.ROOT);
        return finish.equals("foil") || finish.equals("etched") || finish.equals("foil etched");
    }

    private static String normalizedCsvHeader(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static Integer integer(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private record FoilStrip(String text, boolean foil) {
    }
}
