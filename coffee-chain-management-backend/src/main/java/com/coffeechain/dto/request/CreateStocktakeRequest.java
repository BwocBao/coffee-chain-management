package com.coffeechain.dto.request;

import java.util.ArrayList;
import java.util.List;

public class CreateStocktakeRequest {
    private Long maKho;
    private String ghiChu;
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