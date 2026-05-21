package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Thong tin ton kho theo kho va nguyen lieu")
public class InventoryResponse {

    @Schema(description = "Ma ton kho", example = "1")
    private Long maTonKho;

    @Schema(description = "Ma kho", example = "1")
    private Long maKho;

    @Schema(description = "Ten kho", example = "Kho tong Phung Loc")
    private String tenKho;

    @Schema(description = "Loai kho", example = "CENTRAL")
    private String loaiKho;

    @Schema(description = "Ma chi nhanh neu la kho chi nhanh", example = "2")
    private Long maChiNhanh;

    @Schema(description = "Ma nguyen lieu", example = "5")
    private Long maNguyenLieu;

    @Schema(description = "Ten nguyen lieu", example = "Sua tuoi")
    private String tenNguyenLieu;

    @Schema(description = "Ten don vi tinh", example = "Lit")
    private String tenDonViTinh;

    @Schema(description = "Ky hieu don vi tinh", example = "L")
    private String kyHieu;

    @Schema(description = "So luong ton hien tai", example = "35.5")
    private BigDecimal soLuongTon;

    @Schema(description = "Muc ton toi thieu cua nguyen lieu", example = "10")
    private BigDecimal mucTonToiThieu;

    @Schema(description = "Trang thai ton kho", example = "OK")
    private String trangThaiTonKho;

    @Schema(description = "Lan cap nhat cuoi")
    private LocalDateTime lanCapNhatCuoi;

    public Long getMaTonKho() {
        return maTonKho;
    }

    public void setMaTonKho(Long maTonKho) {
        this.maTonKho = maTonKho;
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

    public String getLoaiKho() {
        return loaiKho;
    }

    public void setLoaiKho(String loaiKho) {
        this.loaiKho = loaiKho;
    }

    public Long getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Long maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
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

    public String getTenDonViTinh() {
        return tenDonViTinh;
    }

    public void setTenDonViTinh(String tenDonViTinh) {
        this.tenDonViTinh = tenDonViTinh;
    }

    public String getKyHieu() {
        return kyHieu;
    }

    public void setKyHieu(String kyHieu) {
        this.kyHieu = kyHieu;
    }

    public BigDecimal getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(BigDecimal soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public BigDecimal getMucTonToiThieu() {
        return mucTonToiThieu;
    }

    public void setMucTonToiThieu(BigDecimal mucTonToiThieu) {
        this.mucTonToiThieu = mucTonToiThieu;
    }

    public String getTrangThaiTonKho() {
        return trangThaiTonKho;
    }

    public void setTrangThaiTonKho(String trangThaiTonKho) {
        this.trangThaiTonKho = trangThaiTonKho;
    }

    public LocalDateTime getLanCapNhatCuoi() {
        return lanCapNhatCuoi;
    }

    public void setLanCapNhatCuoi(LocalDateTime lanCapNhatCuoi) {
        this.lanCapNhatCuoi = lanCapNhatCuoi;
    }
}