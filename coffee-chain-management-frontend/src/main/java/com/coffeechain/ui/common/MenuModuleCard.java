package com.coffeechain.ui.common;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Card module cấp cao trên menu tổng/quản trị hệ thống.
 * Đang dùng ở MenuTongFrame và QuanTriHeThongFrame để mở các nhóm chức năng lớn.
 */
public class MenuModuleCard extends JPanel {
    private static final int ICON_SIZE = 58;
    private static final int BUTTON_W = 100;
    private static final int BUTTON_H = 30;
    private static final int RADIUS = 12;

    private final Runnable action;
    private final boolean compact;
    private boolean hover;

    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JLabel descriptionLabel;
    private final RoundedButton actionButton;

    public MenuModuleCard(String title, String description, Icon icon, Runnable action) {
        this(title, description, icon, action, false);
    }

    public MenuModuleCard(String title, String description, Icon icon, Runnable action, boolean compact) {
        this.action = action;
        this.compact = compact;

        setLayout(null);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(iconLabel);

        titleLabel = new JLabel(title);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(UiTheme.PRIMARY);
        titleLabel.setFont(UiTheme.bold(compact ? 15 : 16));
        add(titleLabel);

        descriptionLabel = new JLabel(toHtmlCenter(description));
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descriptionLabel.setForeground(UiTheme.TEXT_SOFT);
        descriptionLabel.setFont(UiTheme.regular(compact ? 13 : 14));
        add(descriptionLabel);

        actionButton = new RoundedButton("Truy cập");
        actionButton.addActionListener(e -> runAction());
        add(actionButton);

        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setHoverState(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                /*
                 * Khi rê chuột từ card vào label/icon/button con,
                 * Swing có thể bắn mouseExited cho component cha.
                 * Vì vậy delay nhẹ rồi kiểm tra chuột thật sự còn nằm trong card không.
                 */
                SwingUtilities.invokeLater(() -> {
                    Point mouse = getMousePosition();
                    setHoverState(mouse != null && contains(mouse));
                });
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                /*
                 * Nếu click vào button thì button tự xử lý bằng ActionListener.
                 * Tránh gọi runAction() 2 lần.
                 */
                if (e.getSource() != actionButton) {
                    runAction();
                }
            }
        };

        addMouseListener(listener);
        iconLabel.addMouseListener(listener);
        titleLabel.addMouseListener(listener);
        descriptionLabel.addMouseListener(listener);
        actionButton.addMouseListener(listener);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int w = getWidth();
        int h = getHeight();

        if (compact) {
            int iconSize = Math.min(ICON_SIZE, 54);
            iconLabel.setBounds((w - iconSize) / 2, 18, iconSize, iconSize);
            titleLabel.setBounds(12, 78, w - 24, 28);
            descriptionLabel.setBounds(16, 108, w - 32, 54);
            actionButton.setBounds((w - BUTTON_W) / 2, h - 40, BUTTON_W, BUTTON_H);
            return;
        }

        iconLabel.setBounds((w - ICON_SIZE) / 2, 24, ICON_SIZE, ICON_SIZE);
        titleLabel.setBounds(12, 94, w - 24, 30);
        descriptionLabel.setBounds(18, 132, w - 29, 56);
        actionButton.setBounds((w - BUTTON_W) / 2, h - 48, BUTTON_W, BUTTON_H);
    }

    private void runAction() {
        if (action != null) {
            action.run();
        }
    }

    private void setHoverState(boolean hover) {
        if (this.hover == hover) {
            return;
        }

        this.hover = hover;
        updateHover();
        repaint();
    }

    private void updateHover() {
        titleLabel.setForeground(hover ? UiTheme.PRIMARY_DARK : UiTheme.PRIMARY);
        descriptionLabel.setForeground(hover ? UiTheme.TEXT_DARK : UiTheme.TEXT_SOFT);
        actionButton.background(hover ? UiTheme.PRIMARY_DARK : UiTheme.PRIMARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /*
         * Chỉ vẽ nền card ở đây.
         * Viền hover sẽ được vẽ cuối cùng trong paint()
         * để không bị label/button con che mất.
         */
        g2.setColor(hover ? Color.WHITE : UiTheme.CARD_BG);
        g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, RADIUS, RADIUS);

        g2.dispose();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        /*
         * Vẽ viền sau cùng để hover border luôn nổi lên trên.
         * Vẽ lùi vào trong vài pixel để không bị clip ở mép component.
         */
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (hover) {
            g2.setColor(UiTheme.PRIMARY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, RADIUS, RADIUS);
        } else {
            g2.setColor(UiTheme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, RADIUS, RADIUS);
        }

        g2.dispose();
    }

    private static String toHtmlCenter(String text) {
        if (text == null) {
            return "";
        }

        return "<html><div style='text-align:center;'>"
                + text.replace("\n", "<br/>")
                + "</div></html>";
    }
}
