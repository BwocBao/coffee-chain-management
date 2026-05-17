package com.coffeechain.ui.common;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Wrapper bo góc cho field đăng nhập, gồm icon trái, field ở giữa và component phải tùy chọn.
 * Đang dùng ở LoginFrame cho username/password input.
 */
public class LoginFieldPanel extends JPanel {
    public LoginFieldPanel(Icon leftIcon, JComponent field, JComponent rightComponent) {
        setLayout(new BorderLayout(18, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 47, 0, 18));

        JLabel iconLabel = new JLabel(leftIcon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(38, 74));
        add(iconLabel, BorderLayout.WEST);

        add(field, BorderLayout.CENTER);

        if (rightComponent != null) {
            rightComponent.setPreferredSize(new Dimension(42, 74));
            add(rightComponent, BorderLayout.EAST);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiTheme.INPUT_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}
