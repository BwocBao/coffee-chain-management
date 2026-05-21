package com.coffeechain.dto.request;

import java.util.LinkedHashSet;
import java.util.Set;

public class UpdateRolePermissionsRequest {
    private Set<Long> permissionIds = new LinkedHashSet<>();
    public Set<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(Set<Long> permissionIds) { this.permissionIds = permissionIds; }
}
