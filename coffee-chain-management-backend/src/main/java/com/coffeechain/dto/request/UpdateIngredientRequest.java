package com.coffeechain.dto.request;

import java.math.BigDecimal;

public class UpdateIngredientRequest {
    private String tenNguyenLieu;
    private Long maDonViTinh;
    private BigDecimal mucTonToiThieu;
    private String trangThai;

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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}