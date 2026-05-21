package com.coffeechain.dto.response;

import java.math.BigDecimal;

public class IngredientResponse {
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private Long maDonViTinh;
    private String tenDonViTinh;
    private String kyHieuDonViTinh;
    private BigDecimal mucTonToiThieu;
    private String trangThai;

    public IngredientResponse() {
    }

    public IngredientResponse(
            Long maNguyenLieu,
            String tenNguyenLieu,
            Long maDonViTinh,
            String tenDonViTinh,
            String kyHieuDonViTinh,
            BigDecimal mucTonToiThieu,
            String trangThai
    ) {
        this.maNguyenLieu = maNguyenLieu;
        this.tenNguyenLieu = tenNguyenLieu;
        this.maDonViTinh = maDonViTinh;
        this.tenDonViTinh = tenDonViTinh;
        this.kyHieuDonViTinh = kyHieuDonViTinh;
        this.mucTonToiThieu = mucTonToiThieu;
        this.trangThai = trangThai;
    }

    public Long getMaNguyenLieu() {
        return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
        this.maNguyenLieu = maNguyenLieu;
    }

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

    public String getTenDonViTinh() {
        return tenDonViTinh;
    }

    public void setTenDonViTinh(String tenDonViTinh) {
        this.tenDonViTinh = tenDonViTinh;
    }

    public String getKyHieuDonViTinh() {
        return kyHieuDonViTinh;
    }

    public void setKyHieuDonViTinh(String kyHieuDonViTinh) {
        this.kyHieuDonViTinh = kyHieuDonViTinh;
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