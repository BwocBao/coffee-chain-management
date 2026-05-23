package com.coffeechain.service;

import com.coffeechain.config.ApiConfig;
import com.coffeechain.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Client gọi API quản lý kho từ frontend Swing. */
public class WarehouseApiClient extends ApiClientSupport {

  public List<WarehouseDto> searchWarehouses(String keyword, String loaiKho, String trangThai)
      throws IOException, InterruptedException {
    String params =
        optionalParamWithoutPrefix("keyword", keyword)
            + optionalParam("loaiKho", loaiKho)
            + optionalParam("trangThai", trangThai);
    String url =
        ApiConfig.WAREHOUSES_URL + (params.isBlank() ? "" : "?" + stripLeadingAmpersand(params));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<WarehouseDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<WarehouseDto>>>() {});
    List<WarehouseDto> data = extractData(baseResponse);
    return data == null ? new ArrayList<>() : data;
  }

  public WarehouseLookupDto getLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.WAREHOUSE_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<WarehouseLookupDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<WarehouseLookupDto>>() {});
    return extractData(baseResponse);
  }

  public WarehouseDto createWarehouse(WarehouseRequest requestBody)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.WAREHOUSES_URL))
            .header("Authorization", bearerToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<WarehouseDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<WarehouseDto>>() {});
    return extractData(baseResponse);
  }

  public WarehouseDto updateWarehouse(Long id, WarehouseRequest requestBody)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.WAREHOUSES_URL + "/" + id))
            .header("Authorization", bearerToken())
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<WarehouseDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<WarehouseDto>>() {});
    return extractData(baseResponse);
  }

  public WarehouseDto deactivateWarehouse(Long id) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.WAREHOUSES_URL + "/" + id))
            .header("Authorization", bearerToken())
            .DELETE()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<WarehouseDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<WarehouseDto>>() {});
    return extractData(baseResponse);
  }

  private String optionalParam(String name, Object value) {
    String param = optionalParamWithoutPrefix(name, value);
    return param.isBlank() ? "" : "&" + param;
  }

  private String optionalParamWithoutPrefix(String name, Object value) {
    if (value == null) {
      return "";
    }
    String text = String.valueOf(value).trim();
    if (text.isEmpty()) {
      return "";
    }
    return name + "=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
  }

  private String stripLeadingAmpersand(String value) {
    return value.startsWith("&") ? value.substring(1) : value;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WarehouseDto {
    private Long maKho;
    private String tenKho;
    private String loaiKho;
    private Long maChiNhanh;
    private String tenChiNhanh;
    private String trangThai;

    public Long getMaKho() {
      return maKho;
    }

    public void setMaKho(Long maKho) {
      this.maKho = maKho;
    }

    public String getTenKho() {
      return tenKho;
    }

    public void setTenKho(String tenKho) {
      this.tenKho = tenKho;
    }

    public String getLoaiKho() {
      return loaiKho;
    }

    public void setLoaiKho(String loaiKho) {
      this.loaiKho = loaiKho;
    }

    public Long getMaChiNhanh() {
      return maChiNhanh;
    }

    public void setMaChiNhanh(Long maChiNhanh) {
      this.maChiNhanh = maChiNhanh;
    }

    public String getTenChiNhanh() {
      return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
      this.tenChiNhanh = tenChiNhanh;
    }

    public String getTrangThai() {
      return trangThai;
    }

    public void setTrangThai(String trangThai) {
      this.trangThai = trangThai;
    }
  }

  public static class WarehouseRequest {
    private String tenKho;
    private String loaiKho;
    private Long maChiNhanh;
    private String trangThai;

    public String getTenKho() {
      return tenKho;
    }

    public void setTenKho(String tenKho) {
      this.tenKho = tenKho;
    }

    public String getLoaiKho() {
      return loaiKho;
    }

    public void setLoaiKho(String loaiKho) {
      this.loaiKho = loaiKho;
    }

    public Long getMaChiNhanh() {
      return maChiNhanh;
    }

    public void setMaChiNhanh(Long maChiNhanh) {
      this.maChiNhanh = maChiNhanh;
    }

    public String getTrangThai() {
      return trangThai;
    }

    public void setTrangThai(String trangThai) {
      this.trangThai = trangThai;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WarehouseLookupDto {
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
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
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

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
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

    @Override
    public String toString() {
      return name == null || name.isBlank() ? (code == null ? "" : code) : name;
    }
  }
}
