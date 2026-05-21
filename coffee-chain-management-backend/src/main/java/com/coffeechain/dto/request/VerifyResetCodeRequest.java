package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request kiem tra ma xac nhan quen mat khau")
public class VerifyResetCodeRequest {
    @Schema(description = "Email cua tai khoan", example = "admin@phungloc.local", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Ma xac nhan 6 chu so", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

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
}
