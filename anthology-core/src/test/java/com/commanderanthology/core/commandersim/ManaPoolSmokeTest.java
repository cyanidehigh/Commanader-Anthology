package com.commanderanthology.core.commandersim;

import java.util.List;
import java.util.Optional;

public final class ManaPoolSmokeTest {
    private ManaPoolSmokeTest() {
    }

    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("Alice", "Bob"));
        int alice = game.playerOrder().get(0);

        game.addMana(alice, ManaType.WHITE, 1);
        game.addMana(alice, ManaType.BLUE, 2);
        game.addMana(alice, ManaType.COLORLESS, 3);

        ManaPool pool = game.players().get(alice).manaPoolDetails();
        assertEquals(1, pool.white(), "white mana should be represented in the pool");
        assertEquals(2, pool.blue(), "blue mana should be represented in the pool");
        assertEquals(3, pool.colorless(), "colorless mana should be represented in the pool");
        assertEquals(6, game.players().get(alice).manaPool(), "legacy total pool query should remain available");
        assertTrue(pool.canPayGeneric(5), "colored and colorless mana can pay current generic costs");

        game.advanceStep();
        assertEquals(0, game.players().get(alice).manaPool(), "mana pool should empty when advancing steps");
        closePriorityWindow(game);

        int hand = game.players().get(alice).zoneIds().get(ZoneType.HAND);
        GameObject creature = game.addObject(
                "Test Creature",
                ObjectType.CARD,
                alice,
                hand,
                Optional.of(CardKind.CREATURE),
                4,
                2,
                2
        );
        advanceTo(game, TurnStep.PRECOMBAT_MAIN);
        game.addMana(alice, ManaType.WHITE, 1);
        game.addMana(alice, ManaType.BLUE, 2);
        game.addMana(alice, ManaType.COLORLESS, 3);
        GameRules rules = new GameRules();
        rules.execute(game, rules.legalMoves(game, alice).stream()
                .filter(move -> move.token().sourceObject().equals(String.valueOf(creature.objectId())))
                .findFirst()
                .orElseThrow()
                .token());
        assertEquals(2, game.players().get(alice).manaPool(), "generic payment should spend from the real pool");

        closePriorityWindow(game);
        game.advanceStep();
        assertEquals(0, game.players().get(alice).manaPool(), "mana pool should empty when advancing steps");
        assertEquals(0, game.players().get(alice).manaPoolDetails().white(), "white mana should empty");
        assertEquals(0, game.players().get(alice).manaPoolDetails().blue(), "blue mana should empty");
        assertEquals(0, game.players().get(alice).manaPoolDetails().colorless(), "colorless mana should empty");

        game.validate();
        System.out.println("Mana pool smoke test passed.");
    }

    private static void advanceTo(GameFoundation game, TurnStep targetStep) {
        while (game.currentStep() != targetStep) {
            closePriorityWindow(game);
            game.advanceStep();
        }
    }

    private static void closePriorityWindow(GameFoundation game) {
        while (game.priorityPlayerId().isPresent()) {
            game.passPriority(game.priorityPlayerId().getAsInt());
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}
