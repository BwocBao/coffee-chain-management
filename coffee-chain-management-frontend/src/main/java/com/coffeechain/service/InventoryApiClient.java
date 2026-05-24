package com.coffeechain.service;

import com.coffeechain.config.ApiConfig;
import com.coffeechain.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Client goi cac API kho tu frontend Swing. Man nhap kho dung client nay de lay combobox va tao
 * phieu nhap.
 */
public class InventoryApiClient extends ApiClientSupport {

  public InventoryLookupDto getImportLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_IMPORT_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<InventoryLookupDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<InventoryLookupDto>>() {});
    return extractData(baseResponse);
  }

  public ImportReceiptDto createImportReceipt(CreateImportReceiptRequest requestBody)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_IMPORTS_URL))
            .header("Authorization", bearerToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<ImportReceiptDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<ImportReceiptDto>>() {});
    return extractData(baseResponse);
  }

  public List<InventoryStockDto> getExportStock(Long maKho)
      throws IOException, InterruptedException {
    String url = ApiConfig.INVENTORY_EXPORT_STOCK_URL + "?maKho=" + maKho;

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<InventoryStockDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<InventoryStockDto>>>() {});
    return extractData(baseResponse);
  }

  public PageResponseDto<InventoryDto> getStock(
      Long maKho, Long maNguyenLieu, String tuKhoa, String trangThaiTonKho, int page, int size)
      throws IOException, InterruptedException {
    String url =
        ApiConfig.INVENTORY_STOCK_URL
            + "?page="
            + page
            + "&size="
            + size
            + optionalParam("maKho", maKho)
            + optionalParam("maNguyenLieu", maNguyenLieu)
            + optionalParam("tuKhoa", tuKhoa)
            + optionalParam("trangThaiTonKho", trangThaiTonKho);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<PageResponseDto<InventoryDto>> baseResponse =
        readBaseResponse(
            response, new TypeReference<BaseResponse<PageResponseDto<InventoryDto>>>() {});
    return extractData(baseResponse);
  }

  public List<BatchInventoryDto> getStockLots(Long maKho, Long maNguyenLieu)
      throws IOException, InterruptedException {
    String params =
        optionalParamWithoutPrefix("maKho", maKho) + optionalParam("maNguyenLieu", maNguyenLieu);
    String url =
        ApiConfig.INVENTORY_STOCK_LOTS_URL
            + (params.isBlank() ? "" : "?" + stripLeadingAmpersand(params));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<BatchInventoryDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<BatchInventoryDto>>>() {});
    return extractData(baseResponse);
  }

  public StockSummaryDto getStockSummary(Long maKho) throws IOException, InterruptedException {
    String url = ApiConfig.INVENTORY_STOCK_SUMMARY_URL + (maKho == null ? "" : "?maKho=" + maKho);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<StockSummaryDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<StockSummaryDto>>() {});
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

  public static class InventoryLookupDto {

    private List<OptionDto> warehouses = new ArrayList<>();
    private List<OptionDto> suppliers = new ArrayList<>();
    private List<OptionDto> ingredients = new ArrayList<>();

    public List<OptionDto> getWarehouses() {
      return warehouses;
    }

    public void setWarehouses(List<OptionDto> warehouses) {
      this.warehouses = warehouses;
    }

    public List<OptionDto> getSuppliers() {
      return suppliers;
    }

    public void setSuppliers(List<OptionDto> suppliers) {
      this.suppliers = suppliers;
    }

    public List<OptionDto> getIngredients() {
      return ingredients;
    }

    public void setIngredients(List<OptionDto> ingredients) {
      this.ingredients = ingredients;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PageResponseDto<T> {
    private List<T> content = new ArrayList<>();
    private long totalElements;
    private int page;
    private int size;
    private int totalPages;

    public List<T> getContent() {
      return content;
    }

    public void setContent(List<T> content) {
      this.content = content;
    }

    public long getTotalElements() {
      return totalElements;
    }

    public void setTotalElements(long totalElements) {
      this.totalElements = totalElements;
    }

    public int getPage() {
      return page;
    }

    public void setPage(int page) {
      this.page = page;
    }

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }

    public int getTotalPages() {
      return totalPages;
    }

    public void setTotalPages(int totalPages) {
      this.totalPages = totalPages;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InventoryDto {
    private Long maTonKho;
    private Long maKho;
    private String tenKho;
    private String loaiKho;
    private Long maChiNhanh;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String tenDonViTinh;
    private String kyHieu;
    private BigDecimal soLuongTon;
    private BigDecimal mucTonToiThieu;
    private String trangThaiTonKho;
    private String lanCapNhatCuoi;

    public Long getMaTonKho() {
      return maTonKho;
    }

    public void setMaTonKho(Long maTonKho) {
      this.maTonKho = maTonKho;
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

    public String getTenDonViTinh() {
      return tenDonViTinh;
    }

    public void setTenDonViTinh(String tenDonViTinh) {
      this.tenDonViTinh = tenDonViTinh;
    }

    public String getKyHieu() {
      return kyHieu;
    }

    public void setKyHieu(String kyHieu) {
      this.kyHieu = kyHieu;
    }

    public BigDecimal getSoLuongTon() {
      return soLuongTon;
    }

    public void setSoLuongTon(BigDecimal soLuongTon) {
      this.soLuongTon = soLuongTon;
    }

    public BigDecimal getMucTonToiThieu() {
      return mucTonToiThieu;
    }

    public void setMucTonToiThieu(BigDecimal mucTonToiThieu) {
      this.mucTonToiThieu = mucTonToiThieu;
    }

    public String getTrangThaiTonKho() {
      return trangThaiTonKho;
    }

    public void setTrangThaiTonKho(String trangThaiTonKho) {
      this.trangThaiTonKho = trangThaiTonKho;
    }

    public String getLanCapNhatCuoi() {
      return lanCapNhatCuoi;
    }

    public void setLanCapNhatCuoi(String lanCapNhatCuoi) {
      this.lanCapNhatCuoi = lanCapNhatCuoi;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BatchInventoryDto {
    private Long maLoHang;
    private Long maKho;
    private String tenKho;
    private Long maNguyenLieu;
    private String tenNguyenLieu;
    private String kyHieu;
    private BigDecimal soLuongConLai;
    private String trangThai;
    private String hanSuDung;
    private String ngayTao;

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

    public String getKyHieu() {
      return kyHieu;
    }

    public void setKyHieu(String kyHieu) {
      this.kyHieu = kyHieu;
    }

    public BigDecimal getSoLuongConLai() {
      return soLuongConLai;
    }

    public void setSoLuongConLai(BigDecimal soLuongConLai) {
      this.soLuongConLai = soLuongConLai;
    }

    public String getTrangThai() {
      return trangThai;
    }

    public void setTrangThai(String trangThai) {
      this.trangThai = trangThai;
    }

    public String getHanSuDung() {
      return hanSuDung;
    }

    public void setHanSuDung(String hanSuDung) {
      this.hanSuDung = hanSuDung;
    }

    public String getNgayTao() {
      return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
      this.ngayTao = ngayTao;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StockSummaryDto {
    private Long tongSoNguyenLieu;
    private BigDecimal tongSoLuongTon;
    private Long soNguyenLieuHetHang;
    private Long soNguyenLieuTonThap;
    private Long soNguyenLieuOnDinh;

    public Long getTongSoNguyenLieu() {
      return tongSoNguyenLieu;
    }

    public void setTongSoNguyenLieu(Long tongSoNguyenLieu) {
      this.tongSoNguyenLieu = tongSoNguyenLieu;
    }

    public BigDecimal getTongSoLuongTon() {
      return tongSoLuongTon;
    }

    public void setTongSoLuongTon(BigDecimal tongSoLuongTon) {
      this.tongSoLuongTon = tongSoLuongTon;
    }

    public Long getSoNguyenLieuHetHang() {
      return soNguyenLieuHetHang;
    }

    public void setSoNguyenLieuHetHang(Long soNguyenLieuHetHang) {
      this.soNguyenLieuHetHang = soNguyenLieuHetHang;
    }

    public Long getSoNguyenLieuTonThap() {
      return soNguyenLieuTonThap;
    }

    public void setSoNguyenLieuTonThap(Long soNguyenLieuTonThap) {
      this.soNguyenLieuTonThap = soNguyenLieuTonThap;
    }

    public Long getSoNguyenLieuOnDinh() {
      return soNguyenLieuOnDinh;
    }

    public void setSoNguyenLieuOnDinh(Long soNguyenLieuOnDinh) {
      this.soNguyenLieuOnDinh = soNguyenLieuOnDinh;
    }
  }

  public static class OptionDto {

    private Long id;
    private String name;
    private String description;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
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
      if (description == null || description.isBlank()) {
        return name == null ? "" : name;
      }
      return (name == null ? "" : name);
    }
  }

  public static class CreateImportReceiptRequest {

    private Long maKho;
    private Long maNhaCungCap;
    private String ghiChu;
    private List<CreateImportReceiptItemRequest> items = new ArrayList<>();

    public Long getMaKho() {
      return maKho;
    }

    public void setMaKho(Long maKho) {
      this.maKho = maKho;
    }

    public Long getMaNhaCungCap() {
      return maNhaCungCap;
    }

    public void setMaNhaCungCap(Long maNhaCungCap) {
      this.maNhaCungCap = maNhaCungCap;
    }

    public String getGhiChu() {
      return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
      this.ghiChu = ghiChu;
    }

    public List<CreateImportReceiptItemRequest> getItems() {
      return items;
    }

    public void setItems(List<CreateImportReceiptItemRequest> items) {
      this.items = items;
    }
  }

  public static class CreateImportReceiptItemRequest {

    private Long maNguyenLieu;
    private BigDecimal soLuongNhap;
    private BigDecimal donGiaNhap;
    private String soLo;
    private LocalDate hanSuDung;

    public Long getMaNguyenLieu() {
      return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
      this.maNguyenLieu = maNguyenLieu;
    }

    public BigDecimal getSoLuongNhap() {
      return soLuongNhap;
    }

    public void setSoLuongNhap(BigDecimal soLuongNhap) {
      this.soLuongNhap = soLuongNhap;
    }

    public BigDecimal getDonGiaNhap() {
      return donGiaNhap;
    }

    public void setDonGiaNhap(BigDecimal donGiaNhap) {
      this.donGiaNhap = donGiaNhap;
    }

    public String getSoLo() {
      return soLo;
    }

    public void setSoLo(String soLo) {
      this.soLo = soLo;
    }

    public LocalDate getHanSuDung() {
      return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
      this.hanSuDung = hanSuDung;
    }
  }

  public static class ImportReceiptDto {

    private Long maPhieuNhap;
    private String tenKho;
    private String tenNhaCungCap;
    private BigDecimal tongTien;
    private int soDongChiTiet;
    private LocalDateTime ngayNhap;

    public Long getMaPhieuNhap() {
      return maPhieuNhap;
    }

    public void setMaPhieuNhap(Long maPhieuNhap) {
      this.maPhieuNhap = maPhieuNhap;
    }

    public String getTenKho() {
      return tenKho;
    }

    public void setTenKho(String tenKho) {
      this.tenKho = tenKho;
    }

    public String getTenNhaCungCap() {
      return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
      this.tenNhaCungCap = tenNhaCungCap;
    }

    public BigDecimal getTongTien() {
      return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
      this.tongTien = tongTien;
    }

    public int getSoDongChiTiet() {
      return soDongChiTiet;
    }

    public void setSoDongChiTiet(int soDongChiTiet) {
      this.soDongChiTiet = soDongChiTiet;
    }

    public LocalDateTime getNgayNhap() {
      return ngayNhap;
    }

    public void setNgayNhap(LocalDateTime ngayNhap) {
      this.ngayNhap = ngayNhap;
    }
  }

  public InventoryTransferLookupDto getTransferLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_TRANSFER_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<InventoryTransferLookupDto> baseResponse =
        readBaseResponse(
            response, new TypeReference<BaseResponse<InventoryTransferLookupDto>>() {});
    return extractData(baseResponse);
  }

  public List<InventoryStockDto> getTransferStock(Long maKhoNguon)
      throws IOException, InterruptedException {
    String url = ApiConfig.INVENTORY_TRANSFER_STOCK_URL + "?maKhoNguon=" + maKhoNguon;
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<InventoryStockDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<InventoryStockDto>>>() {});
    return extractData(baseResponse);
  }

  public List<InventoryLotDto> getTransferLots(Long maKhoNguon, Long maNguyenLieu)
      throws IOException, InterruptedException {
    String url =
        ApiConfig.INVENTORY_TRANSFER_LOTS_URL
            + "?maKhoNguon="
            + maKhoNguon
            + "&maNguyenLieu="
            + maNguyenLieu;
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<InventoryLotDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<InventoryLotDto>>>() {});
    return extractData(baseResponse);
  }

  public TransferReceiptDto createTransferReceipt(CreateTransferReceiptRequest requestBody)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_TRANSFERS_URL))
            .header("Authorization", bearerToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<TransferReceiptDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<TransferReceiptDto>>() {});
    return extractData(baseResponse);
  }

  public InventoryExportLookupDto getExportLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_EXPORT_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<InventoryExportLookupDto> baseResponse =
        readBaseResponse(response, new TypeReference<>() {});
    return extractData(baseResponse);
  }

  public List<InventoryLotDto> getExportLots(Long maKho, Long maNguyenLieu)
      throws IOException, InterruptedException {
    String url =
        ApiConfig.INVENTORY_EXPORT_LOTS_URL + "?maKho=" + maKho + "&maNguyenLieu=" + maNguyenLieu;

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<List<InventoryLotDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<InventoryLotDto>>>() {});
    return extractData(baseResponse);
  }

  public ExportReceiptDto createExportReceipt(CreateExportReceiptRequest requestBody)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.INVENTORY_EXPORTS_URL))
            .header("Authorization", bearerToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<ExportReceiptDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<ExportReceiptDto>>() {});
    return extractData(baseResponse);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InventoryStockDto extends OptionDto {

    private BigDecimal soLuongTon;

    public BigDecimal getSoLuongTon() {
      return soLuongTon;
    }

    public void setSoLuongTon(BigDecimal soLuongTon) {
      this.soLuongTon = soLuongTon;
    }
  }

  public static class InventoryExportLookupDto {
    private List<OptionDto> warehouses = new ArrayList<>();
    private List<OptionDto> ingredients = new ArrayList<>();
    private List<OptionDto> exportTypes = new ArrayList<>();

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

    public List<OptionDto> getExportTypes() {
      return exportTypes;
    }

    public void setExportTypes(List<OptionDto> exportTypes) {
      this.exportTypes = exportTypes;
    }
  }

  public static class InventoryTransferLookupDto {
    private List<OptionDto> sourceWarehouses = new ArrayList<>();
    private List<OptionDto> destinationWarehouses = new ArrayList<>();

    public List<OptionDto> getSourceWarehouses() {
      return sourceWarehouses;
    }

    public void setSourceWarehouses(List<OptionDto> sourceWarehouses) {
      this.sourceWarehouses = sourceWarehouses;
    }

    public List<OptionDto> getDestinationWarehouses() {
      return destinationWarehouses;
    }

    public void setDestinationWarehouses(List<OptionDto> destinationWarehouses) {
      this.destinationWarehouses = destinationWarehouses;
    }
  }

  public static class CreateTransferReceiptRequest {
    private Long maKhoNguon;
    private Long maKhoDich;
    private boolean chonLoThuCong;
    private String ghiChu;
    private List<CreateTransferReceiptItemRequest> items = new ArrayList<>();

    public Long getMaKhoNguon() {
      return maKhoNguon;
    }

    public void setMaKhoNguon(Long maKhoNguon) {
      this.maKhoNguon = maKhoNguon;
    }

    public Long getMaKhoDich() {
      return maKhoDich;
    }

    public void setMaKhoDich(Long maKhoDich) {
      this.maKhoDich = maKhoDich;
    }

    public boolean isChonLoThuCong() {
      return chonLoThuCong;
    }

    public void setChonLoThuCong(boolean chonLoThuCong) {
      this.chonLoThuCong = chonLoThuCong;
    }

    public String getGhiChu() {
      return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
      this.ghiChu = ghiChu;
    }

    public List<CreateTransferReceiptItemRequest> getItems() {
      return items;
    }

    public void setItems(List<CreateTransferReceiptItemRequest> items) {
      this.items = items;
    }
  }

  public static class CreateTransferReceiptItemRequest {
    private Long maNguyenLieu;
    private BigDecimal soLuongDieuChuyen;
    private List<TransferLotSelectionRequest> loHangDieuChuyen = new ArrayList<>();

    public Long getMaNguyenLieu() {
      return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
      this.maNguyenLieu = maNguyenLieu;
    }

    public BigDecimal getSoLuongDieuChuyen() {
      return soLuongDieuChuyen;
    }

    public void setSoLuongDieuChuyen(BigDecimal soLuongDieuChuyen) {
      this.soLuongDieuChuyen = soLuongDieuChuyen;
    }

    public List<TransferLotSelectionRequest> getLoHangDieuChuyen() {
      return loHangDieuChuyen;
    }

    public void setLoHangDieuChuyen(List<TransferLotSelectionRequest> loHangDieuChuyen) {
      this.loHangDieuChuyen = loHangDieuChuyen;
    }
  }

  public static class TransferLotSelectionRequest {
    private Long maLoHang;
    private BigDecimal soLuongDieuChuyen;

    public TransferLotSelectionRequest() {}

    public TransferLotSelectionRequest(Long maLoHang, BigDecimal soLuongDieuChuyen) {
      this.maLoHang = maLoHang;
      this.soLuongDieuChuyen = soLuongDieuChuyen;
    }

    public Long getMaLoHang() {
      return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
      this.maLoHang = maLoHang;
    }

    public BigDecimal getSoLuongDieuChuyen() {
      return soLuongDieuChuyen;
    }

    public void setSoLuongDieuChuyen(BigDecimal soLuongDieuChuyen) {
      this.soLuongDieuChuyen = soLuongDieuChuyen;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TransferReceiptDto {
    private Long maPhieuDieuChuyen;
    private String tenKhoNguon;
    private String tenKhoDich;
    private String trangThai;
    private int soDongChiTiet;
    private LocalDateTime ngayDieuChuyen;

    public Long getMaPhieuDieuChuyen() {
      return maPhieuDieuChuyen;
    }

    public void setMaPhieuDieuChuyen(Long maPhieuDieuChuyen) {
      this.maPhieuDieuChuyen = maPhieuDieuChuyen;
    }

    public String getTenKhoNguon() {
      return tenKhoNguon;
    }

    public void setTenKhoNguon(String tenKhoNguon) {
      this.tenKhoNguon = tenKhoNguon;
    }

    public String getTenKhoDich() {
      return tenKhoDich;
    }

    public void setTenKhoDich(String tenKhoDich) {
      this.tenKhoDich = tenKhoDich;
    }

    public String getTrangThai() {
      return trangThai;
    }

    public void setTrangThai(String trangThai) {
      this.trangThai = trangThai;
    }

    public int getSoDongChiTiet() {
      return soDongChiTiet;
    }

    public void setSoDongChiTiet(int soDongChiTiet) {
      this.soDongChiTiet = soDongChiTiet;
    }

    public LocalDateTime getNgayDieuChuyen() {
      return ngayDieuChuyen;
    }

    public void setNgayDieuChuyen(LocalDateTime ngayDieuChuyen) {
      this.ngayDieuChuyen = ngayDieuChuyen;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InventoryLotDto {

    @JsonAlias({"maLoHang", "id"})
    private Long maLoHang;

    @JsonAlias({"maKho"})
    private Long maKho;

    @JsonAlias({"maNguyenLieu"})
    private Long maNguyenLieu;

    @JsonAlias({"tenNguyenLieu", "name"})
    private String tenNguyenLieu;

    @JsonAlias({"soLuongConLai", "quantity", "remainingQuantity"})
    private BigDecimal soLuongConLai;

    @JsonAlias({"donGiaNhap", "unitCost", "importUnitPrice"})
    private BigDecimal donGiaNhap;

    /*
     * Để String cho dễ parse.
     * Tránh lỗi nếu backend trả DATE/TIMESTAMP ở format khác nhau.
     */
    @JsonAlias({"hanSuDung", "expiryDate"})
    private String hanSuDung;

    @JsonAlias({"ngayTao", "createdAt"})
    private String ngayTao;

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

    public BigDecimal getSoLuongConLai() {
      return soLuongConLai;
    }

    public void setSoLuongConLai(BigDecimal soLuongConLai) {
      this.soLuongConLai = soLuongConLai;
    }

    public BigDecimal getDonGiaNhap() {
      return donGiaNhap;
    }

    public void setDonGiaNhap(BigDecimal donGiaNhap) {
      this.donGiaNhap = donGiaNhap;
    }

    public String getHanSuDung() {
      return hanSuDung;
    }

    public void setHanSuDung(String hanSuDung) {
      this.hanSuDung = hanSuDung;
    }

    public String getNgayTao() {
      return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
      this.ngayTao = ngayTao;
    }
  }

  public static class CreateExportReceiptRequest {
    private Long maKho;
    private String loaiXuat;
    private Boolean chonLoThuCong;
    private String ghiChu;
    private List<CreateExportReceiptItemRequest> items = new ArrayList<>();

    public Long getMaKho() {
      return maKho;
    }

    public void setMaKho(Long maKho) {
      this.maKho = maKho;
    }

    public String getLoaiXuat() {
      return loaiXuat;
    }

    public void setLoaiXuat(String loaiXuat) {
      this.loaiXuat = loaiXuat;
    }

    public Boolean getChonLoThuCong() {
      return chonLoThuCong;
    }

    public void setChonLoThuCong(Boolean chonLoThuCong) {
      this.chonLoThuCong = chonLoThuCong;
    }

    public String getGhiChu() {
      return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
      this.ghiChu = ghiChu;
    }

    public List<CreateExportReceiptItemRequest> getItems() {
      return items;
    }

    public void setItems(List<CreateExportReceiptItemRequest> items) {
      this.items = items;
    }
  }

  public static class CreateExportReceiptItemRequest {
    private Long maNguyenLieu;
    private BigDecimal soLuongXuat;
    private BigDecimal donGiaXuat;
    private List<ExportLotSelectionRequest> loHangXuat = new ArrayList<>();

    public Long getMaNguyenLieu() {
      return maNguyenLieu;
    }

    public void setMaNguyenLieu(Long maNguyenLieu) {
      this.maNguyenLieu = maNguyenLieu;
    }

    public BigDecimal getSoLuongXuat() {
      return soLuongXuat;
    }

    public void setSoLuongXuat(BigDecimal soLuongXuat) {
      this.soLuongXuat = soLuongXuat;
    }

    public BigDecimal getDonGiaXuat() {
      return donGiaXuat;
    }

    public void setDonGiaXuat(BigDecimal donGiaXuat) {
      this.donGiaXuat = donGiaXuat;
    }

    public List<ExportLotSelectionRequest> getLoHangXuat() {
      return loHangXuat;
    }

    public void setLoHangXuat(List<ExportLotSelectionRequest> loHangXuat) {
      this.loHangXuat = loHangXuat;
    }
  }

  public static class ExportLotSelectionRequest {
    private Long maLoHang;
    private BigDecimal soLuongXuat;

    public Long getMaLoHang() {
      return maLoHang;
    }

    public void setMaLoHang(Long maLoHang) {
      this.maLoHang = maLoHang;
    }

    public BigDecimal getSoLuongXuat() {
      return soLuongXuat;
    }

    public void setSoLuongXuat(BigDecimal soLuongXuat) {
      this.soLuongXuat = soLuongXuat;
    }
  }

  public static class ExportReceiptDto {
    private Long maPhieuXuat;
    private String tenKho;
    private String loaiXuat;
    private BigDecimal tongGiaTriXuat;
    private int soDongChiTiet;
    private LocalDateTime ngayXuat;

    public Long getMaPhieuXuat() {
      return maPhieuXuat;
    }

    public void setMaPhieuXuat(Long maPhieuXuat) {
      this.maPhieuXuat = maPhieuXuat;
    }

    public String getTenKho() {
      return tenKho;
    }

    public void setTenKho(String tenKho) {
      this.tenKho = tenKho;
    }

    public String getLoaiXuat() {
      return loaiXuat;
    }

    public void setLoaiXuat(String loaiXuat) {
      this.loaiXuat = loaiXuat;
    }

    public BigDecimal getTongGiaTriXuat() {
      return tongGiaTriXuat;
    }

    public void setTongGiaTriXuat(BigDecimal tongGiaTriXuat) {
      this.tongGiaTriXuat = tongGiaTriXuat;
    }

    public int getSoDongChiTiet() {
      return soDongChiTiet;
    }

    public void setSoDongChiTiet(int soDongChiTiet) {
      this.soDongChiTiet = soDongChiTiet;
    }

    public LocalDateTime getNgayXuat() {
      return ngayXuat;
    }

    public void setNgayXuat(LocalDateTime ngayXuat) {
      this.ngayXuat = ngayXuat;
    }
  }
}
