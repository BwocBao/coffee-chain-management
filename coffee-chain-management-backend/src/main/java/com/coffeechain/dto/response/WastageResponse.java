package com.coffeechain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WastageResponse {
    private Long maPhieuHaoHut;
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private Long maLoHang;
    private BigDecimal soLuongHaoHut;
    private String loaiHaoHut;
    private LocalDateTime ngayHaoHut;
    private String ghiChu;
    private Long maNguoiBaoCao;
    private String tenNguoiBaoCao;

    public WastageResponse() {
    }

    public WastageResponse(
            Long maPhieuHaoHut,
            Long maKho,
            String tenKho,
            Long maNguyenLieu,
            String tenNguyenLieu,
            String donViTinh,
            Long maLoHang,
            BigDecimal soLuongHaoHut,
            String loaiHaoHut,
            LocalDateTime ngayHaoHut,
            String ghiChu,
            Long maNguoiBaoCao,
            String tenNguoiBaoCao
    ) {
        this.maPhieuHaoHut = maPhieuHaoHut;
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.maNguyenLieu = maNguyenLieu;
        this.tenNguyenLieu = tenNguyenLieu;
        this.donViTinh = donViTinh;
        this.maLoHang = maLoHang;
        this.soLuongHaoHut = soLuongHaoHut;
        this.loaiHaoHut = loaiHaoHut;
        this.ngayHaoHut = ngayHaoHut;
        this.ghiChu = ghiChu;
        this.maNguoiBaoCao = maNguoiBaoCao;
        this.tenNguoiBaoCao = tenNguoiBaoCao;
    }

    public Long getMaPhieuHaoHut() {
        return maPhieuHaoHut;
    }

    public void setMaPhieuHaoHut(Long maPhieuHaoHut) {
        this.maPhieuHaoHut = maPhieuHaoHut;
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

    public LocalDateTime getNgayHaoHut() {
        return ngayHaoHut;
    }

    public void setNgayHaoHut(LocalDateTime ngayHaoHut) {
        this.ngayHaoHut = ngayHaoHut;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Long getMaNguoiBaoCao() {
        return maNguoiBaoCao;
    }

    public void setMaNguoiBaoCao(Long maNguoiBaoCao) {
        this.maNguoiBaoCao = maNguoiBaoCao;
    }

    public String getTenNguoiBaoCao() {
        return tenNguoiBaoCao;
    }

    public void setTenNguoiBaoCao(String tenNguoiBaoCao) {
        this.tenNguoiBaoCao = tenNguoiBaoCao;
    }
}