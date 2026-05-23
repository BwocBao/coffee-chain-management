package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    description =
        "DTO response RecipeIngredientLineResponse. Mot dong nguyen lieu trong cong thuc san pham.")
public record RecipeIngredientLineResponse(
    @Schema(description = "Ma dong cong thuc", example = "1") Long maCongThuc,
    @Schema(description = "Ma nguyen lieu", example = "2") Long maNguyenLieu,
    @Schema(description = "Ten nguyen lieu", example = "Ca phe hat Robusta") String tenNguyenLieu,
    @Schema(description = "Don vi tinh cua nguyen lieu", example = "g") String donViTinh,
    @Schema(description = "So luong can cho mot san pham", example = "20") BigDecimal soLuongCan,
    @Schema(description = "Gia von theo mot don vi tinh", example = "95") BigDecimal giaVonDvt,
    @Schema(description = "Thanh tien gia von cua dong cong thuc", example = "1900")
        BigDecimal thanhTien) {}
