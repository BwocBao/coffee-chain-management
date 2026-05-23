package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "DTO request UpdateIngredientStatusRequest. Swagger hien thi cac field frontend can gui len backend.")
public class UpdateIngredientStatusRequest {
  @Schema(description = "Trang thai hien tai cua ban ghi/nghiep vu", example = "ACTIVE")
  private String trangThai;

  public String getTrangThai() {
    return trangThai;
  }

  public void setTrangThai(String trangThai) {
    this.trangThai = trangThai;
  }
}
