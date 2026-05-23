package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO request CreateWarehouseRequest. Swagger hien thi cac field frontend can gui len backend.")
public class CreateWarehouseRequest {
    @Schema(description = "Ten kho hien thi tren giao dien", example = "Kho tổng Phụng Lộc")
    private String tenKho;
    @Schema(description = "Loai kho: CENTRAL hoac BRANCH", example = "Gia tri mau")
    private String loaiKho;
    @Schema(description = "Ma chi nhanh lien quan", example = "1")
    private Long maChiNhanh;

    public String getTenKho() {
        return tenKho;
    }

    public void setTenKho(String tenKho) {
        this.tenKho = tenKho;
    }

    public String getLoaiKho() {
        return loaiKho;
    }

    public void setLoaiKho(String loaiKho) {
        this.loaiKho = loaiKho;
    }

    public Long getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Long maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
    }
}
