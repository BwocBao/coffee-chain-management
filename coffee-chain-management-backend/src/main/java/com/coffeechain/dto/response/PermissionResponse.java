package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "DTO response PermissionResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class PermissionResponse {
  @Schema(description = "Gia tri $field trong response tra ve frontend (ma quyen).", example = "1")
  private Long maQuyen;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ma chuc nang).",
      example = "1")
  private Long maChucNang;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (module).",
      example = "Gia tri mau")
  private String module;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (action).",
      example = "Gia tri mau")
  private String action;

  @Schema(description = "Ma xac nhan", example = "Gia tri mau")
  private String code;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ten quyen).",
      example = "Tên hiển thị mẫu")
  private String tenQuyen;

  public PermissionResponse() {}

  public PermissionResponse(
      Long maQuyen, Long maChucNang, String module, String action, String tenQuyen) {
    this.maQuyen = maQuyen;
    this.maChucNang = maChucNang;
    this.module = module;
    this.action = action;
    this.code = module + ":" + action;
    this.tenQuyen = tenQuyen;
  }

  public Long getMaQuyen() {
    return maQuyen;
  }

  public void setMaQuyen(Long maQuyen) {
    this.maQuyen = maQuyen;
  }

  public Long getMaChucNang() {
    return maChucNang;
  }

  public void setMaChucNang(Long maChucNang) {
    this.maChucNang = maChucNang;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
    refreshCode();
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
    refreshCode();
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getTenQuyen() {
    return tenQuyen;
  }

  public void setTenQuyen(String tenQuyen) {
    this.tenQuyen = tenQuyen;
  }

  private void refreshCode() {
    if (module != null && action != null) this.code = module + ":" + action;
  }
}
