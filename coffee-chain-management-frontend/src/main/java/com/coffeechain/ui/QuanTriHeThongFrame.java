package com.coffeechain.ui;

import com.coffeechain.service.AuthApiClient;
import com.coffeechain.ui.common.AppHeaderPanel;
import com.coffeechain.ui.common.BackgroundImagePanel;
import com.coffeechain.ui.common.FrameNavigator;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.MenuModuleCard;
import com.coffeechain.ui.common.PermissionUtil;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Menu con của nhóm quản trị hệ thống. Từ đây user có thể mở màn phân quyền hoặc màn tạo tài khoản
 * nếu có quyền tương ứng.
 */
public class QuanTriHeThongFrame extends JFrame {
  private static final int ROOT_W = 734;
  private static final int ROOT_H = 533;

  private static final String ICON_CAFE = "icons/menu-system-admin/coffee.svg";
  private static final String ICON_AVATAR = "icons/menu-system-admin/avatar.svg";
  private static final String ICON_LOGOUT = "icons/menu-system-admin/logout.svg";
  private static final String BACKGROUND_IMAGE = "icons/menu-system-admin/background.jpg";
  private static final String ICON_PERMISSION = "icons/menu-system-admin/phan-quyen.svg";
  private static final String ICON_CREATE_USER = "icons/menu-system-admin/tao-tai-khoan.svg";

  private final JPanel root = new BackgroundImagePanel(BACKGROUND_IMAGE);
  private final AuthApiClient authApiClient = new AuthApiClient();

  public QuanTriHeThongFrame() {
    setTitle("Phùng Lộc - Quản trị hệ thống");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);

    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    setContentPane(root);

    buildHeader();
    buildMenuCards();
    buildBackButton();

    pack();
    setLocationRelativeTo(null);
  }

  private void buildHeader() {
    AppHeaderPanel header =
        new AppHeaderPanel(
            this,
            authApiClient,
            "QUẢN TRỊ HỆ THỐNG PHÙNG LỘC",
            "Phân quyền, bảo mật và quản lý tài khoản",
            430,
            ICON_CAFE,
            ICON_AVATAR,
            ICON_LOGOUT);
    header.setBounds(31, 45, 660, 78);
    root.add(header);
  }

  private void buildMenuCards() {
    List<SystemMenuItem> items = new ArrayList<>();

    if (PermissionUtil.hasAny("ROLE:VIEW", "ROLE:CREATE", "ROLE:UPDATE", "ROLE:DELETE")) {
      items.add(
          new SystemMenuItem(
              "Phân quyền & bảo mật",
              "Tạo role và\nquản lý quyền truy cập",
              ICON_PERMISSION,
              () -> FrameNavigator.open(this, new PhanQuyenBaoMatFrame())));
    }

    if (PermissionUtil.hasAny("USER:CREATE", "USER:VIEW")) {
      items.add(
          new SystemMenuItem(
              "Tạo tài khoản",
              "Tạo tài khoản\ncho nhân viên mới",
              ICON_CREATE_USER,
              () -> FrameNavigator.open(this, new TaoTaiKhoanFrame())));
    }

    if (items.isEmpty()) {
      JLabel noPermission =
          new JLabel(
              "<html><div style='text-align:center;'>Bạn chưa có quyền sử dụng chức năng quản trị hệ thống.<br/>Vui lòng liên hệ quản trị viên.</div></html>");
      noPermission.setHorizontalAlignment(SwingConstants.CENTER);
      noPermission.setFont(UiTheme.regular(16));
      noPermission.setForeground(UiTheme.TEXT_DARK);
      noPermission.setBounds(80, 230, 580, 60);
      root.add(noPermission);
      return;
    }

    int cardW = 230;
    int cardH = 240;
    int gap = 100;

    int totalW = items.size() * cardW + Math.max(0, items.size() - 1) * gap;
    int startX = Math.max(0, (ROOT_W - totalW) / 2);
    int y = 172;

    for (int i = 0; i < items.size(); i++) {
      SystemMenuItem item = items.get(i);

      MenuModuleCard card =
          new MenuModuleCard(
              item.title, item.description, IconLoader.svg(item.iconPath, 58, 58), item.action);

      int x = startX + i * (cardW + gap);
      card.setBounds(x, y, cardW, cardH);
      root.add(card);
    }
  }

  private void buildBackButton() {
    RoundedButton backButton = new RoundedButton("Quay lại");
    backButton.setBounds(556, 492, 100, 30);
    backButton.addActionListener(e -> FrameNavigator.open(this, new MenuTongFrame()));
    root.add(backButton);
  }

  private record SystemMenuItem(
      String title, String description, String iconPath, Runnable action) {}
}
