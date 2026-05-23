package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mot lua chon dung cho combobox cua man hinh kho")
public class InventoryOptionResponse {
  @Schema(description = "Ma khoa chinh cua du lieu", example = "1")
  private Long id;

  @Schema(description = "Ten hien thi tren giao dien", example = "Kho tong Phung Loc")
  private String name;

  @Schema(description = "Thong tin phu de hien thi them neu can", example = "CENTRAL")
  private String description;

  public InventoryOptionResponse() {}

  public InventoryOptionResponse(Long id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
