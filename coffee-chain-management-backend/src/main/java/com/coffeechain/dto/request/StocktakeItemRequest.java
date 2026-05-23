package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO request StocktakeItemRequest. Swagger hien thi cac field frontend can gui len backend.")
public class StocktakeItemRequest {
    @Schema(description = "Ma nguyen lieu", example = "1")
    private Long maNguyenLieu;
    @Schema(description = "Ma lo hang nguyen lieu", example = "1")
    private Long maLoHang;
    @Schema(description = "So luong ton theo he thong", example = "100.5")
    private BigDecimal soLuongHeThong;
    @Schema(description = "So luong thuc te kiem dem", example = "100.5")
    private BigDecimal soLuongThucTe;
    @Schema(description = "Ly do chenh lech", example = "Gia tri mau")
    private String lyDoChenhLech;
    @Schema(description = "Huong xu ly dong kiem kho", example = "Gia tri mau")
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
