package com.coffeechain.dto.response;

public class BranchStatisticsResponse {
    private Integer tongSoChiNhanh;
    private Integer soChiNhanhDangHoatDong;
    private Integer soChiNhanhDaDong;
    private Integer soChiNhanhBaoTri;
    private Long maChiNhanhNhieuNhanVienNhat;
    private String tenChiNhanhNhieuNhanVienNhat;
    private Integer soNhanVienNhieuNhat;

    public Integer getTongSoChiNhanh() {
        return tongSoChiNhanh;
    }

    public void setTongSoChiNhanh(Integer tongSoChiNhanh) {
        this.tongSoChiNhanh = tongSoChiNhanh;
    }

    public Integer getSoChiNhanhDangHoatDong() {
        return soChiNhanhDangHoatDong;
    }

    public void setSoChiNhanhDangHoatDong(Integer soChiNhanhDangHoatDong) {
        this.soChiNhanhDangHoatDong = soChiNhanhDangHoatDong;
    }

    public Integer getSoChiNhanhDaDong() {
        return soChiNhanhDaDong;
    }

    public void setSoChiNhanhDaDong(Integer soChiNhanhDaDong) {
        this.soChiNhanhDaDong = soChiNhanhDaDong;
    }

    public Integer getSoChiNhanhBaoTri() {
        return soChiNhanhBaoTri;
    }

    public void setSoChiNhanhBaoTri(Integer soChiNhanhBaoTri) {
        this.soChiNhanhBaoTri = soChiNhanhBaoTri;
    }

    public Long getMaChiNhanhNhieuNhanVienNhat() {
        return maChiNhanhNhieuNhanVienNhat;
    }

    public void setMaChiNhanhNhieuNhanVienNhat(Long maChiNhanhNhieuNhanVienNhat) {
        this.maChiNhanhNhieuNhanVienNhat = maChiNhanhNhieuNhanVienNhat;
    }

    public String getTenChiNhanhNhieuNhanVienNhat() {
        return tenChiNhanhNhieuNhanVienNhat;
    }

    public void setTenChiNhanhNhieuNhanVienNhat(String tenChiNhanhNhieuNhanVienNhat) {
        this.tenChiNhanhNhieuNhanVienNhat = tenChiNhanhNhieuNhanVienNhat;
    }

    public Integer getSoNhanVienNhieuNhat() {
        return soNhanVienNhieuNhat;
    }

    public void setSoNhanVienNhieuNhat(Integer soNhanVienNhieuNhat) {
        this.soNhanVienNhieuNhat = soNhanVienNhieuNhat;
    }
}