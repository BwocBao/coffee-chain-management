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
 * Wrapper bo góc cho text field/password field, có thể gắn icon trái và component phải.
 * Dùng cho các form nhập liệu để tránh lặp code border/background.
 */
public class RoundedFieldPanel extends JPanel {
    private final int radius;

    public RoundedFieldPanel(JComponent field) {
        this(null, field, null, 10, 16, 12);
    }

    public RoundedFieldPanel(JComponent field, JComponent rightComponent) {
        this(null, field, rightComponent, 10, 16, 12);
    }

    public RoundedFieldPanel(Icon leftIcon, JComponent field, JComponent rightComponent) {
        this(leftIcon, field, rightComponent, 10, 16, 12);
    }

    public RoundedFieldPanel(Icon leftIcon, JComponent field, JComponent rightComponent, int radius, int leftPadding, int rightPadding) {
        this.radius = radius;
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(0, leftPadding, 0, rightPadding));

        if (leftIcon != null) {
            JLabel iconLabel = new JLabel(leftIcon);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setPreferredSize(new Dimension(32, 1));
            add(iconLabel, BorderLayout.WEST);
        }

        add(field, BorderLayout.CENTER);

        if (rightComponent != null) {
            rightComponent.setPreferredSize(new Dimension(34, 1));
            add(rightComponent, BorderLayout.EAST);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiTheme.INPUT_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
