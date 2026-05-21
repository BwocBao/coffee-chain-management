package com.coffeechain.dto.response;

import java.util.ArrayList;
import java.util.List;

public class WarehouseLookupResponse {
    private List<OptionDto> warehouseTypes = new ArrayList<>();
    private List<OptionDto> branches = new ArrayList<>();

    public List<OptionDto> getWarehouseTypes() {
        return warehouseTypes;
    }

    public void setWarehouseTypes(List<OptionDto> warehouseTypes) {
        this.warehouseTypes = warehouseTypes;
    }

    public List<OptionDto> getBranches() {
        return branches;
    }

    public void setBranches(List<OptionDto> branches) {
        this.branches = branches;
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