package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.CardIdentityStatus;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CardLookupValidatorSmokeTest {
    private static final String SOL_RING_ORACLE_ID = "6ad8011d-3471-4369-9d68-b264cc027487";

    private CardLookupValidatorSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        ScryfallCardLookupService lookup = new ScryfallCardLookupService();
        require(!lookup.sqliteCaches().isEmpty(), "Scryfall SQLite cache discovered");
        ScryfallCardSelection solRing = lookup.lookupPreferredCard("Sol Ring", null).orElseThrow();
        require(SOL_RING_ORACLE_ID.equals(solRing.oracleId()), "Sol Ring oracle id");
        require(SOL_RING_ORACLE_ID.equals(lookup.lookupPreferredCard("Sol Ring 12 *F*", null).orElseThrow().oracleId()), "dirty Sol Ring oracle id");
        require(lookup.lookupPreferredCard("Lantern of Insight 5DN-135", null).isPresent(), "set-number suffix lookup");

        Path tempDir = Files.createTempDirectory("anthology-card-validator-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
        Deck deck = state.createDeck("Validator Test Deck", "Karn, Legacy Reforged");
        DeckSlot slot = state.addDeckSlot(deck.id(), "Sol Ring", 1, DeckSection.ARTIFACT);
        require(slot.identityStatus() == CardIdentityStatus.RESOLVED, "deck slot resolved");
        require(SOL_RING_ORACLE_ID.equals(slot.oracleId()), "deck slot oracle id");
        require(slot.preferredScryfallCardId() != null, "deck slot scryfall id");

        CardContainer binder = state.createContainer("Validator Test Binder", ContainerType.BINDER);
        InventoryEntry entry = state.addInventoryEntry(binder.id(), "Sol Ring", 1, false);
        require(entry.identityStatus() == CardIdentityStatus.RESOLVED, "inventory entry resolved");
        require(SOL_RING_ORACLE_ID.equals(entry.oracleId()), "inventory entry oracle id");
        require(entry.scryfallCardId() != null, "inventory entry scryfall id");

        System.out.println("Card lookup validator smoke test passed: " + solRing.printingLabel());
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
