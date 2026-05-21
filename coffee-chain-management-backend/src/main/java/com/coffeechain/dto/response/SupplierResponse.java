package com.coffeechain.dto.response;

public class SupplierResponse {
    private Long maNhaCungCap;
    private String tenNhaCungCap;
    private String soDienThoai;
    private String email;
    private String diaChi;

    public SupplierResponse() {
    }

    public SupplierResponse(
            Long maNhaCungCap,
            String tenNhaCungCap,
            String soDienThoai,
            String email,
            String diaChi
    ) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
    }

    public Long getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(Long maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}