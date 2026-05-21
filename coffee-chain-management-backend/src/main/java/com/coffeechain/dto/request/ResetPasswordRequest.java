package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request dat lai mat khau bang ma xac nhan")
public class ResetPasswordRequest {
    @Schema(description = "Email cua tai khoan", example = "admin@phungloc.local", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Ma xac nhan 6 chu so", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Mat khau moi, toi thieu 6 ky tu", example = "newpass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
