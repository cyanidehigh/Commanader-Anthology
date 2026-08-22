package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.move.MoveType;

import java.util.List;
import java.util.Optional;

public final class TimingAndPrioritySmokeTest {
    private TimingAndPrioritySmokeTest() {
    }

    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("Alice", "Bob"));
        GameRules rules = new GameRules();
        int alice = game.playerOrder().get(0);
        int bob = game.playerOrder().get(1);
        seedAliceHand(game, alice);

        assertTrue(game.priorityPlayerId().isEmpty(), "untap setup should not start with priority");
        assertTrue(rules.legalMoves(game, alice).isEmpty(), "no priority means no legal moves");
        assertFalse(game.isMainPhaseSorceryWindow(alice), "untap is not a sorcery window");

        game.advanceStep();
        assertEquals(TurnStep.UPKEEP, game.currentStep(), "first advance should enter upkeep");
        assertEquals(alice, game.priorityPlayerId().orElseThrow(), "active player should receive upkeep priority");
        game.addMana(alice, 5);
        assertFalse(game.isMainPhaseSorceryWindow(alice), "upkeep is not a sorcery window");
        assertTrue(hasCastNamed(game, rules.legalMoves(game, alice), "Opt"), "instant should be legal during upkeep priority");
        assertFalse(hasMove(rules.legalMoves(game, alice), MoveType.PLAY_LAND), "land play should not be legal during upkeep");
        assertFalse(hasCastNamed(game, rules.legalMoves(game, alice), "Divination"), "sorcery should not be legal during upkeep");
        assertFalse(hasCastNamed(game, rules.legalMoves(game, alice), "Grizzly Bears"), "creature should not be legal during upkeep");

        rules.execute(game, passMove(rules, game, alice));
        assertEquals(bob, game.priorityPlayerId().orElseThrow(), "priority should pass to the nonactive player");
        assertTrue(rules.legalMoves(game, alice).isEmpty(), "player without priority should have no legal moves");
        rules.execute(game, passMove(rules, game, bob));
        assertTrue(game.priorityPlayerId().isEmpty(), "all players passing should close empty upkeep priority");

        game.advanceStep();
        assertEquals(TurnStep.DRAW, game.currentStep(), "next step should be draw");
        assertEquals(alice, game.priorityPlayerId().orElseThrow(), "draw step should open priority after draw action");
        assertFalse(game.isMainPhaseSorceryWindow(alice), "draw step is not a sorcery window");
        closePriorityWindow(rules, game);

        game.advanceStep();
        assertEquals(TurnStep.PRECOMBAT_MAIN, game.currentStep(), "next step should be precombat main");
        game.addMana(alice, 5);
        assertTrue(game.isMainPhaseSorceryWindow(alice), "active player's main phase with empty stack is a sorcery window");
        assertTrue(hasMove(rules.legalMoves(game, alice), MoveType.PLAY_LAND), "land play should be legal in sorcery window");
        assertTrue(hasCastNamed(game, rules.legalMoves(game, alice), "Divination"), "sorcery should be legal in sorcery window");
        assertTrue(hasCastNamed(game, rules.legalMoves(game, alice), "Grizzly Bears"), "creature should be legal in sorcery window");

        rules.execute(game, castMove(game, rules, alice, "Grizzly Bears"));
        assertFalse(game.isMainPhaseSorceryWindow(alice), "non-empty stack should close the sorcery window");
        assertTrue(hasCastNamed(game, rules.legalMoves(game, alice), "Opt"), "instant should remain legal while stack is non-empty");
        assertFalse(hasMove(rules.legalMoves(game, alice), MoveType.PLAY_LAND), "land play should not be legal while stack is non-empty");
        assertFalse(hasCastNamed(game, rules.legalMoves(game, alice), "Divination"), "sorcery should not be legal while stack is non-empty");

        game.validate();
        System.out.println("Timing and priority smoke test passed.");
    }

    private static void seedAliceHand(GameFoundation game, int playerId) {
        int hand = game.players().get(playerId).zoneIds().get(ZoneType.HAND);
        game.addObject("Forest", ObjectType.CARD, playerId, hand, Optional.of(CardKind.LAND), 0, 0, 0);
        game.addObject("Grizzly Bears", ObjectType.CARD, playerId, hand, Optional.of(CardKind.CREATURE), 2, 2, 2);
        game.addObject("Divination", ObjectType.CARD, playerId, hand, Optional.of(CardKind.SORCERY), 3, 0, 0);
        game.addObject("Opt", ObjectType.CARD, playerId, hand, Optional.of(CardKind.INSTANT), 1, 0, 0);
    }

    private static void closePriorityWindow(GameRules rules, GameFoundation game) {
        while (game.priorityPlayerId().isPresent()) {
            int playerId = game.priorityPlayerId().getAsInt();
            rules.execute(game, passMove(rules, game, playerId));
        }
    }

    private static com.commanderanthology.core.move.StructuredMoveToken passMove(
            GameRules rules,
            GameFoundation game,
            int playerId
    ) {
        return rules.legalMoves(game, playerId).stream()
                .filter(move -> move.token().type() == MoveType.PASS_PRIORITY)
                .findFirst()
                .orElseThrow()
                .token();
    }

    private static com.commanderanthology.core.move.StructuredMoveToken castMove(
            GameFoundation game,
            GameRules rules,
            int playerId,
            String cardName
    ) {
        return rules.legalMoves(game, playerId).stream()
                .filter(move -> move.token().type() == MoveType.CAST_SPELL)
                .filter(move -> objectName(game, move).equals(cardName))
                .findFirst()
                .orElseThrow()
                .token();
    }

    private static boolean hasMove(List<LegalMove> moves, MoveType moveType) {
        return moves.stream().anyMatch(move -> move.token().type() == moveType);
    }

    private static boolean hasCastNamed(GameFoundation game, List<LegalMove> moves, String cardName) {
        return moves.stream()
                .filter(move -> move.token().type() == MoveType.CAST_SPELL)
                .anyMatch(move -> objectName(game, move).equals(cardName));
    }

    private static String objectName(GameFoundation game, LegalMove move) {
        return game.objects().get(Integer.parseInt(move.token().sourceObject())).name();
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
