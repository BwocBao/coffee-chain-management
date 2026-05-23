package com.coffeechain.controller;

import io.swagger.v3.oas.annotations.tags.Tag;


import com.coffeechain.dto.request.CreateWarehouseRequest;
import com.coffeechain.dto.request.UpdateWarehouseRequest;
import com.coffeechain.dto.request.UpdateWarehouseStatusRequest;
import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.response.WarehouseLookupResponse;
import com.coffeechain.dto.response.WarehouseResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quan ly thong tin kho", description = "Nhom API Quan ly thong tin kho. Swagger mo ta quyen can co, request, response va luong su dung chinh.")
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    private final WarehouseService warehouseService;
    private final AuthGuard authGuard;

    public WarehouseController(WarehouseService warehouseService, AuthGuard authGuard) {
        this.warehouseService = warehouseService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy danh sách kho",
            description = "Tìm kiếm và lọc kho theo từ khóa, loại kho và trạng thái."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem kho", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<WarehouseResponse>>> searchWarehouses(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "loaiKho", required = false) String loaiKho,
            @RequestParam(value = "trangThai", required = false) String trangThai
    ) {
        authGuard.requirePermission(authHeader, "WAREHOUSE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy danh sách kho thành công",
                warehouseService.searchWarehouses(keyword, loaiKho, trangThai)
        ));
    }

    @Operation(
            summary = "Lấy dữ liệu combobox cho màn quản lý kho",
            description = "Trả về loại kho và danh sách chi nhánh ACTIVE để frontend render form."
    )
    @GetMapping("/lookups")
    public ResponseEntity<BaseResponse<WarehouseLookupResponse>> getWarehouseLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requirePermission(authHeader, "WAREHOUSE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy dữ liệu quản lý kho thành công",
                warehouseService.getWarehouseLookups()
        ));
    }

    @Operation(summary = "Lấy chi tiết kho")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<WarehouseResponse>> getWarehouseById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        authGuard.requirePermission(authHeader, "WAREHOUSE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết kho thành công",
                warehouseService.getWarehouseById(id)
        ));
    }

    @Operation(summary = "Tạo kho")
    @PostMapping
    public ResponseEntity<BaseResponse<WarehouseResponse>> createWarehouse(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateWarehouseRequest request
    ) {
        authGuard.requirePermission(authHeader, "WAREHOUSE:CREATE");

        return ResponseEntity.ok(BaseResponse.created(
                "Tạo kho thành công",
                warehouseService.createWarehouse(request)
        ));
    }

    @Operation(summary = "Cập nhật kho")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<WarehouseResponse>> updateWarehouse(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateWarehouseRequest request
    ) {
        authGuard.requirePermission(authHeader, "WAREHOUSE:UPDATE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật kho thành công",
                warehouseService.updateWarehouse(id, request)
        ));
    }

    @Operation(summary = "Cập nhật trạng thái kho")
    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse<WarehouseResponse>> updateWarehouseStatus(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateWarehouseStatusRequest request
    ) {
        authGuard.requirePermission(authHeader, "WAREHOUSE:UPDATE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật trạng thái kho thành công",
                warehouseService.updateStatus(id, request == null ? null : request.getTrangThai())
        ));
    }

    @Operation(
            summary = "Ngưng hoạt động kho",
            description = "Không xóa cứng kho, chỉ chuyển trạng thái sang INACTIVE."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<WarehouseResponse>> deactivateWarehouse(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        authGuard.requirePermission(authHeader, "WAREHOUSE:DELETE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Ngưng hoạt động kho thành công",
                warehouseService.deactivateWarehouse(id)
        ));
    }
}