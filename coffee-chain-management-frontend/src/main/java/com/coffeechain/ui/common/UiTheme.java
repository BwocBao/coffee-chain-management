package com.coffeechain.ui.common;

import java.awt.Color;
import java.awt.Font;

/**
 * Bộ màu và font dùng chung cho frontend Swing.
 *
 * <p>Các hằng Color dùng để thống nhất màu giữa các màn. regular(size) và bold(size) tạo Font theo
 * size truyền vào. Khi cần đổi style toàn app, ưu tiên chỉnh tại đây trước khi chỉnh từng màn.
 */
public final class UiTheme {
  private UiTheme() {}

  public static final String FONT = "Segoe UI";

  public static final Color PAGE_BG = Color.WHITE;
  public static final Color COFFEE_BG = Color.decode("#F7EBD5");
  public static final Color LOGIN_BG = Color.decode("#FDF0D5");

  public static final Color CARD_BG = new Color(255, 255, 255, 238);
  public static final Color INPUT_BG = Color.decode("#F6F6F6");

  public static final Color PRIMARY = Color.decode("#C67C4E");
  public static final Color PRIMARY_DARK = Color.decode("#B66F43");
  public static final Color ORANGE = Color.decode("#F47B20");

  public static final Color TEXT_DARK = Color.decode("#1F1F1F");
  public static final Color TEXT_MUTED = Color.decode("#8B8AA5");
  public static final Color TEXT_SOFT = Color.decode("#6F6F6F");
  public static final Color BORDER = Color.decode("#EFE7DE");
  public static final Color BORDER_LIGHT = Color.decode("#E7E7E7");
  public static final Color CARD_ICON_BG = Color.decode("#ECECEC");

  public static final Color ERROR = Color.decode("#BE3C2D");
  public static final Color SUCCESS = Color.decode("#3C8C5A");

  public static Font regular(int size) {
    return new Font(FONT, Font.PLAIN, size);
  }

  public static Font bold(int size) {
    return new Font(FONT, Font.BOLD, size);
  }
}
