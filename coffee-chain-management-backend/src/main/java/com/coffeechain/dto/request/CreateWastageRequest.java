package com.coffeechain.dto.request;

import java.math.BigDecimal;

public class CreateWastageRequest {
    private Long maKho;
    private Long maNguyenLieu;
    private Long maLoHang;
    private BigDecimal soLuongHaoHut;
    private String loaiHaoHut;
    private String ghiChu;

    public Long getMaKho() {
        return maKho;
    }

    public void setMaKho(Long maKho) {
        this.maKho = maKho;
    }

    public Long getMaNguyenLieu() {
        return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
        this.maNguyenLieu = maNguyenLieu;
    }

    public Long getMaLoHang() {
        return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
        this.maLoHang = maLoHang;
    }

    public BigDecimal getSoLuongHaoHut() {
        return soLuongHaoHut;
    }

    public void setSoLuongHaoHut(BigDecimal soLuongHaoHut) {
        this.soLuongHaoHut = soLuongHaoHut;
    }

    public String getLoaiHaoHut() {
        return loaiHaoHut;
    }

    public void setLoaiHaoHut(String loaiHaoHut) {
        this.loaiHaoHut = loaiHaoHut;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}