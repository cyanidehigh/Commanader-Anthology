package com.commanderanthology.desktop;

import com.commanderanthology.core.deck.DeckSection;

public final class DeckImportParserSmokeTest {
    private DeckImportParserSmokeTest() {
    }

    public static void main(String[] args) {
        String decklist = """
                Commander:
                1 Karn, Legacy Reforged

                Artifacts (3)
                1 Sol Ring
                2x Basalt Monolith
                1 Expedition Map (2XM) 255
                1 Walking Ballista (SLD) 1265 *F*

                Lands:
                32 Wastes

                Maybeboard:
                1 Mana Crypt
                """;

        DeckImportParseResult result = DeckImportParser.parse(decklist);
        require(result.warnings().isEmpty(), "warnings");
        require(result.rows().size() == 5, "imported row count");
        require(result.rows().stream().allMatch(row -> row.section() == DeckSection.OTHER), "deck cards import as normal intent rows");
        require("Sol Ring".equals(result.rows().get(0).cardName()), "commander section skipped");
        require(result.rows().get(1).quantity() == 2, "quantity x syntax");
        require("Expedition Map".equals(result.rows().get(2).cardName()), "printing metadata stripped");
        require("Walking Ballista".equals(result.rows().get(3).cardName()), "foil printing metadata stripped");

        System.out.println("Deck import parser smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
