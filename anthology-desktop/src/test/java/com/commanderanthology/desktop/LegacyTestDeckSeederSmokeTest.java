package com.commanderanthology.desktop;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LegacyTestDeckSeederSmokeTest {
    private LegacyTestDeckSeederSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-legacy-seed-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
        state.importLegacyTestDecks();

        require(state.decks().size() >= 50, "legacy deck count");
        require(state.hasDeckNamed("Karn, Legacy Reforged"), "Karn deck present");
        require(state.decks().stream().anyMatch(deck -> state.deckSlotsFor(deck.id()).size() >= 90), "full deck imported");

        System.out.println("Legacy test deck seeder smoke test passed with " + state.decks().size() + " decks.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
