package com.coffeechain.service;

import com.coffeechain.dto.request.CreateStocktakeRequest;
import com.coffeechain.dto.request.StocktakeItemRequest;
import com.coffeechain.dto.request.UpdateStocktakeRequest;
import com.coffeechain.dto.response.StocktakeItemResponse;
import com.coffeechain.dto.response.StocktakeLookupResponse;
import com.coffeechain.dto.response.StocktakeResponse;
import com.coffeechain.dto.response.StocktakeSystemStockResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.StocktakeRepository;
import com.coffeechain.security.SessionUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StocktakeService {
  private static final String ROLE_QUAN_LY_CHI_NHANH = "QUAN_LY_CHI_NHANH";

  private static final String DRAFT = "DRAFT";
  private static final String COMPLETED = "COMPLETED";
  private static final String CANCELLED = "CANCELLED";

  private static final String NO_ACTION = "NO_ACTION";
  private static final String ADJUST_STOCK = "ADJUST_STOCK";
  private static final String CREATE_WASTAGE = "CREATE_WASTAGE";

  private final StocktakeRepository stocktakeRepository;

  public StocktakeService(StocktakeRepository stocktakeRepository) {
    this.stocktakeRepository = stocktakeRepository;
  }

  public StocktakeLookupResponse getLookups(SessionUser user) {
    Long forcedWarehouseId = resolveWarehouseId(user, null);

    StocktakeLookupResponse response = new StocktakeLookupResponse();
    response.setWarehouses(stocktakeRepository.findWarehouseOptions(forcedWarehouseId));
    response.setIngredients(stocktakeRepository.findIngredientOptions());

    response.setHandlingOptions(
        List.of(
            new StocktakeLookupResponse.OptionDto(
                null, NO_ACTION, "Không xử lý", "Chỉ ghi nhận chênh lệch"),
            new StocktakeLookupResponse.OptionDto(
                null,
                ADJUST_STOCK,
                "Điều chỉnh tồn",
                "Cộng tồn kho theo số lượng thực tế"),
            new StocktakeLookupResponse.OptionDto(
                null,
                CREATE_WASTAGE,
                "Tạo hao hụt",
                "Trừ tồn kho theo hao hụt")));

    response.setStatuses(
        List.of(
            new StocktakeLookupResponse.OptionDto(
                null, DRAFT, "Phiếu nháp", "Phiếu đang nhập dữ liệu"),
            new StocktakeLookupResponse.OptionDto(
                null, COMPLETED, "Hoàn tất", "Phiếu đã hoàn tất kiểm kho"),
            new StocktakeLookupResponse.OptionDto(null, CANCELLED, "Đã hủy", "Phiếu đã bị hủy")));

    return response;
  }

  public List<StocktakeSystemStockResponse> getSystemStock(
      SessionUser user, Long maKho, Long maNguyenLieu) {
    Long allowedWarehouseId = resolveWarehouseId(user, maKho);

    if (allowedWarehouseId == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn kho");
    }

    return stocktakeRepository.findSystemStock(allowedWarehouseId, maNguyenLieu);
  }

  public List<StocktakeResponse> searchStocktakes(
      SessionUser user,
      Long maKho,
      String trangThai,
      LocalDateTime fromDate,
      LocalDateTime toDate,
      String keyword) {
    Long allowedWarehouseId = resolveWarehouseId(user, maKho);
    String status = normalizeStatusAllowNull(trangThai);
    validateDateRange(fromDate, toDate);

    List<StocktakeResponse> rows =
        stocktakeRepository.searchStocktakes(allowedWarehouseId, status, fromDate, toDate, keyword);

    for (StocktakeResponse row : rows) {
      row.setItems(stocktakeRepository.findItems(row.getMaPhieuKiemKho()));
    }

    return rows;
  }

  public StocktakeResponse getById(SessionUser user, Long id) {
    validateId(id, "Mã phiếu kiểm kho không hợp lệ");

    Long forcedWarehouseId = resolveWarehouseId(user, null);

    StocktakeResponse response =
        stocktakeRepository
            .findById(id, forcedWarehouseId)
            .orElseThrow(
                () -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu kiểm kho"));

    response.setItems(stocktakeRepository.findItems(id));
    return response;
  }

  @Transactional(rollbackFor = Exception.class)
  public StocktakeResponse createStocktake(SessionUser user, CreateStocktakeRequest request) {
    /*
     * Transaction: TX_CREATE_STOCKTAKE - Tạo phiếu kiểm kho
     *
     * Flow:
     * 1. Validate kho, ghi chú và danh sách dòng kiểm kho.
     * 2. Kiểm tra quyền kho:
     *    - ADMIN / QUAN_LY_KHO: được tạo phiếu cho các kho.
     *    - QUAN_LY_CHI_NHANH: chỉ được tạo phiếu cho kho của chi nhánh mình.
     * 3. Insert PHIEUKIEMKHO với trạng thái DRAFT.
     * 4. Insert nhiều dòng CHITIETPHIEUKIEMKHO.
     * 5. Tính sẵn:
     *    - so_luong_chenh_lech = so_luong_thuc_te - so_luong_he_thong.
     *    - ty_le_chenh_lech = chênh lệch / hệ thống * 100.
     * 6. Return phiếu vừa tạo.
     */

    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
    }

    Long maKho = request == null ? null : request.getMaKho();
    String ghiChu = normalizeNote(request == null ? null : request.getGhiChu());

    Long allowedWarehouseId = resolveWarehouseId(user, maKho);

    validateItems(request == null ? null : request.getItems());

    Long stocktakeId =
        stocktakeRepository.insertHeader(allowedWarehouseId, user.getMaNguoiDung(), ghiChu);

    insertItems(stocktakeId, request.getItems());

    return getById(user, stocktakeId);
  }

  @Transactional(rollbackFor = Exception.class)
  public StocktakeResponse updateStocktake(
      SessionUser user, Long id, UpdateStocktakeRequest request) {
    /*
     * Transaction: TX_UPDATE_STOCKTAKE - Cập nhật phiếu kiểm kho
     *
     * Flow:
     * 1. Khóa phiếu kiểm kho bằng SELECT ... FOR UPDATE.
     * 2. Chỉ cho sửa khi phiếu còn DRAFT.
     * 3. Validate kho và danh sách chi tiết mới.
     * 4. Update PHIEUKIEMKHO.
     * 5. Xóa các dòng CHITIETPHIEUKIEMKHO cũ.
     * 6. Insert lại danh sách dòng kiểm kho mới.
     * 7. Return dữ liệu sau cập nhật.
     */

    validateId(id, "Mã phiếu kiểm kho không hợp lệ");

    StocktakeRepository.HeaderLock header = stocktakeRepository.findHeaderForUpdate(id);

    if (header == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu kiểm kho");
    }

    if (!DRAFT.equals(header.getTrangThai())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Chỉ được sửa phiếu kiểm kho ở trạng thái DRAFT");
    }

    Long maKho = request == null ? null : request.getMaKho();
    String ghiChu = normalizeNote(request == null ? null : request.getGhiChu());

    Long allowedWarehouseId = resolveWarehouseId(user, maKho);

    validateItems(request == null ? null : request.getItems());

    stocktakeRepository.updateHeader(id, allowedWarehouseId, ghiChu);
    stocktakeRepository.deleteItems(id);
    insertItems(id, request.getItems());

    return getById(user, id);
  }

  @Transactional(rollbackFor = Exception.class)
  public StocktakeResponse completeStocktake(SessionUser user, Long id) {
    /*
     * Transaction: TX_COMPLETE_STOCKTAKE - Hoàn tất kiểm kho
     *
     * Flow:
     * 1. Khóa phiếu kiểm kho bằng SELECT ... FOR UPDATE.
     * 2. Kiểm tra phiếu còn DRAFT.
     * 3. Lấy danh sách chi tiết kiểm kho.
     * 4. Với từng dòng:
     *    - Nếu NO_ACTION: chỉ giữ dữ liệu kiểm kho, không cập nhật tồn.
     *    - Nếu ADJUST_STOCK:
     *         + Khóa lô bằng SELECT ... FOR UPDATE.
     *         + Khóa TONKHO bằng SELECT ... FOR UPDATE.
     *         + Cập nhật LOHANG_NGUYENLIEU về số lượng thực tế.
     *         + Cập nhật TONKHO theo phần chênh lệch.
     *         + Ghi NHATKY_KHO = STOCKTAKE_ADJUST.
     *    - Nếu CREATE_WASTAGE:
     *         + Chỉ áp dụng khi số lượng thực tế nhỏ hơn số lượng hiện tại.
     *         + Tạo PHIEUHAOHUT loại LOST.
     *         + Cập nhật LOHANG_NGUYENLIEU.
     *         + Cập nhật TONKHO.
     *         + Ghi NHATKY_KHO = WASTAGE.
     * 5. Update PHIEUKIEMKHO.trang_thai = COMPLETED.
     * 6. Commit nếu thành công, rollback nếu có lỗi.
     */

    validateId(id, "Mã phiếu kiểm kho không hợp lệ");

    StocktakeRepository.HeaderLock header = stocktakeRepository.findHeaderForUpdate(id);

    if (header == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu kiểm kho");
    }

    if (!DRAFT.equals(header.getTrangThai())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Chỉ được hoàn tất phiếu kiểm kho ở trạng thái DRAFT");
    }

    resolveWarehouseId(user, header.getMaKho());

    List<StocktakeItemResponse> items = stocktakeRepository.findItems(id);

    if (items.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Phiếu kiểm kho chưa có dòng chi tiết");
    }

    for (StocktakeItemResponse item : items) {
      processCompleteItem(user, header.getMaKho(), id, item);
    }

    stocktakeRepository.updateStatus(id, COMPLETED);

    return getById(user, id);
  }

  @Transactional(rollbackFor = Exception.class)
  public StocktakeResponse cancelStocktake(SessionUser user, Long id) {
    /*
     * Transaction: TX_CANCEL_STOCKTAKE - Hủy phiếu kiểm kho
     *
     * Flow:
     * 1. Khóa phiếu kiểm kho.
     * 2. Chỉ cho hủy nếu phiếu còn DRAFT.
     * 3. Update trạng thái = CANCELLED.
     * 4. Không cập nhật tồn kho, không ghi NHATKY_KHO.
     */

    validateId(id, "Mã phiếu kiểm kho không hợp lệ");

    StocktakeRepository.HeaderLock header = stocktakeRepository.findHeaderForUpdate(id);

    if (header == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu kiểm kho");
    }

    resolveWarehouseId(user, header.getMaKho());

    if (!DRAFT.equals(header.getTrangThai())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Chỉ được hủy phiếu kiểm kho ở trạng thái DRAFT");
    }

    stocktakeRepository.updateStatus(id, CANCELLED);

    return getById(user, id);
  }

  private void processCompleteItem(
      SessionUser user, Long maKho, Long maPhieuKiemKho, StocktakeItemResponse item) {
    String huongXuLy = item.getHuongXuLy();

    if (NO_ACTION.equals(huongXuLy)) {
      return;
    }

    Long maLoHang = item.getMaLoHang();

    if (maLoHang == null) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Dòng kiểm kho có xử lý tồn bắt buộc phải chọn lô hàng");
    }

    StocktakeRepository.LotLock lot = stocktakeRepository.findLotForUpdate(maLoHang);

    if (lot == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy lô hàng: " + maLoHang);
    }

    if (!lot.getMaKho().equals(maKho)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Lô hàng không thuộc kho kiểm kho");
    }

    if (!lot.getMaNguyenLieu().equals(item.getMaNguyenLieu())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Lô hàng không thuộc nguyên liệu kiểm kho");
    }

    StocktakeRepository.StockLock stock =
        stocktakeRepository.findStockForUpdate(maKho, item.getMaNguyenLieu());

    if (stock == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Không tìm thấy tồn kho của nguyên liệu");
    }

    BigDecimal actualQuantity = item.getSoLuongThucTe();
    BigDecimal currentLotQuantity = lot.getSoLuongConLai();

    if (actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng thực tế không được âm");
    }

    BigDecimal delta = actualQuantity.subtract(currentLotQuantity);

    if (ADJUST_STOCK.equals(huongXuLy)) {
      applyStocktakeAdjustment(
          user, maKho, maPhieuKiemKho, item, lot, stock, actualQuantity, delta);
      return;
    }

    if (CREATE_WASTAGE.equals(huongXuLy)) {
      applyStocktakeWastage(user, maKho, item, lot, stock, actualQuantity);
    }
  }

  private void applyStocktakeAdjustment(
      SessionUser user,
      Long maKho,
      Long maPhieuKiemKho,
      StocktakeItemResponse item,
      StocktakeRepository.LotLock lot,
      StocktakeRepository.StockLock stock,
      BigDecimal actualQuantity,
      BigDecimal delta) {
    if (delta.compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    BigDecimal stockBefore = stock.getSoLuongTon();
    BigDecimal stockAfter = stockBefore.add(delta);

    if (stockAfter.compareTo(BigDecimal.ZERO) < 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Điều chỉnh làm tồn kho bị âm");
    }

    stocktakeRepository.updateLotQuantity(lot.getMaLoHang(), actualQuantity);
    stocktakeRepository.updateStockQuantity(stock.getMaTonKho(), stockAfter);

    stocktakeRepository.insertInventoryLog(
        maKho,
        item.getMaNguyenLieu(),
        lot.getMaLoHang(),
        "STOCKTAKE_ADJUST",
        "PHIEUKIEMKHO",
        maPhieuKiemKho,
        delta,
        stockBefore,
        stockAfter,
        user.getMaNguoiDung());
  }

  private void applyStocktakeWastage(
      SessionUser user,
      Long maKho,
      StocktakeItemResponse item,
      StocktakeRepository.LotLock lot,
      StocktakeRepository.StockLock stock,
      BigDecimal actualQuantity) {
    BigDecimal currentLotQuantity = lot.getSoLuongConLai();

    if (actualQuantity.compareTo(currentLotQuantity) > 0) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "CREATE_WASTAGE chỉ áp dụng khi số lượng thực tế nhỏ hơn số lượng hệ thống");
    }

    BigDecimal wastageQuantity = currentLotQuantity.subtract(actualQuantity);

    if (wastageQuantity.compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    BigDecimal stockBefore = stock.getSoLuongTon();
    BigDecimal stockAfter = stockBefore.subtract(wastageQuantity);

    if (stockAfter.compareTo(BigDecimal.ZERO) < 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Hao hụt làm tồn kho bị âm");
    }

    Long wastageId =
        stocktakeRepository.insertWastageFromStocktake(
            maKho,
            item.getMaNguyenLieu(),
            lot.getMaLoHang(),
            wastageQuantity,
            item.getLyDoChenhLech(),
            user.getMaNguoiDung());

    stocktakeRepository.updateLotQuantity(lot.getMaLoHang(), actualQuantity);
    stocktakeRepository.updateStockQuantity(stock.getMaTonKho(), stockAfter);

    stocktakeRepository.insertInventoryLog(
        maKho,
        item.getMaNguyenLieu(),
        lot.getMaLoHang(),
        "WASTAGE",
        "PHIEUHAOHUT",
        wastageId,
        wastageQuantity.negate(),
        stockBefore,
        stockAfter,
        user.getMaNguoiDung());
  }

  private void insertItems(Long stocktakeId, List<StocktakeItemRequest> items) {
    Set<String> uniqueKeys = new HashSet<>();

    for (StocktakeItemRequest item : items) {
      Long maNguyenLieu = item.getMaNguyenLieu();
      Long maLoHang = item.getMaLoHang();

      String uniqueKey = maNguyenLieu + "_" + maLoHang;

      if (!uniqueKeys.add(uniqueKey)) {
        throw new AppException(
            HttpStatus.CONFLICT, "Một lô/nguyên liệu không được kiểm trùng trong cùng phiếu");
      }

      BigDecimal systemQuantity = item.getSoLuongHeThong();
      BigDecimal actualQuantity = item.getSoLuongThucTe();
      BigDecimal difference = actualQuantity.subtract(systemQuantity);
      BigDecimal rate = calculateDifferenceRate(systemQuantity, difference);

      String reason = normalizeNote(item.getLyDoChenhLech());
      String handling = normalizeHandlingRequired(item.getHuongXuLy());

      stocktakeRepository.insertItem(
          stocktakeId,
          maNguyenLieu,
          maLoHang,
          systemQuantity,
          actualQuantity,
          difference,
          rate,
          reason,
          handling);
    }
  }

  private void validateItems(List<StocktakeItemRequest> items) {
    if (items == null || items.isEmpty()) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Phiếu kiểm kho phải có ít nhất một dòng chi tiết");
    }

    for (StocktakeItemRequest item : items) {
      if (item == null) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Dòng kiểm kho không hợp lệ");
      }

      if (item.getMaNguyenLieu() == null || item.getMaNguyenLieu() <= 0) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn nguyên liệu");
      }

      if (item.getSoLuongHeThong() == null
          || item.getSoLuongHeThong().compareTo(BigDecimal.ZERO) < 0) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng hệ thống không được âm");
      }

      if (item.getSoLuongThucTe() == null
          || item.getSoLuongThucTe().compareTo(BigDecimal.ZERO) < 0) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng thực tế không được âm");
      }

      normalizeHandlingRequired(item.getHuongXuLy());
    }
  }

  private BigDecimal calculateDifferenceRate(BigDecimal systemQuantity, BigDecimal difference) {
    if (systemQuantity == null || systemQuantity.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    return difference
        .multiply(BigDecimal.valueOf(100))
        .divide(systemQuantity, 4, RoundingMode.HALF_UP);
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

    Long ownWarehouseId = stocktakeRepository.findActiveWarehouseIdByBranchId(maChiNhanh);

    if (ownWarehouseId == null) {
      throw new AppException(HttpStatus.FORBIDDEN, "Chi nhánh của tài khoản chưa có kho hoạt động");
    }

    if (requestedWarehouseId != null && !requestedWarehouseId.equals(ownWarehouseId)) {
      throw new AppException(HttpStatus.FORBIDDEN, "Bạn chỉ được thao tác kho của chi nhánh mình");
    }

    return ownWarehouseId;
  }

  private String normalizeHandlingRequired(String value) {
    String handling = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);

    if (!NO_ACTION.equals(handling)
        && !ADJUST_STOCK.equals(handling)
        && !CREATE_WASTAGE.equals(handling)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Hướng xử lý kiểm kho không hợp lệ");
    }

    return handling;
  }

  private String normalizeStatusAllowNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String status = value.trim().toUpperCase(Locale.ROOT);

    if (!DRAFT.equals(status) && !COMPLETED.equals(status) && !CANCELLED.equals(status)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái phiếu kiểm kho không hợp lệ");
    }

    return status;
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
