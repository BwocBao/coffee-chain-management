package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "DTO response ExpiryStatisticsResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class ExpiryStatisticsResponse {
  @Schema(
      description = "Gia tri $field trong response tra ve frontend (tong so lo).",
      example = "1")
  private Integer tongSoLo;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so lo dang hoat dong).",
      example = "1")
  private Integer soLoDangHoatDong;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so lo sap het han).",
      example = "1")
  private Integer soLoSapHetHan;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so lo da het han).",
      example = "1")
  private Integer soLoDaHetHan;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so lo da dung het).",
      example = "1")
  private Integer soLoDaDungHet;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so lo khong co han su dung).",
      example = "1")
  private Integer soLoKhongCoHanSuDung;

  public Integer getTongSoLo() {
    return tongSoLo;
  }

  public void setTongSoLo(Integer tongSoLo) {
    this.tongSoLo = tongSoLo;
  }

  public Integer getSoLoDangHoatDong() {
    return soLoDangHoatDong;
  }

  public void setSoLoDangHoatDong(Integer soLoDangHoatDong) {
    this.soLoDangHoatDong = soLoDangHoatDong;
  }

  public Integer getSoLoSapHetHan() {
    return soLoSapHetHan;
  }

  public void setSoLoSapHetHan(Integer soLoSapHetHan) {
    this.soLoSapHetHan = soLoSapHetHan;
  }

  public Integer getSoLoDaHetHan() {
    return soLoDaHetHan;
  }

  public void setSoLoDaHetHan(Integer soLoDaHetHan) {
    this.soLoDaHetHan = soLoDaHetHan;
  }

  public Integer getSoLoDaDungHet() {
    return soLoDaDungHet;
  }

  public void setSoLoDaDungHet(Integer soLoDaDungHet) {
    this.soLoDaDungHet = soLoDaDungHet;
  }

  public Integer getSoLoKhongCoHanSuDung() {
    return soLoKhongCoHanSuDung;
  }

  public void setSoLoKhongCoHanSuDung(Integer soLoKhongCoHanSuDung) {
    this.soLoKhongCoHanSuDung = soLoKhongCoHanSuDung;
  }
}
