package com.coffeechain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StocktakeSystemStockResponse {
    private Long maLoHang;
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private BigDecimal soLuongHeThong;
    private LocalDate hanSuDung;
    private String trangThaiLo;

    public StocktakeSystemStockResponse() {
    }

    public StocktakeSystemStockResponse(
            Long maLoHang,
            Long maKho,
            String tenKho,
            Long maNguyenLieu,
            String tenNguyenLieu,
            String donViTinh,
            BigDecimal soLuongHeThong,
            LocalDate hanSuDung,
            String trangThaiLo
    ) {
        this.maLoHang = maLoHang;
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.maNguyenLieu = maNguyenLieu;
        this.tenNguyenLieu = tenNguyenLieu;
        this.donViTinh = donViTinh;
        this.soLuongHeThong = soLuongHeThong;
        this.hanSuDung = hanSuDung;
        this.trangThaiLo = trangThaiLo;
    }

    public Long getMaLoHang() {
        return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
        this.maLoHang = maLoHang;
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

    public BigDecimal getSoLuongHeThong() {
        return soLuongHeThong;
    }

    public void setSoLuongHeThong(BigDecimal soLuongHeThong) {
        this.soLuongHeThong = soLuongHeThong;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public String getTrangThaiLo() {
        return trangThaiLo;
    }

    public void setTrangThaiLo(String trangThaiLo) {
        this.trangThaiLo = trangThaiLo;
    }
}