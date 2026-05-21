package com.coffeechain.dto.response;

public class BranchResponse {
    private Long maChiNhanh;
    private String tenChiNhanh;
    private String diaChi;
    private String soDienThoai;
    private Long maKho;
    private String tenKho;
    private Integer soNhanVien;
    private String trangThai;

    public BranchResponse() {
    }

    public BranchResponse(
            Long maChiNhanh,
            String tenChiNhanh,
            String diaChi,
            String soDienThoai,
            Long maKho,
            String tenKho,
            Integer soNhanVien,
            String trangThai
    ) {
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.soNhanVien = soNhanVien;
        this.trangThai = trangThai;
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

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
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

    public Integer getSoNhanVien() {
        return soNhanVien;
    }

    public void setSoNhanVien(Integer soNhanVien) {
        this.soNhanVien = soNhanVien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}