package com.coffeechain.service;

import com.coffeechain.dto.request.CreateBranchRequest;
import com.coffeechain.dto.request.UpdateBranchRequest;
import com.coffeechain.dto.response.BranchResponse;
import com.coffeechain.dto.response.BranchStatisticsResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.BranchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class BranchService {
    private static final String ACTIVE = "ACTIVE";
    private static final String CLOSED = "CLOSED";
    private static final String MAINTENANCE = "MAINTENANCE";

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<BranchResponse> searchBranches(String keyword, String status) {
        String normalizedStatus = normalizeStatusAllowNull(status);
        return branchRepository.searchBranches(keyword, normalizedStatus);
    }

    public BranchResponse getBranchById(Long id) {
        validateId(id);

        return branchRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy chi nhánh"));
    }

    public BranchStatisticsResponse getStatistics() {
        return branchRepository.getStatistics();
    }

    @Transactional(rollbackFor = Exception.class)
    public BranchResponse createBranch(CreateBranchRequest request) {
        /*
         * Transaction: TX_CREATE_BRANCH - Tạo chi nhánh
         *
         * Flow:
         * 1. Validate tên chi nhánh, địa chỉ và số điện thoại.
         * 2. Kiểm tra tên chi nhánh có bị trùng không.
         * 3. Insert CHINHANH với trạng thái ACTIVE.
         * 4. Query lại chi nhánh vừa tạo.
         * 5. Return response.
         */

        String name = normalizeText(request == null ? null : request.getTenChiNhanh());
        String address = normalizeText(request == null ? null : request.getDiaChi());
        String phone = normalizeText(request == null ? null : request.getSoDienThoai());

        validateBranchInput(name, address, phone);

        if (branchRepository.existsByName(name)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên chi nhánh đã tồn tại");
        }

        Long id = branchRepository.insertBranch(name, address, phone, ACTIVE);

        return getBranchById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public BranchResponse updateBranch(Long id, UpdateBranchRequest request) {
        /*
         * Transaction: TX_UPDATE_BRANCH - Cập nhật chi nhánh
         *
         * Flow:
         * 1. Kiểm tra chi nhánh tồn tại.
         * 2. Validate tên chi nhánh, địa chỉ, số điện thoại và trạng thái.
         * 3. Kiểm tra tên mới có trùng với chi nhánh khác không.
         * 4. Update CHINHANH.
         * 5. Query lại dữ liệu sau cập nhật.
         */

        validateId(id);

        if (!branchRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy chi nhánh");
        }

        String name = normalizeText(request == null ? null : request.getTenChiNhanh());
        String address = normalizeText(request == null ? null : request.getDiaChi());
        String phone = normalizeText(request == null ? null : request.getSoDienThoai());
        String status = normalizeStatusRequired(request == null ? null : request.getTrangThai());

        validateBranchInput(name, address, phone);

        if (branchRepository.existsByNameExceptId(name, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên chi nhánh đã tồn tại");
        }

        branchRepository.updateBranch(id, name, address, phone, status);

        return getBranchById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public BranchResponse updateStatus(Long id, String status) {
        /*
         * Transaction: TX_UPDATE_BRANCH_STATUS - Cập nhật trạng thái chi nhánh
         *
         * Flow:
         * 1. Kiểm tra chi nhánh tồn tại.
         * 2. Validate trạng thái chỉ nhận ACTIVE, CLOSED hoặc MAINTENANCE.
         * 3. Update CHINHANH.trang_thai.
         * 4. Query lại dữ liệu sau cập nhật.
         */

        validateId(id);

        if (!branchRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy chi nhánh");
        }

        String normalizedStatus = normalizeStatusRequired(status);
        branchRepository.updateStatus(id, normalizedStatus);

        return getBranchById(id);
    }

    private void validateBranchInput(String name, String address, String phone) {
        if (name == null || name.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên chi nhánh");
        }

        if (name.length() > 150) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên chi nhánh không được vượt quá 150 ký tự");
        }

        if (address != null && address.length() > 255) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Địa chỉ không được vượt quá 255 ký tự");
        }

        if (phone != null && phone.length() > 30) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Số điện thoại không được vượt quá 30 ký tự");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã chi nhánh không hợp lệ");
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

        if (!ACTIVE.equals(value) && !CLOSED.equals(value) && !MAINTENANCE.equals(value)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái chi nhánh không hợp lệ");
        }

        return value;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}