package com.coffeechain.dto.response;

import java.math.BigDecimal;

public record PosOrderItemResponse(
    Long maCtHoaDon,
    Long maSanPham,
    String tenSanPham,
    Integer soLuong,
    BigDecimal donGiaBan,
    BigDecimal thanhTienDong) {}
