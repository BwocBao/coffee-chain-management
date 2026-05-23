package com.coffeechain.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreateBranchRequest;
import com.coffeechain.dto.request.UpdateBranchRequest;
import com.coffeechain.dto.request.UpdateBranchStatusRequest;
import com.coffeechain.dto.response.BranchResponse;
import com.coffeechain.dto.response.BranchStatisticsResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quan ly chi nhanh", description = "Nhom API Quan ly chi nhanh. Swagger mo ta quyen can co, request, response va luong su dung chinh.")
@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService branchService;
    private final AuthGuard authGuard;

    public BranchController(BranchService branchService, AuthGuard authGuard) {
        this.branchService = branchService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy danh sách chi nhánh",
            description = """
                    Trả về danh sách chi nhánh kèm thông tin kho và số nhân viên.
                    Dùng để render bảng: Mã CN, Tên chi nhánh, Địa chỉ, SĐT, Kho, Số nhân viên, Trạng thái.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem chi nhánh", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<BranchResponse>>> searchBranches(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status
    ) {
        authGuard.requirePermission(authHeader, "BRANCH:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy danh sách chi nhánh thành công",
                branchService.searchBranches(keyword, status)
        ));
    }

    @Operation(
            summary = "Lấy thống kê chi nhánh",
            description = "Trả về tổng số chi nhánh, số chi nhánh đang hoạt động, đã đóng và chi nhánh nhiều nhân viên nhất."
    )
    @GetMapping("/statistics")
    public ResponseEntity<BaseResponse<BranchStatisticsResponse>> getStatistics(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requirePermission(authHeader, "BRANCH:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy thống kê chi nhánh thành công",
                branchService.getStatistics()
        ));
    }

    @Operation(summary = "Lấy chi tiết chi nhánh")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<BranchResponse>> getBranchById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        authGuard.requirePermission(authHeader, "BRANCH:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết chi nhánh thành công",
                branchService.getBranchById(id)
        ));
    }

    @Operation(summary = "Tạo chi nhánh")
    @PostMapping
    public ResponseEntity<BaseResponse<BranchResponse>> createBranch(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateBranchRequest request
    ) {
        authGuard.requirePermission(authHeader, "BRANCH:CREATE");

        return ResponseEntity.ok(BaseResponse.created(
                "Tạo chi nhánh thành công",
                branchService.createBranch(request)
        ));
    }

    @Operation(summary = "Cập nhật chi nhánh")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<BranchResponse>> updateBranch(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateBranchRequest request
    ) {
        authGuard.requirePermission(authHeader, "BRANCH:UPDATE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật chi nhánh thành công",
                branchService.updateBranch(id, request)
        ));
    }

    @Operation(
            summary = "Cập nhật trạng thái chi nhánh",
            description = "Dùng để chuyển chi nhánh sang ACTIVE, CLOSED hoặc MAINTENANCE."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse<BranchResponse>> updateBranchStatus(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateBranchStatusRequest request
    ) {
        authGuard.requirePermission(authHeader, "BRANCH:UPDATE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật trạng thái chi nhánh thành công",
                branchService.updateStatus(id, request == null ? null : request.getTrangThai())
        ));
    }
}