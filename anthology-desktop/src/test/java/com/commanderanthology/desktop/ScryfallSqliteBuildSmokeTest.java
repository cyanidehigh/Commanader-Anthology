package com.commanderanthology.desktop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

public final class ScryfallSqliteBuildSmokeTest {
    private ScryfallSqliteBuildSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-sqlite-build-test");
        Files.writeString(tempDir.resolve("default_cards.json"), """
                [
                  {
                    "id":"c4300d24-1cae-4dd5-be7e-38cc677cf5bd",
                    "oracle_id":"6ad8011d-3471-4369-9d68-b264cc027487",
                    "name":"Sol Ring",
                    "oracle_name":"Sol Ring",
                    "mana_cost":"{1}",
                    "type_line":"Artifact",
                    "oracle_text":"{T}: Add {C}{C}.",
                    "set":"lea",
                    "set_name":"Limited Edition Alpha",
                    "collector_number":"269",
                    "rarity":"uncommon",
                    "released_at":"1993-08-05",
                    "image_uris":{"normal":"https://cards.scryfall.io/normal/front/example.jpg"},
                    "legalities":{"commander":"legal"}
                  }
                ]
                """);

        ScryfallBulkDataService service = new ScryfallBulkDataService(tempDir);
        Path sqlite = service.buildSqliteFromDefaultCards(message -> { });
        require(Files.exists(sqlite), "sqlite exists");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + sqlite.toAbsolutePath());
             var statement = connection.prepareStatement("select oracle_id, details_json from cards where normalized_name = ?")) {
            statement.setString(1, "sol ring");
            try (var results = statement.executeQuery()) {
                require(results.next(), "row exists");
                require("6ad8011d-3471-4369-9d68-b264cc027487".equals(results.getString("oracle_id")), "oracle id");
                require(results.getString("details_json").contains("Limited Edition Alpha"), "details json");
            }
        }
        System.out.println("Scryfall SQLite build smoke test passed: " + sqlite);
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
