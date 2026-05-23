package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO response CreateUserResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class CreateUserResponse {
    @Schema(description = "Ten dang nhap", example = "Tên hiển thị mẫu")
    private String tenDangNhap;
    @Schema(description = "Gia tri $field trong response tra ve frontend (ten vai tro).", example = "Tên hiển thị mẫu")
    private String tenVaiTro;
    @Schema(description = "Ma chi nhanh lien quan", example = "1")
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
