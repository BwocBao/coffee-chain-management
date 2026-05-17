package com.coffeechain.dto;

import java.util.ArrayList;
import java.util.List;

public class CreateUserLookupResponse {
    private List<OptionDto> roles = new ArrayList<>();
    private List<OptionDto> branches = new ArrayList<>();

    public List<OptionDto> getRoles() {
        return roles;
    }

    public void setRoles(List<OptionDto> roles) {
        this.roles = roles;
    }

    public List<OptionDto> getBranches() {
        return branches;
    }

    public void setBranches(List<OptionDto> branches) {
        this.branches = branches;
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

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
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
    }
}