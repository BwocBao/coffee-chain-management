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

/**
 * Client gọi API quản lý nguyên liệu từ frontend Swing.
 */
public class IngredientApiClient extends ApiClientSupport {

    public List<IngredientDto> searchIngredients(String keyword, String status, Long unitId)
            throws IOException, InterruptedException {
        String params = optionalParamWithoutPrefix("keyword", keyword)
                + optionalParam("status", status)
                + optionalParam("unitId", unitId);
        String url = ApiConfig.INGREDIENTS_URL + (params.isBlank() ? "" : "?" + stripLeadingAmpersand(params));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<List<IngredientDto>> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<List<IngredientDto>>>() {}
        );
        List<IngredientDto> data = extractData(baseResponse);
        return data == null ? new ArrayList<>() : data;
    }

    public IngredientLookupDto getLookups() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INGREDIENT_LOOKUPS_URL))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<IngredientLookupDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<IngredientLookupDto>>() {}
        );
        return extractData(baseResponse);
    }

    public IngredientDto createIngredient(IngredientRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INGREDIENTS_URL))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<IngredientDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<IngredientDto>>() {}
        );
        return extractData(baseResponse);
    }

    public IngredientDto updateIngredient(Long id, IngredientRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INGREDIENTS_URL + "/" + id))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<IngredientDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<IngredientDto>>() {}
        );
        return extractData(baseResponse);
    }

    public IngredientDto deactivateIngredient(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INGREDIENTS_URL + "/" + id))
                .header("Authorization", bearerToken())
                .DELETE()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<IngredientDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<IngredientDto>>() {}
        );
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
    public static class IngredientDto {
        private Long maNguyenLieu;
        private String tenNguyenLieu;
        private Long maDonViTinh;
        private String tenDonViTinh;
        private String kyHieuDonViTinh;
        private BigDecimal mucTonToiThieu;
        private String trangThai;

        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public String getTenNguyenLieu() { return tenNguyenLieu; }
        public void setTenNguyenLieu(String tenNguyenLieu) { this.tenNguyenLieu = tenNguyenLieu; }
        public Long getMaDonViTinh() { return maDonViTinh; }
        public void setMaDonViTinh(Long maDonViTinh) { this.maDonViTinh = maDonViTinh; }
        public String getTenDonViTinh() { return tenDonViTinh; }
        public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }
        public String getKyHieuDonViTinh() { return kyHieuDonViTinh; }
        public void setKyHieuDonViTinh(String kyHieuDonViTinh) { this.kyHieuDonViTinh = kyHieuDonViTinh; }
        public BigDecimal getMucTonToiThieu() { return mucTonToiThieu; }
        public void setMucTonToiThieu(BigDecimal mucTonToiThieu) { this.mucTonToiThieu = mucTonToiThieu; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }

    public static class IngredientRequest {
        private String tenNguyenLieu;
        private Long maDonViTinh;
        private BigDecimal mucTonToiThieu;
        private String trangThai;

        public String getTenNguyenLieu() { return tenNguyenLieu; }
        public void setTenNguyenLieu(String tenNguyenLieu) { this.tenNguyenLieu = tenNguyenLieu; }
        public Long getMaDonViTinh() { return maDonViTinh; }
        public void setMaDonViTinh(Long maDonViTinh) { this.maDonViTinh = maDonViTinh; }
        public BigDecimal getMucTonToiThieu() { return mucTonToiThieu; }
        public void setMucTonToiThieu(BigDecimal mucTonToiThieu) { this.mucTonToiThieu = mucTonToiThieu; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IngredientLookupDto {
        private List<OptionDto> units = new ArrayList<>();

        public List<OptionDto> getUnits() { return units; }
        public void setUnits(List<OptionDto> units) { this.units = units; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OptionDto {
        private Long id;
        private String name;
        private String description;

        public OptionDto() {}

        public OptionDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return name == null || name.isBlank() ? "-" : name;
        }
    }
}