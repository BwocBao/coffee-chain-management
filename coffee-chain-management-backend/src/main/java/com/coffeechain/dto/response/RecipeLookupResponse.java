package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "DTO response RecipeLookupResponse. Du lieu combobox can nap cho man hinh quan ly cong thuc.")
public record RecipeLookupResponse(
        @Schema(description = "Danh sach nguyen lieu co the chon vao cong thuc")
        List<IngredientOption> ingredients,
        @Schema(description = "Danh sach trang thai san pham hop le")
        List<StatusOption> statuses
) {
    @Schema(description = "Lua chon nguyen lieu trong combobox cong thuc")
    public record IngredientOption(
            @Schema(description = "Ma nguyen lieu", example = "1")
            Long id,
            @Schema(description = "Ten nguyen lieu", example = "Ca phe hat Robusta")
            String name,
            @Schema(description = "Don vi tinh", example = "g")
            String unit,
            @Schema(description = "Gia von theo don vi tinh", example = "95")
            BigDecimal giaVonDvt
    ) {
    }

    @Schema(description = "Lua chon trang thai san pham")
    public record StatusOption(
            @Schema(description = "Ma trang thai", example = "AVAILABLE")
            String code,
            @Schema(description = "Ten hien thi trang thai", example = "Dang ban")
            String name
    ) {
    }
}
