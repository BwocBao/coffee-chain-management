package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "DTO response WarehouseResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class WarehouseResponse {
  @Schema(description = "Ma kho lien quan den nghiep vu", example = "1")
  private Long maKho;

  @Schema(description = "Ten kho hien thi tren giao dien", example = "Kho tổng Phụng Lộc")
  private String tenKho;

  @Schema(description = "Loai kho: CENTRAL hoac BRANCH", example = "Gia tri mau")
  private String loaiKho;

  @Schema(description = "Ma chi nhanh lien quan", example = "1")
  private Long maChiNhanh;

  @Schema(description = "Ten chi nhanh hien thi tren giao dien", example = "Tên hiển thị mẫu")
  private String tenChiNhanh;

  @Schema(description = "Trang thai hien tai cua ban ghi/nghiep vu", example = "ACTIVE")
  private String trangThai;

  public WarehouseResponse() {}

  public WarehouseResponse(
      Long maKho,
      String tenKho,
      String loaiKho,
      Long maChiNhanh,
      String tenChiNhanh,
      String trangThai) {
    this.maKho = maKho;
    this.tenKho = tenKho;
    this.loaiKho = loaiKho;
    this.maChiNhanh = maChiNhanh;
    this.tenChiNhanh = tenChiNhanh;
    this.trangThai = trangThai;
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

  public String getTenChiNhanh() {
    return tenChiNhanh;
  }

  public void setTenChiNhanh(String tenChiNhanh) {
    this.tenChiNhanh = tenChiNhanh;
  }

  public String getTrangThai() {
    return trangThai;
  }

  public void setTrangThai(String trangThai) {
    this.trangThai = trangThai;
  }
}
