package com.coffeechain.ui.createuser;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Bộ component custom dùng riêng cho màn {@code TaoTaiKhoanFrame}. Các component này được tách ra
 * để giảm độ dài frame nhưng giữ nguyên style và kích thước cũ.
 */
public final class CreateUserUiComponents {
  private static final Color PAGE_BG = Color.decode("#F7EBD5");
  private static final Color PRIMARY = Color.decode("#C67C4E");
  private static final Color PRIMARY_DARK = Color.decode("#B66F43");
  private static final Color TEXT_DARK = Color.decode("#1F1F1F");
  private static final Color TEXT_MUTED = Color.decode("#8B8AA5");
  private static final Color INPUT_BG = Color.decode("#F6F6F6");
  private static final Color BORDER = Color.decode("#EFE7DE");

  private CreateUserUiComponents() {}

  /** Panel nền của màn tạo tài khoản, có thể vẽ ảnh background hoặc màu nền mặc định. */
  public static class BackgroundImagePanel extends JPanel {
    private final Image backgroundImage;

    public BackgroundImagePanel(String imagePath) {
      Image loadedImage = null;
      try {
        if (imagePath != null && !imagePath.isBlank()) {
          java.net.URL url = getClass().getClassLoader().getResource(imagePath);
          if (url != null) {
            loadedImage = new ImageIcon(url).getImage();
          }
        }
      } catch (Exception ignored) {
      }
      this.backgroundImage = loadedImage;
      setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      if (backgroundImage == null) {
        g.setColor(PAGE_BG);
        g.fillRect(0, 0, getWidth(), getHeight());
        return;
      }

      int panelW = getWidth();
      int panelH = getHeight();
      int imgW = backgroundImage.getWidth(this);
      int imgH = backgroundImage.getHeight(this);

      if (imgW <= 0 || imgH <= 0) {
        return;
      }

      double scale = Math.max(panelW / (double) imgW, panelH / (double) imgH);
      int drawW = (int) Math.round(imgW * scale);
      int drawH = (int) Math.round(imgH * scale);
      int x = (panelW - drawW) / 2;
      int y = (panelH - drawH) / 2;

      g.drawImage(backgroundImage, x, y, drawW, drawH, this);

      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(new Color(255, 255, 255, 110));
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.dispose();
    }
  }

  /** Khung ảnh bên phải form tạo tài khoản. */
  public static class IllustrationPanel extends JPanel {
    private final String imagePath;

    public IllustrationPanel(String imagePath) {
      this.imagePath = imagePath;
      setOpaque(false);
      setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setColor(Color.WHITE);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

      g2.setColor(BORDER);
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

      g2.dispose();
      super.paintComponent(g);

      if (imagePath != null && !imagePath.isBlank()) {
        try {
          Icon icon;
          if (imagePath.toLowerCase().endsWith(".svg")) {
            icon = new FlatSVGIcon(imagePath, getWidth() - 28, getHeight() - 28);
          } else {
            java.net.URL url = getClass().getClassLoader().getResource(imagePath);
            if (url == null) {
              return;
            }
            Image image = new ImageIcon(url).getImage();
            icon =
                new ImageIcon(
                    image.getScaledInstance(getWidth() - 28, getHeight() - 28, Image.SCALE_SMOOTH));
          }
          icon.paintIcon(this, g, 14, 14);
        } catch (Exception ignored) {
        }
      }
    }
  }

  /** Panel bo góc dùng làm card chính trong màn tạo tài khoản. */
  public static class RoundedPanel extends JPanel {
    private final int radius;
    private final Color fill;

    public RoundedPanel(int radius, Color fill) {
      this.radius = radius;
      this.fill = fill;
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(fill);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
      g2.dispose();
      super.paintComponent(g);
    }
  }

  /**
   * Wrapper bo góc cho text field/password field, có thể gắn thêm component bên phải như nút mắt.
   */
  public static class RoundedFieldPanel extends JPanel {
    public RoundedFieldPanel(JComponent field) {
      this(field, null);
    }

    public RoundedFieldPanel(JComponent field, JComponent rightComponent) {
      setLayout(new BorderLayout(8, 0));
      setOpaque(false);
      setBorder(new EmptyBorder(0, 16, 0, 12));
      add(field, BorderLayout.CENTER);
      if (rightComponent != null) {
        rightComponent.setPreferredSize(new Dimension(34, 46));
        add(rightComponent, BorderLayout.EAST);
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(INPUT_BG);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
      g2.dispose();
      super.paintComponent(g);
    }
  }

  /** Wrapper bo góc cho combo box chọn vai trò. */
  public static class RoundedComboPanel extends JPanel {
    public RoundedComboPanel(JComboBox<?> comboBox) {
      setLayout(new BorderLayout());
      setOpaque(false);
      setBorder(new EmptyBorder(0, 12, 0, 12));

      comboBox.setBorder(null);
      comboBox.setOpaque(false);
      comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      comboBox.setForeground(TEXT_DARK);

      add(comboBox, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(INPUT_BG);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
      g2.dispose();
      super.paintComponent(g);
    }
  }

  /** Text field có placeholder tự vẽ khi chưa nhập dữ liệu. */
  public static class PromptTextField extends JTextField {
    private final String prompt;

    public PromptTextField(String prompt) {
      this.prompt = prompt;
      setOpaque(false);
      setBorder(null);
      setFont(new Font("Segoe UI", Font.PLAIN, 13));
      setForeground(TEXT_DARK);
      setCaretColor(TEXT_DARK);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (getText().isEmpty() && !isFocusOwner()) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TEXT_MUTED);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(prompt, 0, (getHeight() + fm.getAscent()) / 2 - 3);
        g2.dispose();
      }
    }
  }

  /** Password field có placeholder và ký tự che mật khẩu mặc định. */
  public static class PromptPasswordField extends JPasswordField {
    private final String prompt;

    public PromptPasswordField(String prompt) {
      this.prompt = prompt;
      setOpaque(false);
      setBorder(null);
      setFont(new Font("Segoe UI", Font.PLAIN, 13));
      setForeground(TEXT_DARK);
      setCaretColor(TEXT_DARK);
      setEchoChar('\u2022');
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (getPassword().length == 0 && !isFocusOwner()) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TEXT_MUTED);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(prompt, 0, (getHeight() + fm.getAscent()) / 2 - 3);
        g2.dispose();
      }
    }
  }

  /** Button bo góc dùng cho nút tạo tài khoản, có màu hover riêng. */
  public static class RoundedButton extends JButton {
    private boolean hover;

    public RoundedButton(String text) {
      super(text);
      setForeground(Color.WHITE);
      setFont(new Font("Segoe UI", Font.BOLD, 14));
      setBorderPainted(false);
      setContentAreaFilled(false);
      setFocusPainted(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
              hover = true;
              repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
              hover = false;
              repaint();
            }
          });
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(hover ? PRIMARY_DARK : PRIMARY);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
      g2.dispose();
      super.paintComponent(g);
    }
  }

  /** Icon con mắt dùng cho nút ẩn/hiện mật khẩu. */
  public static class EyeLineIcon implements Icon {
    private final boolean open;

    public EyeLineIcon(boolean open) {
      this.open = open;
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
      g2.setColor(TEXT_MUTED);
      g2.setStroke(new BasicStroke(1.8f));
      g2.drawArc(x + 3, y + 8, 20, 10, 0, 180);
      g2.drawArc(x + 3, y + 8, 20, 10, 180, 180);
      g2.drawOval(x + 10, y + 11, 6, 6);
      if (!open) {
        g2.drawLine(x + 3, y + 3, x + 23, y + 23);
      }
      g2.dispose();
    }
  }
}
