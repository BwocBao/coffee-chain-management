package com.coffeechain.dto.request;

import java.math.BigDecimal;

public class CreateIngredientRequest {
    private String tenNguyenLieu;
    private Long maDonViTinh;
    private BigDecimal mucTonToiThieu;

    public String getTenNguyenLieu() {
        return tenNguyenLieu;
    }

    public void setTenNguyenLieu(String tenNguyenLieu) {
        this.tenNguyenLieu = tenNguyenLieu;
    }

    public Long getMaDonViTinh() {
        return maDonViTinh;
    }

    public void setMaDonViTinh(Long maDonViTinh) {
        this.maDonViTinh = maDonViTinh;
    }

    public BigDecimal getMucTonToiThieu() {
        return mucTonToiThieu;
    }

    public void setMucTonToiThieu(BigDecimal mucTonToiThieu) {
        this.mucTonToiThieu = mucTonToiThieu;
    }
}