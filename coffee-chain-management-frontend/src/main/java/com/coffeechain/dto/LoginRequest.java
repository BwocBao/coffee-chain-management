package com.coffeechain.dto;

/** Body gửi lên API /api/auth/login. */
public class LoginRequest {
  private String tenDangNhap;
  private String matKhau;

  public LoginRequest() {}

  public LoginRequest(String tenDangNhap, String matKhau) {
    this.tenDangNhap = tenDangNhap;
    this.matKhau = matKhau;
  }

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
}
