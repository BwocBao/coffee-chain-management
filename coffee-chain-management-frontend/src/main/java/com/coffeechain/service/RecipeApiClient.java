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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecipeApiClient extends ApiClientSupport {
    public RecipeLookupDto getLookups() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.RECIPE_LOOKUPS_URL))
                .header("Authorization", bearerToken())
                .GET()
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<RecipeLookupDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<RecipeLookupDto>>() {});
        return extractData(baseResponse);
    }

    public List<RecipeSummaryDto> searchRecipes(String keyword, String status) throws IOException, InterruptedException {
        String params = optionalParamWithoutPrefix("keyword", keyword) + optionalParam("status", status);
        String url = ApiConfig.RECIPES_URL + (params.isBlank() ? "" : "?" + stripLeadingAmpersand(params));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<List<RecipeSummaryDto>> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<List<RecipeSummaryDto>>>() {});
        List<RecipeSummaryDto> data = extractData(baseResponse);
        return data == null ? new ArrayList<>() : data;
    }

    public RecipeDetailDto getRecipeDetail(Long maSanPham) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.RECIPES_URL + "/" + maSanPham))
                .header("Authorization", bearerToken())
                .GET()
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<RecipeDetailDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<RecipeDetailDto>>() {});
        return extractData(baseResponse);
    }

    public RecipeDetailDto createRecipe(RecipeRequest body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.RECIPES_URL))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<RecipeDetailDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<RecipeDetailDto>>() {});
        return extractData(baseResponse);
    }

    public RecipeDetailDto updateRecipe(Long maSanPham, RecipeRequest body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.RECIPES_URL + "/" + maSanPham))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(body), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<RecipeDetailDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<RecipeDetailDto>>() {});
        return extractData(baseResponse);
    }

    public RecipeDetailDto deleteFormula(Long maSanPham) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.RECIPES_URL + "/" + maSanPham + "/formula"))
                .header("Authorization", bearerToken())
                .DELETE()
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<RecipeDetailDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<RecipeDetailDto>>() {});
        return extractData(baseResponse);
    }

    public RecipeDetailDto stopSelling(Long maSanPham) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.RECIPES_URL + "/" + maSanPham + "/stop-selling"))
                .header("Authorization", bearerToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<RecipeDetailDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<RecipeDetailDto>>() {});
        return extractData(baseResponse);
    }

    private String optionalParam(String name, Object value) {
        String param = optionalParamWithoutPrefix(name, value);
        return param.isBlank() ? "" : "&" + param;
    }

    private String optionalParamWithoutPrefix(String name, Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return "";
        return name + "=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String stripLeadingAmpersand(String value) { return value.startsWith("&") ? value.substring(1) : value; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipeLookupDto {
        private List<IngredientOption> ingredients = new ArrayList<>();
        private List<StatusOption> statuses = new ArrayList<>();
        public List<IngredientOption> getIngredients() { return ingredients; }
        public void setIngredients(List<IngredientOption> ingredients) { this.ingredients = ingredients; }
        public List<StatusOption> getStatuses() { return statuses; }
        public void setStatuses(List<StatusOption> statuses) { this.statuses = statuses; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IngredientOption {
        private Long id;
        private String name;
        private String unit;
        private BigDecimal giaVonDvt;
        public IngredientOption() {}
        public IngredientOption(Long id, String name, String unit, BigDecimal giaVonDvt) { this.id = id; this.name = name; this.unit = unit; this.giaVonDvt = giaVonDvt; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getGiaVonDvt() { return giaVonDvt; }
        public void setGiaVonDvt(BigDecimal giaVonDvt) { this.giaVonDvt = giaVonDvt; }
        @Override public String toString() { return name == null ? "-" : name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusOption {
        private String code;
        private String name;
        public StatusOption() {}
        public StatusOption(String code, String name) { this.code = code; this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        @Override public String toString() { return name == null ? code : name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipeSummaryDto {
        private Long maSanPham;
        private String maCongThucHienThi;
        private String tenSanPham;
        private BigDecimal giaBanHienTai;
        private String trangThai;
        private LocalDateTime ngayTao;
        private Integer soNguyenLieu;
        public Long getMaSanPham() { return maSanPham; }
        public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }
        public String getMaCongThucHienThi() { return maCongThucHienThi; }
        public void setMaCongThucHienThi(String maCongThucHienThi) { this.maCongThucHienThi = maCongThucHienThi; }
        public String getTenSanPham() { return tenSanPham; }
        public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
        public BigDecimal getGiaBanHienTai() { return giaBanHienTai; }
        public void setGiaBanHienTai(BigDecimal giaBanHienTai) { this.giaBanHienTai = giaBanHienTai; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public LocalDateTime getNgayTao() { return ngayTao; }
        public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
        public Integer getSoNguyenLieu() { return soNguyenLieu; }
        public void setSoNguyenLieu(Integer soNguyenLieu) { this.soNguyenLieu = soNguyenLieu; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipeDetailDto {
        private Long maSanPham;
        private String maCongThucHienThi;
        private String tenSanPham;
        private String hinhAnh;
        private BigDecimal giaBanHienTai;
        private String trangThai;
        private LocalDateTime ngayTao;
        private List<RecipeIngredientLineDto> items = new ArrayList<>();
        private BigDecimal tongGiaVon;
        private BigDecimal bienLoiNhuanGop;
        public Long getMaSanPham() { return maSanPham; }
        public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }
        public String getMaCongThucHienThi() { return maCongThucHienThi; }
        public void setMaCongThucHienThi(String maCongThucHienThi) { this.maCongThucHienThi = maCongThucHienThi; }
        public String getTenSanPham() { return tenSanPham; }
        public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
        public String getHinhAnh() { return hinhAnh; }
        public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
        public BigDecimal getGiaBanHienTai() { return giaBanHienTai; }
        public void setGiaBanHienTai(BigDecimal giaBanHienTai) { this.giaBanHienTai = giaBanHienTai; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public LocalDateTime getNgayTao() { return ngayTao; }
        public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
        public List<RecipeIngredientLineDto> getItems() { return items; }
        public void setItems(List<RecipeIngredientLineDto> items) { this.items = items; }
        public BigDecimal getTongGiaVon() { return tongGiaVon; }
        public void setTongGiaVon(BigDecimal tongGiaVon) { this.tongGiaVon = tongGiaVon; }
        public BigDecimal getBienLoiNhuanGop() { return bienLoiNhuanGop; }
        public void setBienLoiNhuanGop(BigDecimal bienLoiNhuanGop) { this.bienLoiNhuanGop = bienLoiNhuanGop; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipeIngredientLineDto {
        private Long maCongThuc;
        private Long maNguyenLieu;
        private String tenNguyenLieu;
        private String donViTinh;
        private BigDecimal soLuongCan;
        private BigDecimal giaVonDvt;
        private BigDecimal thanhTien;
        public Long getMaCongThuc() { return maCongThuc; }
        public void setMaCongThuc(Long maCongThuc) { this.maCongThuc = maCongThuc; }
        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public String getTenNguyenLieu() { return tenNguyenLieu; }
        public void setTenNguyenLieu(String tenNguyenLieu) { this.tenNguyenLieu = tenNguyenLieu; }
        public String getDonViTinh() { return donViTinh; }
        public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
        public BigDecimal getSoLuongCan() { return soLuongCan; }
        public void setSoLuongCan(BigDecimal soLuongCan) { this.soLuongCan = soLuongCan; }
        public BigDecimal getGiaVonDvt() { return giaVonDvt; }
        public void setGiaVonDvt(BigDecimal giaVonDvt) { this.giaVonDvt = giaVonDvt; }
        public BigDecimal getThanhTien() { return thanhTien; }
        public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
    }

    public static class RecipeRequest {
        private String tenSanPham;
        private String hinhAnh;
        private BigDecimal giaBanHienTai;
        private String trangThai;
        private List<RecipeIngredientRequest> items = new ArrayList<>();
        public String getTenSanPham() { return tenSanPham; }
        public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
        public String getHinhAnh() { return hinhAnh; }
        public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
        public BigDecimal getGiaBanHienTai() { return giaBanHienTai; }
        public void setGiaBanHienTai(BigDecimal giaBanHienTai) { this.giaBanHienTai = giaBanHienTai; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public List<RecipeIngredientRequest> getItems() { return items; }
        public void setItems(List<RecipeIngredientRequest> items) { this.items = items; }
    }

    public static class RecipeIngredientRequest {
        private Long maNguyenLieu;
        private BigDecimal soLuongCan;
        public RecipeIngredientRequest() {}
        public RecipeIngredientRequest(Long maNguyenLieu, BigDecimal soLuongCan) { this.maNguyenLieu = maNguyenLieu; this.soLuongCan = soLuongCan; }
        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public BigDecimal getSoLuongCan() { return soLuongCan; }
        public void setSoLuongCan(BigDecimal soLuongCan) { this.soLuongCan = soLuongCan; }
    }
}