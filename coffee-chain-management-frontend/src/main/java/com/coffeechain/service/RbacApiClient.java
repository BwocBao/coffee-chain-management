package com.coffeechain.service;

import com.coffeechain.config.ApiConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Client gọi nhóm API phân quyền RBAC. Được dùng bởi màn PhanQuyenBaoMatFrame để tải role, tải
 * permission và lưu quyền cho role.
 */
public class RbacApiClient {
  private static final String BASE_URL = ApiConfig.BASE_URL + "/api";

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<RoleDto> getRoles() throws Exception {
    JsonNode data = sendGet("/rbac/roles");
    return objectMapper.convertValue(data, new TypeReference<List<RoleDto>>() {});
  }

  public List<PermissionGroupDto> getPermissionGroups() throws Exception {
    JsonNode data = sendGet("/rbac/permissions");
    return objectMapper.convertValue(data, new TypeReference<List<PermissionGroupDto>>() {});
  }

  public RolePermissionDto getRolePermissions(Long roleId) throws Exception {
    JsonNode data = sendGet("/rbac/roles/" + roleId + "/permissions");
    return objectMapper.convertValue(data, RolePermissionDto.class);
  }

  public RolePermissionDto updateRolePermissions(Long roleId, Set<Long> permissionIds)
      throws Exception {
    String body = objectMapper.writeValueAsString(new UpdateRolePermissionsRequest(permissionIds));
    JsonNode data = sendJson("PUT", "/rbac/roles/" + roleId + "/permissions", body);
    return objectMapper.convertValue(data, RolePermissionDto.class);
  }

  public RoleDto createRole(String tenVaiTro) throws Exception {
    String body = objectMapper.writeValueAsString(new CreateRoleRequest(tenVaiTro));
    JsonNode data = sendJson("POST", "/rbac/roles", body);
    return objectMapper.convertValue(data, RoleDto.class);
  }

  private JsonNode sendGet(String path) throws Exception {
    HttpRequest request = baseRequest(path).GET().build();
    return send(request);
  }

  private JsonNode sendJson(String method, String path, String body) throws Exception {
    HttpRequest request =
        baseRequest(path)
            .method(method, HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/json")
            .build();
    return send(request);
  }

  private HttpRequest.Builder baseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + path))
        .header("Accept", "application/json")
        .header("Authorization", "Bearer " + SessionManager.getToken());
  }

  private JsonNode send(HttpRequest request) throws IOException, InterruptedException {
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    JsonNode root = objectMapper.readTree(response.body());
    boolean success = root.path("success").asBoolean(false);
    if (response.statusCode() < 200 || response.statusCode() >= 300 || !success) {
      throw new RuntimeException(root.path("message").asText("Lỗi gọi API phân quyền"));
    }
    return root.path("data");
  }

  public static class RoleDto {
    private Long maVaiTro;
    private String tenVaiTro;

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

    @Override
    public String toString() {
      return tenVaiTro;
    }
  }

  public static class PermissionGroupDto {
    private String module;
    private String tenModule;
    private List<PermissionDto> permissions = new ArrayList<>();

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

    public List<PermissionDto> getPermissions() {
      return permissions;
    }

    public void setPermissions(List<PermissionDto> permissions) {
      this.permissions = permissions;
    }
  }

  public static class PermissionDto {
    private Long maQuyen;
    private Long maChucNang;
    private String module;
    private String action;
    private String code;
    private String tenQuyen;

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
    }

    public String getAction() {
      return action;
    }

    public void setAction(String action) {
      this.action = action;
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
  }

  public static class RolePermissionDto {
    private Long maVaiTro;
    private String tenVaiTro;
    private Set<Long> permissionIds = new LinkedHashSet<>();
    private Set<String> permissionCodes = new LinkedHashSet<>();

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

  private record UpdateRolePermissionsRequest(Set<Long> permissionIds) {}

  private record CreateRoleRequest(String tenVaiTro) {}
}
