package com.coffeechain.ui.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

/**
 * Panel nền có thể vẽ ảnh từ resources và phủ lớp màu overlay. Đang dùng ở các màn menu như
 * MenuTongFrame và QuanTriHeThongFrame để giữ background đồng nhất.
 */
public class BackgroundImagePanel extends JPanel {
  private final Image backgroundImage;
  private final Color fallbackColor;
  private final Color overlayColor;

  public BackgroundImagePanel(String imagePath) {
    this(imagePath, UiTheme.COFFEE_BG, new Color(255, 255, 255, 200));
  }

  public BackgroundImagePanel(String imagePath, Color fallbackColor, Color overlayColor) {
    this.backgroundImage = IconLoader.imageFromResource(imagePath);
    this.fallbackColor = fallbackColor == null ? UiTheme.COFFEE_BG : fallbackColor;
    this.overlayColor = overlayColor;
    setLayout(null);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (backgroundImage == null) {
      g.setColor(fallbackColor);
      g.fillRect(0, 0, getWidth(), getHeight());
      return;
    }

    int panelW = getWidth();
    int panelH = getHeight();
    int imgW = backgroundImage.getWidth(this);
    int imgH = backgroundImage.getHeight(this);
    if (imgW <= 0 || imgH <= 0) return;

    double scale = Math.max(panelW / (double) imgW, panelH / (double) imgH);
    int drawW = (int) Math.round(imgW * scale);
    int drawH = (int) Math.round(imgH * scale);
    int x = (panelW - drawW) / 2;
    int y = (panelH - drawH) / 2;

    g.drawImage(backgroundImage, x, y, drawW, drawH, this);

    if (overlayColor != null) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(overlayColor);
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.dispose();
    }
  }
}
