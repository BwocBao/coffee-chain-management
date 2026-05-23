package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO response WastageLotResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class WastageLotResponse {
    @Schema(description = "Ma lo hang nguyen lieu", example = "1")
    private Long maLoHang;
    @Schema(description = "Ma kho lien quan den nghiep vu", example = "1")
    private Long maKho;
    @Schema(description = "Ten kho hien thi tren giao dien", example = "Kho tổng Phụng Lộc")
    private String tenKho;
    @Schema(description = "Ma nguyen lieu", example = "1")
    private Long maNguyenLieu;
    @Schema(description = "Ten nguyen lieu", example = "Cà phê hạt Arabica")
    private String tenNguyenLieu;
    @Schema(description = "Gia tri $field trong response tra ve frontend (don vi tinh).", example = "Gia tri mau")
    private String donViTinh;
    @Schema(description = "So luong con lai", example = "100.5")
    private BigDecimal soLuongConLai;
    @Schema(description = "Han su dung cua lo hang", example = "2026-05-22")
    private LocalDate hanSuDung;
    @Schema(description = "Trang thai hien tai cua ban ghi/nghiep vu", example = "ACTIVE")
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
