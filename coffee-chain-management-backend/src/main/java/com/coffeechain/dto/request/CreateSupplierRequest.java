package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO request CreateSupplierRequest. Swagger hien thi cac field frontend can gui len backend.")
public class CreateSupplierRequest {
    @Schema(description = "Ten nha cung cap", example = "Tên hiển thị mẫu")
    private String tenNhaCungCap;
    @Schema(description = "So dien thoai lien he", example = "Gia tri mau")
    private String soDienThoai;
    @Schema(description = "Email lien he hoac tai khoan", example = "admin@phungloc.local")
    private String email;
    @Schema(description = "Dia chi lien he/hoat dong", example = "Gia tri mau")
    private String diaChi;

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}
