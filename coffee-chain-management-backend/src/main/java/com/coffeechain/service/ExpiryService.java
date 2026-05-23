package com.coffeechain.service;

import com.coffeechain.dto.response.ExpiryLookupResponse;
import com.coffeechain.dto.response.ExpiryLotResponse;
import com.coffeechain.dto.response.ExpiryRefreshResponse;
import com.coffeechain.dto.response.ExpiryStatisticsResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.ExpiryRepository;
import com.coffeechain.security.SessionUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ExpiryService {
  private static final String ROLE_QUAN_LY_CHI_NHANH = "QUAN_LY_CHI_NHANH";

  private static final String STATUS_ACTIVE = "ACTIVE";
  private static final String STATUS_EXPIRED = "EXPIRED";
  private static final String STATUS_USED_UP = "USED_UP";

  private static final String WARNING_HET_HANG = "HET_HANG";
  private static final String WARNING_KHONG_CO_HSD = "KHONG_CO_HSD";
  private static final String WARNING_DA_HET_HAN = "DA_HET_HAN";
  private static final String WARNING_SAP_HET_HAN = "SAP_HET_HAN";
  private static final String WARNING_BINH_THUONG = "BINH_THUONG";

  private final ExpiryRepository expiryRepository;

  public ExpiryService(ExpiryRepository expiryRepository) {
    this.expiryRepository = expiryRepository;
  }

  public ExpiryLookupResponse getLookups(SessionUser user) {
    Long forcedWarehouseId = resolveForcedWarehouseId(user, null);

    ExpiryLookupResponse response = new ExpiryLookupResponse();

    response.setWarehouses(expiryRepository.findWarehouseOptions(forcedWarehouseId));
    response.setIngredients(expiryRepository.findIngredientOptions());

    response.setStatuses(
        List.of(
            new ExpiryLookupResponse.OptionDto(
                null, STATUS_ACTIVE, "Đang hoạt động", "Lô còn sử dụng"),
            new ExpiryLookupResponse.OptionDto(
                null, STATUS_EXPIRED, "Đã hết hạn", "Lô quá hạn sử dụng"),
            new ExpiryLookupResponse.OptionDto(
                null, STATUS_USED_UP, "Đã dùng hết", "Lô không còn số lượng")));

    response.setWarningLevels(
        List.of(
            new ExpiryLookupResponse.OptionDto(
                null, WARNING_DA_HET_HAN, "Đã hết hạn", "Lô đã quá hạn sử dụng"),
            new ExpiryLookupResponse.OptionDto(
                null, WARNING_SAP_HET_HAN, "Sắp hết hạn", "Lô còn hạn nhưng sắp hết hạn"),
            new ExpiryLookupResponse.OptionDto(
                null, WARNING_BINH_THUONG, "Bình thường", "Lô còn hạn dài"),
            new ExpiryLookupResponse.OptionDto(
                null, WARNING_KHONG_CO_HSD, "Không có hạn sử dụng", "Lô không nhập hạn sử dụng"),
            new ExpiryLookupResponse.OptionDto(
                null, WARNING_HET_HANG, "Hết hàng", "Lô đã dùng hết")));

    return response;
  }

  public List<ExpiryLotResponse> searchLots(
      SessionUser user,
      Long maKho,
      Long maNguyenLieu,
      String trangThai,
      String mucCanhBao,
      Integer daysToExpire,
      Boolean onlyAvailable,
      Integer warningDays) {
    Long allowedWarehouseId = resolveForcedWarehouseId(user, maKho);

    String normalizedStatus = normalizeStatusAllowNull(trangThai);
    String normalizedWarning = normalizeWarningAllowNull(mucCanhBao);
    Integer safeWarningDays = normalizeWarningDays(warningDays);
    Integer safeDaysToExpire = normalizeDaysToExpire(daysToExpire);

    return expiryRepository.searchLots(
        allowedWarehouseId,
        maNguyenLieu,
        normalizedStatus,
        normalizedWarning,
        safeDaysToExpire,
        onlyAvailable,
        safeWarningDays);
  }

  public ExpiryLotResponse getLotById(SessionUser user, Long maLoHang) {
    validateId(maLoHang, "Mã lô hàng không hợp lệ");

    Long forcedWarehouseId = resolveForcedWarehouseId(user, null);

    return expiryRepository.findLotById(maLoHang, forcedWarehouseId).stream()
        .findFirst()
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy lô hàng"));
  }

  public ExpiryStatisticsResponse getStatistics(SessionUser user, Long maKho, Integer warningDays) {
    Long allowedWarehouseId = resolveForcedWarehouseId(user, maKho);
    Integer safeWarningDays = normalizeWarningDays(warningDays);

    return expiryRepository.getStatistics(allowedWarehouseId, safeWarningDays);
  }

  public ExpiryRefreshResponse refreshExpiredLots() {
    /*
     * Flow:
     * 1. Gọi procedure prc_cap_nhat_lo_het_han trong Oracle.
     * 2. Procedure cập nhật trạng thái lô:
     *    - so_luong_con_lai = 0 -> USED_UP
     *    - han_su_dung < hôm nay -> EXPIRED
     * 3. Trả thời điểm refresh cho frontend.
     */
    expiryRepository.refreshExpiredLots();

    return new ExpiryRefreshResponse(
        "Đã rà soát và cập nhật trạng thái lô hết hạn", LocalDateTime.now());
  }

  private Long resolveForcedWarehouseId(SessionUser user, Long requestedWarehouseId) {
    /*
     * ADMIN và QUAN_LY_KHO:
     * - Được xem tất cả kho.
     *
     * QUAN_LY_CHI_NHANH:
     * - Chỉ được xem kho thuộc chi nhánh của mình.
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
      throw new AppException(
          HttpStatus.FORBIDDEN, "Tài khoản quản lý chi nhánh chưa được gán chi nhánh");
    }

    Long ownWarehouseId = expiryRepository.findActiveWarehouseIdByBranchId(maChiNhanh);
    if (ownWarehouseId == null) {
      throw new AppException(HttpStatus.FORBIDDEN, "Chi nhánh của tài khoản chưa có kho hoạt động");
    }

    if (requestedWarehouseId != null && !requestedWarehouseId.equals(ownWarehouseId)) {
      throw new AppException(
          HttpStatus.FORBIDDEN, "Bạn chỉ được xem lô hàng thuộc kho của chi nhánh mình");
    }

    return ownWarehouseId;
  }

  private String normalizeStatusAllowNull(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }

    String value = status.trim().toUpperCase(Locale.ROOT);

    if (!STATUS_ACTIVE.equals(value)
        && !STATUS_EXPIRED.equals(value)
        && !STATUS_USED_UP.equals(value)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái lô hàng không hợp lệ");
    }

    return value;
  }

  private String normalizeWarningAllowNull(String warning) {
    if (warning == null || warning.isBlank()) {
      return null;
    }

    String value = warning.trim().toUpperCase(Locale.ROOT);

    if (!WARNING_HET_HANG.equals(value)
        && !WARNING_KHONG_CO_HSD.equals(value)
        && !WARNING_DA_HET_HAN.equals(value)
        && !WARNING_SAP_HET_HAN.equals(value)
        && !WARNING_BINH_THUONG.equals(value)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Mức cảnh báo hạn sử dụng không hợp lệ");
    }

    return value;
  }

  private Integer normalizeWarningDays(Integer warningDays) {
    if (warningDays == null) {
      return 30;
    }

    if (warningDays < 1 || warningDays > 365) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Số ngày cảnh báo phải nằm trong khoảng 1 đến 365");
    }

    return warningDays;
  }

  private Integer normalizeDaysToExpire(Integer daysToExpire) {
    if (daysToExpire == null) {
      return null;
    }

    if (daysToExpire < 0 || daysToExpire > 365) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Số ngày lọc hạn sử dụng phải nằm trong khoảng 0 đến 365");
    }

    return daysToExpire;
  }

  private void validateId(Long id, String message) {
    if (id == null || id <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, message);
    }
  }
}
