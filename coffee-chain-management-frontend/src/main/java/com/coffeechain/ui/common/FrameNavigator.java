package com.coffeechain.ui.common;

import com.coffeechain.service.AuthApiClient;
import com.coffeechain.service.SessionManager;
import com.coffeechain.ui.LoginFrame;
import javax.swing.JFrame;

/**
 * Helper điều hướng giữa các JFrame. Dùng để mở màn mới, đóng màn hiện tại và xử lý logout thống
 * nhất.
 */
public final class FrameNavigator {
  private FrameNavigator() {}

  /** Mở frame tiếp theo và đóng frame hiện tại nếu current khác null. */
  public static void open(JFrame current, JFrame next) {
    next.setVisible(true);
    if (current != null) current.dispose();
  }

  /** Gọi API logout, xóa session local và đưa user về LoginFrame. */
  public static void logout(JFrame current, AuthApiClient authApiClient) {
    Async.run(
        () -> {
          try {
            authApiClient.logout();
          } catch (Exception ignored) {
            // Logout backend lỗi vẫn xóa session local.
          }
          SessionManager.clear();
          Async.ui(() -> open(current, new LoginFrame()));
        });
  }
}
