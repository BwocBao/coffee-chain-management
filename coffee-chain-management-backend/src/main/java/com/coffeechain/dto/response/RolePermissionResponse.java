package com.coffeechain.dto.response;

import java.util.LinkedHashSet;
import java.util.Set;

public class RolePermissionResponse {
    private Long maVaiTro;
    private String tenVaiTro;
    private Set<Long> permissionIds = new LinkedHashSet<>();
    private Set<String> permissionCodes = new LinkedHashSet<>();

    public RolePermissionResponse() {}
    public RolePermissionResponse(Long maVaiTro, String tenVaiTro, Set<Long> permissionIds, Set<String> permissionCodes) {
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.permissionIds = permissionIds;
        this.permissionCodes = permissionCodes;
    }
    public Long getMaVaiTro() { return maVaiTro; }
    public void setMaVaiTro(Long maVaiTro) { this.maVaiTro = maVaiTro; }
    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }
    public Set<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(Set<Long> permissionIds) { this.permissionIds = permissionIds; }
    public Set<String> getPermissionCodes() { return permissionCodes; }
    public void setPermissionCodes(Set<String> permissionCodes) { this.permissionCodes = permissionCodes; }
}
