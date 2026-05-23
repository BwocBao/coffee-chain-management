package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Lo hang duoc chon thu cong khi dieu chuyen kho")
public class TransferLotSelectionRequest {
    @Schema(description = "Ma lo hang nguon", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long maLoHang;

    @Schema(description = "So luong dieu chuyen tu lo nay", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal soLuongDieuChuyen;

    public Long getMaLoHang() {
        return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
        this.maLoHang = maLoHang;
    }

    public BigDecimal getSoLuongDieuChuyen() {
        return soLuongDieuChuyen;
    }

    public void setSoLuongDieuChuyen(BigDecimal soLuongDieuChuyen) {
        this.soLuongDieuChuyen = soLuongDieuChuyen;
    }
}
