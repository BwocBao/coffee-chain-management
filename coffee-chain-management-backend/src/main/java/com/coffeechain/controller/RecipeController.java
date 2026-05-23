package com.coffeechain.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.coffeechain.dto.request.CreateRecipeRequest;
import com.coffeechain.dto.request.UpdateRecipeRequest;
import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.response.RecipeDetailResponse;
import com.coffeechain.dto.response.RecipeLookupResponse;
import com.coffeechain.dto.response.RecipeSummaryResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quan ly cong thuc san pham", description = "Nhom API Quan ly cong thuc san pham. Swagger mo ta quyen can co, request, response va luong su dung chinh.")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private final AuthGuard authGuard;

    public RecipeController(RecipeService recipeService, AuthGuard authGuard) {
        this.recipeService = recipeService;
        this.authGuard = authGuard;
    }

    @Operation(summary = "Lấy dữ liệu combobox cho quản lý công thức")
    @GetMapping("/lookups")
    public ResponseEntity<BaseResponse<RecipeLookupResponse>> getLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requirePermission(authHeader, "RECIPE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy dữ liệu quản lý công thức thành công",
                recipeService.getLookups()
        ));
    }

    @Operation(summary = "Tra cứu danh sách công thức/sản phẩm")
    @GetMapping
    public ResponseEntity<BaseResponse<List<RecipeSummaryResponse>>> searchRecipes(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status
    ) {
        authGuard.requirePermission(authHeader, "RECIPE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Tra cứu công thức thành công",
                recipeService.searchRecipes(keyword, status)
        ));
    }

    @Operation(summary = "Xem chi tiết công thức và định giá")
    @GetMapping("/{maSanPham}")
    public ResponseEntity<BaseResponse<RecipeDetailResponse>> getRecipeDetail(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long maSanPham
    ) {
        authGuard.requirePermission(authHeader, "RECIPE:VIEW");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy chi tiết công thức thành công",
                recipeService.getRecipeDetail(maSanPham)
        ));
    }

    @Operation(summary = "Tạo sản phẩm và công thức mới")
    @PostMapping
    public ResponseEntity<BaseResponse<RecipeDetailResponse>> createRecipe(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateRecipeRequest request
    ) {
        authGuard.requirePermission(authHeader, "RECIPE:MANAGE");

        return ResponseEntity.ok(BaseResponse.created(
                "Tạo công thức thành công",
                recipeService.createRecipe(request)
        ));
    }

    @Operation(summary = "Cập nhật sản phẩm và thay công thức hiện hành")
    @PutMapping("/{maSanPham}")
    public ResponseEntity<BaseResponse<RecipeDetailResponse>> updateRecipe(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long maSanPham,
            @RequestBody UpdateRecipeRequest request
    ) {
        authGuard.requirePermission(authHeader, "RECIPE:MANAGE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Cập nhật công thức thành công",
                recipeService.updateRecipe(maSanPham, request)
        ));
    }

    @Operation(summary = "Xóa công thức hiện hành nhưng không xóa sản phẩm")
    @DeleteMapping("/{maSanPham}/formula")
    public ResponseEntity<BaseResponse<RecipeDetailResponse>> deleteFormula(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long maSanPham
    ) {
        authGuard.requirePermission(authHeader, "RECIPE:MANAGE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Xóa công thức hiện hành thành công",
                recipeService.deleteFormula(maSanPham)
        ));
    }

    @Operation(summary = "Ngừng bán sản phẩm nhưng vẫn giữ công thức")
    @PatchMapping("/{maSanPham}/stop-selling")
    public ResponseEntity<BaseResponse<RecipeDetailResponse>> stopSelling(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long maSanPham
    ) {
        authGuard.requirePermission(authHeader, "RECIPE:MANAGE");

        return ResponseEntity.ok(BaseResponse.ok(
                "Ngừng bán sản phẩm thành công",
                recipeService.stopSelling(maSanPham)
        ));
    }
}