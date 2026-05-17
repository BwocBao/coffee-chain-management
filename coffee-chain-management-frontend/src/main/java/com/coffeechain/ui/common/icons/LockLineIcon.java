package com.coffeechain.ui.common.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Icon ổ khóa dạng line.
 *
 * Đang dùng ở LoginFrame, bên trái ô nhập mật khẩu.
 * Constructor nhận Color để màn đăng nhập có thể dùng đúng màu icon theo thiết kế.
 */
public class LockLineIcon implements Icon {
    private final Color color;
    public LockLineIcon(Color color) { this.color = color; }
    @Override public int getIconWidth() { return 28; }
    @Override public int getIconHeight() { return 28; }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(color);
        g2.drawRoundRect(x + 3, y + 12, 22, 12, 3, 3);
        g2.drawArc(x + 8, y + 2, 12, 18, 0, 180);
        g2.dispose();
    }
}
