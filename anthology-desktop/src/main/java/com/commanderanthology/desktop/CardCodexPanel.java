package com.commanderanthology.desktop;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class CardCodexPanel extends JPanel {
    private final ScryfallCardLookupService lookup = new ScryfallCardLookupService();
    private final ScryfallBulkDataService bulkData = new ScryfallBulkDataService();
    private final JTextField query = new JTextField();
    private final SearchTableModel tableModel = new SearchTableModel();
    private final CacheTableModel cacheTableModel = new CacheTableModel();
    private final BulkTableModel bulkTableModel = new BulkTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTable cacheTable = new JTable(cacheTableModel);
    private final JTable bulkTable = new JTable(bulkTableModel);
    private final JTextArea details = new JTextArea();
    private final JLabel status = new JLabel("Local card cache: " + cacheSummary());

    CardCodexPanel() {
        super(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(AnthologyTheme.SURFACE);
        setBorder(AnthologyTheme.panelBorder());
        add(createSearchBar(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        JLabel title = new JLabel("Card Codex");
        title.setForeground(AnthologyTheme.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(title, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        query.addActionListener(event -> search());
        row.add(query, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1, 0, 8, 0));
        buttons.setOpaque(false);
        buttons.add(button("Search", this::search));
        buttons.add(button("Refresh cache status", this::refreshCacheStatus));
        buttons.add(button("Check Scryfall", this::checkBulkData));
        buttons.add(button("Install/update bulk", this::installBulkData));
        buttons.add(button("Adopt legacy SQLite", this::adoptLegacySqlite));
        buttons.add(button("Build SQLite", this::buildSqlite));
        row.add(buttons, BorderLayout.EAST);
        panel.add(row, BorderLayout.CENTER);

        status.setForeground(AnthologyTheme.MUTED);
        panel.add(status, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        JPanel main = new JPanel(new GridLayout(1, 2, 12, 0));
        main.setOpaque(false);

        AnthologyTheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedDetails();
            }
        });
        main.add(AnthologyTheme.scroll(table));

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBackground(AnthologyTheme.SURFACE_ALT);
        details.setForeground(AnthologyTheme.TEXT);
        details.setCaretColor(AnthologyTheme.TEXT);
        details.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        details.setText("Search for a card name to inspect local Scryfall data.");
        main.add(AnthologyTheme.scroll(details));
        body.add(main, BorderLayout.CENTER);

        AnthologyTheme.styleTable(cacheTable);
        JPanel lower = new JPanel(new GridLayout(2, 1, 0, 8));
        lower.setOpaque(false);
        JScrollPane cacheScroll = AnthologyTheme.scroll(cacheTable);
        cacheScroll.setPreferredSize(new Dimension(0, 140));
        lower.add(cacheScroll);
        AnthologyTheme.styleTable(bulkTable);
        lower.add(AnthologyTheme.scroll(bulkTable));
        lower.setPreferredSize(new Dimension(0, 280));
        body.add(lower, BorderLayout.SOUTH);
        refreshCacheStatus();
        return body;
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        AnthologyTheme.styleButton(button);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void search() {
        List<ScryfallCardSelection> rows = lookup.searchCards(query.getText(), 200);
        tableModel.setRows(rows);
        status.setText(rows.size() + " matches   Local card cache: " + cacheSummary());
        if (!rows.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        } else {
            details.setText("No local matches.");
        }
    }

    private void refreshCacheStatus() {
        cacheTableModel.setRows(lookup.cacheStatuses());
        status.setText("Local card cache: " + cacheSummary() + "   API fallback: enabled after local miss");
    }

    private void checkBulkData() {
        status.setText("Checking Scryfall bulk metadata...");
        new SwingWorker<List<ScryfallBulkDataStatus>, Void>() {
            @Override
            protected List<ScryfallBulkDataStatus> doInBackground() {
                return bulkData.checkForUpdates();
            }

            @Override
            protected void done() {
                try {
                    List<ScryfallBulkDataStatus> rows = get();
                    bulkTableModel.setRows(rows);
                    long updates = rows.stream().filter(ScryfallBulkDataStatus::updateAvailable).count();
                    status.setText(updates == 0 ? "Scryfall bulk data is up to date." : updates + " Scryfall bulk files need install/update.");
                } catch (Exception error) {
                    status.setText("Scryfall bulk check failed.");
                    JOptionPane.showMessageDialog(CardCodexPanel.this, rootMessage(error));
                }
            }
        }.execute();
    }

    private void installBulkData() {
        if (JOptionPane.showConfirmDialog(this, "This downloads Scryfall bulk JSON into Anthology AppData. It can be several GB.", "Install/update bulk data", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        status.setText("Installing Scryfall bulk data...");
        new SwingWorker<List<ScryfallBulkDataStatus>, Void>() {
            @Override
            protected List<ScryfallBulkDataStatus> doInBackground() {
                return bulkData.installAll(message -> status.setText(message));
            }

            @Override
            protected void done() {
                try {
                    bulkTableModel.setRows(get());
                    refreshCacheStatus();
                    status.setText("Scryfall bulk data installed.");
                } catch (Exception error) {
                    status.setText("Scryfall bulk install failed.");
                    JOptionPane.showMessageDialog(CardCodexPanel.this, rootMessage(error));
                }
            }
        }.execute();
    }

    private void adoptLegacySqlite() {
        try {
            bulkData.adoptExistingSqliteCache()
                    .ifPresentOrElse(
                            path -> JOptionPane.showMessageDialog(this, "Adopted SQLite cache from " + path + "\ninto " + bulkData.cacheDirectory()),
                            () -> JOptionPane.showMessageDialog(this, "No existing external SQLite cache was found.")
                    );
            refreshCacheStatus();
        } catch (RuntimeException error) {
            JOptionPane.showMessageDialog(this, error.getMessage());
        }
    }

    private void buildSqlite() {
        if (JOptionPane.showConfirmDialog(this, "Build Anthology's SQLite cache from default_cards.json. This can take several minutes.", "Build SQLite card cache", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        status.setText("Building SQLite card cache...");
        new SwingWorker<java.nio.file.Path, Void>() {
            @Override
            protected java.nio.file.Path doInBackground() {
                return bulkData.buildSqliteFromDefaultCards(message -> status.setText(message));
            }

            @Override
            protected void done() {
                try {
                    status.setText("SQLite card cache built: " + get());
                    refreshCacheStatus();
                } catch (Exception error) {
                    status.setText("SQLite card cache build failed.");
                    JOptionPane.showMessageDialog(CardCodexPanel.this, rootMessage(error));
                }
            }
        }.execute();
    }

    private void showSelectedDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        ScryfallCardSelection selection = tableModel.selectionAt(table.convertRowIndexToModel(row));
        ScryfallCardDetails card = lookup.cardDetails(selection.scryfallCardId()).orElse(null);
        if (card == null) {
            details.setText(selection.printingLabel());
            return;
        }
        details.setText(format(card));
        details.setCaretPosition(0);
    }

    private String format(ScryfallCardDetails card) {
        return """
                %s

                Oracle: %s
                Mana: %s
                Type: %s
                Set: %s (%s #%s)
                Rarity: %s

                %s

                Image: %s
                Scryfall ID: %s
                Oracle ID: %s
                """.formatted(
                card.title(),
                blank(card.oracleName()),
                blank(card.manaCost()),
                blank(card.typeLine()),
                blank(card.setName()),
                blank(card.setCode()),
                blank(card.collectorNumber()),
                blank(card.rarity()),
                blank(card.oracleText()),
                blank(card.imageUrl()),
                blank(card.scryfallCardId()),
                blank(card.oracleId())
        );
    }

    private String cacheSummary() {
        List<String> caches = lookup.sqliteCaches().stream()
                .map(path -> path.getParent().toString())
                .collect(Collectors.toList());
        return caches.isEmpty() ? "seed only" : caches.size() + " SQLite cache(s)";
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String rootMessage(Exception error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? error.toString() : current.getMessage();
    }

    private static final class SearchTableModel extends AbstractTableModel {
        private final String[] columns = {"Card", "Oracle", "Set", "No."};
        private List<ScryfallCardSelection> rows = new ArrayList<>();

        void setRows(List<ScryfallCardSelection> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        ScryfallCardSelection selectionAt(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScryfallCardSelection selection = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> selection.printingName();
                case 1 -> selection.oracleName();
                case 2 -> selection.setCode();
                case 3 -> selection.collectorNumber();
                default -> "";
            };
        }
    }

    private static final class CacheTableModel extends AbstractTableModel {
        private final String[] columns = {"Directory", "SQLite", "Default Cards", "Oracle", "Rulings", "Manifest"};
        private List<ScryfallCacheStatus> rows = new ArrayList<>();

        void setRows(List<ScryfallCacheStatus> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScryfallCacheStatus row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.directory().toString();
                case 1 -> row.hasSqlite() ? "Yes" : "No";
                case 2 -> row.hasDefaultCards() ? "Yes" : "No";
                case 3 -> row.hasOracleCards() ? "Yes" : "No";
                case 4 -> row.hasRulings() ? "Yes" : "No";
                case 5 -> row.hasManifest() ? "Yes" : "No";
                default -> "";
            };
        }
    }

    private static final class BulkTableModel extends AbstractTableModel {
        private final String[] columns = {"Bulk File", "Type", "State", "Remote", "Local", "Size"};
        private List<ScryfallBulkDataStatus> rows = new ArrayList<>();

        void setRows(List<ScryfallBulkDataStatus> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScryfallBulkDataStatus row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.name();
                case 1 -> row.type();
                case 2 -> row.stateLabel();
                case 3 -> row.remoteUpdatedAt();
                case 4 -> row.localUpdatedAt() == null ? "-" : row.localUpdatedAt();
                case 5 -> formatBytes(row.size());
                default -> "";
            };
        }

        private String formatBytes(long bytes) {
            if (bytes <= 0) {
                return "unknown";
            }
            double mib = bytes / 1024.0 / 1024.0;
            if (mib < 1024) {
                return String.format("%.1f MB", mib);
            }
            return String.format("%.2f GB", mib / 1024.0);
        }
    }
}
