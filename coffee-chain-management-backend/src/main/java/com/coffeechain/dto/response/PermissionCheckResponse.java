package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO response PermissionCheckResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class PermissionCheckResponse {
    @Schema(description = "Gia tri $field trong response tra ve frontend (permission).", example = "Gia tri mau")
    private String permission;
    @Schema(description = "Gia tri $field trong response tra ve frontend (allowed).", example = "true")
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
