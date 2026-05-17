package com.coffeechain.dto;

/**
 * Kết quả API kiểm tra một permission cụ thể.
 * Chủ yếu dùng cho debug hoặc màn nào cần hỏi backend lại về một quyền đơn lẻ.
 */
public class PermissionCheckResponse {
    private String permission;
    private boolean allowed;

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }

}
