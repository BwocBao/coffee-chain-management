package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "DTO request CreateRecipeRequest. Frontend gui len khi tao san pham/cong thuc moi.")
public record CreateRecipeRequest(
        @Schema(description = "Ten san pham hien thi tren POS", example = "Ca phe sua da", requiredMode = Schema.RequiredMode.REQUIRED)
        String tenSanPham,
        @Schema(description = "Duong dan hoac ten file hinh anh san pham", example = "ca-phe-sua-da.png")
        String hinhAnh,
        @Schema(description = "Gia ban hien tai cua san pham", example = "35000", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal giaBanHienTai,
        @Schema(description = "Trang thai san pham, vi du AVAILABLE hoac INACTIVE", example = "AVAILABLE")
        String trangThai,
        @Schema(description = "Danh sach nguyen lieu tao nen cong thuc", requiredMode = Schema.RequiredMode.REQUIRED)
        List<RecipeIngredientRequest> items
) {
}
