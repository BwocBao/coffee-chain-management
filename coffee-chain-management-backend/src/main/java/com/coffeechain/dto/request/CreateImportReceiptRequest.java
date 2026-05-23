package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Payload tao phieu nhap kho")
public class CreateImportReceiptRequest {
  @Schema(
      description = "Ma kho nhap hang",
      example = "1",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long maKho;

  @Schema(
      description = "Ma nha cung cap",
      example = "1",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long maNhaCungCap;

  @Schema(description = "Ghi chu phieu nhap", example = "Nhap bo sung dau ngay")
  private String ghiChu;

  @Schema(
      description = "Danh sach nguyen lieu trong phieu nhap",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<CreateImportReceiptItemRequest> items = new ArrayList<>();

  public Long getMaKho() {
    return maKho;
  }

  public void setMaKho(Long maKho) {
    this.maKho = maKho;
  }

  public Long getMaNhaCungCap() {
    return maNhaCungCap;
  }

  public void setMaNhaCungCap(Long maNhaCungCap) {
    this.maNhaCungCap = maNhaCungCap;
  }

  public String getGhiChu() {
    return ghiChu;
  }

  public void setGhiChu(String ghiChu) {
    this.ghiChu = ghiChu;
  }

  public List<CreateImportReceiptItemRequest> getItems() {
    return items;
  }

  public void setItems(List<CreateImportReceiptItemRequest> items) {
    this.items = items;
  }
}
