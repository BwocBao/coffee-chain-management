package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Du lieu can nap cho man hinh nhap kho")
public class InventoryLookupResponse {
    @Schema(description = "Danh sach kho co the nhap hang")
    private List<InventoryOptionResponse> warehouses = new ArrayList<>();

    @Schema(description = "Danh sach nha cung cap")
    private List<InventoryOptionResponse> suppliers = new ArrayList<>();

    @Schema(description = "Danh sach nguyen lieu")
    private List<InventoryOptionResponse> ingredients = new ArrayList<>();

    public List<InventoryOptionResponse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<InventoryOptionResponse> warehouses) {
        this.warehouses = warehouses;
    }

    public List<InventoryOptionResponse> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<InventoryOptionResponse> suppliers) {
        this.suppliers = suppliers;
    }

    public List<InventoryOptionResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<InventoryOptionResponse> ingredients) {
        this.ingredients = ingredients;
    }
}
