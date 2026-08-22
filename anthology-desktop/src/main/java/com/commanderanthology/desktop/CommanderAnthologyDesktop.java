package com.commanderanthology.desktop;

import javax.swing.SwingUtilities;

public final class CommanderAnthologyDesktop {
    private CommanderAnthologyDesktop() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnthologyTheme.install();
            new DesktopShell().show();
        });
    }
}
