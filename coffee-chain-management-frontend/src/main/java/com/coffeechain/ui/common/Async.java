package com.coffeechain.ui.common;

import javax.swing.SwingUtilities;

/**
 * Helper chạy tác vụ nền và quay lại Swing UI thread. Dùng khi màn hình cần gọi API hoặc xử lý lâu
 * mà không làm treo giao diện.
 */
public final class Async {
  private Async() {}

  public static void run(Runnable backgroundTask) {
    new Thread(backgroundTask).start();
  }

  public static void ui(Runnable uiTask) {
    SwingUtilities.invokeLater(uiTask);
  }
}
