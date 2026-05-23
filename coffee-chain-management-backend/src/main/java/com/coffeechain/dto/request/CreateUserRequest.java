package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request tạo tài khoản người dùng mới")
public class CreateUserRequest {
  @Schema(
      description = "Tên đăng nhập, không được trùng với tài khoản khác",
      example = "thungan01",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Tên đăng nhập không được trống")
  private String tenDangNhap;

  @Schema(
      description = "Mật khẩu ban đầu của tài khoản, tối thiểu 6 ký tự",
      example = "123456",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Mật khẩu không được trống")
  @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
  private String matKhau;

  @Schema(
      description = "Email của người dùng, không được trùng",
      example = "thungan01@phungloccoffee.vn",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Email không được trống")
  @Email(message = "Email không đúng định dạng")
  private String email;

  @Schema(
      description = "Tên vai trò cần gán cho tài khoản",
      example = "THU_NGAN",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Vai trò không được trống")
  private String tenVaiTro;

  @Schema(
      description = "Mã chi nhánh. Bắt buộc với tài khoản không phải ADMIN; ADMIN có thể để null.",
      example = "1")
  private Long maChiNhanh;

  public String getTenDangNhap() {
    return tenDangNhap;
  }

  public void setTenDangNhap(String tenDangNhap) {
    this.tenDangNhap = tenDangNhap;
  }

  public String getMatKhau() {
    return matKhau;
  }

  public void setMatKhau(String matKhau) {
    this.matKhau = matKhau;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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
}
