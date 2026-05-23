package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(
    description =
        "DTO response IngredientLookupResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class IngredientLookupResponse {
  @Schema(description = "Danh sach don vi tinh dung cho combobox")
  private List<OptionDto> units = new ArrayList<>();

  public List<OptionDto> getUnits() {
    return units;
  }

  public void setUnits(List<OptionDto> units) {
    this.units = units;
  }

  public static class OptionDto {
    @Schema(description = "Ma dinh danh cua ban ghi", example = "1")
    private Long id;

    @Schema(description = "Ten hien thi", example = "Gia tri mau")
    private String name;

    @Schema(description = "Mo ta hoac thong tin phu", example = "Gia tri mau")
    private String description;

    public OptionDto() {}

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
