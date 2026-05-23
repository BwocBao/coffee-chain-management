package com.coffeechain.dto.response;

import java.math.BigDecimal;

public record PosPaymentResponse(
        Long orderCode,
        BigDecimal soTien,
        String moTa,
        String checkoutUrl,
        String qrCode,
        String trangThai
) {
}
