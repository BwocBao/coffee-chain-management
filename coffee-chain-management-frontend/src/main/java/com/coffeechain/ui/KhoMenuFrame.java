package com.coffeechain.ui;

import com.coffeechain.ui.common.FeatureMenuCard;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.PermissionUtil;
import com.coffeechain.ui.common.UiTheme;
import com.coffeechain.ui.common.UserCircle;
import com.coffeechain.ui.common.icons.BellIcon;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu chức năng kho.
 * Hiển thị các nghiệp vụ kho theo quyền như nhập, xuất, điều chuyển, kiểm kho, hao hụt và tra cứu tồn.
 */
public class KhoMenuFrame extends JFrame {
    private static final int CANVAS_W = 1120;
    private static final int HEADER_X = 40;

    private static final int CARD_W = 250;
    private static final int CARD_H = 180;
    private static final int GAP_X = 24;
    private static final int GAP_Y = 24;
    private static final int GRID_X = 67;
    private static final int GRID_Y = 185;
    private static final int COLS = 4;

    private static final String ICON_BACK = "icons/menu-kho/left.svg";

    private final JPanel contentPanel = new JPanel(null);
    private final List<FeatureMenuCard> menuCards = new ArrayList<>();
    private final JScrollPane scrollPane;

    public KhoMenuFrame() {
        setTitle("Phụng Lộc - Quản lý kho");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1220, 820);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);

        contentPanel.setBackground(Color.WHITE);
        contentPanel.setPreferredSize(new Dimension(CANVAS_W, 900));

        scrollPane = createHiddenScrollbarScrollPane(contentPanel);
        setContentPane(scrollPane);

        buildHeader();
        buildMenuGrid();

        SwingUtilities.invokeLater(() -> {
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
            scrollPane.getVerticalScrollBar().setValue(0);
        });
    }

    private JScrollPane createHiddenScrollbarScrollPane(JPanel panel) {
        JScrollPane pane = new JScrollPane(panel);
        pane.setBorder(null);
        pane.setWheelScrollingEnabled(true);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        pane.getVerticalScrollBar().setMinimumSize(new Dimension(0, 0));
        pane.getVerticalScrollBar().setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
        return pane;
    }

    private void buildHeader() {
        JLabel backLabel = new JLabel("Quay lại");
        backLabel.setIcon(IconLoader.svg(ICON_BACK, 22, 22));
        backLabel.setIconTextGap(6);
        backLabel.setBounds(HEADER_X, 28, 150, 34);
        backLabel.setForeground(Color.decode("#F47B20"));
        backLabel.setFont(UiTheme.regular(18));
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { goBack(); }
        });
        contentPanel.add(backLabel);

        JLabel titleLabel = new JLabel("QUẢN LÝ KHO");
        titleLabel.setBounds(HEADER_X, 92, 500, 45);
        titleLabel.setForeground(UiTheme.PRIMARY_DARK);
        titleLabel.setFont(UiTheme.bold(31));
        contentPanel.add(titleLabel);

        JLabel bell = new JLabel(new BellIcon());
        bell.setBounds(CANVAS_W - 65, 42, 28, 28);
        contentPanel.add(bell);

        UserCircle userCircle = new UserCircle();
        userCircle.setBounds(CANVAS_W - 28, 40, 36, 36);
        contentPanel.add(userCircle);
    }

    private void buildMenuGrid() {
        List<KhoMenuItem> visibleItems = getMenuItems()
                .stream()
                .filter(this::canShow)
                .toList();

        if (visibleItems.isEmpty()) {
            JPanel emptyPanel = createEmptyPermissionPanel();
            emptyPanel.setBounds(HEADER_X, GRID_Y, CANVAS_W - HEADER_X * 2, 140);
            contentPanel.add(emptyPanel);
            contentPanel.setPreferredSize(new Dimension(CANVAS_W, 760));
            contentPanel.revalidate();
            return;
        }

        for (int i = 0; i < visibleItems.size(); i++) {
            KhoMenuItem item = visibleItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = GRID_X + col * (CARD_W + GAP_X);
            int y = GRID_Y + row * (CARD_H + GAP_Y);

            FeatureMenuCard card = new FeatureMenuCard(
                    item.title(),
                    item.description(),
                    getMenuIcon(item),
                    getMenuIcon(item),
                    () -> handleMenuClick(item)
            );
            card.setBounds(x, y, CARD_W, CARD_H);
            contentPanel.add(card);
            menuCards.add(card);
        }

        int rows = (int) Math.ceil(visibleItems.size() / (double) COLS);
        int contentHeight = GRID_Y + rows * CARD_H + Math.max(0, rows - 1) * GAP_Y + 80;
        contentPanel.setPreferredSize(new Dimension(CANVAS_W, Math.max(760, contentHeight)));
        contentPanel.revalidate();
    }

    private boolean canShow(KhoMenuItem item) {
        return item.permission() == null || PermissionUtil.hasAny(item.permission());
    }

    private JPanel createEmptyPermissionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel label = new JLabel("<html><div style='text-align:center;'>"
                + "Bạn chưa có quyền sử dụng chức năng kho.<br>"
                + "Vui lòng liên hệ quản trị viên để được cấp quyền."
                + "</div></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(UiTheme.TEXT_SOFT);
        label.setFont(UiTheme.regular(16));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private List<KhoMenuItem> getMenuItems() {
        return List.of(
                new KhoMenuItem("Nhập kho", "Tạo phiếu nhập và cập nhật tồn", "INVENTORY:IMPORT", "icons/menu-kho/nhap-kho.svg"),
                new KhoMenuItem("Xuất kho", "Tạo phiếu xuất và trừ tồn", "INVENTORY:EXPORT", "icons/menu-kho/xuat-kho.svg"),
                new KhoMenuItem("Điều chuyển kho", "Chuyển giữa các kho nguồn - đích", "INVENTORY:TRANSFER", "icons/menu-kho/dieu-chuyen-kho.svg"),
                new KhoMenuItem("Kiểm kho", "So sánh tồn hệ thống", "STOCKTAKE:CREATE", "icons/menu-kho/kiem-kho.svg"),
                new KhoMenuItem("Báo cáo hao hụt", "Ghi nhận hư hỏng, hết hạn, thất thoát", "WASTAGE:CREATE", "icons/menu-kho/bao-cao-hao-hut.svg"),
                new KhoMenuItem("Xem tồn kho", "Xem tồn hiện tại theo kho/nguyên liệu", "INVENTORY:VIEW", "icons/menu-kho/xem-ton-kho.svg"),
                new KhoMenuItem("Theo dõi HSD", "Theo dõi lô, HSD và cảnh báo", "INVENTORY:VIEW", "icons/menu-kho/theo-doi-hsd.svg"),
                new KhoMenuItem("Lịch sử", "Tra cứu nhập xuất điều chuyển", "INVENTORY:VIEW", "icons/menu-kho/tra-cuu-lich-su.svg"),
                new KhoMenuItem("Quản lý kho", "Thêm, sửa, xóa thông tin kho", "WAREHOUSE:VIEW", "icons/menu-kho/quan-ly-kho.svg"),
                new KhoMenuItem("Quản lý nguyên liệu", "Thêm, sửa, xóa nguyên liệu", "INGREDIENT:VIEW", "icons/menu-kho/quan-ly-nguyen-lieu.svg"),
                new KhoMenuItem("Quản lý nhà cung cấp", "Thêm, sửa, ngưng hoạt động NCC", "SUPPLIER:VIEW", "icons/menu-kho/quan-ly-nha-cung-cap.svg"),
                new KhoMenuItem("Quản lý đơn vị tính", "Gram, ml, cái, chai, gói ...", "UNIT:VIEW", "icons/menu-kho/quan-ly-don-vi-tinh.svg")
        );
    }

    private Icon getMenuIcon(KhoMenuItem item) {
        return IconLoader.svg(item.iconPath(), 30, 30);
    }

    private void handleMenuClick(KhoMenuItem item) {
        if (item.permission() != null && !PermissionUtil.hasAny(item.permission())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn chưa có quyền: " + item.permission(),
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if ("icons/menu-kho/nhap-kho.svg".equals(item.iconPath())) {
            new NhapKhoFrame().setVisible(true);
            dispose();
            return;
        }

        if ("icons/menu-kho/xuat-kho.svg".equals(item.iconPath())) {
            new XuatKhoFrame().setVisible(true);
            dispose();
            return;
        }

        if ("icons/menu-kho/xem-ton-kho.svg".equals(item.iconPath())) {
            new XemTonKhoFrame().setVisible(true);
            dispose();
            return;
        }

        openPlaceholder(item);
    }

    private void openPlaceholder(KhoMenuItem item) {
        String message = "Bạn vừa chọn: " + item.title() + "\n\n"
                + "Permission: " + (item.permission() == null ? "Chưa gắn quyền UI" : item.permission()) + "\n\n"
                + "Sau này thay openPlaceholder(...) bằng mở frame/panel CRUD tương ứng.";
        JOptionPane.showMessageDialog(this, message, item.title(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void goBack() {
        new MenuTongFrame().setVisible(true);
        dispose();
    }

    private record KhoMenuItem(
            String title,
            String description,
            String permission,
            String iconPath
    ) {}
}
