package com.coffeechain.dto.response;

import java.math.BigDecimal;

public record PosProductResponse(
    Long maSanPham, String tenSanPham, String hinhAnh, BigDecimal giaBanHienTai) {}
