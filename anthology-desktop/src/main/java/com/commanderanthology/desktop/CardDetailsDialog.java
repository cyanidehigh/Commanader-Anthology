package com.commanderanthology.desktop;

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

final class CardDetailsDialog {
    private static final ScryfallCardLookupService LOOKUP = new ScryfallCardLookupService();
    private static final CardImageCache IMAGE_CACHE = new CardImageCache();

    private CardDetailsDialog() {
    }

    static void show(Component parent, String displayName, String scryfallCardId) {
        ScryfallCardDetails details = LOOKUP.cardDetails(scryfallCardId).orElse(null);
        showDetails(parent, displayName, details);
    }

    static void showIdentity(Component parent, String displayName, String oracleId, String cardName) {
        ScryfallCardDetails details = LOOKUP.cardDetailsForIdentity(oracleId, cardName).orElse(null);
        showDetails(parent, displayName, details);
    }

    private static void showDetails(Component parent, String displayName, ScryfallCardDetails details) {
        JTextArea text = new JTextArea(details == null ? displayName : format(details));
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBackground(AnthologyTheme.SURFACE_ALT);
        text.setForeground(AnthologyTheme.TEXT);
        text.setCaretColor(AnthologyTheme.TEXT);
        text.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        text.setCaretPosition(0);
        JScrollPane textScroll = AnthologyTheme.scroll(text);
        textScroll.setPreferredSize(new Dimension(560, 520));

        JLabel image = new JLabel("No image loaded", JLabel.CENTER);
        image.setForeground(AnthologyTheme.MUTED);
        image.setPreferredSize(new Dimension(280, 520));
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(AnthologyTheme.SURFACE_DEEP);
        imagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(AnthologyTheme.OUTLINE));
        imagePanel.add(image, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textScroll, imagePanel);
        split.setBackground(AnthologyTheme.SURFACE);
        split.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        split.setDividerLocation(560);
        split.setPreferredSize(new Dimension(860, 540));

        if (details != null && details.imageUrl() != null && !details.imageUrl().isBlank()) {
            loadImage(details.scryfallCardId(), details.imageUrl(), image);
        }
        JOptionPane.showMessageDialog(parent, split, displayName, JOptionPane.PLAIN_MESSAGE);
    }

    private static void loadImage(String scryfallCardId, String imageUrl, JLabel target) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                BufferedImage image = IMAGE_CACHE.load(scryfallCardId, imageUrl);
                if (image == null) {
                    return null;
                }
                Image scaled = image.getScaledInstance(260, -1, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon == null) {
                        target.setText("Image unavailable");
                    } else {
                        target.setText("");
                        target.setIcon(icon);
                    }
                } catch (Exception error) {
                    target.setText("Image unavailable");
                }
            }
        }.execute();
    }

    private static String format(ScryfallCardDetails card) {
        return """
                %s

                Oracle: %s
                Mana: %s
                Type: %s
                Set: %s (%s #%s)
                Rarity: %s

                %s
                """.formatted(
                card.title(),
                blank(card.oracleName()),
                blank(card.manaCost()),
                blank(card.typeLine()),
                blank(card.setName()),
                blank(card.setCode()),
                blank(card.collectorNumber()),
                blank(card.rarity()),
                blank(card.oracleText())
        );
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
