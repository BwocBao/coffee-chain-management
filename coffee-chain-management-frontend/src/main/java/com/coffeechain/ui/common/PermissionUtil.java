package com.coffeechain.ui.common;

import com.coffeechain.service.SessionManager;

/**
 * Helper đọc quyền từ SessionManager để quyết định có hiển thị menu/nút chức năng hay không.
 * Các frame menu dùng hasAny(...) trước khi add card vào UI.
 */
public final class PermissionUtil {
    private PermissionUtil() {}

    /**
     * Trả true nếu user hiện tại có ít nhất một permission trong danh sách.
     * ADMIN được SessionManager xử lý là có toàn quyền.
     */
    public static boolean hasAny(String... permissions) {
        if (permissions == null) return false;
        for (String permission : permissions) {
            if (SessionManager.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
}
