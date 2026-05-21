package com.coffeechain.ui;

import com.coffeechain.service.AuthApiClient;
import com.coffeechain.service.AuthApiClient.ForgotPasswordResponse;
import com.coffeechain.ui.common.CenteredPanel;
import com.coffeechain.ui.common.LoginFieldPanel;
import com.coffeechain.ui.common.PromptPasswordField;
import com.coffeechain.ui.common.PromptTextField;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.ShadowCardPanel;
import com.coffeechain.ui.common.UiTheme;
import com.coffeechain.ui.common.icons.LockLineIcon;
import com.coffeechain.ui.common.icons.UserLineIcon;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;

/**
 * Man hinh quen mat khau: nhap email, nhap ma xac nhan, sau do dat lai mat khau.
 */
public class ForgotPasswordFrame extends JFrame {
    private static final int CARD_W = 620;
    private static final int CARD_H = 590;
    private static final String SEND_CODE_TEXT = "Gửi mã";
    private static final String VERIFY_CODE_TEXT = "Xác nhận mã";
    private static final String RESET_PASSWORD_TEXT = "Đổi mật khẩu";
    private static final Color PAGE_BG = Color.decode("#FDF0D5");
    private static final Color ICON_GRAY = Color.decode("#D9D9D9");
    private static final Color ERROR = Color.decode("#BE3C2D");
    private static final Color SUCCESS = Color.decode("#3C8C5A");

    private final PromptTextField emailField = new PromptTextField("  Email");
    private final PromptTextField codeField = new PromptTextField("  Mã xác nhận");
    private final PromptPasswordField newPasswordField = new PromptPasswordField("  Mật khẩu mới");
    private final PromptPasswordField confirmPasswordField = new PromptPasswordField("  Nhập lại mật khẩu mới");
    private final RoundedButton sendCodeButton = new RoundedButton(SEND_CODE_TEXT);
    private final RoundedButton verifyCodeButton = new RoundedButton(VERIFY_CODE_TEXT);
    private final RoundedButton resetPasswordButton = new RoundedButton(RESET_PASSWORD_TEXT);
    private final RoundedButton closeButton = new RoundedButton("Quay lại");
    private final JLabel messageLabel = new JLabel(" ");
    private final JLabel hintLabel = new JLabel(" ");

    private final AuthApiClient authApiClient = new AuthApiClient();
    private int currentStep = 1;
    private boolean busy = false;

    public ForgotPasswordFrame(JFrame parent) {
        setTitle("Phụng Lộc - Quên mật khẩu");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 720);
        setMinimumSize(new Dimension(760, 680));
        setLocationRelativeTo(parent);
        setContentPane(new ForgotPasswordBackgroundPanel());

        ShadowCardPanel card = new ShadowCardPanel(20, Color.WHITE);
        card.setLayout(null);
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setBounds(0, 0, CARD_W, CARD_H);

        setLayout(new GridBagLayout());
        add(new CenteredPanel(card, 700, 660, 40, 30));
        buildCard(card);
        setStep(1);
    }

    private void buildCard(JPanel card) {
        JLabel title = new JLabel("<html>Forgot Password<br/>Phụng Lộc Coffee</html>");
        title.setForeground(UiTheme.TEXT_DARK);
        title.setFont(UiTheme.bold(26));
        title.setBounds(54, 42, 430, 72);
        card.add(title);

        JLabel subtitle = new JLabel("Nhập email để nhận mã xác nhận đặt lại mật khẩu");
        subtitle.setForeground(UiTheme.TEXT_SOFT);
        subtitle.setFont(UiTheme.regular(14));
        subtitle.setBounds(54, 122, 430, 26);
        card.add(subtitle);

        LoginFieldPanel emailPanel = new LoginFieldPanel(new UserLineIcon(ICON_GRAY), emailField, null);
        emailPanel.setBounds(50, 172, 520, 58);
        card.add(emailPanel);

        LoginFieldPanel codePanel = new LoginFieldPanel(new LockLineIcon(ICON_GRAY), codeField, null);
        codePanel.setBounds(50, 248, 270, 58);
        card.add(codePanel);

        sendCodeButton.setBounds(340, 248, 110, 58);
        sendCodeButton.addActionListener(e -> requestCode());
        card.add(sendCodeButton);

        verifyCodeButton.setBounds(462, 248, 108, 58);
        verifyCodeButton.addActionListener(e -> verifyCode());
        card.add(verifyCodeButton);

        LoginFieldPanel newPasswordPanel = new LoginFieldPanel(new LockLineIcon(ICON_GRAY), newPasswordField, null);
        newPasswordPanel.setBounds(50, 330, 520, 58);
        card.add(newPasswordPanel);

        LoginFieldPanel confirmPasswordPanel = new LoginFieldPanel(new LockLineIcon(ICON_GRAY), confirmPasswordField, null);
        confirmPasswordPanel.setBounds(50, 404, 520, 58);
        card.add(confirmPasswordPanel);

        resetPasswordButton.setBounds(50, 486, 170, 48);
        resetPasswordButton.addActionListener(e -> resetPassword());
        card.add(resetPasswordButton);

        closeButton.background(Color.decode("#B9B9B9")).hover(Color.decode("#A8A8A8"));
        closeButton.setForeground(UiTheme.TEXT_DARK);
        closeButton.setBounds(430, 486, 140, 48);
        closeButton.addActionListener(e -> dispose());
        card.add(closeButton);

//        hintLabel.setForeground(UiTheme.TEXT_SOFT);
//        hintLabel.setFont(UiTheme.regular(12));
//        hintLabel.setBounds(50, 536, 520, 18);
//        card.add(hintLabel);

        messageLabel.setForeground(ERROR);
        messageLabel.setFont(UiTheme.regular(12));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBounds(50, 558, 520, 22);
        card.add(messageLabel);
    }

    private void requestCode() {
        String email = emailField.getText().trim();
        if (email.isBlank() || !email.contains("@")) {
            showMessage("Vui long nhap email hop le", false);
            return;
        }

        hintLabel.setText("Dang gui ma xac nhan den email, vui long doi vai giay.");
        showMessage("Dang gui ma xac nhan...", true);
        setBusy(true, "Dang gui ma xac nhan...");
        new Thread(() -> {
            try {
                ForgotPasswordResponse response = authApiClient.forgotPassword(email);
                SwingUtilities.invokeLater(() -> {
                    setStep(2);
                    String debugCode = response == null ? null : response.getDebugCode();
                    if (debugCode != null && !debugCode.isBlank()) {
                        hintLabel.setText("Ma test local: " + debugCode);
                    } else {
                        hintLabel.setText("Kiem tra email de lay ma xac nhan.");
                    }
                    showMessage("Da gui ma xac nhan", true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    hintLabel.setText("Kiem tra email tai khoan hoac cau hinh SMTP roi thu lai.");
                    showMessage("Khong gui duoc ma: " + ex.getMessage(), false);
                });
            } finally {
                SwingUtilities.invokeLater(() -> setBusy(false, null));
            }
        }).start();
    }

    private void verifyCode() {
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        if (code.length() != 6) {
            showMessage("Ma xac nhan gom 6 chu so", false);
            return;
        }

        hintLabel.setText("Dang kiem tra ma xac nhan.");
        showMessage("Dang kiem tra ma...", true);
        setBusy(true, "Dang kiem tra ma...");
        new Thread(() -> {
            try {
                authApiClient.verifyResetCode(email, code);
                SwingUtilities.invokeLater(() -> {
                    setStep(3);
                    showMessage("Ma hop le, hay nhap mat khau moi", true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> showMessage("Ma khong hop le: " + ex.getMessage(), false));
            } finally {
                SwingUtilities.invokeLater(() -> setBusy(false, null));
            }
        }).start();
    }

    private void resetPassword() {
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (newPassword.length() < 6) {
            showMessage("Mat khau moi phai co it nhat 6 ky tu", false);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showMessage("Mat khau nhap lai khong khop", false);
            return;
        }

        hintLabel.setText("Dang cap nhat mat khau moi.");
        showMessage("Dang doi mat khau...", true);
        setBusy(true, "Dang doi mat khau...");
        new Thread(() -> {
            try {
                authApiClient.resetPassword(email, code, newPassword);
                SwingUtilities.invokeLater(() -> {
                    setStep(1);
                    clearSensitiveFields();
                    hintLabel.setText("Ban co the quay lai dang nhap bang mat khau moi.");
                    showMessage("Doi mat khau thanh cong", true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> showMessage("Doi mat khau that bai: " + ex.getMessage(), false));
            } finally {
                SwingUtilities.invokeLater(() -> setBusy(false, null));
            }
        }).start();
    }

    private void setStep(int step) {
        currentStep = step;
        if (busy) {
            return;
        }
        emailField.setEnabled(step == 1);
        codeField.setEnabled(step >= 2);
        newPasswordField.setEnabled(step >= 3);
        confirmPasswordField.setEnabled(step >= 3);
        sendCodeButton.setEnabled(step == 1);
        verifyCodeButton.setEnabled(step == 2);
        resetPasswordButton.setEnabled(step == 3);
    }

    private void setBusy(boolean busy, String message) {
        this.busy = busy;

        if (busy) {
            emailField.setEnabled(false);
            codeField.setEnabled(false);
            newPasswordField.setEnabled(false);
            confirmPasswordField.setEnabled(false);
            sendCodeButton.setEnabled(false);
            verifyCodeButton.setEnabled(false);
            resetPasswordButton.setEnabled(false);

            if (currentStep == 1) {
                sendCodeButton.setText("Dang gui...");
            } else if (currentStep == 2) {
                verifyCodeButton.setText("Dang kiem...");
            } else if (currentStep == 3) {
                resetPasswordButton.setText("Dang doi...");
            }
        } else {
            sendCodeButton.setText(SEND_CODE_TEXT);
            verifyCodeButton.setText(VERIFY_CODE_TEXT);
            resetPasswordButton.setText(RESET_PASSWORD_TEXT);
            setStep(currentStep);
        }

        if (message != null) {
            showMessage(message, true);
        }
    }

    private void clearSensitiveFields() {
        codeField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void showMessage(String msg, boolean ok) {
        messageLabel.setForeground(ok ? SUCCESS : ERROR);
        messageLabel.setText(msg);
    }

    private static class ForgotPasswordBackgroundPanel extends JPanel {
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
