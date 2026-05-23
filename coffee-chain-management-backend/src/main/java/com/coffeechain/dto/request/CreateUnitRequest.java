package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO request CreateUnitRequest. Swagger hien thi cac field frontend can gui len backend.")
public class CreateUnitRequest {
    @Schema(description = "Ten don vi tinh", example = "Tên hiển thị mẫu")
    private String tenDonViTinh;
    @Schema(description = "Ky hieu don vi tinh", example = "Gia tri mau")
    private String kyHieu;

    public String getTenDonViTinh() {
        return tenDonViTinh;
    }

    public void setTenDonViTinh(String tenDonViTinh) {
        this.tenDonViTinh = tenDonViTinh;
    }

    public String getKyHieu() {
        return kyHieu;
    }

    public void setKyHieu(String kyHieu) {
        this.kyHieu = kyHieu;
    }
}
