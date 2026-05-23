package com.coffeechain.service;

import com.coffeechain.dto.request.CreateWarehouseRequest;
import com.coffeechain.dto.request.UpdateWarehouseRequest;
import com.coffeechain.dto.response.WarehouseLookupResponse;
import com.coffeechain.dto.response.WarehouseResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.WarehouseRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseService {
  private static final String CENTRAL = "CENTRAL";
  private static final String BRANCH = "BRANCH";
  private static final String ACTIVE = "ACTIVE";
  private static final String INACTIVE = "INACTIVE";

  private final WarehouseRepository warehouseRepository;

  public WarehouseService(WarehouseRepository warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  public List<WarehouseResponse> searchWarehouses(
      String keyword, String loaiKho, String trangThai) {
    String normalizedType = normalizeTypeAllowNull(loaiKho);
    String normalizedStatus = normalizeStatusAllowNull(trangThai);

    return warehouseRepository.searchWarehouses(keyword, normalizedType, normalizedStatus);
  }

  public WarehouseResponse getWarehouseById(Long id) {
    validateId(id);

    return warehouseRepository
        .findById(id)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy kho"));
  }

  public WarehouseLookupResponse getWarehouseLookups() {
    WarehouseLookupResponse response = new WarehouseLookupResponse();

    response.setWarehouseTypes(
        List.of(
            new WarehouseLookupResponse.OptionDto(
                null, CENTRAL, "Kho tổng", "Kho trung tâm của hệ thống"),
            new WarehouseLookupResponse.OptionDto(
                null, BRANCH, "Kho chi nhánh", "Kho gắn với một chi nhánh cụ thể")));

    response.setBranches(warehouseRepository.findActiveBranchOptions());

    return response;
  }

  @Transactional(rollbackFor = Exception.class)
  public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
    /*
     * Transaction: TX_CREATE_WAREHOUSE - Tạo kho
     *
     * Flow:
     * 1. Validate tên kho, loại kho và chi nhánh.
     * 2. Nếu loại kho là CENTRAL:
     *    - maChiNhanh phải null.
     *    - Hệ thống chỉ nên có một kho tổng.
     * 3. Nếu loại kho là BRANCH:
     *    - maChiNhanh bắt buộc có.
     *    - Chi nhánh phải tồn tại và đang ACTIVE.
     *    - Mỗi chi nhánh chỉ có một kho.
     * 4. Kiểm tra tên kho không bị trùng.
     * 5. Insert KHO với trang_thai = ACTIVE.
     * 6. Query lại kho vừa tạo để trả response.
     */

    String tenKho = normalizeText(request == null ? null : request.getTenKho());
    String loaiKho = normalizeTypeRequired(request == null ? null : request.getLoaiKho());
    Long maChiNhanh = request == null ? null : request.getMaChiNhanh();

    validateWarehouseInput(null, tenKho, loaiKho, maChiNhanh, true);

    if (warehouseRepository.existsByName(tenKho)) {
      throw new AppException(HttpStatus.CONFLICT, "Tên kho đã tồn tại");
    }

    Long maKho = warehouseRepository.insertWarehouse(tenKho, loaiKho, maChiNhanh, ACTIVE);

    return getWarehouseById(maKho);
  }

  @Transactional(rollbackFor = Exception.class)
  public WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request) {
    /*
     * Transaction: TX_UPDATE_WAREHOUSE - Cập nhật kho
     *
     * Flow:
     * 1. Kiểm tra kho có tồn tại không.
     * 2. Validate tên kho, loại kho, chi nhánh và trạng thái.
     * 3. Kiểm tra tên kho không trùng với kho khác.
     * 4. Kiểm tra ràng buộc:
     *    - CENTRAL không có chi nhánh.
     *    - BRANCH bắt buộc có chi nhánh.
     *    - Một chi nhánh chỉ có một kho.
     * 5. Update KHO.
     * 6. Query lại dữ liệu sau cập nhật.
     */

    validateId(id);

    if (!warehouseRepository.existsById(id)) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy kho");
    }

    String tenKho = normalizeText(request == null ? null : request.getTenKho());
    String loaiKho = normalizeTypeRequired(request == null ? null : request.getLoaiKho());
    Long maChiNhanh = request == null ? null : request.getMaChiNhanh();
    String trangThai = normalizeStatusRequired(request == null ? null : request.getTrangThai());

    validateWarehouseInput(id, tenKho, loaiKho, maChiNhanh, false);

    if (warehouseRepository.existsByNameExceptId(tenKho, id)) {
      throw new AppException(HttpStatus.CONFLICT, "Tên kho đã tồn tại");
    }

    warehouseRepository.updateWarehouse(id, tenKho, loaiKho, maChiNhanh, trangThai);

    return getWarehouseById(id);
  }

  @Transactional(rollbackFor = Exception.class)
  public WarehouseResponse updateStatus(Long id, String trangThai) {
    /*
     * Transaction: TX_UPDATE_WAREHOUSE_STATUS - Đổi trạng thái kho
     *
     * Flow:
     * 1. Kiểm tra kho có tồn tại không.
     * 2. Validate trạng thái chỉ nhận ACTIVE hoặc INACTIVE.
     * 3. Update KHO.trang_thai.
     * 4. Query lại dữ liệu sau cập nhật.
     */

    validateId(id);

    if (!warehouseRepository.existsById(id)) {
      throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy kho");
    }

    String status = normalizeStatusRequired(trangThai);

    warehouseRepository.updateStatus(id, status);

    return getWarehouseById(id);
  }

  @Transactional(rollbackFor = Exception.class)
  public WarehouseResponse deactivateWarehouse(Long id) {
    /*
     * Transaction: TX_DEACTIVATE_WAREHOUSE - Ngưng hoạt động kho
     *
     * Flow:
     * 1. Kiểm tra kho có tồn tại không.
     * 2. Không xóa cứng vì kho có thể đã phát sinh tồn kho, phiếu nhập/xuất,
     *    điều chuyển và nhật ký kho.
     * 3. Chuyển trạng thái sang INACTIVE.
     */

    return updateStatus(id, INACTIVE);
  }

  private void validateWarehouseInput(
      Long currentWarehouseId, String tenKho, String loaiKho, Long maChiNhanh, boolean creating) {
    if (tenKho == null || tenKho.isBlank()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên kho");
    }

    if (tenKho.length() > 150) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Tên kho không được vượt quá 150 ký tự");
    }

    if (CENTRAL.equals(loaiKho)) {
      if (maChiNhanh != null) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Kho tổng không được gắn với chi nhánh");
      }

      boolean centralExists =
          creating
              ? warehouseRepository.existsCentralWarehouse()
              : warehouseRepository.existsCentralWarehouseExceptId(currentWarehouseId);

      if (centralExists) {
        throw new AppException(HttpStatus.CONFLICT, "Hệ thống chỉ được có một kho tổng");
      }

      return;
    }

    if (BRANCH.equals(loaiKho)) {
      if (maChiNhanh == null) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Kho chi nhánh phải gắn với chi nhánh");
      }

      if (!warehouseRepository.existsActiveBranch(maChiNhanh)) {
        throw new AppException(
            HttpStatus.BAD_REQUEST, "Chi nhánh không tồn tại hoặc không hoạt động");
      }

      boolean branchWarehouseExists =
          creating
              ? warehouseRepository.existsWarehouseForBranch(maChiNhanh)
              : warehouseRepository.existsWarehouseForBranchExceptId(
                  maChiNhanh, currentWarehouseId);

      if (branchWarehouseExists) {
        throw new AppException(HttpStatus.CONFLICT, "Chi nhánh này đã có kho");
      }
    }
  }

  private void validateId(Long id) {
    if (id == null || id <= 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Mã kho không hợp lệ");
    }
  }

  private String normalizeTypeAllowNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return normalizeTypeRequired(value);
  }

  private String normalizeTypeRequired(String value) {
    String type = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);

    if (!CENTRAL.equals(type) && !BRANCH.equals(type)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Loại kho không hợp lệ");
    }

    return type;
  }

  private String normalizeStatusAllowNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return normalizeStatusRequired(value);
  }

  private String normalizeStatusRequired(String value) {
    String status = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);

    if (!ACTIVE.equals(status) && !INACTIVE.equals(status)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái kho không hợp lệ");
    }

    return status;
  }

  private String normalizeText(String value) {
    return value == null ? null : value.trim();
  }
}
