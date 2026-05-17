package com.coffeechain.ui;

import com.coffeechain.service.UserApiClient;
import com.coffeechain.service.UserApiClient.CreateUserRequest;
import com.coffeechain.service.UserApiClient.CreateUserResponse;
import com.coffeechain.service.UserApiClient.OptionDto;
import com.coffeechain.service.UserApiClient.UserLookupResponse;
import com.coffeechain.ui.createuser.CreateUserUiComponents.PromptPasswordField;
import com.coffeechain.ui.createuser.CreateUserUiComponents.PromptTextField;
import com.coffeechain.ui.createuser.CreateUserUiComponents.RoundedButton;
import com.coffeechain.ui.createuser.CreateUserUiComponents.RoundedFieldPanel;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Màn tạo tài khoản.
 *
 * Flow frontend:
 * 1. Mở frame -> gọi GET /api/users/lookups để load role và chi nhánh.
 * 2. Người dùng nhập username, email, password.
 * 3. Chọn branch và role.
 * 4. Bấm Signup -> gọi POST /api/users.
 *
 * Luật phân quyền chính vẫn nằm ở backend:
 * - ADMIN có thể tạo nhiều role.
 * - QUAN_LY_CHI_NHANH chỉ được tạo THU_NGAN trong chi nhánh của mình.
 */
public class TaoTaiKhoanFrame extends JFrame {

    private static final int ROOT_W = 1120;
    private static final int ROOT_H = 760;

    private static final int LEFT_W = 515;
    private static final int FORM_X = 128;
    private static final int FIELD_W = 360;
    private static final int FIELD_H = 34;

    private static final Color WHITE = Color.WHITE;
    private static final Color FIELD_FILL = Color.decode("#F6F6F6");
    private static final Color FIELD_DROPDOWN_SELECTED = Color.decode("#F8DCC6");
    private static final Color PRIMARY = Color.decode("#C67C4E");
    private static final Color PRIMARY_DARK = Color.decode("#B66F43");
    private static final Color TEXT_DARK = Color.decode("#111111");
    private static final Color TEXT_MUTED = Color.decode("#8B8AA5");
    private static final Color ERROR = Color.decode("#BE3C2D");
    private static final Color SUCCESS = Color.decode("#3C8C5A");

    private static final String RIGHT_IMAGE_PATH = "icons/menu-system-admin/register.png";
    private static final String ICON_BACK = "icons/phan-quyen-bao-mat/left.svg";

    private final UserApiClient userApiClient = new UserApiClient();

    private final JPanel root = new JPanel(null);

    private final PromptTextField usernameField = new PromptTextField("      Nhập tên của bạn");
    private final PromptTextField emailField = new PromptTextField("      Nhập email của bạn");
    private final PromptPasswordField passwordField = new PromptPasswordField("      Nhập mật khẩu");

    private final JComboBox<OptionDto> branchCombo = new JComboBox<>();
    private final JComboBox<OptionDto> roleCombo = new JComboBox<>();

    private final JCheckBox agreeCheckBox = new JCheckBox("I agree to the terms & policy");

    private final JLabel messageLabel = new JLabel(" ");
    private final RoundedButton createButton = new RoundedButton("Signup");

    public TaoTaiKhoanFrame() {
        setTitle("Phụng Lộc - Tạo tài khoản");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
        root.setBackground(WHITE);
        setContentPane(root);

        buildPage();
        bindEvents();
        loadLookups();

        pack();
        setLocationRelativeTo(null);
    }

    private void buildPage() {
        buildLeftPanel();
        buildRightImage();
    }

    private void buildLeftPanel() {
        JLabel backLabel = new JLabel("Tạo tài khoản");
        backLabel.setBounds(28, 18, 200, 24);
        backLabel.setForeground(new Color(200, 200, 200));
        backLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        root.add(backLabel);

        JLabel backButton = new JLabel("Quay lại");
        backButton.setIcon(loadSvgIcon(ICON_BACK, 18, 18));
        backButton.setIconTextGap(6);
        backButton.setBounds(28, 46, 120, 28);
        backButton.setForeground(PRIMARY);
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goBack();
            }
        });
        root.add(backButton);

        JLabel title = new JLabel("Get Started Now");
        title.setBounds(FORM_X, 112, 360, 42);
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        root.add(title);

        int y = 205;

        addLabel("Name", FORM_X, y);
        addField(usernameField, FORM_X, y + 26, FIELD_W, FIELD_H);

        y += 76;

        addLabel("Email address", FORM_X, y);
        addField(emailField, FORM_X, y + 26, FIELD_W, FIELD_H);

        y += 76;

        addLabel("Password", FORM_X, y);
        addField(passwordField, FORM_X, y + 26, FIELD_W, FIELD_H);

        y += 84;

        addLabel("Branch", FORM_X, y);
        addCombo(branchCombo, FORM_X, y + 26, FIELD_W, FIELD_H);

        y += 76;

        addLabel("Vai trò", FORM_X, y);
        addCombo(roleCombo, FORM_X, y + 26, FIELD_W, FIELD_H);

        agreeCheckBox.setBounds(FORM_X, 580, FIELD_W, 24);
        agreeCheckBox.setOpaque(false);
        agreeCheckBox.setForeground(TEXT_DARK);
        agreeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        agreeCheckBox.setFocusPainted(false);
        root.add(agreeCheckBox);

        createButton.setBounds(FORM_X, 602, FIELD_W, 34);
        createButton.addActionListener(e -> handleCreateUser());
        root.add(createButton);

        messageLabel.setBounds(FORM_X, 654, FIELD_W + 80, 24);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(ERROR);
        root.add(messageLabel);
    }

    private void buildRightImage() {
        FullImagePanel imagePanel = new FullImagePanel(RIGHT_IMAGE_PATH);

        int imageX = LEFT_W+20;
        int imageY = 0;
        int imageW = ROOT_W - LEFT_W;
        int imageH = ROOT_H;

        imagePanel.setBounds(imageX, imageY, imageW, imageH);
        root.add(imagePanel);
    }

    private void bindEvents() {
        roleCombo.addActionListener(e -> updateBranchStateByRole());
    }

    private void loadLookups() {
        setFormEnabled(false);
        showMessage("Đang tải dữ liệu...", true);

        new SwingWorker<UserLookupResponse, Void>() {
            @Override
            protected UserLookupResponse doInBackground() throws Exception {
                return userApiClient.getCreateUserLookups();
            }

            @Override
            protected void done() {
                try {
                    UserLookupResponse response = get();

                    roleCombo.setModel(new DefaultComboBoxModel<>(
                            response.getRoles().toArray(new OptionDto[0])
                    ));

                    branchCombo.setModel(new DefaultComboBoxModel<>(
                            response.getBranches().toArray(new OptionDto[0])
                    ));

                    updateBranchStateByRole();
                    setFormEnabled(true);
                    showMessage(" ", true);
                } catch (Exception ex) {
                    setFormEnabled(false);
                    showMessage("Không tải được vai trò/chi nhánh", false);
                    JOptionPane.showMessageDialog(
                            thisFrame(),
                            unwrapMessage(ex),
                            "Tạo tài khoản",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void handleCreateUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        OptionDto role = (OptionDto) roleCombo.getSelectedItem();
        OptionDto branch = (OptionDto) branchCombo.getSelectedItem();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            showMessage("Vui lòng nhập đầy đủ tên, email và mật khẩu", false);
            return;
        }

        if (!email.contains("@")) {
            showMessage("Email không hợp lệ", false);
            return;
        }

        if (password.length() < 6) {
            showMessage("Mật khẩu phải có ít nhất 6 ký tự", false);
            return;
        }

        if (role == null || role.getName().isBlank()) {
            showMessage("Vui lòng chọn vai trò", false);
            return;
        }

        if (!agreeCheckBox.isSelected()) {
            showMessage("Vui lòng đồng ý với điều khoản trước khi tạo tài khoản", false);
            return;
        }

        Long branchId = null;
        if (!"ADMIN".equalsIgnoreCase(role.getName())) {
            if (branch == null || branch.getId() == null) {
                showMessage("Tài khoản không phải ADMIN phải có chi nhánh", false);
                return;
            }
            branchId = branch.getId();
        }

        CreateUserRequest request = new CreateUserRequest();
        request.setTenDangNhap(username);
        request.setEmail(email);
        request.setMatKhau(password);
        request.setTenVaiTro(role.getName());
        request.setMaChiNhanh(branchId);

        createButton.setEnabled(false);
        showMessage("Đang tạo tài khoản...", true);

        new SwingWorker<CreateUserResponse, Void>() {
            @Override
            protected CreateUserResponse doInBackground() throws Exception {
                return userApiClient.createUser(request);
            }

            @Override
            protected void done() {
                createButton.setEnabled(true);

                try {
                    CreateUserResponse response = get();

                    showMessage("Tạo tài khoản thành công", true);

                    clearForm();
                } catch (Exception ex) {
                    showMessage("Tạo tài khoản thất bại", false);
                }
            }
        }.execute();
    }

    private void updateBranchStateByRole() {
        OptionDto role = (OptionDto) roleCombo.getSelectedItem();
        boolean isAdminRole = role != null && "ADMIN".equalsIgnoreCase(role.getName());

        if (isAdminRole) {
            branchCombo.setSelectedIndex(-1);
            branchCombo.setEnabled(false);
            return;
        }

        branchCombo.setEnabled(branchCombo.getItemCount() > 1);

        if (branchCombo.getSelectedIndex() < 0 && branchCombo.getItemCount() > 0) {
            branchCombo.setSelectedIndex(0);
        }
    }
    private void clearForm() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        agreeCheckBox.setSelected(false);

        if (roleCombo.getItemCount() > 0) {
            roleCombo.setSelectedIndex(0);
        }

        if (branchCombo.getItemCount() > 0) {
            branchCombo.setSelectedIndex(0);
        }

        updateBranchStateByRole();

        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private void setFormEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        roleCombo.setEnabled(enabled);
        branchCombo.setEnabled(enabled);
        agreeCheckBox.setEnabled(enabled);
        createButton.setEnabled(enabled);

        if (enabled) {
            updateBranchStateByRole();
        }
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, FIELD_W, 20);
        label.setForeground(TEXT_DARK);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        root.add(label);
    }

    private void addField(javax.swing.JTextField field, int x, int y, int w, int h) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        RoundedFieldPanel panel = new RoundedFieldPanel(field);
        panel.setBounds(x, y, w, h);
        root.add(panel);
    }

    private void addCombo(JComboBox<?> combo, int x, int y, int w, int h) {
        styleCombo(combo);

        SmoothInputPanel panel = new SmoothInputPanel();
        panel.setLayout(new BorderLayout());
        panel.setBounds(x, y, w, h);
        panel.add(combo, BorderLayout.CENTER);

        root.add(panel);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setUI(new DesignComboBoxUI());
        combo.setRenderer(new DesignComboBoxRenderer());
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(FIELD_FILL);
        combo.setForeground(TEXT_DARK);
        combo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
        combo.setOpaque(false);
        combo.setFocusable(false);
        combo.setMaximumRowCount(8);

        // Fix lỗi nền bị sẫm khi combo bị disabled
        combo.setEnabled(true);
    }

    private void showMessage(String message, boolean ok) {
        messageLabel.setForeground(ok ? SUCCESS : ERROR);
        messageLabel.setText(message);
    }

    private void goBack() {
        try {
            new QuanTriHeThongFrame().setVisible(true);
        } catch (Exception ignored) {
            try {
                new MenuTongFrame().setVisible(true);
            } catch (Exception ignoredAgain) {
                // Nếu chưa có frame trước đó thì chỉ đóng màn hiện tại.
            }
        }
        dispose();
    }

    private JFrame thisFrame() {
        return this;
    }

    private String unwrapMessage(Exception ex) {
        Throwable current = ex;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? "Không xử lý được yêu cầu"
                : current.getMessage();
    }

    private Icon loadSvgIcon(String path, int width, int height) {
        if (path == null || path.isBlank()) {
            return null;
        }

        try {
            return new FlatSVGIcon(path, width, height);
        } catch (Exception e) {
            return null;
        }
    }

    private static class SmoothInputPanel extends JPanel {
        SmoothInputPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(FIELD_FILL);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class DesignComboBoxUI extends BasicComboBoxUI {
        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            c.setOpaque(false);
        }

        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(TEXT_DARK);

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2 + 1;

                    g2.drawLine(cx - 5, cy - 3, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 5, cy - 3);

                    g2.dispose();
                }
            };

            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
            // SmoothInputPanel đã tự vẽ nền, không để ComboBox vẽ nền mặc định nữa.
        }
    }

    private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );

            if (value instanceof OptionDto option) {
                label.setText(option.getName());
            } else {
                label.setText("");
            }

            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
            label.setForeground(TEXT_DARK);

            /*
             * index == -1 nghĩa là phần đang hiển thị trong combo box,
             * không phải item trong dropdown.
             *
             * Chỗ này phải set opaque(false), nếu không Swing sẽ tự vẽ
             * nền xám đậm khi combo bị disabled.
             */
            if (index == -1) {
                label.setOpaque(false);
                label.setBackground(FIELD_FILL);
            } else {
                label.setOpaque(true);
                label.setBackground(isSelected ? FIELD_DROPDOWN_SELECTED : Color.WHITE);
            }

            return label;
        }
    }

    private static class FullImagePanel extends JPanel {
        private final Image image;

        FullImagePanel(String imagePath) {
            setOpaque(false);

            Image loaded = null;
            try {
                java.net.URL url = getClass().getClassLoader().getResource(imagePath);
                if (url != null) {
                    loaded = new ImageIcon(url).getImage();
                }
            } catch (Exception ignored) {
            }

            this.image = loaded;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (image == null) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                return;
            }

            int imgW = image.getWidth(this);
            int imgH = image.getHeight(this);

            if (imgW <= 0 || imgH <= 0) {
                return;
            }

            double scale = Math.max(getWidth() / (double) imgW, getHeight() / (double) imgH);

            int drawW = (int) Math.round(imgW * scale);
            int drawH = (int) Math.round(imgH * scale);

            int x = (getWidth() - drawW) / 2;
            int y = (getHeight() - drawH) / 2;

            g.drawImage(image, x, y, drawW, drawH, this);
        }
    }
}