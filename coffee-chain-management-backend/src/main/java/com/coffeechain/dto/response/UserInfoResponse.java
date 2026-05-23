package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Thông tin người dùng đang đăng nhập")
public class UserInfoResponse {
  @Schema(description = "Mã người dùng", example = "1")
  private Long maNguoiDung;

  @Schema(description = "Tên đăng nhập", example = "admin")
  private String tenDangNhap;

  @Schema(description = "Mã vai trò", example = "1")
  private Long maVaiTro;

  @Schema(description = "Tên vai trò", example = "ADMIN")
  private String tenVaiTro;

  @Schema(description = "Mã chi nhánh của người dùng. Có thể null với ADMIN.", example = "1")
  private Long maChiNhanh;

  @Schema(
      description = "Tên chi nhánh của người dùng. Có thể null với ADMIN.",
      example = "Chi nhánh trung tâm")
  private String tenChiNhanh;

  @Schema(
      description = "Danh sách permission code của người dùng",
      example = "[\"USER:CREATE\", \"ROLE:VIEW\"]")
  private Set<String> permissions;

  @Schema(description = "Thời điểm token hết hạn", example = "2026-05-10T23:59:59")
  private LocalDateTime expiredAt;

  public UserInfoResponse() {}

  public UserInfoResponse(
      Long maNguoiDung,
      String tenDangNhap,
      Long maVaiTro,
      String tenVaiTro,
      Long maChiNhanh,
      String tenChiNhanh,
      Set<String> permissions,
      LocalDateTime expiredAt) {
    this.maNguoiDung = maNguoiDung;
    this.tenDangNhap = tenDangNhap;
    this.maVaiTro = maVaiTro;
    this.tenVaiTro = tenVaiTro;
    this.maChiNhanh = maChiNhanh;
    this.tenChiNhanh = tenChiNhanh;
    this.permissions = permissions;
    this.expiredAt = expiredAt;
  }

  public Long getMaNguoiDung() {
    return maNguoiDung;
  }

  public void setMaNguoiDung(Long maNguoiDung) {
    this.maNguoiDung = maNguoiDung;
  }

  public String getTenDangNhap() {
    return tenDangNhap;
  }

  public void setTenDangNhap(String tenDangNhap) {
    this.tenDangNhap = tenDangNhap;
  }

  public Long getMaVaiTro() {
    return maVaiTro;
  }

  public void setMaVaiTro(Long maVaiTro) {
    this.maVaiTro = maVaiTro;
  }

  public String getTenVaiTro() {
    return tenVaiTro;
  }

  public void setTenVaiTro(String tenVaiTro) {
    this.tenVaiTro = tenVaiTro;
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

  public Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }

  public LocalDateTime getExpiredAt() {
    return expiredAt;
  }

  public void setExpiredAt(LocalDateTime expiredAt) {
    this.expiredAt = expiredAt;
  }
}
