package com.coffeechain.service;

import com.coffeechain.dto.request.CreateWastageRequest;
import com.coffeechain.dto.response.WastageLookupResponse;
import com.coffeechain.dto.response.WastageLotResponse;
import com.coffeechain.dto.response.WastageResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.WastageRepository;
import com.coffeechain.security.SessionUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WastageService {
  private static final String ROLE_QUAN_LY_CHI_NHANH = "QUAN_LY_CHI_NHANH";

  private static final String EXPIRED = "EXPIRED";
  private static final String DAMAGED = "DAMAGED";
  private static final String LOST = "LOST";
  private static final String SPILL = "SPILL";

  private final WastageRepository wastageRepository;

  public WastageService(WastageRepository wastageRepository) {
    this.wastageRepository = wastageRepository;
  }

  public WastageLookupResponse getLookups(SessionUser user) {
    Long forcedWarehouseId = resolveWarehouseId(user, null);

    WastageLookupResponse response = new WastageLookupResponse();
    response.setWarehouses(wastageRepository.findWarehouseOptions(forcedWarehouseId));
    response.setIngredients(wastageRepository.findIngredientOptions());

    response.setWastageTypes(
        List.of(
            new WastageLookupResponse.OptionDto(
                null, EXPIRED, "Hết hạn", "Nguyên liệu quá hạn sử dụng"),
            new WastageLookupResponse.OptionDto(null, DAMAGED, "Hư hỏng", "Nguyên liệu bị hư hỏng"),
            new WastageLookupResponse.OptionDto(
                null, LOST, "Thất thoát", "Nguyên liệu bị thất thoát"),
            new WastageLookupResponse.OptionDto(
                null, SPILL, "Đổ vỡ", "Nguyên liệu bị đổ, rơi vãi")));

    return response;
  }

  public List<WastageLotResponse> getAvailableLots(
      SessionUser user, Long maKho, Long maNguyenLieu) {
    if (maNguyenLieu == null || maNguyenLieu <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn nguyên liệu");
    }

    Long allowedWarehouseId = resolveWarehouseId(user, maKho);

    if (allowedWarehouseId == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn kho");
    }

    return wastageRepository.findAvailableLots(allowedWarehouseId, maNguyenLieu);
  }

  public List<WastageResponse> searchWastages(
      SessionUser user,
      Long maKho,
      Long maNguyenLieu,
      String loaiHaoHut,
      LocalDateTime fromDate,
      LocalDateTime toDate,
      String keyword) {
    Long allowedWarehouseId = resolveWarehouseId(user, maKho);
    String normalizedType = normalizeWastageTypeAllowNull(loaiHaoHut);

    validateDateRange(fromDate, toDate);

    return wastageRepository.searchWastages(
        allowedWarehouseId, maNguyenLieu, normalizedType, fromDate, toDate, keyword);
  }

  public WastageResponse getById(SessionUser user, Long id) {
    validateId(id, "Mã phiếu hao hụt không hợp lệ");

    Long forcedWarehouseId = resolveWarehouseId(user, null);

    return wastageRepository
        .findById(id, forcedWarehouseId)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu hao hụt"));
  }

  @Transactional(rollbackFor = Exception.class)
  public WastageResponse createWastage(SessionUser user, CreateWastageRequest request) {
    /*
     * Transaction: TX_CREATE_WASTAGE - Báo cáo hao hụt
     *
     * Flow:
     * 1. Validate kho, nguyên liệu, lô hàng, số lượng hao hụt, loại hao hụt.
     * 2. Kiểm tra quyền xem/thao tác kho:
     *    - ADMIN / QUAN_LY_KHO: được thao tác các kho.
     *    - QUAN_LY_CHI_NHANH: chỉ được thao tác kho của chi nhánh mình.
     * 3. Khóa lô hàng bằng SELECT ... FOR UPDATE để tránh 2 người cùng trừ một lô.
     * 4. Kiểm tra lô thuộc đúng kho và đúng nguyên liệu.
     * 5. Kiểm tra lô còn đủ số lượng để ghi nhận hao hụt.
     * 6. Khóa dòng TONKHO bằng SELECT ... FOR UPDATE.
     * 7. Insert PHIEUHAOHUT.
     * 8. Trừ LOHANG_NGUYENLIEU.so_luong_con_lai.
     * 9. Trừ TONKHO.so_luong_ton.
     * 10. Insert NHATKY_KHO với loai_giao_dich = WASTAGE.
     * 11. Commit nếu thành công, rollback nếu có lỗi.
     */

    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
    }

    Long maKho = request == null ? null : request.getMaKho();
    Long maNguyenLieu = request == null ? null : request.getMaNguyenLieu();
    Long maLoHang = request == null ? null : request.getMaLoHang();
    BigDecimal soLuongHaoHut = request == null ? null : request.getSoLuongHaoHut();
    String loaiHaoHut =
        normalizeWastageTypeRequired(request == null ? null : request.getLoaiHaoHut());
    String ghiChu = normalizeNote(request == null ? null : request.getGhiChu());

    validateCreateInput(maKho, maNguyenLieu, maLoHang, soLuongHaoHut);

    Long allowedWarehouseId = resolveWarehouseId(user, maKho);

    WastageRepository.LotLock lot = wastageRepository.findLotForUpdate(maLoHang);

    if (lot == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy lô hàng");
    }

    if (!lot.getMaKho().equals(allowedWarehouseId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Lô hàng không thuộc kho đã chọn");
    }

    if (!lot.getMaNguyenLieu().equals(maNguyenLieu)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Lô hàng không thuộc nguyên liệu đã chọn");
    }

    if ("USED_UP".equalsIgnoreCase(lot.getTrangThai())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Lô hàng đã dùng hết, không thể báo cáo hao hụt");
    }

    if (lot.getSoLuongConLai().compareTo(soLuongHaoHut) < 0) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Số lượng hao hụt vượt quá số lượng còn lại của lô");
    }

    if (EXPIRED.equals(loaiHaoHut)
        && lot.getHanSuDung() != null
        && !lot.getHanSuDung().isBefore(LocalDate.now())
        && !"EXPIRED".equalsIgnoreCase(lot.getTrangThai())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Lô hàng chưa hết hạn, không thể báo cáo loại hao hụt EXPIRED");
    }

    WastageRepository.StockLock stock =
        wastageRepository.findStockForUpdate(allowedWarehouseId, maNguyenLieu);

    if (stock == null) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Không tìm thấy dòng tồn kho của nguyên liệu trong kho");
    }

    if (stock.getSoLuongTon().compareTo(soLuongHaoHut) < 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Tồn kho tổng không đủ để ghi nhận hao hụt");
    }

    BigDecimal lotAfter = lot.getSoLuongConLai().subtract(soLuongHaoHut);
    BigDecimal stockBefore = stock.getSoLuongTon();
    BigDecimal stockAfter = stockBefore.subtract(soLuongHaoHut);

    Long wastageId =
        wastageRepository.insertWastage(
            allowedWarehouseId,
            maNguyenLieu,
            maLoHang,
            soLuongHaoHut,
            loaiHaoHut,
            ghiChu,
            user.getMaNguoiDung());

    wastageRepository.updateLotAfterWastage(maLoHang, lotAfter);
    wastageRepository.updateStockAfterWastage(stock.getMaTonKho(), stockAfter);

    wastageRepository.insertInventoryLog(
        allowedWarehouseId,
        maNguyenLieu,
        maLoHang,
        wastageId,
        soLuongHaoHut.negate(),
        stockBefore,
        stockAfter,
        user.getMaNguoiDung());

    return getById(user, wastageId);
  }

  private void validateCreateInput(
      Long maKho, Long maNguyenLieu, Long maLoHang, BigDecimal soLuongHaoHut) {
    if (maKho == null || maKho <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn kho");
    }

    if (maNguyenLieu == null || maNguyenLieu <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn nguyên liệu");
    }

    if (maLoHang == null || maLoHang <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn lô hàng");
    }

    if (soLuongHaoHut == null || soLuongHaoHut.compareTo(BigDecimal.ZERO) <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng hao hụt phải lớn hơn 0");
    }
  }

  private Long resolveWarehouseId(SessionUser user, Long requestedWarehouseId) {
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

    Long ownWarehouseId = wastageRepository.findActiveWarehouseIdByBranchId(maChiNhanh);

    if (ownWarehouseId == null) {
      throw new AppException(HttpStatus.FORBIDDEN, "Chi nhánh của tài khoản chưa có kho hoạt động");
    }

    if (requestedWarehouseId != null && !requestedWarehouseId.equals(ownWarehouseId)) {
      throw new AppException(HttpStatus.FORBIDDEN, "Bạn chỉ được thao tác kho của chi nhánh mình");
    }

    return ownWarehouseId;
  }

  private String normalizeWastageTypeAllowNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return normalizeWastageTypeRequired(value);
  }

  private String normalizeWastageTypeRequired(String value) {
    String type = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);

    if (!EXPIRED.equals(type)
        && !DAMAGED.equals(type)
        && !LOST.equals(type)
        && !SPILL.equals(type)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Loại hao hụt không hợp lệ");
    }

    return type;
  }

  private String normalizeNote(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String note = value.trim();

    if (note.length() > 255) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Ghi chú không được vượt quá 255 ký tự");
    }

    return note;
  }

  private void validateDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Ngày bắt đầu không được lớn hơn ngày kết thúc");
    }
  }

  private void validateId(Long id, String message) {
    if (id == null || id <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, message);
    }
  }
}
