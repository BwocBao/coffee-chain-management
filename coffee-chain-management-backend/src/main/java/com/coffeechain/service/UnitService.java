package com.coffeechain.service;

import com.coffeechain.dto.request.CreateUnitRequest;
import com.coffeechain.dto.request.UpdateUnitRequest;
import com.coffeechain.dto.response.UnitResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.UnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitService {
    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    public List<UnitResponse> searchUnits(String keyword) {
        return unitRepository.searchUnits(keyword);
    }

    public UnitResponse getUnitById(Long id) {
        validateId(id);

        return unitRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn vị tính"));
    }

    @Transactional(rollbackFor = Exception.class)
    public UnitResponse createUnit(CreateUnitRequest request) {
        /*
         * Transaction: TX_CREATE_UNIT - Tạo đơn vị tính
         *
         * Flow:
         * 1. Validate tên đơn vị tính và ký hiệu.
         * 2. Kiểm tra ký hiệu đơn vị tính đã tồn tại chưa.
         * 3. Insert DONVITINH.
         * 4. Query lại đơn vị tính vừa tạo.
         * 5. Return response.
         */

        String name = normalizeText(request == null ? null : request.getTenDonViTinh());
        String symbol = normalizeText(request == null ? null : request.getKyHieu());

        validateUnitInput(name, symbol);

        if (unitRepository.existsBySymbol(symbol)) {
            throw new AppException(HttpStatus.CONFLICT, "Ký hiệu đơn vị tính đã tồn tại");
        }

        Long id = unitRepository.insertUnit(name, symbol);

        return getUnitById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public UnitResponse updateUnit(Long id, UpdateUnitRequest request) {
        /*
         * Transaction: TX_UPDATE_UNIT - Cập nhật đơn vị tính
         *
         * Flow:
         * 1. Kiểm tra đơn vị tính có tồn tại không.
         * 2. Validate tên đơn vị tính và ký hiệu.
         * 3. Kiểm tra ký hiệu mới có bị trùng với đơn vị tính khác không.
         * 4. Update DONVITINH.
         * 5. Query lại dữ liệu sau cập nhật.
         * 6. Return response.
         */

        validateId(id);

        if (!unitRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn vị tính");
        }

        String name = normalizeText(request == null ? null : request.getTenDonViTinh());
        String symbol = normalizeText(request == null ? null : request.getKyHieu());

        validateUnitInput(name, symbol);

        if (unitRepository.existsBySymbolExceptId(symbol, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Ký hiệu đơn vị tính đã tồn tại");
        }

        unitRepository.updateUnit(id, name, symbol);

        return getUnitById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUnit(Long id) {
        /*
         * Transaction: TX_DELETE_UNIT - Xóa đơn vị tính
         *
         * Flow:
         * 1. Kiểm tra đơn vị tính có tồn tại không.
         * 2. Kiểm tra đơn vị tính đã được nguyên liệu sử dụng chưa.
         * 3. Nếu đã có nguyên liệu dùng đơn vị này thì không cho xóa.
         * 4. Nếu chưa được dùng thì DELETE khỏi DONVITINH.
         *
         * Lý do:
         * - NGUYENLIEU đang tham chiếu DONVITINH bằng ma_don_vi_tinh.
         * - Nếu xóa đơn vị đã dùng, dữ liệu nguyên liệu sẽ bị mất liên kết.
         */

        validateId(id);

        if (!unitRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn vị tính");
        }

        if (unitRepository.hasIngredient(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn vị tính đã được nguyên liệu sử dụng, không thể xóa");
        }

        unitRepository.deleteUnit(id);
    }

    private void validateUnitInput(String name, String symbol) {
        if (name == null || name.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên đơn vị tính");
        }

        if (name.length() > 50) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên đơn vị tính không được vượt quá 50 ký tự");
        }

        if (symbol == null || symbol.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập ký hiệu đơn vị tính");
        }

        if (symbol.length() > 20) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ký hiệu đơn vị tính không được vượt quá 20 ký tự");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã đơn vị tính không hợp lệ");
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}