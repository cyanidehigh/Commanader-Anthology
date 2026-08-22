package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ManualCardSelectionSmokeTest {
    private ManualCardSelectionSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-manual-selection-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
        ScryfallCardSelection selection = new ScryfallCardLookupService()
                .lookupPreferredCard("Sol Ring", null)
                .orElseThrow();

        Deck deck = state.createDeck("Manual Selection Test", "Karn, Legacy Reforged");
        DeckSlot slot = state.addDeckSlot(deck.id(), new ImportedDeckRow(
                1,
                selection.printingName(),
                DeckSection.ARTIFACT,
                selection.scryfallCardId(),
                selection.setCode(),
                selection.collectorNumber()
        ));

        CardContainer binder = state.createContainer("Manual Binder", ContainerType.BINDER);
        InventoryEntry entry = state.addInventoryEntry(binder.id(), new ImportedCollectionRow(
                1,
                selection.printingName(),
                selection.setCode(),
                selection.collectorNumber(),
                selection.scryfallCardId(),
                false
        ));

        require(selection.scryfallCardId().equals(slot.preferredScryfallCardId()), "deck selected identity");
        require(selection.scryfallCardId().equals(entry.scryfallCardId()), "collection selected identity");
        require(selection.oracleId().equals(slot.oracleId()), "deck oracle identity");
        require(selection.oracleId().equals(entry.oracleId()), "collection oracle identity");
        System.out.println("Manual card selection smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
