package com.coffeechain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(
    description =
        "DTO request CreateStocktakeRequest. Swagger hien thi cac field frontend can gui len backend.")
public class CreateStocktakeRequest {
  @Schema(description = "Ma kho lien quan den nghiep vu", example = "1")
  private Long maKho;

  @Schema(description = "Ghi chu nghiep vu", example = "Gia tri mau")
  private String ghiChu;

  @Schema(description = "Danh sach dong chi tiet cua phieu")
  private List<StocktakeItemRequest> items = new ArrayList<>();

  public Long getMaKho() {
    return maKho;
  }

  public void setMaKho(Long maKho) {
    this.maKho = maKho;
  }

  public String getGhiChu() {
    return ghiChu;
  }

  public void setGhiChu(String ghiChu) {
    this.ghiChu = ghiChu;
  }

  public List<StocktakeItemRequest> getItems() {
    return items;
  }

  public void setItems(List<StocktakeItemRequest> items) {
    this.items = items;
  }
}
