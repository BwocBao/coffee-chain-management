package com.coffeechain.ui.common.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Icon chuông thông báo vẽ bằng Java2D.
 *
 * <p>Đang dùng ở KhoMenuFrame, góc phải phần header của màn quản lý kho. Constructor không tham số
 * dùng màu mặc định; constructor có Color cho phép đổi màu nét vẽ. paintIcon(...) là nơi vẽ hình
 * chuông lên component Swing.
 */
public class BellIcon implements Icon {
  private final Color color;

  public BellIcon() {
    this(Color.decode("#F47B20"));
  }

  public BellIcon(Color color) {
    this.color = color;
  }

  @Override
  public int getIconWidth() {
    return 26;
  }

  @Override
  public int getIconHeight() {
    return 26;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(color);
    g2.setStroke(new BasicStroke(2f));
    g2.drawArc(x + 6, y + 6, 14, 16, 0, 180);
    g2.drawLine(x + 6, y + 14, x + 6, y + 20);
    g2.drawLine(x + 20, y + 14, x + 20, y + 20);
    g2.drawLine(x + 4, y + 20, x + 22, y + 20);
    g2.fillOval(x + 11, y + 22, 4, 4);
    g2.dispose();
  }
}
