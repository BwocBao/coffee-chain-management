package com.coffeechain.service;

import com.coffeechain.dto.response.BankQrResponse;
import com.coffeechain.dto.response.PayOsWebhookResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.PosRepository;
import com.coffeechain.repository.PosRepository.OrderRow;
import com.coffeechain.repository.PosRepository.PayOsPaymentRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;

@Service
public class PayOsPaymentService {
    private final PosRepository posRepository;
    private final PosInventoryDeductionService deductionService;
    private final ObjectMapper objectMapper;

    @Value("${payos.client-id:}")
    private String clientId;

    @Value("${payos.api-key:}")
    private String apiKey;

    @Value("${payos.checksum-key:}")
    private String checksumKey;

    @Value("${payos.return-url:}")
    private String returnUrl;

    @Value("${payos.cancel-url:}")
    private String cancelUrl;

    public PayOsPaymentService(
            PosRepository posRepository,
            PosInventoryDeductionService deductionService,
            ObjectMapper objectMapper
    ) {
        this.posRepository = posRepository;
        this.deductionService = deductionService;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public BankQrResponse createBankQr(Long maHoaDon) {
        OrderRow order = posRepository.lockOrder(maHoaDon)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don"));

        if (!"PENDING".equals(order.trangThaiHoaDon())
                || !"PENDING".equals(order.trangThaiThanhToan())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hoa don khong o trang thai cho thanh toan");
        }

        PayOsPaymentRow existing = posRepository.findPayOsPaymentForOrder(maHoaDon).orElse(null);
        if (existing != null && "PENDING".equals(existing.trangThai())) {
            return toBankQrResponse(existing);
        }

        ensurePayOsConfigured();

        Long orderCode = generateOrderCode(maHoaDon);
        String description = "PL" + maHoaDon;
        Long amount = toPayOsAmount(order.tongThanhToan());

        CreatePaymentLinkRequest request = new CreatePaymentLinkRequest();
        request.setOrderCode(orderCode);
        request.setAmount(amount);
        request.setDescription(description);
        request.setReturnUrl(returnUrl);
        request.setCancelUrl(cancelUrl);

        try {
            CreatePaymentLinkResponse response = new PayOS(clientId, apiKey, checksumKey)
                    .paymentRequests()
                    .create(request);

            Long maThanhToan = posRepository.createPayOsPayment(
                    maHoaDon,
                    orderCode,
                    response.getPaymentLinkId(),
                    response.getCheckoutUrl(),
                    response.getQrCode(),
                    BigDecimal.valueOf(amount),
                    description,
                    "PENDING"
            );

            PayOsPaymentRow saved = posRepository.findPayOsPaymentById(maThanhToan)
                    .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Khong doc duoc giao dich payOS vua tao"));
            return toBankQrResponse(saved);
        } catch (Exception ex) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "Khong tao duoc QR payOS: " + rootMessage(ex));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PayOsWebhookResponse handleWebhook(String rawBody) {
        ensurePayOsConfigured();

        Webhook webhook;
        WebhookData data;
        try {
            webhook = objectMapper.readValue(rawBody, Webhook.class);
            data = new PayOS(clientId, apiKey, checksumKey).webhooks().verify(webhook);
        } catch (Exception ex) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Webhook payOS khong hop le: " + rootMessage(ex));
        }

        if (data == null || data.getOrderCode() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Webhook payOS thieu orderCode");
        }

        PayOsPaymentRow payment = posRepository.lockPayOsPaymentByOrderCode(data.getOrderCode())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay giao dich payOS"));

        OrderRow order = posRepository.lockOrder(payment.maHoaDon())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Khong tim thay hoa don payOS"));

        if ("COMPLETED".equals(order.trangThaiHoaDon())) {
            posRepository.updatePayOsRawWebhook(payment.maThanhToan(), data.getReference(), rawBody);
            return new PayOsWebhookResponse(payment.orderCode(), payment.maHoaDon(), "ALREADY_COMPLETED");
        }

        if (isSuccessWebhook(webhook, data)) {
            BigDecimal paidAmount = BigDecimal.valueOf(data.getAmount() == null ? 0L : data.getAmount());
            if (paidAmount.compareTo(payment.soTien()) != 0 || paidAmount.compareTo(order.tongThanhToan()) != 0) {
                posRepository.markPayOsFailed(payment.maThanhToan(), data.getReference(), rawBody);
                throw new AppException(HttpStatus.BAD_REQUEST, "So tien webhook payOS khong khop hoa don");
            }

            posRepository.markPayOsPaid(payment.maThanhToan(), data.getReference(), rawBody);
            posRepository.markOrderPaid(payment.maHoaDon(), "BANK_TRANSFER");
            deductionService.completePaidOrderAndDeductStock(payment.maHoaDon());
            return new PayOsWebhookResponse(payment.orderCode(), payment.maHoaDon(), "COMPLETED");
        }

        if (isCancelledWebhook(webhook, data)) {
            posRepository.markPayOsCancelled(payment.maThanhToan(), data.getReference(), rawBody);
            posRepository.cancelOrder(payment.maHoaDon());
            return new PayOsWebhookResponse(payment.orderCode(), payment.maHoaDon(), "CANCELLED");
        }

        posRepository.markPayOsFailed(payment.maThanhToan(), data.getReference(), rawBody);
        posRepository.cancelOrder(payment.maHoaDon());
        return new PayOsWebhookResponse(payment.orderCode(), payment.maHoaDon(), "FAILED");
    }

    private BankQrResponse toBankQrResponse(PayOsPaymentRow payment) {
        return new BankQrResponse(
                payment.maHoaDon(),
                payment.orderCode(),
                payment.soTien(),
                payment.moTa(),
                payment.checkoutUrl(),
                payment.qrCode(),
                payment.trangThai()
        );
    }

    private Long generateOrderCode(Long maHoaDon) {
        long suffix = System.currentTimeMillis() % 100000L;
        return maHoaDon * 100000L + suffix;
    }

    private Long toPayOsAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tong thanh toan khong hop le");
        }
        return amount.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
    }

    private boolean isSuccessWebhook(Webhook webhook, WebhookData data) {
        return Boolean.TRUE.equals(webhook.getSuccess())
                || "00".equals(data.getCode())
                || "PAID".equalsIgnoreCase(data.getDesc());
    }

    private boolean isCancelledWebhook(Webhook webhook, WebhookData data) {
        String code = data.getCode();
        String desc = data.getDesc();
        return "CANCELLED".equalsIgnoreCase(code)
                || "CANCELLED".equalsIgnoreCase(desc)
                || "CANCELED".equalsIgnoreCase(code)
                || "CANCELED".equalsIgnoreCase(desc);
    }

    private void ensurePayOsConfigured() {
        if (isBlank(clientId) || isBlank(apiKey) || isBlank(checksumKey) || isBlank(returnUrl) || isBlank(cancelUrl)
                || "YOUR_CLIENT_ID".equals(clientId) || "YOUR_API_KEY".equals(apiKey) || "YOUR_CHECKSUM_KEY".equals(checksumKey)) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Chua cau hinh payOS trong backend");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String rootMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}