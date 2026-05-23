package com.coffeechain.service;

import com.coffeechain.dto.UserInfoResponse;

import java.util.Set;

/**
 * Lưu phiên đăng nhập hiện tại trong bộ nhớ của app Swing.
 * Các màn hình dùng class này để lấy token, thông tin user và kiểm tra quyền trước khi hiển thị chức năng.
 */
public class SessionManager {
    private static String token;
    private static UserInfoResponse currentUser;

    private SessionManager() {
    }

    public static void saveSession(String newToken, UserInfoResponse user) {
        token = newToken;
        currentUser = user;
    }

    public static String getToken() {
        return token;
    }

    public static UserInfoResponse getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserDisplayName() {
        if (currentUser == null) {
            return "";
        }

        String username = currentUser.getTenDangNhap();
        if (username != null && !username.isBlank()) {
            return username;
        }

        String role = currentUser.getTenVaiTro();
        return role == null ? "" : role;
    }

    public static String getCurrentUserRole() {
        return currentUser == null ? null : currentUser.getTenVaiTro();
    }

    public static boolean hasPermission(String permission) {
        if (currentUser == null || permission == null) {
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(currentUser.getTenVaiTro())) {
            return true;
        }

        Set<String> permissions = currentUser.getPermissions();

        if (permissions == null) {
            return false;
        }

        return permissions.contains(permission.toUpperCase());
    }

    public static boolean hasAnyPermission(String... permissions) {
        if (currentUser == null) {
            return false;
        }

        // ADMIN có toàn quyền
        if ("ADMIN".equalsIgnoreCase(currentUser.getTenVaiTro())) {
            return true;
        }

        if (permissions == null) {
            return false;
        }

        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }

        return false;
    }

    public static void clear() {
        token = null;
        currentUser = null;
    }
}
