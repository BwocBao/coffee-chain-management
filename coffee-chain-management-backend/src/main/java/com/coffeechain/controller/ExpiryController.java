package com.coffeechain.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.response.ExpiryLookupResponse;
import com.coffeechain.dto.response.ExpiryLotResponse;
import com.coffeechain.dto.response.ExpiryRefreshResponse;
import com.coffeechain.dto.response.ExpiryStatisticsResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.service.ExpiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Theo doi han su dung lo hang", description = "Nhom API Theo doi han su dung lo hang. Swagger mo ta quyen can co, request, response va luong su dung chinh.")
@RestController
@RequestMapping("/api/inventory/expiry")
public class ExpiryController {
    private final ExpiryService expiryService;
    private final AuthGuard authGuard;

    public ExpiryController(ExpiryService expiryService, AuthGuard authGuard) {
        this.expiryService = expiryService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy dữ liệu combobox cho màn theo dõi hạn sử dụng",
            description = """
                    Trả về danh sách kho, nguyên liệu, trạng thái lô và mức cảnh báo.
                    Dùng để render bộ lọc trên frontend.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy dữ liệu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem tồn kho", content = @Content)
    })
    @GetMapping("/lookups")
    public ResponseEntity<BaseResponse<ExpiryLookupResponse>> getLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy dữ liệu theo dõi hạn sử dụng thành công",
                expiryService.getLookups(user)
        ));
    }

    @Operation(
            summary = "Lấy danh sách lô theo hạn sử dụng",
            description = """
                    Tra cứu lô hàng theo kho, nguyên liệu, trạng thái, mức cảnh báo và số ngày còn hạn.
                    Mức cảnh báo gồm: DA_HET_HAN, SAP_HET_HAN, BINH_THUONG, KHONG_CO_HSD, HET_HANG.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem tồn kho", content = @Content)
    })
    @GetMapping("/lots")
    public ResponseEntity<BaseResponse<List<ExpiryLotResponse>>> searchLots(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestParam(value = "maKho", required = false) Long maKho,
            @RequestParam(value = "maNguyenLieu", required = false) Long maNguyenLieu,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "mucCanhBao", required = false) String mucCanhBao,
            @RequestParam(value = "daysToExpire", required = false) Integer daysToExpire,
            @RequestParam(value = "onlyAvailable", required = false) Boolean onlyAvailable,
            @RequestParam(value = "warningDays", required = false) Integer warningDays
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy danh sách lô theo hạn sử dụng thành công",
                expiryService.searchLots(
                        user,
                        maKho,
                        maNguyenLieu,
                        trangThai,
                        mucCanhBao,
                        daysToExpire,
                        onlyAvailable,
                        warningDays
                )
        ));
    }

    @Operation(summary = "Lấy chi tiết lô theo hạn sử dụng")
    @GetMapping("/lots/{id}")
    public ResponseEntity<BaseResponse<ExpiryLotResponse>> getLotById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết lô hàng thành công",
                expiryService.getLotById(user, id)
        ));
    }

    @Operation(
            summary = "Lấy thống kê hạn sử dụng",
            description = """
                    Trả về số lô đang hoạt động, sắp hết hạn, đã hết hạn, đã dùng hết và không có hạn sử dụng.
                    warningDays mặc định là 30 nếu frontend không truyền.
                    """
    )
    @GetMapping("/statistics")
    public ResponseEntity<BaseResponse<ExpiryStatisticsResponse>> getStatistics(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "maKho", required = false) Long maKho,
            @RequestParam(value = "warningDays", required = false) Integer warningDays
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy thống kê hạn sử dụng thành công",
                expiryService.getStatistics(user, maKho, warningDays)
        ));
    }

    @Operation(
            summary = "Rà soát và cập nhật trạng thái lô hết hạn",
            description = """
                    Gọi procedure prc_cap_nhat_lo_het_han trong Oracle.
                    Procedure sẽ chuyển lô quá hạn sang EXPIRED và lô hết số lượng sang USED_UP.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rà soát thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật tồn kho", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<ExpiryRefreshResponse>> refreshExpiredLots(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requirePermission(authHeader, "INVENTORY:ADJUST");

        return ResponseEntity.ok(BaseResponse.ok(
                "Rà soát hạn sử dụng thành công",
                expiryService.refreshExpiredLots()
        ));
    }
}