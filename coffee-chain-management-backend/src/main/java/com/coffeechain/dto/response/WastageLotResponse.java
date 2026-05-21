package com.coffeechain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WastageLotResponse {
    private Long maLoHang;
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private BigDecimal soLuongConLai;
    private LocalDate hanSuDung;
    private String trangThai;

    public WastageLotResponse() {
    }

    public WastageLotResponse(
            Long maLoHang,
            Long maKho,
            String tenKho,
            Long maNguyenLieu,
            String tenNguyenLieu,
            String donViTinh,
            BigDecimal soLuongConLai,
            LocalDate hanSuDung,
            String trangThai
    ) {
        this.maLoHang = maLoHang;
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.maNguyenLieu = maNguyenLieu;
        this.tenNguyenLieu = tenNguyenLieu;
        this.donViTinh = donViTinh;
        this.soLuongConLai = soLuongConLai;
        this.hanSuDung = hanSuDung;
        this.trangThai = trangThai;
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

    public BigDecimal getSoLuongConLai() {
        return soLuongConLai;
    }

    public void setSoLuongConLai(BigDecimal soLuongConLai) {
        this.soLuongConLai = soLuongConLai;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}