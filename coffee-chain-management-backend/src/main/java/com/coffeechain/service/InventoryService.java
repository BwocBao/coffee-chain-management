package com.coffeechain.service;

import com.coffeechain.dto.*;
import com.coffeechain.dto.request.*;
import com.coffeechain.dto.response.*;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.InventoryRepository;
import com.coffeechain.repository.InventoryRepository.LotRecord;
import com.coffeechain.repository.InventoryRepository.WarehouseRecord;
import com.coffeechain.security.SessionUser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
  private static final Set<String> EXPORT_TYPES =
      Set.of("INTERNAL_USE", "RETURN_SUPPLIER", "TRAINING", "OTHER");
  private static final Set<String> STOCK_STATUS = Set.of("ON_DINH", "TON_THAP", "HET_HANG");

  private final InventoryRepository inventoryRepository;

  public InventoryService(InventoryRepository inventoryRepository) {
    this.inventoryRepository = inventoryRepository;
  }

  public InventoryLookupResponse getImportLookup() {
    InventoryLookupResponse response = new InventoryLookupResponse();
    response.setWarehouses(inventoryRepository.findWarehouses());
    response.setSuppliers(inventoryRepository.findSuppliers());
    response.setIngredients(inventoryRepository.findIngredients());
    return response;
  }

  public InventoryExportLookupResponse getExportLookup() {
    InventoryExportLookupResponse response = new InventoryExportLookupResponse();
    response.setWarehouses(inventoryRepository.findWarehouses());
    response.setIngredients(inventoryRepository.findIngredients());
    response.setExportTypes(
        List.of(
            new InventoryOptionResponse(1L, "INTERNAL_USE", "Xuất dùng nội bộ"),
            new InventoryOptionResponse(2L, "RETURN_SUPPLIER", "Xuất trả nhà cung cấp"),
            new InventoryOptionResponse(3L, "TRAINING", "Xuất để training"),
            new InventoryOptionResponse(4L, "OTHER", "Xuất khác")));
    return response;
  }

  public List<InventoryLotResponse> getLotsForExport(
      Long maKho, Long maNguyenLieu, SessionUser user) {
    if (maKho == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon kho");
    }
    if (maNguyenLieu == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon nguyen lieu");
    }

    WarehouseRecord warehouse = inventoryRepository.findWarehouse(maKho);
    if (warehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho");
    }
    validateWarehouseAccess(warehouse, user);

    if (!inventoryRepository.ingredientExists(maNguyenLieu)) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay nguyen lieu: " + maNguyenLieu);
    }

    return inventoryRepository.findLotResponsesForExport(maKho, maNguyenLieu);
  }

  @Transactional
  public ImportReceiptResponse createImportReceipt(
      CreateImportReceiptRequest request, SessionUser user) {
    validateRequest(request);

    WarehouseRecord warehouse = inventoryRepository.findWarehouse(request.getMaKho());
    if (warehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho nhap");
    }
    validateWarehouseAccess(warehouse, user);

    String supplierName = inventoryRepository.findSupplierName(request.getMaNhaCungCap());
    if (supplierName == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay nha cung cap");
    }

    BigDecimal total = calculateTotal(request.getItems());
    Long receiptId =
        inventoryRepository.createImportReceipt(
            request.getMaKho(),
            request.getMaNhaCungCap(),
            total,
            user.getMaNguoiDung(),
            request.getGhiChu());

    for (CreateImportReceiptItemRequest item : request.getItems()) {
      if (!inventoryRepository.ingredientExists(item.getMaNguyenLieu())) {
        throw new AppException(
            HttpStatus.NOT_FOUND, "Khong tim thay nguyen lieu: " + item.getMaNguyenLieu());
      }

      BigDecimal before =
          inventoryRepository.findCurrentStock(request.getMaKho(), item.getMaNguyenLieu());
      Long detailId = inventoryRepository.createImportDetail(receiptId, item);
      Long lotId =
          inventoryRepository.createLot(
              request.getMaKho(),
              item.getMaNguyenLieu(),
              detailId,
              item.getSoLuongNhap(),
              item.getHanSuDung());
      inventoryRepository.increaseStock(
          request.getMaKho(), item.getMaNguyenLieu(), item.getSoLuongNhap());

      BigDecimal after = before.add(item.getSoLuongNhap());
      inventoryRepository.createInventoryJournal(
          request.getMaKho(),
          item.getMaNguyenLieu(),
          lotId,
          receiptId,
          item.getSoLuongNhap(),
          before,
          after,
          user.getMaNguoiDung());
    }

    return inventoryRepository.findImportReceipt(receiptId, request.getItems().size());
  }

  @Transactional
  public ExportReceiptResponse createExportReceipt(
      CreateExportReceiptRequest request, SessionUser user) {
    validateExportRequest(request);

    WarehouseRecord warehouse = inventoryRepository.findWarehouse(request.getMaKho());
    if (warehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho xuat");
    }
    validateWarehouseAccess(warehouse, user);

    boolean manualMode = Boolean.TRUE.equals(request.getChonLoThuCong());
    List<ComputedExportItem> computedItems = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (CreateExportReceiptItemRequest item : request.getItems()) {
      if (!inventoryRepository.ingredientExists(item.getMaNguyenLieu())) {
        throw new AppException(
            HttpStatus.NOT_FOUND, "Khong tim thay nguyen lieu: " + item.getMaNguyenLieu());
      }

      BigDecimal stockBeforeItem =
          inventoryRepository.findCurrentStock(request.getMaKho(), item.getMaNguyenLieu());
      if (stockBeforeItem.compareTo(item.getSoLuongXuat()) < 0) {
        throw new AppException(
            HttpStatus.BAD_REQUEST, "Ton kho khong du cho nguyen lieu: " + item.getMaNguyenLieu());
      }

      List<LotAllocation> allocations =
          manualMode
              ? buildManualAllocations(request.getMaKho(), item)
              : buildFefoAllocations(request.getMaKho(), item);

      BigDecimal itemValue = calculateAllocationValue(allocations);
      total = total.add(itemValue);
      computedItems.add(new ComputedExportItem(item, allocations));
    }

    Long receiptId =
        inventoryRepository.createExportReceipt(
            request.getMaKho(),
            normalizeExportType(request.getLoaiXuat()),
            total,
            user.getMaNguoiDung(),
            request.getGhiChu());

    int detailCount = 0;

    for (ComputedExportItem computedItem : computedItems) {
      CreateExportReceiptItemRequest item = computedItem.item();
      for (LotAllocation allocation : computedItem.allocations()) {
        BigDecimal stockBefore =
            inventoryRepository.findCurrentStock(request.getMaKho(), item.getMaNguyenLieu());
        inventoryRepository.createExportDetail(
            receiptId,
            item,
            allocation.maLoHang(),
            allocation.quantity(),
            normalizeUnitPrice(allocation.unitPrice()));

        boolean lotUpdated =
            inventoryRepository.decreaseLot(allocation.maLoHang(), allocation.quantity());
        if (!lotUpdated) {
          throw new AppException(
              HttpStatus.BAD_REQUEST, "Lo hang khong du so luong: " + allocation.maLoHang());
        }

        boolean stockUpdated =
            inventoryRepository.decreaseStock(
                request.getMaKho(), item.getMaNguyenLieu(), allocation.quantity());
        if (!stockUpdated) {
          throw new AppException(
              HttpStatus.BAD_REQUEST,
              "Ton kho khong du cho nguyen lieu: " + item.getMaNguyenLieu());
        }

        BigDecimal stockAfter = stockBefore.subtract(allocation.quantity());
        inventoryRepository.createInventoryJournal(
            request.getMaKho(),
            item.getMaNguyenLieu(),
            allocation.maLoHang(),
            "EXPORT",
            "PHIEUXUAT",
            receiptId,
            allocation.quantity().negate(),
            stockBefore,
            stockAfter,
            user.getMaNguoiDung());
        detailCount++;
      }
    }

    return inventoryRepository.findExportReceipt(receiptId, detailCount);
  }

  public List<InventoryStockOptionResponse> getExportStock(Long maKho, SessionUser user) {
    if (maKho == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn kho xuất");
    }

    WarehouseRecord warehouse = inventoryRepository.findWarehouse(maKho);
    if (warehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy kho xuất");
    }

    validateWarehouseAccess(warehouse, user);

    return inventoryRepository.findIngredientsWithStockForExport(maKho);
  }

  public PageResponse<InventoryResponse> getStock(
      Long maKho,
      Long maNguyenLieu,
      String tuKhoa,
      String trangThaiTonKho,
      int page,
      int size,
      SessionUser user) {
    validatePagination(page, size);

    Long branchScope = resolveBranchScope(user);
    if (maKho != null) {
      validateWarehouseAccessForView(maKho, user);
    }

    String searchText = normalizeSearchText(tuKhoa);
    String normalizedStatus = normalizeStockStatus(trangThaiTonKho);

    List<InventoryResponse> rows =
        inventoryRepository.findStock(
            maKho, maNguyenLieu, searchText, normalizedStatus, branchScope, page, size);
    long total =
        inventoryRepository.countStock(
            maKho, maNguyenLieu, searchText, normalizedStatus, branchScope);

    return PageResponse.of(rows, total, page, size);
  }

  public List<BatchInventoryResponse> getBatchStock(
      Long maKho, Long maNguyenLieu, SessionUser user) {
    Long branchScope = resolveBranchScope(user);
    if (maKho != null) {
      validateWarehouseAccessForView(maKho, user);
    }

    return inventoryRepository.findBatchStock(maKho, maNguyenLieu, branchScope);
  }

  public TongQuanTonKhoResponse getStockSummary(Long maKho, SessionUser user) {
    Long branchScope = resolveBranchScope(user);
    if (maKho != null) {
      validateWarehouseAccessForView(maKho, user);
    }

    return inventoryRepository.findStockSummary(maKho, branchScope);
  }

  public InventoryTransferLookupResponse getTransferLookup() {
    InventoryTransferLookupResponse response = new InventoryTransferLookupResponse();

    response.setSourceWarehouses(inventoryRepository.findWarehouses());
    response.setDestinationWarehouses(inventoryRepository.findWarehouses());

    return response;
  }

  public List<InventoryLotResponse> getLotsForTransfer(
      Long maKhoNguon, Long maNguyenLieu, SessionUser user) {
    if (maKhoNguon == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon kho nguon");
    }
    if (maNguyenLieu == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon nguyen lieu");
    }

    WarehouseRecord warehouse = inventoryRepository.findWarehouse(maKhoNguon);
    if (warehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho nguon");
    }

    validateWarehouseAccess(warehouse, user);

    if (!inventoryRepository.ingredientExists(maNguyenLieu)) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay nguyen lieu: " + maNguyenLieu);
    }

    return inventoryRepository.findLotResponsesForExport(maKhoNguon, maNguyenLieu);
  }

  public List<InventoryStockOptionResponse> getTransferStock(Long maKhoNguon, SessionUser user) {
    if (maKhoNguon == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon kho nguon");
    }

    WarehouseRecord warehouse = inventoryRepository.findWarehouse(maKhoNguon);
    if (warehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho nguon");
    }

    validateWarehouseAccess(warehouse, user);

    return inventoryRepository.findIngredientsWithStockForExport(maKhoNguon);
  }

  @Transactional
  public TransferReceiptResponse createTransferReceipt(
      CreateTransferReceiptRequest request, SessionUser user) {
    validateTransferRequest(request);

    WarehouseRecord sourceWarehouse = inventoryRepository.findWarehouse(request.getMaKhoNguon());
    if (sourceWarehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho nguon");
    }

    WarehouseRecord destinationWarehouse =
        inventoryRepository.findWarehouse(request.getMaKhoDich());
    if (destinationWarehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho dich");
    }

    validateWarehouseAccess(sourceWarehouse, user);
    validateWarehouseAccess(destinationWarehouse, user);

    Long receiptId =
        inventoryRepository.createTransferReceipt(
            request.getMaKhoNguon(),
            request.getMaKhoDich(),
            user.getMaNguoiDung(),
            request.getGhiChu());

    boolean manualMode = Boolean.TRUE.equals(request.getChonLoThuCong());
    int detailCount = 0;

    for (CreateTransferReceiptItemRequest item : request.getItems()) {
      if (!inventoryRepository.ingredientExists(item.getMaNguyenLieu())) {
        throw new AppException(
            HttpStatus.NOT_FOUND, "Khong tim thay nguyen lieu: " + item.getMaNguyenLieu());
      }

      BigDecimal sourceStockBeforeItem =
          inventoryRepository.findCurrentStock(request.getMaKhoNguon(), item.getMaNguyenLieu());

      if (sourceStockBeforeItem.compareTo(item.getSoLuongDieuChuyen()) < 0) {
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "Ton kho nguon khong du cho nguyen lieu: " + item.getMaNguyenLieu());
      }

      List<TransferAllocation> allocations =
          manualMode
              ? buildManualTransferAllocations(request.getMaKhoNguon(), item)
              : buildFefoTransferAllocations(request.getMaKhoNguon(), item);

      for (TransferAllocation allocation : allocations) {
        BigDecimal sourceStockBefore =
            inventoryRepository.findCurrentStock(request.getMaKhoNguon(), item.getMaNguyenLieu());

        BigDecimal destinationStockBefore =
            inventoryRepository.findCurrentStock(request.getMaKhoDich(), item.getMaNguyenLieu());

        boolean lotUpdated =
            inventoryRepository.decreaseLot(allocation.maLoHangNguon(), allocation.quantity());

        if (!lotUpdated) {
          throw new AppException(
              HttpStatus.BAD_REQUEST,
              "Lo hang nguon khong du so luong: " + allocation.maLoHangNguon());
        }

        boolean sourceStockUpdated =
            inventoryRepository.decreaseStock(
                request.getMaKhoNguon(), item.getMaNguyenLieu(), allocation.quantity());

        if (!sourceStockUpdated) {
          throw new AppException(
              HttpStatus.BAD_REQUEST,
              "Ton kho nguon khong du cho nguyen lieu: " + item.getMaNguyenLieu());
        }

        Long destinationLotId =
            inventoryRepository.createLot(
                request.getMaKhoDich(),
                item.getMaNguyenLieu(),
                null,
                allocation.quantity(),
                allocation.hanSuDung());

        inventoryRepository.increaseStock(
            request.getMaKhoDich(), item.getMaNguyenLieu(), allocation.quantity());

        inventoryRepository.createTransferDetail(
            receiptId,
            item.getMaNguyenLieu(),
            allocation.maLoHangNguon(),
            destinationLotId,
            allocation.quantity());

        BigDecimal sourceStockAfter = sourceStockBefore.subtract(allocation.quantity());
        BigDecimal destinationStockAfter = destinationStockBefore.add(allocation.quantity());

        inventoryRepository.createInventoryJournal(
            request.getMaKhoNguon(),
            item.getMaNguyenLieu(),
            allocation.maLoHangNguon(),
            "TRANSFER_OUT",
            "PHIEUDIEUCHUYEN",
            receiptId,
            allocation.quantity().negate(),
            sourceStockBefore,
            sourceStockAfter,
            user.getMaNguoiDung());

        inventoryRepository.createInventoryJournal(
            request.getMaKhoDich(),
            item.getMaNguyenLieu(),
            destinationLotId,
            "TRANSFER_IN",
            "PHIEUDIEUCHUYEN",
            receiptId,
            allocation.quantity(),
            destinationStockBefore,
            destinationStockAfter,
            user.getMaNguoiDung());

        detailCount++;
      }
    }

    return inventoryRepository.findTransferReceipt(receiptId, detailCount);
  }

  private void validateTransferRequest(CreateTransferReceiptRequest request) {
    if (request == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Thieu du lieu phieu dieu chuyen");
    }

    if (request.getMaKhoNguon() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon kho nguon");
    }

    if (request.getMaKhoDich() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon kho dich");
    }

    if (request.getMaKhoNguon().equals(request.getMaKhoDich())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Kho nguon va kho dich khong duoc trung nhau");
    }

    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long them it nhat mot dong nguyen lieu");
    }

    boolean manualMode = Boolean.TRUE.equals(request.getChonLoThuCong());

    for (CreateTransferReceiptItemRequest item : request.getItems()) {
      validateTransferItem(item, manualMode);
    }
  }

  private void validateTransferItem(CreateTransferReceiptItemRequest item, boolean manualMode) {
    if (item == null || item.getMaNguyenLieu() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon nguyen lieu");
    }

    if (item.getSoLuongDieuChuyen() == null
        || item.getSoLuongDieuChuyen().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "So luong dieu chuyen phai lon hon 0");
    }

    if (manualMode
        && (item.getLoHangDieuChuyen() == null || item.getLoHangDieuChuyen().isEmpty())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Vui long chon lo hang khi bat che do chon lo thu cong");
    }
  }

  private List<TransferAllocation> buildFefoTransferAllocations(
      Long maKhoNguon, CreateTransferReceiptItemRequest item) {
    List<LotRecord> lots =
        inventoryRepository.lockLotsForExport(maKhoNguon, item.getMaNguyenLieu());

    List<TransferAllocation> allocations = new ArrayList<>();
    BigDecimal remaining = item.getSoLuongDieuChuyen();

    for (LotRecord lot : lots) {
      if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }

      BigDecimal available = lot.soLuongConLai();
      if (available == null || available.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }

      BigDecimal take = available.min(remaining);
      allocations.add(new TransferAllocation(lot.maLoHang(), take, lot.hanSuDung()));

      remaining = remaining.subtract(take);
    }

    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "Lo hang khong du so luong dieu chuyen cho nguyen lieu: " + item.getMaNguyenLieu());
    }

    return allocations;
  }

  private List<TransferAllocation> buildManualTransferAllocations(
      Long maKhoNguon, CreateTransferReceiptItemRequest item) {
    Map<Long, BigDecimal> quantitiesByLot = new LinkedHashMap<>();

    for (TransferLotSelectionRequest lotSelection : item.getLoHangDieuChuyen()) {
      if (lotSelection == null || lotSelection.getMaLoHang() == null) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon lo hang hop le");
      }

      if (lotSelection.getSoLuongDieuChuyen() == null
          || lotSelection.getSoLuongDieuChuyen().compareTo(BigDecimal.ZERO) <= 0) {
        throw new AppException(
            HttpStatus.BAD_REQUEST, "So luong dieu chuyen theo lo phai lon hon 0");
      }

      quantitiesByLot.merge(
          lotSelection.getMaLoHang(), lotSelection.getSoLuongDieuChuyen(), BigDecimal::add);
    }

    BigDecimal selectedTotal =
        quantitiesByLot.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

    if (selectedTotal.compareTo(item.getSoLuongDieuChuyen()) != 0) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Tong so luong theo lo phai bang so luong dieu chuyen");
    }

    List<TransferAllocation> allocations = new ArrayList<>();

    for (Map.Entry<Long, BigDecimal> entry : quantitiesByLot.entrySet()) {
      LotRecord lot =
          inventoryRepository.lockLotForExport(entry.getKey(), maKhoNguon, item.getMaNguyenLieu());

      if (lot == null) {
        throw new AppException(
            HttpStatus.NOT_FOUND, "Khong tim thay lo hang hop le: " + entry.getKey());
      }

      if (lot.soLuongConLai().compareTo(entry.getValue()) < 0) {
        throw new AppException(
            HttpStatus.BAD_REQUEST, "Lo hang khong du so luong: " + entry.getKey());
      }

      allocations.add(new TransferAllocation(entry.getKey(), entry.getValue(), lot.hanSuDung()));
    }

    return allocations;
  }

  private record TransferAllocation(
      Long maLoHangNguon, BigDecimal quantity, java.time.LocalDate hanSuDung) {}

  private void validateRequest(CreateImportReceiptRequest request) {
    if (request == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Thieu du lieu phieu nhap");
    }
    if (request.getMaKho() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon kho nhap");
    }
    if (request.getMaNhaCungCap() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon nha cung cap");
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long them it nhat mot dong nguyen lieu");
    }

    for (CreateImportReceiptItemRequest item : request.getItems()) {
      validateItem(item);
    }
  }

  private void validateItem(CreateImportReceiptItemRequest item) {
    if (item == null || item.getMaNguyenLieu() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon nguyen lieu");
    }
    if (item.getSoLuongNhap() == null || item.getSoLuongNhap().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "So luong nhap phai lon hon 0");
    }
    if (item.getDonGiaNhap() == null || item.getDonGiaNhap().compareTo(BigDecimal.ZERO) < 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Don gia nhap khong hop le");
    }
  }

  private BigDecimal calculateTotal(List<CreateImportReceiptItemRequest> items) {
    BigDecimal total = BigDecimal.ZERO;
    for (CreateImportReceiptItemRequest item : items) {
      total = total.add(item.getSoLuongNhap().multiply(item.getDonGiaNhap()));
    }
    return total;
  }

  private void validateExportRequest(CreateExportReceiptRequest request) {
    if (request == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Thieu du lieu phieu xuat");
    }
    if (request.getMaKho() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon kho xuat");
    }
    String exportType = normalizeExportType(request.getLoaiXuat());
    if (!EXPORT_TYPES.contains(exportType)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Loai xuat kho khong hop le");
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long them it nhat mot dong nguyen lieu");
    }

    boolean manualMode = Boolean.TRUE.equals(request.getChonLoThuCong());
    for (CreateExportReceiptItemRequest item : request.getItems()) {
      validateExportItem(item, manualMode);
    }
  }

  private void validateExportItem(CreateExportReceiptItemRequest item, boolean manualMode) {
    if (item == null || item.getMaNguyenLieu() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon nguyen lieu");
    }
    if (item.getSoLuongXuat() == null || item.getSoLuongXuat().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "So luong xuat phai lon hon 0");
    }
    if (manualMode && (item.getLoHangXuat() == null || item.getLoHangXuat().isEmpty())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Vui long chon lo hang khi bat che do chon lo thu cong");
    }
  }

  private List<LotAllocation> buildFefoAllocations(
      Long maKho, CreateExportReceiptItemRequest item) {
    List<LotRecord> lots = inventoryRepository.lockLotsForExport(maKho, item.getMaNguyenLieu());
    List<LotAllocation> allocations = new ArrayList<>();
    BigDecimal remaining = item.getSoLuongXuat();

    for (LotRecord lot : lots) {
      if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      BigDecimal available = lot.soLuongConLai();
      if (available == null || available.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }

      BigDecimal take = available.min(remaining);
      allocations.add(new LotAllocation(lot.maLoHang(), take, normalizeUnitPrice(lot.donGiaNhap())));
      remaining = remaining.subtract(take);
    }

    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "Lo hang khong du so luong xuat cho nguyen lieu: " + item.getMaNguyenLieu());
    }

    return allocations;
  }

  private List<LotAllocation> buildManualAllocations(
      Long maKho, CreateExportReceiptItemRequest item) {
    Map<Long, BigDecimal> quantitiesByLot = new LinkedHashMap<>();
    for (ExportLotSelectionRequest lotSelection : item.getLoHangXuat()) {
      if (lotSelection == null || lotSelection.getMaLoHang() == null) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Vui long chon lo hang hop le");
      }
      if (lotSelection.getSoLuongXuat() == null
          || lotSelection.getSoLuongXuat().compareTo(BigDecimal.ZERO) <= 0) {
        throw new AppException(HttpStatus.BAD_REQUEST, "So luong xuat theo lo phai lon hon 0");
      }
      quantitiesByLot.merge(
          lotSelection.getMaLoHang(), lotSelection.getSoLuongXuat(), BigDecimal::add);
    }

    BigDecimal selectedTotal =
        quantitiesByLot.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    if (selectedTotal.compareTo(item.getSoLuongXuat()) != 0) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Tong so luong theo lo phai bang so luong xuat");
    }

    List<LotAllocation> allocations = new ArrayList<>();
    for (Map.Entry<Long, BigDecimal> entry : quantitiesByLot.entrySet()) {
      LotRecord lot =
          inventoryRepository.lockLotForExport(entry.getKey(), maKho, item.getMaNguyenLieu());
      if (lot == null) {
        throw new AppException(
            HttpStatus.NOT_FOUND, "Khong tim thay lo hang hop le: " + entry.getKey());
      }
      if (lot.soLuongConLai().compareTo(entry.getValue()) < 0) {
        throw new AppException(
            HttpStatus.BAD_REQUEST, "Lo hang khong du so luong: " + entry.getKey());
      }
      allocations.add(new LotAllocation(entry.getKey(), entry.getValue(), normalizeUnitPrice(lot.donGiaNhap())));
    }

    return allocations;
  }


  private BigDecimal calculateAllocationValue(List<LotAllocation> allocations) {
    BigDecimal total = BigDecimal.ZERO;
    for (LotAllocation allocation : allocations) {
      total = total.add(allocation.quantity().multiply(normalizeUnitPrice(allocation.unitPrice())));
    }
    return total;
  }

  private String normalizeExportType(String value) {
    return value == null ? "" : value.trim().toUpperCase();
  }

  private String normalizeStockStatus(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String normalized = value.trim().toUpperCase();
    if (!STOCK_STATUS.contains(normalized)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Trang thai ton kho khong hop le");
    }
    return normalized;
  }

  private String normalizeSearchText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private void validatePagination(int page, int size) {
    if (page < 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Page phai lon hon hoac bang 0");
    }
    if (size <= 0 || size > 100) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Size phai nam trong khoang 1 den 100");
    }
  }

  private BigDecimal normalizeUnitPrice(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private void validateWarehouseAccess(WarehouseRecord warehouse, SessionUser user) {
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
    }
    if ("ADMIN".equalsIgnoreCase(user.getTenVaiTro())
        || "QUAN_LY_KHO".equalsIgnoreCase(user.getTenVaiTro())) {
      return;
    }
    if (warehouse.maChiNhanh() == null
        || user.getMaChiNhanh() == null
        || !warehouse.maChiNhanh().equals(user.getMaChiNhanh())) {
      throw new AppException(HttpStatus.FORBIDDEN, "Khong duoc thao tac kho cua chi nhanh khac");
    }
  }

  private void validateWarehouseAccessForView(Long maKho, SessionUser user) {
    WarehouseRecord warehouse = inventoryRepository.findWarehouse(maKho);
    if (warehouse == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "Khong tim thay kho");
    }
    validateWarehouseAccess(warehouse, user);
  }

  private Long resolveBranchScope(SessionUser user) {
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
    }
    if ("ADMIN".equalsIgnoreCase(user.getTenVaiTro())
        || "QUAN_LY_KHO".equalsIgnoreCase(user.getTenVaiTro())) {
      return null;
    }
    if (user.getMaChiNhanh() == null) {
      throw new AppException(HttpStatus.FORBIDDEN, "Tai khoan chua duoc gan chi nhanh");
    }
    return user.getMaChiNhanh();
  }

  private record ComputedExportItem(
          CreateExportReceiptItemRequest item,
          List<LotAllocation> allocations) {}

  private record LotAllocation(
          Long maLoHang,
          BigDecimal quantity,
          BigDecimal unitPrice) {}
}
