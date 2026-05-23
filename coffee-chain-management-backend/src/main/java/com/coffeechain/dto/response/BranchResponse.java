package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "DTO response BranchResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class BranchResponse {
  @Schema(description = "Ma chi nhanh lien quan", example = "1")
  private Long maChiNhanh;

  @Schema(description = "Ten chi nhanh hien thi tren giao dien", example = "Tên hiển thị mẫu")
  private String tenChiNhanh;

  @Schema(description = "Dia chi lien he/hoat dong", example = "Gia tri mau")
  private String diaChi;

  @Schema(description = "So dien thoai lien he", example = "Gia tri mau")
  private String soDienThoai;

  @Schema(description = "Ma kho lien quan den nghiep vu", example = "1")
  private Long maKho;

  @Schema(description = "Ten kho hien thi tren giao dien", example = "Kho tổng Phụng Lộc")
  private String tenKho;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (so nhan vien).",
      example = "1")
  private Integer soNhanVien;

  @Schema(description = "Trang thai hien tai cua ban ghi/nghiep vu", example = "ACTIVE")
  private String trangThai;

  public BranchResponse() {}

  public BranchResponse(
      Long maChiNhanh,
      String tenChiNhanh,
      String diaChi,
      String soDienThoai,
      Long maKho,
      String tenKho,
      Integer soNhanVien,
      String trangThai) {
    this.maChiNhanh = maChiNhanh;
    this.tenChiNhanh = tenChiNhanh;
    this.diaChi = diaChi;
    this.soDienThoai = soDienThoai;
    this.maKho = maKho;
    this.tenKho = tenKho;
    this.soNhanVien = soNhanVien;
    this.trangThai = trangThai;
  }

  public Long getMaChiNhanh() {
    return maChiNhanh;
  }

  public void setMaChiNhanh(Long maChiNhanh) {
    this.maChiNhanh = maChiNhanh;
  }

  public String getTenChiNhanh() {
    return tenChiNhanh;
  }

  public void setTenChiNhanh(String tenChiNhanh) {
    this.tenChiNhanh = tenChiNhanh;
  }

  public String getDiaChi() {
    return diaChi;
  }

  public void setDiaChi(String diaChi) {
    this.diaChi = diaChi;
  }

  public String getSoDienThoai() {
    return soDienThoai;
  }

  public void setSoDienThoai(String soDienThoai) {
    this.soDienThoai = soDienThoai;
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

  public Integer getSoNhanVien() {
    return soNhanVien;
  }

  public void setSoNhanVien(Integer soNhanVien) {
    this.soNhanVien = soNhanVien;
  }

  public String getTrangThai() {
    return trangThai;
  }

  public void setTrangThai(String trangThai) {
    this.trangThai = trangThai;
  }
}
