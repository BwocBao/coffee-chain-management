package com.coffeechain.ui.common;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JTextField;

/**
 * Text field có placeholder tự vẽ khi chưa nhập. Đang dùng ở LoginFrame và các form cần input theo
 * style chung.
 */
public class PromptTextField extends JTextField {
  private final String prompt;

  public PromptTextField(String prompt) {
    this.prompt = prompt;
    setOpaque(false);
    setBorder(null);
    setFont(UiTheme.regular(13));
    setForeground(UiTheme.TEXT_DARK);
    setCaretColor(UiTheme.TEXT_DARK);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (getText().isEmpty() && !isFocusOwner()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(UiTheme.TEXT_MUTED);
      g2.setFont(getFont());
      FontMetrics fm = g2.getFontMetrics();
      g2.drawString(prompt, 0, (getHeight() + fm.getAscent()) / 2 - 3);
      g2.dispose();
    }
  }
}
