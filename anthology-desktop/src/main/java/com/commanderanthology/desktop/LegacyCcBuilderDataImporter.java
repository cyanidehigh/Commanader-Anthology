package com.commanderanthology.desktop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class LegacyCcBuilderDataImporter {
    private LegacyCcBuilderDataImporter() {
    }

    static Optional<DesktopSnapshot> loadSnapshot() {
        Path dataFile = dataFile();
        if (!Files.exists(dataFile)) {
            return Optional.empty();
        }
        return new DesktopPersistence(dataFile).load();
    }

    static Path dataFile() {
        String appData = System.getenv("APPDATA");
        Path root = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), "AppData", "Roaming")
                : Path.of(appData);
        return root.resolve("Commander Analyst").resolve("commander-analyst-data.json");
    }
}
