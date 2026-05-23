package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
    description =
        "DTO response InventoryHistoryResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class InventoryHistoryResponse {
  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ma nhat ky kho).",
      example = "1")
  private Long maNhatKyKho;

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

  @Schema(description = "Ma lo hang nguyen lieu", example = "1")
  private Long maLoHang;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (loai giao dich).",
      example = "Gia tri mau")
  private String loaiGiaoDich;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ten chung tu).",
      example = "Tên hiển thị mẫu")
  private String tenChungTu;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ma chung tu).",
      example = "1")
  private Long maChungTu;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so luong thay doi).",
      example = "100.5")
  private BigDecimal soLuongThayDoi;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so luong truoc).",
      example = "100.5")
  private BigDecimal soLuongTruoc;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so luong sau).",
      example = "100.5")
  private BigDecimal soLuongSau;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (thoi gian).",
      example = "2026-05-22T08:30:00")
  private LocalDateTime thoiGian;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ma nguoi thao tac).",
      example = "1")
  private Long maNguoiThaoTac;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ten nguoi thao tac).",
      example = "Tên hiển thị mẫu")
  private String tenNguoiThaoTac;

  public InventoryHistoryResponse() {}

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
      String tenNguoiThaoTac) {
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
