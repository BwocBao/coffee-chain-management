package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO response PosOrderSummaryResponse. Dong tom tat hoa don POS cho bang danh sach.")
public record PosOrderSummaryResponse(
    @Schema(description = "Ma hoa don", example = "15") Long maHoaDon,
    @Schema(description = "Ma chi nhanh", example = "1") Long maChiNhanh,
    @Schema(description = "Ten chi nhanh", example = "Ben Thanh") String tenChiNhanh,
    @Schema(description = "Ma may POS", example = "1") Long maPos,
    @Schema(description = "Trang thai hoa don", example = "PENDING") String trangThaiHoaDon,
    @Schema(description = "Trang thai thanh toan", example = "PENDING") String trangThaiThanhToan,
    @Schema(description = "Phuong thuc thanh toan", example = "CASH") String phuongThucThanhToan,
    @Schema(description = "Tong thanh toan", example = "85000") BigDecimal tongThanhToan,
    @Schema(description = "Thoi gian tao hoa don") LocalDateTime thoiGianTaoHoaDon,
    @Schema(description = "So dong san pham trong hoa don", example = "3") Integer soDong) {}
