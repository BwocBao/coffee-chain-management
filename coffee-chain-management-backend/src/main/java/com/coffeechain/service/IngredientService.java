package com.coffeechain.service;

import com.coffeechain.dto.request.CreateIngredientRequest;
import com.coffeechain.dto.request.UpdateIngredientRequest;
import com.coffeechain.dto.response.IngredientLookupResponse;
import com.coffeechain.dto.response.IngredientResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class IngredientService {
    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<IngredientResponse> searchIngredients(
            String keyword,
            String status,
            Long unitId
    ) {
        String normalizedStatus = normalizeStatusAllowNull(status);
        return ingredientRepository.searchIngredients(keyword, normalizedStatus, unitId);
    }

    public IngredientResponse getIngredientById(Long id) {
        validateId(id);

        return ingredientRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nguyên liệu"));
    }

    public IngredientLookupResponse getIngredientLookups() {
        IngredientLookupResponse response = new IngredientLookupResponse();
        response.setUnits(ingredientRepository.findUnitOptions());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public IngredientResponse createIngredient(CreateIngredientRequest request) {
        /*
         * Transaction: TX_CREATE_INGREDIENT - Tạo nguyên liệu
         *
         * Flow:
         * 1. Validate tên nguyên liệu, đơn vị tính và mức tồn tối thiểu.
         * 2. Kiểm tra đơn vị tính có tồn tại trong DONVITINH không.
         * 3. Kiểm tra tên nguyên liệu có bị trùng không.
         * 4. Insert NGUYENLIEU với trạng thái ACTIVE.
         * 5. Query lại nguyên liệu vừa tạo để trả response.
         */

        String name = normalizeText(request == null ? null : request.getTenNguyenLieu());
        Long unitId = request == null ? null : request.getMaDonViTinh();
        BigDecimal minimumStock = request == null ? null : request.getMucTonToiThieu();

        validateIngredientInput(name, unitId, minimumStock);

        if (!ingredientRepository.existsUnit(unitId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn vị tính không tồn tại");
        }

        if (ingredientRepository.existsByName(name)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên nguyên liệu đã tồn tại");
        }

        Long id = ingredientRepository.insertIngredient(name, unitId, minimumStock, ACTIVE);

        return getIngredientById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public IngredientResponse updateIngredient(Long id, UpdateIngredientRequest request) {
        /*
         * Transaction: TX_UPDATE_INGREDIENT - Cập nhật nguyên liệu
         *
         * Flow:
         * 1. Kiểm tra nguyên liệu có tồn tại không.
         * 2. Validate tên nguyên liệu, đơn vị tính, mức tồn tối thiểu và trạng thái.
         * 3. Kiểm tra đơn vị tính có tồn tại không.
         * 4. Kiểm tra tên nguyên liệu mới có trùng nguyên liệu khác không.
         * 5. Update NGUYENLIEU.
         * 6. Query lại dữ liệu sau cập nhật.
         */

        validateId(id);

        if (!ingredientRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nguyên liệu");
        }

        String name = normalizeText(request == null ? null : request.getTenNguyenLieu());
        Long unitId = request == null ? null : request.getMaDonViTinh();
        BigDecimal minimumStock = request == null ? null : request.getMucTonToiThieu();
        String status = normalizeStatusRequired(request == null ? null : request.getTrangThai());

        validateIngredientInput(name, unitId, minimumStock);

        if (!ingredientRepository.existsUnit(unitId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn vị tính không tồn tại");
        }

        if (ingredientRepository.existsByNameExceptId(name, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên nguyên liệu đã tồn tại");
        }

        ingredientRepository.updateIngredient(id, name, unitId, minimumStock, status);

        return getIngredientById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public IngredientResponse updateStatus(Long id, String status) {
        /*
         * Transaction: TX_UPDATE_INGREDIENT_STATUS - Đổi trạng thái nguyên liệu
         *
         * Flow:
         * 1. Kiểm tra nguyên liệu có tồn tại không.
         * 2. Validate trạng thái chỉ nhận ACTIVE hoặc INACTIVE.
         * 3. Update NGUYENLIEU.trang_thai.
         * 4. Query lại dữ liệu sau cập nhật.
         */

        validateId(id);

        if (!ingredientRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nguyên liệu");
        }

        String normalizedStatus = normalizeStatusRequired(status);
        ingredientRepository.updateStatus(id, normalizedStatus);

        return getIngredientById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public IngredientResponse deactivateIngredient(Long id) {
        /*
         * Transaction: TX_DEACTIVATE_INGREDIENT - Ngưng hoạt động nguyên liệu
         *
         * Flow:
         * 1. Kiểm tra nguyên liệu có tồn tại không.
         * 2. Không xóa cứng vì nguyên liệu có thể đã phát sinh công thức, tồn kho,
         *    lô hàng, phiếu nhập, phiếu xuất và nhật ký kho.
         * 3. Chuyển trạng thái sang INACTIVE.
         */

        return updateStatus(id, INACTIVE);
    }

    private void validateIngredientInput(
            String name,
            Long unitId,
            BigDecimal minimumStock
    ) {
        if (name == null || name.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên nguyên liệu");
        }

        if (name.length() > 150) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên nguyên liệu không được vượt quá 150 ký tự");
        }

        if (unitId == null || unitId <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn đơn vị tính");
        }

        if (minimumStock == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập mức tồn tối thiểu");
        }

        if (minimumStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mức tồn tối thiểu không được âm");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã nguyên liệu không hợp lệ");
        }
    }

    private String normalizeStatusAllowNull(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return normalizeStatusRequired(status);
    }

    private String normalizeStatusRequired(String status) {
        String value = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);

        if (!ACTIVE.equals(value) && !INACTIVE.equals(value)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái nguyên liệu không hợp lệ");
        }

        return value;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}