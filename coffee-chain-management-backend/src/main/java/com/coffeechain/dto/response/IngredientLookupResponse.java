package com.coffeechain.dto.response;

import java.util.ArrayList;
import java.util.List;

public class IngredientLookupResponse {
    private List<OptionDto> units = new ArrayList<>();

    public List<OptionDto> getUnits() {
        return units;
    }

    public void setUnits(List<OptionDto> units) {
        this.units = units;
    }

    public static class OptionDto {
        private Long id;
        private String name;
        private String description;

        public OptionDto() {
        }

        public OptionDto(Long id, String name, String description) {
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
}