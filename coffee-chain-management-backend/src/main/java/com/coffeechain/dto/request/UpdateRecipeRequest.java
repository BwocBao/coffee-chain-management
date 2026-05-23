package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "DTO request UpdateRecipeRequest. Frontend gui len khi cap nhat san pham/cong thuc.")
public record UpdateRecipeRequest(
        @Schema(description = "Ten san pham sau khi cap nhat", example = "Bac xiu")
        String tenSanPham,
        @Schema(description = "Duong dan hoac ten file hinh anh san pham", example = "bac-xiu.png")
        String hinhAnh,
        @Schema(description = "Gia ban hien tai sau khi cap nhat", example = "39000")
        BigDecimal giaBanHienTai,
        @Schema(description = "Trang thai san pham, vi du AVAILABLE hoac INACTIVE", example = "AVAILABLE")
        String trangThai,
        @Schema(description = "Danh sach nguyen lieu moi cua cong thuc. Backend thay the cong thuc cu bang danh sach nay.")
        List<RecipeIngredientRequest> items
) {
}
