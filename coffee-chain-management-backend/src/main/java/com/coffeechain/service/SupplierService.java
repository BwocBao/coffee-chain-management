package com.coffeechain.service;


import com.coffeechain.dto.request.CreateSupplierRequest;
import com.coffeechain.dto.request.UpdateSupplierRequest;
import com.coffeechain.dto.response.SupplierResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierResponse> searchSuppliers(String keyword) {
        return supplierRepository.searchSuppliers(keyword);
    }

    public SupplierResponse getSupplierById(Long id) {
        validateId(id);

        return supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp"));
    }

    @Transactional(rollbackFor = Exception.class)
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        /*
         * Transaction: TX_CREATE_SUPPLIER - Tạo nhà cung cấp
         *
         * Flow:
         * 1. Validate tên nhà cung cấp, số điện thoại, email, địa chỉ.
         * 2. Kiểm tra tên nhà cung cấp đã tồn tại chưa.
         * 3. Insert NHACUNGCAP.
         * 4. Query lại nhà cung cấp vừa tạo.
         * 5. Return response.
         */

        String name = normalizeText(request == null ? null : request.getTenNhaCungCap());
        String phone = normalizeText(request == null ? null : request.getSoDienThoai());
        String email = normalizeEmail(request == null ? null : request.getEmail());
        String address = normalizeText(request == null ? null : request.getDiaChi());

        validateSupplierInput(name, phone, email, address);

        if (supplierRepository.existsByName(name)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên nhà cung cấp đã tồn tại");
        }

        Long id = supplierRepository.insertSupplier(name, phone, email, address);

        return getSupplierById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        /*
         * Transaction: TX_UPDATE_SUPPLIER - Cập nhật nhà cung cấp
         *
         * Flow:
         * 1. Kiểm tra nhà cung cấp có tồn tại không.
         * 2. Validate dữ liệu cập nhật.
         * 3. Kiểm tra tên nhà cung cấp có bị trùng với nhà cung cấp khác không.
         * 4. Update NHACUNGCAP.
         * 5. Query lại dữ liệu sau cập nhật.
         * 6. Return response.
         */

        validateId(id);

        if (!supplierRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp");
        }

        String name = normalizeText(request == null ? null : request.getTenNhaCungCap());
        String phone = normalizeText(request == null ? null : request.getSoDienThoai());
        String email = normalizeEmail(request == null ? null : request.getEmail());
        String address = normalizeText(request == null ? null : request.getDiaChi());

        validateSupplierInput(name, phone, email, address);

        if (supplierRepository.existsByNameExceptId(name, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên nhà cung cấp đã tồn tại");
        }

        supplierRepository.updateSupplier(id, name, phone, email, address);

        return getSupplierById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long id) {
        /*
         * Transaction: TX_DELETE_SUPPLIER - Xóa nhà cung cấp
         *
         * Flow:
         * 1. Kiểm tra nhà cung cấp có tồn tại không.
         * 2. Kiểm tra nhà cung cấp đã phát sinh PHIEUNHAP chưa.
         * 3. Nếu đã phát sinh phiếu nhập thì không cho xóa.
         * 4. Nếu chưa phát sinh phiếu nhập thì DELETE khỏi NHACUNGCAP.
         *
         * Lý do:
         * - NHACUNGCAP đang được PHIEUNHAP tham chiếu.
         * - Nếu xóa nhà cung cấp đã có lịch sử nhập kho thì sẽ mất dữ liệu lịch sử.
         */

        validateId(id);

        if (!supplierRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp");
        }

        if (supplierRepository.hasImportReceipt(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Nhà cung cấp đã có phiếu nhập, không thể xóa");
        }

        supplierRepository.deleteSupplier(id);
    }

    private void validateSupplierInput(
            String name,
            String phone,
            String email,
            String address
    ) {
        if (name == null || name.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên nhà cung cấp");
        }

        if (name.length() > 150) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên nhà cung cấp không được vượt quá 150 ký tự");
        }

        if (phone != null && phone.length() > 30) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Số điện thoại không được vượt quá 30 ký tự");
        }

        if (email != null && !email.isBlank()) {
            if (!email.contains("@") || email.length() > 100) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Email không hợp lệ");
            }
        }

        if (address != null && address.length() > 255) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Địa chỉ không được vượt quá 255 ký tự");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mã nhà cung cấp không hợp lệ");
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}