package com.commanderanthology.game.gdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.commanderanthology.core.commandersim.GameFoundation;

public final class CommanderGdxGameLauncher {
    private CommanderGdxGameLauncher() {
    }

    public static void launch(GameFoundation game, String mode, String playerDeckName, String opponentDeckName) {
        Thread thread = new Thread(() -> {
            Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
            configuration.setTitle("Commander Anthology - Game");
            configuration.setWindowedMode(1280, 820);
            configuration.setWindowSizeLimits(980, 640, -1, -1);
            configuration.useVsync(true);
            new Lwjgl3Application(new CommanderPlaymatApplication(game, mode, playerDeckName, opponentDeckName), configuration);
        }, "commander-anthology-gdx-game");
        thread.setDaemon(false);
        thread.start();
    }
}
