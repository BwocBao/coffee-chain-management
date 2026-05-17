package com.coffeechain.ui.common;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Card menu chức năng dạng lớn, có title, mô tả, icon thường và icon hover.
 * Đang dùng ở KhoMenuFrame cho các nghiệp vụ kho.
 */
public class FeatureMenuCard extends JPanel {
    private static final int ICON_BOX_SIZE = 40;
    private static final int ICON_SIZE = 30;

    private final Runnable action;
    private final Icon normalIcon;
    private final Icon hoverIcon;

    private boolean hover;

    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JTextArea descriptionArea;

    public FeatureMenuCard(String title, String description, Icon normalIcon, Icon hoverIcon, Runnable action) {
        this.action = action;
        this.normalIcon = normalIcon;
        this.hoverIcon = hoverIcon == null ? normalIcon : hoverIcon;

        setLayout(null);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        iconLabel = new JLabel(normalIcon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(iconLabel);

        titleLabel = new JLabel("<html><div style='text-align:center;'>" + title + "</div></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(UiTheme.TEXT_DARK);
        titleLabel.setFont(UiTheme.bold(18));
        add(titleLabel);

        descriptionArea = new JTextArea(description);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setForeground(UiTheme.TEXT_SOFT);
        descriptionArea.setFont(UiTheme.regular(12));
        add(descriptionArea);

        MouseAdapter listener = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; updateHover(); repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; updateHover(); repaint(); }
            @Override public void mouseClicked(MouseEvent e) { runAction(); }
        };

        addMouseListener(listener);
        iconLabel.addMouseListener(listener);
        titleLabel.addMouseListener(listener);
        descriptionArea.addMouseListener(listener);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        iconLabel.setBounds((w - ICON_BOX_SIZE) / 2, 35, ICON_BOX_SIZE, ICON_BOX_SIZE);
        titleLabel.setBounds(22, 88, w - 44, 52);
        descriptionArea.setBounds(30, 138, w - 60, 38);
    }

    private void runAction() {
        if (action != null) {
            action.run();
        }
    }

    private void updateHover() {
        titleLabel.setForeground(hover ? Color.WHITE : UiTheme.TEXT_DARK);
        descriptionArea.setForeground(hover ? Color.WHITE : UiTheme.TEXT_SOFT);
        iconLabel.setIcon(hover ? hoverIcon : normalIcon);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(hover ? UiTheme.PRIMARY : Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

        g2.setColor(hover ? UiTheme.PRIMARY_DARK : UiTheme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

        g2.setColor(hover ? new Color(255, 255, 255, 235) : Color.decode("#ECECEC"));
        g2.fillRoundRect((getWidth() - ICON_BOX_SIZE) / 2, 35, ICON_BOX_SIZE, ICON_BOX_SIZE, 8, 8);

        g2.dispose();
        super.paintComponent(g);
    }
}
