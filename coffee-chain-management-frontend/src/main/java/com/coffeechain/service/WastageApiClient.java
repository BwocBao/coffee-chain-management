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

public class WastageApiClient extends ApiClientSupport {

    public WastageLookupDto getLookups() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_WASTAGE_LOOKUPS_URL))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<WastageLookupDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<WastageLookupDto>>() {}
        );
        return extractData(baseResponse);
    }

    public List<WastageLotDto> getAvailableLots(Long maKho, Long maNguyenLieu) throws IOException, InterruptedException {
        String url = ApiConfig.INVENTORY_WASTAGE_LOTS_URL
                + "?maKho=" + maKho
                + "&maNguyenLieu=" + maNguyenLieu;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<List<WastageLotDto>> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<List<WastageLotDto>>>() {}
        );
        return extractData(baseResponse);
    }

    public List<WastageDto> searchWastages(
            Long maKho,
            Long maNguyenLieu,
            String loaiHaoHut,
            String fromDate,
            String toDate,
            String keyword
    ) throws IOException, InterruptedException {
        String params = optionalParam("maKho", maKho)
                + optionalParam("maNguyenLieu", maNguyenLieu)
                + optionalParam("loaiHaoHut", loaiHaoHut)
                + optionalParam("fromDate", fromDate)
                + optionalParam("toDate", toDate)
                + optionalParam("keyword", keyword);

        String url = ApiConfig.INVENTORY_WASTAGES_URL
                + (params.isBlank() ? "" : "?" + params.substring(1));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<List<WastageDto>> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<List<WastageDto>>>() {}
        );
        return extractData(baseResponse);
    }

    public WastageDto createWastage(CreateWastageRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_WASTAGES_URL))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<WastageDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<WastageDto>>() {}
        );
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
    public static class WastageLookupDto {
        private List<OptionDto> warehouses = new ArrayList<>();
        private List<OptionDto> ingredients = new ArrayList<>();
        private List<OptionDto> wastageTypes = new ArrayList<>();

        public List<OptionDto> getWarehouses() { return warehouses; }
        public void setWarehouses(List<OptionDto> warehouses) { this.warehouses = warehouses; }
        public List<OptionDto> getIngredients() { return ingredients; }
        public void setIngredients(List<OptionDto> ingredients) { this.ingredients = ingredients; }
        public List<OptionDto> getWastageTypes() { return wastageTypes; }
        public void setWastageTypes(List<OptionDto> wastageTypes) { this.wastageTypes = wastageTypes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OptionDto {
        private Long id;
        private String code;
        private String name;
        private String description;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return name == null ? "" : name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WastageLotDto {
        private Long maLoHang;
        private Long maKho;
        private String tenKho;
        private Long maNguyenLieu;
        private String tenNguyenLieu;
        private String donViTinh;
        private BigDecimal soLuongConLai;
        private String hanSuDung;
        private String trangThai;

        public Long getMaLoHang() { return maLoHang; }
        public void setMaLoHang(Long maLoHang) { this.maLoHang = maLoHang; }
        public Long getMaKho() { return maKho; }
        public void setMaKho(Long maKho) { this.maKho = maKho; }
        public String getTenKho() { return tenKho; }
        public void setTenKho(String tenKho) { this.tenKho = tenKho; }
        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public String getTenNguyenLieu() { return tenNguyenLieu; }
        public void setTenNguyenLieu(String tenNguyenLieu) { this.tenNguyenLieu = tenNguyenLieu; }
        public String getDonViTinh() { return donViTinh; }
        public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
        public BigDecimal getSoLuongConLai() { return soLuongConLai; }
        public void setSoLuongConLai(BigDecimal soLuongConLai) { this.soLuongConLai = soLuongConLai; }
        public String getHanSuDung() { return hanSuDung; }
        public void setHanSuDung(String hanSuDung) { this.hanSuDung = hanSuDung; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

        @Override
        public String toString() {
            return maLoHang == null ? "" : "Lô #" + maLoHang;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WastageDto {
        private Long maPhieuHaoHut;
        private Long maKho;
        private String tenKho;
        private Long maNguyenLieu;
        private String tenNguyenLieu;
        private String donViTinh;
        private Long maLoHang;
        private BigDecimal soLuongHaoHut;
        private String loaiHaoHut;
        private String ngayHaoHut;
        private String ghiChu;
        private Long maNguoiBaoCao;
        private String tenNguoiBaoCao;

        public Long getMaPhieuHaoHut() { return maPhieuHaoHut; }
        public void setMaPhieuHaoHut(Long maPhieuHaoHut) { this.maPhieuHaoHut = maPhieuHaoHut; }
        public Long getMaKho() { return maKho; }
        public void setMaKho(Long maKho) { this.maKho = maKho; }
        public String getTenKho() { return tenKho; }
        public void setTenKho(String tenKho) { this.tenKho = tenKho; }
        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public String getTenNguyenLieu() { return tenNguyenLieu; }
        public void setTenNguyenLieu(String tenNguyenLieu) { this.tenNguyenLieu = tenNguyenLieu; }
        public String getDonViTinh() { return donViTinh; }
        public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
        public Long getMaLoHang() { return maLoHang; }
        public void setMaLoHang(Long maLoHang) { this.maLoHang = maLoHang; }
        public BigDecimal getSoLuongHaoHut() { return soLuongHaoHut; }
        public void setSoLuongHaoHut(BigDecimal soLuongHaoHut) { this.soLuongHaoHut = soLuongHaoHut; }
        public String getLoaiHaoHut() { return loaiHaoHut; }
        public void setLoaiHaoHut(String loaiHaoHut) { this.loaiHaoHut = loaiHaoHut; }
        public String getNgayHaoHut() { return ngayHaoHut; }
        public void setNgayHaoHut(String ngayHaoHut) { this.ngayHaoHut = ngayHaoHut; }
        public String getGhiChu() { return ghiChu; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
        public Long getMaNguoiBaoCao() { return maNguoiBaoCao; }
        public void setMaNguoiBaoCao(Long maNguoiBaoCao) { this.maNguoiBaoCao = maNguoiBaoCao; }
        public String getTenNguoiBaoCao() { return tenNguoiBaoCao; }
        public void setTenNguoiBaoCao(String tenNguoiBaoCao) { this.tenNguoiBaoCao = tenNguoiBaoCao; }
    }

    public static class CreateWastageRequest {
        private Long maKho;
        private Long maNguyenLieu;
        private Long maLoHang;
        private BigDecimal soLuongHaoHut;
        private String loaiHaoHut;
        private String ghiChu;

        public Long getMaKho() { return maKho; }
        public void setMaKho(Long maKho) { this.maKho = maKho; }
        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public Long getMaLoHang() { return maLoHang; }
        public void setMaLoHang(Long maLoHang) { this.maLoHang = maLoHang; }
        public BigDecimal getSoLuongHaoHut() { return soLuongHaoHut; }
        public void setSoLuongHaoHut(BigDecimal soLuongHaoHut) { this.soLuongHaoHut = soLuongHaoHut; }
        public String getLoaiHaoHut() { return loaiHaoHut; }
        public void setLoaiHaoHut(String loaiHaoHut) { this.loaiHaoHut = loaiHaoHut; }
        public String getGhiChu() { return ghiChu; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    }
}