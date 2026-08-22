package com.commanderanthology.desktop;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class SyncPanel extends JPanel {
    private final DesktopAppState state;
    private final JLabel status = new JLabel();
    private final JTextArea info = new JTextArea();

    SyncPanel(DesktopAppState state) {
        super(new BorderLayout(12, 12));
        this.state = state;
        setOpaque(true);
        setBackground(AnthologyTheme.SURFACE);
        setBorder(AnthologyTheme.panelBorder());
        add(header(), BorderLayout.NORTH);
        add(info(), BorderLayout.CENTER);
        add(actions(), BorderLayout.SOUTH);
        refresh();
    }

    private JPanel header() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        panel.setOpaque(false);
        JLabel title = new JLabel("Backup");
        title.setForeground(AnthologyTheme.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        status.setForeground(AnthologyTheme.MUTED);
        panel.add(title);
        panel.add(status);
        return panel;
    }

    private JTextArea info() {
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBackground(AnthologyTheme.SURFACE_ALT);
        info.setForeground(AnthologyTheme.TEXT);
        info.setCaretColor(AnthologyTheme.TEXT);
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return info;
    }

    private JPanel actions() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 8, 0));
        panel.setOpaque(false);
        panel.add(button("Export backup", this::exportBundle));
        panel.add(button("Import backup", this::importBundle));
        panel.add(button("Refresh", this::refresh));
        return panel;
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        AnthologyTheme.styleButton(button);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void exportBundle() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("commander-anthology-backup-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".json"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        state.exportBundle(chooser.getSelectedFile().toPath());
        refresh();
        JOptionPane.showMessageDialog(this, "Exported backup to " + chooser.getSelectedFile());
    }

    private void importBundle() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (JOptionPane.showConfirmDialog(
                this,
                "Importing replaces the current local Anthology desktop state.",
                "Import backup",
                JOptionPane.OK_CANCEL_OPTION
        ) != JOptionPane.OK_OPTION) {
            return;
        }
        state.importBundle(chooser.getSelectedFile().toPath());
        refresh();
        JOptionPane.showMessageDialog(this, "Imported backup from " + chooser.getSelectedFile());
    }

    private void refresh() {
        status.setText("Live state: " + state.dataFile());
        int deckCount = state.decks().size();
        int containerCount = state.visibleContainers().size();
        int inventoryRows = state.visibleContainers().stream().mapToInt(container -> state.entriesFor(container.id()).size()).sum();
        int deckRows = state.decks().stream().mapToInt(deck -> state.deckSlotsFor(deck.id()).size()).sum();
        info.setText("""
                Local export/import is the current backup and restore path.

                Current backup contents:

                Decks: %d
                Visible containers: %d
                Inventory rows: %d
                Deck rows: %d
                Unresolved card input rows: %d

                This exports/imports the user-owned desktop state JSON. Generated Scryfall bulk files and caches are not included.
                """.formatted(deckCount, containerCount, inventoryRows, deckRows, state.unresolvedCardInputCount()));
        info.setCaretPosition(0);
    }
}
