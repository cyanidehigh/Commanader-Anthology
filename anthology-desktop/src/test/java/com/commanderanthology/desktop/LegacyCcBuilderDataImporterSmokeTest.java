package com.commanderanthology.desktop;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LegacyCcBuilderDataImporterSmokeTest {
    private LegacyCcBuilderDataImporterSmokeTest() {
    }

    public static void main(String[] args) {
        DesktopSnapshot snapshot = LegacyCcBuilderDataImporter.loadSnapshot().orElseThrow();

        require(snapshot.decks().size() >= 4, "deck count");
        require(snapshot.containers().size() > snapshot.decks().size(), "container count");
        require(snapshot.inventoryEntries().size() > 1000, "inventory count");
        require(snapshot.deckSlots().size() > 100, "deck slot count");
        require(snapshot.deckAssignments().size() > 0, "assignment count");
        require(snapshot.decks().stream().anyMatch(deck -> "Rafiq of the Many".equals(deck.name())), "Rafiq deck");

        runStateImportCheck();

        System.out.println("Legacy CCBuilder data import smoke test passed from " + LegacyCcBuilderDataImporter.dataFile());
    }

    private static void runStateImportCheck() {
        try {
            Path tempDir = Files.createTempDirectory("anthology-ccbuilder-import-test");
            DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
            require(state.importCcBuilderUserData(), "state import");
            require(state.decks().size() >= 4, "state deck count");
            require(state.visibleContainers().size() > 40, "state visible container count");
            require(state.decks().stream().anyMatch(deck -> state.deckSlotsFor(deck.id()).size() > 50), "state deck slots present");
            require(state.visibleContainers().stream().anyMatch(container -> state.entriesFor(container.id()).size() > 100), "state collection entries present");
            require(state.unresolvedCardInputCount() < 80, "state unresolved rows after validation");
        } catch (Exception error) {
            throw new AssertionError("State import check failed", error);
        }
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
