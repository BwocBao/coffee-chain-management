package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response tao QR chuyen khoan ngan hang qua payOS")
public record BankQrResponse(
        @Schema(description = "Ma hoa don POS", example = "15")
        Long maHoaDon,

        @Schema(description = "Ma don hang gui sang payOS", example = "150001")
        Long orderCode,

        @Schema(description = "So tien can thanh toan", example = "85000")
        BigDecimal soTien,

        @Schema(description = "Mo ta ngan hien tren giao dich payOS", example = "PL15")
        String moTa,

        @Schema(description = "URL checkout payOS")
        String checkoutUrl,

        @Schema(description = "Ma QR do payOS tra ve")
        String qrCode,

        @Schema(description = "Trang thai giao dich payOS", example = "PENDING")
        String trangThai
) {
}