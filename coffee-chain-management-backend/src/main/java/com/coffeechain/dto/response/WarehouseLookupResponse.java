package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(
    description =
        "DTO response WarehouseLookupResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class WarehouseLookupResponse {
  @Schema(description = "Gia tri $field trong response tra ve frontend (warehouse types).")
  private List<OptionDto> warehouseTypes = new ArrayList<>();

  @Schema(description = "Danh sach chi nhanh dung cho combobox")
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
