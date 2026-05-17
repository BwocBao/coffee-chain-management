package com.coffeechain.dto;

import java.util.ArrayList;
import java.util.List;

public class PermissionGroupResponse {
    private String module;
    private String tenModule;
    private List<PermissionResponse> permissions = new ArrayList<>();

    public PermissionGroupResponse() {}
    public PermissionGroupResponse(String module, String tenModule) {
        this.module = module;
        this.tenModule = tenModule;
    }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getTenModule() { return tenModule; }
    public void setTenModule(String tenModule) { this.tenModule = tenModule; }
    public List<PermissionResponse> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionResponse> permissions) { this.permissions = permissions; }
}
