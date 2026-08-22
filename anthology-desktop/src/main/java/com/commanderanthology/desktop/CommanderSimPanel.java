package com.commanderanthology.desktop;

import com.commanderanthology.core.commandersim.GameFoundation;
import com.commanderanthology.core.commandersim.CardKind;
import com.commanderanthology.core.commandersim.ObjectType;
import com.commanderanthology.core.commandersim.Player;
import com.commanderanthology.core.commandersim.ZoneType;
import com.commanderanthology.core.deck.Deck;
import com.commanderanthology.core.deck.DeckSection;
import com.commanderanthology.core.deck.DeckSlot;
import com.commanderanthology.game.gdx.CommanderGdxGameLauncher;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

final class CommanderSimPanel extends JPanel {
    private static final DeckChoice RANDOM_DECK = new DeckChoice(null, "Random");

    private final DesktopAppState state;
    private final JComboBox<String> modeSelect = new JComboBox<>(new String[] {"Play", "Auto"});
    private final JComboBox<DeckChoice> playerDeckSelect = new JComboBox<>();
    private final JComboBox<DeckChoice> opponentDeckSelect = new JComboBox<>();
    private final JLabel status = new JLabel("No game loaded.");
    private final Random random = new Random();

    CommanderSimPanel(DesktopAppState state) {
        super(new BorderLayout(0, 18));
        this.state = state;
        setBackground(AnthologyTheme.SURFACE);

        add(createSetupPanel(), BorderLayout.NORTH);
        add(createStatusPanel(), BorderLayout.CENTER);
        refreshDeckChoices();
    }

    private JPanel createSetupPanel() {
        JPanel setup = new JPanel(new BorderLayout(0, 14));
        setup.setOpaque(false);

        JLabel title = new JLabel("Game setup");
        title.setForeground(AnthologyTheme.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        setup.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 10, 12);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, constraints, 0, "Mode", modeSelect);
        addRow(form, constraints, 1, "Player deck", playerDeckSelect);
        addRow(form, constraints, 2, "Opponent deck", opponentDeckSelect);
        setup.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton refresh = new JButton("Refresh decks");
        JButton start = new JButton("Start game");
        AnthologyTheme.styleButton(refresh);
        AnthologyTheme.stylePrimaryButton(start);
        refresh.addActionListener(event -> refreshDeckChoices());
        start.addActionListener(event -> startGame());
        actions.add(refresh);
        actions.add(start);
        setup.add(actions, BorderLayout.SOUTH);
        return setup;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        status.setForeground(AnthologyTheme.MUTED);
        status.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(status);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private void refreshDeckChoices() {
        DeckChoice selectedPlayer = (DeckChoice) playerDeckSelect.getSelectedItem();
        DeckChoice selectedOpponent = (DeckChoice) opponentDeckSelect.getSelectedItem();

        List<DeckChoice> deckChoices = state.decks().stream()
                .map(deck -> new DeckChoice(deck, deck.name()))
                .toList();

        playerDeckSelect.removeAllItems();
        opponentDeckSelect.removeAllItems();
        opponentDeckSelect.addItem(RANDOM_DECK);
        for (DeckChoice choice : deckChoices) {
            playerDeckSelect.addItem(choice);
            opponentDeckSelect.addItem(choice);
        }

        restoreSelection(playerDeckSelect, selectedPlayer);
        restoreSelection(opponentDeckSelect, selectedOpponent == null ? RANDOM_DECK : selectedOpponent);
        if (playerDeckSelect.getSelectedItem() == null && playerDeckSelect.getItemCount() > 0) {
            playerDeckSelect.setSelectedIndex(0);
        }
        if (opponentDeckSelect.getSelectedItem() == null && opponentDeckSelect.getItemCount() > 0) {
            opponentDeckSelect.setSelectedIndex(0);
        }
    }

    private void startGame() {
        if (playerDeckSelect.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Create or import a player deck first.");
            return;
        }
        DeckChoice playerDeck = (DeckChoice) playerDeckSelect.getSelectedItem();
        DeckChoice opponentDeck = resolvedOpponentDeck();
        if (playerDeck == null || playerDeck.deck() == null || opponentDeck == null || opponentDeck.deck() == null) {
            JOptionPane.showMessageDialog(this, "Choose a player deck and opponent deck.");
            return;
        }

        String mode = modeSelect.getSelectedItem().toString();
        GameFoundation game = GameFoundation.buildGame(List.of("Player", "Opponent"), 40);
        loadDeckIntoGame(game, game.playerOrder().get(0), playerDeck.deck());
        loadDeckIntoGame(game, game.playerOrder().get(1), opponentDeck.deck());
        game.prepareOpeningHands(random, 7);
        status.setText("Loaded " + playerDeck.label() + " vs " + opponentDeck.label() + " in " + mode + " mode.");
        openGameplayWindow(game, mode, playerDeck, opponentDeck);
    }

    private void loadDeckIntoGame(GameFoundation game, int playerId, Deck deck) {
        int libraryZoneId = game.players().get(playerId).zoneIds().get(ZoneType.LIBRARY);
        int commandZoneId = game.sharedZoneIds().get(ZoneType.COMMAND);
        for (DeckSlot slot : state.deckSlotsFor(deck.id())) {
            int destinationZoneId = slot.section() == DeckSection.COMMANDER ? commandZoneId : libraryZoneId;
            int quantity = slot.section() == DeckSection.COMMANDER ? 1 : slot.desiredQuantity();
            for (int copy = 0; copy < quantity; copy++) {
                String cardName = slot.oracleName() == null ? slot.cardName() : slot.oracleName();
                if (slot.section() == DeckSection.COMMANDER) {
                    game.addCommander(cardName, playerId, cardKind(slot), 0, 0, 0);
                } else {
                    game.addObject(
                            cardName,
                            ObjectType.CARD,
                            playerId,
                            destinationZoneId,
                            cardKind(slot),
                            0,
                            0,
                            0
                    );
                }
            }
        }
        game.validate();
    }

    private DeckChoice resolvedOpponentDeck() {
        DeckChoice selected = (DeckChoice) opponentDeckSelect.getSelectedItem();
        if (selected == null) {
            return null;
        }
        if (selected.deck() != null) {
            return selected;
        }
        List<DeckChoice> choices = new ArrayList<>();
        for (int index = 0; index < opponentDeckSelect.getItemCount(); index++) {
            DeckChoice choice = opponentDeckSelect.getItemAt(index);
            if (choice.deck() != null) {
                choices.add(choice);
            }
        }
        if (choices.isEmpty()) {
            return null;
        }
        return choices.get(random.nextInt(choices.size()));
    }

    private void openGameplayWindow(GameFoundation game, String mode, DeckChoice playerDeck, DeckChoice opponentDeck) {
        CommanderGdxGameLauncher.launch(game, mode, playerDeck.label(), opponentDeck.label());
    }

    private static Optional<CardKind> cardKind(DeckSlot slot) {
        return switch (slot.section()) {
            case LAND -> Optional.of(CardKind.LAND);
            case CREATURE, COMMANDER -> Optional.of(CardKind.CREATURE);
            case INSTANT -> Optional.of(CardKind.INSTANT);
            case SORCERY -> Optional.of(CardKind.SORCERY);
            default -> Optional.empty();
        };
    }

    private static void restoreSelection(JComboBox<DeckChoice> combo, DeckChoice selection) {
        if (selection == null) {
            return;
        }
        for (int index = 0; index < combo.getItemCount(); index++) {
            DeckChoice candidate = combo.getItemAt(index);
            if (candidate.sameDeck(selection)) {
                combo.setSelectedIndex(index);
                return;
            }
        }
    }

    private static void addRow(JPanel form, GridBagConstraints constraints, int row, String label, java.awt.Component field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(AnthologyTheme.MUTED);
        form.add(labelComponent, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(field, constraints);
    }

    private record DeckChoice(Deck deck, String label) {
        boolean sameDeck(DeckChoice other) {
            if (deck == null || other.deck == null) {
                return deck == null && other.deck == null;
            }
            return deck.id().equals(other.deck.id());
        }

        @Override
        public String toString() {
            if (deck == null) {
                return label;
            }
            String commander = deck.commanderName() == null ? "Commander not set" : deck.commanderName();
            return label + " - " + commander;
        }
    }

}
