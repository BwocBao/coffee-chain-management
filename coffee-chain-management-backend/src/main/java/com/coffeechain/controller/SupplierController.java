package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreateSupplierRequest;
import com.coffeechain.dto.request.UpdateSupplierRequest;
import com.coffeechain.dto.response.SupplierResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;
    private final AuthGuard authGuard;

    public SupplierController(SupplierService supplierService, AuthGuard authGuard) {
        this.supplierService = supplierService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy danh sách nhà cung cấp",
            description = "Tìm kiếm nhà cung cấp theo tên, số điện thoại, email hoặc địa chỉ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem nhà cung cấp", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<SupplierResponse>>> searchSuppliers(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        authGuard.requirePermission(authHeader, "SUPPLIER:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy danh sách nhà cung cấp thành công",
                supplierService.searchSuppliers(keyword)
        ));
    }

    @Operation(summary = "Lấy chi tiết nhà cung cấp")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SupplierResponse>> getSupplierById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        authGuard.requirePermission(authHeader, "SUPPLIER:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết nhà cung cấp thành công",
                supplierService.getSupplierById(id)
        ));
    }

    @Operation(summary = "Tạo nhà cung cấp")
    @PostMapping
    public ResponseEntity<BaseResponse<SupplierResponse>> createSupplier(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateSupplierRequest request
    ) {
        authGuard.requirePermission(authHeader, "SUPPLIER:CREATE");

        return ResponseEntity.ok(BaseResponse.created(
                "Tạo nhà cung cấp thành công",
                supplierService.createSupplier(request)
        ));
    }

    @Operation(summary = "Cập nhật nhà cung cấp")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<SupplierResponse>> updateSupplier(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateSupplierRequest request
    ) {
        authGuard.requirePermission(authHeader, "SUPPLIER:UPDATE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật nhà cung cấp thành công",
                supplierService.updateSupplier(id, request)
        ));
    }

    @Operation(
            summary = "Xóa nhà cung cấp",
            description = "Chỉ xóa được nhà cung cấp chưa phát sinh phiếu nhập."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteSupplier(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        authGuard.requirePermission(authHeader, "SUPPLIER:DELETE");

        supplierService.deleteSupplier(id);

        return ResponseEntity.ok(BaseResponse.ok(
                "Xóa nhà cung cấp thành công",
                null
        ));
    }
}