package com.coffeechain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpiryLotResponse {
    private Long maLoHang;
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private BigDecimal soLuongConLai;
    private LocalDateTime ngayTao;
    private LocalDate hanSuDung;
    private Integer soNgayConLai;
    private String trangThai;
    private String mucCanhBao;

    public ExpiryLotResponse() {
    }

    public ExpiryLotResponse(
            Long maLoHang,
            Long maKho,
            String tenKho,
            Long maNguyenLieu,
            String tenNguyenLieu,
            String donViTinh,
            BigDecimal soLuongConLai,
            LocalDateTime ngayTao,
            LocalDate hanSuDung,
            Integer soNgayConLai,
            String trangThai,
            String mucCanhBao
    ) {
        this.maLoHang = maLoHang;
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.maNguyenLieu = maNguyenLieu;
        this.tenNguyenLieu = tenNguyenLieu;
        this.donViTinh = donViTinh;
        this.soLuongConLai = soLuongConLai;
        this.ngayTao = ngayTao;
        this.hanSuDung = hanSuDung;
        this.soNgayConLai = soNgayConLai;
        this.trangThai = trangThai;
        this.mucCanhBao = mucCanhBao;
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

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public Integer getSoNgayConLai() {
        return soNgayConLai;
    }

    public void setSoNgayConLai(Integer soNgayConLai) {
        this.soNgayConLai = soNgayConLai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMucCanhBao() {
        return mucCanhBao;
    }

    public void setMucCanhBao(String mucCanhBao) {
        this.mucCanhBao = mucCanhBao;
    }
}