package com.commanderanthology.desktop;

import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckSlot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DeckImportReviewedIdentitySmokeTest {
    private DeckImportReviewedIdentitySmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-deck-import-reviewed-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
        Deck deck = state.createDeck("Reviewed Import Test", "Karn, Legacy Reforged");
        state.addImportedDeckSlots(deck.id(), List.of(new ImportedDeckRow(1, "Sol Ring", null)));

        DeckSlot slot = state.deckSlotsFor(deck.id()).stream()
                .filter(candidate -> "Sol Ring".equals(candidate.oracleName()))
                .findFirst()
                .orElseThrow();
        require("6ad8011d-3471-4369-9d68-b264cc027487".equals(slot.oracleId()), "oracle identity");
        require(slot.preferredScryfallCardId() == null, "import should not pin preferred scryfall card");
        require(slot.preferredSetCode() == null, "import should not pin preferred set");
        System.out.println("Deck import reviewed identity smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
