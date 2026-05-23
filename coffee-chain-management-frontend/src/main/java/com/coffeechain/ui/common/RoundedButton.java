package com.coffeechain.ui.common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 * JButton bo góc theo style chung của app. Dùng ở LoginFrame, MenuTongFrame, QuanTriHeThongFrame và
 * các form có nút hành động chính.
 */
public class RoundedButton extends JButton {
  private Color backgroundColor = UiTheme.PRIMARY;
  private Color hoverColor = UiTheme.PRIMARY_DARK;
  private Color disabledColor = new Color(190, 170, 155);
  private int radius = 8;

  public RoundedButton(String text) {
    super(text);
    setForeground(Color.WHITE);
    setFont(UiTheme.bold(13));
    setBorder(new EmptyBorder(0, 12, 0, 12));
    setBorderPainted(false);
    setContentAreaFilled(false);
    setFocusPainted(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public RoundedButton background(Color color) {
    this.backgroundColor = color;
    repaint();
    return this;
  }

  public RoundedButton hover(Color color) {
    this.hoverColor = color;
    repaint();
    return this;
  }

  public RoundedButton radius(int radius) {
    this.radius = radius;
    repaint();
    return this;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Color fill =
        !isEnabled() ? disabledColor : (getModel().isRollover() ? hoverColor : backgroundColor);
    g2.setColor(fill);
    g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
    g2.dispose();
    super.paintComponent(g);
  }
}
