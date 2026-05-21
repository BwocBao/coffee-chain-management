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
 * Client gọi API quản lý nhà cung cấp từ frontend Swing.
 */
public class SupplierApiClient extends ApiClientSupport {

    public List<SupplierDto> searchSuppliers(String keyword) throws IOException, InterruptedException {
        String url = ApiConfig.SUPPLIERS_URL;
        if (keyword != null && !keyword.trim().isEmpty()) {
            url += "?keyword=" + URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<List<SupplierDto>> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<List<SupplierDto>>>() {}
        );
        List<SupplierDto> data = extractData(baseResponse);
        return data == null ? new ArrayList<>() : data;
    }

    public SupplierDto createSupplier(SupplierRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.SUPPLIERS_URL))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<SupplierDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<SupplierDto>>() {}
        );
        return extractData(baseResponse);
    }

    public SupplierDto updateSupplier(Long id, SupplierRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.SUPPLIERS_URL + "/" + id))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<SupplierDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<SupplierDto>>() {}
        );
        return extractData(baseResponse);
    }

    public void deleteSupplier(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.SUPPLIERS_URL + "/" + id))
                .header("Authorization", bearerToken())
                .DELETE()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<Void> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<Void>>() {}
        );
        extractData(baseResponse);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SupplierDto {
        private Long maNhaCungCap;
        private String tenNhaCungCap;
        private String soDienThoai;
        private String email;
        private String diaChi;

        public Long getMaNhaCungCap() {
            return maNhaCungCap;
        }

        public void setMaNhaCungCap(Long maNhaCungCap) {
            this.maNhaCungCap = maNhaCungCap;
        }

        public String getTenNhaCungCap() {
            return tenNhaCungCap;
        }

        public void setTenNhaCungCap(String tenNhaCungCap) {
            this.tenNhaCungCap = tenNhaCungCap;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public void setDiaChi(String diaChi) {
            this.diaChi = diaChi;
        }
    }

    public static class SupplierRequest {
        private String tenNhaCungCap;
        private String soDienThoai;
        private String email;
        private String diaChi;

        public String getTenNhaCungCap() {
            return tenNhaCungCap;
        }

        public void setTenNhaCungCap(String tenNhaCungCap) {
            this.tenNhaCungCap = tenNhaCungCap;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public void setDiaChi(String diaChi) {
            this.diaChi = diaChi;
        }
    }
}
