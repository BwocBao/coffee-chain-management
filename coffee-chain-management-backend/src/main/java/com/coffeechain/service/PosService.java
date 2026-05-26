package com.coffeechain.service;

import com.coffeechain.dto.request.CreatePosOrderRequest;
import com.coffeechain.dto.request.PosOrderItemRequest;
import com.coffeechain.dto.response.BankQrResponse;
import com.coffeechain.dto.response.PosLookupResponse;
import com.coffeechain.dto.response.PosOrderResponse;
import com.coffeechain.dto.response.PosOrderSummaryResponse;
import com.coffeechain.dto.response.PosProductResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.PosRepository;
import com.coffeechain.repository.PosRepository.OrderRow;
import com.coffeechain.repository.PosRepository.ProductOrderRow;
import com.coffeechain.security.SessionUser;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PosService {
  private final PosRepository posRepository;
  private final PosInventoryDeductionService deductionService;
  private final PayOsPaymentService payOsPaymentService;

  public PosService(
      PosRepository posRepository,
      PosInventoryDeductionService deductionService,
      PayOsPaymentService payOsPaymentService) {
    this.posRepository = posRepository;
    this.deductionService = deductionService;
    this.payOsPaymentService = payOsPaymentService;
  }

  public List<PosProductResponse> getProducts() {
    return posRepository.findPosProducts();
  }

  public PosLookupResponse getLookups(SessionUser user) {
    PosLookupResponse response = new PosLookupResponse();
    Long forcedBranch = forcedBranch(user);
    response.setBranches(posRepository.findBranchOptions(forcedBranch));
    response.setPosDevices(posRepository.findPosOptions(forcedBranch));
    return response;
  }

  public List<PosOrderSummaryResponse> searchOrders(
      Long maChiNhanh, String keyword, String status, Integer limit, SessionUser user) {
    Long forcedBranch = forcedBranch(user);
    if (forcedBranch != null && maChiNhanh != null && !forcedBranch.equals(maChiNhanh)) {
      throw new AppException(HttpStatus.FORBIDDEN, "Khong duoc xem don hang cua chi nhanh khac");
    }

    int safeLimit = limit == null ? 50 : limit;
    return posRepository.searchOrders(forcedBranch, maChiNhanh, keyword, status, safeLimit);
  }

  @Transactional(rollbackFor = Exception.class)
  public PosOrderResponse createOrder(CreatePosOrderRequest request, SessionUser user) {
    validateCreateOrderRequest(request);

    if (!posRepository.branchExists(request.maChiNhanh())) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay chi nhanh dang hoat dong");
    }

    if (!posRepository.posBelongsToBranch(request.maPos(), request.maChiNhanh())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "May POS khong thuoc chi nhanh da chon");
    }

    validateBranchScope(user, request.maChiNhanh());

    BigDecimal total = BigDecimal.ZERO;
    Set<Long> productIds = new HashSet<>();

    for (PosOrderItemRequest item : request.items()) {
      if (!productIds.add(item.maSanPham())) {
        throw new AppException(
            HttpStatus.BAD_REQUEST, "Mot san pham khong duoc nhap lap trong cung hoa don");
      }

      ProductOrderRow product = posRepository.findProductForOrder(item.maSanPham());
      if (product == null) {
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "San pham khong kha dung hoac chua co cong thuc: " + item.maSanPham());
      }

      total = total.add(product.giaBanHienTai().multiply(BigDecimal.valueOf(item.soLuong())));
    }

    Long maHoaDon =
        posRepository.createOrder(
            request.maChiNhanh(), request.maPos(), user.getMaNguoiDung(), total);

    for (PosOrderItemRequest item : request.items()) {
      ProductOrderRow product = posRepository.findProductForOrder(item.maSanPham());
      posRepository.createOrderDetail(maHoaDon, item, product.giaBanHienTai());
    }

    return getOrder(maHoaDon, user);
  }

  public PosOrderResponse getOrder(Long maHoaDon, SessionUser user) {
    OrderRow order = findOrderOrThrow(maHoaDon);
    validateBranchScope(user, order.maChiNhanh());
    return buildOrderResponse(order);
  }

  public void requireOrderAccess(Long maHoaDon, SessionUser user) {
    OrderRow order = findOrderOrThrow(maHoaDon);
    validateBranchScope(user, order.maChiNhanh());
  }

  @Transactional(rollbackFor = Exception.class)
  public PosOrderResponse payCash(Long maHoaDon, SessionUser user) {
    OrderRow order = lockOrderOrThrow(maHoaDon);
    validateBranchScope(user, order.maChiNhanh());

    if (!"PENDING".equals(order.trangThaiHoaDon())
        || !"PENDING".equals(order.trangThaiThanhToan())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Hoa don khong o trang thai cho thanh toan");
    }

    posRepository.markOrderPaid(maHoaDon, "CASH");
    deductionService.completePaidOrderAndDeductStock(maHoaDon);

    return getOrder(maHoaDon, user);
  }

  @Transactional(rollbackFor = Exception.class)
  public BankQrResponse createBankQr(Long maHoaDon, SessionUser user) {
    OrderRow order = findOrderOrThrow(maHoaDon);
    validateBranchScope(user, order.maChiNhanh());
    return payOsPaymentService.createBankQr(maHoaDon);
  }

  @Transactional(rollbackFor = Exception.class)
  public PosOrderResponse cancelOrder(Long maHoaDon, SessionUser user) {
    OrderRow order = lockOrderOrThrow(maHoaDon);
    validateBranchScope(user, order.maChiNhanh());

    if (!"PENDING".equals(order.trangThaiHoaDon())
        || !"PENDING".equals(order.trangThaiThanhToan())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Chi co the huy hoa don dang cho thanh toan");
    }

    posRepository.cancelPendingPayOsPaymentForOrder(maHoaDon);
    posRepository.cancelOrder(maHoaDon);
    return getOrder(maHoaDon, user);
  }

  private OrderRow findOrderOrThrow(Long maHoaDon) {
    return posRepository
        .findOrder(maHoaDon)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));
  }

  private OrderRow lockOrderOrThrow(Long maHoaDon) {
    return posRepository
        .lockOrder(maHoaDon)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));
  }

  private PosOrderResponse buildOrderResponse(OrderRow order) {
    Long maHoaDon = order.maHoaDon();
    return new PosOrderResponse(
        order.maHoaDon(),
        order.maChiNhanh(),
        order.tenChiNhanh(),
        order.maPos(),
        order.maNguoiDung(),
        order.trangThaiHoaDon(),
        order.trangThaiThanhToan(),
        order.phuongThucThanhToan(),
        order.tongThanhToan(),
        order.thoiGianTaoHoaDon(),
        order.thoiGianThanhToan(),
        posRepository.findOrderItems(maHoaDon),
        posRepository.findLatestPayOsPayment(maHoaDon).orElse(null));
  }

  private Long forcedBranch(SessionUser user) {
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chua dang nhap hoac token het han");
    }

    String role = user.getTenVaiTro() == null ? "" : user.getTenVaiTro().trim().toUpperCase();
    if ("ADMIN".equals(role)) {
      return null;
    }

    Long userBranchId = user.getMaChiNhanh();
    if (userBranchId == null) {
      throw new AppException(HttpStatus.FORBIDDEN, "Tai khoan chua gan chi nhanh");
    }
    return userBranchId;
  }

  private void validateBranchScope(SessionUser user, Long maChiNhanh) {
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chua dang nhap hoac token het han");
    }

    String role = user.getTenVaiTro() == null ? "" : user.getTenVaiTro().trim().toUpperCase();
    if ("ADMIN".equals(role)) {
      return;
    }

    Long userBranchId = user.getMaChiNhanh();
    if (userBranchId != null && userBranchId.equals(maChiNhanh)) {
      return;
    }

    throw new AppException(HttpStatus.FORBIDDEN, "Khong duoc thao tac don hang cua chi nhanh khac");
  }

  private void validateCreateOrderRequest(CreatePosOrderRequest request) {
    if (request == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Thieu du lieu hoa don");
    }

    if (request.maChiNhanh() == null || request.maChiNhanh() <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Ma chi nhanh khong hop le");
    }

    if (request.maPos() == null || request.maPos() <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Ma POS khong hop le");
    }

    if (request.items() == null || request.items().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Hoa don phai co it nhat mot san pham");
    }

    for (PosOrderItemRequest item : request.items()) {
      if (item == null || item.maSanPham() == null || item.maSanPham() <= 0) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Ma san pham khong hop le");
      }

      if (item.soLuong() == null || item.soLuong() <= 0) {
        throw new AppException(HttpStatus.BAD_REQUEST, "So luong san pham phai lon hon 0");
      }
    }
  }
}
