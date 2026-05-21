package com.coffeechain.dto.response;

public class RoleResponse {
    private Long maVaiTro;
    private String tenVaiTro;

    public RoleResponse() {}
    public RoleResponse(Long maVaiTro, String tenVaiTro) {
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
    }
    public Long getMaVaiTro() { return maVaiTro; }
    public void setMaVaiTro(Long maVaiTro) { this.maVaiTro = maVaiTro; }
    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }
}
