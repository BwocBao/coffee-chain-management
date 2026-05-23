package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ket qua tao phieu nhap kho")
public class ImportReceiptResponse {
  @Schema(description = "Ma phieu nhap vua tao", example = "10")
  private Long maPhieuNhap;

  @Schema(description = "Ten kho nhap", example = "Kho tong Phung Loc")
  private String tenKho;

  @Schema(description = "Ten nha cung cap", example = "NCC Ca Phe Cao Nguyen")
  private String tenNhaCungCap;

  @Schema(description = "Tong tien phieu nhap", example = "1200000")
  private BigDecimal tongTien;

  @Schema(description = "So dong chi tiet trong phieu", example = "3")
  private int soDongChiTiet;

  @Schema(description = "Thoi diem tao phieu")
  private LocalDateTime ngayNhap;

  public Long getMaPhieuNhap() {
    return maPhieuNhap;
  }

  public void setMaPhieuNhap(Long maPhieuNhap) {
    this.maPhieuNhap = maPhieuNhap;
  }

  public String getTenKho() {
    return tenKho;
  }

  public void setTenKho(String tenKho) {
    this.tenKho = tenKho;
  }

  public String getTenNhaCungCap() {
    return tenNhaCungCap;
  }

  public void setTenNhaCungCap(String tenNhaCungCap) {
    this.tenNhaCungCap = tenNhaCungCap;
  }

  public BigDecimal getTongTien() {
    return tongTien;
  }

  public void setTongTien(BigDecimal tongTien) {
    this.tongTien = tongTien;
  }

  public int getSoDongChiTiet() {
    return soDongChiTiet;
  }

  public void setSoDongChiTiet(int soDongChiTiet) {
    this.soDongChiTiet = soDongChiTiet;
  }

  public LocalDateTime getNgayNhap() {
    return ngayNhap;
  }

  public void setNgayNhap(LocalDateTime ngayNhap) {
    this.ngayNhap = ngayNhap;
  }
}
