package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Thong tin tong quan ton kho")
public class TongQuanTonKhoResponse {

  @Schema(description = "Tong so dong nguyen lieu ton kho", example = "25")
  private Long tongSoNguyenLieu;

  @Schema(description = "Tong so luong ton", example = "1250.5")
  private BigDecimal tongSoLuongTon;

  @Schema(description = "So nguyen lieu da het hang", example = "2")
  private Long soNguyenLieuHetHang;

  @Schema(description = "So nguyen lieu sap het hang hoac ton thap", example = "5")
  private Long soNguyenLieuTonThap;

  @Schema(description = "So nguyen lieu con ton o muc an toan", example = "18")
  private Long soNguyenLieuOnDinh;

  public Long getTongSoNguyenLieu() {
    return tongSoNguyenLieu;
  }

  public void setTongSoNguyenLieu(Long tongSoNguyenLieu) {
    this.tongSoNguyenLieu = tongSoNguyenLieu;
  }

  public BigDecimal getTongSoLuongTon() {
    return tongSoLuongTon;
  }

  public void setTongSoLuongTon(BigDecimal tongSoLuongTon) {
    this.tongSoLuongTon = tongSoLuongTon;
  }

  public Long getSoNguyenLieuHetHang() {
    return soNguyenLieuHetHang;
  }

  public void setSoNguyenLieuHetHang(Long soNguyenLieuHetHang) {
    this.soNguyenLieuHetHang = soNguyenLieuHetHang;
  }

  public Long getSoNguyenLieuTonThap() {
    return soNguyenLieuTonThap;
  }

  public void setSoNguyenLieuTonThap(Long soNguyenLieuTonThap) {
    this.soNguyenLieuTonThap = soNguyenLieuTonThap;
  }

  public Long getSoNguyenLieuOnDinh() {
    return soNguyenLieuOnDinh;
  }

  public void setSoNguyenLieuOnDinh(Long soNguyenLieuOnDinh) {
    this.soNguyenLieuOnDinh = soNguyenLieuOnDinh;
  }
}
