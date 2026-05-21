package com.coffeechain.dto.request;

public class CreateWarehouseRequest {
    private String tenKho;
    private String loaiKho;
    private Long maChiNhanh;

    public String getTenKho() {
        return tenKho;
    }

    public void setTenKho(String tenKho) {
        this.tenKho = tenKho;
    }

    public String getLoaiKho() {
        return loaiKho;
    }

    public void setLoaiKho(String loaiKho) {
        this.loaiKho = loaiKho;
    }

    public Long getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Long maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
    }
}