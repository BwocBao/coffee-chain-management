package com.coffeechain.dto.response;

import java.util.ArrayList;
import java.util.List;

public class WastageLookupResponse {
    private List<OptionDto> warehouses = new ArrayList<>();
    private List<OptionDto> ingredients = new ArrayList<>();
    private List<OptionDto> wastageTypes = new ArrayList<>();

    public List<OptionDto> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<OptionDto> warehouses) {
        this.warehouses = warehouses;
    }

    public List<OptionDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<OptionDto> ingredients) {
        this.ingredients = ingredients;
    }

    public List<OptionDto> getWastageTypes() {
        return wastageTypes;
    }

    public void setWastageTypes(List<OptionDto> wastageTypes) {
        this.wastageTypes = wastageTypes;
    }

    public static class OptionDto {
        private Long id;
        private String code;
        private String name;
        private String description;

        public OptionDto() {
        }

        public OptionDto(Long id, String code, String name, String description) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}