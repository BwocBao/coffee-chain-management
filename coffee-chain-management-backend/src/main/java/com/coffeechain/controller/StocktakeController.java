package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreateStocktakeRequest;
import com.coffeechain.dto.request.UpdateStocktakeRequest;
import com.coffeechain.dto.response.StocktakeLookupResponse;
import com.coffeechain.dto.response.StocktakeResponse;
import com.coffeechain.dto.response.StocktakeSystemStockResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.service.StocktakeService;
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
@RequestMapping("/api/inventory/stocktakes")
public class StocktakeController {
    private final StocktakeService stocktakeService;
    private final AuthGuard authGuard;

    public StocktakeController(StocktakeService stocktakeService, AuthGuard authGuard) {
        this.stocktakeService = stocktakeService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy dữ liệu combobox cho kiểm kho",
            description = "Trả về danh sách kho, nguyên liệu, trạng thái phiếu và hướng xử lý kiểm kho."
    )
    @GetMapping("/lookups")
    public ResponseEntity<BaseResponse<StocktakeLookupResponse>> getLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy dữ liệu kiểm kho thành công",
                stocktakeService.getLookups(user)
        ));
    }

    @Operation(
            summary = "Lấy số lượng hệ thống để lập phiếu kiểm kho",
            description = "Frontend gọi API này sau khi chọn kho hoặc nguyên liệu để lấy danh sách lô còn tồn."
    )
    @GetMapping("/system-stock")
    public ResponseEntity<BaseResponse<List<StocktakeSystemStockResponse>>> getSystemStock(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long maKho,
            @RequestParam(value = "maNguyenLieu", required = false) Long maNguyenLieu
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy tồn hệ thống thành công",
                stocktakeService.getSystemStock(user, maKho, maNguyenLieu)
        ));
    }

    @Operation(
            summary = "Tra cứu danh sách phiếu kiểm kho",
            description = "Lọc theo kho, trạng thái, thời gian và từ khóa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tra cứu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem kiểm kho", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<StocktakeResponse>>> searchStocktakes(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestParam(value = "maKho", required = false) Long maKho,
            @RequestParam(value = "trangThai", required = false) String trangThai,

            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Tra cứu phiếu kiểm kho thành công",
                stocktakeService.searchStocktakes(user, maKho, trangThai, fromDate, toDate, keyword)
        ));
    }

    @Operation(summary = "Lấy chi tiết phiếu kiểm kho")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<StocktakeResponse>> getById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết phiếu kiểm kho thành công",
                stocktakeService.getById(user, id)
        ));
    }

    @Operation(
            summary = "Tạo phiếu kiểm kho",
            description = "Tạo phiếu kiểm kho ở trạng thái DRAFT và lưu các dòng chi tiết kiểm kho."
    )
    @PostMapping
    public ResponseEntity<BaseResponse<StocktakeResponse>> createStocktake(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateStocktakeRequest request
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:CREATE");

        return ResponseEntity.ok(BaseResponse.created(
                "Tạo phiếu kiểm kho thành công",
                stocktakeService.createStocktake(user, request)
        ));
    }

    @Operation(
            summary = "Cập nhật phiếu kiểm kho",
            description = "Chỉ được cập nhật phiếu kiểm kho khi còn trạng thái DRAFT."
    )
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<StocktakeResponse>> updateStocktake(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody UpdateStocktakeRequest request
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:UPDATE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật phiếu kiểm kho thành công",
                stocktakeService.updateStocktake(user, id, request)
        ));
    }

    @Operation(
            summary = "Hoàn tất phiếu kiểm kho",
            description = """
                    Hoàn tất kiểm kho và xử lý theo từng dòng:
                    NO_ACTION: chỉ ghi nhận chênh lệch.
                    ADJUST_STOCK: cập nhật lô, tồn kho và ghi NHATKY_KHO = STOCKTAKE_ADJUST.
                    CREATE_WASTAGE: tạo PHIEUHAOHUT, cập nhật lô, tồn kho và ghi NHATKY_KHO = WASTAGE.
                    """
    )
    @PostMapping("/{id}/complete")
    public ResponseEntity<BaseResponse<StocktakeResponse>> completeStocktake(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:ADJUST");

        return ResponseEntity.ok(BaseResponse.ok(
                "Hoàn tất kiểm kho thành công",
                stocktakeService.completeStocktake(user, id)
        ));
    }

    @Operation(
            summary = "Hủy phiếu kiểm kho",
            description = "Chỉ được hủy phiếu kiểm kho khi còn trạng thái DRAFT."
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BaseResponse<StocktakeResponse>> cancelStocktake(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "STOCKTAKE:CANCEL");

        return ResponseEntity.ok(BaseResponse.ok(
                "Hủy phiếu kiểm kho thành công",
                stocktakeService.cancelStocktake(user, id)
        ));
    }
}