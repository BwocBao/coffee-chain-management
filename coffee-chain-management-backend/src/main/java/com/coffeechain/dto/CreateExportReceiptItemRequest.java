package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Mot dong nguyen lieu trong phieu xuat kho")
public class CreateExportReceiptItemRequest {
    @Schema(description = "Ma nguyen lieu can xuat", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long maNguyenLieu;

    @Schema(description = "Tong so luong xuat", example = "300", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal soLuongXuat;

    @Schema(description = "Don gia xuat de tinh tong gia tri. Co the de 0 neu khong can gia tri", example = "95000")
    private BigDecimal donGiaXuat = BigDecimal.ZERO;

    @Schema(description = "Danh sach lo thu cong. Chi can gui khi chonLoThuCong = true")
    private List<ExportLotSelectionRequest> loHangXuat = new ArrayList<>();

    public Long getMaNguyenLieu() {
        return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
        this.maNguyenLieu = maNguyenLieu;
    }

    public BigDecimal getSoLuongXuat() {
        return soLuongXuat;
    }

    public void setSoLuongXuat(BigDecimal soLuongXuat) {
        this.soLuongXuat = soLuongXuat;
    }

    public BigDecimal getDonGiaXuat() {
        return donGiaXuat;
    }

    public void setDonGiaXuat(BigDecimal donGiaXuat) {
        this.donGiaXuat = donGiaXuat;
    }

    public List<ExportLotSelectionRequest> getLoHangXuat() {
        return loHangXuat;
    }

    public void setLoHangXuat(List<ExportLotSelectionRequest> loHangXuat) {
        this.loHangXuat = loHangXuat;
    }
}
