package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.LinkedHashSet;
import java.util.Set;

@Schema(description = "DTO request UpdateRolePermissionsRequest. Swagger hien thi cac field frontend can gui len backend.")
public class UpdateRolePermissionsRequest {
    @Schema(description = "Gia tri $field trong request gui len backend (permission ids).", example = "1")
    private Set<Long> permissionIds = new LinkedHashSet<>();
    public Set<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(Set<Long> permissionIds) { this.permissionIds = permissionIds; }
}
