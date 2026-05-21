package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thong tin lo hang con ton de xuat kho")
public class InventoryLotResponse {
    @Schema(description = "Ma lo hang", example = "5")
    private Long maLoHang;

    @Schema(description = "So lo neu lo duoc tao tu phieu nhap", example = "LOT-001")
    private String soLo;

    @Schema(description = "Ma nguyen lieu", example = "2")
    private Long maNguyenLieu;

    @Schema(description = "Ten nguyen lieu", example = "Ca phe hat Robusta")
    private String tenNguyenLieu;

    @Schema(description = "Don vi tinh", example = "g")
    private String donViTinh;

    @Schema(description = "So luong con lai", example = "27000")
    private BigDecimal soLuongConLai;

    @Schema(description = "Han su dung cua lo")
    private LocalDate hanSuDung;

    @Schema(description = "Thoi diem tao lo")
    private LocalDateTime ngayTao;

    public Long getMaLoHang() {
        return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
        this.maLoHang = maLoHang;
    }

    public String getSoLo() {
        return soLo;
    }

    public void setSoLo(String soLo) {
        this.soLo = soLo;
    }

    public Long getMaNguyenLieu() {
        return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
        this.maNguyenLieu = maNguyenLieu;
    }

    public String getTenNguyenLieu() {
        return tenNguyenLieu;
    }

    public void setTenNguyenLieu(String tenNguyenLieu) {
        this.tenNguyenLieu = tenNguyenLieu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getSoLuongConLai() {
        return soLuongConLai;
    }

    public void setSoLuongConLai(BigDecimal soLuongConLai) {
        this.soLuongConLai = soLuongConLai;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
