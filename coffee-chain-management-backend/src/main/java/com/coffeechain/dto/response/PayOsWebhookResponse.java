package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response xu ly webhook payOS")
public record PayOsWebhookResponse(
        @Schema(description = "Order code payOS", example = "150001")
        Long orderCode,

        @Schema(description = "Ma hoa don trong he thong", example = "15")
        Long maHoaDon,

        @Schema(description = "Ket qua xu ly webhook", example = "COMPLETED")
        String status
) {
}