package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO request CreateBranchRequest. Swagger hien thi cac field frontend can gui len backend.")
public class CreateBranchRequest {
    @Schema(description = "Ten chi nhanh hien thi tren giao dien", example = "Tên hiển thị mẫu")
    private String tenChiNhanh;
    @Schema(description = "Dia chi lien he/hoat dong", example = "Gia tri mau")
    private String diaChi;
    @Schema(description = "So dien thoai lien he", example = "Gia tri mau")
    private String soDienThoai;

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }
}
