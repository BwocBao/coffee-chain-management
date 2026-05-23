package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Mot dong nguyen lieu trong phieu nhap kho")
public class CreateImportReceiptItemRequest {
  @Schema(
      description = "Ma nguyen lieu can nhap",
      example = "1",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long maNguyenLieu;

  @Schema(
      description = "So luong nhap",
      example = "1000",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal soLuongNhap;

  @Schema(
      description = "Don gia nhap",
      example = "120000",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal donGiaNhap;

  @Schema(description = "So lo cua nguyen lieu", example = "LOT-CAFE-001")
  private String soLo;

  @Schema(description = "Han su dung cua lo hang", example = "2027-12-31")
  private LocalDate hanSuDung;

  public Long getMaNguyenLieu() {
    return maNguyenLieu;
  }

  public void setMaNguyenLieu(Long maNguyenLieu) {
    this.maNguyenLieu = maNguyenLieu;
  }

  public BigDecimal getSoLuongNhap() {
    return soLuongNhap;
  }

  public void setSoLuongNhap(BigDecimal soLuongNhap) {
    this.soLuongNhap = soLuongNhap;
  }

  public BigDecimal getDonGiaNhap() {
    return donGiaNhap;
  }

  public void setDonGiaNhap(BigDecimal donGiaNhap) {
    this.donGiaNhap = donGiaNhap;
  }

  public String getSoLo() {
    return soLo;
  }

  public void setSoLo(String soLo) {
    this.soLo = soLo;
  }

  public LocalDate getHanSuDung() {
    return hanSuDung;
  }

  public void setHanSuDung(LocalDate hanSuDung) {
    this.hanSuDung = hanSuDung;
  }
}
