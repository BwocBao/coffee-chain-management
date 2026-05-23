package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO response InventoryStockOptionResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class InventoryStockOptionResponse {
    @Schema(description = "Ma dinh danh cua ban ghi", example = "1")
    private Long id;
    @Schema(description = "Ten hien thi", example = "Gia tri mau")
    private String name;
    @Schema(description = "Mo ta hoac thong tin phu", example = "Gia tri mau")
    private String description;
    @Schema(description = "So luong ton hien tai", example = "100.5")
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
