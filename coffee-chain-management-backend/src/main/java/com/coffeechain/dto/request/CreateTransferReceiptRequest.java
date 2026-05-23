package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Payload tao phieu dieu chuyen kho")
public class CreateTransferReceiptRequest {
  @Schema(description = "Ma kho nguon", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long maKhoNguon;

  @Schema(description = "Ma kho dich", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long maKhoDich;

  @Schema(
      description = "Bat che do chon lo thu cong. Neu false, backend tu dieu chuyen theo FEFO",
      example = "false")
  private Boolean chonLoThuCong = false;

  @Schema(
      description = "Ghi chu phieu dieu chuyen",
      example = "Dieu chuyen nguyen lieu tu kho tong ve chi nhanh")
  private String ghiChu;

  @Schema(
      description = "Danh sach nguyen lieu trong phieu dieu chuyen",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<CreateTransferReceiptItemRequest> items = new ArrayList<>();

  public Long getMaKhoNguon() {
    return maKhoNguon;
  }

  public void setMaKhoNguon(Long maKhoNguon) {
    this.maKhoNguon = maKhoNguon;
  }

  public Long getMaKhoDich() {
    return maKhoDich;
  }

  public void setMaKhoDich(Long maKhoDich) {
    this.maKhoDich = maKhoDich;
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

  public List<CreateTransferReceiptItemRequest> getItems() {
    return items;
  }

  public void setItems(List<CreateTransferReceiptItemRequest> items) {
    this.items = items;
  }
}
