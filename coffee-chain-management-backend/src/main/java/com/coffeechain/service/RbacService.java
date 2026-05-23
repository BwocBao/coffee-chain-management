package com.coffeechain.service;

import com.coffeechain.dto.request.CreateRoleRequest;
import com.coffeechain.dto.request.UpdateRolePermissionsRequest;
import com.coffeechain.dto.response.PermissionGroupResponse;
import com.coffeechain.dto.response.PermissionResponse;
import com.coffeechain.dto.response.RolePermissionResponse;
import com.coffeechain.dto.response.RoleResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.RbacRepository;
import java.text.Normalizer;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RbacService {
  private final RbacRepository rbacRepository;

  public RbacService(RbacRepository rbacRepository) {
    this.rbacRepository = rbacRepository;
  }

  public List<RoleResponse> findAllRoles() {
    return rbacRepository.findAllRoles();
  }

  @Transactional
  public RoleResponse createRole(CreateRoleRequest request) {
    if (request == null
        || request.getTenVaiTro() == null
        || request.getTenVaiTro().trim().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Tên vai trò không được để trống");
    }
    String tenVaiTro = normalizeRoleCode(request.getTenVaiTro());
    if ("ADMIN".equals(tenVaiTro))
      throw new AppException(HttpStatus.BAD_REQUEST, "Không được tạo trùng vai trò ADMIN");
    if (rbacRepository.findRoleByName(tenVaiTro).isPresent())
      throw new AppException(HttpStatus.CONFLICT, "Vai trò đã tồn tại: " + tenVaiTro);
    Long id = rbacRepository.createRole(tenVaiTro);
    return new RoleResponse(id, tenVaiTro);
  }

  public List<PermissionGroupResponse> findAllPermissionGroups() {
    Map<String, PermissionGroupResponse> groups = new LinkedHashMap<>();
    for (PermissionResponse permission : rbacRepository.findAllPermissions()) {
      groups
          .computeIfAbsent(
              permission.getModule(),
              key -> new PermissionGroupResponse(key, moduleDisplayName(key)))
          .getPermissions()
          .add(permission);
    }
    return new ArrayList<>(groups.values());
  }

  public RolePermissionResponse findRolePermissions(Long maVaiTro) {
    RoleResponse role = findExistingRole(maVaiTro);
    return new RolePermissionResponse(
        role.getMaVaiTro(),
        role.getTenVaiTro(),
        rbacRepository.findPermissionIdsByRoleId(maVaiTro),
        rbacRepository.findPermissionCodesByRoleId(maVaiTro));
  }

  @Transactional
  public RolePermissionResponse updateRolePermissions(
      Long maVaiTro, UpdateRolePermissionsRequest request) {
    RoleResponse role = findExistingRole(maVaiTro);
    if ("ADMIN".equalsIgnoreCase(role.getTenVaiTro())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Không được sửa quyền của vai trò ADMIN để tránh khóa hệ thống");
    }
    Set<Long> permissionIds =
        request == null || request.getPermissionIds() == null
            ? new LinkedHashSet<>()
            : new LinkedHashSet<>(request.getPermissionIds());
    if (!permissionIds.isEmpty()
        && rbacRepository.countPermissionsByIds(permissionIds) != permissionIds.size()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Danh sách quyền có mã quyền không tồn tại");
    }
    rbacRepository.replaceRolePermissions(maVaiTro, permissionIds);
    return findRolePermissions(maVaiTro);
  }

  private RoleResponse findExistingRole(Long maVaiTro) {
    if (maVaiTro == null) throw new AppException(HttpStatus.BAD_REQUEST, "Thiếu mã vai trò");
    return rbacRepository
        .findRoleById(maVaiTro)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy vai trò"));
  }

  private String normalizeRoleCode(String value) {
    String noAccent =
        Normalizer.normalize(value.trim().toUpperCase(Locale.ROOT), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
    return noAccent.replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+", "").replaceAll("_+$", "");
  }

  private String moduleDisplayName(String module) {
    return switch (module) {
      case "USER" -> "Người dùng";
      case "ROLE" -> "Vai trò & phân quyền";
      case "BRANCH" -> "Chi nhánh";
      case "PRODUCT" -> "Sản phẩm / công thức";
      case "INGREDIENT" -> "Nguyên liệu";
      case "SUPPLIER" -> "Nhà cung cấp";
      case "INVENTORY" -> "Kho";
      case "STOCKTAKE" -> "Kiểm kho";
      case "WASTAGE" -> "Hao hụt";
      case "ORDER" -> "Đơn hàng / POS";
      case "REPORT" -> "Báo cáo";
      default -> module;
    };
  }
}
