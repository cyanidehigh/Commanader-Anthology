package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.ai.BasicAiPlayer;

import java.util.List;

public final class BasicAiGameDriver {
    private final GameRules rules;
    private final BasicAiPlayer ai;

    public BasicAiGameDriver() {
        this(new GameRules(), new BasicAiPlayer());
    }

    public BasicAiGameDriver(GameRules rules, BasicAiPlayer ai) {
        this.rules = rules;
        this.ai = ai;
    }

    public int run(GameFoundation game, int maxActions) {
        if (maxActions <= 0) {
            throw new IllegalArgumentException("Max actions must be positive.");
        }
        int actions = 0;
        while (actions < maxActions) {
            step(game);
            actions += 1;
        }
        game.validate();
        return actions;
    }

    public void step(GameFoundation game) {
        if (game.priorityPlayerId().isEmpty()) {
            game.advanceStep();
            return;
        }
        int playerId = game.priorityPlayerId().getAsInt();
        List<LegalMove> legalMoves = rules.legalMoves(game, playerId);
        LegalMove selected = ai.chooseMove(game, playerId, legalMoves)
                .orElseThrow(() -> new IllegalStateException("Priority player has no legal move."));
        rules.execute(game, selected.token());
    }

    public GameRules rules() {
        return rules;
    }
}
