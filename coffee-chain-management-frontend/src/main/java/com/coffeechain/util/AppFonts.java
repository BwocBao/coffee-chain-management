package com.coffeechain.util;

import java.awt.*;
import java.io.InputStream;

public final class AppFonts {
  private static final Font REGULAR = loadFont("/fonts/Poppins-Regular.ttf", Font.PLAIN);
  private static final Font MEDIUM = loadFont("/fonts/Poppins-Medium.ttf", Font.PLAIN);
  private static final Font SEMIBOLD = loadFont("/fonts/Poppins-SemiBold.ttf", Font.BOLD);
  private static final Font BOLD = loadFont("/fonts/Poppins-Bold.ttf", Font.BOLD);

  private AppFonts() {}

  private static Font loadFont(String path, int fallbackStyle) {
    try (InputStream inputStream = AppFonts.class.getResourceAsStream(path)) {
      if (inputStream == null) {
        System.err.println("Không tìm thấy font: " + path);
        return new Font("Segoe UI", fallbackStyle, 14);
      }

      Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);

      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(font);

      return font;
    } catch (Exception e) {
      e.printStackTrace();
      return new Font("Segoe UI", fallbackStyle, 14);
    }
  }

  public static Font regular(float size) {
    return REGULAR.deriveFont(Font.PLAIN, size);
  }

  public static Font medium(float size) {
    return MEDIUM.deriveFont(Font.PLAIN, size);
  }

  public static Font semiBold(float size) {
    return SEMIBOLD.deriveFont(Font.BOLD, size);
  }

  public static Font bold(float size) {
    return BOLD.deriveFont(Font.BOLD, size);
  }
}
