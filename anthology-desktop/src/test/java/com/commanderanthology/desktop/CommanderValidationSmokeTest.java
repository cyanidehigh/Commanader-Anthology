package com.commanderanthology.desktop;

import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CommanderValidationSmokeTest {
    private CommanderValidationSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        ScryfallCardLookupService lookup = new ScryfallCardLookupService();
        CommanderValidation legalCommander = lookup.validateCommander("Karn, Legacy Reforged");
        require(legalCommander.valid(), "known commander should validate");

        CommanderValidation nonCommander = lookup.validateCommander("Sol Ring");
        require(!nonCommander.valid(), "real non-commander card should be rejected");

        Path tempDir = Files.createTempDirectory("anthology-commander-validation-test");
        DesktopAppState state = new DesktopAppState(new DesktopPersistence(tempDir.resolve("state.json")));
        Deck deck = state.createDeck("Karn Test", "Karn, Legacy Reforged");
        require("Karn, Legacy Reforged".equals(deck.commanderName()), "deck should store normalized commander name");
        DeckSlot commanderSlot = state.deckSlotsFor(deck.id()).stream()
                .filter(slot -> slot.section() == DeckSection.COMMANDER)
                .findFirst()
                .orElseThrow();
        require("Karn, Legacy Reforged".equals(commanderSlot.oracleName()), "deck should auto-create commander slot");
        require(commanderSlot.desiredQuantity() == 1, "commander slot should be singleton");

        DeckSlot fallbackCommander = state.addDeckSlot(deck.id(), "Rafiq of the Many", 1, DeckSection.OTHER);
        Deck updated = state.setCommanderFromSlot(fallbackCommander.id());
        require("Rafiq of the Many".equals(updated.commanderName()), "set commander should update deck metadata");
        long rafiqCommanderSlots = state.deckSlotsFor(deck.id()).stream()
                .filter(slot -> slot.section() == DeckSection.COMMANDER)
                .filter(slot -> "Rafiq of the Many".equals(slot.oracleName()))
                .count();
        require(rafiqCommanderSlots == 1, "set commander should create one commander slot");
        require(state.deckSlotsFor(deck.id()).stream().noneMatch(slot -> slot.id().equals(fallbackCommander.id())), "source singleton row should be removed");

        boolean rejected = false;
        try {
            state.createDeck("Bad Commander Test", "Sol Ring");
        } catch (IllegalArgumentException error) {
            rejected = true;
        }
        require(rejected, "deck creation should reject illegal commander");
        System.out.println("Commander validation smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
