package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreateWastageRequest;
import com.coffeechain.dto.response.WastageLookupResponse;
import com.coffeechain.dto.response.WastageLotResponse;
import com.coffeechain.dto.response.WastageResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.service.WastageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/wastages")
public class WastageController {
    private final WastageService wastageService;
    private final AuthGuard authGuard;

    public WastageController(WastageService wastageService, AuthGuard authGuard) {
        this.wastageService = wastageService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy dữ liệu combobox cho báo cáo hao hụt",
            description = "Trả về danh sách kho, nguyên liệu và loại hao hụt."
    )
    @GetMapping("/lookups")
    public ResponseEntity<BaseResponse<WastageLookupResponse>> getLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "WASTAGE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy dữ liệu báo cáo hao hụt thành công",
                wastageService.getLookups(user)
        ));
    }

    @Operation(
            summary = "Lấy danh sách lô còn tồn để báo cáo hao hụt",
            description = "Frontend gọi API này sau khi chọn kho và nguyên liệu."
    )
    @GetMapping("/lots")
    public ResponseEntity<BaseResponse<List<WastageLotResponse>>> getAvailableLots(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long maKho,
            @RequestParam Long maNguyenLieu
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "WASTAGE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy danh sách lô còn tồn thành công",
                wastageService.getAvailableLots(user, maKho, maNguyenLieu)
        ));
    }

    @Operation(
            summary = "Tra cứu danh sách phiếu hao hụt",
            description = "Lọc theo kho, nguyên liệu, loại hao hụt, thời gian và từ khóa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tra cứu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem hao hụt", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<WastageResponse>>> searchWastages(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestParam(value = "maKho", required = false) Long maKho,
            @RequestParam(value = "maNguyenLieu", required = false) Long maNguyenLieu,
            @RequestParam(value = "loaiHaoHut", required = false) String loaiHaoHut,

            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "WASTAGE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Tra cứu phiếu hao hụt thành công",
                wastageService.searchWastages(
                        user,
                        maKho,
                        maNguyenLieu,
                        loaiHaoHut,
                        fromDate,
                        toDate,
                        keyword
                )
        ));
    }

    @Operation(summary = "Lấy chi tiết phiếu hao hụt")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<WastageResponse>> getById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "WASTAGE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết phiếu hao hụt thành công",
                wastageService.getById(user, id)
        ));
    }

    @Operation(
            summary = "Tạo báo cáo hao hụt",
            description = """
                    Tạo phiếu hao hụt và xử lý đúng nghiệp vụ:
                    1. Insert PHIEUHAOHUT.
                    2. Trừ LOHANG_NGUYENLIEU.
                    3. Trừ TONKHO.
                    4. Ghi NHATKY_KHO với loai_giao_dich = WASTAGE.
                    """
    )
    @PostMapping
    public ResponseEntity<BaseResponse<WastageResponse>> createWastage(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateWastageRequest request
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "WASTAGE:CREATE");

        return ResponseEntity.ok(BaseResponse.created(
                "Tạo báo cáo hao hụt thành công",
                wastageService.createWastage(user, request)
        ));
    }
}