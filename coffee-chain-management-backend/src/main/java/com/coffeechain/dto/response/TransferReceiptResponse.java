package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Ket qua tao phieu dieu chuyen kho")
public class TransferReceiptResponse {
    @Schema(description = "Ma phieu dieu chuyen vua tao", example = "15")
    private Long maPhieuDieuChuyen;

    @Schema(description = "Ten kho nguon", example = "Kho tong Phung Loc")
    private String tenKhoNguon;

    @Schema(description = "Ten kho dich", example = "Kho Giga Mall")
    private String tenKhoDich;

    @Schema(description = "Trang thai phieu dieu chuyen", example = "COMPLETED")
    private String trangThai;

    @Schema(description = "So dong chi tiet", example = "2")
    private int soDongChiTiet;

    @Schema(description = "Thoi diem tao phieu")
    private LocalDateTime ngayDieuChuyen;

    public Long getMaPhieuDieuChuyen() {
        return maPhieuDieuChuyen;
    }

    public void setMaPhieuDieuChuyen(Long maPhieuDieuChuyen) {
        this.maPhieuDieuChuyen = maPhieuDieuChuyen;
    }

    public String getTenKhoNguon() {
        return tenKhoNguon;
    }

    public void setTenKhoNguon(String tenKhoNguon) {
        this.tenKhoNguon = tenKhoNguon;
    }

    public String getTenKhoDich() {
        return tenKhoDich;
    }

    public void setTenKhoDich(String tenKhoDich) {
        this.tenKhoDich = tenKhoDich;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoDongChiTiet() {
        return soDongChiTiet;
    }

    public void setSoDongChiTiet(int soDongChiTiet) {
        this.soDongChiTiet = soDongChiTiet;
    }

    public LocalDateTime getNgayDieuChuyen() {
        return ngayDieuChuyen;
    }

    public void setNgayDieuChuyen(LocalDateTime ngayDieuChuyen) {
        this.ngayDieuChuyen = ngayDieuChuyen;
    }
}