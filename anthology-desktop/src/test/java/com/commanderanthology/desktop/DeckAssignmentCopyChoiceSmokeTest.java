package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DeckAssignmentCopyChoiceSmokeTest {
    private DeckAssignmentCopyChoiceSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-copy-choice-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
        Deck deck = state.createDeck("Copy Choice Test", "Karn, Legacy Reforged");
        DeckSlot slot = state.addDeckSlot(deck.id(), "Sol Ring", 1, DeckSection.ARTIFACT);
        CardContainer binder = state.createContainer("Binder Copy", ContainerType.BINDER);
        CardContainer box = state.createContainer("Box Copy", ContainerType.BOX);
        InventoryEntry binderCopy = state.addInventoryEntry(binder.id(), "Sol Ring", 1, false);
        InventoryEntry boxCopy = state.addInventoryEntry(box.id(), "Sol Ring", 1, false);

        require(state.assignInventoryEntryById(slot, boxCopy.id()), "assign chosen copy");
        require(state.assignedEntriesFor(slot.id()).stream().anyMatch(entry -> entry.id().equals(boxCopy.id())), "chosen copy assigned");
        require(state.entriesFor(binder.id()).stream().anyMatch(entry -> entry.id().equals(binderCopy.id())), "unchosen copy remains");
        System.out.println("Deck assignment copy choice smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
