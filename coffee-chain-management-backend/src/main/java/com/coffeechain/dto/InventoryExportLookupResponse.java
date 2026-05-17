package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Du lieu can nap cho man hinh xuat kho")
public class InventoryExportLookupResponse {
    @Schema(description = "Danh sach kho co the xuat hang")
    private List<InventoryOptionResponse> warehouses = new ArrayList<>();

    @Schema(description = "Danh sach nguyen lieu")
    private List<InventoryOptionResponse> ingredients = new ArrayList<>();

    @Schema(description = "Danh sach loai xuat kho")
    private List<InventoryOptionResponse> exportTypes = new ArrayList<>();

    public List<InventoryOptionResponse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<InventoryOptionResponse> warehouses) {
        this.warehouses = warehouses;
    }

    public List<InventoryOptionResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<InventoryOptionResponse> ingredients) {
        this.ingredients = ingredients;
    }

    public List<InventoryOptionResponse> getExportTypes() {
        return exportTypes;
    }

    public void setExportTypes(List<InventoryOptionResponse> exportTypes) {
        this.exportTypes = exportTypes;
    }
}
