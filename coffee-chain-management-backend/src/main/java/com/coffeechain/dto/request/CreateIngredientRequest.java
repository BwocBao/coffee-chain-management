package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    description =
        "DTO request CreateIngredientRequest. Swagger hien thi cac field frontend can gui len backend.")
public class CreateIngredientRequest {
  @Schema(description = "Ten nguyen lieu", example = "Cà phê hạt Arabica")
  private String tenNguyenLieu;

  @Schema(description = "Ma don vi tinh", example = "1")
  private Long maDonViTinh;

  @Schema(description = "Muc ton toi thieu dung de canh bao", example = "100.5")
  private BigDecimal mucTonToiThieu;

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

  public BigDecimal getMucTonToiThieu() {
    return mucTonToiThieu;
  }

  public void setMucTonToiThieu(BigDecimal mucTonToiThieu) {
    this.mucTonToiThieu = mucTonToiThieu;
  }
}
