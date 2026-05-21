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

public class StocktakeApiClient extends ApiClientSupport {

    public StocktakeLookupDto getLookups() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_STOCKTAKE_LOOKUPS_URL))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<StocktakeLookupDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<StocktakeLookupDto>>() {}
        );
        return extractData(baseResponse);
    }

    public List<StocktakeSystemStockDto> getSystemStock(Long maKho, Long maNguyenLieu) throws IOException, InterruptedException {
        String params = optionalParam("maKho", maKho) + optionalParam("maNguyenLieu", maNguyenLieu);
        String url = ApiConfig.INVENTORY_STOCKTAKE_SYSTEM_STOCK_URL
                + (params.isBlank() ? "" : "?" + params.substring(1));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<List<StocktakeSystemStockDto>> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<List<StocktakeSystemStockDto>>>() {}
        );
        return extractData(baseResponse);
    }

    public List<StocktakeDto> searchStocktakes(
            Long maKho,
            String trangThai,
            String fromDate,
            String toDate,
            String keyword
    ) throws IOException, InterruptedException {
        String params = optionalParam("maKho", maKho)
                + optionalParam("trangThai", trangThai)
                + optionalParam("fromDate", fromDate)
                + optionalParam("toDate", toDate)
                + optionalParam("keyword", keyword);
        String url = ApiConfig.INVENTORY_STOCKTAKES_URL
                + (params.isBlank() ? "" : "?" + params.substring(1));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<List<StocktakeDto>> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<List<StocktakeDto>>>() {}
        );
        return extractData(baseResponse);
    }

    public StocktakeDto getById(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_STOCKTAKES_URL + "/" + id))
                .header("Authorization", bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<StocktakeDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<StocktakeDto>>() {}
        );
        return extractData(baseResponse);
    }

    public StocktakeDto createStocktake(StocktakeRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_STOCKTAKES_URL))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<StocktakeDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<StocktakeDto>>() {}
        );
        return extractData(baseResponse);
    }

    public StocktakeDto updateStocktake(Long id, StocktakeRequest requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_STOCKTAKES_URL + "/" + id))
                .header("Authorization", bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<StocktakeDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<StocktakeDto>>() {}
        );
        return extractData(baseResponse);
    }

    public StocktakeDto completeStocktake(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_STOCKTAKES_URL + "/" + id + "/complete"))
                .header("Authorization", bearerToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<StocktakeDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<StocktakeDto>>() {}
        );
        return extractData(baseResponse);
    }

    public StocktakeDto cancelStocktake(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.INVENTORY_STOCKTAKES_URL + "/" + id + "/cancel"))
                .header("Authorization", bearerToken())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = send(request);
        BaseResponse<StocktakeDto> baseResponse = readBaseResponse(
                response,
                new TypeReference<BaseResponse<StocktakeDto>>() {}
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
    public static class StocktakeLookupDto {
        private List<OptionDto> warehouses = new ArrayList<>();
        private List<OptionDto> ingredients = new ArrayList<>();
        private List<OptionDto> handlingOptions = new ArrayList<>();
        private List<OptionDto> statuses = new ArrayList<>();

        public List<OptionDto> getWarehouses() { return warehouses; }
        public void setWarehouses(List<OptionDto> warehouses) { this.warehouses = warehouses; }
        public List<OptionDto> getIngredients() { return ingredients; }
        public void setIngredients(List<OptionDto> ingredients) { this.ingredients = ingredients; }
        public List<OptionDto> getHandlingOptions() { return handlingOptions; }
        public void setHandlingOptions(List<OptionDto> handlingOptions) { this.handlingOptions = handlingOptions; }
        public List<OptionDto> getStatuses() { return statuses; }
        public void setStatuses(List<OptionDto> statuses) { this.statuses = statuses; }
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
    public static class StocktakeSystemStockDto {
        private Long maLoHang;
        private Long maKho;
        private String tenKho;
        private Long maNguyenLieu;
        private String tenNguyenLieu;
        private String donViTinh;
        private BigDecimal soLuongHeThong;
        private String hanSuDung;
        private String trangThaiLo;

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
        public BigDecimal getSoLuongHeThong() { return soLuongHeThong; }
        public void setSoLuongHeThong(BigDecimal soLuongHeThong) { this.soLuongHeThong = soLuongHeThong; }
        public String getHanSuDung() { return hanSuDung; }
        public void setHanSuDung(String hanSuDung) { this.hanSuDung = hanSuDung; }
        public String getTrangThaiLo() { return trangThaiLo; }
        public void setTrangThaiLo(String trangThaiLo) { this.trangThaiLo = trangThaiLo; }

        @Override
        public String toString() {
            String qty = soLuongHeThong == null ? "0" : soLuongHeThong.stripTrailingZeros().toPlainString();
            return "Lô #" + maLoHang + " - " + qty + " " + (donViTinh == null ? "" : donViTinh);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StocktakeDto {
        private Long maPhieuKiemKho;
        private Long maKho;
        private String tenKho;
        private String ngayKiemKho;
        private Long maNguoiKiem;
        private String tenNguoiKiem;
        private String trangThai;
        private String ghiChu;
        private Integer soDongChiTiet;
        private List<StocktakeItemDto> items = new ArrayList<>();

        public Long getMaPhieuKiemKho() { return maPhieuKiemKho; }
        public void setMaPhieuKiemKho(Long maPhieuKiemKho) { this.maPhieuKiemKho = maPhieuKiemKho; }
        public Long getMaKho() { return maKho; }
        public void setMaKho(Long maKho) { this.maKho = maKho; }
        public String getTenKho() { return tenKho; }
        public void setTenKho(String tenKho) { this.tenKho = tenKho; }
        public String getNgayKiemKho() { return ngayKiemKho; }
        public void setNgayKiemKho(String ngayKiemKho) { this.ngayKiemKho = ngayKiemKho; }
        public Long getMaNguoiKiem() { return maNguoiKiem; }
        public void setMaNguoiKiem(Long maNguoiKiem) { this.maNguoiKiem = maNguoiKiem; }
        public String getTenNguoiKiem() { return tenNguoiKiem; }
        public void setTenNguoiKiem(String tenNguoiKiem) { this.tenNguoiKiem = tenNguoiKiem; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public String getGhiChu() { return ghiChu; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
        public Integer getSoDongChiTiet() { return soDongChiTiet; }
        public void setSoDongChiTiet(Integer soDongChiTiet) { this.soDongChiTiet = soDongChiTiet; }
        public List<StocktakeItemDto> getItems() { return items; }
        public void setItems(List<StocktakeItemDto> items) { this.items = items; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StocktakeItemDto {
        private Long maCtPhieuKiemKho;
        private Long maPhieuKiemKho;
        private Long maNguyenLieu;
        private String tenNguyenLieu;
        private String donViTinh;
        private Long maLoHang;
        private BigDecimal soLuongHeThong;
        private BigDecimal soLuongThucTe;
        private BigDecimal soLuongChenhLech;
        private BigDecimal tyLeChenhLech;
        private String lyDoChenhLech;
        private String huongXuLy;

        public Long getMaCtPhieuKiemKho() { return maCtPhieuKiemKho; }
        public void setMaCtPhieuKiemKho(Long maCtPhieuKiemKho) { this.maCtPhieuKiemKho = maCtPhieuKiemKho; }
        public Long getMaPhieuKiemKho() { return maPhieuKiemKho; }
        public void setMaPhieuKiemKho(Long maPhieuKiemKho) { this.maPhieuKiemKho = maPhieuKiemKho; }
        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public String getTenNguyenLieu() { return tenNguyenLieu; }
        public void setTenNguyenLieu(String tenNguyenLieu) { this.tenNguyenLieu = tenNguyenLieu; }
        public String getDonViTinh() { return donViTinh; }
        public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
        public Long getMaLoHang() { return maLoHang; }
        public void setMaLoHang(Long maLoHang) { this.maLoHang = maLoHang; }
        public BigDecimal getSoLuongHeThong() { return soLuongHeThong; }
        public void setSoLuongHeThong(BigDecimal soLuongHeThong) { this.soLuongHeThong = soLuongHeThong; }
        public BigDecimal getSoLuongThucTe() { return soLuongThucTe; }
        public void setSoLuongThucTe(BigDecimal soLuongThucTe) { this.soLuongThucTe = soLuongThucTe; }
        public BigDecimal getSoLuongChenhLech() { return soLuongChenhLech; }
        public void setSoLuongChenhLech(BigDecimal soLuongChenhLech) { this.soLuongChenhLech = soLuongChenhLech; }
        public BigDecimal getTyLeChenhLech() { return tyLeChenhLech; }
        public void setTyLeChenhLech(BigDecimal tyLeChenhLech) { this.tyLeChenhLech = tyLeChenhLech; }
        public String getLyDoChenhLech() { return lyDoChenhLech; }
        public void setLyDoChenhLech(String lyDoChenhLech) { this.lyDoChenhLech = lyDoChenhLech; }
        public String getHuongXuLy() { return huongXuLy; }
        public void setHuongXuLy(String huongXuLy) { this.huongXuLy = huongXuLy; }
    }

    public static class StocktakeRequest {
        private Long maKho;
        private String ghiChu;
        private List<StocktakeItemRequest> items = new ArrayList<>();

        public Long getMaKho() { return maKho; }
        public void setMaKho(Long maKho) { this.maKho = maKho; }
        public String getGhiChu() { return ghiChu; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
        public List<StocktakeItemRequest> getItems() { return items; }
        public void setItems(List<StocktakeItemRequest> items) { this.items = items; }
    }

    public static class StocktakeItemRequest {
        private Long maNguyenLieu;
        private Long maLoHang;
        private BigDecimal soLuongHeThong;
        private BigDecimal soLuongThucTe;
        private String lyDoChenhLech;
        private String huongXuLy;

        public Long getMaNguyenLieu() { return maNguyenLieu; }
        public void setMaNguyenLieu(Long maNguyenLieu) { this.maNguyenLieu = maNguyenLieu; }
        public Long getMaLoHang() { return maLoHang; }
        public void setMaLoHang(Long maLoHang) { this.maLoHang = maLoHang; }
        public BigDecimal getSoLuongHeThong() { return soLuongHeThong; }
        public void setSoLuongHeThong(BigDecimal soLuongHeThong) { this.soLuongHeThong = soLuongHeThong; }
        public BigDecimal getSoLuongThucTe() { return soLuongThucTe; }
        public void setSoLuongThucTe(BigDecimal soLuongThucTe) { this.soLuongThucTe = soLuongThucTe; }
        public String getLyDoChenhLech() { return lyDoChenhLech; }
        public void setLyDoChenhLech(String lyDoChenhLech) { this.lyDoChenhLech = lyDoChenhLech; }
        public String getHuongXuLy() { return huongXuLy; }
        public void setHuongXuLy(String huongXuLy) { this.huongXuLy = huongXuLy; }
    }
}