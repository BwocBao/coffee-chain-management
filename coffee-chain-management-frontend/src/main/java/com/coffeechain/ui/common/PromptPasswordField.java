package com.coffeechain.ui.common;

import javax.swing.JPasswordField;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Password field có placeholder tự vẽ khi chưa nhập.
 * Đang dùng ở LoginFrame để giữ giao diện field gọn mà không cần JLabel riêng.
 */
public class PromptPasswordField extends JPasswordField {
    private final String prompt;

    public PromptPasswordField(String prompt) {
        this.prompt = prompt;
        setOpaque(false);
        setBorder(null);
        setFont(UiTheme.regular(13));
        setForeground(UiTheme.TEXT_DARK);
        setCaretColor(UiTheme.TEXT_DARK);
        setEchoChar('•');
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length == 0 && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(UiTheme.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(prompt, 0, (getHeight() + fm.getAscent()) / 2 - 3);
            g2.dispose();
        }
    }
}
