package com.commanderanthology.desktop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

final class LegacyTestDeckSeeder {
    private LegacyTestDeckSeeder() {
    }

    static int seedAvailableDecks(DesktopAppState state) {
        Path importDecksPath = findImportDecksPath();
        if (importDecksPath == null) {
            return 0;
        }

        try {
            List<Path> deckFiles = Files.list(importDecksPath)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".txt"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .toList();
            int imported = 0;
            for (Path deckFile : deckFiles) {
                String deckName = deckNameFromFile(deckFile);
                if (state.hasDeckNamed(deckName)) {
                    continue;
                }
                String rawText = Files.readString(deckFile, StandardCharsets.UTF_8);
                DeckImportParseResult parsed = DeckImportParser.parse(rawText);
                state.createImportedDeck(deckName, deckName, parsed.rows());
                imported++;
            }
            return imported;
        } catch (IOException error) {
            System.err.println("Could not seed legacy test decks: " + error.getMessage());
            return 0;
        }
    }

    private static Path findImportDecksPath() {
        Path cursor = Path.of("").toAbsolutePath();
        while (cursor != null) {
            Path candidate = cursor.resolve(Path.of("Commander-Sim", "PROD", "import_decks"));
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            cursor = cursor.getParent();
        }
        return null;
    }

    private static String deckNameFromFile(Path deckFile) {
        String fileName = deckFile.getFileName().toString();
        String withoutExtension = fileName.replaceFirst("\\.txt$", "");
        return withoutExtension.replace('_', '\'');
    }

}
