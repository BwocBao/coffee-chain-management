package com.coffeechain.ui;

import com.coffeechain.dto.PermissionCheckResponse;
import com.coffeechain.dto.UserInfoResponse;
import com.coffeechain.service.AuthApiClient;
import com.coffeechain.service.SessionManager;
import java.awt.*;
import java.util.Set;
import javax.swing.*;

/**
 * Màn home cũ/đơn giản của app. Hiện luồng chính sau login đang đi qua MenuTongFrame, nên class này
 * giữ lại để tham khảo hoặc tái dùng sau.
 */
public class HomeFrame extends JFrame {
  private final AuthApiClient authApiClient = new AuthApiClient();
  private final JTextArea infoArea = new JTextArea();

  public HomeFrame() {
    setTitle("Coffee Chain - Home");
    setSize(720, 520);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton meButton = new JButton("Gọi /me");
    JButton checkButton = new JButton("Check INVENTORY:IMPORT");
    JButton logoutButton = new JButton("Đăng xuất");
    JButton khoMenuButton = new JButton("Menu kho");

    buttonPanel.add(meButton);
    buttonPanel.add(checkButton);
    buttonPanel.add(logoutButton);
    // Chỉ hiện nút Menu kho nếu user có ít nhất 1 quyền liên quan tới kho
    if (SessionManager.hasAnyPermission(
        "INVENTORY:VIEW",
        "INVENTORY:IMPORT",
        "INVENTORY:EXPORT",
        "INVENTORY:TRANSFER",
        "STOCKTAKE:MANAGE",
        "WASTAGE:CREATE",
        "WAREHOUSE:VIEW",
        "INGREDIENT:VIEW",
        "SUPPLIER:VIEW",
        "BRANCH:VIEW",
        "UNIT:VIEW")) {
      buttonPanel.add(khoMenuButton);
    }

    infoArea.setEditable(false);
    add(buttonPanel, BorderLayout.NORTH);
    add(new JScrollPane(infoArea), BorderLayout.CENTER);

    meButton.addActionListener(e -> loadMe());
    checkButton.addActionListener(e -> checkPermission("INVENTORY:IMPORT"));
    logoutButton.addActionListener(e -> logout());
    khoMenuButton.addActionListener(
        e -> {
          new KhoMenuFrame().setVisible(true);
        });

    renderSessionInfo();
  }

  private void renderSessionInfo() {
    UserInfoResponse user = SessionManager.getCurrentUser();

    if (user == null) {
      infoArea.setText("Chưa có session.");
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Đăng nhập thành công\n\n");
    sb.append("Mã người dùng: ").append(user.getMaNguoiDung()).append("\n");
    sb.append("Tên đăng nhập: ").append(user.getTenDangNhap()).append("\n");
    sb.append("Vai trò: ").append(user.getTenVaiTro()).append("\n");
    sb.append("Mã chi nhánh: ").append(user.getMaChiNhanh()).append("\n");
    sb.append("Tên chi nhánh: ").append(user.getTenChiNhanh()).append("\n");
    sb.append("Hết hạn lúc: ").append(user.getExpiredAt()).append("\n\n");

    sb.append("Token:\n");
    sb.append(SessionManager.getToken()).append("\n\n");

    sb.append("Permissions:\n");

    Set<String> permissions = user.getPermissions();

    if (permissions == null || permissions.isEmpty()) {
      sb.append("- Không có quyền nào\n");
    } else {
      for (String permission : permissions) {
        sb.append("- ").append(permission).append("\n");
      }
    }

    infoArea.setText(sb.toString());
  }

  private void loadMe() {
    new Thread(
            () -> {
              try {
                UserInfoResponse user = authApiClient.getMe();

                SwingUtilities.invokeLater(
                    () -> infoArea.append("\n/me OK: " + user.getTenDangNhap()));
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> infoArea.append("\n/me lỗi: " + ex.getMessage()));
              }
            })
        .start();
  }

  private void checkPermission(String permission) {
    new Thread(
            () -> {
              try {
                PermissionCheckResponse response = authApiClient.checkPermission(permission);

                SwingUtilities.invokeLater(
                    () -> {
                      String result = response.isAllowed() ? "Có quyền" : "Không có quyền";
                      infoArea.append("\nCheck " + permission + ": " + result);
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(
                    () -> infoArea.append("\nCheck permission lỗi: " + ex.getMessage()));
              }
            })
        .start();
  }

  private void logout() {
    new Thread(
            () -> {
              try {
                authApiClient.logout();
                SessionManager.clear();

                SwingUtilities.invokeLater(
                    () -> {
                      JOptionPane.showMessageDialog(this, "Đăng xuất thành công");
                      new LoginFrame().setVisible(true);
                      dispose();
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(
                    () -> JOptionPane.showMessageDialog(this, "Logout lỗi: " + ex.getMessage()));
              }
            })
        .start();
  }
}
