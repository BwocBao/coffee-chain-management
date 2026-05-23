package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
    description =
        "DTO response RecipeSummaryResponse. Thong tin tom tat san pham/cong thuc tren danh sach.")
public record RecipeSummaryResponse(
    @Schema(description = "Ma san pham", example = "1") Long maSanPham,
    @Schema(description = "Ma cong thuc hien thi cho nguoi dung", example = "CT-0001")
        String maCongThucHienThi,
    @Schema(description = "Ten san pham", example = "Ca phe sua da") String tenSanPham,
    @Schema(description = "Gia ban hien tai", example = "35000") BigDecimal giaBanHienTai,
    @Schema(description = "Trang thai san pham", example = "AVAILABLE") String trangThai,
    @Schema(description = "Thoi diem tao san pham/cong thuc", example = "2026-05-22T08:30:00")
        LocalDateTime ngayTao,
    @Schema(description = "So nguyen lieu trong cong thuc", example = "3") Integer soNguyenLieu) {}
