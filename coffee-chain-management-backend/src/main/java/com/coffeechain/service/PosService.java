package com.coffeechain.service;

import com.coffeechain.dto.request.CreatePosOrderRequest;
import com.coffeechain.dto.request.PosOrderItemRequest;
import com.coffeechain.dto.response.BankQrResponse;
import com.coffeechain.dto.response.PosOrderResponse;
import com.coffeechain.dto.response.PosProductResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.PosRepository;
import com.coffeechain.repository.PosRepository.OrderRow;
import com.coffeechain.repository.PosRepository.ProductOrderRow;
import com.coffeechain.security.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PosService {
    private final PosRepository posRepository;
    private final PosInventoryDeductionService deductionService;
    private final PayOsPaymentService payOsPaymentService;

    public PosService(
            PosRepository posRepository,
            PosInventoryDeductionService deductionService,
            PayOsPaymentService payOsPaymentService
    ) {
        this.posRepository = posRepository;
        this.deductionService = deductionService;
        this.payOsPaymentService = payOsPaymentService;
    }

    public List<PosProductResponse> getProducts() {
        return posRepository.findPosProducts();
    }

    @Transactional(rollbackFor = Exception.class)
    public PosOrderResponse createOrder(CreatePosOrderRequest request, SessionUser user) {
        validateCreateOrderRequest(request);

        if (!posRepository.branchExists(request.maChiNhanh())) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy chi nhánh đang hoạt động");
        }

        if (!posRepository.posBelongsToBranch(request.maPos(), request.maChiNhanh())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Máy POS không thuộc chi nhánh đã chọn");
        }

        BigDecimal total = BigDecimal.ZERO;
        Set<Long> productIds = new HashSet<>();

        for (PosOrderItemRequest item : request.items()) {
            if (!productIds.add(item.maSanPham())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Một sản phẩm không được nhập lặp trong cùng hóa đơn");
            }

            ProductOrderRow product = posRepository.findProductForOrder(item.maSanPham());
            if (product == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm không khả dụng hoặc chưa có công thức: " + item.maSanPham());
            }

            total = total.add(product.giaBanHienTai().multiply(BigDecimal.valueOf(item.soLuong())));
        }

        Long maHoaDon = posRepository.createOrder(
                request.maChiNhanh(),
                request.maPos(),
                user.getMaNguoiDung(),
                total
        );

        for (PosOrderItemRequest item : request.items()) {
            ProductOrderRow product = posRepository.findProductForOrder(item.maSanPham());
            posRepository.createOrderDetail(maHoaDon, item, product.giaBanHienTai());
        }

        return getOrder(maHoaDon);
    }

    public PosOrderResponse getOrder(Long maHoaDon) {
        OrderRow order = posRepository.findOrder(maHoaDon)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));

        return new PosOrderResponse(
                order.maHoaDon(),
                order.maChiNhanh(),
                order.maPos(),
                order.maNguoiDung(),
                order.trangThaiHoaDon(),
                order.trangThaiThanhToan(),
                order.phuongThucThanhToan(),
                order.tongThanhToan(),
                order.thoiGianTaoHoaDon(),
                order.thoiGianThanhToan(),
                posRepository.findOrderItems(maHoaDon),
                posRepository.findLatestPayOsPayment(maHoaDon).orElse(null)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public PosOrderResponse payCash(Long maHoaDon) {
        OrderRow order = posRepository.lockOrder(maHoaDon)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));

        if (!"PENDING".equals(order.trangThaiHoaDon())
                || !"PENDING".equals(order.trangThaiThanhToan())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hóa đơn không ở trạng thái chờ thanh toán");
        }

        posRepository.markOrderPaid(maHoaDon, "CASH");
        deductionService.completePaidOrderAndDeductStock(maHoaDon);

        return getOrder(maHoaDon);
    }


    @Transactional(rollbackFor = Exception.class)
    public BankQrResponse createBankQr(Long maHoaDon) {
        return payOsPaymentService.createBankQr(maHoaDon);
    }

    @Transactional(rollbackFor = Exception.class)
    public PosOrderResponse cancelOrder(Long maHoaDon) {
        OrderRow order = posRepository.lockOrder(maHoaDon)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));

        if (!"PENDING".equals(order.trangThaiHoaDon())
                || !"PENDING".equals(order.trangThaiThanhToan())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Chi co the huy hoa don dang cho thanh toan");
        }

        posRepository.cancelPendingPayOsPaymentForOrder(maHoaDon);
        posRepository.cancelOrder(maHoaDon);
        return getOrder(maHoaDon);
    }
    private void validateCreateOrderRequest(CreatePosOrderRequest request) {
        if (request == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu hóa đơn");
        }

        if (request.maChiNhanh() == null || request.maChiNhanh() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã chi nhánh không hợp lệ");
        }

        if (request.maPos() == null || request.maPos() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã POS không hợp lệ");
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hóa đơn phải có ít nhất một sản phẩm");
        }

        for (PosOrderItemRequest item : request.items()) {
            if (item == null || item.maSanPham() == null || item.maSanPham() <= 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Mã sản phẩm không hợp lệ");
            }

            if (item.soLuong() == null || item.soLuong() <= 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm phải lớn hơn 0");
            }
        }
    }
}