package com.commanderanthology.core.commandersim;

import java.util.List;

public final class StateBasedActionsSmokeTest {
    private StateBasedActionsSmokeTest() {
    }

    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("Alice", "Bob"));
        int alice = game.playerOrder().get(0);
        int bob = game.playerOrder().get(1);

        closeUntapAndOpenPriority(game);
        assertEquals(alice, game.priorityPlayerId().orElseThrow(), "active player should receive priority first");

        int lifeAfterLoss = game.changeLife(alice, -40);
        assertEquals(0, lifeAfterLoss, "Alice should be at zero life");
        assertTrue(game.players().get(alice).lost(), "Alice should lose as a state-based action at 0 life");
        assertTrue(game.gameOver(), "1v1 game should be over once one player has lost");
        assertTrue(game.priorityPlayerId().isEmpty(), "priority should clear after game-over state-based actions");

        GameRules rules = new GameRules();
        assertTrue(rules.legalMoves(game, alice).isEmpty(), "lost players should not receive legal moves");
        assertTrue(rules.legalMoves(game, bob).isEmpty(), "game-over state should not produce more legal moves");
        game.validate();
        System.out.println("State-based actions smoke test passed.");
    }

    private static void closeUntapAndOpenPriority(GameFoundation game) {
        game.advanceStep();
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
