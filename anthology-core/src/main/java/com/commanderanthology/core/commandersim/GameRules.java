package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.move.MoveType;
import com.commanderanthology.core.move.StructuredMoveToken;
import com.commanderanthology.core.validation.ValidationGate;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GameRules {
    private final Clock clock;
    private final ArrayList<GameEvent> events = new ArrayList<>();

    public GameRules() {
        this(Clock.systemUTC());
    }

    public GameRules(Clock clock) {
        this.clock = clock;
    }

    public List<LegalMove> legalMoves(GameFoundation game, int playerId) {
        ArrayList<LegalMove> moves = new ArrayList<>();
        if (game.gameOver() || game.players().get(playerId).lost()) {
            return List.of();
        }
        if (game.priorityPlayerId().isPresent() && game.priorityPlayerId().getAsInt() == playerId) {
            moves.add(legalMove(passToken(game, playerId)));
            int handZoneId = game.players().get(playerId).zoneIds().get(ZoneType.HAND);
            for (GameObject object : game.objectsInZone(handZoneId)) {
                if (game.canPlayLand(playerId, object.objectId())) {
                    moves.add(legalMove(cardToken(MoveType.PLAY_LAND, game, playerId, object, ZoneType.HAND, ZoneType.BATTLEFIELD)));
                }
                if (game.canCastSpell(playerId, object.objectId())) {
                    moves.add(legalMove(cardToken(MoveType.CAST_SPELL, game, playerId, object, ZoneType.HAND, ZoneType.STACK)));
                }
            }
            for (GameObject object : game.objectsInZone(game.sharedZoneIds().get(ZoneType.BATTLEFIELD))) {
                if (game.canActivateManaAbility(playerId, object.objectId())) {
                    moves.add(legalMove(cardToken(MoveType.ACTIVATE_MANA_ABILITY, game, playerId, object, ZoneType.BATTLEFIELD, ZoneType.BATTLEFIELD)));
                }
            }
        }
        return List.copyOf(moves);
    }

    public GameEvent execute(GameFoundation game, StructuredMoveToken requestedMove) {
        LegalMove legalMove = legalMoves(game, Integer.parseInt(requestedMove.actor()))
                .stream()
                .filter(candidate -> candidate.token().legalMoveId().equals(requestedMove.legalMoveId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Requested move is not currently legal."));

        StructuredMoveToken token = legalMove.token();
        int actor = Integer.parseInt(token.actor());
        if (token.type() == MoveType.PLAY_LAND || token.type() == MoveType.CAST_SPELL || token.type() == MoveType.ACTIVATE_MANA_ABILITY) {
            int objectId = Integer.parseInt(token.sourceObject());
            if (token.type() == MoveType.PLAY_LAND) {
                game.playLand(actor, objectId);
            } else if (token.type() == MoveType.CAST_SPELL) {
                game.castSpell(actor, objectId);
            } else {
                game.activateManaAbility(actor, objectId);
            }
        } else if (token.type() == MoveType.PASS_PRIORITY) {
            game.passPriority(actor);
        } else {
            throw new IllegalArgumentException("Unsupported game move type: " + token.type());
        }
        game.checkStateBasedActions();
        game.validate();

        GameEvent event = new GameEvent(Instant.now(clock), ValidationGate.FINAL_EXECUTION_VALIDATION, token, token.type().name());
        events.add(event);
        return event;
    }

    public List<GameEvent> events() {
        return List.copyOf(events);
    }

    private static LegalMove legalMove(StructuredMoveToken token) {
        return new LegalMove(token, ValidationGate.LEGAL_MOVE_GENERATION);
    }

    private static StructuredMoveToken passToken(GameFoundation game, int playerId) {
        return new StructuredMoveToken(
                MoveType.PASS_PRIORITY,
                "pass:" + game.turnNumber() + ":" + game.currentStep() + ":" + playerId,
                String.valueOf(playerId),
                "priority",
                Optional.empty(),
                Optional.of("Pass priority"),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                game.currentStep().name(),
                List.of("priority-player"),
                Optional.empty()
        );
    }

    private static StructuredMoveToken cardToken(
            MoveType moveType,
            GameFoundation game,
            int playerId,
            GameObject object,
            ZoneType fromZone,
            ZoneType toZone
    ) {
        return new StructuredMoveToken(
                moveType,
                moveType.name().toLowerCase() + ":" + object.objectId(),
                String.valueOf(playerId),
                String.valueOf(object.objectId()),
                Optional.empty(),
                Optional.of(object.name()),
                Optional.of(fromZone.name()),
                Optional.of(toZone.name()),
                List.of(),
                List.of(),
                Optional.of("normal"),
                Map.of("generic", object.manaCost()),
                Optional.empty(),
                game.currentStep().name(),
                List.of("priority-player", "rules-legal"),
                Optional.empty()
        );
    }
}
