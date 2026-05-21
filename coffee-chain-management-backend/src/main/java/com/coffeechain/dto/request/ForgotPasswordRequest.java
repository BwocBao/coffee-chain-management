package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request yeu cau gui ma xac nhan quen mat khau")
public class ForgotPasswordRequest {
    @Schema(description = "Email cua tai khoan can khoi phuc", example = "admin@phungloc.local", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
