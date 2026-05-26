package com.coffeechain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PosOrderResponse(
    Long maHoaDon,
    Long maChiNhanh,
    String tenChiNhanh,
    Long maPos,
    Long maNguoiDung,
    String trangThaiHoaDon,
    String trangThaiThanhToan,
    String phuongThucThanhToan,
    BigDecimal tongThanhToan,
    LocalDateTime thoiGianTaoHoaDon,
    LocalDateTime thoiGianThanhToan,
    List<PosOrderItemResponse> items,
    PosPaymentResponse payment) {}
