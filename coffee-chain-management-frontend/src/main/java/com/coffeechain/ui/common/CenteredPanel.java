package com.coffeechain.ui.common;

import javax.swing.JPanel;
import java.awt.Dimension;

/**
 * Wrapper giúp căn giữa một panel con trong một vùng có kích thước cố định.
 * Đang dùng ở LoginFrame để đặt card đăng nhập đúng vị trí.
 */
public class CenteredPanel extends JPanel {
    public CenteredPanel(JPanel child, int width, int height, int childX, int childY) {
        setOpaque(false);
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        child.setLocation(childX, childY);
        add(child);
    }
}
