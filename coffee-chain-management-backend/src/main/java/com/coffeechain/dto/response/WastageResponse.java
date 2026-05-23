package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO response WastageResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class WastageResponse {
    @Schema(description = "Ma phieu hao hut", example = "1")
    private Long maPhieuHaoHut;
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
    @Schema(description = "Ma lo hang nguyen lieu", example = "1")
    private Long maLoHang;
    @Schema(description = "So luong hao hut", example = "100.5")
    private BigDecimal soLuongHaoHut;
    @Schema(description = "Loai hao hut: DAMAGED, EXPIRED, SPILL, LOST hoac OTHER", example = "Gia tri mau")
    private String loaiHaoHut;
    @Schema(description = "Gia tri $field trong response tra ve frontend (ngay hao hut).", example = "2026-05-22T08:30:00")
    private LocalDateTime ngayHaoHut;
    @Schema(description = "Ghi chu nghiep vu", example = "Gia tri mau")
    private String ghiChu;
    @Schema(description = "Gia tri $field trong response tra ve frontend (ma nguoi bao cao).", example = "1")
    private Long maNguoiBaoCao;
    @Schema(description = "Gia tri $field trong response tra ve frontend (ten nguoi bao cao).", example = "Tên hiển thị mẫu")
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
