package com.commanderanthology.core.commandersim;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class StartingGameSmokeTest {
    private StartingGameSmokeTest() {
    }

    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("Alice", "Bob"));
        int alice = game.playerOrder().get(0);
        int bob = game.playerOrder().get(1);

        GameObject aliceCommander = game.addCommander(
                "Rafiq of the Many",
                alice,
                Optional.of(CardKind.CREATURE),
                4,
                3,
                3
        );
        GameObject bobCommander = game.addCommander(
                "Karn, Legacy Reforged",
                bob,
                Optional.of(CardKind.CREATURE),
                5,
                0,
                0
        );
        seedLibrary(game, alice, "Alice");
        seedLibrary(game, bob, "Bob");

        game.prepareOpeningHands(new Random(103L), 7);

        assertEquals(40, game.players().get(alice).life(), "Commander starting life should be 40");
        assertEquals(40, game.players().get(bob).life(), "Commander starting life should be 40");
        assertTrue(game.objects().get(aliceCommander.objectId()).commander(), "Alice commander should be marked as commander");
        assertTrue(game.objects().get(bobCommander.objectId()).commander(), "Bob commander should be marked as commander");
        assertEquals(
                game.sharedZoneIds().get(ZoneType.COMMAND),
                game.objects().get(aliceCommander.objectId()).zoneId(),
                "Alice commander should stay in command zone"
        );
        assertEquals(
                game.sharedZoneIds().get(ZoneType.COMMAND),
                game.objects().get(bobCommander.objectId()).zoneId(),
                "Bob commander should stay in command zone"
        );
        assertEquals(2, game.objectsInZone(game.sharedZoneIds().get(ZoneType.COMMAND)).size(), "command zone should contain both commanders");
        assertEquals(7, game.objectsInZone(game.players().get(alice).zoneIds().get(ZoneType.HAND)).size(), "Alice opening hand should be seven");
        assertEquals(7, game.objectsInZone(game.players().get(bob).zoneIds().get(ZoneType.HAND)).size(), "Bob opening hand should be seven");
        assertEquals(3, game.objectsInZone(game.players().get(alice).zoneIds().get(ZoneType.LIBRARY)).size(), "Alice library should keep remaining cards");
        assertEquals(3, game.objectsInZone(game.players().get(bob).zoneIds().get(ZoneType.LIBRARY)).size(), "Bob library should keep remaining cards");
        assertEquals(TurnStep.UNTAP, game.currentStep(), "game should still begin at untap before first advancement");
        assertTrue(game.priorityPlayerId().isEmpty(), "players should not receive priority during initial untap setup");
        game.validate();
        System.out.println("Starting game smoke test passed.");
    }

    private static void seedLibrary(GameFoundation game, int playerId, String prefix) {
        int library = game.players().get(playerId).zoneIds().get(ZoneType.LIBRARY);
        for (int index = 0; index < 10; index++) {
            game.addObject(
                    prefix + " Library Card " + index,
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
