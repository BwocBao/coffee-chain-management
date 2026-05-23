package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    description =
        "DTO response InventoryHistorySummaryResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class InventoryHistorySummaryResponse {
  @Schema(description = "Ma kho lien quan den nghiep vu", example = "1")
  private Long maKho;

  @Schema(description = "Ten kho hien thi tren giao dien", example = "Kho tổng Phụng Lộc")
  private String tenKho;

  @Schema(description = "Ma nguyen lieu", example = "1")
  private Long maNguyenLieu;

  @Schema(description = "Ten nguyen lieu", example = "Cà phê hạt Arabica")
  private String tenNguyenLieu;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (don vi tinh).",
      example = "Gia tri mau")
  private String donViTinh;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong nhap).",
      example = "100.5")
  private BigDecimal tongNhap;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong xuat).",
      example = "100.5")
  private BigDecimal tongXuat;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong dieu chuyen vao).",
      example = "100.5")
  private BigDecimal tongDieuChuyenVao;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong dieu chuyen ra).",
      example = "100.5")
  private BigDecimal tongDieuChuyenRa;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong hao hut).",
      example = "100.5")
  private BigDecimal tongHaoHut;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong ban hang tru kho).",
      example = "100.5")
  private BigDecimal tongBanHangTruKho;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong hoan tru kho).",
      example = "100.5")
  private BigDecimal tongHoanTruKho;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong dieu chinh kiem kho).",
      example = "100.5")
  private BigDecimal tongDieuChinhKiemKho;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (bien dong rong).",
      example = "100.5")
  private BigDecimal bienDongRong;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so giao dich).",
      example = "1")
  private Integer soGiaoDich;

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

  public BigDecimal getTongNhap() {
    return tongNhap;
  }

  public void setTongNhap(BigDecimal tongNhap) {
    this.tongNhap = tongNhap;
  }

  public BigDecimal getTongXuat() {
    return tongXuat;
  }

  public void setTongXuat(BigDecimal tongXuat) {
    this.tongXuat = tongXuat;
  }

  public BigDecimal getTongDieuChuyenVao() {
    return tongDieuChuyenVao;
  }

  public void setTongDieuChuyenVao(BigDecimal tongDieuChuyenVao) {
    this.tongDieuChuyenVao = tongDieuChuyenVao;
  }

  public BigDecimal getTongDieuChuyenRa() {
    return tongDieuChuyenRa;
  }

  public void setTongDieuChuyenRa(BigDecimal tongDieuChuyenRa) {
    this.tongDieuChuyenRa = tongDieuChuyenRa;
  }

  public BigDecimal getTongHaoHut() {
    return tongHaoHut;
  }

  public void setTongHaoHut(BigDecimal tongHaoHut) {
    this.tongHaoHut = tongHaoHut;
  }

  public BigDecimal getTongBanHangTruKho() {
    return tongBanHangTruKho;
  }

  public void setTongBanHangTruKho(BigDecimal tongBanHangTruKho) {
    this.tongBanHangTruKho = tongBanHangTruKho;
  }

  public BigDecimal getTongHoanTruKho() {
    return tongHoanTruKho;
  }

  public void setTongHoanTruKho(BigDecimal tongHoanTruKho) {
    this.tongHoanTruKho = tongHoanTruKho;
  }

  public BigDecimal getTongDieuChinhKiemKho() {
    return tongDieuChinhKiemKho;
  }

  public void setTongDieuChinhKiemKho(BigDecimal tongDieuChinhKiemKho) {
    this.tongDieuChinhKiemKho = tongDieuChinhKiemKho;
  }

  public BigDecimal getBienDongRong() {
    return bienDongRong;
  }

  public void setBienDongRong(BigDecimal bienDongRong) {
    this.bienDongRong = bienDongRong;
  }

  public Integer getSoGiaoDich() {
    return soGiaoDich;
  }

  public void setSoGiaoDich(Integer soGiaoDich) {
    this.soGiaoDich = soGiaoDich;
  }
}
