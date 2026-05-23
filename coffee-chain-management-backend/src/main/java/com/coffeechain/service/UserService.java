package com.coffeechain.service;

import com.coffeechain.dto.request.CreateUserRequest;
import com.coffeechain.dto.response.CreateUserLookupResponse;
import com.coffeechain.dto.response.CreateUserResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.NguoiDungRepository;
import com.coffeechain.security.PasswordUtil;
import com.coffeechain.security.SessionUser;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private static final String ROLE_ADMIN = "ADMIN";
  private static final String ROLE_QUAN_LY_CHI_NHANH = "QUAN_LY_CHI_NHANH";
  private static final String ROLE_THU_NGAN = "THU_NGAN";

  private final NguoiDungRepository nguoiDungRepository;

  public UserService(NguoiDungRepository nguoiDungRepository) {
    this.nguoiDungRepository = nguoiDungRepository;
  }

  public CreateUserLookupResponse getCreateUserLookups(SessionUser currentUser) {
    /*
     * Flow:
     * 1. Kiểm tra user hiện tại đã đăng nhập chưa.
     * 2. Xác định vai trò hiện tại:
     *    - ADMIN:
     *      + Trả tất cả vai trò.
     *      + Trả tất cả chi nhánh ACTIVE.
     *    - QUAN_LY_CHI_NHANH:
     *      + Chỉ trả vai trò THU_NGAN.
     *      + Chỉ trả chi nhánh của chính quản lý đó.
     *    - Role khác:
     *      + Không được lấy dữ liệu tạo tài khoản.
     * 3. Trả response cho frontend render combobox.
     */

    if (currentUser == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
    }

    String currentRole = normalizeRoleName(currentUser.getTenVaiTro());

    CreateUserLookupResponse response = new CreateUserLookupResponse();

    if (ROLE_ADMIN.equals(currentRole)) {
      response.setRoles(nguoiDungRepository.findRoleOptionsForCreateUser());
      response.setBranches(nguoiDungRepository.findActiveBranchOptions());
      return response;
    }

    if (ROLE_QUAN_LY_CHI_NHANH.equals(currentRole)) {
      Long maChiNhanh = currentUser.getMaChiNhanh();

      if (maChiNhanh == null) {
        throw new AppException(
            HttpStatus.FORBIDDEN, "Tài khoản quản lý chi nhánh chưa được gán chi nhánh");
      }

      response.setRoles(
          nguoiDungRepository
              .findRoleOptionByName(ROLE_THU_NGAN)
              .map(List::of)
              .orElseThrow(
                  () ->
                      new AppException(HttpStatus.BAD_REQUEST, "Không tìm thấy vai trò THU_NGAN")));

      response.setBranches(
          nguoiDungRepository
              .findBranchOptionById(maChiNhanh)
              .map(List::of)
              .orElseThrow(
                  () ->
                      new AppException(
                          HttpStatus.BAD_REQUEST,
                          "Chi nhánh của quản lý không tồn tại hoặc không hoạt động")));

      return response;
    }

    throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền tạo tài khoản");
  }

  @Transactional(rollbackFor = Exception.class)
  public CreateUserResponse createUser(SessionUser currentUser, CreateUserRequest request) {
    /*
     * Transaction: TX_CREATE_USER - Tạo tài khoản người dùng
     *
     * Flow:
     * 1. Kiểm tra người gọi đã đăng nhập chưa.
     * 2. Chuẩn hóa username, email, role.
     * 3. Validate dữ liệu đầu vào.
     * 4. Kiểm tra username/email đã tồn tại chưa.
     * 5. Kiểm tra vai trò cần tạo có tồn tại không.
     * 6. Xác định chi nhánh được gán cho user mới:
     *    - ADMIN:
     *      + Tạo ADMIN thì maChiNhanh = null.
     *      + Tạo role khác thì bắt buộc có maChiNhanh hợp lệ.
     *    - QUAN_LY_CHI_NHANH:
     *      + Chỉ được tạo THU_NGAN.
     *      + maChiNhanh tự lấy từ tài khoản quản lý hiện tại.
     *    - Role khác:
     *      + Không được tạo tài khoản.
     * 7. Hash mật khẩu.
     * 8. Insert NGUOIDUNG với trạng thái ACTIVE.
     * 9. Return response.
     */

    if (currentUser == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
    }

    if (request == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Dữ liệu tạo tài khoản không được để trống");
    }

    String tenDangNhap = normalizeUsername(request.getTenDangNhap());
    String email = normalizeEmail(request.getEmail());
    String matKhau = request.getMatKhau();
    String roleName = normalizeRoleName(request.getTenVaiTro());

    validateCreateUserInput(tenDangNhap, email, matKhau, roleName);

    validateDuplicateUser(tenDangNhap, email);

    Long roleId = findRoleIdOrThrow(roleName);

    Long maChiNhanh = resolveBranchForNewUser(currentUser, roleName, request.getMaChiNhanh());

    String hash = PasswordUtil.hashPassword(matKhau);

    nguoiDungRepository.createUser(tenDangNhap, hash, roleId, maChiNhanh, email);

    return new CreateUserResponse(tenDangNhap, roleName, maChiNhanh);
  }

  private void validateCreateUserInput(
      String tenDangNhap, String email, String matKhau, String roleName) {
    if (isBlank(tenDangNhap)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên đăng nhập");
    }

    if (tenDangNhap.length() < 3 || tenDangNhap.length() > 80) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Tên đăng nhập phải từ 3 đến 80 ký tự");
    }

    if (!tenDangNhap.matches("^[a-zA-Z0-9._-]+$")) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "Tên đăng nhập chỉ được chứa chữ, số, dấu chấm, gạch dưới hoặc gạch ngang");
    }

    if (isBlank(email) || !email.contains("@") || email.length() > 150) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Email không hợp lệ");
    }

    if (matKhau == null || matKhau.length() < 6) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Mật khẩu phải có ít nhất 6 ký tự");
    }

    if (isBlank(roleName)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn vai trò");
    }
  }

  private void validateDuplicateUser(String tenDangNhap, String email) {
    if (nguoiDungRepository.existsUser(tenDangNhap)) {
      throw new AppException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
    }

    if (nguoiDungRepository.existsEmail(email)) {
      throw new AppException(HttpStatus.CONFLICT, "Email đã tồn tại");
    }
  }

  private Long findRoleIdOrThrow(String roleName) {
    Long roleId = nguoiDungRepository.findRoleId(roleName);

    if (roleId == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vai trò không tồn tại: " + roleName);
    }

    return roleId;
  }

  private Long resolveBranchForNewUser(
      SessionUser currentUser, String newUserRole, Long requestedBranchId) {
    String currentRole = normalizeRoleName(currentUser.getTenVaiTro());

    if (ROLE_ADMIN.equals(currentRole)) {
      return resolveBranchWhenAdminCreatesUser(newUserRole, requestedBranchId);
    }

    if (ROLE_QUAN_LY_CHI_NHANH.equals(currentRole)) {
      return resolveBranchWhenBranchManagerCreatesUser(currentUser, newUserRole);
    }

    throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền tạo tài khoản");
  }

  private Long resolveBranchWhenBranchManagerCreatesUser(
      SessionUser currentUser, String newUserRole) {
    if (!ROLE_THU_NGAN.equals(newUserRole)) {
      throw new AppException(
          HttpStatus.FORBIDDEN, "Quản lý chi nhánh chỉ được tạo tài khoản THU_NGAN");
    }

    Long maChiNhanh = currentUser.getMaChiNhanh();

    if (maChiNhanh == null) {
      throw new AppException(
          HttpStatus.FORBIDDEN, "Tài khoản quản lý chi nhánh chưa được gán chi nhánh");
    }

    return maChiNhanh;
  }

  private Long resolveBranchWhenAdminCreatesUser(String newUserRole, Long requestedBranchId) {
    if (ROLE_ADMIN.equals(newUserRole)) {
      return null;
    }

    if (requestedBranchId == null) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Tài khoản không phải ADMIN phải có mã chi nhánh");
    }

    if (!nguoiDungRepository.existsActiveBranch(requestedBranchId)) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Chi nhánh không tồn tại hoặc không hoạt động");
    }

    return requestedBranchId;
  }

  private String normalizeUsername(String username) {
    return username == null ? "" : username.trim();
  }

  private String normalizeRoleName(String roleName) {
    return roleName == null ? "" : roleName.trim().toUpperCase(Locale.ROOT);
  }

  private String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
