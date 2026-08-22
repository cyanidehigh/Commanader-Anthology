package com.commanderanthology.game.gdx;

import com.commanderanthology.core.commandersim.GameFoundation;

import java.util.List;

public final class GdxLauncherSmokeTest {
    private GdxLauncherSmokeTest() {
    }

    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("Player", "Opponent"), 40);
        require(game.playerOrder().size() == 2, "game state available to gdx module");
        System.out.println("GDX launcher smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
