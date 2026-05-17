package com.coffeechain.ui.common;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Avatar tròn đơn giản vẽ bằng Java2D.
 * Đang dùng ở KhoMenuFrame, góc phải header kho.
 */
public class UserCircle extends JPanel {
    public UserCircle() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.decode("#D9D9D9"));
        g2.fillOval(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
