package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thong tin ton kho theo lo hang nguyen lieu")
public class BatchInventoryResponse {

    @Schema(description = "Ma lo hang", example = "10")
    private Long maLoHang;

    @Schema(description = "Ma kho", example = "1")
    private Long maKho;

    @Schema(description = "Ten kho", example = "Kho tong Phung Loc")
    private String tenKho;

    @Schema(description = "Ma nguyen lieu", example = "5")
    private Long maNguyenLieu;

    @Schema(description = "Ten nguyen lieu", example = "Sua tuoi")
    private String tenNguyenLieu;

    @Schema(description = "Ky hieu don vi tinh", example = "L")
    private String kyHieu;

    @Schema(description = "So luong con lai trong lo", example = "20.5")
    private BigDecimal soLuongConLai;

    @Schema(description = "Trang thai lo hang", example = "ACTIVE")
    private String trangThai;

    @Schema(description = "Han su dung cua lo hang")
    private LocalDate hanSuDung;

    @Schema(description = "Ngay tao lo hang")
    private LocalDateTime ngayTao;

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

    public String getKyHieu() {
        return kyHieu;
    }

    public void setKyHieu(String kyHieu) {
        this.kyHieu = kyHieu;
    }

    public BigDecimal getSoLuongConLai() {
        return soLuongConLai;
    }

    public void setSoLuongConLai(BigDecimal soLuongConLai) {
        this.soLuongConLai = soLuongConLai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
