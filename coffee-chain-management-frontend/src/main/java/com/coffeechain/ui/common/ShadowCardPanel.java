package com.coffeechain.ui.common;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Card nền trắng có shadow nhẹ.
 * Đang dùng ở LoginFrame để tạo khối đăng nhập nổi trên nền.
 */
public class ShadowCardPanel extends JPanel {
    private final int radius;
    private final Color fill;

    public ShadowCardPanel() {
        this(20, Color.WHITE);
    }

    public ShadowCardPanel(int radius, Color fill) {
        this.radius = radius;
        this.fill = fill;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 18; i > 0; i--) {
            int alpha = Math.max(2, 25 - i);
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRoundRect(i / 2, i / 2 + 4, getWidth() - i, getHeight() - i, radius, radius);
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
