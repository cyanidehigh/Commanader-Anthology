package com.commanderanthology.desktop;

import com.commanderanthology.core.CommanderAnthologyCore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;

final class DesktopShell {
    private static final int SIDEBAR_WIDTH = 260;
    private static final int SIDEBAR_INSET = 18;
    private static final int NAV_WIDTH = SIDEBAR_WIDTH - (SIDEBAR_INSET * 2);

    private final JFrame frame = new JFrame(CommanderAnthologyCore.PRODUCT_NAME);
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<DesktopWorkspace, JButton> buttons = new EnumMap<>(DesktopWorkspace.class);
    private final DesktopAppState state = new DesktopAppState();

    void show() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1120, 720));
        frame.setLocationByPlatform(true);
        frame.setContentPane(createRoot());
        select(DesktopWorkspace.DECKS);
        frame.setVisible(true);
    }

    private JPanel createRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AnthologyTheme.BACKGROUND);

        DesktopWorkspace[] workspaces = DesktopWorkspace.visible();
        JPanel sidebar = createSidebar(workspaces);
        for (DesktopWorkspace workspace : workspaces) {
            content.add(createWorkspace(workspace), workspace.name());
        }
        content.setBackground(AnthologyTheme.BACKGROUND);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, content);
        split.setDividerLocation(SIDEBAR_WIDTH);
        split.setDividerSize(1);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setEnabled(false);
        root.add(split, BorderLayout.CENTER);
        return root;
    }

    private JPanel createSidebar(DesktopWorkspace[] workspaces) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AnthologyTheme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(SIDEBAR_INSET, SIDEBAR_INSET, SIDEBAR_INSET, SIDEBAR_INSET));

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 236));
        brand.setPreferredSize(new Dimension(224, 236));

        JLabel title = new JLabel(CommanderAnthologyCore.PRODUCT_NAME);
        title.setForeground(AnthologyTheme.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Desktop full spectrum");
        subtitle.setForeground(AnthologyTheme.MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        brand.add(title);
        brand.add(Box.createVerticalStrut(3));
        brand.add(subtitle);
        brand.add(Box.createVerticalStrut(22));

        ImageIcon logo = AnthologyTheme.logoIcon(156);
        if (logo != null) {
            JLabel logoLabel = new JLabel(logo);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(logoLabel);
        }

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(10));

        for (DesktopWorkspace workspace : workspaces) {
            JButton button = new JButton(workspace.label());
            Dimension navSize = new Dimension(NAV_WIDTH, 42);
            button.setMinimumSize(navSize);
            button.setPreferredSize(navSize);
            button.setMaximumSize(navSize);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(event -> select(workspace));
            buttons.put(workspace, button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("V" + CommanderAnthologyCore.VERSION);
        version.setForeground(AnthologyTheme.MUTED);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(version);
        return sidebar;
    }

    private JPanel createWorkspace(DesktopWorkspace workspace) {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(AnthologyTheme.BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AnthologyTheme.BACKGROUND);

        JLabel title = new JLabel(workspace.label());
        title.setForeground(AnthologyTheme.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JLabel summary = new JLabel(workspace.summary());
        summary.setForeground(AnthologyTheme.MUTED);
        summary.setFont(summary.getFont().deriveFont(13f));

        header.add(title, BorderLayout.NORTH);
        header.add(summary, BorderLayout.SOUTH);
        page.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AnthologyTheme.SURFACE);
        body.setBorder(AnthologyTheme.panelBorder());

        body.add(createWorkspaceBody(workspace), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private Component createWorkspaceBody(DesktopWorkspace workspace) {
        return switch (workspace) {
            case DECKS -> new DeckBuilderPanel(state);
            case COLLECTION -> new CollectionPanel(state);
            case CARD_CODEX -> new CardCodexPanel();
            case SIM -> new CommanderSimPanel(state);
            case SYNC -> new SyncPanel(state);
            default -> createTextBody(workspace.body());
        };
    }

    private Component createTextBody(String textBody) {
        JTextArea text = new JTextArea(textBody);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBackground(AnthologyTheme.SURFACE);
        text.setForeground(AnthologyTheme.TEXT);
        text.setCaretColor(AnthologyTheme.TEXT);
        text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        text.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = AnthologyTheme.scroll(text);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AnthologyTheme.SURFACE);
        return scrollPane;
    }

    private void select(DesktopWorkspace workspace) {
        cards.show(content, workspace.name());
        buttons.forEach((key, button) -> {
            boolean selected = key == workspace;
            AnthologyTheme.styleNavButton(button, selected);
        });
    }
}
