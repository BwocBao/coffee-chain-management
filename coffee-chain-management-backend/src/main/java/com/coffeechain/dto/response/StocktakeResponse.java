package com.coffeechain.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StocktakeResponse {
    private Long maPhieuKiemKho;
    private Long maKho;
    private String tenKho;
    private LocalDateTime ngayKiemKho;
    private Long maNguoiKiem;
    private String tenNguoiKiem;
    private String trangThai;
    private String ghiChu;
    private Integer soDongChiTiet;
    private List<StocktakeItemResponse> items = new ArrayList<>();

    public StocktakeResponse() {
    }

    public StocktakeResponse(
            Long maPhieuKiemKho,
            Long maKho,
            String tenKho,
            LocalDateTime ngayKiemKho,
            Long maNguoiKiem,
            String tenNguoiKiem,
            String trangThai,
            String ghiChu,
            Integer soDongChiTiet
    ) {
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