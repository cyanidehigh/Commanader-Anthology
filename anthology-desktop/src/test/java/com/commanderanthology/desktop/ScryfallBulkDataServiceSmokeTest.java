package com.commanderanthology.desktop;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ScryfallBulkDataServiceSmokeTest {
    private ScryfallBulkDataServiceSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-bulk-service-test");
        ScryfallBulkDataService service = new ScryfallBulkDataService(tempDir);
        Path adopted = service.adoptExistingSqliteCache().orElseThrow();
        require(Files.exists(tempDir.resolve("scryfall-cards.sqlite")), "adopted sqlite exists");
        require(Files.size(tempDir.resolve("scryfall-cards.sqlite")) > 1000, "adopted sqlite size");
        System.out.println("Scryfall bulk data service smoke test passed: adopted " + adopted);
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
