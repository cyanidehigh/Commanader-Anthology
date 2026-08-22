package com.commanderanthology.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ScryfallCardLookupService {
    private static final String SQLITE_FILE = "scryfall-cards.sqlite";
    private static final String SEED_RESOURCE = "/com/commanderanthology/desktop/card-lookup-seed.tsv";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    List<ScryfallCardSelection> lookupCardOptions(String cardName, String setCode) {
        String cleanName = cardName == null ? "" : cardName.trim();
        if (cleanName.isEmpty()) {
            return List.of();
        }
        String cleanSetCode = blankToNull(setCode);
        for (String lookupName : lookupNameVariants(cleanName)) {
            List<ScryfallCardSelection> sqliteMatches = lookupSqlite(lookupName, cleanSetCode);
            if (!sqliteMatches.isEmpty()) {
                return sqliteMatches;
            }
            List<ScryfallCardSelection> seedMatches = lookupSeed(lookupName, cleanSetCode);
            if (!seedMatches.isEmpty()) {
                return seedMatches;
            }
        }
        return lookupApi(cleanName, cleanSetCode).map(List::of).orElseGet(List::of);
    }

    List<ScryfallCacheStatus> cacheStatuses() {
        List<Path> directories = new ArrayList<>();
        appDataRoot().ifPresent(root -> {
            directories.add(root.resolve("Commander Anthology").resolve("scryfall-bulk-data"));
            directories.add(root.resolve("Commander Analyst").resolve("scryfall-bulk-data"));
        });
        for (Path root : currentDirectoryRoots()) {
            directories.add(root.resolve("Commander analyst").resolve("data").resolve("scryfall-bulk-data"));
        }
        return directories.stream()
                .map(Path::toAbsolutePath)
                .distinct()
                .filter(Files::exists)
                .map(ScryfallCardLookupService::cacheStatus)
                .toList();
    }

    private static ScryfallCacheStatus cacheStatus(Path directory) {
        Path defaultCards = directory.resolve("default_cards.json");
        Path sqlite = directory.resolve(SQLITE_FILE);
        return new ScryfallCacheStatus(
                directory,
                Files.exists(defaultCards),
                Files.exists(directory.resolve("oracle_cards.json")),
                Files.exists(directory.resolve("rulings.json")),
                Files.exists(directory.resolve("manifest.json")),
                Files.exists(sqlite),
                size(defaultCards),
                size(sqlite)
        );
    }

    Optional<ScryfallCardSelection> lookupPreferredCard(String cardName, String setCode) {
        return lookupCardOptions(cardName, setCode).stream().findFirst();
    }

    CommanderValidation validateCommander(String cardName) {
        String cleanName = cardName == null ? "" : cardName.trim();
        if (cleanName.isEmpty()) {
            return CommanderValidation.invalid(cleanName, "Commander is required.");
        }
        Optional<CommanderCandidate> candidate = lookupCommanderCandidate(cleanName);
        if (candidate.isEmpty()) {
            return CommanderValidation.invalid(cleanName, "Commander was not found in the card lookup.");
        }
        CommanderCandidate commander = candidate.get();
        if (!commander.commanderLegal()) {
            return CommanderValidation.invalid(commander.name(), commander.name() + " is not legal in Commander.");
        }
        if (!commander.canBeCommander()) {
            return CommanderValidation.invalid(commander.name(), commander.name() + " cannot be your commander.");
        }
        return CommanderValidation.valid(commander.oracleName());
    }

    Optional<ScryfallCardSelection> lookupCardById(String scryfallCardId) {
        String cleanId = scryfallCardId == null ? "" : scryfallCardId.trim();
        if (cleanId.isEmpty()) {
            return Optional.empty();
        }
        for (Path sqliteCache : sqliteCaches()) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteCache.toAbsolutePath());
                 var statement = connection.prepareStatement("""
                         select id, oracle_id, name, oracle_name, set_code, collector_number
                         from cards
                         where id = ?
                         limit 1
                         """)) {
                statement.setString(1, cleanId);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        return Optional.of(selection(results));
                    }
                }
            } catch (SQLException error) {
                // Try the next known cache before falling back.
            }
        }
        return lookupSeedById(cleanId);
    }

    private Optional<CommanderCandidate> lookupCommanderCandidate(String cardName) {
        for (String lookupName : lookupNameVariants(cardName)) {
            Optional<CommanderCandidate> sqlite = lookupCommanderCandidateSqlite(lookupName);
            if (sqlite.isPresent()) {
                return sqlite;
            }
        }
        return lookupCommanderCandidateApi(cardName);
    }

    private Optional<CommanderCandidate> lookupCommanderCandidateSqlite(String cardName) {
        String normalizedName = normalize(cardName);
        String sql = """
                select name, oracle_name, commander_legal, details_json
                from cards
                where normalized_name = ? or normalized_oracle_name = ?
                order by commander_legal desc, released_at asc, collector_sort asc
                limit 1
                """;
        for (Path sqliteCache : sqliteCaches()) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteCache.toAbsolutePath());
                 var statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizedName);
                statement.setString(2, normalizedName);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        ScryfallCardDetails details = parseDetails(results.getString("details_json"));
                        return Optional.of(new CommanderCandidate(
                                results.getString("name"),
                                results.getString("oracle_name"),
                                results.getInt("commander_legal") == 1,
                                details.typeLine(),
                                details.oracleText(),
                                jsonValue(results.getString("details_json"), "power"),
                                jsonValue(results.getString("details_json"), "toughness")
                        ));
                    }
                }
            } catch (SQLException error) {
                // Try the next local cache before falling back.
            }
        }
        return Optional.empty();
    }

    private Optional<CommanderCandidate> lookupCommanderCandidateApi(String cardName) {
        String cleanName = cardName == null ? "" : cardName.trim();
        if (cleanName.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> exact = requestApi("https://api.scryfall.com/cards/named?exact=" + urlEncode(cleanName));
        Optional<String> body = exact.or(() -> requestApi("https://api.scryfall.com/cards/named?fuzzy=" + urlEncode(cleanName)));
        return body.map(json -> new CommanderCandidate(
                jsonValue(json, "name"),
                firstNonBlank(jsonValue(json, "oracle_name"), jsonValue(json, "name")),
                "legal".equals(jsonValue(json, "commander")),
                jsonValue(json, "type_line"),
                jsonValue(json, "oracle_text"),
                jsonValue(json, "power"),
                jsonValue(json, "toughness")
        ));
    }

    Optional<ScryfallCardSelection> lookupApi(String cardName, String setCode) {
        String cleanName = cardName == null ? "" : cardName.trim();
        if (cleanName.isEmpty()) {
            return Optional.empty();
        }
        String setQuery = setCode == null || setCode.isBlank() ? "" : "&set=" + urlEncode(setCode.trim().toLowerCase(Locale.ROOT));
        Optional<String> exact = requestApi("https://api.scryfall.com/cards/named?exact=" + urlEncode(cleanName) + setQuery);
        Optional<String> body = exact.or(() -> requestApi("https://api.scryfall.com/cards/named?fuzzy=" + urlEncode(cleanName) + setQuery));
        return body.map(this::selectionFromApiJson);
    }

    List<ScryfallCardSelection> searchCards(String query, int limit) {
        String cleanQuery = query == null ? "" : query.trim();
        if (cleanQuery.isEmpty() || limit <= 0) {
            return List.of();
        }
        String sql = """
                select id, oracle_id, name, oracle_name, set_code, collector_number
                from cards
                where normalized_name like ? or normalized_oracle_name like ?
                order by commander_legal desc, oracle_name asc, released_at asc, collector_sort asc
                limit ?
                """;
        String like = "%" + normalize(cleanQuery) + "%";
        for (Path sqliteCache : sqliteCaches()) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteCache.toAbsolutePath());
                 var statement = connection.prepareStatement(sql)) {
                statement.setString(1, like);
                statement.setString(2, like);
                statement.setInt(3, limit);
                List<ScryfallCardSelection> matches = new ArrayList<>();
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        matches.add(selection(results));
                    }
                }
                if (!matches.isEmpty()) {
                    return matches.stream().distinct().toList();
                }
            } catch (SQLException error) {
                // Try next local cache.
            }
        }
        return seedSelections().stream()
                .filter(selection -> normalize(selection.printingName()).contains(normalize(cleanQuery))
                        || normalize(selection.oracleName()).contains(normalize(cleanQuery)))
                .limit(limit)
                .toList();
    }

    Optional<ScryfallCardDetails> cardDetails(String scryfallCardId) {
        String cleanId = scryfallCardId == null ? "" : scryfallCardId.trim();
        if (cleanId.isEmpty()) {
            return Optional.empty();
        }
        for (Path sqliteCache : sqliteCaches()) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteCache.toAbsolutePath());
                 var statement = connection.prepareStatement("""
                         select details_json
                         from cards
                         where id = ?
                         limit 1
                         """)) {
                statement.setString(1, cleanId);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        return Optional.of(parseDetails(results.getString("details_json")));
                    }
                }
            } catch (SQLException error) {
                // Try next local cache.
            }
        }
        return lookupCardById(cleanId).map(selection -> new ScryfallCardDetails(
                selection.scryfallCardId(),
                selection.oracleId(),
                selection.printingName(),
                selection.oracleName(),
                null,
                null,
                null,
                null,
                selection.setCode(),
                selection.collectorNumber(),
                null,
                null
        ));
    }

    Optional<ScryfallCardDetails> cardDetailsForIdentity(String oracleId, String cardName) {
        String cleanOracleId = oracleId == null ? "" : oracleId.trim();
        if (!cleanOracleId.isEmpty()) {
            for (Path sqliteCache : sqliteCaches()) {
                try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteCache.toAbsolutePath());
                     var statement = connection.prepareStatement("""
                             select details_json
                             from cards
                             where oracle_id = ?
                             order by commander_legal desc, released_at asc, collector_sort asc
                             limit 1
                             """)) {
                    statement.setString(1, cleanOracleId);
                    try (ResultSet results = statement.executeQuery()) {
                        if (results.next()) {
                            return Optional.of(parseDetails(results.getString("details_json")));
                        }
                    }
                } catch (SQLException error) {
                    // Try next local cache before falling back.
                }
            }
        }
        return lookupPreferredCard(cardName, null).flatMap(selection -> cardDetails(selection.scryfallCardId()));
    }

    private List<ScryfallCardSelection> lookupSqlite(String cardName, String setCode) {
        List<Path> sqliteCaches = sqliteCaches();
        String normalizedName = normalize(cardName);
        String cleanSetCode = setCode == null ? null : setCode.toLowerCase(Locale.ROOT);
        String sql = """
                select id, oracle_id, name, oracle_name, set_code, collector_number, commander_legal, released_at, collector_sort
                from cards
                where (normalized_name = ? or normalized_oracle_name = ?)
                """;
        if (cleanSetCode != null) {
            sql += " and set_code = ?";
        }
        sql += " order by commander_legal desc, released_at asc, collector_sort asc";

        for (Path sqliteCache : sqliteCaches) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteCache.toAbsolutePath());
                 var statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizedName);
                statement.setString(2, normalizedName);
                if (cleanSetCode != null) {
                    statement.setString(3, cleanSetCode);
                }
                List<ScryfallCardSelection> matches = new ArrayList<>();
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        matches.add(selection(results));
                    }
                }
                if (!matches.isEmpty()) {
                    return matches.stream()
                            .filter(selection -> selection.scryfallCardId() != null && !selection.scryfallCardId().isBlank())
                            .distinct()
                            .toList();
                }
            } catch (SQLException error) {
                // Try the next known cache before falling back to the bundled seed.
            }
        }
        return List.of();
    }

    private Optional<String> requestApi(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "CommanderAnthology/0.1")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Optional.of(response.body());
            }
        } catch (IOException | InterruptedException error) {
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return Optional.empty();
    }

    private ScryfallCardSelection selectionFromApiJson(String json) {
        String name = jsonValue(json, "name");
        String oracleName = jsonValue(json, "oracle_name");
        return new ScryfallCardSelection(
                jsonValue(json, "id"),
                jsonValue(json, "oracle_id"),
                oracleName == null ? name : oracleName,
                name,
                upper(jsonValue(json, "set")),
                jsonValue(json, "collector_number")
        );
    }

    List<Path> sqliteCaches() {
        List<Path> candidates = new ArrayList<>();
        appDataRoot().ifPresent(root -> {
            candidates.add(root.resolve("Commander Anthology").resolve("scryfall-bulk-data").resolve(SQLITE_FILE));
            candidates.add(root.resolve("Commander Analyst").resolve("scryfall-bulk-data").resolve(SQLITE_FILE));
        });
        for (Path root : currentDirectoryRoots()) {
            candidates.add(root.resolve("Commander analyst").resolve("data").resolve("scryfall-bulk-data").resolve(SQLITE_FILE));
        }
        return candidates.stream()
                .map(Path::toAbsolutePath)
                .filter(Files::exists)
                .distinct()
                .toList();
    }

    private ScryfallCardSelection selection(ResultSet results) throws SQLException {
        return new ScryfallCardSelection(
                results.getString("id"),
                results.getString("oracle_id"),
                results.getString("oracle_name"),
                results.getString("name"),
                results.getString("set_code").toUpperCase(Locale.ROOT),
                results.getString("collector_number")
        );
    }

    private List<ScryfallCardSelection> lookupSeed(String cardName, String setCode) {
        InputStream stream = ScryfallCardLookupService.class.getResourceAsStream(SEED_RESOURCE);
        if (stream == null) {
            return List.of();
        }
        String normalizedName = normalize(cardName);
        String cleanSetCode = setCode == null ? null : setCode.toLowerCase(Locale.ROOT);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .map(line -> line.split("\t", -1))
                    .filter(parts -> parts.length >= 6)
                    .map(parts -> new ScryfallCardSelection(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]))
                    .filter(selection -> normalize(selection.printingName()).equals(normalizedName)
                            || normalize(selection.oracleName()).equals(normalizedName))
                    .filter(selection -> cleanSetCode == null || selection.setCode().equalsIgnoreCase(cleanSetCode))
                    .sorted(Comparator.comparing(ScryfallCardSelection::setCode).thenComparing(ScryfallCardSelection::collectorNumber))
                    .toList();
        } catch (IOException error) {
            return List.of();
        }
    }

    private Optional<ScryfallCardSelection> lookupSeedById(String scryfallCardId) {
        return seedSelections().stream()
                .filter(selection -> selection.scryfallCardId().equalsIgnoreCase(scryfallCardId))
                .findFirst();
    }

    private List<ScryfallCardSelection> seedSelections() {
        InputStream stream = ScryfallCardLookupService.class.getResourceAsStream(SEED_RESOURCE);
        if (stream == null) {
            return List.of();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .map(line -> line.split("\t", -1))
                    .filter(parts -> parts.length >= 6)
                    .map(parts -> new ScryfallCardSelection(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]))
                    .toList();
        } catch (IOException error) {
            return List.of();
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static ScryfallCardDetails parseDetails(String json) {
        return new ScryfallCardDetails(
                jsonValue(json, "scryfallCardId"),
                jsonValue(json, "oracleId"),
                jsonValue(json, "name"),
                jsonValue(json, "oracleName"),
                jsonValue(json, "manaCost"),
                jsonValue(json, "typeLine"),
                jsonValue(json, "oracleText"),
                jsonValue(json, "setName"),
                jsonValue(json, "setCode"),
                jsonValue(json, "collectorNumber"),
                jsonValue(json, "rarity"),
                jsonValue(json, "imageUrl")
        );
    }

    private static String jsonValue(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(null|\"((?:\\\\.|[^\"])*)\")").matcher(json);
        if (!matcher.find() || "null".equals(matcher.group(1))) {
            return null;
        }
        return matcher.group(2)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static List<String> lookupNameVariants(String cardName) {
        Set<String> variants = new LinkedHashSet<>();
        addVariant(variants, cardName);

        String withoutDecorations = cardName
                .replaceAll("\\s+\\*[^*]+\\*\\s*$", "")
                .replace("â˜…", "")
                .replace("★", "")
                .replaceAll("\\s+", " ")
                .trim();
        addVariant(variants, withoutDecorations);

        String withoutBracketText = withoutDecorations
                .replaceAll("\\s*\\[[^]]+]\\s*$", "")
                .replaceAll("\\s*\\([^)]+\\)\\s*$", "")
                .trim();
        addVariant(variants, withoutBracketText);

        addVariant(variants, withoutBracketText.replaceAll("\\s+[A-Za-z0-9]{2,5}-\\d+[A-Za-z]?$", ""));
        addVariant(variants, withoutBracketText.replaceAll("\\s+\\d+[A-Za-z]?$", ""));
        addVariant(variants, withoutBracketText.replaceAll("\\s+[A-Za-z]{1,5}\\d+[A-Za-z]?$", ""));
        return variants.stream().toList();
    }

    private static void addVariant(Set<String> variants, String value) {
        if (value != null && !value.isBlank()) {
            variants.add(value.trim());
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String upper(String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }

    private record CommanderCandidate(
            String name,
            String oracleName,
            boolean commanderLegal,
            String typeLine,
            String oracleText,
            String power,
            String toughness
    ) {
        boolean canBeCommander() {
            return CommanderLegalityRules.canBeCommander(typeLine, oracleText, power, toughness);
        }
    }

    private static long size(Path path) {
        try {
            return Files.exists(path) ? Files.size(path) : 0L;
        } catch (IOException error) {
            return 0L;
        }
    }

    private static Optional<Path> appDataRoot() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Path.of(appData));
    }

    private static List<Path> currentDirectoryRoots() {
        List<Path> roots = new ArrayList<>();
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            roots.add(current);
            current = current.getParent();
        }
        return roots;
    }
}
