package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Mot dong nguyen lieu trong phieu dieu chuyen kho")
public class CreateTransferReceiptItemRequest {
  @Schema(
      description = "Ma nguyen lieu can dieu chuyen",
      example = "2",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long maNguyenLieu;

  @Schema(
      description = "Tong so luong dieu chuyen",
      example = "300",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal soLuongDieuChuyen;

  @Schema(description = "Danh sach lo thu cong. Chi can gui khi chonLoThuCong = true")
  private List<TransferLotSelectionRequest> loHangDieuChuyen = new ArrayList<>();

  public Long getMaNguyenLieu() {
    return maNguyenLieu;
  }

  public void setMaNguyenLieu(Long maNguyenLieu) {
    this.maNguyenLieu = maNguyenLieu;
  }

  public BigDecimal getSoLuongDieuChuyen() {
    return soLuongDieuChuyen;
  }

  public void setSoLuongDieuChuyen(BigDecimal soLuongDieuChuyen) {
    this.soLuongDieuChuyen = soLuongDieuChuyen;
  }

  public List<TransferLotSelectionRequest> getLoHangDieuChuyen() {
    return loHangDieuChuyen;
  }

  public void setLoHangDieuChuyen(List<TransferLotSelectionRequest> loHangDieuChuyen) {
    this.loHangDieuChuyen = loHangDieuChuyen;
  }
}
