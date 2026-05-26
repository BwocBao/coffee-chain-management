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

public class PosApiClient extends ApiClientSupport {

  public PosLookupDto getLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.POS_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();
    HttpResponse<String> response = send(request);
    BaseResponse<PosLookupDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<PosLookupDto>>() {});
    return extractData(baseResponse);
  }

  public List<PosProductDto> getProducts() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.POS_PRODUCTS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();
    HttpResponse<String> response = send(request);
    BaseResponse<List<PosProductDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<PosProductDto>>>() {});
    List<PosProductDto> data = extractData(baseResponse);
    return data == null ? new ArrayList<>() : data;
  }

  public List<PosOrderSummaryDto> searchOrders(Long maChiNhanh, String keyword, String status)
      throws IOException, InterruptedException {
    StringBuilder url = new StringBuilder(ApiConfig.POS_ORDERS_URL).append("?limit=100");
    appendParam(url, "maChiNhanh", maChiNhanh);
    appendParam(url, "keyword", keyword);
    appendParam(url, "status", status);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url.toString()))
            .header("Authorization", bearerToken())
            .GET()
            .build();
    HttpResponse<String> response = send(request);
    BaseResponse<List<PosOrderSummaryDto>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<List<PosOrderSummaryDto>>>() {});
    List<PosOrderSummaryDto> data = extractData(baseResponse);
    return data == null ? new ArrayList<>() : data;
  }

  public PosOrderDto createOrder(CreatePosOrderRequest requestBody)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.POS_ORDERS_URL))
            .header("Authorization", bearerToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();
    HttpResponse<String> response = send(request);
    BaseResponse<PosOrderDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<PosOrderDto>>() {});
    return extractData(baseResponse);
  }

  public PosOrderDto getOrder(Long maHoaDon) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.POS_ORDERS_URL + "/" + maHoaDon))
            .header("Authorization", bearerToken())
            .GET()
            .build();
    HttpResponse<String> response = send(request);
    BaseResponse<PosOrderDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<PosOrderDto>>() {});
    return extractData(baseResponse);
  }

  public PosOrderDto payCash(Long maHoaDon) throws IOException, InterruptedException {
    return postOrderAction(maHoaDon, "pay-cash");
  }

  public BankQrDto createBankQr(Long maHoaDon) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.POS_ORDERS_URL + "/" + maHoaDon + "/create-bank-qr"))
            .header("Authorization", bearerToken())
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    HttpResponse<String> response = send(request);
    BaseResponse<BankQrDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<BankQrDto>>() {});
    return extractData(baseResponse);
  }


  public byte[] downloadInvoicePdf(Long maHoaDon) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.POS_ORDERS_URL + "/" + maHoaDon + "/invoice.pdf"))
            .header("Authorization", bearerToken())
            .GET()
            .build();
    HttpResponse<byte[]> response = sendBytes(request);
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return response.body();
    }
    throw new IOException("Không tải được PDF hóa đơn. HTTP " + response.statusCode());
  }
  public PosOrderDto cancelOrder(Long maHoaDon) throws IOException, InterruptedException {
    return postOrderAction(maHoaDon, "cancel");
  }

  private PosOrderDto postOrderAction(Long maHoaDon, String action)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.POS_ORDERS_URL + "/" + maHoaDon + "/" + action))
            .header("Authorization", bearerToken())
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    HttpResponse<String> response = send(request);
    BaseResponse<PosOrderDto> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<PosOrderDto>>() {});
    return extractData(baseResponse);
  }

  private void appendParam(StringBuilder url, String name, Object value) {
    if (value == null) return;
    String text = String.valueOf(value).trim();
    if (text.isEmpty()) return;
    url.append('&')
        .append(name)
        .append('=')
        .append(URLEncoder.encode(text, StandardCharsets.UTF_8));
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PosLookupDto {
    private List<OptionDto> branches = new ArrayList<>();
    private List<OptionDto> posDevices = new ArrayList<>();

    public List<OptionDto> getBranches() { return branches; }
    public void setBranches(List<OptionDto> branches) { this.branches = branches; }
    public List<OptionDto> getPosDevices() { return posDevices; }
    public void setPosDevices(List<OptionDto> posDevices) { this.posDevices = posDevices; }
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
  public static class PosProductDto {
    private Long maSanPham;
    private String tenSanPham;
    private String hinhAnh;
    private BigDecimal giaBanHienTai;

    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public BigDecimal getGiaBanHienTai() { return giaBanHienTai; }
    public void setGiaBanHienTai(BigDecimal giaBanHienTai) { this.giaBanHienTai = giaBanHienTai; }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PosOrderSummaryDto {
    private Long maHoaDon;
    private Long maChiNhanh;
    private String tenChiNhanh;
    private Long maPos;
    private String trangThaiHoaDon;
    private String trangThaiThanhToan;
    private String phuongThucThanhToan;
    private BigDecimal tongThanhToan;
    private LocalDateTime thoiGianTaoHoaDon;
    private Integer soDong;

    public Long getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(Long maHoaDon) { this.maHoaDon = maHoaDon; }
    public Long getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Long maChiNhanh) { this.maChiNhanh = maChiNhanh; }
    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }
    public Long getMaPos() { return maPos; }
    public void setMaPos(Long maPos) { this.maPos = maPos; }
    public String getTrangThaiHoaDon() { return trangThaiHoaDon; }
    public void setTrangThaiHoaDon(String trangThaiHoaDon) { this.trangThaiHoaDon = trangThaiHoaDon; }
    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public BigDecimal getTongThanhToan() { return tongThanhToan; }
    public void setTongThanhToan(BigDecimal tongThanhToan) { this.tongThanhToan = tongThanhToan; }
    public LocalDateTime getThoiGianTaoHoaDon() { return thoiGianTaoHoaDon; }
    public void setThoiGianTaoHoaDon(LocalDateTime thoiGianTaoHoaDon) { this.thoiGianTaoHoaDon = thoiGianTaoHoaDon; }
    public Integer getSoDong() { return soDong; }
    public void setSoDong(Integer soDong) { this.soDong = soDong; }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PosOrderDto extends PosOrderSummaryDto {
    private Long maNguoiDung;
    private LocalDateTime thoiGianThanhToan;
    private List<PosOrderItemDto> items = new ArrayList<>();
    private PosPaymentDto payment;

    public Long getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Long maNguoiDung) { this.maNguoiDung = maNguoiDung; }
    public LocalDateTime getThoiGianThanhToan() { return thoiGianThanhToan; }
    public void setThoiGianThanhToan(LocalDateTime thoiGianThanhToan) { this.thoiGianThanhToan = thoiGianThanhToan; }
    public List<PosOrderItemDto> getItems() { return items; }
    public void setItems(List<PosOrderItemDto> items) { this.items = items; }
    public PosPaymentDto getPayment() { return payment; }
    public void setPayment(PosPaymentDto payment) { this.payment = payment; }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PosOrderItemDto {
    private Long maCtHoaDon;
    private Long maSanPham;
    private String tenSanPham;
    private Integer soLuong;
    private BigDecimal donGiaBan;
    private BigDecimal thanhTienDong;

    public Long getMaCtHoaDon() { return maCtHoaDon; }
    public void setMaCtHoaDon(Long maCtHoaDon) { this.maCtHoaDon = maCtHoaDon; }
    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGiaBan() { return donGiaBan; }
    public void setDonGiaBan(BigDecimal donGiaBan) { this.donGiaBan = donGiaBan; }
    public BigDecimal getThanhTienDong() { return thanhTienDong; }
    public void setThanhTienDong(BigDecimal thanhTienDong) { this.thanhTienDong = thanhTienDong; }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PosPaymentDto {
    private Long orderCode;
    private BigDecimal soTien;
    private String moTa;
    private String checkoutUrl;
    private String qrCode;
    private String trangThai;

    public Long getOrderCode() { return orderCode; }
    public void setOrderCode(Long orderCode) { this.orderCode = orderCode; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BankQrDto extends PosPaymentDto {
    private Long maHoaDon;

    public Long getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(Long maHoaDon) { this.maHoaDon = maHoaDon; }
  }

  public static class CreatePosOrderRequest {
    private Long maChiNhanh;
    private Long maPos;
    private List<PosOrderItemRequest> items = new ArrayList<>();
    private String ghiChu;

    public Long getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Long maChiNhanh) { this.maChiNhanh = maChiNhanh; }
    public Long getMaPos() { return maPos; }
    public void setMaPos(Long maPos) { this.maPos = maPos; }
    public List<PosOrderItemRequest> getItems() { return items; }
    public void setItems(List<PosOrderItemRequest> items) { this.items = items; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
  }

  public static class PosOrderItemRequest {
    private Long maSanPham;
    private Integer soLuong;

    public PosOrderItemRequest() {}

    public PosOrderItemRequest(Long maSanPham, Integer soLuong) {
      this.maSanPham = maSanPham;
      this.soLuong = soLuong;
    }

    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
  }
}
