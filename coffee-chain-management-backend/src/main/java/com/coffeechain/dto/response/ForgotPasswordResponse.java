package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Ket qua yeu cau quen mat khau")
public class ForgotPasswordResponse {
    @Schema(description = "Email da duoc che bot de hien thi an toan", example = "a***@phungloc.local")
    private String maskedEmail;

    @Schema(description = "Thoi diem ma xac nhan het han")
    private LocalDateTime expiresAt;

    @Schema(description = "Ma xac nhan chi tra ve khi app.auth.password-reset.return-code-in-response=true")
    private String debugCode;

    public ForgotPasswordResponse() {
    }

    public ForgotPasswordResponse(String maskedEmail, LocalDateTime expiresAt, String debugCode) {
        this.maskedEmail = maskedEmail;
        this.expiresAt = expiresAt;
        this.debugCode = debugCode;
    }

    public String getMaskedEmail() {
        return maskedEmail;
    }

    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getDebugCode() {
        return debugCode;
    }

    public void setDebugCode(String debugCode) {
        this.debugCode = debugCode;
    }
}
