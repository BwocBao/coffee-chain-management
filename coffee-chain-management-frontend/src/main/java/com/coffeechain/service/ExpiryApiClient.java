package com.coffeechain.service;

import com.coffeechain.config.ApiConfig;
import com.coffeechain.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExpiryApiClient extends ApiClientSupport {

  public ExpiryLookupDto getLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_EXPIRY_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<ExpiryLookupDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<ExpiryLookupDto>>() {});
    return extractData(baseResponse);
  }

  public List<ExpiryLotDto> searchLots(
      Long maKho,
      Long maNguyenLieu,
      String trangThai,
      String mucCanhBao,
      Integer daysToExpire,
      Boolean onlyAvailable,
      Integer warningDays)
      throws IOException, InterruptedException {
    String params =
        optionalParam("maKho", maKho)
            + optionalParam("maNguyenLieu", maNguyenLieu)
            + optionalParam("trangThai", trangThai)
            + optionalParam("mucCanhBao", mucCanhBao)
            + optionalParam("daysToExpire", daysToExpire)
            + optionalParam("onlyAvailable", onlyAvailable)
            + optionalParam("warningDays", warningDays);

    String url =
        ApiConfig.INVENTORY_EXPIRY_LOTS_URL + (params.isBlank() ? "" : "?" + params.substring(1));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<ExpiryLotDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<ExpiryLotDto>>>() {});
    return extractData(baseResponse);
  }

  public ExpiryStatisticsDto getStatistics(Long maKho, Integer warningDays)
      throws IOException, InterruptedException {
    String params = optionalParam("maKho", maKho) + optionalParam("warningDays", warningDays);
    String url =
        ApiConfig.INVENTORY_EXPIRY_STATISTICS_URL
            + (params.isBlank() ? "" : "?" + params.substring(1));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<ExpiryStatisticsDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<ExpiryStatisticsDto>>() {});
    return extractData(baseResponse);
  }

  public ExpiryRefreshDto refreshExpiredLots() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_EXPIRY_REFRESH_URL))
            .header("Authorization", bearerToken())
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<ExpiryRefreshDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<ExpiryRefreshDto>>() {});
    return extractData(baseResponse);
  }

  private String optionalParam(String name, Object value) {
    if (value == null) {
      return "";
    }
    String text = String.valueOf(value).trim();
    if (text.isEmpty()) {
      return "";
    }
    return "&" + name + "=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExpiryLookupDto {
    private List<OptionDto> warehouses = new ArrayList<>();
    private List<OptionDto> ingredients = new ArrayList<>();
    private List<OptionDto> statuses = new ArrayList<>();
    private List<OptionDto> warningLevels = new ArrayList<>();

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

    public List<OptionDto> getStatuses() {
      return statuses;
    }

    public void setStatuses(List<OptionDto> statuses) {
      this.statuses = statuses;
    }

    public List<OptionDto> getWarningLevels() {
      return warningLevels;
    }

    public void setWarningLevels(List<OptionDto> warningLevels) {
      this.warningLevels = warningLevels;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class OptionDto {
    private Long id;
    private String code;
    private String name;
    private String description;

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
      return name == null ? "" : name;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExpiryLotDto {
    private Long maLoHang;
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private BigDecimal soLuongConLai;
    private String ngayTao;
    private String hanSuDung;
    private Integer soNgayConLai;
    private String trangThai;
    private String mucCanhBao;

    public Long getMaLoHang() {
      return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
      this.maLoHang = maLoHang;
    }

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

    public Long getMaNguyenLieu() {
      return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
      this.maNguyenLieu = maNguyenLieu;
    }

    public String getTenNguyenLieu() {
      return tenNguyenLieu;
    }

    public void setTenNguyenLieu(String tenNguyenLieu) {
      this.tenNguyenLieu = tenNguyenLieu;
    }

    public String getDonViTinh() {
      return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
      this.donViTinh = donViTinh;
    }

    public BigDecimal getSoLuongConLai() {
      return soLuongConLai;
    }

    public void setSoLuongConLai(BigDecimal soLuongConLai) {
      this.soLuongConLai = soLuongConLai;
    }

    public String getNgayTao() {
      return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
      this.ngayTao = ngayTao;
    }

    public String getHanSuDung() {
      return hanSuDung;
    }

    public void setHanSuDung(String hanSuDung) {
      this.hanSuDung = hanSuDung;
    }

    public Integer getSoNgayConLai() {
      return soNgayConLai;
    }

    public void setSoNgayConLai(Integer soNgayConLai) {
      this.soNgayConLai = soNgayConLai;
    }

    public String getTrangThai() {
      return trangThai;
    }

    public void setTrangThai(String trangThai) {
      this.trangThai = trangThai;
    }

    public String getMucCanhBao() {
      return mucCanhBao;
    }

    public void setMucCanhBao(String mucCanhBao) {
      this.mucCanhBao = mucCanhBao;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExpiryStatisticsDto {
    private Integer tongSoLo;
    private Integer soLoDangHoatDong;
    private Integer soLoSapHetHan;
    private Integer soLoDaHetHan;
    private Integer soLoDaDungHet;
    private Integer soLoKhongCoHanSuDung;

    public Integer getTongSoLo() {
      return tongSoLo;
    }

    public void setTongSoLo(Integer tongSoLo) {
      this.tongSoLo = tongSoLo;
    }

    public Integer getSoLoDangHoatDong() {
      return soLoDangHoatDong;
    }

    public void setSoLoDangHoatDong(Integer soLoDangHoatDong) {
      this.soLoDangHoatDong = soLoDangHoatDong;
    }

    public Integer getSoLoSapHetHan() {
      return soLoSapHetHan;
    }

    public void setSoLoSapHetHan(Integer soLoSapHetHan) {
      this.soLoSapHetHan = soLoSapHetHan;
    }

    public Integer getSoLoDaHetHan() {
      return soLoDaHetHan;
    }

    public void setSoLoDaHetHan(Integer soLoDaHetHan) {
      this.soLoDaHetHan = soLoDaHetHan;
    }

    public Integer getSoLoDaDungHet() {
      return soLoDaDungHet;
    }

    public void setSoLoDaDungHet(Integer soLoDaDungHet) {
      this.soLoDaDungHet = soLoDaDungHet;
    }

    public Integer getSoLoKhongCoHanSuDung() {
      return soLoKhongCoHanSuDung;
    }

    public void setSoLoKhongCoHanSuDung(Integer soLoKhongCoHanSuDung) {
      this.soLoKhongCoHanSuDung = soLoKhongCoHanSuDung;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExpiryRefreshDto {
    private String message;
    private String refreshedAt;

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getRefreshedAt() {
      return refreshedAt;
    }

    public void setRefreshedAt(String refreshedAt) {
      this.refreshedAt = refreshedAt;
    }
  }
}
