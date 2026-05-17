package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ket qua tao phieu xuat kho")
public class ExportReceiptResponse {
    @Schema(description = "Ma phieu xuat vua tao", example = "15")
    private Long maPhieuXuat;

    @Schema(description = "Ten kho xuat", example = "Kho tong Phung Loc")
    private String tenKho;

    @Schema(description = "Loai xuat", example = "TRAINING")
    private String loaiXuat;

    @Schema(description = "Trang thai phieu xuat", example = "COMPLETED")
    private String trangThai;

    @Schema(description = "Tong gia tri xuat", example = "950000")
    private BigDecimal tongGiaTriXuat;

    @Schema(description = "So dong chi tiet phieu xuat", example = "2")
    private int soDongChiTiet;

    @Schema(description = "Thoi diem tao phieu")
    private LocalDateTime ngayXuat;

    public Long getMaPhieuXuat() {
        return maPhieuXuat;
    }

    public void setMaPhieuXuat(Long maPhieuXuat) {
        this.maPhieuXuat = maPhieuXuat;
    }

    public String getTenKho() {
        return tenKho;
    }

    public void setTenKho(String tenKho) {
        this.tenKho = tenKho;
    }

    public String getLoaiXuat() {
        return loaiXuat;
    }

    public void setLoaiXuat(String loaiXuat) {
        this.loaiXuat = loaiXuat;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getTongGiaTriXuat() {
        return tongGiaTriXuat;
    }

    public void setTongGiaTriXuat(BigDecimal tongGiaTriXuat) {
        this.tongGiaTriXuat = tongGiaTriXuat;
    }

    public int getSoDongChiTiet() {
        return soDongChiTiet;
    }

    public void setSoDongChiTiet(int soDongChiTiet) {
        this.soDongChiTiet = soDongChiTiet;
    }

    public LocalDateTime getNgayXuat() {
        return ngayXuat;
    }

    public void setNgayXuat(LocalDateTime ngayXuat) {
        this.ngayXuat = ngayXuat;
    }
}
