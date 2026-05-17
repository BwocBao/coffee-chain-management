package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Lo hang duoc chon thu cong khi xuat kho")
public class ExportLotSelectionRequest {
    @Schema(description = "Ma lo hang", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long maLoHang;

    @Schema(description = "So luong xuat tu lo nay", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal soLuongXuat;

    public Long getMaLoHang() {
        return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
        this.maLoHang = maLoHang;
    }

    public BigDecimal getSoLuongXuat() {
        return soLuongXuat;
    }

    public void setSoLuongXuat(BigDecimal soLuongXuat) {
        this.soLuongXuat = soLuongXuat;
    }
}
