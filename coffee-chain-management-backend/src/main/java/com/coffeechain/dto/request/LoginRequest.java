package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request đăng nhập hệ thống")
public class LoginRequest {
    @Schema(description = "Tên đăng nhập", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenDangNhap;

    @Schema(description = "Mật khẩu", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String matKhau;

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
}
