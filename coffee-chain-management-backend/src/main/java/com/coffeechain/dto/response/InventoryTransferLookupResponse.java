package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Du lieu can nap cho man hinh dieu chuyen kho")
public class InventoryTransferLookupResponse {
  @Schema(description = "Danh sach kho nguon")
  private List<InventoryOptionResponse> sourceWarehouses = new ArrayList<>();

  @Schema(description = "Danh sach kho dich")
  private List<InventoryOptionResponse> destinationWarehouses = new ArrayList<>();

  public List<InventoryOptionResponse> getSourceWarehouses() {
    return sourceWarehouses;
  }

  public void setSourceWarehouses(List<InventoryOptionResponse> sourceWarehouses) {
    this.sourceWarehouses = sourceWarehouses;
  }

  public List<InventoryOptionResponse> getDestinationWarehouses() {
    return destinationWarehouses;
  }

  public void setDestinationWarehouses(List<InventoryOptionResponse> destinationWarehouses) {
    this.destinationWarehouses = destinationWarehouses;
  }
}
