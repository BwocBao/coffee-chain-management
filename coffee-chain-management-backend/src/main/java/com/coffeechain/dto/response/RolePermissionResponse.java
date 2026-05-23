package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import java.util.Set;

@Schema(
    description =
        "DTO response RolePermissionResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class RolePermissionResponse {
  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ma vai tro).",
      example = "1")
  private Long maVaiTro;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ten vai tro).",
      example = "Tên hiển thị mẫu")
  private String tenVaiTro;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (permission ids).",
      example = "1")
  private Set<Long> permissionIds = new LinkedHashSet<>();

  @Schema(description = "Gia tri $field trong response tra ve frontend (permission codes).")
  private Set<String> permissionCodes = new LinkedHashSet<>();

  public RolePermissionResponse() {}

  public RolePermissionResponse(
      Long maVaiTro, String tenVaiTro, Set<Long> permissionIds, Set<String> permissionCodes) {
    this.maVaiTro = maVaiTro;
    this.tenVaiTro = tenVaiTro;
    this.permissionIds = permissionIds;
    this.permissionCodes = permissionCodes;
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

  public Set<Long> getPermissionIds() {
    return permissionIds;
  }

  public void setPermissionIds(Set<Long> permissionIds) {
    this.permissionIds = permissionIds;
  }

  public Set<String> getPermissionCodes() {
    return permissionCodes;
  }

  public void setPermissionCodes(Set<String> permissionCodes) {
    this.permissionCodes = permissionCodes;
  }
}
