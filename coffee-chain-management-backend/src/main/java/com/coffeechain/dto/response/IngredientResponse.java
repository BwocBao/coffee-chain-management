package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    description =
        "DTO response IngredientResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class IngredientResponse {
  @Schema(description = "Ma nguyen lieu", example = "1")
  private Long maNguyenLieu;

  @Schema(description = "Ten nguyen lieu", example = "Cà phê hạt Arabica")
  private String tenNguyenLieu;

  @Schema(description = "Ma don vi tinh", example = "1")
  private Long maDonViTinh;

  @Schema(description = "Ten don vi tinh", example = "Tên hiển thị mẫu")
  private String tenDonViTinh;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ky hieu don vi tinh).",
      example = "Gia tri mau")
  private String kyHieuDonViTinh;

  @Schema(description = "Muc ton toi thieu dung de canh bao", example = "100.5")
  private BigDecimal mucTonToiThieu;

  @Schema(description = "Trang thai hien tai cua ban ghi/nghiep vu", example = "ACTIVE")
  private String trangThai;

  public IngredientResponse() {}

  public IngredientResponse(
      Long maNguyenLieu,
      String tenNguyenLieu,
      Long maDonViTinh,
      String tenDonViTinh,
      String kyHieuDonViTinh,
      BigDecimal mucTonToiThieu,
      String trangThai) {
    this.maNguyenLieu = maNguyenLieu;
    this.tenNguyenLieu = tenNguyenLieu;
    this.maDonViTinh = maDonViTinh;
    this.tenDonViTinh = tenDonViTinh;
    this.kyHieuDonViTinh = kyHieuDonViTinh;
    this.mucTonToiThieu = mucTonToiThieu;
    this.trangThai = trangThai;
  }

  public Long getMaNguyenLieu() {
    return maNguyenLieu;
  }

  public void setMaNguyenLieu(Long maNguyenLieu) {
    this.maNguyenLieu = maNguyenLieu;
  }

  public String getTenNguyenLieu() {
    return tenNguyenLieu;
  }

  public void setTenNguyenLieu(String tenNguyenLieu) {
    this.tenNguyenLieu = tenNguyenLieu;
  }

  public Long getMaDonViTinh() {
    return maDonViTinh;
  }

  public void setMaDonViTinh(Long maDonViTinh) {
    this.maDonViTinh = maDonViTinh;
  }

  public String getTenDonViTinh() {
    return tenDonViTinh;
  }

  public void setTenDonViTinh(String tenDonViTinh) {
    this.tenDonViTinh = tenDonViTinh;
  }

  public String getKyHieuDonViTinh() {
    return kyHieuDonViTinh;
  }

  public void setKyHieuDonViTinh(String kyHieuDonViTinh) {
    this.kyHieuDonViTinh = kyHieuDonViTinh;
  }

  public BigDecimal getMucTonToiThieu() {
    return mucTonToiThieu;
  }

  public void setMucTonToiThieu(BigDecimal mucTonToiThieu) {
    this.mucTonToiThieu = mucTonToiThieu;
  }

  public String getTrangThai() {
    return trangThai;
  }

  public void setTrangThai(String trangThai) {
    this.trangThai = trangThai;
  }
}
