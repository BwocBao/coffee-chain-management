package com.coffeechain.dto.response;

public class UnitResponse {
    private Long maDonViTinh;
    private String tenDonViTinh;
    private String kyHieu;

    public UnitResponse() {
    }

    public UnitResponse(Long maDonViTinh, String tenDonViTinh, String kyHieu) {
        this.maDonViTinh = maDonViTinh;
        this.tenDonViTinh = tenDonViTinh;
        this.kyHieu = kyHieu;
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

    public String getKyHieu() {
        return kyHieu;
    }

    public void setKyHieu(String kyHieu) {
        this.kyHieu = kyHieu;
    }
}