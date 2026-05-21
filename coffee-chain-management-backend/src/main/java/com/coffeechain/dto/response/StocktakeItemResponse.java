package com.coffeechain.dto.response;

import java.math.BigDecimal;

public class StocktakeItemResponse {
    private Long maCtPhieuKiemKho;
    private Long maPhieuKiemKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private Long maLoHang;
    private BigDecimal soLuongHeThong;
    private BigDecimal soLuongThucTe;
    private BigDecimal soLuongChenhLech;
    private BigDecimal tyLeChenhLech;
    private String lyDoChenhLech;
    private String huongXuLy;

    public StocktakeItemResponse() {
    }

    public StocktakeItemResponse(
            Long maCtPhieuKiemKho,
            Long maPhieuKiemKho,
            Long maNguyenLieu,
            String tenNguyenLieu,
            String donViTinh,
            Long maLoHang,
            BigDecimal soLuongHeThong,
            BigDecimal soLuongThucTe,
            BigDecimal soLuongChenhLech,
            BigDecimal tyLeChenhLech,
            String lyDoChenhLech,
            String huongXuLy
    ) {
        this.maCtPhieuKiemKho = maCtPhieuKiemKho;
        this.maPhieuKiemKho = maPhieuKiemKho;
        this.maNguyenLieu = maNguyenLieu;
        this.tenNguyenLieu = tenNguyenLieu;
        this.donViTinh = donViTinh;
        this.maLoHang = maLoHang;
        this.soLuongHeThong = soLuongHeThong;
        this.soLuongThucTe = soLuongThucTe;
        this.soLuongChenhLech = soLuongChenhLech;
        this.tyLeChenhLech = tyLeChenhLech;
        this.lyDoChenhLech = lyDoChenhLech;
        this.huongXuLy = huongXuLy;
    }

    public Long getMaCtPhieuKiemKho() {
        return maCtPhieuKiemKho;
    }

    public void setMaCtPhieuKiemKho(Long maCtPhieuKiemKho) {
        this.maCtPhieuKiemKho = maCtPhieuKiemKho;
    }

    public Long getMaPhieuKiemKho() {
        return maPhieuKiemKho;
    }

    public void setMaPhieuKiemKho(Long maPhieuKiemKho) {
        this.maPhieuKiemKho = maPhieuKiemKho;
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

    public BigDecimal getSoLuongHeThong() {
        return soLuongHeThong;
    }

    public void setSoLuongHeThong(BigDecimal soLuongHeThong) {
        this.soLuongHeThong = soLuongHeThong;
    }

    public BigDecimal getSoLuongThucTe() {
        return soLuongThucTe;
    }

    public void setSoLuongThucTe(BigDecimal soLuongThucTe) {
        this.soLuongThucTe = soLuongThucTe;
    }

    public BigDecimal getSoLuongChenhLech() {
        return soLuongChenhLech;
    }

    public void setSoLuongChenhLech(BigDecimal soLuongChenhLech) {
        this.soLuongChenhLech = soLuongChenhLech;
    }

    public BigDecimal getTyLeChenhLech() {
        return tyLeChenhLech;
    }

    public void setTyLeChenhLech(BigDecimal tyLeChenhLech) {
        this.tyLeChenhLech = tyLeChenhLech;
    }

    public String getLyDoChenhLech() {
        return lyDoChenhLech;
    }

    public void setLyDoChenhLech(String lyDoChenhLech) {
        this.lyDoChenhLech = lyDoChenhLech;
    }

    public String getHuongXuLy() {
        return huongXuLy;
    }

    public void setHuongXuLy(String huongXuLy) {
        this.huongXuLy = huongXuLy;
    }
}