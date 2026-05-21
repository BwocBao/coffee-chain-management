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
 * Client gọi API quản lý chi nhánh từ frontend Swing.
 */
public class BranchApiClient extends ApiClientSupport {

    public List<BranchDto> searchBranches(String keyword, String status) throws IOException, InterruptedException {
        String params = optionalParamWithoutPrefix("keyword", keyword) + optionalParam("status", status);
        String url = ApiConfig.BRANCHES_URL + (params.isBlank() ? "" : "?" + stripLeadingAmpersand(params));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", bearerToken()).GET().build();
        HttpResponse<String> response = send(request);
        BaseResponse<List<BranchDto>> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<List<BranchDto>>>() {});
        List<BranchDto> data = extractData(baseResponse);
        return data == null ? new ArrayList<>() : data;
    }

    public BranchStatisticsDto getStatistics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ApiConfig.BRANCH_STATISTICS_URL)).header("Authorization", bearerToken()).GET().build();
        HttpResponse<String> response = send(request);
        BaseResponse<BranchStatisticsDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<BranchStatisticsDto>>() {});
        return extractData(baseResponse);
    }

    public BranchDto createBranch(BranchRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BRANCHES_URL))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<BranchDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<BranchDto>>() {});
        return extractData(baseResponse);
    }

    public BranchDto updateBranch(Long id, BranchRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BRANCHES_URL + "/" + id))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = send(request);
        BaseResponse<BranchDto> baseResponse = readBaseResponse(response, new TypeReference<BaseResponse<BranchDto>>() {});
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

    private String stripLeadingAmpersand(String value) {
        return value.startsWith("&") ? value.substring(1) : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchDto {
        private Long maChiNhanh;
        private String tenChiNhanh;
        private String diaChi;
        private String soDienThoai;
        private Long maKho;
        private String tenKho;
        private Integer soNhanVien;
        private String trangThai;

        public Long getMaChiNhanh() { return maChiNhanh; }
        public void setMaChiNhanh(Long maChiNhanh) { this.maChiNhanh = maChiNhanh; }
        public String getTenChiNhanh() { return tenChiNhanh; }
        public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }
        public String getDiaChi() { return diaChi; }
        public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
        public String getSoDienThoai() { return soDienThoai; }
        public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
        public Long getMaKho() { return maKho; }
        public void setMaKho(Long maKho) { this.maKho = maKho; }
        public String getTenKho() { return tenKho; }
        public void setTenKho(String tenKho) { this.tenKho = tenKho; }
        public Integer getSoNhanVien() { return soNhanVien; }
        public void setSoNhanVien(Integer soNhanVien) { this.soNhanVien = soNhanVien; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }

    public static class BranchRequest {
        private String tenChiNhanh;
        private String diaChi;
        private String soDienThoai;
        private String trangThai;

        public String getTenChiNhanh() { return tenChiNhanh; }
        public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }
        public String getDiaChi() { return diaChi; }
        public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
        public String getSoDienThoai() { return soDienThoai; }
        public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchStatisticsDto {
        private Integer tongSoChiNhanh;
        private Integer soChiNhanhDangHoatDong;
        private Integer soChiNhanhDaDong;
        private Integer soChiNhanhBaoTri;
        private Long maChiNhanhNhieuNhanVienNhat;
        private String tenChiNhanhNhieuNhanVienNhat;
        private Integer soNhanVienNhieuNhat;

        public Integer getTongSoChiNhanh() { return tongSoChiNhanh; }
        public void setTongSoChiNhanh(Integer tongSoChiNhanh) { this.tongSoChiNhanh = tongSoChiNhanh; }
        public Integer getSoChiNhanhDangHoatDong() { return soChiNhanhDangHoatDong; }
        public void setSoChiNhanhDangHoatDong(Integer soChiNhanhDangHoatDong) { this.soChiNhanhDangHoatDong = soChiNhanhDangHoatDong; }
        public Integer getSoChiNhanhDaDong() { return soChiNhanhDaDong; }
        public void setSoChiNhanhDaDong(Integer soChiNhanhDaDong) { this.soChiNhanhDaDong = soChiNhanhDaDong; }
        public Integer getSoChiNhanhBaoTri() { return soChiNhanhBaoTri; }
        public void setSoChiNhanhBaoTri(Integer soChiNhanhBaoTri) { this.soChiNhanhBaoTri = soChiNhanhBaoTri; }
        public Long getMaChiNhanhNhieuNhanVienNhat() { return maChiNhanhNhieuNhanVienNhat; }
        public void setMaChiNhanhNhieuNhanVienNhat(Long maChiNhanhNhieuNhanVienNhat) { this.maChiNhanhNhieuNhanVienNhat = maChiNhanhNhieuNhanVienNhat; }
        public String getTenChiNhanhNhieuNhanVienNhat() { return tenChiNhanhNhieuNhanVienNhat; }
        public void setTenChiNhanhNhieuNhanVienNhat(String tenChiNhanhNhieuNhanVienNhat) { this.tenChiNhanhNhieuNhanVienNhat = tenChiNhanhNhieuNhanVienNhat; }
        public Integer getSoNhanVienNhieuNhat() { return soNhanVienNhieuNhat; }
        public void setSoNhanVienNhieuNhat(Integer soNhanVienNhieuNhat) { this.soNhanVienNhieuNhat = soNhanVienNhieuNhat; }
    }
}