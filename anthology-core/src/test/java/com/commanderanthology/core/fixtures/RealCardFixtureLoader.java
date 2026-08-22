package com.commanderanthology.core.fixtures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class RealCardFixtureLoader {
    private static final String KEYWORD_FIXTURE_RESOURCE = "real-card-fixtures/keyword-cards.psv";

    private RealCardFixtureLoader() {
    }

    public static List<RealCardFixture> loadKeywordFixtures() {
        return loadResource(KEYWORD_FIXTURE_RESOURCE);
    }

    public static List<RealCardFixture> load(Path path) {
        try {
            return parseLines(Files.readAllLines(path, StandardCharsets.UTF_8), path.toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load real card fixture file: " + path, exception);
        }
    }

    private static List<RealCardFixture> loadResource(String resourcePath) {
        ClassLoader classLoader = RealCardFixtureLoader.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Missing real card fixture resource: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return parseLines(reader.lines().toList(), resourcePath);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load real card fixture resource: " + resourcePath, exception);
        }
    }

    private static List<RealCardFixture> parseLines(List<String> lines, String sourceName) {
        if (lines.size() < 2) {
            throw new IllegalStateException("Real card fixture file is empty: " + sourceName);
        }
        List<RealCardFixture> fixtures = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length != 9) {
                throw new IllegalStateException(
                        "Expected 9 fields in real card fixture line " + (index + 1) + " but found " + parts.length
                );
            }
            fixtures.add(new RealCardFixture(
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    parts[4].replace("\\n", "\n"),
                    splitKeywords(parts[5]),
                    optionalField(parts[6]),
                    optionalField(parts[7]),
                    parts[8]
            ));
        }
        return List.copyOf(fixtures);
    }

    public static RealCardFixture requireByName(List<RealCardFixture> fixtures, String name) {
        return fixtures.stream()
                .filter(fixture -> fixture.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing real card fixture: " + name));
    }

    private static List<String> splitKeywords(String rawValue) {
        if (rawValue.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rawValue.split(";"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private static Optional<String> optionalField(String value) {
        if (value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
