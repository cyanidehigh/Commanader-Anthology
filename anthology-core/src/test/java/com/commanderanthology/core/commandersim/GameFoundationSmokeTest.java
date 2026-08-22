package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.move.MoveType;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class GameFoundationSmokeTest {
    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("Alice", "Bob"));
        int aliceId = game.playerOrder().get(0);
        int bobId = game.playerOrder().get(1);
        GameRules rules = new GameRules();

        advanceTo(game, TurnStep.PRECOMBAT_MAIN);
        int handZoneId = game.players().get(aliceId).zoneIds().get(ZoneType.HAND);
        GameObject forest = game.addObject(
                "Forest",
                ObjectType.CARD,
                aliceId,
                handZoneId,
                Optional.of(CardKind.LAND),
                0,
                0,
                0
        );
        GameObject bear = game.addObject(
                "Grizzly Bears",
                ObjectType.CARD,
                aliceId,
                handZoneId,
                Optional.of(CardKind.CREATURE),
                2,
                2,
                2
        );
        GameObject libraryCard = game.addObject(
                "Island",
                ObjectType.CARD,
                aliceId,
                game.players().get(aliceId).zoneIds().get(ZoneType.LIBRARY),
                Optional.of(CardKind.LAND),
                0,
                0,
                0
        );
        game.drawCard(aliceId);
        assertEquals(
                game.players().get(aliceId).zoneIds().get(ZoneType.HAND),
                game.objects().get(libraryCard.objectId()).zoneId(),
                "draw should move top library card to hand"
        );
        assertShuffleChangesLibraryOrder();
        assertOpeningHandsShuffleAndDraw();

        assertLegal(rules.legalMoves(game, aliceId), MoveType.PLAY_LAND, "land play should be legal");
        rules.execute(game, rules.legalMoves(game, aliceId).stream()
                .filter(move -> move.token().type() == MoveType.PLAY_LAND)
                .findFirst()
                .orElseThrow()
                .token());
        assertEquals(
                game.sharedZoneIds().get(ZoneType.BATTLEFIELD),
                game.objects().get(forest.objectId()).zoneId(),
                "land should move to battlefield"
        );
        assertFalse(
                rules.legalMoves(game, aliceId).stream().anyMatch(move -> move.token().type() == MoveType.PLAY_LAND),
                "second land should not be legal this turn"
        );

        game.addMana(aliceId, 2);
        assertLegal(rules.legalMoves(game, aliceId), MoveType.CAST_SPELL, "creature cast should be legal with enough mana");
        rules.execute(game, rules.legalMoves(game, aliceId).stream()
                .filter(move -> move.token().type() == MoveType.CAST_SPELL)
                .findFirst()
                .orElseThrow()
                .token());
        assertEquals(
                game.sharedZoneIds().get(ZoneType.STACK),
                game.objects().get(bear.objectId()).zoneId(),
                "cast creature should move to stack first"
        );
        game.passPriority(aliceId);
        PriorityResult resolution = game.passPriority(bobId);
        assertEquals(PriorityEvent.STACK_RESOLVED, resolution.event(), "all players passing should resolve stack top");
        assertEquals(
                game.sharedZoneIds().get(ZoneType.BATTLEFIELD),
                game.objects().get(bear.objectId()).zoneId(),
                "resolved creature should move to battlefield"
        );
        assertFalse(rules.events().isEmpty(), "executed moves should be recorded as game events");
        game.validate();
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

    private static void assertLegal(List<LegalMove> moves, MoveType moveType, String message) {
        assertTrue(moves.stream().anyMatch(move -> move.token().type() == moveType), message);
    }

    private static void assertShuffleChangesLibraryOrder() {
        GameFoundation game = GameFoundation.buildGame(List.of("Shuffle Tester"));
        int playerId = game.playerOrder().get(0);
        int library = game.players().get(playerId).zoneIds().get(ZoneType.LIBRARY);
        for (int index = 0; index < 10; index++) {
            game.addObject("Card " + index, ObjectType.CARD, playerId, library, Optional.of(CardKind.LAND), 0, 0, 0);
        }
        List<Integer> before = game.zones().get(library).objectIds();
        game.shuffleLibrary(playerId, new Random(7L));
        List<Integer> after = game.zones().get(library).objectIds();
        assertFalse(before.equals(after), "shuffle should change library order with seeded random");
    }

    private static void assertOpeningHandsShuffleAndDraw() {
        GameFoundation game = GameFoundation.buildGame(List.of("Alice", "Bob"));
        for (int playerId : game.playerOrder()) {
            int library = game.players().get(playerId).zoneIds().get(ZoneType.LIBRARY);
            for (int index = 0; index < 10; index++) {
                game.addObject(
                        "Card " + playerId + "." + index,
                        ObjectType.CARD,
                        playerId,
                        library,
                        Optional.of(CardKind.LAND),
                        0,
                        0,
                        0
                );
            }
        }

        game.prepareOpeningHands(new Random(11L), 7);

        for (int playerId : game.playerOrder()) {
            int library = game.players().get(playerId).zoneIds().get(ZoneType.LIBRARY);
            int hand = game.players().get(playerId).zoneIds().get(ZoneType.HAND);
            assertEquals(3, game.objectsInZone(library).size(), "opening hand should leave remaining library cards");
            assertEquals(7, game.objectsInZone(hand).size(), "opening hand should draw seven cards");
        }
        game.validate();
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}
