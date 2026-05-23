package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO request RecipeIngredientRequest. Mot dong nguyen lieu trong cong thuc san pham.")
public record RecipeIngredientRequest(
        @Schema(description = "Ma nguyen lieu duoc dung trong cong thuc", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long maNguyenLieu,
        @Schema(description = "So luong nguyen lieu can cho mot san pham", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal soLuongCan
) {
}
