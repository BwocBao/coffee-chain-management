package com.coffeechain.ui.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * JPanel bo góc với màu fill và tùy chọn border. Dùng làm card/header/container ở nhiều màn menu.
 */
public class RoundedPanel extends JPanel {
  private final int radius;
  private final Color fill;
  private final Color border;

  public RoundedPanel(int radius, Color fill) {
    this(radius, fill, null);
  }

  public RoundedPanel(int radius, Color fill, Color border) {
    this.radius = radius;
    this.fill = fill;
    this.border = border;
    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(fill);
    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
    if (border != null) {
      g2.setColor(border);
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
    }
    g2.dispose();
    super.paintComponent(g);
  }
}
