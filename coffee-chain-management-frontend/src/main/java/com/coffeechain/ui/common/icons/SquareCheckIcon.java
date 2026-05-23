package com.coffeechain.ui.common.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Icon checkbox vuông tự vẽ.
 *
 * <p>Đang dùng ở LoginFrame cho checkbox Remember Me. Tham số selected quyết định trạng thái tick,
 * selectedColor là màu khi tick, borderColor là màu viền khi chưa tick.
 */
public class SquareCheckIcon implements Icon {
  private final boolean selected;
  private final Color primary;
  private final Color border;

  public SquareCheckIcon(boolean selected, Color primary, Color border) {
    this.selected = selected;
    this.primary = primary;
    this.border = border;
  }

  @Override
  public int getIconWidth() {
    return 30;
  }

  @Override
  public int getIconHeight() {
    return 30;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setStroke(new BasicStroke(1.2f));
    g2.setColor(border);
    g2.drawRoundRect(x + 1, y + 1, 28, 28, 4, 4);
    if (selected) {
      g2.setColor(primary);
      g2.fillRoundRect(x + 4, y + 4, 22, 22, 4, 4);
      g2.setColor(Color.WHITE);
      g2.setStroke(new BasicStroke(2f));
      g2.drawLine(x + 9, y + 16, x + 14, y + 21);
      g2.drawLine(x + 14, y + 21, x + 23, y + 10);
    }
    g2.dispose();
  }
}
