package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.InventoryEntry;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class DeckBuilderPanel extends JPanel {
    private final DesktopAppState state;
    private final DefaultListModel<Deck> deckModel = new DefaultListModel<>();
    private final JList<Deck> deckList = new JList<>(deckModel);
    private final DeckSlotTableModel slotTableModel = new DeckSlotTableModel();
    private final JTable slotTable = new JTable(slotTableModel);
    private final JLabel deckTitle = new JLabel("No deck selected");
    private final JLabel commanderLabel = new JLabel("Commander not set");
    private final JLabel statsLabel = new JLabel("0 wanted   0 assigned   0 available   0 missing");
    private final ScryfallCardLookupService cardLookup = new ScryfallCardLookupService();

    DeckBuilderPanel(DesktopAppState state) {
        super(new BorderLayout(16, 16));
        this.state = state;
        setOpaque(false);
        add(createDeckList(), BorderLayout.WEST);
        add(createDeckDetail(), BorderLayout.CENTER);
        refreshDecks();
    }

    private JPanel createDeckList() {
        JPanel panel = cardPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(340, 0));
        JLabel title = titleLabel("Decks");
        panel.add(title, BorderLayout.NORTH);

        deckList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deckList.setCellRenderer((list, deck, index, selected, focus) -> {
            JLabel label = new JLabel("<html><b>" + escape(deck.name()) + "</b><br><span style='color:#D8C49A'>"
                    + escape(deck.commanderName() == null ? "Commander not set" : deck.commanderName()) + "</span></html>");
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            label.setBackground(selected ? AnthologyTheme.SELECTED : AnthologyTheme.SURFACE_ALT);
            label.setForeground(AnthologyTheme.TEXT);
            return label;
        });
        deckList.addListSelectionListener(this::deckChanged);
        panel.add(AnthologyTheme.scroll(deckList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 6));
        buttons.setOpaque(false);
        buttons.add(button("New deck", this::addDeck));
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createDeckDetail() {
        JPanel panel = cardPanel(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);
        JPanel titleBlock = new JPanel(new GridLayout(0, 1, 0, 3));
        titleBlock.setOpaque(false);
        deckTitle.setForeground(AnthologyTheme.TEXT);
        deckTitle.setFont(deckTitle.getFont().deriveFont(Font.BOLD, 22f));
        commanderLabel.setForeground(AnthologyTheme.MUTED);
        statsLabel.setForeground(AnthologyTheme.GOLD);
        titleBlock.add(deckTitle);
        titleBlock.add(commanderLabel);
        titleBlock.add(statsLabel);
        header.add(titleBlock, BorderLayout.CENTER);

        JPanel deckButtons = new JPanel(new GridLayout(1, 0, 8, 0));
        deckButtons.setOpaque(false);
        deckButtons.add(button("Add card", this::addSlot));
        deckButtons.add(button("Import", this::importDeck));
        deckButtons.add(button("More...", this::showDeckActions));
        header.add(deckButtons, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        AnthologyTheme.styleTable(slotTable);
        AnthologyTheme.styleBadgeColumn(slotTable, 0);
        panel.add(AnthologyTheme.scroll(slotTable), BorderLayout.CENTER);

        JPanel rowButtons = new JPanel(new GridLayout(1, 0, 8, 0));
        rowButtons.setOpaque(false);
        rowButtons.add(button("Assign selected", this::assignSelected));
        rowButtons.add(button("Set commander", this::setSelectedCommander));
        rowButtons.add(button("More selected...", this::showSelectedCardActions));
        panel.add(rowButtons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel cardPanel(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        AnthologyTheme.stylePanel(panel);
        return panel;
    }

    private JLabel titleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(AnthologyTheme.TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        return label;
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        AnthologyTheme.styleButton(button);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void deckChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            refreshDetail();
        }
    }

    private Deck selectedDeck() {
        return deckList.getSelectedValue();
    }

    private DeckSlot selectedSlot() {
        int row = slotTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return slotTableModel.slotAt(slotTable.convertRowIndexToModel(row));
    }

    private void refreshDecks() {
        String selectedId = selectedDeck() == null ? null : selectedDeck().id();
        deckModel.clear();
        for (Deck deck : state.decks()) {
            deckModel.addElement(deck);
            if (deck.id().equals(selectedId)) {
                deckList.setSelectedValue(deck, true);
            }
        }
        if (!deckModel.isEmpty() && deckList.getSelectedIndex() < 0) {
            deckList.setSelectedIndex(0);
        }
        refreshDetail();
    }

    private void refreshDetail() {
        Deck deck = selectedDeck();
        if (deck == null) {
            deckTitle.setText("No deck selected");
            commanderLabel.setText("Commander not set");
            statsLabel.setText("0 wanted   0 assigned   0 available   0 missing");
            slotTableModel.setRows(List.of());
            return;
        }

        List<DeckSlotRow> rows = state.deckSlotsFor(deck.id()).stream()
                .map(slot -> new DeckSlotRow(
                        slot,
                        state.assignedQuantityFor(slot.id()),
                        state.assignedEntriesFor(slot.id()),
                        state.availableEntriesFor(slot)
                ))
                .toList();
        int wanted = rows.stream().mapToInt(row -> row.slot().desiredQuantity()).sum();
        int assigned = rows.stream().mapToInt(DeckSlotRow::assigned).sum();
        int available = rows.stream()
                .mapToInt(row -> Math.min(
                        Math.max(0, row.slot().desiredQuantity() - row.assigned()),
                        row.available().stream().mapToInt(InventoryEntry::quantity).sum()
                ))
                .sum();
        int missing = Math.max(0, wanted - assigned - available);

        deckTitle.setText(deck.name());
        commanderLabel.setText(deck.commanderName() == null ? "Commander not set" : deck.commanderName());
        statsLabel.setText(wanted + " wanted   " + assigned + " assigned   " + available + " available   " + missing + " missing");
        slotTableModel.setRows(rows);
    }

    private void addDeck() {
        DeckForm form = DeckForm.blank();
        if (form.show(this, "New deck", cardLookup)) {
            Deck deck = state.createDeck(form.name(), form.commander());
            refreshDecks();
            deckList.setSelectedValue(deck, true);
        }
    }

    private void editDeck() {
        Deck deck = selectedDeck();
        if (deck == null) {
            return;
        }
        DeckForm form = new DeckForm(deck.name(), deck.commanderName());
        if (form.show(this, "Edit deck", cardLookup)) {
            state.updateDeck(deck.id(), form.name(), form.commander());
            refreshDecks();
        }
    }

    private void deleteDeck() {
        Deck deck = selectedDeck();
        if (deck == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete " + deck.name() + " and its deck rows?", "Delete deck", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            state.deleteDeck(deck.id());
            refreshDecks();
        }
    }

    private void showDeckActions() {
        Deck deck = selectedDeck();
        if (deck == null) {
            JOptionPane.showMessageDialog(this, "Select a deck first.");
            return;
        }
        String[] actions = {"Edit deck", "Resolve deck", "Delete deck"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose an action for " + deck.name() + ".",
                "Deck actions",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                actions,
                actions[0]
        );
        switch (choice) {
            case 0 -> editDeck();
            case 1 -> resolveDeck();
            case 2 -> deleteDeck();
            default -> {
            }
        }
    }

    private void addSlot() {
        Deck deck = selectedDeck();
        if (deck == null) {
            return;
        }
        SlotForm form = SlotForm.blank();
        if (form.show(this, "Add deck card", cardLookup)) {
            state.addDeckSlot(deck.id(), form.toImportedRow());
            refreshDetail();
        }
    }

    private void editSelectedSlot() {
        DeckSlot slot = selectedSlot();
        if (slot == null) {
            return;
        }
        SlotForm form = new SlotForm(slot.cardName(), slot.desiredQuantity(), slot.section(), selectionFrom(slot));
        if (form.show(this, "Edit deck card", cardLookup)) {
            state.updateDeckSlot(slot.id(), form.toImportedRow());
            refreshDetail();
        }
    }

    private void deleteSelectedSlot() {
        DeckSlot slot = selectedSlot();
        if (slot == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remove " + slot.cardName() + " from this deck?", "Delete deck card", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            state.deleteDeckSlot(slot.id());
            refreshDetail();
        }
    }

    private void viewSelectedSlot() {
        DeckSlot slot = selectedSlot();
        if (slot == null) {
            return;
        }
        String displayName = slot.oracleName() == null ? slot.cardName() : slot.oracleName();
        if (slot.preferredScryfallCardId() != null) {
            CardDetailsDialog.show(this, displayName, slot.preferredScryfallCardId());
        } else {
            CardDetailsDialog.showIdentity(this, displayName, slot.oracleId(), slot.cardName());
        }
    }

    private void showSelectedCardActions() {
        DeckSlot slot = selectedSlot();
        if (slot == null) {
            JOptionPane.showMessageDialog(this, "Select a deck card first.");
            return;
        }
        String[] actions = {"View", "Edit", "Unassign", "Delete"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose an action for " + slot.cardName() + ".",
                "Selected card actions",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                actions,
                actions[0]
        );
        switch (choice) {
            case 0 -> viewSelectedSlot();
            case 1 -> editSelectedSlot();
            case 2 -> unassignSelected();
            case 3 -> deleteSelectedSlot();
            default -> {
            }
        }
    }

    private void assignSelected() {
        DeckSlot slot = selectedSlot();
        if (slot == null) {
            return;
        }
        List<InventoryEntry> available = state.availableEntriesFor(slot);
        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching available collection card found.");
            return;
        }
        JComboBox<InventoryEntry> chooser = new JComboBox<>(available.toArray(InventoryEntry[]::new));
        chooser.setRenderer((list, entry, index, selected, focus) -> new JLabel(entry == null ? "" : inventoryChoiceLabel(entry)));
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Choose physical copy for " + slot.cardName()), BorderLayout.NORTH);
        panel.add(chooser, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, panel, "Assign collection copy", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            InventoryEntry selected = (InventoryEntry) chooser.getSelectedItem();
            if (selected != null && !state.assignInventoryEntryById(slot, selected.id())) {
                JOptionPane.showMessageDialog(this, "That collection copy is no longer available.");
            }
        }
        refreshDetail();
    }

    private String inventoryChoiceLabel(InventoryEntry entry) {
        String printing = entry.printingName() == null ? entry.cardName() : entry.printingName();
        String set = entry.setCode() == null ? "" : " - " + entry.setCode() + " #" + entry.collectorNumber();
        String foil = entry.foil() ? " - Foil" : "";
        return "x" + entry.quantity() + " " + printing + set + foil + " - " + state.containerName(entry.containerId());
    }

    private void unassignSelected() {
        DeckSlot slot = selectedSlot();
        if (slot == null) {
            return;
        }
        if (!state.unassignOne(slot)) {
            JOptionPane.showMessageDialog(this, "No assigned copy found for this row.");
        }
        refreshDetail();
    }

    private void setSelectedCommander() {
        DeckSlot slot = selectedSlot();
        if (slot == null) {
            JOptionPane.showMessageDialog(this, "Select a deck card first.");
            return;
        }
        try {
            Deck deck = state.setCommanderFromSlot(slot.id());
            refreshDecks();
            deckList.setSelectedValue(deck, true);
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Commander issue", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resolveDeck() {
        Deck deck = selectedDeck();
        if (deck == null) {
            return;
        }
        int resolved = state.resolveAllDeckSlots(deck.id());
        refreshDetail();
        JOptionPane.showMessageDialog(this, "Resolved " + resolved + " deck rows against the local card lookup.");
    }

    private void importDeck() {
        Deck deck = selectedDeck();
        if (deck == null) {
            return;
        }
        JTextArea input = new JTextArea(20, 64);
        input.setLineWrap(false);
        JPanel form = new JPanel(new BorderLayout(0, 8));
        form.add(new JLabel("Paste decklist"), BorderLayout.NORTH);
        form.add(new JScrollPane(input), BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(this, form, "Import into " + deck.name(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        DeckImportParseResult result = DeckImportParser.parse(input.getText());
        if (result.rows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No deck cards could be imported.");
            return;
        }

        List<DeckImportReviewRow> reviewRows = result.rows().stream()
                .map(this::reviewRow)
                .collect(Collectors.toCollection(ArrayList::new));
        DeckImportReviewModel reviewModel = new DeckImportReviewModel(reviewRows);
        JTable reviewTable = new JTable(reviewModel);
        AnthologyTheme.styleTable(reviewTable);
        AnthologyTheme.styleBadgeColumn(reviewTable, 2);

        JPanel reviewPanel = new JPanel(new BorderLayout(0, 8));
        JLabel summary = new JLabel(reviewSummary(reviewRows));
        reviewPanel.add(summary, BorderLayout.NORTH);
        reviewPanel.add(AnthologyTheme.scroll(reviewTable), BorderLayout.CENTER);
        String warnings = result.warnings().isEmpty() ? "" : "\n\nSkipped:\n" + result.warnings().stream().limit(8).collect(Collectors.joining("\n"));
        if (JOptionPane.showConfirmDialog(this, reviewPanel, "Review deck import" + warnings, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        List<ImportedDeckRow> rows = reviewRows.stream()
                .map(DeckImportReviewRow::toImportedRow)
                .toList();
        state.addImportedDeckSlots(deck.id(), rows);
        refreshDetail();
        JOptionPane.showMessageDialog(this, "Imported " + rows.size() + " deck rows.");
    }

    private DeckImportReviewRow reviewRow(ImportedDeckRow row) {
        ScryfallCardSelection matched = null;
        if (row.scryfallCardId() != null) {
            matched = cardLookup.lookupCardById(row.scryfallCardId()).orElse(null);
        }
        if (matched == null) {
            matched = cardLookup.lookupPreferredCard(row.cardName(), row.setCode()).orElse(null);
        }
        return new DeckImportReviewRow(row, matched);
    }

    private static String reviewSummary(List<DeckImportReviewRow> rows) {
        long matched = rows.stream().filter(row -> row.matched() != null).count();
        long unmatched = rows.size() - matched;
        return rows.size() + " rows   " + matched + " matched   " + unmatched + " unmatched";
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private record DeckSlotRow(DeckSlot slot, int assigned, List<InventoryEntry> assignedEntries, List<InventoryEntry> available) {
    }

    private final class DeckSlotTableModel extends AbstractTableModel {
        private final String[] columns = {"Status", "Qty", "Card", "Assigned", "Available", "Copy / source"};
        private List<DeckSlotRow> rows = new ArrayList<>();

        void setRows(List<DeckSlotRow> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        DeckSlot slotAt(int row) {
            return rows.get(row).slot();
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
            DeckSlotRow row = rows.get(rowIndex);
            DeckSlot slot = row.slot();
            int available = row.available().stream().mapToInt(InventoryEntry::quantity).sum();
            return switch (columnIndex) {
                case 0 -> status(slot, row.assigned(), available);
                case 1 -> "x" + slot.desiredQuantity();
                case 2 -> slot.oracleName() == null ? slot.cardName() : slot.oracleName();
                case 3 -> row.assigned() + "/" + slot.desiredQuantity();
                case 4 -> Integer.toString(available);
                case 5 -> copyText(slot, row);
                default -> "";
            };
        }

        private String copyText(DeckSlot slot, DeckSlotRow row) {
            InventoryEntry assigned = row.assignedEntries().isEmpty() ? null : row.assignedEntries().get(0);
            if (assigned != null) {
                String printing = assigned.printingName() == null ? assigned.cardName() : assigned.printingName();
                String source = state.assignmentSourceNameFor(slot.id(), assigned.id());
                String set = assigned.setCode() == null ? "" : " (" + assigned.setCode() + " #" + assigned.collectorNumber() + ")";
                return printing + set + (source == null ? "" : " from " + source);
            }
            InventoryEntry available = row.available().isEmpty() ? null : row.available().get(0);
            if (available != null) {
                return available.cardName() + " in " + state.containerName(available.containerId());
            }
            return "";
        }
    }

    private static final class DeckImportReviewModel extends AbstractTableModel {
        private final String[] columns = {"Qty", "Card", "Status"};
        private List<DeckImportReviewRow> rows;

        DeckImportReviewModel(List<DeckImportReviewRow> rows) {
            this.rows = rows;
        }

        void setRows(List<DeckImportReviewRow> rows) {
            this.rows = rows;
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
            DeckImportReviewRow row = rows.get(rowIndex);
            ImportedDeckRow imported = row.row();
            return switch (columnIndex) {
                case 0 -> "x" + imported.quantity();
                case 1 -> imported.cardName();
                case 2 -> status(row);
                default -> "";
            };
        }

        private String status(DeckImportReviewRow row) {
            if (row.matched() != null) {
                return "Matched";
            }
            return "Unmatched";
        }
    }

    private static final class DeckImportReviewRow {
        private final ImportedDeckRow row;
        private final ScryfallCardSelection matched;

        DeckImportReviewRow(ImportedDeckRow row, ScryfallCardSelection matched) {
            this.row = row;
            this.matched = matched;
        }

        ImportedDeckRow row() {
            return row;
        }

        ScryfallCardSelection matched() {
            return matched;
        }

        ImportedDeckRow toImportedRow() {
            if (matched == null) {
                return row;
            }
            return new ImportedDeckRow(
                    row.quantity(),
                    matched.oracleName(),
                    row.section()
            );
        }
    }

    private static String status(DeckSlot slot, int assigned, int available) {
        if (assigned >= slot.desiredQuantity()) {
            return "Assigned";
        }
        if (assigned > 0) {
            return "Partial";
        }
        if (available > 0) {
            return "Available";
        }
        return "Missing";
    }

    private static String symbol(DeckSlot slot, int assigned, int available) {
        if (assigned >= slot.desiredQuantity()) {
            return "✓";
        }
        if (assigned > 0) {
            return "◐";
        }
        if (available > 0) {
            return "○";
        }
        return "●";
    }

    private static ScryfallCardSelection selectionFrom(DeckSlot slot) {
        if (slot.preferredScryfallCardId() == null || slot.oracleId() == null || slot.oracleName() == null) {
            return null;
        }
        return new ScryfallCardSelection(
                slot.preferredScryfallCardId(),
                slot.oracleId(),
                slot.oracleName(),
                slot.preferredPrintingName() == null ? slot.cardName() : slot.preferredPrintingName(),
                slot.preferredSetCode() == null ? "" : slot.preferredSetCode(),
                slot.preferredCollectorNumber() == null ? "" : slot.preferredCollectorNumber()
        );
    }

    private static final class DeckForm {
        private final JTextField name;
        private final JTextField commander;

        DeckForm(String name, String commander) {
            this.name = new JTextField(name == null ? "" : name);
            this.commander = new JTextField(commander == null ? "" : commander);
        }

        static DeckForm blank() {
            return new DeckForm("", "");
        }

        boolean show(JPanel parent, String title, ScryfallCardLookupService cardLookup) {
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
            form.add(new JLabel("Deck name"));
            form.add(name);
            form.add(new JLabel("Commander"));
            form.add(commander);
            while (true) {
                if (JOptionPane.showConfirmDialog(parent, form, title, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                    return false;
                }
                if (name.getText().isBlank()) {
                    JOptionPane.showMessageDialog(parent, "Deck name is required.", "Deck issue", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                CommanderValidation validation = cardLookup.validateCommander(commander.getText());
                if (!validation.valid()) {
                    JOptionPane.showMessageDialog(parent, validation.message(), "Commander issue", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                commander.setText(validation.cardName());
                return true;
            }
        }

        String name() {
            return name.getText();
        }

        String commander() {
            return commander.getText();
        }
    }

    private static final class SlotForm {
        private final JTextField cardName;
        private final JSpinner quantity;
        private final JLabel selectedLabel = new JLabel("No printing selected");
        private ScryfallCardSelection selected;
        private final DeckSection section;

        SlotForm(String cardName, int quantity, DeckSection section) {
            this(cardName, quantity, section, null);
        }

        SlotForm(String cardName, int quantity, DeckSection section, ScryfallCardSelection selected) {
            this.cardName = new JTextField(cardName == null ? "" : cardName);
            this.quantity = new JSpinner(new SpinnerNumberModel(quantity, 1, 999, 1));
            this.section = section == null ? DeckSection.OTHER : section;
            this.selected = selected;
            updateSelectedLabel();
        }

        static SlotForm blank() {
            return new SlotForm("", 1, DeckSection.OTHER);
        }

        boolean show(JPanel parent, String title, ScryfallCardLookupService cardLookup) {
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
            form.add(new JLabel("Card name"));
            form.add(cardName);
            form.add(new JLabel("Quantity"));
            form.add(quantity);
            form.add(selectedLabel);
            JButton choose = new JButton("Choose printing");
            AnthologyTheme.styleButton(choose);
            choose.addActionListener(event -> choosePrinting(parent, cardLookup));
            form.add(choose);
            return JOptionPane.showConfirmDialog(parent, form, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION
                    && !cardName.getText().isBlank();
        }

        ImportedDeckRow toImportedRow() {
            if (selected == null) {
                return new ImportedDeckRow((Integer) quantity.getValue(), cardName.getText(), section);
            }
            return new ImportedDeckRow(
                    (Integer) quantity.getValue(),
                    selected.printingName(),
                    section,
                    selected.scryfallCardId(),
                    selected.setCode(),
                    selected.collectorNumber()
            );
        }

        private void choosePrinting(JPanel parent, ScryfallCardLookupService cardLookup) {
            List<ScryfallCardSelection> options = cardLookup.lookupCardOptions(cardName.getText(), null);
            if (options.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No printing options found for " + cardName.getText() + ".");
                return;
            }
            JComboBox<ScryfallCardSelection> combo = new JComboBox<>(options.toArray(ScryfallCardSelection[]::new));
            combo.setRenderer((list, selection, index, selectedRow, focus) -> new JLabel(selection == null ? "" : selection.printingLabel()));
            if (selected != null) {
                combo.setSelectedItem(selected);
            }
            if (JOptionPane.showConfirmDialog(parent, combo, "Choose printing", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                selected = (ScryfallCardSelection) combo.getSelectedItem();
                if (selected != null) {
                    cardName.setText(selected.printingName());
                }
                updateSelectedLabel();
            }
        }

        private void updateSelectedLabel() {
            selectedLabel.setText(selected == null ? "No printing selected" : selected.printingLabel());
        }
    }
}
