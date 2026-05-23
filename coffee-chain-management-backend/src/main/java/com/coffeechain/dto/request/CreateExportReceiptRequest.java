package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Payload tao phieu xuat kho")
public class CreateExportReceiptRequest {
  @Schema(
      description = "Ma kho xuat hang",
      example = "1",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long maKho;

  @Schema(
      description = "Loai xuat kho",
      example = "TRAINING",
      allowableValues = {"INTERNAL_USE", "RETURN_SUPPLIER", "TRAINING", "OTHER"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String loaiXuat;

  @Schema(
      description = "Bat che do chon lo thu cong. Neu false, backend tu xuat theo FEFO",
      example = "false")
  private Boolean chonLoThuCong = false;

  @Schema(description = "Ghi chu phieu xuat", example = "Xuat nguyen lieu cho dao tao")
  private String ghiChu;

  @Schema(
      description = "Danh sach nguyen lieu trong phieu xuat",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<CreateExportReceiptItemRequest> items = new ArrayList<>();

  public Long getMaKho() {
    return maKho;
  }

  public void setMaKho(Long maKho) {
    this.maKho = maKho;
  }

  public String getLoaiXuat() {
    return loaiXuat;
  }

  public void setLoaiXuat(String loaiXuat) {
    this.loaiXuat = loaiXuat;
  }

  public Boolean getChonLoThuCong() {
    return chonLoThuCong;
  }

  public void setChonLoThuCong(Boolean chonLoThuCong) {
    this.chonLoThuCong = chonLoThuCong;
  }

  public String getGhiChu() {
    return ghiChu;
  }

  public void setGhiChu(String ghiChu) {
    this.ghiChu = ghiChu;
  }

  public List<CreateExportReceiptItemRequest> getItems() {
    return items;
  }

  public void setItems(List<CreateExportReceiptItemRequest> items) {
    this.items = items;
  }
}
