package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(
    description =
        "DTO response WastageLookupResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class WastageLookupResponse {
  @Schema(description = "Danh sach kho dung cho combobox")
  private List<OptionDto> warehouses = new ArrayList<>();

  @Schema(description = "Danh sach nguyen lieu dung cho combobox")
  private List<OptionDto> ingredients = new ArrayList<>();

  @Schema(description = "Gia tri $field trong response tra ve frontend (wastage types).")
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
    @Schema(description = "Ma dinh danh cua ban ghi", example = "1")
    private Long id;

    @Schema(description = "Ma xac nhan", example = "Gia tri mau")
    private String code;

    @Schema(description = "Ten hien thi", example = "Gia tri mau")
    private String name;

    @Schema(description = "Mo ta hoac thong tin phu", example = "Gia tri mau")
    private String description;

    public OptionDto() {}

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
