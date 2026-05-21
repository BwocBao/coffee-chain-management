package com.coffeechain.controller;


import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.response.InventoryHistoryLookupResponse;
import com.coffeechain.dto.response.InventoryHistoryResponse;
import com.coffeechain.dto.response.InventoryHistorySummaryResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.service.InventoryHistoryService;
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
@RequestMapping("/api/inventory/history")
public class InventoryHistoryController {
    private final InventoryHistoryService historyService;
    private final AuthGuard authGuard;

    public InventoryHistoryController(InventoryHistoryService historyService, AuthGuard authGuard) {
        this.historyService = historyService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lấy dữ liệu combobox cho màn tra cứu lịch sử kho",
            description = "Trả về danh sách kho, nguyên liệu và loại giao dịch kho để frontend render bộ lọc."
    )
    @GetMapping("/lookups")
    public ResponseEntity<BaseResponse<InventoryHistoryLookupResponse>> getLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy dữ liệu tra cứu lịch sử kho thành công",
                historyService.getLookups(user)
        ));
    }

    @Operation(
            summary = "Tra cứu lịch sử kho",
            description = """
                    Tra cứu nhật ký kho từ bảng NHATKY_KHO.
                    Có thể lọc theo kho, nguyên liệu, lô hàng, loại giao dịch, khoảng thời gian và từ khóa.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tra cứu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token hết hạn", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem lịch sử kho", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<InventoryHistoryResponse>>> searchHistory(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestParam(value = "maKho", required = false) Long maKho,
            @RequestParam(value = "maNguyenLieu", required = false) Long maNguyenLieu,
            @RequestParam(value = "maLoHang", required = false) Long maLoHang,
            @RequestParam(value = "loaiGiaoDich", required = false) String loaiGiaoDich,

            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Tra cứu lịch sử kho thành công",
                historyService.searchHistory(
                        user,
                        maKho,
                        maNguyenLieu,
                        maLoHang,
                        loaiGiaoDich,
                        fromDate,
                        toDate,
                        keyword
                )
        ));
    }

    @Operation(summary = "Lấy chi tiết một dòng nhật ký kho")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<InventoryHistoryResponse>> getHistoryById(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết nhật ký kho thành công",
                historyService.getHistoryById(user, id)
        ));
    }

    @Operation(
            summary = "Tổng hợp lịch sử nhập xuất kho",
            description = """
                    Tổng hợp số lượng nhập, xuất, điều chuyển, hao hụt, bán hàng trừ kho và điều chỉnh kiểm kho.
                    Dùng cho card thống kê hoặc bảng tổng hợp lịch sử kho.
                    """
    )
    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<List<InventoryHistorySummaryResponse>>> getSummary(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,

            @RequestParam(value = "maKho", required = false) Long maKho,
            @RequestParam(value = "maNguyenLieu", required = false) Long maNguyenLieu,

            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy tổng hợp lịch sử kho thành công",
                historyService.getSummary(user, maKho, maNguyenLieu, fromDate, toDate)
        ));
    }
}