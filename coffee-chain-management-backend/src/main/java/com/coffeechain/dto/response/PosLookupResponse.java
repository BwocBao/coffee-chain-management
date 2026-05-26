package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO response PosLookupResponse. Du lieu combobox cho man hinh POS ban hang.")
public class PosLookupResponse {
  @Schema(description = "Danh sach chi nhanh duoc phep tao/xem don")
  private List<OptionDto> branches = new ArrayList<>();

  @Schema(description = "Danh sach may POS theo chi nhanh")
  private List<OptionDto> posDevices = new ArrayList<>();

  public List<OptionDto> getBranches() {
    return branches;
  }

  public void setBranches(List<OptionDto> branches) {
    this.branches = branches;
  }

  public List<OptionDto> getPosDevices() {
    return posDevices;
  }

  public void setPosDevices(List<OptionDto> posDevices) {
    this.posDevices = posDevices;
  }

  @Schema(description = "Option combobox POS")
  public static class OptionDto {
    private Long id;
    private String code;
    private String name;
    private String description;

    public OptionDto() {}

    public OptionDto(Long id, String code, String name, String description) {
      this.id = id;
      this.code = code;
      this.name = name;
      this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  }
}
