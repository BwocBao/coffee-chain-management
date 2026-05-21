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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu tổng sau đăng nhập.
 * Màn này lọc module lớn theo quyền rồi điều hướng sang quản lý kho, POS hoặc quản trị hệ thống.
 */
public class MenuTongFrame extends JFrame {
    private static final int ROOT_W = 734;
    private static final int ROOT_H = 660;

    private static final String ICON_CAFE = "icons/menu-home/coffee.svg";
    private static final String ICON_AVATAR = "icons/menu-home/avatar.svg";
    private static final String ICON_KHO = "icons/menu-home/quan-ly-kho.svg";
    private static final String ICON_POS = "icons/menu-home/quan-ly-pos.svg";
    private static final String ICON_SYSTEM = "icons/menu-home/quan-tri-he-thong.svg";
    private static final String ICON_BRANCH = "icons/menu-home/quan-ly-chi-nhanh.svg";
    private static final String ICON_LOGOUT = "icons/menu-home/logout.svg";
    private static final String BACKGROUND_IMAGE = "icons/menu-home/background.jpg";

    private final JPanel root = new BackgroundImagePanel(BACKGROUND_IMAGE);
    private final AuthApiClient authApiClient = new AuthApiClient();

    public MenuTongFrame() {
        setTitle("Phùng Lộc - Menu tổng");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
        setContentPane(root);

        buildHeader();
        buildCards();
        buildExitButton();

        pack();
        setLocationRelativeTo(null);
    }

    private void buildHeader() {
        AppHeaderPanel header = new AppHeaderPanel(
                this,
                authApiClient,
                "QUẢN TRỊ HỆ THỐNG PHÙNG LỘC",
                "Hệ thống quản lý chuỗi cà phê Phùng Lộc",
                420,
                ICON_CAFE,
                ICON_AVATAR,
                ICON_LOGOUT
        );
        header.setBounds(31, 45, 660, 78);
        root.add(header);
    }

    private void buildCards() {
        List<MenuItem> items = new ArrayList<>();

        if (PermissionUtil.hasAny(
                "WAREHOUSE:VIEW", "INGREDIENT:VIEW", "SUPPLIER:VIEW", "UNIT:VIEW",
                "INVENTORY:VIEW", "INVENTORY:IMPORT", "INVENTORY:EXPORT", "INVENTORY:TRANSFER",
                "STOCKTAKE:CREATE", "WASTAGE:CREATE")) {
            items.add(new MenuItem(
                    "Quản lý kho",
                    "Quản lý danh mục và\nnghiệp vụ kho",
                    ICON_KHO,
                    () -> FrameNavigator.open(this, new KhoMenuFrame())
            ));
        }

        if (PermissionUtil.hasAny("ORDER:VIEW", "ORDER:CREATE", "ORDER:PAY", "PRODUCT:VIEW", "PRODUCT:CREATE")) {
            items.add(new MenuItem(
                    "Quản lý POS",
                    "Quản lý các đơn hàng và\ncông thức món",
                    ICON_POS,
                    () -> FrameNavigator.open(this, new QuanLyPOSFrame())
            ));
        }

        if (PermissionUtil.hasAny("USER:VIEW", "USER:CREATE", "ROLE:VIEW", "ROLE:CREATE")) {
            items.add(new MenuItem(
                    "Quản trị hệ thống",
                    "Tạo tài khoản, phân quyền\nvà bảo mật",
                    ICON_SYSTEM,
                    () -> FrameNavigator.open(this, new QuanTriHeThongFrame())
            ));
        }

        if (PermissionUtil.hasAny("BRANCH:VIEW", "BRANCH:CREATE", "BRANCH:UPDATE", "BRANCH:DELETE")) {
            items.add(new MenuItem(
                    "Quản lý chi nhánh",
                    "Theo dõi hoạt động,\nnhân sự, doanh thu\ntừng chi nhánh",
                    ICON_BRANCH,
                    () -> FrameNavigator.open(this, new QuanLyChiNhanhFrame())
            ));
        }

        if (items.isEmpty()) {
            JLabel noPermission = new JLabel("Bạn chưa có quyền truy cập chức năng nào.");
            noPermission.setHorizontalAlignment(SwingConstants.CENTER);
            noPermission.setFont(UiTheme.regular(16));
            noPermission.setForeground(UiTheme.TEXT_DARK);
            noPermission.setBounds(100, 230, 530, 40);
            root.add(noPermission);
            return;
        }

        int cardW = 210;
        int cardH = 210;
        int gapX = 80;
        int gapY = 30;

        int startX = (ROOT_W - (cardW * 2 + gapX)) / 2;
        int startY = 145;

        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);

            MenuModuleCard card = new MenuModuleCard(
                    item.title,
                    item.description,
                    IconLoader.svg(item.iconPath, 54, 54),
                    item.action,
                    true
            );

            int row = i / 2;
            int col = i % 2;

            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY);

            card.setBounds(x, y, cardW, cardH);
            root.add(card);
        }
    }

    private void buildExitButton() {
        RoundedButton exitButton = new RoundedButton("Thoát");
        exitButton.setBounds(556, 615, 95, 30);
        exitButton.addActionListener(e -> FrameNavigator.logout(this, authApiClient));
        root.add(exitButton);
    }

    private record MenuItem(String title, String description, String iconPath, Runnable action) {}
}
