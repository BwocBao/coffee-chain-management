package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreateIngredientRequest;
import com.coffeechain.dto.request.UpdateIngredientRequest;
import com.coffeechain.dto.request.UpdateIngredientStatusRequest;
import com.coffeechain.dto.response.IngredientLookupResponse;
import com.coffeechain.dto.response.IngredientResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Quan ly nguyen lieu",
    description =
        "Nhom API Quan ly nguyen lieu. Swagger mo ta quyen can co, request, response va luong su dung chinh.")
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
  private final IngredientService ingredientService;
  private final AuthGuard authGuard;

  public IngredientController(IngredientService ingredientService, AuthGuard authGuard) {
    this.ingredientService = ingredientService;
    this.authGuard = authGuard;
  }

  @Operation(
      summary = "Lấy danh sách nguyên liệu",
      description = "Tìm kiếm và lọc nguyên liệu theo từ khóa, trạng thái và đơn vị tính.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Không có quyền xem nguyên liệu",
        content = @Content)
  })
  @GetMapping
  public ResponseEntity<BaseResponse<List<IngredientResponse>>> searchIngredients(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "unitId", required = false) Long unitId) {
    authGuard.requirePermission(authHeader, "INGREDIENT:VIEW");

    return ResponseEntity.ok(
        BaseResponse.ok(
            "Lấy danh sách nguyên liệu thành công",
            ingredientService.searchIngredients(keyword, status, unitId)));
  }

  @Operation(
      summary = "Lấy dữ liệu combobox cho màn quản lý nguyên liệu",
      description = "Trả về danh sách đơn vị tính để frontend render combobox.")
  @GetMapping("/lookups")
  public ResponseEntity<BaseResponse<IngredientLookupResponse>> getIngredientLookups(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader) {
    authGuard.requirePermission(authHeader, "INGREDIENT:VIEW");

    return ResponseEntity.ok(
        BaseResponse.ok(
            "Lấy dữ liệu quản lý nguyên liệu thành công",
            ingredientService.getIngredientLookups()));
  }

  @Operation(summary = "Lấy chi tiết nguyên liệu")
  @GetMapping("/{id}")
  public ResponseEntity<BaseResponse<IngredientResponse>> getIngredientById(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long id) {
    authGuard.requirePermission(authHeader, "INGREDIENT:VIEW");

    return ResponseEntity.ok(
        BaseResponse.ok(
            "Lấy chi tiết nguyên liệu thành công", ingredientService.getIngredientById(id)));
  }

  @Operation(summary = "Tạo nguyên liệu")
  @PostMapping
  public ResponseEntity<BaseResponse<IngredientResponse>> createIngredient(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @RequestBody CreateIngredientRequest request) {
    authGuard.requirePermission(authHeader, "INGREDIENT:CREATE");

    return ResponseEntity.ok(
        BaseResponse.created(
            "Tạo nguyên liệu thành công", ingredientService.createIngredient(request)));
  }

  @Operation(summary = "Cập nhật nguyên liệu")
  @PutMapping("/{id}")
  public ResponseEntity<BaseResponse<IngredientResponse>> updateIngredient(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long id,
      @RequestBody UpdateIngredientRequest request) {
    authGuard.requirePermission(authHeader, "INGREDIENT:UPDATE");

    return ResponseEntity.ok(
        BaseResponse.ok(
            "Cập nhật nguyên liệu thành công", ingredientService.updateIngredient(id, request)));
  }

  @Operation(summary = "Cập nhật trạng thái nguyên liệu")
  @PatchMapping("/{id}/status")
  public ResponseEntity<BaseResponse<IngredientResponse>> updateIngredientStatus(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long id,
      @RequestBody UpdateIngredientStatusRequest request) {
    authGuard.requirePermission(authHeader, "INGREDIENT:UPDATE");

    return ResponseEntity.ok(
        BaseResponse.ok(
            "Cập nhật trạng thái nguyên liệu thành công",
            ingredientService.updateStatus(id, request == null ? null : request.getTrangThai())));
  }

  @Operation(
      summary = "Ngưng hoạt động nguyên liệu",
      description = "Không xóa cứng nguyên liệu, chỉ chuyển trạng thái sang INACTIVE.")
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseResponse<IngredientResponse>> deactivateIngredient(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long id) {
    authGuard.requirePermission(authHeader, "INGREDIENT:DELETE");

    return ResponseEntity.ok(
        BaseResponse.ok(
            "Ngưng hoạt động nguyên liệu thành công", ingredientService.deactivateIngredient(id)));
  }
}
