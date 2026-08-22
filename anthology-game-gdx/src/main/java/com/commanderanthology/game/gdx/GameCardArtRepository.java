package com.commanderanthology.game.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GameCardArtRepository {
    private static final String SQLITE_FILE = "scryfall-cards.sqlite";

    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, CompletableFuture<Optional<Path>>> pending = new HashMap<>();
    private final Set<String> missing = new HashSet<>();
    private final Path imageCacheDirectory;

    GameCardArtRepository() {
        this.imageCacheDirectory = appDataRoot()
                .resolve("Commander Anthology")
                .resolve("card-images");
    }

    Texture textureFor(String cardName) {
        return textureFor(cardName, ImageUse.TABLE);
    }

    Texture previewTextureFor(String cardName) {
        return textureFor(cardName, ImageUse.PREVIEW);
    }

    private Texture textureFor(String cardName, ImageUse use) {
        String key = normalize(cardName);
        String cacheKey = use.name() + ":" + key;
        if (key.isBlank() || missing.contains(cacheKey)) {
            return null;
        }
        Texture existing = textures.get(cacheKey);
        if (existing != null) {
            return existing;
        }

        CompletableFuture<Optional<Path>> future = pending.get(cacheKey);
        if (future == null) {
            pending.put(cacheKey, CompletableFuture.supplyAsync(() -> resolveImagePath(cardName, use)));
            return null;
        }
        if (!future.isDone()) {
            return null;
        }

        pending.remove(cacheKey);
        Optional<Path> path = future.join();
        if (path.isEmpty()) {
            missing.add(cacheKey);
            return null;
        }
        Texture texture = new Texture(Gdx.files.absolute(path.get().toString()));
        textures.put(cacheKey, texture);
        return texture;
    }

    void dispose() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
    }

    private Optional<Path> resolveImagePath(String cardName, ImageUse use) {
        Optional<CardArtRecord> record = lookupCardArt(cardName);
        if (record.isEmpty()) {
            return Optional.empty();
        }
        Path target = cachePath(record.get().scryfallCardId(), use);
        if (Files.exists(target)) {
            return Optional.of(target);
        }
        String imageUrl = use == ImageUse.PREVIEW
                ? previewImageUrl(record.get().imageUrl())
                : record.get().imageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            Files.createDirectories(imageCacheDirectory);
            try (InputStream stream = URI.create(imageUrl).toURL().openStream()) {
                Files.copy(stream, target);
            }
            return Optional.of(target);
        } catch (IOException | IllegalArgumentException error) {
            return Optional.empty();
        }
    }

    private Optional<CardArtRecord> lookupCardArt(String cardName) {
        String normalized = normalize(cardName);
        if (normalized.isBlank()) {
            return Optional.empty();
        }
        String sql = """
                select id, details_json
                from cards
                where normalized_name = ? or normalized_oracle_name = ?
                order by commander_legal desc, released_at asc, collector_sort asc
                limit 1
                """;
        for (Path sqliteCache : sqliteCaches()) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteCache.toAbsolutePath());
                 var statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalized);
                statement.setString(2, normalized);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        String id = results.getString("id");
                        String imageUrl = jsonValue(results.getString("details_json"), "imageUrl");
                        return Optional.of(new CardArtRecord(id, imageUrl));
                    }
                }
            } catch (SQLException error) {
                // Try the next known local cache.
            }
        }
        return Optional.empty();
    }

    private Path cachePath(String scryfallCardId, ImageUse use) {
        String clean = scryfallCardId.replaceAll("[^A-Za-z0-9_-]", "_");
        return use == ImageUse.PREVIEW
                ? imageCacheDirectory.resolve(clean + "-preview.png")
                : imageCacheDirectory.resolve(clean + ".jpg");
    }

    private static String previewImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return imageUrl
                .replace("/normal/", "/png/")
                .replace("/large/", "/png/")
                .replace("/small/", "/png/")
                .replace(".jpg", ".png");
    }

    private static ArrayList<Path> sqliteCaches() {
        ArrayList<Path> candidates = new ArrayList<>();
        Path appData = appDataRoot();
        candidates.add(appData.resolve("Commander Anthology").resolve("scryfall-bulk-data").resolve(SQLITE_FILE));
        candidates.add(appData.resolve("Commander Analyst").resolve("scryfall-bulk-data").resolve(SQLITE_FILE));
        Path cwd = Path.of("").toAbsolutePath();
        candidates.add(cwd.resolve("Commander analyst").resolve("data").resolve("scryfall-bulk-data").resolve(SQLITE_FILE));
        candidates.add(cwd.getParent() == null
                ? cwd.resolve("Commander analyst").resolve("data").resolve("scryfall-bulk-data").resolve(SQLITE_FILE)
                : cwd.getParent().resolve("Commander analyst").resolve("data").resolve("scryfall-bulk-data").resolve(SQLITE_FILE));
        ArrayList<Path> existing = new ArrayList<>();
        for (Path candidate : candidates) {
            Path absolute = candidate.toAbsolutePath();
            if (Files.exists(absolute) && !existing.contains(absolute)) {
                existing.add(absolute);
            }
        }
        return existing;
    }

    private static Path appDataRoot() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            return Path.of(System.getProperty("user.home"), "AppData", "Roaming");
        }
        return Path.of(appData);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static String jsonValue(String json, String key) {
        if (json == null || json.isBlank()) {
            return null;
        }
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(null|\"((?:\\\\.|[^\"])*)\")").matcher(json);
        if (!matcher.find() || "null".equals(matcher.group(1))) {
            return null;
        }
        return matcher.group(2)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private record CardArtRecord(String scryfallCardId, String imageUrl) {
    }

    private enum ImageUse {
        TABLE,
        PREVIEW
    }
}
