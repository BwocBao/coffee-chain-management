package com.coffeechain.dto.response;

import java.math.BigDecimal;

public class InventoryHistorySummaryResponse {
    private Long maKho;
    private String tenKho;

    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;

    private BigDecimal tongNhap;
    private BigDecimal tongXuat;
    private BigDecimal tongDieuChuyenVao;
    private BigDecimal tongDieuChuyenRa;
    private BigDecimal tongHaoHut;
    private BigDecimal tongBanHangTruKho;
    private BigDecimal tongHoanTruKho;
    private BigDecimal tongDieuChinhKiemKho;
    private BigDecimal bienDongRong;

    private Integer soGiaoDich;

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

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getTongNhap() {
        return tongNhap;
    }

    public void setTongNhap(BigDecimal tongNhap) {
        this.tongNhap = tongNhap;
    }

    public BigDecimal getTongXuat() {
        return tongXuat;
    }

    public void setTongXuat(BigDecimal tongXuat) {
        this.tongXuat = tongXuat;
    }

    public BigDecimal getTongDieuChuyenVao() {
        return tongDieuChuyenVao;
    }

    public void setTongDieuChuyenVao(BigDecimal tongDieuChuyenVao) {
        this.tongDieuChuyenVao = tongDieuChuyenVao;
    }

    public BigDecimal getTongDieuChuyenRa() {
        return tongDieuChuyenRa;
    }

    public void setTongDieuChuyenRa(BigDecimal tongDieuChuyenRa) {
        this.tongDieuChuyenRa = tongDieuChuyenRa;
    }

    public BigDecimal getTongHaoHut() {
        return tongHaoHut;
    }

    public void setTongHaoHut(BigDecimal tongHaoHut) {
        this.tongHaoHut = tongHaoHut;
    }

    public BigDecimal getTongBanHangTruKho() {
        return tongBanHangTruKho;
    }

    public void setTongBanHangTruKho(BigDecimal tongBanHangTruKho) {
        this.tongBanHangTruKho = tongBanHangTruKho;
    }

    public BigDecimal getTongHoanTruKho() {
        return tongHoanTruKho;
    }

    public void setTongHoanTruKho(BigDecimal tongHoanTruKho) {
        this.tongHoanTruKho = tongHoanTruKho;
    }

    public BigDecimal getTongDieuChinhKiemKho() {
        return tongDieuChinhKiemKho;
    }

    public void setTongDieuChinhKiemKho(BigDecimal tongDieuChinhKiemKho) {
        this.tongDieuChinhKiemKho = tongDieuChinhKiemKho;
    }

    public BigDecimal getBienDongRong() {
        return bienDongRong;
    }

    public void setBienDongRong(BigDecimal bienDongRong) {
        this.bienDongRong = bienDongRong;
    }

    public Integer getSoGiaoDich() {
        return soGiaoDich;
    }

    public void setSoGiaoDich(Integer soGiaoDich) {
        this.soGiaoDich = soGiaoDich;
    }
}