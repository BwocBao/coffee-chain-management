package com.coffeechain.ui.common.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Icon người dùng dạng line.
 *
 * <p>Đang dùng ở LoginFrame, bên trái ô nhập username. Constructor nhận Color để đồng bộ màu nét
 * với thiết kế của field đăng nhập.
 */
public class UserLineIcon implements Icon {
  private final Color color;

  public UserLineIcon(Color color) {
    this.color = color;
  }

  @Override
  public int getIconWidth() {
    return 28;
  }

  @Override
  public int getIconHeight() {
    return 28;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setStroke(new BasicStroke(1.2f));
    g2.setColor(color);
    g2.drawOval(x + 10, y + 2, 8, 8);
    g2.drawArc(x + 3, y + 14, 22, 16, 0, 180);
    g2.dispose();
  }
}
