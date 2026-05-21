package com.coffeechain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryHistoryResponse {
    private Long maNhatKyKho;

    private Long maKho;
    private String tenKho;

    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;

    private Long maLoHang;

    private String loaiGiaoDich;
    private String tenChungTu;
    private Long maChungTu;

    private BigDecimal soLuongThayDoi;
    private BigDecimal soLuongTruoc;
    private BigDecimal soLuongSau;

    private LocalDateTime thoiGian;

    private Long maNguoiThaoTac;
    private String tenNguoiThaoTac;

    public InventoryHistoryResponse() {
    }

    public InventoryHistoryResponse(
            Long maNhatKyKho,
            Long maKho,
            String tenKho,
            Long maNguyenLieu,
            String tenNguyenLieu,
            String donViTinh,
            Long maLoHang,
            String loaiGiaoDich,
            String tenChungTu,
            Long maChungTu,
            BigDecimal soLuongThayDoi,
            BigDecimal soLuongTruoc,
            BigDecimal soLuongSau,
            LocalDateTime thoiGian,
            Long maNguoiThaoTac,
            String tenNguoiThaoTac
    ) {
        this.maNhatKyKho = maNhatKyKho;
        this.maKho = maKho;
        this.tenKho = tenKho;
        this.maNguyenLieu = maNguyenLieu;
        this.tenNguyenLieu = tenNguyenLieu;
        this.donViTinh = donViTinh;
        this.maLoHang = maLoHang;
        this.loaiGiaoDich = loaiGiaoDich;
        this.tenChungTu = tenChungTu;
        this.maChungTu = maChungTu;
        this.soLuongThayDoi = soLuongThayDoi;
        this.soLuongTruoc = soLuongTruoc;
        this.soLuongSau = soLuongSau;
        this.thoiGian = thoiGian;
        this.maNguoiThaoTac = maNguoiThaoTac;
        this.tenNguoiThaoTac = tenNguoiThaoTac;
    }

    public Long getMaNhatKyKho() {
        return maNhatKyKho;
    }

    public void setMaNhatKyKho(Long maNhatKyKho) {
        this.maNhatKyKho = maNhatKyKho;
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

    public String getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public void setLoaiGiaoDich(String loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public String getTenChungTu() {
        return tenChungTu;
    }

    public void setTenChungTu(String tenChungTu) {
        this.tenChungTu = tenChungTu;
    }

    public Long getMaChungTu() {
        return maChungTu;
    }

    public void setMaChungTu(Long maChungTu) {
        this.maChungTu = maChungTu;
    }

    public BigDecimal getSoLuongThayDoi() {
        return soLuongThayDoi;
    }

    public void setSoLuongThayDoi(BigDecimal soLuongThayDoi) {
        this.soLuongThayDoi = soLuongThayDoi;
    }

    public BigDecimal getSoLuongTruoc() {
        return soLuongTruoc;
    }

    public void setSoLuongTruoc(BigDecimal soLuongTruoc) {
        this.soLuongTruoc = soLuongTruoc;
    }

    public BigDecimal getSoLuongSau() {
        return soLuongSau;
    }

    public void setSoLuongSau(BigDecimal soLuongSau) {
        this.soLuongSau = soLuongSau;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public Long getMaNguoiThaoTac() {
        return maNguoiThaoTac;
    }

    public void setMaNguoiThaoTac(Long maNguoiThaoTac) {
        this.maNguoiThaoTac = maNguoiThaoTac;
    }

    public String getTenNguoiThaoTac() {
        return tenNguoiThaoTac;
    }

    public void setTenNguoiThaoTac(String tenNguoiThaoTac) {
        this.tenNguoiThaoTac = tenNguoiThaoTac;
    }
}