package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(
    description =
        "DTO response PermissionGroupResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class PermissionGroupResponse {
  @Schema(
      description = "Gia tri $field trong response tra ve frontend (module).",
      example = "Gia tri mau")
  private String module;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ten module).",
      example = "Tên hiển thị mẫu")
  private String tenModule;

  @Schema(description = "Danh sach ma quyen duoc gan")
  private List<PermissionResponse> permissions = new ArrayList<>();

  public PermissionGroupResponse() {}

  public PermissionGroupResponse(String module, String tenModule) {
    this.module = module;
    this.tenModule = tenModule;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getTenModule() {
    return tenModule;
  }

  public void setTenModule(String tenModule) {
    this.tenModule = tenModule;
  }

  public List<PermissionResponse> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<PermissionResponse> permissions) {
    this.permissions = permissions;
  }
}
