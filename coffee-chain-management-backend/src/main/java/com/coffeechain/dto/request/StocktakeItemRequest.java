package com.coffeechain.dto.request;

import java.math.BigDecimal;

public class StocktakeItemRequest {
    private Long maNguyenLieu;
    private Long maLoHang;
    private BigDecimal soLuongHeThong;
    private BigDecimal soLuongThucTe;
    private String lyDoChenhLech;
    private String huongXuLy;

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