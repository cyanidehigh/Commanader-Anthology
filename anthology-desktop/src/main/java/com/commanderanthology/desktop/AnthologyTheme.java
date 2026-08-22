package com.commanderanthology.desktop;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;

final class AnthologyTheme {
    static final Color BACKGROUND = new Color(0x050505);
    static final Color SIDEBAR = new Color(0x090806);
    static final Color SURFACE = new Color(0x111111);
    static final Color SURFACE_ALT = new Color(0x151310);
    static final Color SURFACE_DEEP = new Color(0x0D0C0A);
    static final Color SURFACE_HEADER = new Color(0x211A10);
    static final Color SELECTED = new Color(0x2A1E0D);
    static final Color GOLD = new Color(0xE0A52F);
    static final Color GOLD_DARK = new Color(0x6E4A12);
    static final Color RED = new Color(0xC12A1D);
    static final Color TEXT = new Color(0xF4E8D0);
    static final Color MUTED = new Color(0xD8C49A);
    static final Color OUTLINE = new Color(0x7B6340);
    static final Color GOOD_BG = new Color(0x26321F);
    static final Color GOOD_TEXT = new Color(0xC8E6A0);
    static final Color WARNING_BG = new Color(0x332713);
    static final Color WARNING_TEXT = new Color(0xE0A52F);
    static final Color NEUTRAL_BG = new Color(0x2A2420);
    static final Color NEUTRAL_TEXT = new Color(0xE8D5B0);
    static final Color FOIL_BG = new Color(0x2A2234);
    static final Color FOIL_TEXT = new Color(0xDCC9FF);

    private static final Border PANEL_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x2B2112)),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
    );

    private AnthologyTheme() {
    }

    static void install() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", SURFACE_DEEP);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("TextArea.background", SURFACE_ALT);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("ComboBox.background", SURFACE_DEEP);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ScrollPane.background", SURFACE);
        UIManager.put("Viewport.background", SURFACE);
    }

    static Border panelBorder() {
        return PANEL_BORDER;
    }

    static void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_DARK),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        button.setBackground(SURFACE_HEADER);
        button.setForeground(GOLD);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    static void stylePrimaryButton(JButton button) {
        styleButton(button);
        button.setBackground(GOLD);
        button.setForeground(new Color(0x140D02));
    }

    static void styleNavButton(JButton button, boolean selected) {
        button.setFocusPainted(false);
        button.setHorizontalAlignment(JButton.CENTER);
        button.setBorder(new CenteredNavBorder(selected));
        button.setBackground(selected ? SELECTED : SIDEBAR);
        button.setForeground(selected ? GOLD : TEXT);
        button.setFont(button.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN, 13f));
    }

    private static final class CenteredNavBorder extends AbstractBorder {
        private final boolean selected;

        private CenteredNavBorder(boolean selected) {
            this.selected = selected;
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(10, 15, 10, 15);
        }

        @Override
        public Insets getBorderInsets(Component component, Insets insets) {
            insets.set(10, 15, 10, 15);
            return insets;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            if (!selected) {
                return;
            }
            graphics.setColor(GOLD);
            graphics.fillRect(x, y, 3, height);
        }
    }

    static void stylePanel(JComponent component) {
        component.setOpaque(true);
        component.setBackground(SURFACE);
        component.setBorder(panelBorder());
    }

    static void styleTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setGridColor(new Color(0x2A2114));
        table.setBackground(SURFACE_ALT);
        table.setForeground(TEXT);
        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(SURFACE_HEADER);
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, OUTLINE));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, false, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!selected) {
                    label.setBackground(row % 2 == 0 ? SURFACE_ALT : SURFACE_DEEP);
                    label.setForeground(column == 0 ? GOLD : TEXT);
                }
                return label;
            }
        });
    }

    static void styleBadgeColumn(JTable table, int column) {
        table.getColumnModel().getColumn(column).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, false, row, column);
                String text = value == null ? "" : value.toString();
                label.setText(text);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (selected) {
                    label.setBackground(SELECTED);
                    label.setForeground(TEXT);
                    return label;
                }
                BadgeColors colors = badgeColors(text);
                label.setBackground(colors.background());
                label.setForeground(colors.foreground());
                return label;
            }
        });
    }

    private static BadgeColors badgeColors(String text) {
        String normalized = text == null ? "" : text.trim().toLowerCase();
        if (normalized.equals("assigned") || normalized.equals("resolved") || normalized.equals("resolved printing") || normalized.equals("resolved intent")) {
            return new BadgeColors(GOOD_BG, GOOD_TEXT);
        }
        if (normalized.equals("partial") || normalized.equals("available") || normalized.equals("choose printing")) {
            return new BadgeColors(WARNING_BG, WARNING_TEXT);
        }
        if (normalized.equals("foil")) {
            return new BadgeColors(FOIL_BG, FOIL_TEXT);
        }
        if (normalized.equals("missing") || normalized.equals("manual") || normalized.equals("unresolved")) {
            return new BadgeColors(NEUTRAL_BG, NEUTRAL_TEXT);
        }
        return new BadgeColors(SURFACE_DEEP, MUTED);
    }

    private record BadgeColors(Color background, Color foreground) {
    }

    static JScrollPane scroll(Component component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x2B2112)));
        scroll.getViewport().setBackground(SURFACE_ALT);
        return scroll;
    }

    static ImageIcon logoIcon(int size) {
        try {
            BufferedImage image = ImageIO.read(AnthologyTheme.class.getResource("/cynful_logo.png"));
            Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException | IllegalArgumentException | NullPointerException error) {
            return null;
        }
    }
}
