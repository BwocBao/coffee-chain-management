package com.coffeechain.ui.common;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;

/**
 * Helper load icon SVG và ảnh từ thư mục resources.
 * Nếu path rỗng hoặc resource không tồn tại thì trả null, không tự vẽ icon thay thế.
 */
public final class IconLoader {
    private IconLoader() {
    }

    /**
     * Load SVG từ resources theo path và scale về kích thước truyền vào.
     * Trả null nếu path rỗng hoặc SVG load lỗi.
     */
    public static Icon svg(String path, int width, int height) {
        try {
            if (path == null || path.isBlank()) {
                return null;
            }
            return new FlatSVGIcon(path, width, height);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Load ảnh bitmap từ resources, thường dùng cho background hoặc hình minh họa.
     * Trả null nếu không tìm thấy ảnh.
     */
    public static Image imageFromResource(String path) {
        try {
            if (path == null || path.isBlank()) {
                return null;
            }
            java.net.URL url = IconLoader.class.getClassLoader().getResource(path);
            return url == null ? null : new ImageIcon(url).getImage();
        } catch (Exception e) {
            return null;
        }
    }
}
