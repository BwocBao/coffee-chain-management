package com.coffeechain.ui.common;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Point;

/**
 * Helper tạo JScrollPane theo style app.
 *
 * hiddenVerticalScroll(...) tạo scroll pane ẩn thanh dọc nhưng vẫn cho cuộn bằng chuột.
 * scrollTop(...) đưa viewport về đầu sau khi màn hình render xong.
 */
public final class ScrollFrameSupport {
    private ScrollFrameSupport() {}

    public static JScrollPane hiddenVerticalScroll(JPanel contentPanel, int unitIncrement) {
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setMinimumSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
        return scrollPane;
    }

    public static void scrollTop(JScrollPane scrollPane) {
        SwingUtilities.invokeLater(() -> {
            scrollPane.getViewport().setViewPosition(new Point(0, 0));
            scrollPane.getVerticalScrollBar().setValue(0);
        });
    }
}
