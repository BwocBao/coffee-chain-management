package com.coffeechain.service;

import com.coffeechain.dto.request.CreateRecipeRequest;
import com.coffeechain.dto.request.RecipeIngredientRequest;
import com.coffeechain.dto.request.UpdateRecipeRequest;
import com.coffeechain.dto.response.RecipeDetailResponse;
import com.coffeechain.dto.response.RecipeIngredientLineResponse;
import com.coffeechain.dto.response.RecipeLookupResponse;
import com.coffeechain.dto.response.RecipeSummaryResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RecipeService {
    private static final Set<String> PRODUCT_STATUSES = Set.of(
            "AVAILABLE",
            "OUT_OF_STOCK",
            "STOP_SELLING"
    );

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public RecipeLookupResponse getLookups() {
        return new RecipeLookupResponse(
                recipeRepository.findIngredientOptions(),
                List.of(
                        new RecipeLookupResponse.StatusOption("AVAILABLE", "Có sẵn"),
                        new RecipeLookupResponse.StatusOption("OUT_OF_STOCK", "Không có sẵn"),
                        new RecipeLookupResponse.StatusOption("STOP_SELLING", "Ngừng bán")
                )
        );
    }

    public List<RecipeSummaryResponse> searchRecipes(String keyword, String status) {
        String normalizedStatus = normalizeStatusAllowNull(status);
        return recipeRepository.searchRecipes(keyword, normalizedStatus);
    }

    public RecipeDetailResponse getRecipeDetail(Long maSanPham) {
        validateId(maSanPham, "Mã sản phẩm không hợp lệ");

        RecipeSummaryResponse summary = recipeRepository.findRecipeSummaryById(maSanPham);

        if (summary == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm/công thức");
        }

        List<RecipeIngredientLineResponse> items = recipeRepository.findRecipeItems(maSanPham);
        BigDecimal totalCost = calculateTotalCost(items);
        BigDecimal margin = calculateMargin(summary.giaBanHienTai(), totalCost);
        String image = recipeRepository.findProductImage(maSanPham);

        return new RecipeDetailResponse(
                summary.maSanPham(),
                summary.maCongThucHienThi(),
                summary.tenSanPham(),
                image,
                summary.giaBanHienTai(),
                summary.trangThai(),
                summary.ngayTao(),
                items,
                totalCost,
                margin
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public RecipeDetailResponse createRecipe(CreateRecipeRequest request) {
        /*
         * Flow:
         * 1. Validate tên sản phẩm, giá bán, trạng thái, danh sách nguyên liệu.
         * 2. Kiểm tra tên sản phẩm không bị trùng.
         * 3. Insert SANPHAM.
         * 4. Insert nhiều dòng CONGTHUC_SANPHAM.
         * 5. Return chi tiết công thức vừa tạo.
         */

        validateCreateRequest(request);

        String name = normalizeName(request.tenSanPham());
        String status = normalizeStatusRequired(request.trangThai(), "AVAILABLE");

        if (recipeRepository.productNameExists(name, null)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên sản phẩm đã tồn tại");
        }

        Long maSanPham = recipeRepository.insertProduct(
                name,
                normalizeText(request.hinhAnh()),
                request.giaBanHienTai(),
                status
        );

        insertFormulaItems(maSanPham, request.items());

        return getRecipeDetail(maSanPham);
    }

    @Transactional(rollbackFor = Exception.class)
    public RecipeDetailResponse updateRecipe(Long maSanPham, UpdateRecipeRequest request) {
        /*
         * Flow:
         * 1. Validate sản phẩm tồn tại.
         * 2. Validate thông tin sản phẩm và danh sách công thức mới.
         * 3. Update SANPHAM.
         * 4. Delete công thức hiện hành trong CONGTHUC_SANPHAM.
         * 5. Insert lại danh sách nguyên liệu mới.
         * 6. Return chi tiết sau cập nhật.
         */

        validateId(maSanPham, "Mã sản phẩm không hợp lệ");

        if (!recipeRepository.productExists(maSanPham)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        }

        validateUpdateRequest(request);

        String name = normalizeName(request.tenSanPham());
        String status = normalizeStatusRequired(request.trangThai(), "AVAILABLE");

        if (recipeRepository.productNameExists(name, maSanPham)) {
            throw new AppException(HttpStatus.CONFLICT, "Tên sản phẩm đã tồn tại");
        }

        recipeRepository.updateProduct(
                maSanPham,
                name,
                normalizeText(request.hinhAnh()),
                request.giaBanHienTai(),
                status
        );

        recipeRepository.deleteFormulaByProduct(maSanPham);
        insertFormulaItems(maSanPham, request.items());

        return getRecipeDetail(maSanPham);
    }

    @Transactional(rollbackFor = Exception.class)
    public RecipeDetailResponse deleteFormula(Long maSanPham) {
        /*
         * Flow:
         * 1. Không xóa SANPHAM.
         * 2. Xóa công thức hiện hành trong CONGTHUC_SANPHAM.
         * 3. Chuyển SANPHAM.trang_thai = STOP_SELLING vì sản phẩm không còn công thức để bán.
         */

        validateId(maSanPham, "Mã sản phẩm không hợp lệ");

        if (!recipeRepository.productExists(maSanPham)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        }

        recipeRepository.deleteFormulaByProduct(maSanPham);
        recipeRepository.updateProductStatus(maSanPham, "STOP_SELLING");

        return getRecipeDetail(maSanPham);
    }

    @Transactional(rollbackFor = Exception.class)
    public RecipeDetailResponse stopSelling(Long maSanPham) {
        /*
         * Flow:
         * 1. Không xóa công thức.
         * 2. Chỉ chuyển trạng thái sản phẩm thành STOP_SELLING.
         * 3. Dùng khi muốn ngừng bán nhưng vẫn giữ công thức để xem/mở lại sau.
         */

        validateId(maSanPham, "Mã sản phẩm không hợp lệ");

        if (!recipeRepository.productExists(maSanPham)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        }

        recipeRepository.updateProductStatus(maSanPham, "STOP_SELLING");
        return getRecipeDetail(maSanPham);
    }

    private void insertFormulaItems(Long maSanPham, List<RecipeIngredientRequest> items) {
        Set<Long> usedIngredients = new HashSet<>();

        for (RecipeIngredientRequest item : items) {
            Long maNguyenLieu = item.maNguyenLieu();

            if (!usedIngredients.add(maNguyenLieu)) {
                throw new AppException(HttpStatus.CONFLICT, "Một nguyên liệu không được lặp lại trong cùng công thức");
            }

            if (!recipeRepository.ingredientExists(maNguyenLieu)) {
                throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nguyên liệu: " + maNguyenLieu);
            }

            recipeRepository.insertFormulaItem(
                    maSanPham,
                    maNguyenLieu,
                    item.soLuongCan()
            );
        }
    }

    private void validateCreateRequest(CreateRecipeRequest request) {
        if (request == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu công thức");
        }

        validateProductFields(request.tenSanPham(), request.giaBanHienTai(), request.trangThai());
        validateFormulaItems(request.items());
    }

    private void validateUpdateRequest(UpdateRecipeRequest request) {
        if (request == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu công thức");
        }

        validateProductFields(request.tenSanPham(), request.giaBanHienTai(), request.trangThai());
        validateFormulaItems(request.items());
    }

    private void validateProductFields(String tenSanPham, BigDecimal giaBan, String trangThai) {
        if (tenSanPham == null || tenSanPham.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên sản phẩm không được để trống");
        }

        if (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Giá bán không hợp lệ");
        }

        normalizeStatusRequired(trangThai, "AVAILABLE");
    }

    private void validateFormulaItems(List<RecipeIngredientRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Công thức phải có ít nhất một nguyên liệu");
        }

        for (RecipeIngredientRequest item : items) {
            if (item == null || item.maNguyenLieu() == null || item.maNguyenLieu() <= 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng chọn nguyên liệu");
            }

            if (item.soLuongCan() == null || item.soLuongCan().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Định mức nguyên liệu phải lớn hơn 0");
            }
        }
    }

    private String normalizeStatusAllowNull(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return normalizeStatusRequired(status, null);
    }

    private String normalizeStatusRequired(String status, String defaultValue) {
        String value = status == null || status.isBlank()
                ? defaultValue
                : status.trim().toUpperCase(Locale.ROOT);

        if (value == null || !PRODUCT_STATUSES.contains(value)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái sản phẩm không hợp lệ");
        }

        return value;
    }

    private String normalizeName(String value) {
        return value.trim();
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private BigDecimal calculateTotalCost(List<RecipeIngredientLineResponse> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (RecipeIngredientLineResponse item : items) {
            if (item.thanhTien() != null) {
                total = total.add(item.thanhTien());
            }
        }

        return total;
    }

    private BigDecimal calculateMargin(BigDecimal sellingPrice, BigDecimal totalCost) {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return sellingPrice
                .subtract(totalCost)
                .multiply(BigDecimal.valueOf(100))
                .divide(sellingPrice, 2, RoundingMode.HALF_UP);
    }
}