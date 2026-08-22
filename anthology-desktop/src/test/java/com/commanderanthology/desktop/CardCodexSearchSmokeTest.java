package com.commanderanthology.desktop;

public final class CardCodexSearchSmokeTest {
    private CardCodexSearchSmokeTest() {
    }

    public static void main(String[] args) {
        ScryfallCardLookupService lookup = new ScryfallCardLookupService();
        ScryfallCardSelection solRing = lookup.searchCards("Sol Ring", 20).stream()
                .filter(selection -> "Sol Ring".equals(selection.oracleName()))
                .findFirst()
                .orElseThrow();
        ScryfallCardDetails details = lookup.cardDetails(solRing.scryfallCardId()).orElseThrow();
        require("Sol Ring".equals(details.oracleName()), "details oracle name");
        require(details.oracleText() != null && !details.oracleText().isBlank(), "details oracle text");
        System.out.println("Card Codex search smoke test passed: " + details.title());
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
