package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "DTO response RoleResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class RoleResponse {
  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ma vai tro).",
      example = "1")
  private Long maVaiTro;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ten vai tro).",
      example = "Tên hiển thị mẫu")
  private String tenVaiTro;

  public RoleResponse() {}

  public RoleResponse(Long maVaiTro, String tenVaiTro) {
    this.maVaiTro = maVaiTro;
    this.tenVaiTro = tenVaiTro;
  }

  public Long getMaVaiTro() {
    return maVaiTro;
  }

  public void setMaVaiTro(Long maVaiTro) {
    this.maVaiTro = maVaiTro;
  }

  public String getTenVaiTro() {
    return tenVaiTro;
  }

  public void setTenVaiTro(String tenVaiTro) {
    this.tenVaiTro = tenVaiTro;
  }
}
