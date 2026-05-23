package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    description =
        "DTO response StocktakeItemResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class StocktakeItemResponse {
  @Schema(
      description = "Gia tri $field trong response tra ve frontend (ma ct phieu kiem kho).",
      example = "1")
  private Long maCtPhieuKiemKho;

  @Schema(description = "Ma phieu kiem kho", example = "1")
  private Long maPhieuKiemKho;

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

  @Schema(description = "So luong ton theo he thong", example = "100.5")
  private BigDecimal soLuongHeThong;

  @Schema(description = "So luong thuc te kiem dem", example = "100.5")
  private BigDecimal soLuongThucTe;

  @Schema(description = "So luong chenh lech giua thuc te va he thong", example = "100.5")
  private BigDecimal soLuongChenhLech;

  @Schema(description = "Ty le chenh lech", example = "100.5")
  private BigDecimal tyLeChenhLech;

  @Schema(description = "Ly do chenh lech", example = "Gia tri mau")
  private String lyDoChenhLech;

  @Schema(description = "Huong xu ly dong kiem kho", example = "Gia tri mau")
  private String huongXuLy;

  public StocktakeItemResponse() {}

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
      String huongXuLy) {
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
