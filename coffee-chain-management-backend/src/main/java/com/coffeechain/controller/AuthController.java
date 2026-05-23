package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.LoginRequest;
import com.coffeechain.dto.response.LoginResponse;
import com.coffeechain.dto.response.PermissionCheckResponse;
import com.coffeechain.dto.response.UserInfoResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.security.TokenStore;
import com.coffeechain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "AUTH - Đăng nhập & phiên làm việc",
    description = "API đăng nhập, đăng xuất, lấy thông tin user hiện tại và kiểm tra quyền.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final AuthGuard authGuard;
  private final TokenStore tokenStore;

  public AuthController(AuthService authService, AuthGuard authGuard, TokenStore tokenStore) {
    this.authService = authService;
    this.authGuard = authGuard;
    this.tokenStore = tokenStore;
  }

  @Operation(
      summary = "Đăng nhập",
      description =
          """
                    Đăng nhập bằng tên đăng nhập và mật khẩu.

                    Nếu đăng nhập thành công, backend trả về:
                    - token
                    - thông tin người dùng
                    - vai trò
                    - chi nhánh
                    - danh sách permissions

                    Frontend cần lưu token và gửi lên các API khác bằng header:
                    Authorization: Bearer <token>
                    """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Thông tin đăng nhập",
      required = true,
      content =
          @Content(
              schema = @Schema(implementation = LoginRequest.class),
              examples =
                  @ExampleObject(
                      name = "Đăng nhập admin",
                      value =
                          """
                                    {
                                      "tenDangNhap": "admin",
                                      "matKhau": "admin123"
                                    }
                                    """)))
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
    @ApiResponse(
        responseCode = "400",
        description = "Thiếu tên đăng nhập hoặc mật khẩu",
        content = @Content),
    @ApiResponse(
        responseCode = "401",
        description = "Sai tên đăng nhập hoặc mật khẩu",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Tài khoản không hoạt động hoặc bị khóa",
        content = @Content)
  })
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
    LoginResponse data = authService.login(request);
    return ResponseEntity.ok(BaseResponse.ok("Đăng nhập thành công", data));
  }

  @Operation(
      summary = "Lấy thông tin người dùng hiện tại",
      description =
          """
                    Trả về thông tin user đang đăng nhập dựa trên token hiện tại.

                    API này thường được frontend gọi sau khi mở app lại để kiểm tra phiên đăng nhập
                    và lấy danh sách quyền hiện tại của user.

                    Yêu cầu header:
                    Authorization: Bearer <token>
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lấy thông tin người dùng thành công"),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content)
  })
  @GetMapping("/me")
  public ResponseEntity<BaseResponse<UserInfoResponse>> me(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader) {
    SessionUser user = authGuard.requireLogin(authHeader);
    UserInfoResponse data = authService.toUserInfo(user);

    return ResponseEntity.ok(BaseResponse.ok("Lấy thông tin người dùng thành công", data));
  }

  @Operation(
      summary = "Lấy danh sách quyền của user hiện tại",
      description =
          """
                    Trả về Set<String> permissions của user đang đăng nhập.

                    Ví dụ:
                    - USER:CREATE
                    - ROLE:VIEW
                    - INVENTORY:IMPORT
                    - ORDER:PAY

                    Frontend dùng danh sách này để hiện/ẩn menu, nút thêm/sửa/xóa,
                    còn backend vẫn phải kiểm tra quyền ở từng API bằng AuthGuard.
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lấy danh sách quyền thành công"),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content)
  })
  @GetMapping("/permissions")
  public ResponseEntity<BaseResponse<Set<String>>> permissions(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader) {
    SessionUser user = authGuard.requireLogin(authHeader);

    return ResponseEntity.ok(
        BaseResponse.ok("Lấy danh sách quyền thành công", user.getPermissions()));
  }

  @Operation(
      summary = "Kiểm tra một quyền cụ thể",
      description =
          """
                    Kiểm tra user hiện tại có một permission cụ thể hay không.

                    Ví dụ:
                    GET /api/auth/check?permission=INVENTORY:IMPORT

                    API này chủ yếu dùng để debug hoặc test nhanh.
                    Frontend thực tế có thể tự kiểm tra bằng danh sách permissions đã nhận khi login.
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Kiểm tra quyền thành công"),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content)
  })
  @GetMapping("/check")
  public ResponseEntity<BaseResponse<PermissionCheckResponse>> checkPermission(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @Parameter(
              description = "Permission cần kiểm tra",
              example = "INVENTORY:IMPORT",
              required = true)
          @RequestParam
          String permission) {
    SessionUser user = authGuard.requireLogin(authHeader);
    boolean allowed = user.hasPermission(permission);

    PermissionCheckResponse data = new PermissionCheckResponse(permission, allowed);

    return ResponseEntity.ok(BaseResponse.ok(allowed ? "Có quyền" : "Không có quyền", data));
  }

  @Operation(
      summary = "Đăng xuất",
      description =
          """
                    Xóa phiên đăng nhập hiện tại khỏi TokenStore.

                    Sau khi gọi API này, token cũ sẽ không dùng được nữa.
                    Frontend nên xóa token local và quay về màn đăng nhập.

                    Yêu cầu header:
                    Authorization: Bearer <token>
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
    @ApiResponse(
        responseCode = "401",
        description = "Thiếu token hoặc token không hợp lệ",
        content = @Content)
  })
  @PostMapping("/logout")
  public ResponseEntity<BaseResponse<Void>> logout(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader) {
    String token = authGuard.extractToken(authHeader);
    tokenStore.removeSession(token);

    return ResponseEntity.ok(BaseResponse.ok("Đăng xuất thành công", null));
  }

  @Operation(
      summary = "Demo kiểm tra quyền nhập kho",
      description =
          """
                    API demo để test phân quyền.

                    Chỉ user có quyền INVENTORY:IMPORT mới gọi thành công.
                    API này có thể xóa sau khi hoàn tất phần phân quyền.
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Có quyền INVENTORY:IMPORT"),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Không có quyền INVENTORY:IMPORT",
        content = @Content)
  })
  @GetMapping("/demo/inventory-import")
  public ResponseEntity<BaseResponse<Void>> demoInventoryImport(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader) {
    authGuard.requirePermission(authHeader, "INVENTORY:IMPORT");

    return ResponseEntity.ok(BaseResponse.ok("Bạn có quyền INVENTORY:IMPORT", null));
  }
}
