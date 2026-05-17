package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request tạo vai trò mới")
public class CreateRoleRequest {
    @Schema(description = "Tên vai trò, nên viết hoa không dấu và dùng gạch dưới", example = "CA_TRUONG", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenVaiTro;
    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }
}
