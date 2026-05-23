package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    description =
        "DTO request CreateWastageRequest. Swagger hien thi cac field frontend can gui len backend.")
public class CreateWastageRequest {
  @Schema(description = "Ma kho lien quan den nghiep vu", example = "1")
  private Long maKho;

  @Schema(description = "Ma nguyen lieu", example = "1")
  private Long maNguyenLieu;

  @Schema(description = "Ma lo hang nguyen lieu", example = "1")
  private Long maLoHang;

  @Schema(description = "So luong hao hut", example = "100.5")
  private BigDecimal soLuongHaoHut;

  @Schema(
      description = "Loai hao hut: DAMAGED, EXPIRED, SPILL, LOST hoac OTHER",
      example = "Gia tri mau")
  private String loaiHaoHut;

  @Schema(description = "Ghi chu nghiep vu", example = "Gia tri mau")
  private String ghiChu;

  public Long getMaKho() {
    return maKho;
  }

  public void setMaKho(Long maKho) {
    this.maKho = maKho;
  }

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

  public String getGhiChu() {
    return ghiChu;
  }

  public void setGhiChu(String ghiChu) {
    this.ghiChu = ghiChu;
  }
}
