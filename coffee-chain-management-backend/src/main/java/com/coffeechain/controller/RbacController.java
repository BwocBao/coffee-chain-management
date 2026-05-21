package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreateRoleRequest;
import com.coffeechain.dto.response.PermissionGroupResponse;
import com.coffeechain.dto.response.RolePermissionResponse;
import com.coffeechain.dto.response.RoleResponse;
import com.coffeechain.dto.request.UpdateRolePermissionsRequest;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "RBAC - Phân quyền & bảo mật",
        description = "API quản lý vai trò, danh sách quyền và gán quyền cho vai trò. Chỉ ADMIN được sử dụng nhóm API này."
)
@RestController
@RequestMapping("/api/rbac")
public class RbacController {
    private final RbacService rbacService;
    private final AuthGuard authGuard;

    public RbacController(RbacService rbacService, AuthGuard authGuard) {
        this.rbacService = rbacService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy danh sách vai trò",
            description = """
                    Trả về toàn bộ vai trò trong hệ thống như ADMIN, QUAN_LY_KHO, QUAN_LY_CHI_NHANH, THU_NGAN.
                    
                    Dùng API này để hiển thị danh sách role trong màn Phân quyền & bảo mật.
                    Chỉ ADMIN được gọi API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách vai trò thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải ADMIN", content = @Content)
    })
    @GetMapping("/roles")
    public ResponseEntity<BaseResponse<List<RoleResponse>>> roles(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requireRole(authHeader, "ADMIN");
        return ResponseEntity.ok(
                BaseResponse.ok("Lấy danh sách vai trò thành công", rbacService.findAllRoles())
        );
    }

    @Operation(
            summary = "Tạo vai trò mới",
            description = """
                    Tạo một vai trò mới trong bảng VAITRO.
                    
                    Ví dụ có thể tạo role: CA_TRUONG, NHAN_VIEN_KHO, QUAN_LY_THUC_TAP.
                    Sau khi tạo role, dùng API cập nhật phân quyền để gán quyền cho role đó.
                    
                    Lưu ý:
                    - Chỉ ADMIN được tạo role.
                    - tenVaiTro nên viết hoa, không dấu, dùng dấu gạch dưới.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Thông tin vai trò cần tạo",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateRoleRequest.class),
                    examples = @ExampleObject(
                            name = "Tạo role ca trưởng",
                            value = """
                                    {
                                      "tenVaiTro": "CA_TRUONG"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo vai trò thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc role đã tồn tại", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải ADMIN", content = @Content)
    })
    @PostMapping("/roles")
    public ResponseEntity<BaseResponse<RoleResponse>> createRole(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateRoleRequest request
    ) {
        authGuard.requireRole(authHeader, "ADMIN");
        return ResponseEntity.ok(
                BaseResponse.created("Tạo vai trò thành công", rbacService.createRole(request))
        );
    }

    @Operation(
            summary = "Lấy toàn bộ danh sách quyền",
            description = """
                    Trả về toàn bộ quyền đang có trong hệ thống, được nhóm theo chức năng/module.
                    
                    Ví dụ:
                    - USER:VIEW, USER:CREATE
                    - ROLE:VIEW, ROLE:UPDATE
                    - INVENTORY:IMPORT, INVENTORY:EXPORT
                    - ORDER:CREATE, ORDER:PAY
                    
                    Frontend dùng API này để render danh sách checkbox quyền trong màn Phân quyền & bảo mật.
                    Chỉ ADMIN được gọi API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách quyền thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải ADMIN", content = @Content)
    })
    @GetMapping("/permissions")
    public ResponseEntity<BaseResponse<List<PermissionGroupResponse>>> permissions(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requireRole(authHeader, "ADMIN");
        return ResponseEntity.ok(
                BaseResponse.ok("Lấy danh sách quyền thành công", rbacService.findAllPermissionGroups())
        );
    }

    @Operation(
            summary = "Lấy quyền hiện tại của một vai trò",
            description = """
                    Trả về danh sách quyền hiện đang được gán cho một role cụ thể.
                    
                    Frontend dùng API này khi admin chọn một role trong màn Phân quyền & bảo mật.
                    Các checkbox tương ứng với quyền của role sẽ được tick sẵn.
                    
                    Chỉ ADMIN được gọi API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy quyền của vai trò thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vai trò", content = @Content)
    })
    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<BaseResponse<RolePermissionResponse>> rolePermissions(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @Parameter(
                    description = "ID của vai trò cần xem quyền",
                    example = "4",
                    required = true
            )
            @PathVariable Long roleId
    ) {
        authGuard.requireRole(authHeader, "ADMIN");
        return ResponseEntity.ok(
                BaseResponse.ok("Lấy quyền của vai trò thành công", rbacService.findRolePermissions(roleId))
        );
    }

    @Operation(
            summary = "Cập nhật quyền cho một vai trò",
            description = """
                    Cập nhật lại toàn bộ danh sách quyền của một role.
                    
                    Cách hoạt động:
                    - Frontend gửi danh sách permissionIds được tick.
                    - Backend xóa quyền cũ của role trong bảng VAITRO_QUYEN.
                    - Backend insert lại các quyền mới.
                    
                    API này dùng khi admin bấm nút Lưu phân quyền.
                    
                    Khuyến nghị:
                    - Không nên cho sửa quyền của role ADMIN trên frontend để tránh tự khóa hệ thống.
                    - Chỉ ADMIN được gọi API này.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Danh sách ID quyền được gán cho vai trò",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = UpdateRolePermissionsRequest.class),
                    examples = @ExampleObject(
                            name = "Gán quyền POS cho thu ngân",
                            value = """
                                    {
                                      "permissionIds": [31, 32, 33, 13]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật phân quyền thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải ADMIN hoặc không được sửa role này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vai trò", content = @Content)
    })
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<BaseResponse<RolePermissionResponse>> updateRolePermissions(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @Parameter(
                    description = "ID của vai trò cần cập nhật quyền",
                    example = "4",
                    required = true
            )
            @PathVariable Long roleId,

            @RequestBody UpdateRolePermissionsRequest request
    ) {
        authGuard.requireRole(authHeader, "ADMIN");
        return ResponseEntity.ok(
                BaseResponse.ok("Cập nhật phân quyền thành công", rbacService.updateRolePermissions(roleId, request))
        );
    }
}