package com.commanderanthology.desktop;

import com.commanderanthology.core.collection.CardContainer;
import com.commanderanthology.core.collection.ContainerType;
import com.commanderanthology.core.collection.InventoryEntry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class CollectionPanel extends JPanel {
    private final DesktopAppState state;
    private final DefaultListModel<CardContainer> containerModel = new DefaultListModel<>();
    private final JList<CardContainer> containerList = new JList<>(containerModel);
    private final InventoryTableModel inventoryModel = new InventoryTableModel();
    private final JTable inventoryTable = new JTable(inventoryModel);
    private final JLabel containerTitle = new JLabel("No container selected");
    private final JLabel containerSubtitle = new JLabel("Physical places first");
    private final JLabel statsLabel = new JLabel("0 cards   0 rows");
    private final ScryfallCardLookupService cardLookup = new ScryfallCardLookupService();

    CollectionPanel(DesktopAppState state) {
        super(new BorderLayout(16, 16));
        this.state = state;
        setOpaque(false);
        add(createContainerList(), BorderLayout.WEST);
        add(createContainerDetail(), BorderLayout.CENTER);
        refreshContainers();
    }

    private JPanel createContainerList() {
        JPanel panel = cardPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(340, 0));
        panel.add(titleLabel("Containers"), BorderLayout.NORTH);

        containerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        containerList.setCellRenderer((list, container, index, selected, focus) -> {
            int count = state.entriesFor(container.id()).stream().mapToInt(InventoryEntry::quantity).sum();
            JLabel label = new JLabel("<html><b>" + escape(container.name()) + "</b><br><span style='color:#D8C49A'>"
                    + display(container.type()) + " - " + count + " cards</span></html>");
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            label.setBackground(selected ? AnthologyTheme.SELECTED : AnthologyTheme.SURFACE_ALT);
            label.setForeground(AnthologyTheme.TEXT);
            return label;
        });
        containerList.addListSelectionListener(this::containerChanged);
        panel.add(AnthologyTheme.scroll(containerList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 6));
        buttons.setOpaque(false);
        buttons.add(button("New container", this::addContainer));
        buttons.add(button("Validate all card input", this::validateAllCardInputs));
        buttons.add(button("Edit container", this::editContainer));
        buttons.add(button("Delete container", this::deleteContainer));
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createContainerDetail() {
        JPanel panel = cardPanel(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);
        JPanel titleBlock = new JPanel(new GridLayout(0, 1, 0, 3));
        titleBlock.setOpaque(false);
        containerTitle.setForeground(AnthologyTheme.TEXT);
        containerTitle.setFont(containerTitle.getFont().deriveFont(Font.BOLD, 22f));
        containerSubtitle.setForeground(AnthologyTheme.MUTED);
        statsLabel.setForeground(AnthologyTheme.GOLD);
        titleBlock.add(containerTitle);
        titleBlock.add(containerSubtitle);
        titleBlock.add(statsLabel);
        header.add(titleBlock, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 0, 8, 0));
        buttons.setOpaque(false);
        buttons.add(button("Add card", this::addEntry));
        buttons.add(button("Import", this::importCollection));
        buttons.add(button("Resolve container", this::resolveContainer));
        buttons.add(button("View selected", this::viewSelectedEntry));
        buttons.add(button("Edit selected", this::editSelectedEntry));
        buttons.add(button("Move selected", this::moveSelectedEntry));
        buttons.add(button("Delete selected", this::deleteSelectedEntry));
        header.add(buttons, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        AnthologyTheme.styleTable(inventoryTable);
        AnthologyTheme.styleBadgeColumn(inventoryTable, 2);
        AnthologyTheme.styleBadgeColumn(inventoryTable, 5);
        panel.add(AnthologyTheme.scroll(inventoryTable), BorderLayout.CENTER);
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

    private CardContainer selectedContainer() {
        return containerList.getSelectedValue();
    }

    private InventoryEntry selectedEntry() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return inventoryModel.entryAt(inventoryTable.convertRowIndexToModel(row));
    }

    private void containerChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            refreshDetail();
        }
    }

    private void refreshContainers() {
        String selectedId = selectedContainer() == null ? null : selectedContainer().id();
        containerModel.clear();
        for (CardContainer container : state.visibleContainers()) {
            containerModel.addElement(container);
            if (container.id().equals(selectedId)) {
                containerList.setSelectedValue(container, true);
            }
        }
        if (!containerModel.isEmpty() && containerList.getSelectedIndex() < 0) {
            containerList.setSelectedIndex(0);
        }
        refreshDetail();
    }

    private void refreshDetail() {
        CardContainer container = selectedContainer();
        if (container == null) {
            containerTitle.setText("No container selected");
            containerSubtitle.setText("Physical places first");
            statsLabel.setText("0 cards   0 rows");
            inventoryModel.setRows(List.of());
            return;
        }
        List<InventoryEntry> entries = state.entriesFor(container.id());
        containerTitle.setText(container.name());
        containerSubtitle.setText(display(container.type()));
        statsLabel.setText(entries.stream().mapToInt(InventoryEntry::quantity).sum() + " cards   " + entries.size() + " rows");
        inventoryModel.setRows(entries);
        containerList.repaint();
    }

    private void addContainer() {
        ContainerForm form = ContainerForm.blank();
        if (form.show(this, "New container")) {
            CardContainer container = state.createContainer(form.name(), form.type());
            refreshContainers();
            containerList.setSelectedValue(container, true);
        }
    }

    private void editContainer() {
        CardContainer container = selectedContainer();
        if (container == null) {
            return;
        }
        ContainerForm form = new ContainerForm(container.name(), container.type());
        if (form.show(this, "Edit container")) {
            state.updateContainer(container.id(), form.name(), form.type());
            refreshContainers();
        }
    }

    private void deleteContainer() {
        CardContainer container = selectedContainer();
        if (container == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete " + container.name() + "? Inventory rows inside it will be removed.", "Delete container", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            state.deleteContainer(container.id());
            refreshContainers();
        }
    }

    private void addEntry() {
        CardContainer container = selectedContainer();
        if (container == null) {
            return;
        }
        EntryForm form = EntryForm.blank();
        if (form.show(this, "Add collection card", cardLookup)) {
            state.addInventoryEntry(container.id(), form.toImportedRow());
            refreshDetail();
        }
    }

    private void editSelectedEntry() {
        InventoryEntry entry = selectedEntry();
        if (entry == null) {
            return;
        }
        EntryForm form = new EntryForm(entry.printingName() == null ? entry.cardName() : entry.printingName(), entry.quantity(), entry.foil(), selectionFrom(entry));
        if (form.show(this, "Edit collection card", cardLookup)) {
            state.updateInventoryEntry(entry.id(), form.toImportedRow());
            refreshDetail();
        }
    }

    private void viewSelectedEntry() {
        InventoryEntry entry = selectedEntry();
        if (entry == null) {
            return;
        }
        CardDetailsDialog.show(this, entry.printingName() == null ? entry.cardName() : entry.printingName(), entry.scryfallCardId());
    }

    private void moveSelectedEntry() {
        InventoryEntry entry = selectedEntry();
        CardContainer current = selectedContainer();
        if (entry == null || current == null) {
            return;
        }
        List<CardContainer> targets = state.visibleContainers().stream()
                .filter(container -> !container.id().equals(current.id()))
                .toList();
        if (targets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No other collection container exists.");
            return;
        }
        JComboBox<CardContainer> target = new JComboBox<>(targets.toArray(CardContainer[]::new));
        target.setRenderer((list, container, index, selected, focus) -> new JLabel(container.name() + " (" + display(container.type()) + ")"));
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel("Move " + entry.cardName() + " to"));
        form.add(target);
        if (JOptionPane.showConfirmDialog(this, form, "Move card", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            CardContainer selected = (CardContainer) target.getSelectedItem();
            if (selected != null) {
                state.moveInventoryEntry(entry.id(), selected.id());
                refreshContainers();
            }
        }
    }

    private void deleteSelectedEntry() {
        InventoryEntry entry = selectedEntry();
        if (entry == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remove " + entry.cardName() + " from this container?", "Delete collection card", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            state.deleteInventoryEntry(entry.id());
            refreshDetail();
        }
    }

    private void resolveContainer() {
        CardContainer container = selectedContainer();
        if (container == null) {
            return;
        }
        int resolved = state.resolveAllInventoryEntries(container.id());
        refreshDetail();
        JOptionPane.showMessageDialog(this, "Resolved " + resolved + " collection rows against the local card lookup.");
    }

    private void importCollection() {
        CardContainer container = selectedContainer();
        if (container == null) {
            return;
        }
        JTextArea input = new JTextArea(18, 72);
        input.setLineWrap(false);
        JPanel inputPanel = new JPanel(new BorderLayout(0, 8));
        inputPanel.add(new JLabel("Paste collection rows or CSV"), BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(input), BorderLayout.CENTER);
        JButton loadCsv = button("Load CSV file", () -> loadCsvInto(input));
        inputPanel.add(loadCsv, BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(this, inputPanel, "Import into " + container.name(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        CollectionImportParseResult parseResult = CollectionImportParser.parse(input.getText());
        if (parseResult.rows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No collection rows could be imported.");
            return;
        }

        List<CollectionImportReviewRow> reviewRows = parseResult.rows().stream()
                .map(this::reviewRow)
                .collect(Collectors.toCollection(ArrayList::new));
        CollectionImportReviewModel reviewModel = new CollectionImportReviewModel(reviewRows);
        JTable reviewTable = new JTable(reviewModel);
        AnthologyTheme.styleTable(reviewTable);
        AnthologyTheme.styleBadgeColumn(reviewTable, 3);
        AnthologyTheme.styleBadgeColumn(reviewTable, 5);

        JPanel reviewPanel = new JPanel(new BorderLayout(0, 8));
        JLabel summary = new JLabel(reviewSummary(reviewRows));
        reviewPanel.add(summary, BorderLayout.NORTH);
        reviewPanel.add(AnthologyTheme.scroll(reviewTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 0, 8, 0));
        actions.add(button("Choose selected printing", () -> {
            int row = reviewTable.getSelectedRow();
            if (row >= 0) {
                choosePrinting(reviewRows.get(reviewTable.convertRowIndexToModel(row)), reviewModel);
                summary.setText(reviewSummary(reviewRows));
            }
        }));
        actions.add(button("Resolve all", () -> {
            for (int index = 0; index < reviewRows.size(); index++) {
                reviewRows.set(index, reviewRow(reviewRows.get(index).toImportedRow()));
            }
            reviewModel.setRows(reviewRows);
            summary.setText(reviewSummary(reviewRows));
        }));
        reviewPanel.add(actions, BorderLayout.SOUTH);

        String warnings = parseResult.warnings().isEmpty() ? "" : "\n\nSkipped:\n" + parseResult.warnings().stream().limit(8).collect(Collectors.joining("\n"));
        if (JOptionPane.showConfirmDialog(this, reviewPanel, "Review collection import" + warnings, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        List<ImportedCollectionRow> rows = reviewRows.stream()
                .map(CollectionImportReviewRow::toImportedRow)
                .toList();
        state.addImportedInventoryEntries(container.id(), rows);
        refreshDetail();
        JOptionPane.showMessageDialog(this, "Imported " + rows.size() + " collection rows.");
    }

    private void loadCsvInto(JTextArea input) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            input.setText(Files.readString(file.toPath()));
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, "Could not read CSV file: " + error.getMessage());
        }
    }

    private CollectionImportReviewRow reviewRow(ImportedCollectionRow row) {
        ScryfallCardSelection selected = null;
        List<ScryfallCardSelection> options = List.of();
        if (row.scryfallCardId() != null) {
            selected = cardLookup.lookupCardById(row.scryfallCardId()).orElse(null);
            if (selected != null) {
                options = List.of(selected);
            }
        }
        if (selected == null) {
            options = cardLookup.lookupCardOptions(row.cardName(), row.setCode());
            if (options.size() == 1) {
                selected = options.get(0);
            }
        }
        return new CollectionImportReviewRow(row, options, selected);
    }

    private void choosePrinting(CollectionImportReviewRow row, CollectionImportReviewModel model) {
        if (row.options().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No printing options found for " + row.row().cardName() + ".");
            return;
        }
        JComboBox<ScryfallCardSelection> combo = new JComboBox<>(row.options().toArray(ScryfallCardSelection[]::new));
        combo.setRenderer((list, selection, index, selected, focus) -> new JLabel(selection == null ? "" : selection.printingLabel()));
        if (row.selected() != null) {
            combo.setSelectedItem(row.selected());
        }
        if (JOptionPane.showConfirmDialog(this, combo, "Choose printing", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            row.setSelected((ScryfallCardSelection) combo.getSelectedItem());
            model.fireTableDataChanged();
        }
    }

    private static String reviewSummary(List<CollectionImportReviewRow> rows) {
        long resolved = rows.stream().filter(row -> row.selected() != null).count();
        long choices = rows.stream().filter(row -> row.selected() == null && !row.options().isEmpty()).count();
        long unresolved = rows.size() - resolved - choices;
        return rows.size() + " rows   " + resolved + " resolved   " + choices + " need choice   " + unresolved + " manual/unresolved";
    }

    private void validateAllCardInputs() {
        int before = state.unresolvedCardInputCount();
        int resolved = state.validateAllCardInputs();
        int after = state.unresolvedCardInputCount();
        refreshContainers();
        JOptionPane.showMessageDialog(this, "Validated current card input against the local Scryfall cache.\n"
                + "Resolved this pass: " + resolved + "\n"
                + "Unresolved before: " + before + "\n"
                + "Unresolved after: " + after);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String display(ContainerType type) {
        String text = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static ScryfallCardSelection selectionFrom(InventoryEntry entry) {
        if (entry.scryfallCardId() == null || entry.oracleId() == null || entry.oracleName() == null) {
            return null;
        }
        return new ScryfallCardSelection(
                entry.scryfallCardId(),
                entry.oracleId(),
                entry.oracleName(),
                entry.printingName() == null ? entry.cardName() : entry.printingName(),
                entry.setCode() == null ? "" : entry.setCode(),
                entry.collectorNumber() == null ? "" : entry.collectorNumber()
        );
    }

    private static final class InventoryTableModel extends AbstractTableModel {
        private final String[] columns = {"Qty", "Card", "Status", "Oracle", "Printing", "Foil"};
        private List<InventoryEntry> rows = new ArrayList<>();

        void setRows(List<InventoryEntry> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        InventoryEntry entryAt(int row) {
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
            InventoryEntry entry = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> "x" + entry.quantity();
                case 1 -> entry.printingName() == null ? entry.cardName() : entry.printingName();
                case 2 -> entry.oracleName() == null ? "Manual" : "Resolved printing";
                case 3 -> entry.oracleName() == null ? "" : entry.oracleName();
                case 4 -> printing(entry);
                case 5 -> entry.foil() ? "Foil" : "";
                default -> "";
            };
        }

        private String printing(InventoryEntry entry) {
            if (entry.setCode() == null || entry.setCode().isBlank()) {
                return "";
            }
            if (entry.collectorNumber() == null || entry.collectorNumber().isBlank()) {
                return entry.setCode();
            }
            return entry.setCode() + " #" + entry.collectorNumber();
        }
    }

    private static final class CollectionImportReviewModel extends AbstractTableModel {
        private final String[] columns = {"Qty", "Card", "Requested", "Status", "Selected printing", "Foil"};
        private List<CollectionImportReviewRow> rows;

        CollectionImportReviewModel(List<CollectionImportReviewRow> rows) {
            this.rows = rows;
        }

        void setRows(List<CollectionImportReviewRow> rows) {
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
            CollectionImportReviewRow row = rows.get(rowIndex);
            ImportedCollectionRow imported = row.row();
            return switch (columnIndex) {
                case 0 -> "x" + imported.quantity();
                case 1 -> imported.cardName();
                case 2 -> requested(imported);
                case 3 -> status(row);
                case 4 -> row.selected() == null ? "" : row.selected().printingLabel();
                case 5 -> imported.foil() ? "Foil" : "";
                default -> "";
            };
        }

        private String requested(ImportedCollectionRow row) {
            if (row.scryfallCardId() != null) {
                return "Scryfall " + row.scryfallCardId();
            }
            String set = row.setCode() == null ? "" : row.setCode();
            String collector = row.collectorNumber() == null ? "" : " #" + row.collectorNumber();
            return (set + collector).trim();
        }

        private String status(CollectionImportReviewRow row) {
            if (row.selected() != null) {
                return "Resolved";
            }
            if (!row.options().isEmpty()) {
                return "Choose printing";
            }
            return "Manual";
        }
    }

    private static final class CollectionImportReviewRow {
        private final ImportedCollectionRow row;
        private final List<ScryfallCardSelection> options;
        private ScryfallCardSelection selected;

        CollectionImportReviewRow(ImportedCollectionRow row, List<ScryfallCardSelection> options, ScryfallCardSelection selected) {
            this.row = row;
            this.options = options;
            this.selected = selected;
        }

        ImportedCollectionRow row() {
            return row;
        }

        List<ScryfallCardSelection> options() {
            return options;
        }

        ScryfallCardSelection selected() {
            return selected;
        }

        void setSelected(ScryfallCardSelection selected) {
            this.selected = selected;
        }

        ImportedCollectionRow toImportedRow() {
            if (selected == null) {
                return row;
            }
            return new ImportedCollectionRow(
                    row.quantity(),
                    selected.printingName(),
                    selected.setCode(),
                    selected.collectorNumber(),
                    selected.scryfallCardId(),
                    row.foil()
            );
        }
    }

    private static final class ContainerForm {
        private final JTextField name;
        private final JComboBox<ContainerType> type;

        ContainerForm(String name, ContainerType type) {
            this.name = new JTextField(name == null ? "" : name);
            this.type = new JComboBox<>(new ContainerType[]{ContainerType.BINDER, ContainerType.BOX, ContainerType.SET, ContainerType.ORDERED, ContainerType.PROXY, ContainerType.OTHER});
            this.type.setSelectedItem(type == null ? ContainerType.BOX : type);
        }

        static ContainerForm blank() {
            return new ContainerForm("", ContainerType.BOX);
        }

        boolean show(JPanel parent, String title) {
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
            form.add(new JLabel("Container name"));
            form.add(name);
            form.add(new JLabel("Type"));
            form.add(type);
            return JOptionPane.showConfirmDialog(parent, form, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION
                    && !name.getText().isBlank();
        }

        String name() {
            return name.getText();
        }

        ContainerType type() {
            return (ContainerType) type.getSelectedItem();
        }
    }

    private static final class EntryForm {
        private final JTextField cardName;
        private final JSpinner quantity;
        private final JCheckBox foil;
        private final JLabel selectedLabel = new JLabel("No printing selected");
        private ScryfallCardSelection selected;

        EntryForm(String cardName, int quantity, boolean foil) {
            this(cardName, quantity, foil, null);
        }

        EntryForm(String cardName, int quantity, boolean foil, ScryfallCardSelection selected) {
            this.cardName = new JTextField(cardName == null ? "" : cardName);
            this.quantity = new JSpinner(new SpinnerNumberModel(quantity, 1, 999, 1));
            this.foil = new JCheckBox("Foil", foil);
            this.selected = selected;
            updateSelectedLabel();
        }

        static EntryForm blank() {
            return new EntryForm("", 1, false);
        }

        boolean show(JPanel parent, String title, ScryfallCardLookupService cardLookup) {
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
            form.add(new JLabel("Card name"));
            form.add(cardName);
            form.add(new JLabel("Quantity"));
            form.add(quantity);
            form.add(foil);
            form.add(selectedLabel);
            JButton choose = new JButton("Choose printing");
            AnthologyTheme.styleButton(choose);
            choose.addActionListener(event -> choosePrinting(parent, cardLookup));
            form.add(choose);
            return JOptionPane.showConfirmDialog(parent, form, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION
                    && !cardName.getText().isBlank();
        }

        ImportedCollectionRow toImportedRow() {
            if (selected == null) {
                return new ImportedCollectionRow((Integer) quantity.getValue(), cardName.getText(), null, null, null, foil.isSelected());
            }
            return new ImportedCollectionRow(
                    (Integer) quantity.getValue(),
                    selected.printingName(),
                    selected.setCode(),
                    selected.collectorNumber(),
                    selected.scryfallCardId(),
                    foil.isSelected()
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
