package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO response BranchStatisticsResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class BranchStatisticsResponse {
    @Schema(description = "Gia tri $field trong response tra ve frontend (tong so chi nhanh).", example = "1")
    private Integer tongSoChiNhanh;
    @Schema(description = "Gia tri $field trong response tra ve frontend (so chi nhanh dang hoat dong).", example = "1")
    private Integer soChiNhanhDangHoatDong;
    @Schema(description = "Gia tri $field trong response tra ve frontend (so chi nhanh da dong).", example = "1")
    private Integer soChiNhanhDaDong;
    @Schema(description = "Gia tri $field trong response tra ve frontend (so chi nhanh bao tri).", example = "1")
    private Integer soChiNhanhBaoTri;
    @Schema(description = "Gia tri $field trong response tra ve frontend (ma chi nhanh nhieu nhan vien nhat).", example = "1")
    private Long maChiNhanhNhieuNhanVienNhat;
    @Schema(description = "Gia tri $field trong response tra ve frontend (ten chi nhanh nhieu nhan vien nhat).", example = "Tên hiển thị mẫu")
    private String tenChiNhanhNhieuNhanVienNhat;
    @Schema(description = "Gia tri $field trong response tra ve frontend (so nhan vien nhieu nhat).", example = "1")
    private Integer soNhanVienNhieuNhat;

    public Integer getTongSoChiNhanh() {
        return tongSoChiNhanh;
    }

    public void setTongSoChiNhanh(Integer tongSoChiNhanh) {
        this.tongSoChiNhanh = tongSoChiNhanh;
    }

    public Integer getSoChiNhanhDangHoatDong() {
        return soChiNhanhDangHoatDong;
    }

    public void setSoChiNhanhDangHoatDong(Integer soChiNhanhDangHoatDong) {
        this.soChiNhanhDangHoatDong = soChiNhanhDangHoatDong;
    }

    public Integer getSoChiNhanhDaDong() {
        return soChiNhanhDaDong;
    }

    public void setSoChiNhanhDaDong(Integer soChiNhanhDaDong) {
        this.soChiNhanhDaDong = soChiNhanhDaDong;
    }

    public Integer getSoChiNhanhBaoTri() {
        return soChiNhanhBaoTri;
    }

    public void setSoChiNhanhBaoTri(Integer soChiNhanhBaoTri) {
        this.soChiNhanhBaoTri = soChiNhanhBaoTri;
    }

    public Long getMaChiNhanhNhieuNhanVienNhat() {
        return maChiNhanhNhieuNhanVienNhat;
    }

    public void setMaChiNhanhNhieuNhanVienNhat(Long maChiNhanhNhieuNhanVienNhat) {
        this.maChiNhanhNhieuNhanVienNhat = maChiNhanhNhieuNhanVienNhat;
    }

    public String getTenChiNhanhNhieuNhanVienNhat() {
        return tenChiNhanhNhieuNhanVienNhat;
    }

    public void setTenChiNhanhNhieuNhanVienNhat(String tenChiNhanhNhieuNhanVienNhat) {
        this.tenChiNhanhNhieuNhanVienNhat = tenChiNhanhNhieuNhanVienNhat;
    }

    public Integer getSoNhanVienNhieuNhat() {
        return soNhanVienNhieuNhat;
    }

    public void setSoNhanVienNhieuNhat(Integer soNhanVienNhieuNhat) {
        this.soNhanVienNhieuNhat = soNhanVienNhieuNhat;
    }
}
