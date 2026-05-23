package com.coffeechain.ui.common.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Icon con mắt dùng cho nút ẩn/hiện mật khẩu.
 *
 * <p>Đang dùng ở LoginFrame trong ô password; phiên bản riêng của TaoTaiKhoanFrame nằm trong
 * CreateUserUiComponents để giữ pixel/style cũ của màn đó. Tham số open quyết định có gạch chéo hay
 * không, Color quyết định màu nét vẽ.
 */
public class EyeLineIcon implements Icon {
  private final boolean open;
  private final Color color;

  public EyeLineIcon(boolean open, Color color) {
    this.open = open;
    this.color = color;
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
    g2.setColor(color);
    g2.setStroke(new BasicStroke(2f));
    g2.drawArc(x + 3, y + 8, 24, 14, 0, 180);
    g2.drawArc(x + 3, y + 8, 24, 14, 180, 180);
    g2.drawOval(x + 12, y + 12, 6, 6);
    if (!open) g2.drawLine(x + 3, y + 3, x + 27, y + 27);
    g2.dispose();
  }
}
