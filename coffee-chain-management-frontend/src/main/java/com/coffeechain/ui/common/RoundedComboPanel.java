package com.coffeechain.ui.common;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Wrapper bo góc cho JComboBox.
 * Dùng cho các form cần combo box nhưng vẫn giữ style input chung.
 */
public class RoundedComboPanel extends JPanel {
    public RoundedComboPanel(JComboBox<?> comboBox) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 12, 0, 12));
        comboBox.setBorder(null);
        comboBox.setOpaque(false);
        comboBox.setFont(UiTheme.regular(13));
        comboBox.setForeground(UiTheme.TEXT_DARK);
        add(comboBox, BorderLayout.CENTER);
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
