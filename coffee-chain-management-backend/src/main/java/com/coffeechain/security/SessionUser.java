package com.coffeechain.security;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class SessionUser {
    private Long maNguoiDung;
    private String tenDangNhap;
    private Long maVaiTro;
    private String tenVaiTro;
    private Long maChiNhanh;
    private String tenChiNhanh;
    private Set<String> permissions = new LinkedHashSet<>();
    private LocalDateTime expiredAt;

    public boolean hasPermission(String permission) {
        if (permission == null || permissions == null) return false;
        String normalized = permission.toUpperCase();
        return permissions.contains(normalized) || permissions.contains("ADMIN:*") || permissions.contains("*:*");
    }

    public Long getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Long maNguoiDung) { this.maNguoiDung = maNguoiDung; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public Long getMaVaiTro() { return maVaiTro; }
    public void setMaVaiTro(Long maVaiTro) { this.maVaiTro = maVaiTro; }
    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }
    public Long getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Long maChiNhanh) { this.maChiNhanh = maChiNhanh; }
    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
}
