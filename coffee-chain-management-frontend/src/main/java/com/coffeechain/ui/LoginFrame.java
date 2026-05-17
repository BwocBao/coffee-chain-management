package com.coffeechain.ui;

import com.coffeechain.dto.LoginResponse;
import com.coffeechain.service.AuthApiClient;
import com.coffeechain.service.SessionManager;
import com.coffeechain.ui.common.CenteredPanel;
import com.coffeechain.ui.common.LoginFieldPanel;
import com.coffeechain.ui.common.PromptPasswordField;
import com.coffeechain.ui.common.PromptTextField;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.ShadowCardPanel;
import com.coffeechain.ui.common.UiTheme;
import com.coffeechain.ui.common.icons.EyeLineIcon;
import com.coffeechain.ui.common.icons.LockLineIcon;
import com.coffeechain.ui.common.icons.SquareCheckIcon;
import com.coffeechain.ui.common.icons.UserLineIcon;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;

/**
 * Màn hình đăng nhập đầu tiên của app.
 * Sau khi login thành công sẽ lưu session và mở {@link MenuTongFrame}.
 */
public class LoginFrame extends JFrame {
    private static final int CARD_W = 651;
    private static final int CARD_H = 650;
    private static final Color PAGE_BG = Color.decode("#FDF0D5");
    private static final Color ICON_GRAY = Color.decode("#D9D9D9");
    private static final Color ERROR = Color.decode("#BE3C2D");
    private static final Color SUCCESS = Color.decode("#3C8C5A");

    private final PromptTextField usernameField = new PromptTextField("  Username");
    private final PromptPasswordField passwordField = new PromptPasswordField("  Password");

    private final JCheckBox rememberMe = new JCheckBox("Remember Me");
    private final RoundedButton signInButton = new RoundedButton("Sign in");
    private final JLabel forgotPasswordLabel = new JLabel("Forgot Password?");
    private final JLabel messageLabel = new JLabel(" ");

    private final AuthApiClient authApiClient = new AuthApiClient();
    private boolean passwordVisible = false;

    public LoginFrame() {
        setTitle("Phụng Lộc - Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 780);
        setMinimumSize(new Dimension(820, 720));
        setLocationRelativeTo(null);
        setContentPane(new LoginBackgroundPanel());

        ShadowCardPanel card = new ShadowCardPanel(20, Color.WHITE);
        card.setLayout(null);
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setBounds(0, 0, CARD_W, CARD_H);

        setLayout(new GridBagLayout());
        add(new CenteredPanel(card, 731, 730, 40, 36));

        buildCard(card);
    }

    private void buildCard(JPanel card) {
        JLabel title = new JLabel("<html>Welcome Back<br/>Quản trị hệ thống Phụng Lộc</html>");
        title.setForeground(UiTheme.TEXT_DARK);
        title.setFont(UiTheme.bold(26));
        title.setBounds(60, 67, 430, 78);
        card.add(title);

        JLabel subtitle = new JLabel("Login to your account");
        subtitle.setForeground(UiTheme.TEXT_SOFT);
        subtitle.setFont(UiTheme.regular(14));
        subtitle.setBounds(60, 158, 250, 28);
        card.add(subtitle);

        LoginFieldPanel usernamePanel = new LoginFieldPanel(
                new UserLineIcon(ICON_GRAY),
                usernameField,
                null
        );
        usernamePanel.setBounds(56, 215, 539, 74);
        card.add(usernamePanel);

        JButton eyeButton = createEyeButton();
        LoginFieldPanel passwordPanel = new LoginFieldPanel(
                new LockLineIcon(ICON_GRAY),
                passwordField,
                eyeButton
        );
        passwordPanel.setBounds(56, 327, 539, 74);
        card.add(passwordPanel);

        forgotPasswordLabel.setForeground(UiTheme.TEXT_SOFT);
        forgotPasswordLabel.setFont(UiTheme.regular(13));
        forgotPasswordLabel.setBounds(480, 407, 130, 25);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new ForgotPasswordFrame(LoginFrame.this).setVisible(true);
            }
        });
        card.add(forgotPasswordLabel);

        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);
        rememberMe.setForeground(UiTheme.TEXT_SOFT);
        rememberMe.setFont(UiTheme.regular(14));
        rememberMe.setIcon(new SquareCheckIcon(false, UiTheme.PRIMARY, ICON_GRAY));
        rememberMe.setSelectedIcon(new SquareCheckIcon(true, UiTheme.PRIMARY, ICON_GRAY));
        rememberMe.setBounds(60, 458, 180, 40);
        card.add(rememberMe);

        signInButton.setBounds(56, 520, 539, 74);
        signInButton.addActionListener(e -> doLogin());
        card.add(signInButton);

        messageLabel.setForeground(ERROR);
        messageLabel.setFont(UiTheme.regular(12));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBounds(70, 596, 510, 22);
        card.add(messageLabel);
    }

    private JButton createEyeButton() {
        JButton eyeButton = new JButton(new EyeLineIcon(false, ICON_GRAY));
        eyeButton.setBorderPainted(false);
        eyeButton.setContentAreaFilled(false);
        eyeButton.setFocusPainted(false);
        eyeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeButton.addActionListener(e -> togglePassword(eyeButton));
        return eyeButton;
    }

    private void togglePassword(JButton eyeButton) {
        passwordVisible = !passwordVisible;
        passwordField.setEchoChar(passwordVisible ? (char) 0 : '•');
        eyeButton.setIcon(new EyeLineIcon(passwordVisible, ICON_GRAY));
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isBlank() || password.isBlank()) {
            showMessage("Vui lòng nhập tên đăng nhập và mật khẩu", false);
            return;
        }

        signInButton.setEnabled(false);
        signInButton.setText("Signing in...");
        showMessage("Đang đăng nhập...", true);

        new Thread(() -> {
            try {
                LoginResponse response = authApiClient.login(username, password);
                SessionManager.saveSession(response.getToken(), response.getUser());

                SwingUtilities.invokeLater(() -> {
                    showMessage("Đăng nhập thành công", true);
                    new MenuTongFrame().setVisible(true);
                    dispose();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> showMessage("Đăng nhập thất bại: " + ex.getMessage(), false));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    signInButton.setEnabled(true);
                    signInButton.setText("Sign in");
                });
            }
        }).start();
    }

    private void showMessage(String msg, boolean ok) {
        messageLabel.setForeground(ok ? SUCCESS : ERROR);
        messageLabel.setText(msg);
    }

    private static class LoginBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(PAGE_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
