package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(
    description =
        "DTO response StocktakeResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class StocktakeResponse {
  @Schema(description = "Ma phieu kiem kho", example = "1")
  private Long maPhieuKiemKho;

  @Schema(description = "Ma kho lien quan den nghiep vu", example = "1")
  private Long maKho;

  @Schema(description = "Ten kho hien thi tren giao dien", example = "Kho tổng Phụng Lộc")
  private String tenKho;

  @Schema(description = "Ngay kiem kho", example = "2026-05-22T08:30:00")
  private LocalDateTime ngayKiemKho;

  @Schema(description = "Ma nguoi kiem kho", example = "1")
  private Long maNguoiKiem;

  @Schema(description = "Ten nguoi kiem kho", example = "Tên hiển thị mẫu")
  private String tenNguoiKiem;

  @Schema(description = "Trang thai hien tai cua ban ghi/nghiep vu", example = "ACTIVE")
  private String trangThai;

  @Schema(description = "Ghi chu nghiep vu", example = "Gia tri mau")
  private String ghiChu;

  @Schema(description = "So dong chi tiet trong phieu", example = "1")
  private Integer soDongChiTiet;

  @Schema(description = "Danh sach dong chi tiet cua phieu")
  private List<StocktakeItemResponse> items = new ArrayList<>();

  public StocktakeResponse() {}

  public StocktakeResponse(
      Long maPhieuKiemKho,
      Long maKho,
      String tenKho,
      LocalDateTime ngayKiemKho,
      Long maNguoiKiem,
      String tenNguoiKiem,
      String trangThai,
      String ghiChu,
      Integer soDongChiTiet) {
    this.maPhieuKiemKho = maPhieuKiemKho;
    this.maKho = maKho;
    this.tenKho = tenKho;
    this.ngayKiemKho = ngayKiemKho;
    this.maNguoiKiem = maNguoiKiem;
    this.tenNguoiKiem = tenNguoiKiem;
    this.trangThai = trangThai;
    this.ghiChu = ghiChu;
    this.soDongChiTiet = soDongChiTiet;
  }

  public Long getMaPhieuKiemKho() {
    return maPhieuKiemKho;
  }

  public void setMaPhieuKiemKho(Long maPhieuKiemKho) {
    this.maPhieuKiemKho = maPhieuKiemKho;
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

  public LocalDateTime getNgayKiemKho() {
    return ngayKiemKho;
  }

  public void setNgayKiemKho(LocalDateTime ngayKiemKho) {
    this.ngayKiemKho = ngayKiemKho;
  }

  public Long getMaNguoiKiem() {
    return maNguoiKiem;
  }

  public void setMaNguoiKiem(Long maNguoiKiem) {
    this.maNguoiKiem = maNguoiKiem;
  }

  public String getTenNguoiKiem() {
    return tenNguoiKiem;
  }

  public void setTenNguoiKiem(String tenNguoiKiem) {
    this.tenNguoiKiem = tenNguoiKiem;
  }

  public String getTrangThai() {
    return trangThai;
  }

  public void setTrangThai(String trangThai) {
    this.trangThai = trangThai;
  }

  public String getGhiChu() {
    return ghiChu;
  }

  public void setGhiChu(String ghiChu) {
    this.ghiChu = ghiChu;
  }

  public Integer getSoDongChiTiet() {
    return soDongChiTiet;
  }

  public void setSoDongChiTiet(Integer soDongChiTiet) {
    this.soDongChiTiet = soDongChiTiet;
  }

  public List<StocktakeItemResponse> getItems() {
    return items;
  }

  public void setItems(List<StocktakeItemResponse> items) {
    this.items = items;
  }
}
