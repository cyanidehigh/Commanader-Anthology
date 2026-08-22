package com.commanderanthology.desktop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class RestoreCcBuilderUserData {
    private RestoreCcBuilderUserData() {
    }

    public static void main(String[] args) throws Exception {
        DesktopPersistence persistence = new DesktopPersistence();
        Path target = persistence.dataFile();
        if (Files.exists(target)) {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path backup = target.resolveSibling(target.getFileName() + "." + stamp + ".bak");
            Files.copy(target, backup);
            System.out.println("Backed up current Anthology state to " + backup);
        }

        DesktopAppState state = new DesktopAppState(persistence);
        if (!state.importCcBuilderUserData()) {
            throw new IllegalStateException("No CCBuilder user data found at " + LegacyCcBuilderDataImporter.dataFile());
        }

        int inventoryRows = state.visibleContainers().stream()
                .mapToInt(container -> state.entriesFor(container.id()).size())
                .sum();
        int deckSlots = state.decks().stream()
                .mapToInt(deck -> state.deckSlotsFor(deck.id()).size())
                .sum();
        System.out.println("Restored CCBuilder data into Anthology:");
        System.out.println("Decks: " + state.decks().size());
        System.out.println("Visible containers: " + state.visibleContainers().size());
        System.out.println("Inventory rows: " + inventoryRows);
        System.out.println("Deck slots: " + deckSlots);
        System.out.println("Unresolved card input rows: " + state.unresolvedCardInputCount());
        System.out.println("Anthology state: " + target);
    }
}
