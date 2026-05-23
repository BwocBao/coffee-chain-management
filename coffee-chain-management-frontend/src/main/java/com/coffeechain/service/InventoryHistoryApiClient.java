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

public class InventoryHistoryApiClient extends ApiClientSupport {

  public InventoryHistoryLookupDto getLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_HISTORY_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<InventoryHistoryLookupDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<InventoryHistoryLookupDto>>() {});
    return extractData(baseResponse);
  }

  public List<InventoryHistoryDto> searchHistory(
      Long maKho,
      Long maNguyenLieu,
      Long maLoHang,
      String loaiGiaoDich,
      String fromDate,
      String toDate,
      String keyword)
      throws IOException, InterruptedException {
    String params =
        optionalParam("maKho", maKho)
            + optionalParam("maNguyenLieu", maNguyenLieu)
            + optionalParam("maLoHang", maLoHang)
            + optionalParam("loaiGiaoDich", loaiGiaoDich)
            + optionalParam("fromDate", fromDate)
            + optionalParam("toDate", toDate)
            + optionalParam("keyword", keyword);

    String url =
        ApiConfig.INVENTORY_HISTORY_URL + (params.isBlank() ? "" : "?" + params.substring(1));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<InventoryHistoryDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<InventoryHistoryDto>>>() {});
    return extractData(baseResponse);
  }

  public List<InventoryHistorySummaryDto> getSummary(
      Long maKho, Long maNguyenLieu, String fromDate, String toDate)
      throws IOException, InterruptedException {
    String params =
        optionalParam("maKho", maKho)
            + optionalParam("maNguyenLieu", maNguyenLieu)
            + optionalParam("fromDate", fromDate)
            + optionalParam("toDate", toDate);

    String url =
        ApiConfig.INVENTORY_HISTORY_SUMMARY_URL
            + (params.isBlank() ? "" : "?" + params.substring(1));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<InventoryHistorySummaryDto>> baseResponse =
        readBaseResponse(
            response, new TypeReference<BaseResponse<List<InventoryHistorySummaryDto>>>() {});
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
  public static class InventoryHistoryLookupDto {
    private List<OptionDto> warehouses = new ArrayList<>();
    private List<OptionDto> ingredients = new ArrayList<>();
    private List<OptionDto> transactionTypes = new ArrayList<>();

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

    public List<OptionDto> getTransactionTypes() {
      return transactionTypes;
    }

    public void setTransactionTypes(List<OptionDto> transactionTypes) {
      this.transactionTypes = transactionTypes;
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
  public static class InventoryHistoryDto {
    private Long maNhatKyKho;
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private Long maLoHang;
    private String loaiGiaoDich;
    private String tenChungTu;
    private Long maChungTu;
    private BigDecimal soLuongThayDoi;
    private BigDecimal soLuongTruoc;
    private BigDecimal soLuongSau;
    private String thoiGian;
    private Long maNguoiThaoTac;
    private String tenNguoiThaoTac;

    public Long getMaNhatKyKho() {
      return maNhatKyKho;
    }

    public void setMaNhatKyKho(Long maNhatKyKho) {
      this.maNhatKyKho = maNhatKyKho;
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

    public Long getMaLoHang() {
      return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
      this.maLoHang = maLoHang;
    }

    public String getLoaiGiaoDich() {
      return loaiGiaoDich;
    }

    public void setLoaiGiaoDich(String loaiGiaoDich) {
      this.loaiGiaoDich = loaiGiaoDich;
    }

    public String getTenChungTu() {
      return tenChungTu;
    }

    public void setTenChungTu(String tenChungTu) {
      this.tenChungTu = tenChungTu;
    }

    public Long getMaChungTu() {
      return maChungTu;
    }

    public void setMaChungTu(Long maChungTu) {
      this.maChungTu = maChungTu;
    }

    public BigDecimal getSoLuongThayDoi() {
      return soLuongThayDoi;
    }

    public void setSoLuongThayDoi(BigDecimal soLuongThayDoi) {
      this.soLuongThayDoi = soLuongThayDoi;
    }

    public BigDecimal getSoLuongTruoc() {
      return soLuongTruoc;
    }

    public void setSoLuongTruoc(BigDecimal soLuongTruoc) {
      this.soLuongTruoc = soLuongTruoc;
    }

    public BigDecimal getSoLuongSau() {
      return soLuongSau;
    }

    public void setSoLuongSau(BigDecimal soLuongSau) {
      this.soLuongSau = soLuongSau;
    }

    public String getThoiGian() {
      return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
      this.thoiGian = thoiGian;
    }

    public Long getMaNguoiThaoTac() {
      return maNguoiThaoTac;
    }

    public void setMaNguoiThaoTac(Long maNguoiThaoTac) {
      this.maNguoiThaoTac = maNguoiThaoTac;
    }

    public String getTenNguoiThaoTac() {
      return tenNguoiThaoTac;
    }

    public void setTenNguoiThaoTac(String tenNguoiThaoTac) {
      this.tenNguoiThaoTac = tenNguoiThaoTac;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InventoryHistorySummaryDto {
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String donViTinh;
    private BigDecimal tongNhap;
    private BigDecimal tongXuat;
    private BigDecimal tongDieuChuyenVao;
    private BigDecimal tongDieuChuyenRa;
    private BigDecimal tongHaoHut;
    private BigDecimal tongBanHangTruKho;
    private BigDecimal tongHoanTruKho;
    private BigDecimal tongDieuChinhKiemKho;
    private BigDecimal bienDongRong;
    private Integer soGiaoDich;

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

    public BigDecimal getTongNhap() {
      return tongNhap;
    }

    public void setTongNhap(BigDecimal tongNhap) {
      this.tongNhap = tongNhap;
    }

    public BigDecimal getTongXuat() {
      return tongXuat;
    }

    public void setTongXuat(BigDecimal tongXuat) {
      this.tongXuat = tongXuat;
    }

    public BigDecimal getTongDieuChuyenVao() {
      return tongDieuChuyenVao;
    }

    public void setTongDieuChuyenVao(BigDecimal tongDieuChuyenVao) {
      this.tongDieuChuyenVao = tongDieuChuyenVao;
    }

    public BigDecimal getTongDieuChuyenRa() {
      return tongDieuChuyenRa;
    }

    public void setTongDieuChuyenRa(BigDecimal tongDieuChuyenRa) {
      this.tongDieuChuyenRa = tongDieuChuyenRa;
    }

    public BigDecimal getTongHaoHut() {
      return tongHaoHut;
    }

    public void setTongHaoHut(BigDecimal tongHaoHut) {
      this.tongHaoHut = tongHaoHut;
    }

    public BigDecimal getTongBanHangTruKho() {
      return tongBanHangTruKho;
    }

    public void setTongBanHangTruKho(BigDecimal tongBanHangTruKho) {
      this.tongBanHangTruKho = tongBanHangTruKho;
    }

    public BigDecimal getTongHoanTruKho() {
      return tongHoanTruKho;
    }

    public void setTongHoanTruKho(BigDecimal tongHoanTruKho) {
      this.tongHoanTruKho = tongHoanTruKho;
    }

    public BigDecimal getTongDieuChinhKiemKho() {
      return tongDieuChinhKiemKho;
    }

    public void setTongDieuChinhKiemKho(BigDecimal tongDieuChinhKiemKho) {
      this.tongDieuChinhKiemKho = tongDieuChinhKiemKho;
    }

    public BigDecimal getBienDongRong() {
      return bienDongRong;
    }

    public void setBienDongRong(BigDecimal bienDongRong) {
      this.bienDongRong = bienDongRong;
    }

    public Integer getSoGiaoDich() {
      return soGiaoDich;
    }

    public void setSoGiaoDich(Integer soGiaoDich) {
      this.soGiaoDich = soGiaoDich;
    }
  }
}
