package com.coffeechain.dto;

public class PermissionCheckResponse {
    private String permission;
    private boolean allowed;

    public PermissionCheckResponse() {
    }

    public PermissionCheckResponse(String permission, boolean allowed) {
        this.permission = permission;
        this.allowed = allowed;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
}