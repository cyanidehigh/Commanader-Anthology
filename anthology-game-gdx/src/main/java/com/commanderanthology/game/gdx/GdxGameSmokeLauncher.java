package com.commanderanthology.game.gdx;

import com.commanderanthology.core.commandersim.GameFoundation;

import java.util.List;

public final class GdxGameSmokeLauncher {
    private GdxGameSmokeLauncher() {
    }

    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("Player", "Opponent"), 40);
        CommanderGdxGameLauncher.launch(game, "Play", "Smoke Player Deck", "Smoke Opponent Deck");
    }
}
