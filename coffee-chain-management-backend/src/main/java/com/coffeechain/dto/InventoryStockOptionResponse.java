package com.coffeechain.dto;

import java.math.BigDecimal;

public class InventoryStockOptionResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal soLuongTon;

    public InventoryStockOptionResponse() {
    }

    public InventoryStockOptionResponse(Long id, String name, String description, BigDecimal soLuongTon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.soLuongTon = soLuongTon;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getSoLuongTon() {
        return soLuongTon;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSoLuongTon(BigDecimal soLuongTon) {
        this.soLuongTon = soLuongTon;
    }
}