package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckAssignment;
import com.commanderanthology.core.deck.DeckSlot;

import java.util.List;

record DesktopSnapshot(
        List<Deck> decks,
        List<DeckSlot> deckSlots,
        List<DeckAssignment> deckAssignments,
        List<CardContainer> containers,
        List<InventoryEntry> inventoryEntries
) {
    DesktopSnapshot {
        decks = List.copyOf(decks);
        deckSlots = List.copyOf(deckSlots);
        deckAssignments = List.copyOf(deckAssignments);
        containers = List.copyOf(containers);
        inventoryEntries = List.copyOf(inventoryEntries);
    }
}

