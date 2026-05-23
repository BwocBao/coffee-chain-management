package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "DTO response UnitResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class UnitResponse {
  @Schema(description = "Ma don vi tinh", example = "1")
  private Long maDonViTinh;

  @Schema(description = "Ten don vi tinh", example = "Tên hiển thị mẫu")
  private String tenDonViTinh;

  @Schema(description = "Ky hieu don vi tinh", example = "Gia tri mau")
  private String kyHieu;

  public UnitResponse() {}

  public UnitResponse(Long maDonViTinh, String tenDonViTinh, String kyHieu) {
    this.maDonViTinh = maDonViTinh;
    this.tenDonViTinh = tenDonViTinh;
    this.kyHieu = kyHieu;
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

  public String getKyHieu() {
    return kyHieu;
  }

  public void setKyHieu(String kyHieu) {
    this.kyHieu = kyHieu;
  }
}
