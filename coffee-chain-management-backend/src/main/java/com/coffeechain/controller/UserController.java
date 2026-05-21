package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.response.CreateUserLookupResponse;
import com.coffeechain.dto.request.CreateUserRequest;
import com.coffeechain.dto.response.CreateUserResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "USER - Quản lý tài khoản",
        description = "API tạo và quản lý tài khoản người dùng. Người gọi cần đăng nhập và có quyền phù hợp."
)
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthGuard authGuard;
    private final UserService userService;

    public UserController(AuthGuard authGuard, UserService userService) {
        this.authGuard = authGuard;
        this.userService = userService;
    }

    @Operation(
            summary = "Lấy dữ liệu combobox cho màn tạo tài khoản",
            description = """
                Trả về danh sách vai trò và chi nhánh để frontend render form tạo tài khoản.

                Luật trả dữ liệu:
                - ADMIN: thấy tất cả vai trò và tất cả chi nhánh ACTIVE.
                - QUAN_LY_CHI_NHANH: chỉ thấy vai trò THU_NGAN và chi nhánh của chính mình.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy dữ liệu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền USER:CREATE", content = @Content)
    })
    @GetMapping("/lookups")
    public ResponseEntity<BaseResponse<CreateUserLookupResponse>> getCreateUserLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        SessionUser currentUser = authGuard.requirePermission(authHeader, "USER:CREATE");

        CreateUserLookupResponse data = userService.getCreateUserLookups(currentUser);

        return ResponseEntity.ok(
                BaseResponse.ok("Lấy dữ liệu tạo tài khoản thành công", data)
        );
    }

    @Operation(
            summary = "Tạo tài khoản người dùng",
            description = """
                    Tạo tài khoản mới trong hệ thống.

                    Quyền yêu cầu:
                    - USER:CREATE

                    Luật nghiệp vụ:
                    - ADMIN có thể tạo ADMIN, QUAN_LY_CHI_NHANH, QUAN_LY_KHO, THU_NGAN hoặc role hợp lệ khác.
                    - Tài khoản không phải ADMIN phải có mã chi nhánh.
                    - QUAN_LY_CHI_NHANH chỉ được tạo tài khoản THU_NGAN trong chính chi nhánh của mình.
                    - Tên đăng nhập và email không được trùng.

                    Header bắt buộc:
                    Authorization: Bearer <token>
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Thông tin tài khoản cần tạo",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateUserRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Admin tạo thu ngân",
                                    value = """
                                            {
                                              "tenDangNhap": "thungan01",
                                              "matKhau": "123456",
                                              "email": "thungan01@phungloccoffee.vn",
                                              "tenVaiTro": "THU_NGAN",
                                              "maChiNhanh": 1
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Admin tạo tài khoản admin",
                                    value = """
                                            {
                                              "tenDangNhap": "admin2",
                                              "matKhau": "admin123",
                                              "email": "admin2@phungloccoffee.vn",
                                              "tenVaiTro": "ADMIN",
                                              "maChiNhanh": null
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo tài khoản thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ, role không tồn tại hoặc thiếu mã chi nhánh", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền USER:CREATE hoặc vi phạm phạm vi tạo tài khoản", content = @Content),
            @ApiResponse(responseCode = "409", description = "Tên đăng nhập hoặc email đã tồn tại", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BaseResponse<CreateUserResponse>> createUser(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateUserRequest request
    ) {
        SessionUser currentUser = authGuard.requirePermission(authHeader, "USER:CREATE");

        CreateUserResponse data = userService.createUser(currentUser, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created("Tạo tài khoản thành công", data));
    }
}
