package com.coffeechain.config;

import com.coffeechain.repository.NguoiDungRepository;
import com.coffeechain.security.PasswordUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
  private final NguoiDungRepository nguoiDungRepository;

  @Value("${app.auth.default-admin-username:admin}")
  private String defaultAdminUsername;

  @Value("${app.auth.default-admin-password:admin123}")
  private String defaultAdminPassword;

  @Value("${app.auth.default-admin-email:admin@phungloc.local}")
  private String defaultAdminEmail;

  public DataInitializer(NguoiDungRepository nguoiDungRepository) {
    this.nguoiDungRepository = nguoiDungRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    nguoiDungRepository.ensurePermissionActionConstraintAllowsManage();
    Long adminRoleId = nguoiDungRepository.getOrCreateRole("ADMIN");
    Long quanLyKhoRoleId = nguoiDungRepository.getOrCreateRole("QUAN_LY_KHO");
    Long quanLyChiNhanhRoleId = nguoiDungRepository.getOrCreateRole("QUAN_LY_CHI_NHANH");
    Long thuNganRoleId = nguoiDungRepository.getOrCreateRole("THU_NGAN");

    nguoiDungRepository.deletePermission("STOCKTAKE", "ADJUST");
    nguoiDungRepository.deletePermission("STOCKTAKE", "CANCEL");
    nguoiDungRepository.deletePermission("STOCKTAKE", "COMPLETE");
    nguoiDungRepository.deletePermission("STOCKTAKE", "CREATE");
    nguoiDungRepository.deletePermission("STOCKTAKE", "UPDATE");
    nguoiDungRepository.deletePermission("STOCKTAKE", "DELETE");
    nguoiDungRepository.deletePermission("ORDER", "REFUND");
    nguoiDungRepository.deletePermission("PRODUCT", "CREATE");
    nguoiDungRepository.deletePermission("PRODUCT", "UPDATE");
    nguoiDungRepository.deletePermission("PRODUCT", "DELETE");
    nguoiDungRepository.deletePermission("USER", "UPDATE");
    nguoiDungRepository.deletePermission("USER", "DELETE");
    nguoiDungRepository.deletePermission("WASTAGE", "UPDATE");
    Map<String, List<String>> seed = new LinkedHashMap<>();
    seed.put("USER", List.of("VIEW", "CREATE"));
    seed.put("ROLE", List.of("VIEW", "CREATE", "UPDATE", "DELETE"));
    seed.put("BRANCH", List.of("VIEW", "CREATE", "UPDATE"));
    seed.put("PRODUCT", List.of("VIEW"));
    seed.put("INGREDIENT", List.of("VIEW", "CREATE", "UPDATE", "DELETE"));
    seed.put("SUPPLIER", List.of("VIEW", "CREATE", "UPDATE", "DELETE"));
    seed.put("INVENTORY", List.of("VIEW", "IMPORT", "EXPORT", "TRANSFER", "ADJUST"));
    seed.put("STOCKTAKE", List.of("VIEW", "MANAGE"));
    seed.put("WASTAGE", List.of("VIEW", "CREATE"));
    seed.put("ORDER", List.of("VIEW", "CREATE", "PAY", "CANCEL"));
    seed.put("REPORT", List.of("VIEW"));
    seed.put("UNIT", List.of("VIEW", "CREATE", "UPDATE", "DELETE"));
    seed.put("WAREHOUSE", List.of("VIEW", "CREATE", "UPDATE", "DELETE"));
    seed.put("RECIPE", List.of("VIEW", "MANAGE"));

    for (Map.Entry<String, List<String>> entry : seed.entrySet()) {
      Long maChucNang = nguoiDungRepository.getOrCreateChucNang(entry.getKey());
      for (String action : entry.getValue()) {
        Long maQuyen =
            nguoiDungRepository.getOrCreatePermission(
                maChucNang, action, permissionDisplayName(entry.getKey(), action));
        nguoiDungRepository.assignPermissionToRole(adminRoleId, maQuyen);
      }
    }

    nguoiDungRepository.removePermissionFromRole("QUAN_LY_KHO", "PRODUCT", "VIEW");
    nguoiDungRepository.removePermissionFromRole("QUAN_LY_KHO", "ORDER", "VIEW");
    nguoiDungRepository.removePermissionFromRole("QUAN_LY_KHO", "REPORT", "VIEW");
    nguoiDungRepository.removePermissionFromRole("QUAN_LY_CHI_NHANH", "REPORT", "VIEW");
    nguoiDungRepository.removePermissionFromRole("THU_NGAN", "REPORT", "VIEW");

    assign(quanLyKhoRoleId, "INVENTORY", "VIEW", "IMPORT", "EXPORT", "TRANSFER", "ADJUST");
    assign(quanLyKhoRoleId, "STOCKTAKE", "VIEW", "MANAGE");
    assign(quanLyKhoRoleId, "WASTAGE", "VIEW", "CREATE");
    assign(quanLyKhoRoleId, "SUPPLIER", "VIEW", "CREATE", "UPDATE", "DELETE");
    assign(quanLyKhoRoleId, "INGREDIENT", "VIEW", "CREATE", "UPDATE", "DELETE");
    assign(quanLyKhoRoleId, "BRANCH", "VIEW");
    assign(quanLyKhoRoleId, "UNIT", "VIEW", "CREATE", "UPDATE", "DELETE");
    assign(quanLyKhoRoleId, "WAREHOUSE", "VIEW", "CREATE", "UPDATE", "DELETE");

    assign(quanLyChiNhanhRoleId, "USER", "VIEW", "CREATE");
    assign(quanLyChiNhanhRoleId, "BRANCH", "VIEW");
    assign(quanLyChiNhanhRoleId, "INVENTORY", "VIEW");
    assign(quanLyChiNhanhRoleId, "STOCKTAKE", "VIEW", "MANAGE");
    assign(quanLyChiNhanhRoleId, "WASTAGE", "VIEW", "CREATE");
    assign(quanLyChiNhanhRoleId, "ORDER", "VIEW", "CREATE", "PAY", "CANCEL");
    assign(quanLyChiNhanhRoleId, "INGREDIENT", "VIEW");
    assign(quanLyChiNhanhRoleId, "PRODUCT", "VIEW");
    assign(quanLyChiNhanhRoleId, "UNIT", "VIEW");
    assign(quanLyChiNhanhRoleId, "RECIPE", "VIEW");

    assign(thuNganRoleId, "ORDER", "VIEW", "CREATE", "PAY", "CANCEL");
    assign(thuNganRoleId, "PRODUCT", "VIEW");

    if (!nguoiDungRepository.existsUser(defaultAdminUsername)) {
      String hash = PasswordUtil.hashPassword(defaultAdminPassword);
      nguoiDungRepository.createUser(
          defaultAdminUsername, hash, adminRoleId, null, defaultAdminEmail);
      System.out.println(
          "Đã tạo user mặc định: " + defaultAdminUsername + " / " + defaultAdminPassword);
    }
  }

  private void assign(Long roleId, String module, String... actions) {
    for (String action : actions) {
      Long permissionId = nguoiDungRepository.findPermissionId(module, action);
      if (permissionId != null) {
        nguoiDungRepository.assignPermissionToRole(roleId, permissionId);
      }
    }
  }

  private String permissionDisplayName(String module, String action) {
    return switch (module + ":" + action) {
      case "USER:VIEW" -> "Xem người dùng";
      case "USER:CREATE" -> "Tạo người dùng";

      case "ROLE:VIEW" -> "Xem vai trò";
      case "ROLE:CREATE" -> "Tạo vai trò";
      case "ROLE:UPDATE" -> "Sửa vai trò";
      case "ROLE:DELETE" -> "Xóa vai trò";

      case "BRANCH:VIEW" -> "Xem chi nhánh";
      case "BRANCH:CREATE" -> "Tạo chi nhánh";
      case "BRANCH:UPDATE" -> "Sửa chi nhánh";

      case "PRODUCT:VIEW" -> "Xem sản phẩm";

      case "INGREDIENT:VIEW" -> "Xem nguyên liệu";
      case "INGREDIENT:CREATE" -> "Tạo nguyên liệu";
      case "INGREDIENT:UPDATE" -> "Sửa nguyên liệu";
      case "INGREDIENT:DELETE" -> "Xóa nguyên liệu";

      case "SUPPLIER:VIEW" -> "Xem nhà cung cấp";
      case "SUPPLIER:CREATE" -> "Tạo nhà cung cấp";
      case "SUPPLIER:UPDATE" -> "Sửa nhà cung cấp";
      case "SUPPLIER:DELETE" -> "Xóa nhà cung cấp";

      case "INVENTORY:VIEW" -> "Xem tồn kho";
      case "INVENTORY:IMPORT" -> "Nhập kho";
      case "INVENTORY:EXPORT" -> "Xuất kho";
      case "INVENTORY:TRANSFER" -> "Điều chuyển kho";
      case "INVENTORY:ADJUST" -> "Điều chỉnh tồn kho";

      case "STOCKTAKE:VIEW" -> "Xem kiểm kho";
      case "STOCKTAKE:MANAGE" -> "Quản lý phiếu kiểm kho";

      case "WASTAGE:VIEW" -> "Xem hao hụt";
      case "WASTAGE:CREATE" -> "Báo hao hụt";

      case "ORDER:VIEW" -> "Xem đơn hàng";
      case "ORDER:CREATE" -> "Tạo đơn hàng";
      case "ORDER:PAY" -> "Thanh toán đơn";
      case "ORDER:CANCEL" -> "Hủy đơn hàng";

      case "REPORT:VIEW" -> "Xem bao cao thong ke";


      case "UNIT:VIEW" -> "Xem đơn vị tính";
      case "UNIT:CREATE" -> "Tạo đơn vị tính";
      case "UNIT:UPDATE" -> "Sửa đơn vị tính";
      case "UNIT:DELETE" -> "Xóa đơn vị tính";

      case "WAREHOUSE:VIEW" -> "Xem kho";
      case "WAREHOUSE:CREATE" -> "Tạo kho";
      case "WAREHOUSE:UPDATE" -> "Sửa kho";
      case "WAREHOUSE:DELETE" -> "Ngưng hoạt động kho";

      case "RECIPE:VIEW" -> "Xem công thức";
      case "RECIPE:MANAGE" -> "Quản lý công thức";

      default -> module + ":" + action;
    };
  }
}
