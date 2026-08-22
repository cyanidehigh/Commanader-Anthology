package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckAssignment;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DesktopPersistenceSmokeTest {
    private DesktopPersistenceSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("anthology-persistence-test");
        Path file = tempDir.resolve("state.json");
        DesktopPersistence persistence = new DesktopPersistence(file);

        Deck deck = new Deck("deck-1", "Karn, Legacy Reforged", "Karn, Legacy Reforged", "container-deck-1");
        DeckSlot slot = DeckSlot.unresolved("slot-1", "deck-1", "Basalt Monolith", 1, DeckSection.ARTIFACT);
        DeckAssignment assignment = new DeckAssignment("assign-1", "slot-1", "entry-2", 1, "container-1", "deck-1");
        CardContainer binder = new CardContainer("container-1", "Artifact binder", ContainerType.BINDER);
        CardContainer deckContainer = new CardContainer("container-deck-1", "Karn, Legacy Reforged", ContainerType.DECK);
        InventoryEntry entry = InventoryEntry.unresolved("entry-1", "container-1", "Basalt Monolith", 1, false);

        persistence.save(new DesktopSnapshot(
                List.of(deck),
                List.of(slot),
                List.of(assignment),
                List.of(binder, deckContainer),
                List.of(entry)
        ));

        DesktopSnapshot loaded = persistence.load().orElseThrow();
        require(loaded.decks().size() == 1, "deck count");
        require(loaded.deckSlots().size() == 1, "slot count");
        require(loaded.deckAssignments().size() == 1, "assignment count");
        require(loaded.containers().size() == 2, "container count");
        require(loaded.inventoryEntries().size() == 1, "entry count");
        require("Karn, Legacy Reforged".equals(loaded.decks().get(0).name()), "deck name");
        require(loaded.deckSlots().get(0).section() == DeckSection.ARTIFACT, "deck section");
        require(!loaded.inventoryEntries().get(0).foil(), "foil value");
        String savedText = Files.readString(file);
        require(savedText.stripLeading().startsWith("{"), "json object");
        require(savedText.contains("\"schemaVersion\""), "schema version");

        System.out.println("Desktop persistence smoke test passed: " + file);
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
