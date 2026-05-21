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

/**
 * Client gọi API quản lý đơn vị tính từ frontend Swing.
 */
public class UnitApiClient extends ApiClientSupport {

    public List<UnitDto> searchUnits(String keyword) throws IOException, InterruptedException {
        String url = ApiConfig.UNITS_URL;
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "?keyword=" + URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<List<UnitDto>> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<List<UnitDto>>>() {});
        List<UnitDto> data = extractData(baseResponse);
        return data == null ? new ArrayList<>() : data;
    }

    public UnitDto createUnit(UnitRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.UNITS_URL))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<UnitDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<UnitDto>>() {});
        return extractData(baseResponse);
    }

    public UnitDto updateUnit(Long id, UnitRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.UNITS_URL + "/" + id))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<UnitDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<UnitDto>>() {});
        return extractData(baseResponse);
    }

    public void deleteUnit(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.UNITS_URL + "/" + id))
                .header("Authorization", bearerToken())
                .DELETE()
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<Void> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<Void>>() {});
        extractData(baseResponse);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnitDto {
        private Long maDonViTinh;
        private String tenDonViTinh;
        private String kyHieu;

        public Long getMaDonViTinh() { return maDonViTinh; }
        public void setMaDonViTinh(Long maDonViTinh) { this.maDonViTinh = maDonViTinh; }
        public String getTenDonViTinh() { return tenDonViTinh; }
        public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }
        public String getKyHieu() { return kyHieu; }
        public void setKyHieu(String kyHieu) { this.kyHieu = kyHieu; }
    }

    public static class UnitRequest {
        private String tenDonViTinh;
        private String kyHieu;

        public String getTenDonViTinh() { return tenDonViTinh; }
        public void setTenDonViTinh(String tenDonViTinh) { this.tenDonViTinh = tenDonViTinh; }
        public String getKyHieu() { return kyHieu; }
        public void setKyHieu(String kyHieu) { this.kyHieu = kyHieu; }
    }
}