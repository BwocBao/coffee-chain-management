package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "DTO response RecipeDetailResponse. Chi tiet san pham/cong thuc gom thong tin san pham va cac dong nguyen lieu.")
public record RecipeDetailResponse(
        @Schema(description = "Ma san pham", example = "1")
        Long maSanPham,
        @Schema(description = "Ma cong thuc hien thi cho nguoi dung", example = "CT-0001")
        String maCongThucHienThi,
        @Schema(description = "Ten san pham", example = "Ca phe sua da")
        String tenSanPham,
        @Schema(description = "Duong dan hoac ten file hinh anh san pham", example = "ca-phe-sua-da.png")
        String hinhAnh,
        @Schema(description = "Gia ban hien tai", example = "35000")
        BigDecimal giaBanHienTai,
        @Schema(description = "Trang thai san pham", example = "AVAILABLE")
        String trangThai,
        @Schema(description = "Thoi diem tao san pham/cong thuc", example = "2026-05-22T08:30:00")
        LocalDateTime ngayTao,
        @Schema(description = "Danh sach dong nguyen lieu cua cong thuc")
        List<RecipeIngredientLineResponse> items,
        @Schema(description = "Tong gia von uoc tinh cua mot san pham", example = "6500")
        BigDecimal tongGiaVon,
        @Schema(description = "Bien loi nhuan gop uoc tinh", example = "28500")
        BigDecimal bienLoiNhuanGop
) {
}
