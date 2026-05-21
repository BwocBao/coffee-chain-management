package com.coffeechain.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.imageio.ImageIO;

import com.coffeechain.service.RbacApiClient;
import com.coffeechain.service.SessionManager;
import com.coffeechain.ui.common.Async;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;

/**
 * Màn phân quyền và bảo mật. Màn này mở từ QuanTriHeThongFrame, cho phép xem
 * role, xem nhóm permission và cập nhật quyền cho role.
 */
public class PhanQuyenBaoMatFrame extends JFrame {

    private static final int FRAME_W = 1155;
    private static final int FRAME_H = 820;
    private static final int CANVAS_W = 1120;

    private static final int HEADER_X = 46;
    private static final int ROLE_Y = 184;
    private static final int ROLE_CARD_W = 253;
    private static final int ROLE_CARD_H = 105;
    private static final int ROLE_GAP = 18;

    private static final int TOOLBAR_Y = 322;
    private static final int GROUP_X = 46;
    private static final int GROUP_Y = 390;
    private static final int GROUP_GAP_X = 28;
    private static final int GROUP_GAP_Y = 24;
    private static final int GROUP_W = (CANVAS_W - GROUP_X * 2 - GROUP_GAP_X) / 2 + 20;
    private static final int GROUP_COLS = 2;

    private static final String ICON_BACK = "icons/phan-quyen-bao-mat/left.svg";
    private static final String ICON_ROLE_FALLBACK = "icons/phan-quyen-bao-mat/phan-quyen.svg";
    private static final String ICON_ROLE_ADMIN = "icons/menu-system-admin/role-admin.svg";
    private static final String ICON_ROLE_WAREHOUSE_MANAGER = "icons/menu-system-admin/role-quan-ly-kho.svg";
    private static final String ICON_ROLE_BRANCH_MANAGER = "icons/menu-system-admin/role-quan-ly-chi-nhanh.svg";
    private static final String ICON_ROLE_CASHIER = "icons/menu-system-admin/role-thu-ngan.svg";
    private static final String ICON_AVATAR = "icons/phan-quyen-bao-mat/avatar.svg";

    private final JPanel contentPanel = new JPanel(null);
    private final JScrollPane scrollPane;
    private final RbacApiClient rbacApiClient = new RbacApiClient();

    private List<RbacApiClient.RoleDto> roles = new ArrayList<>();
    private List<RbacApiClient.PermissionGroupDto> permissionGroups = new ArrayList<>();
    private RbacApiClient.RoleDto selectedRole;

    private final List<RoleCard> roleCards = new ArrayList<>();
    private final Map<Long, JCheckBox> checkboxByPermissionId = new LinkedHashMap<>();

    private final JLabel selectedRoleLabel = new JLabel("Chưa chọn vai trò");
    private final JLabel selectedRoleHint = new JLabel("Chọn một vai trò phía trên để xem và cập nhật quyền.");
    private final JLabel statusLabel = new JLabel(" ");
    private final RoundedButton saveButton = new RoundedButton("Lưu phân quyền").radius(12);
    private final RoundedButton reloadButton = new RoundedButton("Tải lại").radius(12)
            .background(new Color(150, 150, 150))
            .hover(new Color(120, 120, 120));

    private boolean readOnlyAdminRole = false;

    public PhanQuyenBaoMatFrame() {
        setTitle("Phụng Lộc - Phân quyền & bảo mật");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(FRAME_W, FRAME_H);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);

        contentPanel.setBackground(UiTheme.PAGE_BG);
        contentPanel.setPreferredSize(new Dimension(CANVAS_W, 930));

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        setContentPane(scrollPane);

        buildHeader();
        buildToolbar();

        if (!"ADMIN".equalsIgnoreCase(SessionManager.getCurrentUserRole())) {
            buildNotAdminPanel();
        } else {
            loadData();
        }

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    private void buildHeader() {
        JLabel backLabel = new JLabel("Quay lại");
        backLabel.setIcon(IconLoader.svg(ICON_BACK, 22, 22));
        backLabel.setIconTextGap(8);
        backLabel.setBounds(HEADER_X, 28, 150, 34);
        backLabel.setForeground(UiTheme.ORANGE);
        backLabel.setFont(UiTheme.regular(18));
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goBack();
            }
        });
        contentPanel.add(backLabel);

        JLabel titleLabel = new JLabel("PHÂN QUYỀN & BẢO MẬT");
        titleLabel.setBounds(HEADER_X, 86, 560, 46);
        titleLabel.setForeground(UiTheme.PRIMARY_DARK);
        titleLabel.setFont(UiTheme.bold(32));
        contentPanel.add(titleLabel);

        JLabel subtitle = new JLabel("Quản lý vai trò, tick quyền truy cập và kiểm soát chức năng theo từng nhóm người dùng.");
        subtitle.setBounds(HEADER_X, 132, 780, 28);
        subtitle.setForeground(UiTheme.TEXT_MUTED);
        subtitle.setFont(UiTheme.regular(15));
        contentPanel.add(subtitle);

        JLabel avatar = new JLabel(IconLoader.svg(ICON_AVATAR, 34, 34));
        avatar.setBounds(CANVAS_W - 52, 42, 40, 40);
        contentPanel.add(avatar);
    }

    private void buildToolbar() {
        ToolbarPanel toolbar = new ToolbarPanel();
        toolbar.setLayout(null);
        toolbar.setBounds(HEADER_X, TOOLBAR_Y - 12, CANVAS_W - HEADER_X * 2 + 40, 50);
        contentPanel.add(toolbar);

        selectedRoleLabel.setBounds(22, 8, 360, 22);
        selectedRoleLabel.setForeground(UiTheme.TEXT_DARK);
        selectedRoleLabel.setFont(UiTheme.bold(16));
        toolbar.add(selectedRoleLabel);

        selectedRoleHint.setBounds(22, 28, 580, 18);
        selectedRoleHint.setForeground(UiTheme.TEXT_MUTED);
        selectedRoleHint.setFont(UiTheme.regular(12));
        toolbar.add(selectedRoleHint);

        reloadButton.setBounds(CANVAS_W - HEADER_X * 2 - 250, 8, 105, 34);
        reloadButton.addActionListener(e -> loadData());
        toolbar.add(reloadButton);

        saveButton.setBounds(CANVAS_W - HEADER_X * 2 - 130, 8, 140, 34);
        saveButton.addActionListener(e -> savePermissions());
        toolbar.add(saveButton);

        statusLabel.setBounds(HEADER_X, TOOLBAR_Y + 45, 780, 24);
        statusLabel.setFont(UiTheme.regular(13));
        statusLabel.setForeground(UiTheme.TEXT_MUTED);
        contentPanel.add(statusLabel);
    }

    private void buildNotAdminPanel() {
        JPanel panel = new EmptyStatePanel(
                "Chỉ ADMIN được sử dụng chức năng Phân quyền & bảo mật.",
                "Vui lòng đăng nhập bằng tài khoản ADMIN để quản lý vai trò và quyền truy cập."
        );
        panel.setBounds(HEADER_X, 230, CANVAS_W - HEADER_X * 2, 210);
        contentPanel.add(panel);
        saveButton.setEnabled(false);
        reloadButton.setEnabled(false);
    }

    private void loadData() {
        setBusy(true, "Đang tải dữ liệu phân quyền...");
        Async.run(() -> {
            try {
                List<RbacApiClient.RoleDto> loadedRoles = rbacApiClient.getRoles();
                List<RbacApiClient.PermissionGroupDto> loadedGroups = rbacApiClient.getPermissionGroups();
                Async.ui(() -> {
                    roles = loadedRoles == null ? List.of() : loadedRoles;
                    permissionGroups = loadedGroups == null ? List.of() : loadedGroups;
                    rebuildRoleCards();
                    rebuildPermissionGroups();
                    if (!roles.isEmpty()) {
                        selectRole(roles.get(0));
                    } else {
                        setBusy(false, "Chưa có vai trò nào trong hệ thống.");
                    }
                });
            } catch (Exception ex) {
                Async.ui(() -> {
                    setBusy(false, "Lỗi tải dữ liệu: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Không tải được dữ liệu phân quyền:\n" + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void rebuildRoleCards() {
        for (RoleCard card : roleCards) {
            contentPanel.remove(card);
        }
        roleCards.clear();

        int totalW = roles.size() * ROLE_CARD_W + Math.max(0, roles.size() - 1) * ROLE_GAP + 3;
        int startX = Math.max(HEADER_X, (CANVAS_W - totalW) / 2);

        for (int i = 0; i < roles.size(); i++) {
            RoleCard card = new RoleCard(roles.get(i));
            card.setBounds(startX + i * (ROLE_CARD_W + ROLE_GAP), ROLE_Y, ROLE_CARD_W, ROLE_CARD_H);
            roleCards.add(card);
            contentPanel.add(card);
        }

        contentPanel.repaint();
    }

    private void rebuildPermissionGroups() {
        for (Component component : new ArrayList<>(Arrays.asList(contentPanel.getComponents()))) {
            if (component instanceof PermissionGroupPanel) {
                contentPanel.remove(component);
            }
        }

        checkboxByPermissionId.clear();

        // Tính số hàng
        int numRows = (int) Math.ceil((double) permissionGroups.size() / GROUP_COLS);

        // Tính chiều cao tối đa cho mỗi hàng
        int[] rowHeights = new int[numRows];
        for (int i = 0; i < numRows; i++) {
            rowHeights[i] = 0;
        }

        for (int i = 0; i < permissionGroups.size(); i++) {
            RbacApiClient.PermissionGroupDto group = permissionGroups.get(i);
            int row = i / GROUP_COLS;
            int groupH = calculateGroupHeight(group);
            rowHeights[row] = Math.max(rowHeights[row], groupH);
        }

        // Layout các card với chiều cao tối đa của mỗi hàng
        int yMax = GROUP_Y;
        for (int i = 0; i < permissionGroups.size(); i++) {
            RbacApiClient.PermissionGroupDto group = permissionGroups.get(i);
            int row = i / GROUP_COLS;
            int col = i % GROUP_COLS;

            // Tính Y position dựa trên tất cả các hàng trước + gap
            int y = GROUP_Y;
            for (int r = 0; r < row; r++) {
                y += rowHeights[r] + GROUP_GAP_Y;
            }

            int x = GROUP_X + col * (GROUP_W + GROUP_GAP_X);
            int groupH = rowHeights[row];

            PermissionGroupPanel panel = new PermissionGroupPanel(group);
            panel.setBounds(x, y, GROUP_W, groupH);
            contentPanel.add(panel);
            yMax = Math.max(yMax, y + groupH + 80);
        }

        contentPanel.setPreferredSize(new Dimension(CANVAS_W, Math.max(930, yMax)));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private int calculateGroupHeight(RbacApiClient.PermissionGroupDto group) {
        int count = group.getPermissions() == null ? 0 : group.getPermissions().size();
        int rows = (int) Math.ceil(count / 2.0);
        return 76 + rows * 38 + 24;
    }

    private void selectRole(RbacApiClient.RoleDto role) {
        selectedRole = role;

        for (RoleCard card : roleCards) {
            card.setSelected(Objects.equals(card.role.getMaVaiTro(), role.getMaVaiTro()));
        }

        String roleName = role.getTenVaiTro();
        selectedRoleLabel.setText("Role đang chọn: " + roleDisplayName(roleName));
        selectedRoleHint.setText(roleDescription(roleName));

        readOnlyAdminRole = "ADMIN".equalsIgnoreCase(roleName);
        saveButton.setEnabled(!readOnlyAdminRole);
        clearAllChecks();
        setAllCheckboxEnabled(!readOnlyAdminRole);

        setBusy(true, "Đang tải quyền của role " + roleDisplayName(roleName) + "...");
        Async.run(() -> {
            try {
                RbacApiClient.RolePermissionDto rolePermission = rbacApiClient.getRolePermissions(role.getMaVaiTro());
                Async.ui(() -> {
                    applyCheckedPermissions(rolePermission.getPermissionIds());
                    setAllCheckboxEnabled(!readOnlyAdminRole);
                    setBusy(false, readOnlyAdminRole
                            ? "ADMIN có toàn quyền. Không cho sửa quyền ADMIN để tránh khóa hệ thống."
                            : "Tick quyền cần cấp rồi bấm Lưu phân quyền.");
                });
            } catch (Exception ex) {
                Async.ui(() -> {
                    setBusy(false, "Lỗi tải quyền role: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Không tải được quyền của role:\n" + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void savePermissions() {
        if (selectedRole == null) {
            return;
        }

        if (readOnlyAdminRole) {
            JOptionPane.showMessageDialog(this,
                    "Không được sửa quyền của ADMIN để tránh khóa hệ thống.",
                    "Không cho phép", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Set<Long> ids = collectCheckedPermissionIds();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Lưu " + ids.size() + " quyền cho role " + roleDisplayName(selectedRole.getTenVaiTro()) + "?",
                "Xác nhận lưu", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true, "Đang lưu phân quyền...");
        Async.run(() -> {
            try {
                rbacApiClient.updateRolePermissions(selectedRole.getMaVaiTro(), ids);
                Async.ui(() -> {
                    setBusy(false, "Lưu phân quyền thành công");
                    JOptionPane.showMessageDialog(this,
                            "Đã cập nhật quyền cho role " + roleDisplayName(selectedRole.getTenVaiTro()),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                Async.ui(() -> {
                    setBusy(false, "Lỗi lưu phân quyền: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Không lưu được phân quyền:\n" + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void clearAllChecks() {
        for (JCheckBox box : checkboxByPermissionId.values()) {
            box.setSelected(false);
        }
    }

    private void applyCheckedPermissions(Set<Long> ids) {
        if (ids == null) {
            return;
        }

        for (Long id : ids) {
            JCheckBox box = checkboxByPermissionId.get(id);
            if (box != null) {
                box.setSelected(true);
            }
        }
    }

    private Set<Long> collectCheckedPermissionIds() {
        Set<Long> ids = new LinkedHashSet<>();
        for (Map.Entry<Long, JCheckBox> entry : checkboxByPermissionId.entrySet()) {
            if (entry.getValue().isSelected()) {
                ids.add(entry.getKey());
            }
        }
        return ids;
    }

    private void setAllCheckboxEnabled(boolean enabled) {
        for (JCheckBox box : checkboxByPermissionId.values()) {
            box.setEnabled(enabled);
        }
    }

    private void setBusy(boolean busy, String message) {
        saveButton.setEnabled(!busy && selectedRole != null && !readOnlyAdminRole);
        reloadButton.setEnabled(!busy);
        statusLabel.setForeground(busy ? UiTheme.PRIMARY_DARK : UiTheme.TEXT_MUTED);
        statusLabel.setText(message == null ? " " : message);
    }

    private void goBack() {
        new QuanTriHeThongFrame().setVisible(true);
        dispose();
    }

    private static String roleDisplayName(String role) {
        if (role == null) {
            return "-";
        }
        return switch (role) {
            case "ADMIN" ->
                "Admin";
            case "QUAN_LY_KHO" ->
                "Quản lý kho";
            case "QUAN_LY_CHI_NHANH" ->
                "Quản lý chi nhánh";
            case "THU_NGAN" ->
                "Thu ngân";
            default ->
                role;
        };
    }

    private static String roleDescription(String role) {
        if (role == null) {
            return "Vai trò tùy chỉnh";
        }
        return switch (role) {
            case "ADMIN" ->
                "Toàn quyền hệ thống, được xem mọi chức năng và quản lý phân quyền.";
            case "QUAN_LY_KHO" ->
                "Quản lý nhập, xuất, điều chuyển, kiểm kho và báo cáo hao hụt.";
            case "QUAN_LY_CHI_NHANH" ->
                "Quản lý nghiệp vụ trong chi nhánh được gán và tạo tài khoản thu ngân.";
            case "THU_NGAN" ->
                "Bán hàng, tạo đơn và thanh toán trên POS.";
            default ->
                "Vai trò tùy chỉnh trong hệ thống.";
        };
    }

    private static String moduleDisplayName(String module) {
        if (module == null) {
            return "Nhóm quyền";
        }
        return switch (module) {
            case "USER" ->
                "Người dùng";
            case "ROLE" ->
                "Vai trò & phân quyền";
            case "BRANCH" ->
                "Chi nhánh";
            case "PRODUCT" ->
                "Sản phẩm";
            case "INGREDIENT" ->
                "Nguyên liệu";
            case "SUPPLIER" ->
                "Nhà cung cấp";
            case "INVENTORY" ->
                "Kho";
            case "STOCKTAKE" ->
                "Kiểm kho";
            case "WASTAGE" ->
                "Hao hụt";
            case "ORDER" ->
                "Đơn hàng / POS";
            case "REPORT" ->
                "Báo cáo";
            case "UNIT" ->
                "Đơn vị tính";
            default ->
                module;
        };
    }

    private static Icon roleIcon(String role) {
        String iconPath = switch (role == null ? "" : role) {
            case "ADMIN" ->
                ICON_ROLE_ADMIN;
            case "QUAN_LY_KHO" ->
                ICON_ROLE_WAREHOUSE_MANAGER;
            case "QUAN_LY_CHI_NHANH" ->
                ICON_ROLE_BRANCH_MANAGER;
            case "THU_NGAN" ->
                ICON_ROLE_CASHIER;
            default ->
                null;
        };

        Icon icon = loadRoleIcon(iconPath, 34, 34);
        return icon == null ? IconLoader.svg(ICON_ROLE_FALLBACK, 28, 28) : icon;
    }

    private static Icon loadRoleIcon(String path, int width, int height) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.toLowerCase().endsWith(".svg")) {
            return IconLoader.svg(path, width, height);
        }
        return pngIcon(path, width, height);
    }
    private static Icon pngIcon(String path, int width, int height) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            java.net.URL url = PhanQuyenBaoMatFrame.class.getClassLoader().getResource(path);
            if (url == null) {
                return null;
            }

            BufferedImage source = ImageIO.read(url);
            if (source == null) {
                return null;
            }

            int minX = source.getWidth();
            int minY = source.getHeight();
            int maxX = -1;
            int maxY = -1;

            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int argb = source.getRGB(x, y);
                    if (!isBakedCheckerBackground(argb)) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }

            if (maxX < minX || maxY < minY) {
                return null;
            }

            BufferedImage cleaned = new BufferedImage(maxX - minX + 1, maxY - minY + 1, BufferedImage.TYPE_INT_ARGB);
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    int argb = source.getRGB(x, y);
                    cleaned.setRGB(x - minX, y - minY, isBakedCheckerBackground(argb) ? 0x00000000 : argb);
                }
            }

            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(cleaned, 0, 0, width, height, null);
            g.dispose();
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean isBakedCheckerBackground(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        return alpha == 0 || (red >= 232 && green >= 232 && blue >= 232 && Math.abs(red - green) <= 6 && Math.abs(green - blue) <= 6);
    }
    private class RoleCard extends JPanel {

        private final RbacApiClient.RoleDto role;
        private boolean hover;
        private boolean selected;

        private final JLabel iconLabel = new JLabel();
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();

        RoleCard(RbacApiClient.RoleDto role) {
            this.role = role;
            setOpaque(false);
            setLayout(null);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setIcon(roleIcon(role.getTenVaiTro()));
            add(iconLabel);

            titleLabel.setText(roleDisplayName(role.getTenVaiTro()));
            titleLabel.setFont(UiTheme.bold(17));
            add(titleLabel);

            descLabel.setText(toHtml(roleDescription(role.getTenVaiTro()), 130));
            descLabel.setFont(UiTheme.regular(12));
            add(descLabel);

            MouseAdapter listener = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    updateColors();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    updateColors();
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectRole(role);
                }
            };

            addMouseListener(listener);
            iconLabel.addMouseListener(listener);
            titleLabel.addMouseListener(listener);
            descLabel.addMouseListener(listener);
            updateColors();
        }

        @Override
        public void doLayout() {
            iconLabel.setBounds(18, 18, 44, 44);
            titleLabel.setBounds(74, 16, 155, 24);
            descLabel.setBounds(74, 42, 160, 44);
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            updateColors();
            repaint();
        }

        private void updateColors() {
            boolean active = hover || selected;
            titleLabel.setForeground(active ? Color.WHITE : UiTheme.TEXT_DARK);
            descLabel.setForeground(active ? new Color(255, 255, 255, 230) : UiTheme.TEXT_MUTED);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean active = hover || selected;
            g2.setColor(active ? UiTheme.PRIMARY : Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

            g2.setColor(active ? UiTheme.PRIMARY_DARK : UiTheme.BORDER_LIGHT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

            g2.setColor(active ? new Color(255, 255, 255, 235) : UiTheme.CARD_ICON_BG);
            g2.fillRoundRect(18, 18, 44, 44, 12, 12);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class PermissionGroupPanel extends JPanel {

        PermissionGroupPanel(RbacApiClient.PermissionGroupDto group) {
            setOpaque(false);
            setLayout(null);

            JLabel title = new JLabel(moduleDisplayName(group.getTenModule()));
            title.setForeground(UiTheme.PRIMARY_DARK);
            title.setFont(UiTheme.bold(18));
            title.setBounds(24, 18, 330, 28);
            add(title);

//            JLabel code = new JLabel(group.getTenModule());
//            code.setHorizontalAlignment(SwingConstants.CENTER);
//            code.setForeground(UiTheme.PRIMARY_DARK);
//            code.setFont(UiTheme.bold(11));
//            code.setBounds(GROUP_W - 106, 20, 78, 24);
//            add(code);
            List<RbacApiClient.PermissionDto> permissions = group.getPermissions() == null
                    ? List.of()
                    : group.getPermissions();

            for (int i = 0; i < permissions.size(); i++) {
                RbacApiClient.PermissionDto permission = permissions.get(i);
                int row = i / 2;
                int col = i % 2;

                JCheckBox box = new JCheckBox(permission.getTenQuyen());
                box.setOpaque(false);
                box.setFocusPainted(false);
                box.setForeground(UiTheme.TEXT_DARK);
                box.setFont(UiTheme.regular(14));
                box.setToolTipText(permission.getCode());
                box.setBounds(24 + col * 235, 62 + row * 38, 220, 30);

                checkboxByPermissionId.put(permission.getMaQuyen(), box);
                add(box);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

            g2.setColor(UiTheme.BORDER_LIGHT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

//            g2.setColor(new Color(198, 124, 78, 28));
//            g2.fillRoundRect(GROUP_W - 106, 20, 78, 24, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ToolbarPanel extends JPanel {

        ToolbarPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 245));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(UiTheme.BORDER_LIGHT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class EmptyStatePanel extends JPanel {

        private final String title;
        private final String message;

        EmptyStatePanel(String title, String message) {
            this.title = title;
            this.message = message;
            setOpaque(false);
            setLayout(new GridBagLayout());

            JLabel label = new JLabel("<html><div style='text-align:center;'>"
                    + "<b>" + title + "</b><br/>"
                    + message
                    + "</div></html>");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(UiTheme.TEXT_MUTED);
            label.setFont(UiTheme.regular(16));
            add(label);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g2.setColor(UiTheme.BORDER_LIGHT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static String toHtml(String text, int width) {
        if (text == null) {
            return "";
        }
        return "<html><div style='width:" + width + "px;'>" + text + "</div></html>";
    }
}
