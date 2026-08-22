package com.commanderanthology.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ScryfallBulkDataService {
    private static final String SQLITE_FILE = "scryfall-cards.sqlite";
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final Path cacheDirectory;

    ScryfallBulkDataService() {
        this(defaultCacheDirectory());
    }

    ScryfallBulkDataService(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    Path cacheDirectory() {
        return cacheDirectory;
    }

    List<ScryfallBulkDataStatus> checkForUpdates() {
        String body = requestText("https://api.scryfall.com/bulk-data");
        List<RemoteBulkItem> remote = parseRemoteBulkItems(body);
        List<LocalBulkItem> local = loadLocalManifest();
        return remote.stream()
                .map(item -> {
                    LocalBulkItem localItem = local.stream()
                            .filter(existing -> existing.type().equals(item.type()))
                            .findFirst()
                            .orElse(null);
                    boolean installed = localItem != null && Files.exists(cacheDirectory.resolve(localItem.fileName()));
                    return new ScryfallBulkDataStatus(
                            item.type(),
                            item.name(),
                            item.updatedAt(),
                            localItem == null ? null : localItem.updatedAt(),
                            item.size(),
                            installed,
                            localItem == null || !localItem.updatedAt().equals(item.updatedAt()),
                            item.downloadUri()
                    );
                })
                .sorted(Comparator.comparing(ScryfallBulkDataStatus::type))
                .toList();
    }

    List<ScryfallBulkDataStatus> installAll(Consumer<String> progress) {
        List<ScryfallBulkDataStatus> statuses = checkForUpdates();
        List<LocalBulkItem> installed = new ArrayList<>();
        for (int index = 0; index < statuses.size(); index++) {
            ScryfallBulkDataStatus status = statuses.get(index);
            progress.accept("Downloading " + (index + 1) + " / " + statuses.size() + ": " + status.name());
            String fileName = status.type() + ".json";
            download(status.downloadUri(), cacheDirectory.resolve(fileName));
            installed.add(new LocalBulkItem(status.type(), status.name(), status.remoteUpdatedAt(), fileName, status.size()));
        }
        writeManifest(installed);
        return checkForUpdates();
    }

    Optional<Path> adoptExistingSqliteCache() {
        Optional<Path> source = new ScryfallCardLookupService().sqliteCaches().stream()
                .filter(path -> !path.toAbsolutePath().startsWith(cacheDirectory.toAbsolutePath()))
                .findFirst();
        source.ifPresent(path -> {
            try {
                Files.createDirectories(cacheDirectory);
                Files.copy(path, cacheDirectory.resolve(SQLITE_FILE), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException error) {
                throw new IllegalStateException("Could not adopt existing SQLite cache", error);
            }
        });
        return source;
    }

    Path buildSqliteFromDefaultCards(Consumer<String> progress) {
        Path source = defaultCardsSource()
                .orElseThrow(() -> new IllegalStateException("No default_cards.json found in Anthology or legacy Scryfall caches."));
        Path target = cacheDirectory.resolve(SQLITE_FILE);
        Path temp = cacheDirectory.resolve(SQLITE_FILE + ".tmp");
        try {
            Files.createDirectories(cacheDirectory);
            Files.deleteIfExists(temp);
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + temp.toAbsolutePath())) {
                configureImport(connection);
                createCardSchema(connection);
                connection.setAutoCommit(false);
                importDefaultCards(source, connection, progress);
                writeSqliteMetadata(connection, source);
                connection.commit();
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            progress.accept("SQLite card cache built: " + target);
            return target;
        } catch (IOException | SQLException error) {
            throw new IllegalStateException("Could not build SQLite card cache", error);
        }
    }

    Optional<Path> defaultCardsSource() {
        Path anthology = cacheDirectory.resolve("default_cards.json");
        if (Files.exists(anthology)) {
            return Optional.of(anthology);
        }
        return new ScryfallCardLookupService().cacheStatuses().stream()
                .filter(ScryfallCacheStatus::hasDefaultCards)
                .map(status -> status.directory().resolve("default_cards.json"))
                .findFirst();
    }

    private void importDefaultCards(Path source, Connection connection, Consumer<String> progress) throws IOException, SQLException {
        String sql = """
                insert into cards(
                    id, oracle_id, name, oracle_name, normalized_name, normalized_oracle_name,
                    set_code, collector_number, collector_sort, released_at, commander_legal, details_json
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8);
             var statement = connection.prepareStatement(sql)) {
            JsonObjectStream objects = new JsonObjectStream(reader);
            int count = 0;
            String object;
            while ((object = objects.nextObject()) != null) {
                String id = jsonString(object, "id");
                String name = jsonString(object, "name");
                String setCode = jsonString(object, "set");
                String collectorNumber = jsonString(object, "collector_number");
                if (id == null || name == null || setCode == null || collectorNumber == null) {
                    continue;
                }
                String oracleName = firstNonBlank(jsonString(object, "oracle_name"), name);
                statement.setString(1, id);
                statement.setString(2, jsonString(object, "oracle_id"));
                statement.setString(3, name);
                statement.setString(4, oracleName);
                statement.setString(5, normalize(name));
                statement.setString(6, normalize(oracleName));
                statement.setString(7, setCode.toLowerCase());
                statement.setString(8, collectorNumber);
                statement.setString(9, collectorSortKey(collectorNumber));
                statement.setString(10, firstNonBlank(jsonString(object, "released_at"), ""));
                statement.setInt(11, "legal".equals(jsonString(object, "commander")) ? 1 : 0);
                statement.setString(12, detailsJson(object, id, name, oracleName, setCode, collectorNumber));
                statement.addBatch();
                count++;
                if (count % 1000 == 0) {
                    statement.executeBatch();
                    if (count % 10000 == 0) {
                        progress.accept("Indexed " + count + " Scryfall cards...");
                    }
                }
            }
            statement.executeBatch();
            progress.accept("Indexed " + count + " Scryfall cards.");
        }
    }

    private static void configureImport(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("pragma journal_mode = off");
            statement.executeUpdate("pragma synchronous = off");
        }
    }

    private static void createCardSchema(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("create table metadata(key text primary key, value text not null)");
            statement.executeUpdate("""
                    create table cards(
                        id text primary key,
                        oracle_id text,
                        name text not null,
                        oracle_name text not null,
                        normalized_name text not null,
                        normalized_oracle_name text not null,
                        set_code text not null,
                        collector_number text not null,
                        collector_sort text not null,
                        released_at text not null,
                        commander_legal integer not null,
                        details_json text not null
                    )
                    """);
            statement.executeUpdate("create index idx_cards_normalized_name on cards(normalized_name)");
            statement.executeUpdate("create index idx_cards_normalized_oracle_name on cards(normalized_oracle_name)");
            statement.executeUpdate("create index idx_cards_oracle_id_set on cards(oracle_id, set_code)");
            statement.executeUpdate("create index idx_cards_set on cards(set_code)");
        }
    }

    private static void writeSqliteMetadata(Connection connection, Path source) throws SQLException, IOException {
        try (var statement = connection.prepareStatement("insert into metadata(key, value) values (?, ?)")) {
            statement.setString(1, "source_last_modified");
            statement.setString(2, Files.getLastModifiedTime(source).toMillis() + "");
            statement.addBatch();
            statement.setString(1, "source_size");
            statement.setString(2, Files.size(source) + "");
            statement.addBatch();
            statement.executeBatch();
        }
    }

    private String requestText(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "CommanderAnthology/0.1")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Scryfall returned " + response.statusCode() + ".");
            }
            return response.body();
        } catch (IOException | InterruptedException error) {
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Could not reach Scryfall bulk-data API", error);
        }
    }

    private void download(String url, Path target) {
        try {
            Files.createDirectories(target.getParent());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "CommanderAnthology/0.1")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Scryfall returned " + response.statusCode() + " while downloading.");
            }
            Path temp = target.resolveSibling(target.getFileName() + ".tmp");
            try (InputStream input = response.body()) {
                Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException | InterruptedException error) {
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Could not download Scryfall bulk data", error);
        }
    }

    private List<LocalBulkItem> loadLocalManifest() {
        Path manifest = cacheDirectory.resolve("manifest.json");
        if (!Files.exists(manifest)) {
            return List.of();
        }
        try {
            return parseLocalManifest(Files.readString(manifest, StandardCharsets.UTF_8));
        } catch (IOException error) {
            return List.of();
        }
    }

    private void writeManifest(List<LocalBulkItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"items\":[");
        for (LocalBulkItem item : items) {
            builder.append("{")
                    .append(json("type", item.type())).append(',')
                    .append(json("name", item.name())).append(',')
                    .append(json("updatedAt", item.updatedAt())).append(',')
                    .append(json("fileName", item.fileName())).append(',')
                    .append("\"size\":").append(item.size())
                    .append("},");
        }
        if (!items.isEmpty()) {
            builder.setLength(builder.length() - 1);
        }
        builder.append("]}");
        try {
            Files.createDirectories(cacheDirectory);
            Files.writeString(cacheDirectory.resolve("manifest.json"), builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IllegalStateException("Could not write Scryfall bulk manifest", error);
        }
    }

    private static List<RemoteBulkItem> parseRemoteBulkItems(String json) {
        List<RemoteBulkItem> items = new ArrayList<>();
        Matcher object = Pattern.compile("\\{[^{}]*\"download_uri\"[^{}]*}").matcher(json);
        while (object.find()) {
            String value = object.group();
            String contentType = jsonString(value, "content_type");
            String downloadUri = jsonString(value, "download_uri");
            if (!"application/json".equals(contentType) || downloadUri == null) {
                continue;
            }
            items.add(new RemoteBulkItem(
                    jsonString(value, "type"),
                    jsonString(value, "name"),
                    jsonString(value, "updated_at"),
                    downloadUri,
                    jsonLong(value, "size")
            ));
        }
        return items;
    }

    private static List<LocalBulkItem> parseLocalManifest(String json) {
        List<LocalBulkItem> items = new ArrayList<>();
        Matcher object = Pattern.compile("\\{[^{}]*\"fileName\"[^{}]*}").matcher(json);
        while (object.find()) {
            String value = object.group();
            items.add(new LocalBulkItem(
                    jsonString(value, "type"),
                    jsonString(value, "name"),
                    jsonString(value, "updatedAt"),
                    jsonString(value, "fileName"),
                    jsonLong(value, "size")
            ));
        }
        return items;
    }

    private static String json(String key, String value) {
        return "\"" + key + "\":\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String detailsJson(String object, String id, String name, String oracleName, String setCode, String collectorNumber) {
        String imageUrl = firstNonBlank(jsonString(object, "normal"), jsonString(object, "large"));
        return "{"
                + jsonNullable("scryfallCardId", id) + ","
                + jsonNullable("oracleId", jsonString(object, "oracle_id")) + ","
                + jsonNullable("name", name) + ","
                + jsonNullable("oracleName", oracleName) + ","
                + jsonNullable("manaCost", jsonString(object, "mana_cost")) + ","
                + jsonNullable("typeLine", jsonString(object, "type_line")) + ","
                + jsonNullable("oracleText", jsonString(object, "oracle_text")) + ","
                + jsonNullable("power", jsonString(object, "power")) + ","
                + jsonNullable("toughness", jsonString(object, "toughness")) + ","
                + jsonNullable("setName", jsonString(object, "set_name")) + ","
                + jsonNullable("setCode", setCode.toUpperCase()) + ","
                + jsonNullable("collectorNumber", collectorNumber) + ","
                + jsonNullable("rarity", jsonString(object, "rarity")) + ","
                + jsonNullable("imageUrl", imageUrl)
                + "}";
    }

    private static String jsonNullable(String key, String value) {
        return value == null ? "\"" + key + "\":null" : json(key, value);
    }

    private static String jsonString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json);
        return matcher.find() ? matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : null;
    }

    private static long jsonLong(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }

    private static String collectorSortKey(String collectorNumber) {
        String digits = collectorNumber == null ? "" : collectorNumber.replaceAll("\\D.*$", "");
        if (digits.isBlank()) {
            return collectorNumber == null ? "" : collectorNumber;
        }
        return "0".repeat(Math.max(0, 12 - digits.length())) + digits + collectorNumber.replaceFirst("^\\d+", "");
    }

    private static Path defaultCacheDirectory() {
        String appData = System.getenv("APPDATA");
        Path root = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), "AppData", "Roaming")
                : Path.of(appData);
        return root.resolve("Commander Anthology").resolve("scryfall-bulk-data");
    }

    private record RemoteBulkItem(String type, String name, String updatedAt, String downloadUri, long size) {
    }

    private record LocalBulkItem(String type, String name, String updatedAt, String fileName, long size) {
    }

    private static final class JsonObjectStream {
        private final Reader reader;
        private boolean inString;
        private boolean escaping;
        private int depth;

        JsonObjectStream(Reader reader) {
            this.reader = reader;
        }

        String nextObject() throws IOException {
            StringBuilder builder = null;
            int value;
            while ((value = reader.read()) >= 0) {
                char character = (char) value;
                if (builder == null) {
                    if (character == '{') {
                        builder = new StringBuilder();
                        builder.append(character);
                        depth = 1;
                        inString = false;
                        escaping = false;
                    }
                    continue;
                }

                builder.append(character);
                if (escaping) {
                    escaping = false;
                    continue;
                }
                if (character == '\\' && inString) {
                    escaping = true;
                    continue;
                }
                if (character == '"') {
                    inString = !inString;
                    continue;
                }
                if (!inString && character == '{') {
                    depth++;
                } else if (!inString && character == '}') {
                    depth--;
                    if (depth == 0) {
                        return builder.toString();
                    }
                }
            }
            return null;
        }
    }
}
