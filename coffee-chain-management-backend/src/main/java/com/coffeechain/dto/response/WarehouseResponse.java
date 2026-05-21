package com.coffeechain.dto.response;

public class WarehouseResponse {
    private Long maKho;
    private String tenKho;
    private String loaiKho;
    private Long maChiNhanh;
    private String tenChiNhanh;
    private String trangThai;

    public WarehouseResponse() {
    }

    public WarehouseResponse(
            Long maKho,
            String tenKho,
            String loaiKho,
            Long maChiNhanh,
            String tenChiNhanh,
            String trangThai
    ) {
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.loaiKho = loaiKho;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.trangThai = trangThai;
    }

    public Long getMaKho() {
        return maKho;
    }

    public void setMaKho(Long maKho) {
        this.maKho = maKho;
    }

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

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}