package com.coffeechain.dto.response;

public class CreateUserResponse {
    private String tenDangNhap;
    private String tenVaiTro;
    private Long maChiNhanh;

    public CreateUserResponse() {}

    public CreateUserResponse(String tenDangNhap, String tenVaiTro, Long maChiNhanh) {
        this.tenDangNhap = tenDangNhap;
        this.tenVaiTro = tenVaiTro;
        this.maChiNhanh = maChiNhanh;
    }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }
    public Long getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Long maChiNhanh) { this.maChiNhanh = maChiNhanh; }
}
