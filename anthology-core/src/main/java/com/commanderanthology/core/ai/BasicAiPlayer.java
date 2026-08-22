package com.commanderanthology.core.ai;

import com.commanderanthology.core.commandersim.GameFoundation;
import com.commanderanthology.core.commandersim.GameObject;
import com.commanderanthology.core.commandersim.LegalMove;
import com.commanderanthology.core.commandersim.Player;
import com.commanderanthology.core.commandersim.ZoneType;
import com.commanderanthology.core.move.MoveType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BasicAiPlayer {
    public static final String SCORING_VERSION = "basic-ai-v0.5.0";

    public Optional<LegalMove> chooseMove(GameFoundation game, int playerId, List<LegalMove> legalMoves) {
        return legalMoves.stream()
                .max(Comparator
                        .comparingInt((LegalMove move) -> score(game, playerId, move, legalMoves).totalScore())
                        .thenComparing(move -> score(game, playerId, move, legalMoves).tieBreak()));
    }

    public MoveScore score(GameFoundation game, int playerId, LegalMove move) {
        return score(game, playerId, move, List.of(move));
    }

    private MoveScore score(GameFoundation game, int playerId, LegalMove move, List<LegalMove> legalMoves) {
        MoveType type = move.token().type();
        int baseScore = switch (type) {
            case PLAY_LAND -> 900;
            case CAST_SPELL -> 700;
            case ACTIVATE_MANA_ABILITY -> shouldMakeMana(game, playerId) ? 650 : 100;
            case PASS_PRIORITY -> 0;
            default -> -100;
        };
        int cardScore = cardScore(game, move);
        int tempoScore = type == MoveType.PASS_PRIORITY && hasUsefulNonPassMove(legalMoves, game, playerId)
                ? -500
                : 0;
        return MoveScore.of(
                move.token().legalMoveId(),
                baseScore,
                Map.of("card", cardScore, "tempo", tempoScore),
                move.token().legalMoveId(),
                SCORING_VERSION
        );
    }

    private boolean shouldMakeMana(GameFoundation game, int playerId) {
        Player player = game.players().get(playerId);
        int handZone = player.zoneIds().get(ZoneType.HAND);
        return game.objectsInZone(handZone).stream()
                .anyMatch(card -> card.manaCost() > player.manaPool());
    }

    private int cardScore(GameFoundation game, LegalMove move) {
        if (move.token().type() != MoveType.CAST_SPELL) {
            return 0;
        }
        return object(game, move)
                .map(object -> object.manaCost() * 20 + object.power() * 10 + object.toughness() * 10)
                .orElse(0);
    }

    private boolean hasUsefulNonPassMove(List<LegalMove> moves, GameFoundation game, int playerId) {
        return moves.stream()
                .filter(move -> move.token().type() != MoveType.PASS_PRIORITY)
                .anyMatch(move -> move.token().type() != MoveType.ACTIVATE_MANA_ABILITY
                || shouldMakeMana(game, playerId));
    }

    private Optional<GameObject> object(GameFoundation game, LegalMove move) {
        try {
            return Optional.ofNullable(game.objects().get(Integer.parseInt(move.token().sourceObject())));
        } catch (NumberFormatException error) {
            return Optional.empty();
        }
    }
}
