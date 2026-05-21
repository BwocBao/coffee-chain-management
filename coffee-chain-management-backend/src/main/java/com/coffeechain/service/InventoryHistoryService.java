package com.coffeechain.service;

import com.coffeechain.dto.response.InventoryHistoryLookupResponse;
import com.coffeechain.dto.response.InventoryHistoryResponse;
import com.coffeechain.dto.response.InventoryHistorySummaryResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.InventoryHistoryRepository;
import com.coffeechain.security.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class InventoryHistoryService {
    private static final String ROLE_QUAN_LY_CHI_NHANH = "QUAN_LY_CHI_NHANH";

    private final InventoryHistoryRepository historyRepository;

    public InventoryHistoryService(InventoryHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public InventoryHistoryLookupResponse getLookups(SessionUser user) {
        Long forcedWarehouseId = resolveForcedWarehouseId(user, null);

        InventoryHistoryLookupResponse response = new InventoryHistoryLookupResponse();

        response.setWarehouses(historyRepository.findWarehouseOptions(forcedWarehouseId));
        response.setIngredients(historyRepository.findIngredientOptions());
        response.setTransactionTypes(buildTransactionTypeOptions());

        return response;
    }

    public List<InventoryHistoryResponse> searchHistory(
            SessionUser user,
            Long maKho,
            Long maNguyenLieu,
            Long maLoHang,
            String loaiGiaoDich,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String keyword
    ) {
        Long allowedWarehouseId = resolveForcedWarehouseId(user, maKho);
        String normalizedType = normalizeTransactionTypeAllowNull(loaiGiaoDich);

        validateDateRange(fromDate, toDate);

        return historyRepository.searchHistory(
                allowedWarehouseId,
                maNguyenLieu,
                maLoHang,
                normalizedType,
                fromDate,
                toDate,
                keyword
        );
    }

    public InventoryHistoryResponse getHistoryById(SessionUser user, Long id) {
        validateId(id, "Mã nhật ký kho không hợp lệ");

        Long forcedWarehouseId = resolveForcedWarehouseId(user, null);

        return historyRepository.findHistoryById(id, forcedWarehouseId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhật ký kho"));
    }

    public List<InventoryHistorySummaryResponse> getSummary(
            SessionUser user,
            Long maKho,
            Long maNguyenLieu,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        Long allowedWarehouseId = resolveForcedWarehouseId(user, maKho);

        validateDateRange(fromDate, toDate);

        return historyRepository.getSummary(
                allowedWarehouseId,
                maNguyenLieu,
                fromDate,
                toDate
        );
    }

    private Long resolveForcedWarehouseId(SessionUser user, Long requestedWarehouseId) {
        /*
         * ADMIN và QUAN_LY_KHO:
         * - Được xem toàn bộ lịch sử kho.
         *
         * QUAN_LY_CHI_NHANH:
         * - Chỉ được xem lịch sử kho thuộc chi nhánh của mình.
         * - Nếu cố truyền maKho khác thì báo lỗi 403.
         */
        if (user == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }

        String role = user.getTenVaiTro();

        if (!ROLE_QUAN_LY_CHI_NHANH.equalsIgnoreCase(role)) {
            return requestedWarehouseId;
        }

        Long maChiNhanh = user.getMaChiNhanh();

        if (maChiNhanh == null) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản quản lý chi nhánh chưa được gán chi nhánh");
        }

        Long ownWarehouseId = historyRepository.findActiveWarehouseIdByBranchId(maChiNhanh);

        if (ownWarehouseId == null) {
            throw new AppException(HttpStatus.FORBIDDEN, "Chi nhánh của tài khoản chưa có kho hoạt động");
        }

        if (requestedWarehouseId != null && !requestedWarehouseId.equals(ownWarehouseId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn chỉ được xem lịch sử kho của chi nhánh mình");
        }

        return ownWarehouseId;
    }

    private List<InventoryHistoryLookupResponse.OptionDto> buildTransactionTypeOptions() {
        return List.of(
                new InventoryHistoryLookupResponse.OptionDto(null, "IMPORT", "Nhập kho", "Tăng tồn từ phiếu nhập"),
                new InventoryHistoryLookupResponse.OptionDto(null, "EXPORT", "Xuất kho", "Giảm tồn từ phiếu xuất"),
                new InventoryHistoryLookupResponse.OptionDto(null, "TRANSFER_IN", "Điều chuyển vào", "Tăng tồn do nhận điều chuyển"),
                new InventoryHistoryLookupResponse.OptionDto(null, "TRANSFER_OUT", "Điều chuyển ra", "Giảm tồn do xuất điều chuyển"),
                new InventoryHistoryLookupResponse.OptionDto(null, "WASTAGE", "Hao hụt", "Giảm tồn do hư hỏng, mất mát, hết hạn"),
                new InventoryHistoryLookupResponse.OptionDto(null, "SALE_DEDUCT", "Bán hàng trừ kho", "Giảm tồn do POS bán hàng"),
                new InventoryHistoryLookupResponse.OptionDto(null, "SALE_REVERSE", "Hoàn trừ kho", "Cộng lại tồn khi hoàn/hủy bán hàng"),
                new InventoryHistoryLookupResponse.OptionDto(null, "STOCKTAKE_ADJUST", "Điều chỉnh kiểm kho", "Tăng hoặc giảm tồn sau kiểm kho")
        );
    }

    private String normalizeTransactionTypeAllowNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String type = value.trim().toUpperCase(Locale.ROOT);

        boolean valid = List.of(
                "IMPORT",
                "EXPORT",
                "TRANSFER_IN",
                "TRANSFER_OUT",
                "WASTAGE",
                "SALE_DEDUCT",
                "SALE_REVERSE",
                "STOCKTAKE_ADJUST"
        ).contains(type);

        if (!valid) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Loại giao dịch kho không hợp lệ");
        }

        return type;
    }

    private void validateDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }
    }

    private void validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, message);
        }
    }
}