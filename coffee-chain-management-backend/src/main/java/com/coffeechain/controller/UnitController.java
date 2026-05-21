package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreateUnitRequest;
import com.coffeechain.dto.request.UpdateUnitRequest;
import com.coffeechain.dto.response.UnitResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
public class UnitController {
    private final UnitService unitService;
    private final AuthGuard authGuard;

    public UnitController(UnitService unitService, AuthGuard authGuard) {
        this.unitService = unitService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy danh sách đơn vị tính",
            description = "Tìm kiếm đơn vị tính theo tên hoặc ký hiệu."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem đơn vị tính", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<UnitResponse>>> searchUnits(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        authGuard.requirePermission(authHeader, "UNIT:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy danh sách đơn vị tính thành công",
                unitService.searchUnits(keyword)
        ));
    }

    @Operation(summary = "Lấy chi tiết đơn vị tính")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UnitResponse>> getUnitById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        authGuard.requirePermission(authHeader, "UNIT:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết đơn vị tính thành công",
                unitService.getUnitById(id)
        ));
    }

    @Operation(summary = "Tạo đơn vị tính")
    @PostMapping
    public ResponseEntity<BaseResponse<UnitResponse>> createUnit(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateUnitRequest request
    ) {
        authGuard.requirePermission(authHeader, "UNIT:CREATE");

        return ResponseEntity.ok(BaseResponse.created(
                "Tạo đơn vị tính thành công",
                unitService.createUnit(request)
        ));
    }

    @Operation(summary = "Cập nhật đơn vị tính")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<UnitResponse>> updateUnit(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateUnitRequest request
    ) {
        authGuard.requirePermission(authHeader, "UNIT:UPDATE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật đơn vị tính thành công",
                unitService.updateUnit(id, request)
        ));
    }

    @Operation(
            summary = "Xóa đơn vị tính",
            description = "Chỉ xóa được đơn vị tính chưa được nguyên liệu sử dụng."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteUnit(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        authGuard.requirePermission(authHeader, "UNIT:DELETE");

        unitService.deleteUnit(id);

        return ResponseEntity.ok(BaseResponse.ok(
                "Xóa đơn vị tính thành công",
                null
        ));
    }
}